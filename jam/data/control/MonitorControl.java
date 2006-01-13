package jam.data.control;

import static javax.swing.SwingConstants.CENTER;
import static javax.swing.SwingConstants.LEFT;
import static javax.swing.SwingConstants.RIGHT;
import jam.data.DataException;
import jam.data.Monitor;
import jam.global.BroadcastEvent;
import jam.global.GoodThread;
import jam.sort.ThreadPriorities;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Reads and displays the monitors.
 * 
 * @version September 2000
 * @author Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since JDK1.1
 */
public final class MonitorControl extends AbstractControl implements Runnable {

	private final int DEFAULT_MAX = 100;

	private final int DEFAULT_THRESHOLD = 10;

	// widgets for configuration
	private final JPanel pUpper;

	private final JPanel pMonitors;

	private final JSpinner spinnerUpdate;

	private final int borderHeight = 5;

	private int interval; // update interval

	private GoodThread loopThread; // loop to update monitors

	private boolean configured = false; // monitors have been configured

	private static MonitorControl mc = null;

	/**
	 * @return the only instance of this class
	 */
	static public MonitorControl getSingletonInstance() {
		if (mc == null) {
			mc = new MonitorControl();
		}
		return mc;
	}

	MonitorControl() {
		super("Monitors Setup", false);
		setResizable(true);
		setLocation(20, 50);
		final Container cddisp = getContentPane();
		cddisp.setLayout(new BorderLayout());
		/* Panel with column titles */
		pUpper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 5));
		Border border = new EmptyBorder(10, 0, 5, 5);
		pUpper.setBorder(border);
		cddisp.add(pUpper, BorderLayout.NORTH);
		/* Add labels of columns */
		pUpper.add(new JLabel("Name", CENTER));
		pUpper.add(new JLabel("Threshold", CENTER));
		pUpper.add(new JLabel("Maximum", CENTER));
		pUpper.add(new JLabel("Alarm", LEFT));
		/* Panel to fill in with monitors */
		pMonitors = new JPanel(new GridLayout(0, 1, 5, borderHeight));
		border = new EmptyBorder(borderHeight, 0, borderHeight, 0);
		pMonitors.setBorder(border);
		// Scroll Panel
		JScrollPane scrollPane = new JScrollPane(pMonitors);
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
		final Integer one = new Integer(1);
		final Integer init = new Integer(1);
		spinnerUpdate = new JSpinner(new SpinnerNumberModel(init, one, null,
				one));
		spinnerUpdate.setPreferredSize(new Dimension(50, spinnerUpdate
				.getPreferredSize().height));
		pupdate.add(spinnerUpdate);
		pupdate.add(new JLabel("sec", LEFT));
		/* panel for buttons */
		final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER, 5,
				5));
		pLower.add(pbutton);
		JPanel pb = new JPanel(new GridLayout(1, 0, 5, 5));
		pbutton.add(pb);
		final JButton brecall = new JButton("Recall");
		brecall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				recall();
			}
		});
		pb.add(brecall);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				configure();
				startLoopThread();
				dispose();
			}
		});
		pb.add(bok);
		final JButton bapply = new JButton("Apply");
		bapply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				configure();
				startLoopThread();
			}
		});
		pb.add(bapply);
		final JButton bcancel = new JButton("Cancel");
		bcancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				configured = false;
				/* stop monitor thread if running */
				if (loopThread != null) {
					loopThread.setState(GoodThread.State.STOP);
				}
			}
		});
		pb.add(bcancel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		/* setup monitors */
		doSetup();
	}

	/**
	 * Setup all monitors.
	 */
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
			pRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
			pMonitors.add(pRow);
			final JLabel labelConfig = new JLabel(monitor.getName(), CENTER);
			JPanel pLabel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			pLabel.add(labelConfig);
			pRow.add(pLabel);
			final JTextField textThreshold = new JTextField();
			textThreshold.setColumns(cols);
			textThreshold.setEditable(true);
			textThreshold.setText("" + DEFAULT_THRESHOLD);
			pRow.add(textThreshold);
			final JTextField textMaximum = new JTextField();
			textMaximum.setColumns(cols);
			textMaximum.setEditable(true);
			textMaximum.setText("" + DEFAULT_MAX);
			pRow.add(textMaximum);
			final JCheckBox checkAlarm = new JCheckBox();
			checkAlarm.setSelected(false);
			pRow.add(checkAlarm);
		}
		pack();
		if (numberMonitors > 0) {
			Dimension dialogDim = calculateScrollDialogSize(this, pRow,
					borderHeight, numberMonitors);
			setSize(dialogDim);
		}

	}

	/**
	 * Recall the monitor's parameters and set the input fields.
	 */
	void recall() {
		/* update interval */
		spinnerUpdate.setValue(new Integer(interval));
		final Iterator mList = Monitor.getMonitorList().iterator();
		/* get the Monitor parameters */
		int base = 0;
		while (mList.hasNext()) {
			final Monitor monitor = (Monitor) mList.next();
			base += 4;
			final JTextField textThreshold = (JTextField) pMonitors
					.getComponent(base + 1);
			final JTextField textMaximum = (JTextField) pMonitors
					.getComponent(base + 2);
			final JCheckBox checkAlarm = (JCheckBox) pMonitors
					.getComponent(base + 3);
			textThreshold.setText(String.valueOf(monitor.getThreshold()));
			textMaximum.setText(String.valueOf(monitor.getMaximum()));
			checkAlarm.setSelected(monitor.getAlarm());
		}
	}

	/**
	 * Configure the monitors, i.e. set the values their parameters according to
	 * the input fields.
	 * 
	 * @throws DataException
	 *             for invalid number input
	 */
	void configure() {
		try {
			// set update interval
			interval = ((Integer) spinnerUpdate.getValue()).intValue();
			if (interval < 1) {
				throw new IllegalArgumentException(
						"Update interval must be greater than 1");
			}
			Monitor.setInterval(interval);
			// set Monitor parameters
			final Iterator it = Monitor.getMonitorList().iterator();
			/* get the Monitor parameters */
			int base = 0;
			while (it.hasNext()) {
				base += 4;
				final Monitor monitor = (Monitor) it.next();
				final JTextField textThreshold = (JTextField) pMonitors
						.getComponent(base + 1);
				final JTextField textMaximum = (JTextField) pMonitors
						.getComponent(base + 2);
				final JCheckBox checkAlarm = (JCheckBox) pMonitors
						.getComponent(base + 3);
				final double threshold = Double.parseDouble(textThreshold
						.getText().trim());
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
			BROADCASTER.broadcast(BroadcastEvent.Command.MONITORS_ENABLED);
		} else {
			throw new IllegalStateException(getClass().getName() + ".start(): "
					+ "called before the monitors were configured.");
		}
	}

	/**
	 * Stop monitors interval updating loop
	 */
	private void loopThreadStopped() {
		loopThread = null;
		for (Iterator it = Monitor.getMonitorList().iterator(); it.hasNext();) {
			((Monitor) it.next()).reset();
		}
		BROADCASTER.broadcast(BroadcastEvent.Command.MONITORS_DISABLED);
	}

	/**
	 * method to run and update monitors
	 * 
	 */
	public void run() {
		try {
			while (loopThread.checkState()) { // loop until stopped
				final int waitForResults = 500;
				final int waitAfterRepaint = interval * 1000 - waitForResults;
				/* read scalers and wait */
				BROADCASTER.broadcast(BroadcastEvent.Command.SCALERS_READ);
				Thread.sleep(waitForResults);
				// loop for each monitor
				for (Iterator it = Monitor.getMonitorList().iterator(); it
						.hasNext();) {
					final Monitor monitor = (Monitor) it.next();
					monitor.update();// update the monitor
				} // end loop monitors
				/* Broadcast event on UI thread */
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						BROADCASTER
								.broadcast(BroadcastEvent.Command.MONITORS_UPDATE);
					}
				});
				Thread.sleep(waitAfterRepaint);
			} // loop until stopped
		} catch (InterruptedException ie) {
			LOGGER.log(Level.SEVERE, "Monitor Interupted", ie);
		}
		loopThreadStopped();
	}
}
