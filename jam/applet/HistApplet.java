package jam.applet;
import java.applet.Applet;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.rmi.*;
import jam.global.*;
import jam.data.*;
import jam.plot.*;
import jam.*;
/**
 * An applet to allow remote viewing of Jam Histograms
 *
 * @author Ken Swartz
 * @version 0.5
 * 
 */
public class HistApplet
	extends Applet
	implements ActionListener, ItemListener {

	private Display display;
	private JamConsole console;
	private RemoteData remoteData;

	private TextField textHost;
	private Button blink;

	private URL localPath;
	private String documentHost;

	// select panel controls
	public Panel pselect;
	FlowLayout flselect;
	private Label lrunState; //run state label         
	private Label lhist; //label for histogram Chooser    
	Choice histogramChooser; //reference needed by command
	private Button boverLay; //button for overlay    
	private Label lgate; //label for gate choicer
	Choice gateChooser; // reference needed by command

	private int sizeX;
	private int sizeY;
	private String expname;

	/**
	 * Initializes the applet.  You never need to call this directly; it is
	 * called automatically by the system once the applet is created.
	 */
	public void init() {
		System.out.println("HistApplet init");
		//setup applet size
		try {
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

		textHost = new TextField("rmi:// ");
		textHost.setColumns(30);
		textHost.setBackground(Color.white);
		textHost.addActionListener(this);
		pHost.add(textHost);

		blink = new Button("Link");
		blink.setActionCommand("link");
		blink.addActionListener(this);
		pHost.add(blink);

		//output console at bottome
		console = new JamConsole(20);
		this.add(BorderLayout.SOUTH, console);

		//display in middle						
		display = new Display((MessageHandler) console);
		this.add(display);

		// tool bar for selecting
		addToolbarSelect(ptop);

		// tool bar for plot actions
		display.addToolbarAction();

		//let display listen for text input commands	
		console.setCommandListener((CommandListener) display);

		//where did we come from, set host url
		//setup applet document path
		localPath = this.getDocumentBase();

		documentHost = this.getDocumentBase().getHost();
		if (documentHost == null) {
			documentHost = "hostname";
		}
		if (expname == null) {
			expname = "expname";
		}
		textHost.setText("rmi://" + documentHost + "/" + expname);
		//XXSystem.out.println("local path  "+localPath);			
		//System.out.println("Document url "+ documentHost);

		//load initial histograms	    
		Histogram.clearList();
		try {
			InitialHistograms inithist = new jam.InitialHistograms();
			display.displayHistogram(inithist.histStart);
			setHistogramList(Histogram.getHistogramList());
			display.setPreference(Display.WHITE_BACKGROUND, true);
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
	 * @param  e    Action event from pull down menus
	 * @since Version 0.5
	 */
	public void itemStateChanged(ItemEvent ie) {

		Histogram hist;
		Gate gate;
		int area;
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
					"Error: no item in histogram choicer " + ie.getItem());
			}

			//a gate has been choicen		
		} else if (ie.getItemSelectable() == gateChooser) {
			//if none ignore
			if (!(ie.getItem().equals("none"))) {
				gate = Gate.getGate((String) ie.getItem());
				if (gate.getType() == Gate.ONE_DIMENSION) {
					area = gate.getArea();
					lowerLimit = 0;
					upperLimit = 0;
					//FIXME		    lowerLimit=gate.getLimits1d()[0];
					//		    upperLimit=gate.getLimits1d()[1];		    			
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
				display.displayGate(gate);
			}
		}
	}
	
	/**
	 *
	 */
	public void readfile(String fileName) {
//		try {
//			FileInputStream fis = new FileInputStream(fileName);
			// FIXME	    histIO.readSpeFile(fis);
//		} catch (FileNotFoundException e) {
//			System.out.println("file not found");
//		}
	}
	
	public void setMode(int mode) {
		//FIXME
		//does nothing for now
		System.out.println("setMode remote");
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
	 * @return  <code>void</code> 
	 */
	public void setHistogramList(java.util.List histogramList) {

		Iterator allHistograms;
		Histogram firstHist = null;
		Histogram hist;
		boolean first = false;

		histogramChooser.removeAll();
		allHistograms = histogramList.iterator();

		//There are histograms in list
		if (histogramList.size() != 0) {
			first = true;

			//loop for all histograms
			while (allHistograms.hasNext()) {
				hist = ((Histogram) allHistograms.next());
				histogramChooser.add(hist.getName());
				//save first histogram	    
				if (first) {
					firstHist = hist;
					first = false;
				}
			}
			//load first histogram in lsit
			if (firstHist != null) {
				display.displayHistogram(firstHist);
				setGateList(firstHist.getGates());
			}

			//no histograms in list	    
		} else {
			histogramChooser.add("No Histograms");

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
	 * @param   gateList  the list of gates
	 * @return  <code>void</code> 
	 */
	public void setGateList(Gate[] gates) {
		//if we have gates load gates of current histogram into chooser 
		if (gateChooser != null) {

			gateChooser.removeAll();
			if (gates.length == 0) {
				gateChooser.add("none");
			} else {
				for (int i = 0; i < gates.length; i++) {
					gateChooser.add((String) gates[i].getName());
				}
			}
		}
	}
	/**
	 * Adds the tool bar the at the top of the plot.
	 *
	 * @return  <code>void</code> 
	 * @since Version 0.5
	 */
	public void addToolbarSelect(Panel p) {

		//panel with selection and print ect..
		pselect = new Panel();
		flselect = new FlowLayout(FlowLayout.LEFT, 10, 5);
		pselect.setLayout(flselect);
		pselect.setBackground(Color.lightGray);
		pselect.setForeground(Color.black);
		p.add(BorderLayout.NORTH, pselect);

		// >>setup select panel 
		lrunState = new Label("      ", Label.CENTER);

		lhist = new Label("Histogram", Label.RIGHT);

		histogramChooser = new Choice();
		histogramChooser.addItem("HISTOGRAMNAMES");
		histogramChooser.addItemListener(this);

		boverLay = new Button("Overlay");
		boverLay.setActionCommand("overlay");
		boverLay.addActionListener(this);

		lgate = new Label("Gate", Label.RIGHT);

		gateChooser = new Choice();
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
