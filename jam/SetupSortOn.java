package jam;
import jam.data.DataBase;
import jam.data.control.DataControl;
import jam.global.GlobalException;
import jam.global.GoodThread;
import jam.global.JamProperties;
import jam.global.MessageHandler;
import jam.io.ExtensionFileFilter;
import jam.sort.DiskDaemon;
import jam.sort.NetDaemon;
import jam.sort.RingBuffer;
import jam.sort.SortDaemon;
import jam.sort.SortException;
import jam.sort.SortRoutine;
import jam.sort.StorageDaemon;
import jam.sort.TapeDaemon;
import jam.sort.VME_Map;
import jam.sort.stream.EventInputStream;
import jam.sort.stream.EventOutputStream;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Class to setup online sorting.
 * Loads a sort file and
 * creates the daemons:
 * <ul>
 * <li>net</li><li>sort</li><li>tape</li>
 * </ul>
 *
 * @author Ken Swartz
 * @author Dale Visser
 * @version 05 newest done 9-98
 * @see jam.sort.NetDaemon
 * @see jam.sort.StorageDaemon
 */
class SetupSortOn implements ActionListener, ItemListener {

    //handles to classes we need
    private JamMain jamMain;
    private RunControl runControl;
    private DisplayCounters displayCounters;
    private JamConsole jamConsole;
    private MessageHandler msgHandler;
    private FrontEndCommunication frontEnd;

    private static final int FILE_TEXT_COLUMNS = 25;

    //stuff for dialog box
    private JDialog d;
    File eventDirectory;

    //strings of data entered
    String experimentName, sortFile, eventInFile, eventOutFile;
    File sortDirectory;
    String histDirectory, dataDirectory;
    String logDirectory;
    // 1/fraction of events to sort
    private int sortInterval;
        String eventFile;

    //text fields
    private JTextField textExpName, textSortFile, textEventInStream, textPathHist, textPathData,
    textEventOutStream;
    private JTextField textPathLog;
    
    /**
     * Whether to handle event writing or delegate to front end.
     */
     boolean storeEventsLocally;
     
    //buttons and check boxes
    private JRadioButton ctape;    //save events to tape
    private JRadioButton cdisk;    //save events to disk
    private JCheckBox clog;    //create a log file
    private JTextField textSortInterval;
    private JButton bok;
    private JButton bapply;
    private JCheckBox checkLock;

    //browse ?
    private String diskPathData;
    private String tapePathData;
    private String logFile;
    private String separator;

    //sorting classes
    SortDaemon sortDaemon;
    NetDaemon netDaemon;
    DiskDaemon diskDaemon;
    TapeDaemon tapeDaemon;
    StorageDaemon storageDaemon;
    SortRoutine sortClass;

    //ring buffers
    RingBuffer sortingRing;
    RingBuffer storageRing;
    //streams to read and write events
    EventInputStream   eventInputStream;
    EventOutputStream   eventOutputStream;

    //tape or disk
    private boolean tapeMode=false;

    //default names
    private String defaultName, defaultSort, defaultEventInStream, defaultEvents, defaultSpectra;
    private String defaultTape, defaultEventOutStream;
    private String defaultLog;
    
