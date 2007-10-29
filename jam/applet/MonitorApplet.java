package jam.applet;

import jam.data.Group;
import jam.data.Monitor;
import jam.data.RemoteData;
import jam.data.Scaler;
import jam.data.control.PlotBar;
import jam.global.LoggerConfig;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * An applet to allow remote viewing of Jam Monitors.
 * 
 * @author Ken Swartz
 * @version 0.5
 */
public class MonitorApplet extends JApplet implements ActionListener,// NOPMD
		ItemListener, Runnable {

	static private final boolean DEBUG = false;

	private transient JPanel pMonitors;

	private transient RemoteData remoteData;

	private transient JCheckBox checkAudio;

	private transient JTextField textError;

	private transient JPanel pal;

	private transient String hostName;

	private transient List<Monitor> monitorList;

	private transient final List<Double> monitorValues = new ArrayList<Double>();

	private transient int interval;

	private transient boolean audioOn = false;

	private transient Thread loopThread;// NOPMD

	private static final String packageName = MonitorApplet.class.getPackage()
			.getName();

	static {
		new LoggerConfig(packageName);
	}

	private static final Logger LOGGER = Logger.getLogger(packageName);

	/**
	 * Initializes the applet. You never need to call this directly; it is
	 * called automatically by the system once the applet is created.
	 */
	public void init() {
		LOGGER.info("MonitorApplet init");
		String expname = "";
		int sizeX = 300;
		int sizeY = 300;
		// setup applet size
		try {
			sizeY = Integer.parseInt(getParameter("height"));
			sizeX = Integer.parseInt(getParameter("width"));
			expname = getParameter("expname");
		} catch (NumberFormatException nfe) {
			LOGGER.log(Level.SEVERE, "height and width not numbers", nfe);
		}

		// applet layout
		this.setLayout(new BorderLayout(0, 0));
		resize(sizeX, sizeY);

		setBackground(Color.lightGray);
		setForeground(Color.black);

		final JPanel ptop = new JPanel();
		ptop.setLayout(new GridLayout(0, 1, 3, 5));
		this.add(BorderLayout.NORTH, ptop);

		final JPanel pHost = new JPanel();
		pHost.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 5));
		ptop.add(pHost);

		final JLabel llink = new JLabel("Link to URL: ", SwingConstants.RIGHT);
		pHost.add(llink);

		final JTextField textHost = new JTextField("rmi:// ");
		textHost.setColumns(30);
		textHost.setBackground(Color.white);
		textHost.setEditable(false);
		textHost.addActionListener(this);
		pHost.add(textHost);

		// alarm panel for display dialog
		pal = new JPanel();
		pal.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));

		checkAudio = new JCheckBox("Audio Alarm", true);
		checkAudio.addItemListener(this);
		pal.add(checkAudio);
		this.add(pal);

		textError = new JTextField();
		textError.setEditable(false);
		this.add(BorderLayout.SOUTH, textError);

		// display in middle
		pMonitors = new JPanel();
		pMonitors.setLayout(new GridLayout(0, 2, 5, 5));
		this.add(pMonitors);

		// where did we come from, set host url
		// setup applet document path
		final URL localPath = this.getDocumentBase();
		String documentHost = localPath.getHost();
		if (documentHost == null) {
			documentHost = "hostname";
		}
		if (expname == null) {
			expname = "expname";
		}
		hostName = "rmi://" + documentHost + "/" + expname;
		textHost.setText(hostName);

	}

	/**
	 * Called to start the applet. You never need to call this directly; it is
	 * called when the applet's document is visited.
	 */
	public void start() {
		LOGGER.info("start");
		if (DEBUG) {
			createExample();
		} else {
			link(hostName);
		}
		setupDisplay();

		if (loopThread == null) {
			loopThread = new Thread(this);
			loopThread.setPriority(2); // lower priority than display and sort
			loopThread.setDaemon(true);
			loopThread.start();
		}
	}

	/**
	 * Receive action frow awt widgets
	 */
	public void actionPerformed(final ActionEvent event) {
		/*
		 * String incommand; String hostName; String file; URL histogramURL;
		 * 
		 * incommand=e.getActionCommand();
		 * 
		 * 
		 * if ((e.getSource()==textHost)) { incommand="link"; }
		 * 
		 * try {
		 * 
		 * if (incommand=="link"){ hostName=textHost.getText().trim();
		 * textError.setText("Trying "+hostName); link(hostName); } } catch
		 * (JamException je) { textError.setText(je.getMessage()); } catch
		 * (SecurityException se){ textError.setText("Security Exception:
		 * "+se.getMessage()); }
		 */
	}

	/**
	 * Recieves the inputs from the pull down menus that are choice changes
	 * 
	 * @param itemEvent
	 *            event from pull down menus
	 * @since Version 0.5
	 */
	public void itemStateChanged(final ItemEvent itemEvent) {
		if (itemEvent.getSource() == checkAudio) {
			audioOn = checkAudio.isSelected();
		}

	}

	/**
	 * Setup the display dialog box.
	 * 
	 */
	private void setupDisplay() {
		pMonitors.removeAll();
		// widgets for dislay page
		final int numberMonitors = monitorList.size();
		final JPanel[] pmon = new JPanel[numberMonitors];
		final JLabel[] labelDisp = new JLabel[numberMonitors];
		final JTextField[] textValue = new JTextField[numberMonitors];
		final PlotBar[] plotBar = new PlotBar[numberMonitors];
		for (int i = 0; i < numberMonitors; i++) {
			makeGUI(pmon, labelDisp, textValue, plotBar, i);
		}
		pMonitors.add(pal);
	}

	private void makeGUI(JPanel[] pmon, JLabel[] labelDisp,
			JTextField[] textValue, PlotBar[] plotBar, final int index) {
		pmon[index] = new JPanel();
		pmon[index].setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		pMonitors.add(pmon[index]);
		labelDisp[index] = new JLabel("          ", SwingConstants.RIGHT);
		final Monitor monitor = monitorList.get(index);
		labelDisp[index].setText(monitor.getName());
		pmon[index].add(labelDisp[index]);
		textValue[index] = new JTextField("           ");
		textValue[index].setColumns(6);
		textValue[index].setEditable(false);
		textValue[index].setBackground(Color.white);
		textValue[index].setText(String.valueOf(0));
		pmon[index].add(textValue[index]);
		plotBar[index] = new PlotBar(monitor);
		pMonitors.add(plotBar[index]);
	}

	/*
	 * non-javadco: link to host with rmi
	 */
	private void link(final String stringURL) {
		LOGGER.info("open a link to " + stringURL);
		try {
			remoteData = (RemoteData) Naming.lookup(stringURL);
		} catch (RemoteException re) {
			textError.setText("Error: Remote lookup up failed URL: "
					+ stringURL + "Excpetion:" + re.getMessage());
		} catch (java.net.MalformedURLException mue) {
			textError.setText("Error: Remote look up malformed URL: "
					+ stringURL);
		} catch (NotBoundException nbe) {
			textError.setText("Error: Remote look up could not find name "
					+ stringURL);
		}
		try {
			LOGGER.info("get monitors");
			/* load monitor list */
			monitorList = remoteData.getMonitorList();
			interval = 10;
			LOGGER.info("interval " + interval);

		} catch (RemoteException re) {
			LOGGER.log(Level.SEVERE, re.getMessage(), re);
			textError.setText("Error: Getting monitors " + hostName);
		}
		textError.setText("link made ");
	}

	/**
	 * creat a set of example monitors for debugging
	 */
	private void createExample() {
		final Group testGroup = Group.createGroup("Test", Group.Type.TEMP);
		final int numberMonitors = 3;
		monitorList = new ArrayList<Monitor>(numberMonitors);
		monitorValues.clear();
		final Scaler scal = testGroup.createScaler("ex", 0);
		Monitor monitor = new Monitor("test", scal);
		monitor.setThreshold(10);
		monitorList.add(0, monitor);
		monitor = new Monitor("help", scal);
		monitorList.add(1, monitor);
		monitor = new Monitor("damn", scal);
		monitorList.add(2, monitor);
		for (int i = 0; i < numberMonitors; i++) {
			monitorValues.set(i, monitorList.get(i).getValue());
		}
		interval = 5;
	}

	/**
	 * 
	 */
	public void run() {
		int count = 0;
		try {
			// infinite loop
			while (count < 10) {
				count++;
				LOGGER.info("loop");

				// update the monitor
				if (!DEBUG) {
					monitorValues.clear();
					monitorValues.addAll(remoteData.getMonitorValues());
				}
				final int numberMonitors = monitorList.size();
				// is the alarm for this monitor set set
				for (int i = 0; i < numberMonitors; i++) {
					LOGGER.info("mon value " + i + " " + monitorValues.get(i));
					if (((monitorList.get(i).getAlarm()) && (monitorValues
							.get(i)) < monitorList.get(i).getThreshold())
							&& audioOn) {
						LOGGER.info("beep for " + i);
						Toolkit.getDefaultToolkit().beep();
					}
				}
				// end loop monitors
				Thread.sleep(interval * 1000);
			}
			// infinite loop
		} catch (RemoteException re) {
			textError.setText("Error: Remote Exception " + re.getMessage());
		} catch (InterruptedException ie) {
			textError.setText("Monitor Interupted ");
		}
	}
}
