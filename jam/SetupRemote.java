/*
 */
package jam;
import jam.data.Gate;
import jam.data.Histogram;
import jam.data.RemoteData;
import jam.global.MessageHandler;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.util.List;

/**
 * Class to make this process into a remote server for Jam or
 * hookup to a remote online acquisition that is a server.
 *
 * @author Ken Swartz
 */
public class SetupRemote implements ActionListener, ItemListener {

	static final String DEFAULT_NAME = "jam";
	static final String DEFAULT_URL = "rmi://meitner.physics.yale.edu/jam";

	final static int SERVER = 0;
	final static int SNAP = 1;
	final static int LINK = 2;

	private JamMain jamMain;
	//XX    private JamApplet jamApplet;
	private MessageHandler msgHandler;

	private Dialog dl;
	private Label lname;
	private TextField textName;
	private Checkbox cserver;
	private Checkbox csnap;
	private Checkbox clink;
	private Button bok;
	private Button bapply;
	private Checkbox checkLock;

	private int mode; //mode server, snap or link
	RemoteData remoteData;
	RemoteAccess remoteAccess;

	private String[] histogramNames;
	private List histogramList, gateList;
	private boolean inApplet; //are we running in a applet

	private boolean setupLock = false;

	/**
	 * Constructor for Jam Applet no dialog box, so we are in an applet
	 */
	/*FIXME
	    public SetupRemote(JamApplet jamApplet,  MessageHandler msgHandler){
	
	  this.jamApplet=jamApplet;        
	  this.msgHandler=msgHandler;
	  jamMain=null;
	  inApplet=true;
	    }
	*/
	