    /**
     * Constructor
     */
    public SetupSortOn(JamMain jamMain , RunControl runControl, DisplayCounters displayCounters,
    FrontEndCommunication frontEnd, JamConsole jamConsole ) {
        defaultName   =JamProperties.getPropString(JamProperties.EXP_NAME);
        defaultSort   =JamProperties.getPropString(JamProperties.SORT_CLASS);
        defaultEventInStream=JamProperties.getPropString(JamProperties.EVENT_INSTREAM);
        defaultEventOutStream=JamProperties.getPropString(JamProperties.EVENT_OUTSTREAM);
        defaultEvents =JamProperties.getPropString(JamProperties.EVENT_OUTPATH);
        defaultSpectra=JamProperties.getPropString(JamProperties.HIST_PATH);
        defaultTape   =JamProperties.getPropString(JamProperties.TAPE_DEV);
		defaultLog = JamProperties.getPropString(JamProperties.LOG_PATH);
		File f=new File(defaultLog);
        String storageType = JamProperties.getPropString(JamProperties.EVENT_WRITER);
        if (storageType.equals(JamProperties.STORE_EVENTS_LOCALLY)) {
            storeEventsLocally=true;
        } else if (storageType.equals(JamProperties.STORE_EVENTS_FRONTEND)) {
            storeEventsLocally=false;
        } else {
            storeEventsLocally=true;
            jamConsole.warningOutln(JamProperties.EVENT_WRITER +
            " set to invalid value \""+storageType+"\".  Valid values are \""+
            JamProperties.STORE_EVENTS_LOCALLY+"\" or \""+
            JamProperties.STORE_EVENTS_FRONTEND+"\". Defaulting to writing events locally.");
        }
        this.jamMain=jamMain;
        this.runControl=runControl;
        this.displayCounters=displayCounters;
        this.jamConsole=jamConsole;
        this.msgHandler=jamConsole;
        this.frontEnd=frontEnd;
        tapePathData=defaultTape;
        d =new JDialog (jamMain,"Setup Online ",false);
        d.setForeground(Color.black);
        d.setBackground(Color.lightGray);
        d.setResizable(false);
        d.setLocation(20,50);
        Container dcp=d.getContentPane();
        dcp.setLayout(new GridLayout(0,1,5,5));

        //panel for experiment name
        JPanel pn= new JPanel();
        pn.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
        dcp.add(pn);

        JLabel ln=new JLabel("        Experiment Name",Label.RIGHT);
        pn.add(ln);

        textExpName =new JTextField(defaultName);
        textExpName.setColumns(20);
        textExpName.setBackground(Color.white);
        pn.add(textExpName);
        pn.add(new Label("              "));

        // panel for sort file
        JPanel pf= new JPanel();
        pf.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
        dcp.add(pf);

        JLabel lf=new JLabel("Sort file (.class)",Label.RIGHT);
        pf.add(lf);

        textSortFile =new JTextField(defaultSort);
        textSortFile.setColumns(FILE_TEXT_COLUMNS);
        textSortFile.setBackground(Color.white);
        pf.add(textSortFile);

        JButton bbrowsef = new JButton(" Browse ");
        pf.add(bbrowsef);
        bbrowsef.setActionCommand("bsort");
        bbrowsef.addActionListener(this);

        // panel input stream
        JPanel pin= new JPanel();
        pin.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
        dcp.add(pin);

        pin.add(new JLabel("Event input stream (.class)",JLabel.LEFT));

        textEventInStream = new JTextField(defaultEventInStream);
        textEventInStream.setColumns(FILE_TEXT_COLUMNS);
        textEventInStream.setBackground(Color.white);
        textEventInStream.setForeground(Color.black);
        pin.add(textEventInStream);

        JButton bbrowsein = new JButton("Browse");
        pin.add(bbrowsein);
        bbrowsein.setActionCommand("binstream");
        bbrowsein.addActionListener(this);

        JPanel pout = new JPanel();
        pout.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
        dcp.add(pout);

        JLabel lout=new JLabel("",Label.LEFT);
        lout.setText("Event output stream (.class)");
        pout.add(lout);

        textEventOutStream =new JTextField(defaultEventOutStream);
        textEventOutStream.setColumns(FILE_TEXT_COLUMNS);
        textEventOutStream.setBackground(Color.white);
        textEventOutStream.setForeground(Color.black);
        pout.add(textEventOutStream);

        JButton bbrowseout = new JButton("Browse");
        pout.add(bbrowseout);
        bbrowseout.setActionCommand("boutstream");
        bbrowseout.addActionListener(this);

        // panel histogram path
        JPanel ph= new JPanel();
        ph.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
        dcp.add(ph);

        JLabel lh=new JLabel("Path for Histograms",Label.RIGHT);
        ph.add(lh);

        textPathHist =new JTextField(defaultSpectra);
        textPathHist.setColumns(FILE_TEXT_COLUMNS);
        textPathHist.setBackground(Color.white);
        ph.add(textPathHist);

        JButton bbrowseh = new JButton(" Browse ");
        ph.add(bbrowseh);
        bbrowseh.setActionCommand("bhist");
        bbrowseh.addActionListener(this);

        // panel data path
        JPanel pd= new JPanel();
        pd.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
        dcp.add(pd);

        JLabel ld=new JLabel("Path for Events",Label.RIGHT);
        pd.add(ld);

        textPathData= new JTextField(defaultEvents);
        textPathData.setColumns(FILE_TEXT_COLUMNS);
        textPathData.setBackground(Color.white);
        pd.add(textPathData);

        JButton bbrowsed = new JButton(" Browse ");
        pd.add(bbrowsed);
        bbrowsed.setActionCommand("bdata");
        bbrowsed.addActionListener(this);

        // panel log path
        JPanel pl= new JPanel();
        pl.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
        dcp.add(pl);

        JLabel ll=new JLabel("Path for Log Files",Label.RIGHT);
        pl.add(ll);

        textPathLog= new JTextField(defaultLog);
        textPathLog.setColumns(FILE_TEXT_COLUMNS);
        textPathLog.setBackground(Color.white);
        pl.add(textPathLog);

        JButton bbrowsel = new JButton(" Browse ");
        pl.add(bbrowsel);
        bbrowsel.setActionCommand("blog");
        bbrowsel.addActionListener(this);

        // panel for storage mode
        JPanel pt= new JPanel();
        pt.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
        dcp.add(pt);

        textSortInterval= new JTextField("1");
        textSortInterval.setColumns(3);
        textSortInterval.setBackground(Color.white);
        pt.add(textSortInterval);

        JLabel lsi=new JLabel("Sort Sample ",Label.LEFT);
        pt.add(lsi);

        ButtonGroup eventMode = new ButtonGroup();
        ctape=new JRadioButton("Events to Tape", false);
        eventMode.add(ctape);
        ctape.addItemListener(this);
        pt.add(ctape);

        cdisk=new JRadioButton("Events to Disk", true);
        eventMode.add(cdisk);
        cdisk.addItemListener(this);
        pt.add(cdisk);

        clog = new JCheckBox("Log Commands", false);
        clog.addItemListener(this);
        clog.setSelected(true);
        pt.add(clog);

        // panel for buttons
        JPanel pb= new JPanel();
        pb.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
        dcp.add(pb);

        bok  =   new JButton("   OK   ");
        bok.setActionCommand("ok");
        bok.addActionListener(this);
        pb.add(bok);

        bapply = new JButton(" Apply  ");
        bapply.setActionCommand("apply");
        bapply.addActionListener(this);
        pb.add(bapply);

        JButton bcancel =new JButton(" Cancel ");
        pb.add(bcancel);
        bcancel.setActionCommand("cancel");
        bcancel.addActionListener(this);

        checkLock =new JCheckBox("Setup Locked", false );
        checkLock.addItemListener(this);
        checkLock.setEnabled(false);
        pb.add(checkLock);

        d.pack();
        
        separator="/";  //default path separator
        separator=System.getProperty("file.separator",separator);

        //Recieves events for closing the dialog box and closes it.
        d.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                d.dispose();
                d.pack();
            }
        }
        );
    }

    /**
     * Show online sorting dialog Box.
     *
     */
    public void show(){
        d.show();
    }

    /**
     * Receives events from this dialog box.
     *
     * @Author Ken Swartz
     *
     */
    public void actionPerformed(ActionEvent ae){
        String command=ae.getActionCommand();
        try {
            if (command=="bsort") {
                sortFile=getSortFile();
                textSortFile.setText(sortDirectory+sortFile);
            } else if (command=="blog") {
                String logDirectory=getPathLog();
                textPathLog.setText(logDirectory);
            } else if (command=="binstream") {
                //eventDirectory=getEventStream();
                textEventInStream.setText(getEventStream());//note side effect: eventDirectory gets set
            } else if (command=="boutstream") {
                //eventDirectory=getEventStream();
                textEventOutStream.setText(getEventStream());//note side effect: eventDirectory gets set
            } else if (command=="bhist") {
                histDirectory=getPathHist();
                textPathHist.setText(histDirectory);
            } else if (command=="bdata") {
                dataDirectory=getPathData();
                textPathData.setText(dataDirectory);
            } else if (command=="ok"||command=="apply"){
                //lock setup so fields cant be edited
                if(jamMain.canSetSortMode()){
                    loadNames();
                    if(clog.isSelected()==true) {//if needed start logging to file
                        logFile=JamProperties.getProperty(JamProperties.LOG_PATH)+File.separator+experimentName;
                        logFile=jamConsole.setLogFileName( logFile );
                        jamConsole.messageOutln("Logging to file: "+logFile);
                        jamConsole.setLogFileOn(true);
                    } else {
                        jamConsole.setLogFileOn(false);
                    }
                    jamConsole.messageOutln("Setup Online Data Acquisition,  Experiment Name: "
                    +experimentName);
                    loadSorter();//load sorting routine
                    jamConsole.messageOutln("Loaded sort class: "+sortFile+".class");
                    if (sortClass != null) {
                        resetAcq();//Kill all existing Daemons and clear data areas
                        setupAcq();//create daemons
                        if (sortClass.getEventSizeMode()==SortRoutine.SET_BY_CNAF) {
                            setupCamac();//set the camac crate
                        } else if (sortClass.getEventSizeMode()==SortRoutine.SET_BY_VME_MAP) {
                            setupVME_Map();
                        }
                        lockMode(true);
                        jamMain.dataChanged();
                        jamConsole.messageOutln("Setup data network, and Online sort daemons setup");
                    }
                    if (command=="ok"){
                        d.dispose();
                    }
                } else {
                    throw new JamException("Can't setup sorting, mode locked ");
                }
            } else if (command=="cancel"){
                d.dispose();
            }
        } catch (SortException je){
            msgHandler.errorOutln(je.getMessage());
            //je.printStackTrace();
        } catch (JamException je) {
            jamConsole.errorOutln(je.getMessage());
            //je.printStackTrace();
        } catch (GlobalException ge) {
            jamConsole.errorOutln(ge.getMessage());
            //ge.printStackTrace();
        }
    }

    /**
     * Recieves events from this check box.
     *
     * @Author Ken Swartz
     */
    public void itemStateChanged(ItemEvent ie){
        try {
            if (ie.getItemSelectable()==checkLock) {
                if(checkLock.isSelected()==false) {
                    //kill daemons, clear data areas
                    resetAcq();
                    //unlock sort mode
                    lockMode(false);
                    jamConsole.closeLogFile();
                }
            } else if (ie.getItemSelectable()==ctape) {
                setTapeMode(ctape.isSelected());
            }
        } catch (JamException je) {
            jamConsole.errorOutln(je.getMessage());
        } catch (GlobalException ge) {
            jamConsole.errorOutln(ge.getMessage());
        }
    }

    /**
     * Save the names of the experiment, the sort file
     * and the event and histogram directories.
     */
    private void loadNames() throws JamException {
        experimentName=textExpName.getText().trim();
        sortFile=textSortFile.getText().trim();
        eventInFile=textEventInStream.getText().trim();
        eventOutFile=textEventOutStream.getText().trim();
        histDirectory=textPathHist.getText().trim()+separator;
        dataDirectory=textPathData.getText().trim()+separator;
        logDirectory=textPathLog.getText().trim()+separator;
        if (!tapeMode) {
            dataDirectory=dataDirectory+separator;
        }
        try {
            sortInterval=Integer.parseInt(textSortInterval.getText().trim());
        } catch (NumberFormatException nfe){
            throw new JamException("Not a valid number for sort Interval");
        }
    }

    /**
     * Load and instantize the sort file.
     */
    private void loadSorter() throws  JamException {
        try{// create sort class
            sortClass=null;
            sortClass=(SortRoutine) Class.forName(sortFile).newInstance();
        } catch (ClassNotFoundException ce) {
            sortClass=null;
            throw new JamException("Cannot find sort class: "+sortFile+" [SetupSortOn]");
        } catch (InstantiationException ie) {
            sortClass=null;
            throw new JamException("Cannot instantize sort file: "+sortFile);
        } catch (IllegalAccessException iae) {
            sortClass=null;
            throw new JamException(" Cannot access sort file: "+sortFile);
        }
    }

    /**
     * Sets up the online sort process.  Creates the necessary daemons and link pipes
     * between the processes.
     *
     * @author Ken Swartz
     * @author Dale Visser
     */
    private void setupAcq() throws SortException, JamException {
        try {//allocate data areas
            sortClass.initialize();
        } catch (Exception e) {
            throw new JamException("Exception in SortRoutine: "+sortClass.getClass().getName()
            +".initialize(); Message= '"+e.getClass().getName()+": "+e.getMessage()+"'");
        }
        DataControl.setupAll();
        //interprocess buffering between daemons
        sortingRing=new RingBuffer();
        if (storeEventsLocally) storageRing=new RingBuffer();
        //typical setup of event streams
        try {//create new event input stream class
            eventInputStream= (EventInputStream) Class.forName(eventInFile).newInstance();
            eventInputStream.setConsole(msgHandler);
        } catch (ClassNotFoundException ce) {
            eventInputStream=null;
            throw new JamException(getClass().getName()+
            ": can't find EventInputStream class: "+eventInFile);
        } catch (InstantiationException ie) {
            eventInputStream=null;
            ie.printStackTrace();
            throw new JamException(getClass().getName()+
            ": can't instantiate EventInputStream class: "+eventInFile);
        } catch (IllegalAccessException iae) {
            eventInputStream=null;
            throw new JamException(getClass().getName()+
            ": illegal access to EventInputStream class: "+eventInFile);
        }
        //eventOutputStream = new L002OutputStream(sortClass.getEventSize());
        try {//create new event input stream class
            eventOutputStream= (EventOutputStream) Class.forName(eventOutFile).newInstance();
            eventOutputStream.setEventSize(sortClass.getEventSize());
        } catch (ClassNotFoundException ce) {
            eventOutputStream=null;
            throw new JamException(getClass().getName()+
            ": can't find EventOutputStream class: "+eventInFile);
        } catch (InstantiationException ie) {
            eventOutputStream=null;
            ie.printStackTrace();
            throw new JamException(getClass().getName()+
            ": can't instantiate EventOutputStream class: "+eventInFile);
        } catch (IllegalAccessException iae) {
            eventOutputStream=null;
            throw new JamException(getClass().getName()+
            ": illegal access to EventOutputStream class: "+eventInFile);
        }
        //create sorter daemon
        sortDaemon=new SortDaemon( runControl, msgHandler);
        sortDaemon.setup(SortDaemon.ONLINE, eventInputStream, sortClass.getEventSize());
        sortDaemon.setRingBuffer(sortingRing);
        sortDaemon.load(sortClass);
        //create storage daemon
        if (storeEventsLocally) {// don't create storage daemon otherwise
            if(tapeMode){
                tapeDaemon = new TapeDaemon(runControl,  msgHandler);
                tapeDaemon.setDevice(dataDirectory);
                storageDaemon=tapeDaemon;
            } else {
                diskDaemon =new DiskDaemon(runControl,  msgHandler);
                storageDaemon=diskDaemon;
            }
            storageDaemon.setupOn(eventInputStream,eventOutputStream);
            storageDaemon.setRingBuffer(storageRing);
        }
        //create net daemon
        netDaemon = new NetDaemon(sortingRing, storageRing, msgHandler,
        JamProperties.getPropString(JamProperties.HOST_DATA_IP),
        JamProperties.getPropInt(JamProperties.HOST_DATA_PORT_RECV));
        //set the fraction of buffers to give to the sort routine
        netDaemon.setSortInterval(sortInterval);
        //tell control about everything
        runControl.setupOn(experimentName, dataDirectory, histDirectory, sortDaemon, netDaemon, storageDaemon);
        //tell status
        displayCounters.setupOn(netDaemon, sortDaemon, storageDaemon);
        //startup daemons
        if (storeEventsLocally) storageDaemon.start();
        sortDaemon.start();
        netDaemon.start();
    }

    /**
     *
     */
    private void setupCamac() throws JamException {
        frontEnd.setup();
        frontEnd.setupCamac(sortClass.getCamacCommands());// tell vme to read files of list of cnafs
    }

    private void setupVME_Map() throws JamException, SortException {
        frontEnd.setup();
        frontEnd.setupVME_Map(sortClass.getVME_Map());
        VME_Map map=sortClass.getVME_Map();
        frontEnd.sendScalerInterval(map.getScalerInterval());
        /*eventInputStream.setScalerTable(map.getScalerTable());*/
    }

    /**
     * reset online data Aquisition
     *      kill all daemons
     *      closes data network
     *      clear all data areas
     *      Histograms, Gates, Scalers, Monitors, Parameters
     */
    private void resetAcq() throws GlobalException {
        if(diskDaemon!=null){
            diskDaemon.setState(GoodThread.STOP);
        }
        if(sortDaemon!=null){
            sortDaemon.load(null);  //make sure sorter Daemon does not have a handle to sortClass
            sortDaemon.setState(GoodThread.STOP);  //this line should be sufficient but above line is needed
        }
        if(netDaemon!=null){
            netDaemon.setState(GoodThread.STOP);
            netDaemon.closeNet();
        }
        if(tapeDaemon!=null){
            tapeDaemon.setState(GoodThread.STOP);
        }
        DataBase.clearAllLists();
    }

    /**
     * Makes the tape the device events will be saved to
     *
     * Author Dale Visser
     */
    private void setTapeMode(boolean mode){
        tapeMode=mode;
        if (mode) {
            diskPathData=textPathData.getText();
            textPathData.setText(tapePathData);
        } else {
            tapePathData=textPathData.getText();
            textPathData.setText(diskPathData);
        }
    }

    /**
     * Is the Browse for the sort class file
     * which showed be in ../jam/sort subdirectory
     * part of the <code>sort</code> Package
     *
     * @author Ken Swartz
     * @author Dale Visser
     */
    private String getSortFile(){
    String [] types = new String [] {"class"};
        JFileChooser fd =new JFileChooser(sortDirectory);
        fd.setFileFilter(new ExtensionFileFilter(types,"Java .class files"));
        int option = fd.showOpenDialog(jamMain);
        //save current values
        if (option == JFileChooser.APPROVE_OPTION && fd.getSelectedFile() != null){
            sortDirectory=fd.getSelectedFile();//save current directory
            sortFile=fd.getSelectedFile().getName();
        }
        return sortFile;
    }

    /**
     * Is the Browse for the Path Name where the
     * histogram file to be saved.
     *
     * @author Ken Swartz
     * @author Dale Visser
     */
    private String getPathHist(){
        JFileChooser fd =new JFileChooser(histDirectory);
        fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fd.showOpenDialog(jamMain);
        //save current values
        if (option == JFileChooser.APPROVE_OPTION && fd.getSelectedFile() != null){
            histDirectory=fd.getSelectedFile().getPath();//save current directory
        }
        return histDirectory;
    }

    /**
     * Is the Browse for the Path Name where the
     * histogram file to be saved.
     *
     * @author Ken Swartz
     * @author Dale Visser
     */
    private String getPathLog(){
        JFileChooser fd =new JFileChooser(logDirectory);
        fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fd.showOpenDialog(jamMain);
        //save current values
        if (option == JFileChooser.APPROVE_OPTION && fd.getSelectedFile() != null){
            logDirectory=fd.getSelectedFile().getPath();//save current directory
        }
        return logDirectory;
    }


    /**
     * Is the Browse for the Path Name where
     *  the events file will be saved.
     *
     * @author Ken Swartz
     * @author Dale Visser
     */
    private String getPathData(){
        JFileChooser fd =new JFileChooser(dataDirectory);
        fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fd.showOpenDialog(jamMain);
        //save current values
        if (option == JFileChooser.APPROVE_OPTION && fd.getSelectedFile() != null){
            dataDirectory=fd.getSelectedFile().getPath();//save current directory
        }
        return dataDirectory;
    }

    /**
     * Locks up the Online setup so the fields cannot be edited.
     *
     * @param  lock  is true if the fields are to be locked
     */
    private void lockMode(boolean lock) throws JamException{
        if (lock) {
            if (tapeMode){
                jamMain.setSortMode(JamMain.ONLINE_TAPE);
            } else {
                jamMain.setSortMode(JamMain.ONLINE_DISK);
            }
            checkLock.setSelected(true);
            checkLock.setEnabled(true);
            textExpName.setEditable(false);
            textExpName.setBackground(Color.lightGray);
            textSortFile.setEditable(false);
            textSortFile.setBackground(Color.lightGray);
            textEventInStream.setEditable(false);
            textEventInStream.setBackground(Color.lightGray);
            textEventOutStream.setEditable(false);
            textEventOutStream.setBackground(Color.lightGray);
            textPathHist.setEditable(false);
            textPathHist.setBackground(Color.lightGray);
            textPathData.setEditable(false);
            textPathData.setBackground(Color.lightGray);
            textPathLog.setEditable(false);
            textPathLog.setBackground(Color.lightGray);
            textSortInterval.setEditable(false);
            textSortInterval.setBackground(Color.lightGray);
            bok.setEnabled(false);
            bapply.setEnabled(false);
        } else {
            jamMain.setSortMode(JamMain.NO_ACQ);
            checkLock.setEnabled(false);
            textExpName.setEditable(true);
            textExpName.setBackground(Color.white);
            textSortFile.setEditable(true);
            textSortFile.setBackground(Color.white);
            textEventInStream.setEditable(true);
            textEventInStream.setBackground(Color.white);
            textEventOutStream.setEditable(true);
            textEventOutStream.setBackground(Color.white);
            textPathHist.setEditable(true);
            textPathHist.setBackground(Color.white);
            textPathData.setEditable(true);
            textPathData.setBackground(Color.white);
            textPathLog.setEditable(true);
            textPathLog.setBackground(Color.white);
            textSortInterval.setEditable(true);
            textSortInterval.setBackground(Color.white);
            bok.setEnabled(true);
            bapply.setEnabled(true);
        }
    }

    /**
     * Browses for the event stream.
     */
    private String getEventStream(){
    String [] types = new String [] {"class"};
        JFileChooser fd =new JFileChooser(eventDirectory);
        fd.setFileFilter(new ExtensionFileFilter(types,"Java .class files"));
        int option = fd.showOpenDialog(jamMain);
        //save current values
        if (option == JFileChooser.APPROVE_OPTION && fd.getSelectedFile() != null){
            eventDirectory=fd.getSelectedFile();//save current directory
            eventFile=fd.getSelectedFile().getName();
        }
        return eventFile;
    }
}