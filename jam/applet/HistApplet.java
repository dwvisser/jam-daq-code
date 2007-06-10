package jam.applet;//NOPMD

import jam.InitialHistograms;
import jam.JamException;
import jam.commands.CommandManager;
import jam.data.Gate;
import jam.data.Histogram;
import jam.data.RemoteData;
import jam.global.LoggerConfig;
import jam.plot.PlotDisplay;
import jam.ui.Console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * An applet to allow remote viewing of Jam Histograms
 * 
 * @author Ken Swartz
 * @version 0.5
 * 
 */
public class HistApplet extends JApplet implements ActionListener, ItemListener {// NOPMD

	private static final Console console;

	private static final Logger LOGGER;

	static {
		final CommandManager manager = CommandManager.getInstance();
		console = new Console(20, manager, manager);
		final String packageName = HistApplet.class.getPackage().getName();
		new LoggerConfig(packageName, console.getLog());
		LOGGER = Logger.getLogger(packageName);
	}

	private transient JButton boverLay; // button for overlay

	private transient PlotDisplay display;

	private transient FlowLayout flselect;

	private transient JComboBox gateChooser; // reference needed by command

	// command

	private transient JComboBox histogramChooser; // reference needed by

	private transient JLabel lgate; // label for gate choicer

	private transient JLabel lhist; // label for histogram Chooser

	private transient JLabel lrunState; // run state label

	/* select panel controls */
	private transient JPanel pselect;

	private transient JTextField textHost;

	/**
	 * Receive action frow awt widgets
	 */
	public void actionPerformed(final ActionEvent actionEvent) {
		String incommand;
		String hostName;
		incommand = actionEvent.getActionCommand();
		if ((actionEvent.getSource() == textHost)) {
			incommand = "link";
		}
		try {
			if (incommand == "link") {
				hostName = textHost.getText().trim();
				LOGGER.info("Trying " + hostName);
				link(hostName);
				LOGGER.info("Remote link made to: " + hostName);
			}
		} catch (JamException je) {
			LOGGER.log(Level.SEVERE, je.getMessage(), je);
		} catch (SecurityException se) {
			LOGGER.log(Level.SEVERE, se.getMessage(), se);
		}
	}

	/**
	 * Adds the tool bar the at the top of the plot.
	 * 
	 * @param pAdd
	 *            panel to add toolbar to
	 * @since Version 0.5
	 */
	public void addToolbarSelect(final JPanel pAdd) {

		// panel with selection and print ect..
		pselect = new JPanel();
		flselect = new FlowLayout(FlowLayout.LEFT, 10, 5);
		pselect.setLayout(flselect);
		pselect.setBackground(Color.lightGray);
		pselect.setForeground(Color.black);
		pAdd.add(BorderLayout.NORTH, pselect);

		// >>setup select panel
		lrunState = new JLabel("      ", SwingConstants.CENTER);

		lhist = new JLabel("Histogram", SwingConstants.RIGHT);

		histogramChooser = new JComboBox();
		histogramChooser.addItem("HISTOGRAMNAMES");
		histogramChooser.addItemListener(this);

		boverLay = new JButton("Overlay");
		boverLay.setActionCommand("overlay");
		boverLay.addActionListener(this);

		lgate = new JLabel("Gate", SwingConstants.RIGHT);

		gateChooser = new JComboBox();
		gateChooser.addItem("GATENAMES");
		gateChooser.addItemListener(this);

		pselect.add(lrunState);
		pselect.add(lhist);
		pselect.add(histogramChooser);
		pselect.add(boverLay);
		pselect.add(lgate);
		pselect.add(gateChooser);
	}

	/**
	 * Initializes the applet. You never need to call this directly; it is
	 * called automatically by the system once the applet is created.
	 */
	public void init() {
		int sizeX = 500;
		int sizeY = 300;
		try {// setup applet size
			sizeY = Integer.parseInt(this.getParameter("height"));
			sizeX = Integer.parseInt(this.getParameter("width"));
		} catch (NumberFormatException nfe) {
			LOGGER.log(Level.SEVERE, "height and width not numbers", nfe);
		}

		final String expname = this.getParameter("expname");

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

		final JLabel llink = new JLabel("Link to URL: ", Label.RIGHT);
		pHost.add(llink);

		textHost = new JTextField("rmi:// ");
		textHost.setColumns(30);
		textHost.setBackground(Color.white);
		textHost.addActionListener(this);
		pHost.add(textHost);

		final AbstractButton blink = new JButton("Link");
		blink.setActionCommand("link");
		blink.addActionListener(this);
		pHost.add(blink);
		/* output console at bottome */
		this.add(BorderLayout.SOUTH, console);
		/* display in middle */
		display = new PlotDisplay(console);
		this.add(display);
		addToolbarSelect(ptop);// tool bar for selecting
		/*
		 * where did we come from, set host url, and setup applet document path
		 */
		final URL localPath = this.getDocumentBase();
		String documentHost = localPath.getHost();
		if (documentHost == null) {
			documentHost = "hostname";
		}
		textHost.setText("rmi://" + documentHost + "/" + expname);
		Histogram.clearList();
		try {
			new InitialHistograms();// load initial histograms
			setHistogramList(Histogram.getHistogramList());
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error create histograms ", e);
		}

	}

