/*
 */
package jam;

import jam.data.Gate;
import jam.data.Histogram;
import jam.data.RemoteData;
import jam.global.JamStatus;
import jam.global.SortMode;

import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.ItemSelectable;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * Class to make this process into a remote server for Jam or hookup to a remote
 * online acquisition that is a server.
 * 
 * @author Ken Swartz
 */
public class SetupRemote extends JDialog implements ActionListener,
		ItemListener {

	private static enum Mode {
		LINK, SERVER, SNAP
	}

	static final String DEFAULT_NAME = "jam";

	static final String DEFAULT_URL = "rmi://meitner.physics.yale.edu/jam";

	private static SetupRemote instance;

	private static final Logger LOGGER = Logger.getLogger(SetupRemote.class.getPackage().getName());

	private static final JamStatus STATUS = JamStatus.getSingletonInstance();

	/**
	 * 
	 * @return the only instance of this class
	 */
	public static SetupRemote getInstance() {
		if (instance == null) {
			instance = new SetupRemote();
		}
		return instance;
	}

	private transient final JButton bapply, bok;

	private transient final JCheckBox checkLock, clink, cserver, csnap;

	private transient final boolean inApplet; // are we running in a applet

	private transient final JLabel lname;

	private transient Mode mode; // mode server, snap or link

	transient RemoteAccess remoteAccess;

	transient RemoteData remoteData;

	private transient final JTextField textName;

	/**
	 * Constructor for Jam Application creates dialog box, we are in an
	 * application
	 */
	public SetupRemote() {
		super(STATUS.getFrame(), "Remote Hookup ", false);
		// create dialog box
		setResizable(false);
		setLocation(20, 50);
		setSize(400, 250);
		final Container contents = getContentPane();
		contents.setLayout(new GridLayout(0, 1, 10, 10));
		// panel for mode
		final Panel panelM = new Panel();
		panelM.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		contents.add(panelM);
		final ButtonGroup cbgmode = new ButtonGroup();
		cserver = new JCheckBox("Server  ", true);
		cbgmode.add(cserver);
		cserver.addItemListener(this);
		panelM.add(cserver);
		csnap = new JCheckBox("SnapShot", false);
		cbgmode.add(csnap);
		csnap.addItemListener(this);
		panelM.add(csnap);
		clink = new JCheckBox("Link    ", false);
		cbgmode.add(clink);
		clink.addItemListener(this);
		panelM.add(clink);
		// panel for name
		final Panel panelN = new Panel();
		panelN.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		contents.add(panelN);
		lname = new JLabel("Name:", Label.RIGHT);
		panelN.add(lname);
		textName = new JTextField(DEFAULT_NAME);
		textName.setColumns(35);
		textName.setBackground(Color.white);
		panelN.add(textName);
		// panel for buttons
		final Panel panelB = new Panel();
		panelB.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		contents.add(panelB);
		bok = new JButton("   OK   ");
		panelB.add(bok);
		bok.setActionCommand("ok");
		bok.addActionListener(this);
		bapply = new JButton(" Apply  ");
		panelB.add(bapply);
		bapply.setActionCommand("apply");
		bapply.addActionListener(this);
		Button bcancel = new Button(" Cancel ");
		panelB.add(bcancel);
		bcancel.setActionCommand("cancel");
		bcancel.addActionListener(this);
		checkLock = new JCheckBox("Setup Locked", false);
		checkLock.setEnabled(false);
		checkLock.addItemListener(this);
		panelB.add(checkLock);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		mode = Mode.SERVER;
		inApplet = false;
	}

	public void actionPerformed(final ActionEvent event) {
		final String command = event.getActionCommand();
		final String name = textName.getText().trim();
		try {
			if (command == "ok" || command == "apply") {
				if (mode == Mode.SERVER) {
					server(name);
					LOGGER.info("Jam made as server: " + name);
				} else if (mode == Mode.SNAP) {
					LOGGER.info("Trying " + name);
					snap(name);
					LOGGER.info("Jam remote snapShot: " + name);
				} else if (mode == Mode.LINK) {
					LOGGER.info("Trying " + name);
					link(textName.getText().trim());
					LOGGER.info("Jam remote link: " + name);
				}

				setActive(true);
				lockFields(true);

				if (command == "ok") {
					dispose();
				}
			} else if (command == "cancel") {
				dispose();
			}
		} catch (JamException je) {
			LOGGER.log(Level.SEVERE, je.getMessage(), je);
		}
	}

	/**
	 * What mode has been picked.
	 */
	public void itemStateChanged(final ItemEvent event) {
		final ItemSelectable item = event.getItemSelectable();
		if (item == cserver) {
			mode = Mode.SERVER;
			lname.setText("Name:");
			textName.setText(DEFAULT_NAME);
		} else if (item == csnap) {
			mode = Mode.SNAP;
			lname.setText("URL:");
			textName.setText(DEFAULT_URL);
		} else if (item == clink) {
			mode = Mode.LINK;
			lname.setText("URL:");
			textName.setText(DEFAULT_URL);
			// lock up state
		} else if (item == checkLock) {
			setActive((checkLock.isSelected()));
			lockFields((checkLock.isSelected()));

			if (!(checkLock.isSelected())) {
				reset();
			}

		}
	}

	/**
	 * Link to a jam process for now just calls snap
	 * 
	 * @param url
	 *            location of service
	 * @exception JamException
	 *                all exceptions given to <code>JamException</code> go to
	 *                the console
	 */
	public void link(final String url) throws JamException {
		snap(url);
	}

	/**
	 * Locks up the Remote setup so the fields cannot be edited. and puts us in
	 * remote mode
	 * 
	 * @param lock
	 *            whether to lock fields
	 * @author Ken Swartz
	 */
	private void lockFields(final boolean lock) {
		checkLock.setSelected(lock);
		checkLock.setEnabled(lock);
		final boolean notLock = lock;
		textName.setEditable(notLock);
		textName.setEnabled(notLock);
		bok.setEnabled(notLock);
		bapply.setEnabled(notLock);
	}

	/**
	 * Not sure what needs to be done here.
	 */
	public void reset() {
		// remoteAccess = null;
		// remoteData = null;
	}

	/**
	 * Create a histogram server.
	 * 
	 * @param name
	 *            name to give the server process
	 * @exception JamException
	 *                sends a message to the console if there is any problem
	 *                setting up
	 */
	public void server(final String name) throws JamException {
		try {
			remoteAccess = new RemoteAccess();
			Naming.rebind(name, remoteAccess);
			lockFields(true);
		} catch (UnknownHostException unhe) {
			throw new JamException(
					"Creating remote server unknown host, name: " + name
							+ " [SetupRemote]", unhe);
		} catch (RemoteException re) {
			throw new JamException("Creating remote server " + re.getMessage()
					+ " [SetupRemote]", re);
		} catch (java.net.MalformedURLException mue) {
			throw new JamException(
					"Creating remote Server malformed URL [SetupRemote]");
		}
	}

	/**
	 * Set whether we are active. (?)
	 * 
	 * @param active
	 *            <code>true</code> if active
	 */
	public void setActive(final boolean active) {
		if ((mode != Mode.SERVER) && (!inApplet)) {
			if (active) {
				STATUS.setSortMode(SortMode.REMOTE, "Remote");
			} else {
				STATUS.setSortMode(SortMode.NO_SORT, "No Sort");
			}
		}
	}

	/**
	 * Get a snap shot of data.
	 * 
	 * @param url
	 *            location of service
	 * @exception JamException
	 *                all exceptions given to <code>JamException</code> go to
	 *                the console
	 */
	public void snap(final String url) throws JamException {
		try {
			if (STATUS.canSetup()) {
				remoteData = (RemoteData) Naming.lookup(url);
				LOGGER.info("Remote lookup OK!");
			} else {
				throw new JamException(
						"Can't view remotely, sort mode locked [SetupRemote]");
			}
		} catch (RemoteException re) {
			throw new JamException("Remote lookup up failed URL: " + url);
		} catch (java.net.MalformedURLException mue) {
			throw new JamException("Remote look up malformed URL: " + url);
		} catch (NotBoundException nbe) {
			throw new JamException("Remote look up could not find name " + url);
		}
		try {
			Histogram.setHistogramList(remoteData.getHistogramList());
			Gate.setGateList(remoteData.getGateList());
		} catch (RemoteException re) {
			throw new JamException(
					"Remote getting histogram list [SetupRemote]", re);
		}
	}
}