package jam.applet;
import jam.JamConsole;
import jam.JamException;
import jam.data.Gate;
import jam.data.Histogram;
import jam.data.RemoteData;
import jam.plot.Display;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
/**
 * An applet to allow remote viewing of Jam Histograms
 *
 * @author Ken Swartz
 * @version 0.5
 * 
 */
public class HistApplet
	extends JApplet
	implements ActionListener, ItemListener {

	private Display display;
	private JamConsole console;
	private RemoteData remoteData;

	private JTextField textHost;
	private JButton blink;

	private URL localPath;
	private String documentHost;

	/* select panel controls */
	public JPanel pselect;
	FlowLayout flselect;
	private JLabel lrunState; //run state label         
	private JLabel lhist; //label for histogram Chooser    
	JComboBox histogramChooser; //reference needed by command
	private JButton boverLay; //button for overlay    
	private JLabel lgate; //label for gate choicer
	JComboBox gateChooser; // reference needed by command

	private int sizeX;
	private int sizeY;
	private String expname;

	/**
	 * Initializes the applet.  You never need to call this directly; it is
	 * called automatically by the system once the applet is created.
	 */
	public void init() {
		try {//setup applet size
			sizeY = Integer.parseInt(this.getParameter("height"));
			sizeX = Integer.parseInt(this.getParameter("width"));
			expname = this.getParameter("expname");
		} catch (NumberFormatException nfe) {
			System.err.println("height and width not numbers");
		}

		//applet layout	
		this.setLayout(new BorderLayout(0, 0));
		resize(sizeX, sizeY);
		setBackground(Color.lightGray);
		setForeground(Color.black);

		Panel ptop = new Panel();
		ptop.setLayout(new GridLayout(0, 1, 3, 5));
		this.add(BorderLayout.NORTH, ptop);

		Panel pHost = new Panel();
		pHost.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 5));
		ptop.add(pHost);

		Label llink = new Label("Link to URL: ", Label.RIGHT);
		pHost.add(llink);

		textHost = new JTextField("rmi:// ");
		textHost.setColumns(30);
		textHost.setBackground(Color.white);
		textHost.addActionListener(this);
		pHost.add(textHost);

		blink = new JButton("Link");
		blink.setActionCommand("link");
		blink.addActionListener(this);
		pHost.add(blink);
		/* output console at bottome */
		console = new JamConsole(20);
		this.add(BorderLayout.SOUTH, console);
		/* display in middle */						
		display = new Display(console);
		this.add(display);
		addToolbarSelect(ptop);// tool bar for selecting
		/* where did we come from, set host url, and 
		 * setup applet document path */
		localPath = this.getDocumentBase();
		documentHost = this.getDocumentBase().getHost();
		if (documentHost == null) {
			documentHost = "hostname";
		}
		if (expname == null) {
			expname = "expname";
		}
		textHost.setText("rmi://" + documentHost + "/" + expname);
		Histogram.clearList();
		try {
			new jam.InitialHistograms();//load initial histograms
			setHistogramList(Histogram.getHistogramList());
			//display.setPreference(Display.JamPrefs.WHITE_BACKGROUND, true);
		} catch (Exception e) {
			System.out.println("Error create histograms ");
		}

	}

	/**
	 * Called to start the applet.  You never need to call this directly; it
	 * is called when the applet's document is visited.
	 */
	public void start() {
		//	    System.out.println("view start");	    
	}

	/**
	 * Called to stop the applet.  This is called when the applet's document is
	 * no longer on the screen.  It is guaranteed to be called before destroy()
	 * is called.  You never need to call this method directly
	 */
	public void stop() {
	}

	/**
	 * Cleans up whatever resources are being held.  If the applet is active
	 * it is stopped.
	 */
	public void destroy() {
	}
	/**
	 * Receive action frow awt widgets
	 */
	public void actionPerformed(ActionEvent e) {

		String incommand;
		String hostName;

		incommand = e.getActionCommand();

		if ((e.getSource() == textHost)) {
			incommand = "link";
		}

		try {

			if (incommand == "link") {
				hostName = textHost.getText().trim();
				console.messageOutln("Trying " + hostName);
				link(hostName);
				console.messageOutln("Remote link made to: " + hostName);
			}

		} catch (JamException je) {
			console.errorOutln(je.getMessage());
		} catch (SecurityException se) {
			console.errorOutln("Security Exception: " + se.getMessage());
		}
		//		readfile(file);
		/*			    	    
			    try {
			    
		//		histogramURL=new URL("http", host, file);
				System.out.println(" >url "+histogramURL);    				
				
		//		System.out.println(histogramURL.openStream());    		
		//		histIO.readSpeFile(histogramURL.openStream());    
		
					    		
			    } catch (MalformedURLException me){
				System.out.println("Error URL");
			    } catch (IOException ioe){
				System.out.println("Error io");
			    }
		*/
		//	    display.setHistogramList( Histogram.getHistograms() );	        
		//	}
	}
	/**
	 * Recieves the inputs from the pull down menus
	 * that are choice changes
	 *
	 * @param ie event from pull down menus
	 * @since Version 0.5
	 */
	public void itemStateChanged(ItemEvent ie) {

		Histogram hist;
		Gate gate;
		double area;
		int lowerLimit;
		int upperLimit;

		//a histogram has been choicen	
		if (ie.getItemSelectable() == histogramChooser) {
			if ((ie.getItem() != null)) {
				hist = Histogram.getHistogram((String) ie.getItem());
				if (hist != null) {
					display.displayHistogram(hist);
					setGateList(hist.getGates());
				} else {
					//error no such histogram
					System.err.println("Error: histogram null [JamCommand]");
				}
			} else {
				//error no such histogram
				System.err.println(
					"Error: no item in histogram chooser " + ie.getItem());
			}

			//a gate has been choicen		
		} else if (ie.getItemSelectable() == gateChooser) {
			//if none ignore
			if (!(ie.getItem().equals("none"))) {
				gate = Gate.getGate((String) ie.getItem());
				if (gate.getDimensionality() == 1) {
					area = gate.getArea();
					final int [] limits=gate.getLimits1d();
					lowerLimit=limits[0];
					upperLimit=limits[1];		    			
					console.messageOut(
						"Gate: "
							+ gate.getName()
							+ ", Ch. "
							+ lowerLimit
							+ " to "
							+ upperLimit,
						JamConsole.NEW);
					console.messageOut("  Area = " + area, JamConsole.END);
				} else {
					area = gate.getArea();
					console.messageOut(
						"Gate " + gate.getName(),
						JamConsole.NEW);
					console.messageOut(", Area = " + area, JamConsole.END);
				}
			}
		}
	}
	
	/**
	 * link to host with rmi
	 */
	private void link(String stringURL) throws JamException {
		System.out.println("open a link to " + stringURL);

		String[] histogramNames;
		java.util.List histogramList, gateList;

		try {

			remoteData = (RemoteData) Naming.lookup(stringURL);

		} catch (RemoteException re) {
			throw new JamException(
				"Remote lookup up failed URL: "
					+ stringURL
					+ "Excpetion:"
					+ re.getMessage());

		} catch (java.net.MalformedURLException mue) {
			throw new JamException(
				"Remote look up malformed URL: " + stringURL);

		} catch (NotBoundException nbe) {
			throw new JamException(
				"Remote look up could not find name " + stringURL);

		}

		try {
			System.out.println("get hist names");
			histogramNames = remoteData.getHistogramNames();
			System.out.println("got hist names");
			System.out.println("names 0 " + histogramNames[0]);
			//load histogram list
			histogramList = remoteData.getHistogramList();
			Histogram.setHistogramList(histogramList);
			//load gate list
			gateList = remoteData.getGateList();
			Gate.setGateList(gateList);

			// jam client

			setHistogramList(histogramList);

		} catch (RemoteException re) {
			System.out.println(re.getMessage());
			throw new JamException("Remote getting histogram list [SetupRemote]");
		}
		System.out.println("link made ");
	}

	/**
	 * Sets the chooser to the current list of histograms.
	 *
	 * @param   histogramList  the list of histograms.
	 */
	public void setHistogramList(java.util.List histogramList) {
		histogramChooser.removeAll();
		histogramChooser.setModel(new DefaultComboBoxModel(
		new Vector(histogramList)));		
		final Histogram firstHist = histogramList.isEmpty() ? null : 
		(Histogram)histogramList.get(0);
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
	/**
	 * Sets the chooser to the current list of gates
	 *
	 * @param   gates  the list of gates
	 */
	public void setGateList(List gates) {
		//if we have gates load gates of current histogram into chooser 
		if (gateChooser != null) {

			gateChooser.removeAll();
			/* set proper model */
			gateChooser.setModel(new DefaultComboBoxModel(new Vector(gates)));
		}
	}
	/**
	 * Adds the tool bar the at the top of the plot.
	 *
	 * @since Version 0.5
	 */
	public void addToolbarSelect(Panel p) {

		//panel with selection and print ect..
		pselect = new JPanel();
		flselect = new FlowLayout(FlowLayout.LEFT, 10, 5);
		pselect.setLayout(flselect);
		pselect.setBackground(Color.lightGray);
		pselect.setForeground(Color.black);
		p.add(BorderLayout.NORTH, pselect);

		// >>setup select panel 
		lrunState = new JLabel("      ", Label.CENTER);

		lhist = new JLabel("Histogram", Label.RIGHT);

		histogramChooser = new JComboBox();
		histogramChooser.addItem("HISTOGRAMNAMES");
		histogramChooser.addItemListener(this);

		boverLay = new JButton("Overlay");
		boverLay.setActionCommand("overlay");
		boverLay.addActionListener(this);

		lgate = new JLabel("Gate", Label.RIGHT);

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

}
