package jam.data.control;

import jam.data.*;
import jam.global.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.border.*;

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
	implements ActionListener, ItemListener, Runnable {

	/**
	 * Master frame
	 */
	private Frame frame;
	/**
	 *
	 */
	private Broadcaster broadcaster;
	private MessageHandler msgHandler;

	//widgets for configuration
	private JDialog dconfig;
	private JPanel pConfig;
	private JPanel pLower;
	private JPanel pMonitors;

	private JLabel labelName;
	private JLabel labelThres;
	private JLabel labelMax;
	private JLabel labelAlarm;

	private JLabel[] labelConfig;
	private JTextField[] textThreshold;
	private JTextField[] textMaximum;
	private JCheckBox[] checkAlarm;
	private JPanel pupdate; //panel with update time
	private JSpinner spinnerUpdate;
	private JPanel pbutton; //panel with buttons
	private JButton brecall, bok, bapply, bcancel;

	//widgits for display
	private JDialog ddisp;
	private JPanel pBars;
	private JPanel[] pm; //panel for monitors
	private JLabel[] labelDisp;
	private JTextField[] textValue;
	private PlotBar[] plotBar;

	//panel with alarm enable
	private JPanel pal;
	private JCheckBox checkAudio;

	//array of monitors loaded at setuptime
	private Monitor[] monitor;
	private int numberMonitors;

	//general variables
	private boolean sortMonitors; //have Monitors been added by sort

	private int interval; //update interval
	private boolean audioAlarm; //is the audio alarm on
	private GoodThread loopThread; //loop to update monitors
	private boolean configured; //monitors have been configured

	public MonitorControl(Frame frame,
						  Broadcaster broadcaster,
						  MessageHandler msgHandler) {
		super("Monitors Setup ", false);;
		this.frame = frame;
		this.broadcaster = broadcaster;
		this.msgHandler = msgHandler;

		//>>dialog to configure Monitors
		dconfig = new JDialog(frame, " Monitors Setup ", false);
		Container cdconfig = dconfig.getContentPane();
		dconfig.setResizable(false);
		dconfig.setLocation(20, 50);

		int spacing = 5;
		cdconfig.setLayout(new GridLayout(0, 1, spacing, spacing));
		pConfig = new JPanel(new BorderLayout());
		Border border = new EmptyBorder(5,10,0,10);
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
		pLower =new JPanel(new GridLayout(0, 1, 0, 0));
		pConfig.add(pLower, BorderLayout.SOUTH);

		// panel for update time
		pupdate = new JPanel();
		pupdate.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		pLower.add(pupdate);

		//panel of update time
		JLabel lUpdate = new JLabel("Update", JLabel.RIGHT);
		pupdate.add(lUpdate);

		final Integer one=new Integer(1);
		spinnerUpdate = new JSpinner(new SpinnerNumberModel(one,one,null,
		one));
		pupdate.add(spinnerUpdate);

		JLabel lunit = new JLabel("sec", JLabel.LEFT);
		pupdate.add(lunit);

		/// panel for buttons
		pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		pLower.add(pbutton);
		JPanel pb = new JPanel(new GridLayout(1,0,5,5));
		pbutton.add(pb);

		brecall = new JButton("Recall");
		brecall.setActionCommand("recall");
		brecall.addActionListener(this);
		pb.add(brecall);

		bok = new JButton("OK");
		bok.setActionCommand("ok");
		bok.addActionListener(this);
		pb.add(bok);

		bapply = new JButton("Apply");
		bapply.setActionCommand("apply");
		bapply.addActionListener(this);
		pb.add(bapply);

		bcancel = new JButton("Cancel");
		bcancel.setActionCommand("cancel");
		bcancel.addActionListener(this);
		pb.add(bcancel);


		//>> dialog box to display Monitors
		ddisp = new JDialog(frame, "Monitors Disabled", false);
		ddisp.setResizable(true);
		ddisp.setLocation(20, 50);
		Container cddisp = ddisp.getContentPane();
		cddisp.setLayout(new BorderLayout());

		//Panel for the bars
		pBars =new JPanel(new GridLayout(0,2,5,5));
		pBars.setBorder(new EmptyBorder(10,10,10,10));
		cddisp.add(pBars, BorderLayout.CENTER);

		// alarm panel for display dialog
		pal = new JPanel();
		cddisp.add(pal, BorderLayout.SOUTH);
		pal.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
		checkAudio = new JCheckBox("Audio Alarm", true);
		checkAudio.addItemListener(this);
		pal.add(checkAudio);


		// variable initialization
		sortMonitors = false;
		configured = false;
		// window listeners for dispose
		ddisp.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ddisp.dispose();
			}
		});
		dconfig.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dconfig.dispose();
			}
		});
		// setup monitors
		setup();

	}

	/**
	 * Action events in configuration window
	 */
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();
		try {
			if (command == "recall") {
				recall();
			} else if ((command == "ok") || (command == "apply")) {
				configure();
				//lock monitor parameters
				start();
				if (command == "ok") {
					dconfig.dispose();
				}
			} else if (command == "cancel") {
				configured = false;
				//stop monitor thread if running
				stop();
				ddisp.setTitle("Monitors Disabled");
			} else if (command == "start") {
				start();
			} else if (command == "stop") {
				stop();
			}
		} catch (DataException je) {
			msgHandler.errorOutln(je.getMessage());
		}
	}

	/**
	 * Checks the alarm states and sets them
	 *
	 *
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getItemSelectable() == checkAudio) {
			audioAlarm = checkAudio.isSelected();
		}
	}

	/**
	 * Setup all monitors.
	 */
	public void setup() {
		numberMonitors = Monitor.getMonitorList().size();
		if (numberMonitors != 0) { // we have monitors in the Monitor list
			sortMonitors = true;
			Iterator enumMonitor = Monitor.getMonitorList().iterator();
			monitor = new Monitor[numberMonitors];
			int count = 0;
			while (enumMonitor.hasNext()) {
				/* put montitors into the monitor array */
				Monitor currentMonitor = (Monitor) enumMonitor.next();
				monitor[count] = currentMonitor;
				count++;
			}
		}
		/* setup dialog boxes */
		setupConfig();
		setupDisplay();
	}

	/**
	 * Setup the display dialog box.
	 *
	 */
	private void setupDisplay() {
		//Container cddisp = ddisp.getContentPane();

		//pBars.removeAll();
		/* widgets for dislay page */
		pm = new JPanel[numberMonitors];
		labelDisp = new JLabel[numberMonitors];
		textValue = new JTextField[numberMonitors];
		plotBar = new PlotBar[numberMonitors];
		if (numberMonitors != 0) {
			for (int i = 0; i < numberMonitors; i++) {
				pm[i] = new JPanel();
				pm[i].setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
				pBars.add(pm[i]);
				labelDisp[i] = new JLabel("          ", JLabel.RIGHT);
				labelDisp[i].setText(monitor[i].getName());
				pm[i].add(labelDisp[i]);
				textValue[i] = new JTextField("           ");
				textValue[i].setColumns(6);
				textValue[i].setEditable(false);
				textValue[i].setText(String.valueOf(0));
				pm[i].add(textValue[i]);
				plotBar[i] = new PlotBar(monitor[i]);
				pBars.add(plotBar[i]);
			}
		}
		ddisp.pack();
	}

	/**
	 * Set up the configuration dialog box.
	 */
	private void setupConfig() {
		//Clear panel
		pMonitors.removeAll();

		//Add labels of columns
		pMonitors.add(labelName);
		pMonitors.add(labelThres);
		pMonitors.add(labelMax);
		pMonitors.add(labelAlarm);

		/* widgets for configuration page */
		labelConfig = new JLabel[numberMonitors];
		textThreshold = new JTextField[numberMonitors];
		textMaximum = new JTextField[numberMonitors];
		checkAlarm = new JCheckBox[numberMonitors];
		/* for each monitor make a panel with
		 * label, threshold, maximum, and alarm */
		if (numberMonitors != 0) {
			for (int i = 0; i < numberMonitors; i++) {

				labelConfig[i] = new JLabel("          ", JLabel.CENTER);
				labelConfig[i].setText(monitor[i].getName());
				pMonitors.add(labelConfig[i]);

				textThreshold[i] = new JTextField("          ");
				textThreshold[i].setColumns(6);
				textThreshold[i].setEditable(true);
				textThreshold[i].setText("10");
				pMonitors.add(textThreshold[i]);

				textMaximum[i] = new JTextField("          ");
				textMaximum[i].setColumns(6);
				textMaximum[i].setEditable(true);
				textMaximum[i].setText("100");
				pMonitors.add(textMaximum[i]);

				checkAlarm[i] = new JCheckBox();
				checkAlarm[i].setSelected(false);
				pMonitors.add(checkAlarm[i]);
			}
		}

		dconfig.pack();
	}

	/**
	 * Show the Display dialog box.
	 */
	public void showDisplay() {
		ddisp.show();
	}

	/**
	 * Show the configuration dialog box.
	 */
	public void showConfig() {
		dconfig.show();
	}
	/**
	 * Default show dialog, shows display dialog
	 */
	public void show() {
		ddisp.show();
	}


	/**
	 * Recall the monitor's parameters
	 * and set the input fields.
	 */
	void recall() {
		/* update interval */
		spinnerUpdate.setValue(new Integer(interval));
		/*get the Monitor parameters */
		for (int i = 0; i < numberMonitors; i++) {
			textThreshold[i].setText("" + monitor[i].getThreshold());
			textMaximum[i].setText("" + monitor[i].getMaximum());
			checkAlarm[i].setSelected(monitor[i].getAlarm());
			plotBar[i].repaint();
		}
	}

	/**
	 * Configure the monitors, i.e. set the values their parameters
	 * according to the input fields.
	 *
	 * @throws DataException for invalid number input
	 */
	void configure() throws DataException {
		try {
			//set update interval
			interval = ((Integer)spinnerUpdate.getValue()).intValue();
			if (interval < 1) {
				throw new IllegalArgumentException("Update interval must be greater than 1");
			}
			Monitor.setInterval(interval);
			//set Monitor parameters
			for (int i = 0; i < numberMonitors; i++) {
				final double threshold =Double.parseDouble(
				textThreshold[i].getText().trim());
				monitor[i].setThreshold(threshold);

				final double maximum = Double.parseDouble(textMaximum[i].getText().trim());
				monitor[i].setMaximum(maximum);

				monitor[i].setAlarm(checkAlarm[i].isSelected());
				plotBar[i].repaint();
			}
			//set audio alarm
			audioAlarm = checkAudio.isSelected();
		} catch (NumberFormatException nfe) {
			throw new DataException("Invalid number input [MonitorControl]");
		}
		configured = true;
	}

	/**
	 * Display monitors
	 */
	private void display() {
		double value;

		for (int i = 0; i < numberMonitors; i++) {
			value = monitor[i].getValue();
			textValue[i].setText("" + value);
			plotBar[i].repaint();
		}

	}

	/**
	 * Start monitors interval updating loop.
	 *
	 * @throws IllegalStateException if the monitors aren't configured yet
	 */
	private void start() {
		if (configured) {
			if (loopThread == null) {
				loopThread = new GoodThread(this);
				loopThread.setPriority(2);
				/* lower priority than display and sort */
				loopThread.setDaemon(true);
				loopThread.start();
			}
			ddisp.setTitle("Monitors Enabled");
		} else {
			throw new IllegalStateException(getClass().getName()+".start(): "+
			"called before the monitors were configured.");
		}
	}

	/**
	 * Stop monitors interval updating loop
	 */
	private void stop() {
		if (loopThread != null) {
			loopThread = null;
		}
		//clear numbers and graphs
		for (int i = 0; i < numberMonitors; i++) {
			textValue[i].setText("");
			monitor[i].reset();
			plotBar[i].repaint();
		}
		ddisp.setTitle("Monitors Disabled");
	}

	/** FIXME
	 * Set the Monitor values
	 * This routine can be called back by
	 *  VME communication
	 *
	 */
	public void setMonitors(int[] scalValue) {

		for (int i = 0; i < scalValue.length; i++) {
			textValue[i].setText(String.valueOf(scalValue[i]));
		}
	}
	/**
	 * method to run and update monitors
	 *
	 */
	public void run() {
		try {
			//infinite loop
			while (loopThread != null) { //attempted fix for stop() deprecation

				//read scalers and wait
				broadcaster.broadcast(BroadcastEvent.SCALERS_READ);
				Thread.sleep(500);
				//loop for each monitor
				for (int i = 0; i < numberMonitors; i++) {
					//update the monitor
					monitor[i].update();
					//If the audio on and are we taking data
					if (audioAlarm && JamStatus.instance().isAcqOn()) {
						//is the alarm for this monitor set set
						if (monitor[i].getAlarm()) {
							//is the value out of bounds
							if (monitor[i].getValue()
								< monitor[i].getThreshold()
								|| monitor[i].getValue()
									> monitor[i].getMaximum()) {
								//FIXME  audioClip=get
								//if (audioClip==null)
								Toolkit.getDefaultToolkit().beep();
							}
						}
					}
				}
				//display monitors
				display();
				//end loop monitors
				Thread.sleep(interval * 1000 - 500);
			}
			//infinite loop
		} catch (InterruptedException ie) {
			msgHandler.errorOutln("Monitor Interupted ");
		}
	}
}
