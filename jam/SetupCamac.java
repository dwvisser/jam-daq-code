/*
 */
package jam;

import jam.global.JamProperties;
import jam.global.MessageHandler;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
/** 
 * Class to setup the camac crate
 * You enter lists of cnafs which are parsed and 
 * written out to files that the VME crate loads
 *
 * There are four list of CNAFs
 *	INIT	run when the crate is inialized
 *	EVENT	run for every event
 *	SCALER	run to read the scaler
 *	CLEAR	run to clear the crate (typically just scalers)
 *	USER	run at user request (no way to request yet)
 *
 * @author Dale Visser
 * @author Ken Swartz
 * @version 0.5 last edit oct 98
 */

class SetupCamac implements ActionListener, ItemListener {

	String listPath;
	String cnafPath;

	String LIST_NAME = "*.ccf";

	final String EVENT_FILE = "/event.ccf";
	final String INIT_FILE = "/init.ccf";
	final String SCALER_FILE = "/scaler.ccf";
	final String CLEAR_FILE = "/clear.ccf";
	final String USER_FILE = "/user.ccf";

	final String INIT = "Init";
	final String EVENT = "Event";
	final String SCALERS = "Scalers";
	final String CLEAR = "Clear";
	final String USER = "User";

	final int HGAP = 5;
	final int VGAP = 5;
	final boolean MULT_MODE = false;

	private Frame frame;
	private FrontEndCommunication frontEnd;
	private MessageHandler msgHandler;

	private Dialog d;

	private CardLayout myCardLayout;
	private Checkbox cinit;
	private Checkbox cevent;
	private Checkbox cscaler;
	private Checkbox cclear;
	private Checkbox cuser;

	private List eventList;
	private List initList;
	private List scalerList;
	private List clearList;
	private List userList;
	private Panel listCards;
	private TextField entry;
	private Button badd;
	private Button binsert;
	private Button bremove;
	private Button bopen;
	private Button bok;
	private Button bapply;

	private int eventSize;
	private Label eventSizeText;
	private String frontCard;

	private String directoryName;
	private String fileName;

