package jam.data.control;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jam.data.Monitor;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GoodThread;
import jam.sort.ThreadPriorities;
import jam.ui.Canceller;
import jam.ui.WindowCancelAction;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.logging.Level;

import static javax.swing.SwingConstants.*;

/**
 * Reads and displays the monitors.
 * 
 * @version September 2000
 * @author Ken Swartz
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @since JDK1.1
 */
@Singleton
public final class MonitorControl extends AbstractControl implements Runnable {

	private static final int THRESHOLD_INDEX = 1;
	private static final int MAX_VALUE_INDEX = 2;
	private static final int CHECKBOX_INDEX = 3;

	private class BroadcastMonitorUpdate implements Runnable {
		public void run() {
			broadcaster.broadcast(BroadcastEvent.Command.MONITORS_UPDATE);
		}
	}

	private transient final BroadcastMonitorUpdate monitorUpdate = new BroadcastMonitorUpdate();

	private static final int BORDER_HEIGHT = 5;

	private transient boolean configured = false; // monitors have been
	// configured

	private transient int interval; // update interval

	private transient GoodThread loopThread; // loop to update monitors

	private transient final JPanel pMonitors;

	private transient final JSpinner spinnerUpdate;

	@Inject
	MonitorControl(final Frame frame, final Broadcaster broadcaster) {
		super(frame, "Monitors Setup", false, broadcaster);
		setResizable(true);
		setLocation(20, 50);
		final Container cddisp = getContentPane();
		cddisp.setLayout(new BorderLayout());
		/* Panel with column titles */
		final JPanel pUpper = new JPanel(
				new FlowLayout(FlowLayout.RIGHT, 12, 5));
		Border border = new EmptyBorder(10, 0, 5, 5);
		pUpper.setBorder(border);
		cddisp.add(pUpper, BorderLayout.NORTH);
		/* Add labels of columns */
		pUpper.add(new JLabel("Name", CENTER));
		pUpper.add(new JLabel("Threshold", CENTER));
		pUpper.add(new JLabel("Maximum", CENTER));
		pUpper.add(new JLabel("Alarm", LEFT));
		/* Panel to fill in with monitors */
		pMonitors = new JPanel(new GridLayout(0, 1, 5, BORDER_HEIGHT));
		border = new EmptyBorder(BORDER_HEIGHT, 0, BORDER_HEIGHT, 0);
		pMonitors.setBorder(border);
		// Scroll Panel
		final JScrollPane scrollPane = new JScrollPane(pMonitors);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		cddisp.add(scrollPane, BorderLayout.CENTER);
		/* Lower panel has widgets that do not change */
		final JPanel pLower = new JPanel(new GridLayout(0, 1, 0, 0));
		cddisp.add(pLower, BorderLayout.SOUTH);
		/* panel for update time */
		final JPanel pupdate = new JPanel();
		pupdate.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		pLower.add(pupdate);
		pupdate.add(new JLabel("Update every", RIGHT));
		final Integer one = 1;
		final Integer init = 1;
		spinnerUpdate = new JSpinner(new SpinnerNumberModel(init, one, null,
				one));
		spinnerUpdate.setPreferredSize(new Dimension(50, spinnerUpdate
				.getPreferredSize().height));
		pupdate.add(spinnerUpdate);
		pupdate.add(new JLabel("sec", LEFT));
		/* panel for buttons */
		final JPanel pbutton = new JPanel(new GridLayout(1, 0, 5, 5));
		pLower.add(pbutton);
		final JButton brecall = new JButton("Recall");
		brecall.addActionListener(event -> recall());
		pbutton.add(brecall);
		final JButton bok = new JButton("OK");
		bok.addActionListener(event -> {
            configure();
            startLoopThread();
            dispose();
        });
		pbutton.add(bok);
		final JButton bapply = new JButton("Apply");
		bapply.addActionListener(event -> {
            configure();
            startLoopThread();
        });
		pbutton.add(bapply);
		final Canceller canceller = () -> {
            configured = false;
            /* stop monitor thread if running */
            if (loopThread != null) {
                loopThread.setState(GoodThread.State.STOP);
            }
        };
		final JButton bcancel = new JButton(new WindowCancelAction(canceller));
		pbutton.add(bcancel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		/* setup monitors */
		doSetup();
	}

	/**
	 * Configure the monitors, i.e. set the values their parameters according to
	 * the input fields.
	 */
	protected void configure() {
		try {
			/* set update interval */
			interval = (Integer) spinnerUpdate.getValue();
			if (interval < 1) {
				throw new IllegalArgumentException(
						"Update interval must be greater than 1");
			}

			Monitor.setInterval(interval);

			/* get the Monitor parameters */
			final List<Monitor> monitors = Monitor.getMonitorList();
			for (int i = 0; i < monitors.size(); i++) {
				final JPanel MonitorPanel = (JPanel) pMonitors.getComponent(i);
				final JTextField textThreshold = (JTextField) MonitorPanel
						.getComponent(MonitorControl.THRESHOLD_INDEX);
				final JTextField textMaximum = (JTextField) MonitorPanel
						.getComponent(MonitorControl.MAX_VALUE_INDEX);
				final JCheckBox checkAlarm = (JCheckBox) MonitorPanel
						.getComponent(MonitorControl.CHECKBOX_INDEX);
				final double threshold = Double.parseDouble(textThreshold
						.getText().trim());
				final Monitor monitor = monitors.get(i);
				monitor.setThreshold(threshold);
				final double maximum = Double.parseDouble(textMaximum.getText()
						.trim());
				monitor.setMaximum(maximum);
				monitor.setAlarm(checkAlarm.isSelected());
			}
		} catch (NumberFormatException nfe) {
			LOGGER.log(Level.SEVERE, "Invalid number input.", nfe);
		}

		configured = true;
	}

	/**
	 * Setup all monitors.
	 */
	@Override
	public void doSetup() {
		int numberMonitors;
		final int cols = 6;
		JPanel pRow = null;
		numberMonitors = Monitor.getMonitorList().size();
		/* Clear panel */
		pMonitors.removeAll();
		/*
		 * for each monitor make a panel with label, threshold, maximum, and
		 * alarm
		 */
		for (Monitor monitor : Monitor.getMonitorList()) {
			pRow = updateMonitor(cols, monitor);
		}
		pack();
		if (numberMonitors > 0) {
			final Dimension dialogDim = calculateScrollDialogSize(this, pRow,
					BORDER_HEIGHT, numberMonitors);
			setSize(dialogDim);
		}

	}

	/**
	 * Stop monitors interval updating loop
	 */
	private void loopThreadStopped() {
		loopThread = null; // NOPMD
		for (Monitor monitor : Monitor.getMonitorList()) {
			monitor.reset();
		}
		broadcaster.broadcast(BroadcastEvent.Command.MONITORS_DISABLED);
	}

	/**
	 * Recall the monitor's parameters and set the input fields.
	 */
	protected void recall() {
		/* update interval */
		spinnerUpdate.setValue(interval);
		/* get the Monitor parameters */
		final List<Monitor> monitors = Monitor.getMonitorList();
		for (int i = 0; i < monitors.size(); i++) {
			final JPanel monitorPanel = (JPanel) pMonitors.getComponent(i);
			final JTextField textThreshold = (JTextField) monitorPanel
					.getComponent(THRESHOLD_INDEX);
			final JTextField textMaximum = (JTextField) monitorPanel
					.getComponent(MAX_VALUE_INDEX);
			final JCheckBox checkAlarm = (JCheckBox) monitorPanel
					.getComponent(CHECKBOX_INDEX);
			final Monitor monitor = monitors.get(i);
			textThreshold.setText(String.valueOf(monitor.getThreshold()));
			textMaximum.setText(String.valueOf(monitor.getMaximum()));
			checkAlarm.setSelected(monitor.isAlarmActivated());
		}
	}

	/**
	 * method to run and update monitors
	 * 
	 */
	public void run() {
		try {
			while (loopThread.checkState()) { // loop until stopped
				updatePeriodically();
			} // loop until stopped
		} catch (InterruptedException ie) {
			LOGGER.log(Level.SEVERE, "Monitor Interupted", ie);
		}
		loopThreadStopped();
	}

	/**
	 * Start monitors interval updating loop.
	 * 
	 * @throws IllegalStateException
	 *             if the monitors aren't configured yet
	 */
	private void startLoopThread() {
		if (configured) {
			if (loopThread == null) {
				loopThread = new GoodThread(this);// defaults to RUN state
				loopThread.setPriority(ThreadPriorities.MESSAGING);
				loopThread.setName("Monitor Thread");
				loopThread.setDaemon(true);
				loopThread.start();
			}
			broadcaster.broadcast(BroadcastEvent.Command.MONITORS_ENABLED);
		} else {
			throw new IllegalStateException(getClass().getName() + ".start(): "
					+ "called before the monitors were configured.");
		}
	}

	private JPanel updateMonitor(final int index, final Monitor monitor) {
		JPanel pRow;
		pRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		pMonitors.add(pRow);
		final JLabel labelConfig = new JLabel(monitor.getName(), CENTER);
		final JPanel pLabel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		pLabel.add(labelConfig);
		pRow.add(pLabel);
		final JTextField textThreshold = new JTextField();
		textThreshold.setColumns(index);
		textThreshold.setEditable(true);
		final int DEFAULT_THRESHOLD = 10;
		textThreshold.setText(Integer.toString(DEFAULT_THRESHOLD));
		pRow.add(textThreshold);
		final JTextField textMaximum = new JTextField();
		textMaximum.setColumns(index);
		textMaximum.setEditable(true);
		final int DEFAULT_MAX = 100;
		textMaximum.setText(Integer.toString(DEFAULT_MAX));
		pRow.add(textMaximum);
		final JCheckBox checkAlarm = new JCheckBox();
		checkAlarm.setSelected(false);
		pRow.add(checkAlarm);
		return pRow;
	}

	private void updatePeriodically() throws InterruptedException {
		final int waitForResults = 500;
		final int waitAfterRepaint = interval * 1000 - waitForResults;

		/* read scalers and wait */
		broadcaster.broadcast(BroadcastEvent.Command.SCALERS_READ);
		Thread.sleep(waitForResults);

		// loop for each monitor
		for (Monitor monitor : Monitor.getMonitorList()) {
			monitor.update();// update the monitor
		} // end loop monitors

		/* Broadcast event on UI thread */
		SwingUtilities.invokeLater(monitorUpdate);
		Thread.sleep(waitAfterRepaint);
	}
}
