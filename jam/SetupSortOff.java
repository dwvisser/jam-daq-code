/*
 */
package jam;
import jam.data.DataBase;
import jam.data.control.DataControl;
import jam.global.*;
import jam.sort.*;
import jam.sort.stream.EventInputStream;
import jam.sort.stream.EventOutputStream;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import java.util.*;

/**
 * Class to setup the offline sort process.
 *
 * @author Dale Visser
 * @author Ken Swartz
 */
class SetupSortOff  implements ActionListener, ItemListener {
    private String defaultSortPath, defaultSortRoutine, defaultEventInStream, 
    defaultEventOutStream, defaultEventPath, defaultSpectra, defaultTape;

    /* handles we need */
    private JamMain jamMain;
    private SortControl sortControl;
    private DisplayCounters displayCounters;
    private Broadcaster broadcaster;
    private MessageHandler msgHandler;
    SortDaemon sortDaemon;
    DiskDaemon diskDaemon;
    TapeDaemon tapeDaemon;
    StorageDaemon storageDaemon;

    /**
     * User sort routine must extend this abstract class
     */
    SortRoutine sortRoutine;//the actual sort routine
    File sortClassPath;//path to base of sort routines' classpath
    Class sortClass;
    //String sortClassName; //class name, including packages with '.' separator
    

    /** Input stream, how tells how to read an event */
    EventInputStream eventInput;

    /** Output stream, tells how to write an event */
    EventOutputStream eventOutput;

    File sortDirectory, eventDirectory;

    /**
     * Indicates event source: from DISK or TAPE.
     */
    private int mode;
    public static final int DISK=0;
    public static final int TAPE=1;

    /**
     * The path to the tape device.
     */
    String tapeDevice;

    // dialog box widgets
    private JDialog d;
    private JTextField textSortPath, textDev;
    private JCheckBox checkLock;
    private JToggleButton ctape, cdisk,defaultPath,specify;
    private JButton bok, bapply,bbrowsef;
    private JComboBox sortChoice, inStreamChooser, outStreamChooser;