	public SetupCamac(
		Frame frame,
		FrontEndCommunication frontEnd,
		MessageHandler msgHandler) {
		listPath = JamProperties.getPropString(JamProperties.CAMAC_PATH);
		cnafPath = JamProperties.getPropString(JamProperties.CNAF_PATH);
		this.frame = frame;
		this.frontEnd = frontEnd;
		this.msgHandler = msgHandler;
		//dialog box			
		d = new Dialog(frame, "Setup Camac ", false);
		d.setForeground(Color.black);
		d.setBackground(Color.lightGray);
		d.setResizable(false);
		d.setLocation(20, 50);
		d.setSize(500, 300);
		d.setLayout(new BorderLayout(HGAP, VGAP));
		Panel dNorth = new Panel(new FlowLayout(FlowLayout.CENTER));
		CheckboxGroup whichList = new CheckboxGroup();
		cinit = new Checkbox(INIT, whichList, true);
		cinit.addItemListener(this);
		dNorth.add(cinit);
		cevent = new Checkbox(EVENT, whichList, false);
		cevent.addItemListener(this);
		dNorth.add(cevent);
		cscaler = new Checkbox(SCALERS, whichList, false);
		cscaler.addItemListener(this);
		dNorth.add(cscaler);
		cclear = new Checkbox(CLEAR, whichList, false);
		cclear.addItemListener(this);
		dNorth.add(cclear);
		cuser = new Checkbox(USER, whichList, false);
		cuser.addItemListener(this);
		dNorth.add(cuser);
		Panel dCenter = new Panel(new BorderLayout(HGAP, VGAP));
		Panel labelPanel = new Panel(new FlowLayout());
		Label lCNAF = new Label("C N A F Description", Label.CENTER);
		labelPanel.add(lCNAF);
		dCenter.add(labelPanel, BorderLayout.NORTH);
		myCardLayout = new CardLayout();
		listCards = new Panel(myCardLayout);
		Panel initCard = new Panel(new BorderLayout());
		initList = new List(10, MULT_MODE);
		initList.setBackground(Color.white);
		initCard.add(initList);
		Panel eventCard = new Panel(new BorderLayout());
		eventList = new List(10, MULT_MODE);
		eventList.setBackground(Color.white);
		eventCard.add(eventList);
		Panel scalerCard = new Panel(new BorderLayout());
		scalerList = new List(10, MULT_MODE);
		scalerList.setBackground(Color.white);
		scalerCard.add(scalerList);
		Panel clearCard = new Panel(new BorderLayout());
		clearList = new List(10, MULT_MODE);
		clearList.setBackground(Color.white);
		clearCard.add(clearList);
		Panel userCard = new Panel(new BorderLayout());
		userList = new List(10, MULT_MODE);
		userList.setBackground(Color.white);
		userCard.add(userList);
		listCards.add(initCard, INIT);
		listCards.add(eventCard, EVENT);
		listCards.add(scalerCard, SCALERS);
		listCards.add(clearCard, CLEAR);
		listCards.add(userCard, USER);
		myCardLayout.show(listCards, INIT);
		dCenter.add(listCards, BorderLayout.CENTER);
		Panel entryPanel = new Panel(new FlowLayout(FlowLayout.LEFT));
		Label lEntry = new Label("Entry: ");
		entryPanel.add(lEntry);
		entry = new TextField(45);
		entry.setBackground(Color.white);
		entryPanel.add(entry);
		dCenter.add(entryPanel, BorderLayout.SOUTH);
		Panel dSouth = new Panel(new FlowLayout());
		Button bsave = new Button(" Save ");
		bsave.setActionCommand("save");
		bsave.addActionListener(this);
		bopen = new Button(" Open ");
		bopen.setActionCommand("open");
		bopen.addActionListener(this);
		bok = new Button("   OK   ");
		bok.setActionCommand("ok");
		bok.addActionListener(this);
		bapply = new Button(" Apply  ");
		bapply.setActionCommand("apply");
		bapply.addActionListener(this);
		Button bcancel = new Button(" Cancel ");
		bcancel.setActionCommand("cancel");
		bcancel.addActionListener(this);
		dSouth.add(bsave);
		dSouth.add(bopen);
		dSouth.add(bok);
		dSouth.add(bapply);
		dSouth.add(bcancel);
		GridBagLayout gbEast = new GridBagLayout();
		GridBagConstraints gbcEast = new GridBagConstraints();
		gbcEast.insets = new Insets(5, 5, 5, 5);
		Panel cEast = new Panel(gbEast);
		Label sizeLabel = new Label("Event Size", Label.CENTER);
		eventSizeText = new Label("0", Label.CENTER);
		try {
			addComponent(
				cEast,
				sizeLabel,
				0,
				GridBagConstraints.RELATIVE,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.NORTH);
			addComponent(
				cEast,
				eventSizeText,
				0,
				GridBagConstraints.RELATIVE,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.NORTH);
		} catch (AWTException ae) {
			System.err.println(
				"Error creating Camac dialog box " + ae + "[SetupCamac]");
		}
		GridBagLayout gbWest = new GridBagLayout();
		GridBagConstraints gbcWest = new GridBagConstraints();
		gbcWest.insets = new Insets(5, 5, 5, 5);
		Panel cWest = new Panel();
		cWest.setLayout(gbWest);
		gbcWest.ipady = 5;
		cWest.setLayout(gbWest);
		badd = new Button(" Add ");
		badd.setActionCommand("add");
		badd.addActionListener(this);
		binsert = new Button(" Insert ");
		binsert.setActionCommand("insert");
		binsert.addActionListener(this);
		bremove = new Button(" Remove ");
		bremove.setActionCommand("remove");
		bremove.addActionListener(this);
		try {
			addComponent(
				cWest,
				badd,
				0,
				GridBagConstraints.RELATIVE,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.NORTH);
			addComponent(
				cWest,
				binsert,
				0,
				GridBagConstraints.RELATIVE,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.NORTH);
			addComponent(
				cWest,
				bremove,
				0,
				GridBagConstraints.RELATIVE,
				1,
				1,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.NORTH);
		} catch (AWTException ae) {
			System.err.println(
				"Error creating Camac dialog box " + ae + "[SetupCamac]");
		}
		dCenter.add(cWest, BorderLayout.WEST);
		dCenter.add(cEast, BorderLayout.EAST);
		d.add(dNorth, BorderLayout.NORTH);
		d.add(dCenter, BorderLayout.CENTER);
		d.add(dSouth, BorderLayout.SOUTH);
		//set first list to display	
		frontCard = INIT;
		//default camac path
		directoryName = listPath;
		fileName = LIST_NAME;
		d.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				d.dispose();
			}
		});
	}
	
	/**
	 * Show the setup Camac dialog box.
	 */
	public void show() {
		d.show();
	}
	
	/** 
	 * ActionPerformed called by a button press
	 *
	 * @param ae ActionEvent
	 */
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();
		try {

			if ((command == "ok") || (command == "apply")) {
				writeCNAF();
				msgHandler.messageOutln("Camac CNAF files written out");
				if (command == "ok") {
					d.dispose();
				}
			} else if (command == "cancel") {
				d.dispose();
			} else if (command == "save") {
				saveLists();
			} else if (command == "open") {
				loadLists();
				//commands for lists		
			} else if (command == "add") {
				if (frontCard.equals(INIT)) {
					initList.add(entry.getText());
				} else if (frontCard.equals(EVENT)) {
					eventList.add(entry.getText());
					setSize();
				} else if (frontCard.equals(CLEAR)) {
					clearList.add(entry.getText());
				} else if (frontCard.equals(USER)) {
					userList.add(entry.getText());
				} else if (frontCard.equals(SCALERS)) {
					scalerList.add(entry.getText());
				} else {
					System.err.println("ERROR unrecognised list [SetupCamac]");
				}
			} else if (command == "insert") {
				if (frontCard.equals(INIT)) {
					initList.add(entry.getText(), initList.getSelectedIndex());
				} else if (frontCard.equals(EVENT)) {
					eventList.add(
						entry.getText(),
						eventList.getSelectedIndex());
					setSize();
				} else if (frontCard.equals(CLEAR)) {
					clearList.add(
						entry.getText(),
						eventList.getSelectedIndex());
				} else if (frontCard.equals(USER)) {
					userList.add(entry.getText(), eventList.getSelectedIndex());
				} else {
					scalerList.add(
						entry.getText(),
						scalerList.getSelectedIndex());
				}
			} else if (command == "remove") {
				if (frontCard.equals(INIT)) {
					initList.remove(initList.getSelectedIndex());
				} else if (frontCard.equals(EVENT)) {
					eventList.remove(eventList.getSelectedIndex());
					setSize();
				} else if (frontCard.equals(CLEAR)) {
					clearList.remove(eventList.getSelectedIndex());
				} else if (frontCard.equals(USER)) {
					userList.remove(eventList.getSelectedIndex());
				} else {
					scalerList.remove(scalerList.getSelectedIndex());
				}
			}
		} catch (JamException je) {
			msgHandler.errorOutln(je.getMessage());
		}

	}

	/**
	 * A call to itemStateChanged,  
	 *
	 * @parameter ie ItemEvent
	 */

	public void itemStateChanged(ItemEvent ie) {
		//FIXME	try {
		if (ie.getItemSelectable() == cinit) {
			//cardLabel.setText(INIT);
			myCardLayout.show(listCards, INIT);
			frontCard = INIT;
		} else if (ie.getItemSelectable() == cevent) {
			//cardLabel.setText(EVENT);
			myCardLayout.show(listCards, EVENT);
			frontCard = EVENT;
		} else if (ie.getItemSelectable() == cscaler) {
			//cardLabel.setText(SCALERS);
			myCardLayout.show(listCards, SCALERS);
			frontCard = SCALERS;
		} else if (ie.getItemSelectable() == cclear) {
			//cardLabel.setText(SCALERS);
			myCardLayout.show(listCards, CLEAR);
			frontCard = CLEAR;
		} else if (ie.getItemSelectable() == cuser) {
			//cardLabel.setText(SCALERS);
			myCardLayout.show(listCards, USER);
			frontCard = USER;
		}
	}
	
	/**
	 * write the canfs to files which the VME crate can
	 * read back in
	 */
	private void writeCNAF() throws JamException {
		int i;

		int parameterNumber = 1;
		File initFile = new File(cnafPath, INIT_FILE);
		File eventFile = new File(cnafPath, EVENT_FILE);
		File scalerFile = new File(cnafPath, SCALER_FILE);
		File clearFile = new File(cnafPath, CLEAR_FILE);
		File userFile = new File(cnafPath, USER_FILE);
		try {
			FileWriter ifw = new FileWriter(initFile);
			FileWriter efw = new FileWriter(eventFile);
			FileWriter sfw = new FileWriter(scalerFile);
			FileWriter cfw = new FileWriter(clearFile);
			FileWriter ufw = new FileWriter(userFile);
			if (initList.getItemCount() > 0) {
				ifw.write("#\n");
				for (i = 0; i < initList.getItemCount(); i++) {
					ifw.write("# " + CNAFcomment(initList.getItem(i)) + "\n");
				}
				ifw.write("#\n");
				for (i = 0; i < initList.getItemCount(); i++) {
					ifw.write(CNAFPD(initList.getItem(i), 0) + "\n");
				}
			}
			ifw.close();
			if (scalerList.getItemCount() > 0) {
				sfw.write("#\n");
				for (i = 0; i < scalerList.getItemCount(); i++) {
					sfw.write("# " + CNAFcomment(scalerList.getItem(i)) + "\n");
				}
				sfw.write("#\n");
				for (i = 0; i < scalerList.getItemCount(); i++) {
					sfw.write(CNAFPD(scalerList.getItem(i), 0) + "\n");
				}
			}
			sfw.close();
			if (clearList.getItemCount() > 0) {
				cfw.write("#\n");
				for (i = 0; i < clearList.getItemCount(); i++) {
					cfw.write("# " + CNAFcomment(clearList.getItem(i)) + "\n");
				}
				cfw.write("#\n");
				for (i = 0; i < clearList.getItemCount(); i++) {
					cfw.write(CNAFPD(clearList.getItem(i), 0) + "\n");
				}
			}
			cfw.close();
			if (userList.getItemCount() > 0) {
				ufw.write("#\n");
				for (i = 0; i < userList.getItemCount(); i++) {
					ufw.write("# " + CNAFcomment(userList.getItem(i)) + "\n");
				}
				ufw.write("#\n");
				for (i = 0; i < userList.getItemCount(); i++) {
					ufw.write(CNAFPD(userList.getItem(i), 0) + "\n");
				}
			}
			ufw.close();
			if (eventList.getItemCount() > 0) {
				efw.write("#\n");
				for (i = 0; i < eventList.getItemCount(); i++) {
					efw.write("# " + CNAFcomment(eventList.getItem(i)) + "\n");
				}
				efw.write("#\n");
				for (i = 0; i < eventList.getItemCount(); i++) {
					if (isReadCommand(eventList.getItem(i))) {
						efw.write(
							CNAFPD(eventList.getItem(i), parameterNumber)
								+ "\n");
						parameterNumber++;
					} else {
						efw.write(CNAFPD(eventList.getItem(i), 0) + "\n");
					}
				}
			}
			efw.close();
		} catch (IOException ioe) {
			throw new JamException("Unable to write out CNAF files [SetupCamac]");

		}
	}
	
	/** 
	 * set up network to communicate with vme
	 */
	public void setupNet() throws JamException {
		frontEnd.setup();
	}
	
	/** 
	 * Parse a  line in the list into a CNAF
	 * The first 4 tokens on the line which must be integers 
	 * plus the parameter 
	 */
	private String CNAFPD(String cnaf, int parameter) throws JamException {
		StringReader sr;
		StreamTokenizer st;

		String temp = new String();
		try {
			sr = new StringReader(cnaf);
			st = new StreamTokenizer(sr);
			st.nextToken();
			temp = " " + temp + String.valueOf((int) st.nval) + " ";
			st.nextToken();
			temp = temp + String.valueOf((int) st.nval) + " ";
			st.nextToken();
			temp = temp + String.valueOf((int) st.nval) + " ";
			st.nextToken();
			temp =
				temp
					+ String.valueOf((int) st.nval)
					+ " "
					+ String.valueOf(parameter)
					+ " 0";
			return temp;
		} catch (IOException ioe) {
			throw new JamException(
				" Not able to parse CNAF command " + cnaf + " [SetupCamac]");
		}
	}

	/**
	 * cnaf comment thats all I know KBS
	 */
	private String CNAFcomment(String cnaf) throws JamException {
		StringReader sr;
		StreamTokenizer st;

		String comment = new String();
		try {
			sr = new StringReader(cnaf);
			st = new StreamTokenizer(sr);
			//go past the cnaf
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			do {
				st.nextToken();
				if (st.ttype == StreamTokenizer.TT_WORD) {
					comment = comment + " " + st.sval;
				} else if (st.ttype == StreamTokenizer.TT_NUMBER) {
					comment = comment + " " + String.valueOf(st.nval);
				} else if (st.ttype == StreamTokenizer.TT_EOL) {
					//XXX FIXME, why do we print here
					System.out.println("CNAF comment end of line token ");
					/*			*/
				}
			} while (st.ttype != StreamTokenizer.TT_EOF);

			return comment;

		} catch (IOException ioe) {
			throw new JamException(
				"Not able to parse comment for CNAF  "
					+ cnaf
					+ " [SetupCamac]");

		}
	}

	public int getEventSize() {
		return eventSize;
	}
	/**
	 * Save the list of cnafs to a file 
	 * .ccf   Camac Command File
	 * so user can at a later time read them back in
	 *
	 *
	 */
	private void saveLists() throws JamException {

		int i;

		FileDialog sd =
			new FileDialog(frame, "Save lists to file", FileDialog.SAVE);

		if (directoryName != null) {

			sd.setDirectory(directoryName);

		}
		if (fileName != null) {

			sd.setFile(fileName);

		} else {
			sd.setFile(LIST_NAME);
		}
		sd.show();

		try {
			if (sd.getFile() != null) {

				directoryName = sd.getDirectory();
				fileName = sd.getFile();

				FileWriter saveStream =
					new FileWriter(sd.getDirectory() + sd.getFile());

				saveStream.write(INIT + "\n");
				for (i = 0; i < initList.getItemCount(); i++) {
					saveStream.write(initList.getItem(i) + "\n");
				}
				saveStream.write(EVENT + "\n");
				for (i = 0; i < eventList.getItemCount(); i++) {
					saveStream.write(eventList.getItem(i) + "\n");
				}
				saveStream.write(SCALERS + "\n");
				for (i = 0; i < scalerList.getItemCount(); i++) {
					saveStream.write(scalerList.getItem(i) + "\n");
				}
				saveStream.write(CLEAR + "\n");
				for (i = 0; i < clearList.getItemCount(); i++) {
					saveStream.write(clearList.getItem(i) + "\n");
				}
				saveStream.write(USER + "\n");
				for (i = 0; i < userList.getItemCount(); i++) {
					saveStream.write(userList.getItem(i) + "\n");
				}

				saveStream.close();
				sd.dispose();
			}
		} catch (IOException ioe) {
			throw new JamException(
				"Not able to save list, file: "
					+ sd.getFile()
					+ " [SetupCamac]");

		}
	}
	/**
	 * Load the list of cnafs from a file
	 * .ccf Camac Command file
	 * A file previously written out by jam 
	 *
	 */
	private void loadLists() throws JamException {
		String listItem;

		int mode = 1;
		FileDialog ld =
			new FileDialog(frame, "Add list from file", FileDialog.LOAD);
		if (directoryName != null) {
			ld.setDirectory(directoryName);
		}
		if (fileName != null) {
			ld.setFile(fileName);
		} else {
			ld.setFile(LIST_NAME);
		}
		ld.show();
		try {
			if (ld.getFile() != null) {
				directoryName = ld.getDirectory();
				fileName = ld.getFile();
				BufferedReader br =
					new BufferedReader(
						new FileReader(ld.getDirectory() + ld.getFile()));
				initList.removeAll();
				eventList.removeAll();
				scalerList.removeAll();
				clearList.removeAll();
				userList.removeAll();
				do {
					listItem = br.readLine();
					if (listItem != null) {
						if (listItem.equals(INIT)) {
							mode = 1;
						} else if (listItem.equals(EVENT)) {
							mode = 2;
						} else if (listItem.equals(SCALERS)) {
							mode = 3;
						} else if (listItem.equals(CLEAR)) {
							mode = 4;
						} else if (listItem.equals(USER)) {
							mode = 5;
						} else if (mode == 1) {
							initList.add(listItem);
						} else if (mode == 2) {
							eventList.add(listItem);
						} else if (mode == 3) {
							scalerList.add(listItem);
						} else if (mode == 4) {
							clearList.add(listItem);
						} else if (mode == 5) {
							userList.add(listItem);
						}
					}
				} while (listItem != null);
				br.close();
				setSize();
				ld.dispose();
			}
		} catch (IOException ioe) {
			throw new JamException(
				"Unable to read file " + ld.getFile() + " [SetupCamac]");
		}
	}

	private void setSize() throws JamException {

		int i;
		int f;
		int size = 0;
		String item = "";
		String sizeText;
		StringReader sr;
		StreamTokenizer st;

		try {
			for (i = 0; i < eventList.getItemCount(); i++) {

				item = eventList.getItem(i);
				sr = new StringReader(item);
				st = new StreamTokenizer(sr);
				st.nextToken();
				st.nextToken();
				st.nextToken();
				st.nextToken();
				if (st.ttype == StreamTokenizer.TT_NUMBER) {
					f = (int) st.nval;
				} else {
					f = -1;
					throw new JamException(
						"function of CNAF is not a number "
							+ item
							+ " [SetupCamac]");
				}
				if (f == 0 || f == 2) {
					size++;
				}
			}

			eventSize = size;
			sizeText = String.valueOf(size);
			eventSizeText.setText(sizeText);

		} catch (IOException ioe) {
			throw new JamException(
				"Determining size of an event, CNAF " + item + " [SetupCamac]");

		}
	}

	private boolean isReadCommand(String cnaf) throws JamException {
		int f = -1;

		StringReader sr;
		StreamTokenizer st;
		try {
			sr = new StringReader(cnaf);
			st = new StreamTokenizer(sr);
			st.nextToken();
			st.nextToken();
			st.nextToken();
			st.nextToken();
			if (st.ttype == StreamTokenizer.TT_NUMBER) {
				f = (int) st.nval;
			} else {
				throw new JamException(
					"Not a integer function for CNAF "
						+ cnaf
						+ " [SetupCamac]");
			}
			return (f == 0 || f == 2); //FIXME for what f should we return true

		} catch (IOException ioe) {
			throw new JamException("Reading CNAF " + cnaf + " [SetupCamac]");
		}
	}

	/**
	 * Lock the setup window so no more edits can be made.
	 */
	void lockFields(boolean lock) {

		if (lock) {

			badd.setEnabled(false);
			binsert.setEnabled(false);
			bremove.setEnabled(false);
			entry.setEnabled(false);
			bopen.setEnabled(false);
			bok.setEnabled(false);
			bapply.setEnabled(false);

			initList.setBackground(Color.lightGray);
			eventList.setBackground(Color.lightGray);
			scalerList.setBackground(Color.lightGray);
			clearList.setBackground(Color.lightGray);
			userList.setBackground(Color.lightGray);
			entry.setBackground(Color.lightGray);

		} else {
			badd.setEnabled(true);
			binsert.setEnabled(true);
			bremove.setEnabled(true);
			entry.setEnabled(true);
			bopen.setEnabled(true);
			bok.setEnabled(true);
			bapply.setEnabled(true);

			initList.setBackground(Color.white);
			eventList.setBackground(Color.white);
			scalerList.setBackground(Color.white);
			clearList.setBackground(Color.white);
			userList.setBackground(Color.white);
			entry.setBackground(Color.white);

		}
	}

	/**
	 * Helper method for GridBagConstains 
	 * S
	 * @return  <code>void</code> 
	 * @since Version 0.5
	 */

	private static void addComponent(
		Container container,
		Component component,
		int gridx,
		int gridy,
		int gridwidth,
		int gridheight,
		int fill,
		int anchor)
		throws AWTException {

		LayoutManager lm = container.getLayout();

		if (!(lm instanceof GridBagLayout)) {

			throw new AWTException("Invaid layout" + lm);

		} else {

			GridBagConstraints gbc = new GridBagConstraints();

			gbc.ipady = 5;

			gbc.gridx = gridx;

			gbc.gridy = gridy;

			gbc.gridwidth = gridwidth;

			gbc.gridheight = gridheight;

			gbc.fill = fill;

			gbc.anchor = anchor;

			((GridBagLayout) lm).setConstraints(component, gbc);

			container.add(component);

		}

	}

}