	/**
	 * Constructor for Jam Application creates dialog box, we are in an application
	 */
	public SetupRemote(JamMain jamMain, MessageHandler msgHandler) {
		this.jamMain = jamMain;
		this.msgHandler = msgHandler;
		//create dialog box
		dl = new Dialog(jamMain, "Remote Hookup ", false);
		dl.setForeground(Color.black);
		dl.setBackground(Color.lightGray);
		dl.setResizable(false);
		dl.setLocation(20, 50);
		dl.setSize(400, 250);
		dl.setLayout(new GridLayout(0, 1, 10, 10));
		// panel for mode     
		Panel pm = new Panel();
		pm.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		dl.add(pm);
		CheckboxGroup cbgmode = new CheckboxGroup();
		cserver = new Checkbox("Server  ", cbgmode, true);
		cserver.addItemListener(this);
		pm.add(cserver);
		csnap = new Checkbox("SnapShot", cbgmode, false);
		csnap.addItemListener(this);
		pm.add(csnap);
		clink = new Checkbox("Link    ", cbgmode, false);
		clink.addItemListener(this);
		pm.add(clink);
		// panel for name
		Panel pn = new Panel();
		pn.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		dl.add(pn);
		lname = new Label("Name:", Label.RIGHT);
		pn.add(lname);
		textName = new TextField(DEFAULT_NAME);
		textName.setColumns(35);
		textName.setBackground(Color.white);
		pn.add(textName);
		// panel for buttons         
		Panel pb = new Panel();
		pb.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		dl.add(pb);
		bok = new Button("   OK   ");
		pb.add(bok);
		bok.setActionCommand("ok");
		bok.addActionListener(this);
		bapply = new Button(" Apply  ");
		pb.add(bapply);
		bapply.setActionCommand("apply");
		bapply.addActionListener(this);
		Button bcancel = new Button(" Cancel ");
		pb.add(bcancel);
		bcancel.setActionCommand("cancel");
		bcancel.addActionListener(this);
		checkLock = new Checkbox("Setup Locked", false);
		checkLock.setEnabled(false);
		checkLock.addItemListener(this);
		pb.add(checkLock);
		dl.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dl.dispose();
			}
		});
		mode = SERVER;
		inApplet = false;
	}

	/**
	 *
	 */
	public void showRemote() {
		dl.show();
	}

	/**
	 * Executed at a action, button pressed
	 *
	 *
	 */
	public void actionPerformed(ActionEvent ae) {

		String command = ae.getActionCommand();
		String name = textName.getText().trim();
		try {
			if (command == "ok" || command == "apply") {
				if (mode == SERVER) {
					server(name);
					msgHandler.messageOutln("Jam made as server: " + name);
				} else if (mode == SNAP) {
					msgHandler.messageOutln("Trying " + name);
					snap(name);
					msgHandler.messageOutln("Jam remote snapShot: " + name);
				} else if (mode == LINK) {
					msgHandler.messageOutln("Trying " + name);
					link(textName.getText().trim());
					msgHandler.messageOutln("Jam remote link: " + name);
				}

				setActive(true);
				lockFields(true);

				if (command == "ok") {
					dl.dispose();
				}
			} else if (command == "cancel") {
				dl.dispose();
			}
		} catch (JamException je) {
			msgHandler.errorOutln(je.getMessage());
		}
	}

	/**
	 * What mode has been picked
	 */
	public void itemStateChanged(ItemEvent ie) {

		try {

			if (ie.getItemSelectable() == cserver) {
				mode = SERVER;
				lname.setText("Name:");
				textName.setText(DEFAULT_NAME);
			} else if (ie.getItemSelectable() == csnap) {
				mode = SNAP;
				lname.setText("URL:");
				textName.setText(DEFAULT_URL);
			} else if (ie.getItemSelectable() == clink) {
				mode = LINK;
				lname.setText("URL:");
				textName.setText(DEFAULT_URL);
				//lock up state    
			} else if (ie.getItemSelectable() == checkLock) {
				setActive((checkLock.getState()));
				lockFields((checkLock.getState()));

				if (!(checkLock.getState())) {
					reset();
					//XXXJamDisp.setSortMode(RunControl.NO_SORT);    
				}

			}
		} catch (JamException je) {
			msgHandler.errorOutln(je.getMessage());
		}

	}

	/**
	 * Create a histogram server.
	 *
	 * @exception   JamException    sends a message to the console if there is any problem setting up
	 */
	public void server(String name) throws JamException {

		try {
			remoteAccess = new RemoteAccess();
			System.out.println("new remoteAccess");
			Naming.rebind(name, remoteAccess);

			lockFields(true);
		} catch (UnknownHostException unhe) {
			System.out.println(unhe.getMessage());
			throw new JamException(
				"Creating remote server unknown host, name: "
					+ name
					+ " [SetupRemote]");
		} catch (RemoteException re) {
			System.out.println(re.getMessage());
			throw new JamException(
				"Creating remote server " + re.getMessage() + " [SetupRemote]");
		} catch (java.net.MalformedURLException mue) {
			throw new JamException("Creating remote Server malformed URL [SetupRemote]");
		}
	}

	/**
	 * Get a snap shot of data
	 *
	 * @exception   JamException    all exceptions given to <code>JamException</code> go to the console
	 */
	public void snap(String stringURL) throws JamException {

		try {
			if (jamMain != null) {// jam client
				if (jamMain.canSetSortMode()) {
					remoteData = (RemoteData) Naming.lookup(stringURL);
					msgHandler.messageOutln("Remote lookup OK!");
				} else {
					throw new JamException("Can't view remotely, sort mode locked [SetupRemote]");
				}
			} else {//applet
				remoteData = (RemoteData) Naming.lookup(stringURL);
			}
		} catch (RemoteException re) {
			throw new JamException("Remote lookup up failed URL: " + stringURL);
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
		} catch (RemoteException re) {
			System.out.println(re.getMessage());
			throw new JamException("Remote getting histogram list [SetupRemote]");
		}
	}
	
	/**
	 * Link to a jam process
	 * for now just calls snap
	 * 
	 * @exception   JamException    all exceptions given to <code>JamException</code> go to the console
	 */
	public void link(String stringURL) throws JamException {
		snap(stringURL);
	}
	
	/**
	 * Not sure what needs to be done here.
	 */
	public void reset() {
//		remoteAccess = null;
//		remoteData = null;
	}
	
	/**
	 *
	 */
	public void setActive(boolean active) throws JamException {

		if (active) {
			if ((mode != SERVER) && (!inApplet)) {
				jamMain.setSortMode(JamMain.REMOTE);
			}
		} else {
			if ((mode != SERVER) && (!inApplet)) {
				jamMain.setSortMode(JamMain.NO_ACQ);
			}
		}
	}
	/**
	 * Locks up the Remote setup so the fields cannot be edited.
	 * and puts us in remote mode
	 *
	 * Author Ken Swartz
	 *
	 */
	private void lockFields(boolean lock) {
		if (lock) {
			setupLock = true;
			checkLock.setState(true);
			checkLock.setEnabled(true);
			textName.setEditable(false);
			textName.setBackground(Color.lightGray);
			bok.setEnabled(false);
			bapply.setEnabled(false);

		} else {
			setupLock = false;
			checkLock.setState(false);
			checkLock.setEnabled(false);
			textName.setEditable(true);
			textName.setBackground(Color.white);
			bok.setEnabled(true);
			bapply.setEnabled(true);

		}
	}
}