    public SetupSortOff(JamMain jamMain,  SortControl sortControl,
    DisplayCounters displayCounters, Broadcaster broadcaster,
    MessageHandler msgHandler ) {
        defaultSortRoutine   =JamProperties.getPropString(JamProperties.SORT_ROUTINE);
        defaultSortPath = JamProperties.getPropString(JamProperties.SORT_CLASSPATH);
        defaultEventInStream=JamProperties.getPropString(JamProperties.EVENT_INSTREAM);
        defaultEventOutStream=JamProperties.getPropString(JamProperties.EVENT_OUTSTREAM);
        defaultEventPath =JamProperties.getPropString(JamProperties.EVENT_INPATH);
        defaultSpectra=JamProperties.getPropString(JamProperties.HIST_PATH);
        defaultTape   =JamProperties.getPropString(JamProperties.TAPE_DEV);
        boolean useDefaultPath=(defaultSortPath==JamProperties.DEFAULT_SORT_CLASSPATH);
        if (!useDefaultPath){
			sortDirectory=new File(defaultSortPath);  
			sortClassPath=sortDirectory;      	
        }
        this.jamMain=jamMain;
        this.sortControl=sortControl;
        this.displayCounters=displayCounters;
        this.broadcaster=broadcaster;
        this.msgHandler=msgHandler;

        d = new JDialog (jamMain,"Setup Offline",false);  //dialog box
        Container cp=d.getContentPane();

        d.setResizable(false);
        d.setLocation(20,50);
        cp.setLayout(new BorderLayout());

		LayoutManager verticalGrid=new GridLayout(0,1,5,5);
		JPanel pNorth=new JPanel(verticalGrid);
		cp.add(pNorth,BorderLayout.NORTH);
		JPanel pradio=new JPanel(new FlowLayout(FlowLayout.CENTER,5,5));
		ButtonGroup pathType=new ButtonGroup();
		defaultPath=new JRadioButton("Use help.* and sort.* in default classpath",useDefaultPath);
		specify=new JRadioButton("Specify a classpath",!useDefaultPath);
		defaultPath.setToolTipText("Don't include your sort routines in the default classpath if "+
		"you want to be able to edit, recompile and reload them without first quitting Jam.");
		specify.setToolTipText("Specify a classpath to dynamically load your sort routine from.");
		pathType.add(defaultPath);
		pathType.add(specify);
		defaultPath.addItemListener(this);
		specify.addItemListener(this);
		pradio.add(defaultPath);
		pradio.add(specify);
		pNorth.add(pradio);
		
		JPanel pCenter=new JPanel(new BorderLayout());
		cp.add(pCenter,BorderLayout.CENTER);
		JPanel pf = new JPanel(new BorderLayout());
		pCenter.add(pf,BorderLayout.NORTH);

        JLabel lf=new JLabel("Sort classpath", JLabel.RIGHT);
        pf.add(lf,BorderLayout.WEST);
        textSortPath =new JTextField(defaultSortPath);
        textSortPath.setToolTipText("Use Browse button to change. \nMay fail if classes have unresolvable references."+
        "\n* use the sort.classpath property in your JamUser.ini file to set this automatically.");
		textSortPath.setColumns(35);
		textSortPath.setEditable(false);
        pf.add(textSortPath,BorderLayout.CENTER);
		bbrowsef = new JButton("Browse");
		pf.add(bbrowsef,BorderLayout.EAST);
		bbrowsef.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				sortClassPath=getSortPath();
				sortChoice.setModel(new DefaultComboBoxModel(getSortClasses(sortClassPath)));
				sortChoice.setSelectedIndex(0);
				textSortPath.setText(sortClassPath.getAbsolutePath());
			}
		});
       


		JPanel pChooserArea =new JPanel(new BorderLayout());
		JPanel pChooserLabels=new JPanel(verticalGrid);
		pChooserArea.add(pChooserLabels,BorderLayout.WEST);
		JPanel pChoosers=new JPanel(verticalGrid);
		pChooserArea.add(pChoosers,BorderLayout.CENTER);
		pCenter.add(pChooserArea);

        pChooserLabels.add(new JLabel("Sort Routine",JLabel.RIGHT),BorderLayout.WEST);
        Vector v=getSortClasses(sortDirectory);
        sortChoice=new JComboBox(v);
        sortChoice.setToolTipText("Select a class to be your sort routine.");
        sortChoice.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				sortClass=(Class)sortChoice.getSelectedItem();
			}
        });
        Iterator it=v.iterator();
        boolean notDone=it.hasNext();
        while (notDone) {
        	Class c=(Class)it.next();
        	String name=c.getName();
        	boolean match = name.equals(defaultSortRoutine);
        	if (match){
				sortChoice.setSelectedItem(c);
        	} 
        	notDone = (!match) & it.hasNext();
        }
        pChoosers.add(sortChoice);
        
        pChooserLabels.add(new JLabel("Event input stream",JLabel.RIGHT));

        Set lhs=new LinkedHashSet(RTSI.find("jam.sort.stream",EventInputStream.class,false));
        lhs.remove(EventInputStream.class);
        inStreamChooser=new JComboBox(new Vector(lhs));
        inStreamChooser.setToolTipText("Select the reader for your event data format.");
		it=lhs.iterator();
		notDone=it.hasNext();
		while (notDone) {
			Class c=(Class)it.next();
			String name=c.getName();
			boolean match = name.equals(defaultEventInStream);
			if (match){
				inStreamChooser.setSelectedItem(c);
			} 
			notDone = (!match) & it.hasNext();
		}
        pChoosers.add(inStreamChooser);

        pChooserLabels.add(new JLabel("Event output stream",Label.RIGHT));

		lhs=new LinkedHashSet(RTSI.find("jam.sort.stream",EventOutputStream.class,false));
		lhs.remove(EventOutputStream.class);
		outStreamChooser=new JComboBox(new Vector(lhs));
		outStreamChooser.setToolTipText("Select the writer for your output event format.");
		it=lhs.iterator();
		notDone=it.hasNext();
		while (notDone) {
			Class c=(Class)it.next();
			String name=c.getName();
			boolean match = name.equals(defaultEventOutStream);
			if (match){
				outStreamChooser.setSelectedItem(c);
			} 
			notDone = (!match) & it.hasNext();
		}
		pChoosers.add(outStreamChooser);

        JPanel pselect=new JPanel();
        pselect.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
        pChooserArea.add(pselect,BorderLayout.SOUTH);
        JLabel ltd=new JLabel("Tape Device:");
        pselect.add(ltd);
        textDev=new JTextField(defaultTape);
        textDev.setColumns(12);
        pselect.add(textDev);

        ButtonGroup eventMode = new ButtonGroup();
        ctape=new JRadioButton("Events from Tape", false);
        ctape.setEnabled(false);
        ctape.setToolTipText("Not implemented.");
        eventMode.add(ctape);
        ctape.addItemListener(this);

        cdisk=new JRadioButton("Events from Disk", true);
        cdisk.setToolTipText("The only option until tape input is implemented.");
        eventMode.add(cdisk);
        cdisk.addItemListener(this);

        pselect.add(ctape);
        pselect.add(cdisk);

        JPanel pb=new JPanel();
        pb.setLayout(new GridLayout(1,0,5,5));
        cp.add(pb,BorderLayout.SOUTH);

        bok  =   new JButton("OK");
        pb.add(bok);
        bok.setActionCommand("ok");
        bok.addActionListener(this);

        bapply = new JButton("Apply");
        pb.add(bapply);
        bapply.setActionCommand("apply");
        bapply.addActionListener(this);

        JButton bcancel =new JButton(" Cancel ");
        pb.add(bcancel);
        bcancel.setActionCommand("cancel");
        bcancel.addActionListener(this);

        checkLock =new JCheckBox("Setup Locked", false );
        checkLock.setEnabled(false);
        checkLock.setActionCommand("checkLock");
        checkLock.addActionListener(this);
        pb.add(checkLock);

        d.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                d.dispose();
            }
        } );

        setMode(DISK);    //initial mode is from disk
        d.pack();
    }

	private Vector getSortClasses(File path) {
		return new Vector(RTSI.find(path, jam.sort.SortRoutine.class));
	}


    /**
     * method to show dialog box
     */
    public void show(){
        d.show();
    }

    /**
     * action performed on widget in dialog box
     *
     */
    public void actionPerformed(ActionEvent ae){
        String command=ae.getActionCommand();
        try {
            if (command=="ok"||command=="apply"){
                if (jamMain.canSetSortMode()) {
                    resetSort();//clear current data areas and kill daemons
                    loadNames();
                    loadSorter();
                    msgHandler.messageOutln("Loaded sort class '"+sortRoutine.getClass().getName()+
                    "', event instream class '"
                    +eventInput.getClass().getName()+"', and event outstream class '"+eventOutput.getClass().getName()+"'");
                    if (sortRoutine != null) {
                        setupSort();      //create data areas and daemons
                        msgHandler.messageOutln("Offline sorting setup");
                    }
                    jamMain.dataChanged();
                    if (command=="ok"){
                        d.dispose();
                    }
                } else {
                    throw new JamException("Can't set up sorting, mode locked.");
                }
            } else if (command=="checkLock") {
                if(!checkLock.isSelected()) {
                    resetSort();        //reset the sort, kill daemons, clear data areas
                } else {
                    System.err.println("Error should not be here [SetupSortOff]");
                }
            } else if (command=="cancel"){
                d.dispose();
            }
        } catch (SortException se){
            msgHandler.errorOutln(se.getMessage());
        } catch (JamException je){
            msgHandler.errorOutln(je.getMessage());
        } catch (GlobalException ge) {
            msgHandler.errorOutln(ge.getMessage());
        }
    }

    /**
     * Choice to unlock setup or
     * choice between tape and disk
     */
    public void itemStateChanged(ItemEvent ie){
    	ItemSelectable selectedItem=ie.getItemSelectable();
        if (selectedItem==ctape) {//set mode, disk or tape
            if(ctape.isSelected()){
                setMode(TAPE);
            }
        } else if (selectedItem==cdisk) {
            if(cdisk.isSelected()){
                setMode(DISK);
            }
        } else if (selectedItem==defaultPath && defaultPath.isSelected()){
        	bbrowsef.setEnabled(false);
        	setChooserDefault(true);
        } else if (selectedItem==specify && specify.isSelected()){
        	bbrowsef.setEnabled(true);
        	setChooserDefault(false);
        }
    }
    
    private void setChooserDefault(boolean isDefault){
    	if (isDefault){
    		Set set=new LinkedHashSet();
    		set.addAll(RTSI.find("help",SortRoutine.class,true));
    		set.addAll(RTSI.find("sort",SortRoutine.class,true));
    		Vector v=new Vector();
    		v.addAll(set);
    		sortChoice.setModel(new DefaultComboBoxModel(v));
    	} else {
			sortChoice.setModel(new DefaultComboBoxModel((Vector)getSortClasses(sortClassPath)));
    	}
    }

    /**
     * Loads the names of objects entered in the dialog box into String objects.
     */
    private void loadNames() throws JamException {
        tapeDevice=textDev.getText();
    }

    /**
     * Resolves the String objects into class names and loads the sorting class
     * and event streams.
     */
    private void loadSorter() throws JamException {
        try {
        	if (specify.isSelected()){
        		/* we call loadClass() in order to guarantee latest version */
				sortRoutine= (SortRoutine)RTSI.loadClass(sortClassPath,sortClass.getName()).newInstance();// create sort class
        	} else {//use default loader
        		sortRoutine=(SortRoutine)sortClass.newInstance();
        	}
        } catch (InstantiationException ie) {
            throw new JamException("Cannot instantiate sort routine: "+sortClass.getName());
        } catch (IllegalAccessException iae) {
            throw new JamException(" Cannot access sort routine: "+sortClass.getName());
        }
        try {//create new event input stream class
            eventInput= (EventInputStream) ((Class)inStreamChooser.getSelectedItem()).newInstance();
            eventInput.setConsole(msgHandler);
        } catch (InstantiationException ie) {
            ie.printStackTrace();
            throw new JamException("Cannot instantize event input stream: "+inStreamChooser.getSelectedItem());
        } catch (IllegalAccessException iae) {
            throw new JamException(" Cannot access event input stream: "+inStreamChooser.getSelectedItem());
        }
        try {//create new event output stream class
            eventOutput = (EventOutputStream) ((Class)outStreamChooser.getSelectedItem()).newInstance();
        } catch (InstantiationException ie) {
            throw new JamException("Cannot instantize event output stream: "+eventOutput.getClass().getName());
        } catch (IllegalAccessException iae) {
            throw new JamException(" Cannot access event output stream: "+eventOutput.getClass().getName());
        }
    }

    /**
     * Sets up the offline sort.
     */
    private void setupSort() throws SortException, JamException, GlobalException {
        String deviceName;

        try {
            sortRoutine.initialize();
        } catch (Exception e) {
            throw new JamException("Exception in SortRoutine: "+sortRoutine.getClass().getName()
            +".initialize(); Message= '"+e.getClass().getName()+": "+e.getMessage()+"'");
        }
        /* setup scaler, parameter, monitors, gate, dialog boxes */
        DataControl.setupAll();
        /* setup sorting */
        sortDaemon=new SortDaemon( sortControl,  msgHandler);
        sortDaemon.setup(SortDaemon.OFFLINE, eventInput, sortRoutine.getEventSize());
        sortDaemon.load(sortRoutine);
        /* eventInputStream to use get event size from sorting routine */
        eventInput.setEventSize(sortRoutine.getEventSize());
        eventInput.setBufferSize(sortRoutine.BUFFER_SIZE);
        /* give sortroutine output stream */
        eventOutput.setEventSize(sortRoutine.getEventSize());
        eventOutput.setBufferSize(sortRoutine.BUFFER_SIZE);
		sortRoutine.setEventOutputStream(eventOutput);
        /* always setup diskDaemon */
        diskDaemon =new DiskDaemon(sortControl,  msgHandler);
        diskDaemon.setupOff(eventInput, eventOutput);
        /* setup source of data tape */
        if(mode==TAPE){
            tapeDaemon = new TapeDaemon(sortControl, msgHandler);
            tapeDaemon.setDevice(tapeDevice);
            tapeDaemon.setupOff(eventInput, eventOutput);
            deviceName=tapeDevice;
            storageDaemon=tapeDaemon;
        } else {
            deviceName="Disk";
            storageDaemon=diskDaemon;
        }
        /* tell run control about all, disk always to device */
        sortControl.setup(this, sortDaemon, storageDaemon, diskDaemon, deviceName);
        /* tell status to setup */
        displayCounters.setupOff(sortDaemon, storageDaemon);
        /* tell sortDaemon to update status */
        sortDaemon.setObserver(broadcaster);
        /* start sortDaemon which is then suspended by Sort control until files entered */
        sortDaemon.start();
        /* lock setup */
        lockMode(true);
    }

    /**
     * Resets offline data aquisition.
     * Kills sort daemon. Clears all data areas: histograms, gates, scalers and monitors.
     */
    public void resetSort() throws JamException,GlobalException {
        if (sortDaemon != null) {
            sortDaemon.setState(GoodThread.STOP);
        }
        DataBase.clearAllLists();
        lockMode(false);
    }

    /**
     * Sets the mode for reading from either tape or disk.
     */
    private void setMode(int mode) {
        if (mode==TAPE) {
            this.mode=TAPE;
            sortControl.setDevice(SortControl.TAPE);
            textDev.setEditable(true);
        } else if (mode==DISK) {
            this.mode=DISK;
            sortControl.setDevice(SortControl.DISK);
            textDev.setEditable(false);
        }
    }

    /**
     * Browses for the sort file.
     */
    private File getSortPath(){
        JFileChooser fd =new JFileChooser(sortDirectory);
        fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fd.showOpenDialog(jamMain);
        //save current values
        if (option == JFileChooser.APPROVE_OPTION && fd.getSelectedFile() != null){
            sortDirectory=fd.getSelectedFile();//save current directory
        }
        return sortDirectory;
    }
	
    /**
     * Lock the setup if it is unlocked than the sort is stopped
     * Set the title bar to indicate offline sort and wether from tape
     * or disk
     *
     */
    private void lockMode(boolean lock) throws JamException {
        if(lock){
            if (mode==DISK) {
                jamMain.setSortMode(JamMain.OFFLINE_DISK);
            } else {
                jamMain.setSortMode(JamMain.OFFLINE_TAPE);
            }
            checkLock.setEnabled(true);
            checkLock.setSelected(true);
            inStreamChooser.setEnabled(false);
            outStreamChooser.setEnabled(false);
            cdisk.setEnabled(false);
            bok.setEnabled(false);
            bapply.setEnabled(false);
            specify.setEnabled(false);
            defaultPath.setEnabled(false);
            bbrowsef.setEnabled(false);
            sortChoice.setEnabled(false);
        } else{
            jamMain.setSortMode(JamMain.NO_ACQ);
            checkLock.setEnabled(false);
            inStreamChooser.setEnabled(true);
            outStreamChooser.setEnabled(true);
            cdisk.setEnabled(true);
            bok.setEnabled(true);
            bapply.setEnabled(true);
            specify.setEnabled(true);
            defaultPath.setEnabled(true);
            bbrowsef.setEnabled(specify.isSelected());
            sortChoice.setEnabled(true);
        }
    }
}