	/**
	 * Recieves the inputs from the pull down menus that are choice changes
	 * 
	 * @param itemEvent
	 *            event from pull down menus
	 * @since Version 0.5
	 */
	public void itemStateChanged(final ItemEvent itemEvent) {

		Histogram hist;
		Gate gate;
		double area;
		int lowerLimit;
		int upperLimit;

		// a histogram has been choicen
		if (itemEvent.getItemSelectable() == histogramChooser) {
			if ((itemEvent.getItem() == null)) {
				// error no such histogram
				LOGGER.log(Level.WARNING,
						"Error: no item in histogram chooser "
								+ itemEvent.getItem());
			} else {
				hist = Histogram.getHistogram((String) itemEvent.getItem());
				if (hist == null) {
					// error no such histogram
					LOGGER.log(Level.WARNING, "Error: histogram null");
				} else {
					display.displayHistogram(hist);
					setGateList(hist.getGates());
				}
			}

			// a gate has been choicen
		} else if (itemEvent.getItemSelectable() == gateChooser
				&& !(itemEvent.getItem().equals("none"))) {
			gate = Gate.getGate((String) itemEvent.getItem());
			if (gate.getDimensionality() == 1) {
				area = gate.getArea();
				final int[] limits = gate.getLimits1d();
				lowerLimit = limits[0];
				upperLimit = limits[1];
				LOGGER.info("Gate: " + gate.getName() + ", Ch. " + lowerLimit
						+ " to " + upperLimit + "  Area = " + area);
			} else {
				area = gate.getArea();
				LOGGER.info("Gate " + gate.getName() + ", Area = " + area);
			}
		}
	}

	/*
	 * non-javadoc: link to host with rmi
	 */
	private void link(final String stringURL) throws JamException {
		LOGGER.fine("open a link to " + stringURL);
		String[] histogramNames;
		List<Histogram> histogramList;
		List<Gate> gateList;
		RemoteData remoteData;
		try {
			remoteData = (RemoteData) Naming.lookup(stringURL);
		} catch (RemoteException re) {
			throw new JamException("Remote lookup up failed URL: " + stringURL,
					re);
		} catch (java.net.MalformedURLException mue) {
			throw new JamException(
					"Remote look up malformed URL: " + stringURL, mue);
		} catch (NotBoundException nbe) {
			throw new JamException("Remote look up could not find name "
					+ stringURL, nbe);
		}
		try {
			LOGGER.fine("get hist names");
			histogramNames = remoteData.getHistogramNames();
			LOGGER.fine("got hist names");
			LOGGER.fine("names 0 " + histogramNames[0]);
			// load histogram list
			histogramList = remoteData.getHistogramList();
			Histogram.setHistogramList(histogramList);
			// load gate list
			gateList = remoteData.getGateList();
			Gate.setGateList(gateList);
			// jam client
			setHistogramList(histogramList);
		} catch (RemoteException re) {
			LOGGER.log(Level.SEVERE, re.getMessage(), re);
			throw new JamException("Remote getting histogram list", re);
		}
		LOGGER.fine("link made ");
	}

	/**
	 * Sets the chooser to the current list of gates
	 * 
	 * @param gates
	 *            the list of gates
	 */
	public void setGateList(final List<Gate> gates) {
		/* if we have gates load gates of current histogram into chooser */
		if (gateChooser != null) {
			gateChooser.removeAll();
			/* set proper model */
			gateChooser.setModel(new DefaultComboBoxModel(new Vector<Gate>(// NOPMD
					gates)));
		}
	}

	/**
	 * Sets the chooser to the current list of histograms.
	 * 
	 * @param histogramList
	 *            the list of histograms.
	 */
	public void setHistogramList(final List<Histogram> histogramList) {
		histogramChooser.removeAll();
		histogramChooser.setModel(new DefaultComboBoxModel(
				new Vector<Histogram>(histogramList)));// NOPMD
		Histogram firstHist = null;
		if (!histogramList.isEmpty()) {
			firstHist = histogramList.get(0);
		}
		if (firstHist != null) {
			display.displayHistogram(firstHist);
			setGateList(firstHist.getGates());
		}
		flselect = new FlowLayout(FlowLayout.LEFT, 10, 5);
		pselect.setLayout(flselect);
		pselect.removeAll();
		pselect.add(lrunState);
		pselect.add(lhist);
		pselect.add(histogramChooser);
		pselect.add(boverLay);
		pselect.add(lgate);
		pselect.add(gateChooser);
		pselect.doLayout();

	}

}
