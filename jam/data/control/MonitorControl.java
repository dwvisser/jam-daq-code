package jam.data.control;

import jam.data.DataException;
import jam.data.Monitor;
import jam.global.BroadcastEvent;
import jam.global.GoodThread;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.sort.ThreadPriorities;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Reads and displays the monitors.
 *
 * @version  September 2000
 * @author   Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @since       JDK1.1
 */
public final class MonitorControl
	extends DataControl
	implements Runnable {	

	private final MessageHandler msgHandler;

	//widgets for configuration
	private JPanel pConfig;
	private JPanel pMonitors;

	private JLabel labelName;
	private JLabel labelThres;
	private JLabel labelMax;
	private JLabel labelAlarm;

	private JSpinner spinnerUpdate;

	//general variables
	private boolean sortMonitors = false; //have Monitors been added by sort

	private int interval; //update interval
	private GoodThread loopThread; //loop to update monitors
	private boolean configured = false; //monitors have been configured
	
	private static MonitorControl mc=null;
	
	static public MonitorControl getSingletonInstance(){
		if (mc==null){
			mc=new MonitorControl();
		}
		return mc;
	}

	MonitorControl() {
		super("Monitors Setup", false);
		msgHandler = JamStatus.instance().getMessageHandler();
		final Container cdconfig = getContentPane();
		setResizable(false);
		setLocation(20, 50);

		int spacing = 5;
		cdconfig.setLayout(new GridLayout(0, 1, spacing, spacing));
		pConfig = new JPanel(new BorderLayout());
		Border border = new EmptyBorder(5, 10, 0, 10);
		pConfig.setBorder(border);
		cdconfig.add(pConfig);

		JPanel pUpper = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pConfig.add(pUpper, BorderLayout.CENTER);

		//Panel to fill in with monitors
		pMonitors = new JPanel(new GridLayout(0, 4, 5, 5));
		pUpper.add(pMonitors);

		labelName = new JLabel("Name", JLabel.CENTER);
		labelThres = new JLabel("Threshold", JLabel.CENTER);
		labelMax = new JLabel("Maximum", JLabel.CENTER);
		labelAlarm = new JLabel("Alarm", JLabel.LEFT);
		pMonitors.add(labelName);
		pMonitors.add(labelThres);
		pMonitors.add(labelMax);
		pMonitors.add(labelAlarm);

		//Lower panel has widgets that do not change
		final JPanel pLower = new JPanel(new GridLayout(0, 1, 0, 0));
		pConfig.add(pLower, BorderLayout.SOUTH);

		// panel for update time
		final JPanel pupdate = new JPanel();
		pupdate.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		pLower.add(pupdate);

		//panel of update time
		JLabel lUpdate = new JLabel("Update", JLabel.RIGHT);
		pupdate.add(lUpdate);

		final Integer one = new Integer(1);
		final Integer init = new Integer(1);
		spinnerUpdate =
			new JSpinner(new SpinnerNumberModel(init, one, null, one));
		spinnerUpdate.setPreferredSize(new Dimension(50, 
		spinnerUpdate.getPreferredSize().height));
		pupdate.add(spinnerUpdate);

		JLabel lunit = new JLabel("sec", JLabel.LEFT);
		pupdate.add(lunit);

		/// panel for buttons
		final JPanel pbutton =
			new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
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
				//stop monitor thread if running
				if (loopThread != null){
					loopThread.setState(GoodThread.STOP);
				}
			}
		});
		pb.add(bcancel);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		// setup monitors
		setup();

	}

	/**
	 * Setup all monitors.
	 */
	public void setup() {
		if (!Monitor.getMonitorList().isEmpty()) {
			sortMonitors = true;
		}
		final int cols = 6;
		/* Clear panel */
		pMonitors.removeAll();
		/* Add labels of columns */
		pMonitors.add(labelName);
		pMonitors.add(labelThres);
		pMonitors.add(labelMax);
		pMonitors.add(labelAlarm);
		/* for each monitor make a panel with
		 * label, threshold, maximum, and alarm */
		Iterator monitors = Monitor.getMonitorList().iterator();
		while (monitors.hasNext()) {
			Monitor monitor = (Monitor) monitors.next();
			final JLabel labelConfig =
				new JLabel(monitor.getName(), JLabel.CENTER);
			//labelConfig[i].setText();
			pMonitors.add(labelConfig);
			final JTextField textThreshold = new JTextField();
			textThreshold.setColumns(cols);
			textThreshold.setEditable(true);
			textThreshold.setText("10");
			pMonitors.add(textThreshold);
			final JTextField textMaximum = new JTextField();
			textMaximum.setColumns(cols);
			textMaximum.setEditable(true);
			textMaximum.setText("100");
			pMonitors.add(textMaximum);
			final JCheckBox checkAlarm = new JCheckBox();
			checkAlarm.setSelected(false);
			pMonitors.add(checkAlarm);
		}
		pack();
	}

	/**
	 * Recall the monitor's parameters
	 * and set the input fields.
	 */
	void recall() {
		/* update interval */
		spinnerUpdate.setValue(new Integer(interval));
		final Iterator mList = Monitor.getMonitorList().iterator();
		/*get the Monitor parameters */
		int base = 0;
		while (mList.hasNext()) {
			final Monitor monitor = (Monitor) mList.next();
			base += 4;
			final JTextField textThreshold =
				(JTextField) pMonitors.getComponent(base + 1);
			final JTextField textMaximum =
				(JTextField) pMonitors.getComponent(base + 2);
			final JCheckBox checkAlarm =
				(JCheckBox) pMonitors.getComponent(base + 3);
			textThreshold.setText(String.valueOf(monitor.getThreshold()));
			textMaximum.setText(String.valueOf(monitor.getMaximum()));
			checkAlarm.setSelected(monitor.getAlarm());
		}
	}

	/**
	 * Configure the monitors, i.e. set the values their parameters
	 * according to the input fields.
	 *
	 * @throws DataException for invalid number input
	 */
	void configure() {
		try {
			//set update interval
			interval = ((Integer) spinnerUpdate.getValue()).intValue();
			if (interval < 1) {
				throw new IllegalArgumentException("Update interval must be greater than 1");
			}
			Monitor.setInterval(interval);
			//set Monitor parameters
			final Iterator it = Monitor.getMonitorList().iterator();
			/*get the Monitor parameters */
			int base = 0;
			while (it.hasNext()) {
				base += 4;
				final Monitor monitor = (Monitor) it.next();
				final JTextField textThreshold =
					(JTextField) pMonitors.getComponent(base + 1);
				final JTextField textMaximum =
					(JTextField) pMonitors.getComponent(base + 2);
				final JCheckBox checkAlarm =
					(JCheckBox) pMonitors.getComponent(base + 3);
				final double threshold =
					Double.parseDouble(textThreshold.getText().trim());
				monitor.setThreshold(threshold);
				final double maximum =
					Double.parseDouble(textMaximum.getText().trim());
				monitor.setMaximum(maximum);
				monitor.setAlarm(checkAlarm.isSelected());
			}
		} catch (NumberFormatException nfe) {
			msgHandler.errorOutln("Invalid number input [MonitorControl]");
		}
		configured = true;
	}

	/**
	 * Start monitors interval updating loop.
	 *
	 * @throws IllegalStateException if the monitors aren't configured yet
	 */
	private void startLoopThread() {
		if (configured) {
			if (loopThread == null) {
				loopThread = new GoodThread(this);//defaults to RUN state
				loopThread.setPriority(ThreadPriorities.MESSAGING);
				loopThread.setName("Monitor Thread");
				loopThread.setDaemon(true);
				loopThread.start();
			}
			broadcaster.broadcast(BroadcastEvent.MONITORS_ENABLED);			
		} else {
			throw new IllegalStateException(
				getClass().getName()
					+ ".start(): "
					+ "called before the monitors were configured.");
		}
	}

	/**
	 * Stop monitors interval updating loop
	 */
	private void loopThreadStopped() {
		loopThread = null;
		for (Iterator it = Monitor.getMonitorList().iterator();
			it.hasNext();
			) {
			((Monitor) it.next()).reset();
		}
		broadcaster.broadcast(BroadcastEvent.MONITORS_DISABLED);		
	}

	/**
	 * method to run and update monitors
	 *
	 */
	public void run() {
		try { 
			while (loopThread.checkState()) { //loop until stopped
				final int waitForResults = 500;
				final int waitAfterRepaint = interval * 1000 - waitForResults;
				/* read scalers and wait */
				broadcaster.broadcast(BroadcastEvent.SCALERS_READ);
				Thread.sleep(waitForResults);
				//loop for each monitor
				for (Iterator it = Monitor.getMonitorList().iterator();
					it.hasNext();
					) {
					final Monitor monitor = (Monitor) it.next();
					monitor.update();//update the monitor
				} //end loop monitors
				/* Broadcast event on UI thread */
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						broadcaster.broadcast(BroadcastEvent.MONITORS_UPDATE);
					}
				});
				Thread.sleep(waitAfterRepaint);
			} //loop until stopped
		} catch (InterruptedException ie) {
			msgHandler.errorOutln("Monitor Interupted");
		} 
		loopThreadStopped();
	}
}
