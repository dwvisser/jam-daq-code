/*
 */
package jam;
import jam.data.DataBase;
import jam.data.control.DataControl;
import jam.global.Broadcaster;
import jam.global.GlobalException;
import jam.global.GoodThread;
import jam.global.JamProperties;
import jam.global.MessageHandler;
import jam.io.ExtensionFileFilter;
import jam.sort.DiskDaemon;
import jam.sort.SortDaemon;
import jam.sort.SortException;
import jam.sort.SortRoutine;
import jam.sort.StorageDaemon;
import jam.sort.TapeDaemon;
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
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Class to setup the offline sort process.
 *
 * @author Dale Visser
 * @author Ken Swartz
 */
class SetupSortOff  implements ActionListener, ItemListener {
    private String defaultSort, defaultEventInStream, defaultEventOutStream,
    defaultEventPath, defaultSpectra, defaultTape;

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
    SortRoutine sortClass;

    /** Input stream, how tells how to read an event */
    EventInputStream eventInput;

    /** Output stream, tells how to write an event */
    EventOutputStream eventOutput;

    String sortFile, eventFile, eventInFile, eventOutFile;
    File sortDirectory, eventDirectory;
    private Vector runList;

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
    private JTextField textSortFile, textEventFile, textEventOutFile, textDev;
    private JCheckBox ctape, cdisk, checkLock;
    private JButton bok, bapply;

    public SetupSortOff(JamMain jamMain,  SortControl sortControl,
    DisplayCounters displayCounters, Broadcaster broadcaster,
    MessageHandler msgHandler ) {
        defaultSort   =JamProperties.getPropString(JamProperties.SORT_CLASS);
        defaultEventInStream=JamProperties.getPropString(JamProperties.EVENT_INSTREAM);
        defaultEventOutStream=JamProperties.getPropString(JamProperties.EVENT_OUTSTREAM);
        defaultEventPath =JamProperties.getPropString(JamProperties.EVENT_INPATH);
        defaultSpectra=JamProperties.getPropString(JamProperties.HIST_PATH);
        defaultTape   =JamProperties.getPropString(JamProperties.TAPE_DEV);
        this.jamMain=jamMain;
        this.sortControl=sortControl;
        this.displayCounters=displayCounters;
        this.broadcaster=broadcaster;
        this.msgHandler=msgHandler;

        d = new JDialog (jamMain,"Setup Offline",false);  //dialog box
        Container cp=d.getContentPane();
        d.setForeground(Color.black);
        d.setBackground(Color.lightGray);

        d.setResizable(false);
        d.setLocation(20,50);
        //d.setSize(550, 250);
        cp.setLayout(new GridLayout(0, 1, 5,5));

        JPanel pf = new JPanel();
        pf.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
        cp.add(pf);

        JLabel lf=new JLabel("", JLabel.LEFT);
        lf.setText("Sort file (.class)");
        pf.add(lf);
        textSortFile =new JTextField(defaultSort);
        textSortFile.setColumns(35);
        textSortFile.setBackground(Color.white);
        textSortFile.setForeground(Color.black);
        pf.add(textSortFile);

        JButton bbrowsef = new JButton("Browse");
        pf.add(bbrowsef);
        bbrowsef.setActionCommand("bsort");
        bbrowsef.addActionListener(this);

        // panel histogram path
        JPanel ph= new JPanel();
        ph.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
        cp.add(ph);

        JLabel lh=new JLabel("",JLabel.LEFT);
        lh.setText("Event input stream (.class)");
        ph.add(lh);

        textEventFile =new JTextField(defaultEventInStream);
        textEventFile.setColumns(35);
        textEventFile.setBackground(Color.white);
        textEventFile.setForeground(Color.black);
        ph.add(textEventFile);

        JButton bbrowseh = new JButton("Browse");
        ph.add(bbrowseh);
        bbrowseh.setActionCommand("binstream");
        bbrowseh.addActionListener(this);

        JPanel pout = new JPanel();
        pout.setLayout(new FlowLayout(FlowLayout.RIGHT,5,5));
        cp.add(pout);

        JLabel lout=new JLabel("",Label.LEFT);
        lout.setText("Event output stream (.class)");
        pout.add(lout);

        textEventOutFile =new JTextField(defaultEventOutStream);
        textEventOutFile.setColumns(35);
        textEventOutFile.setBackground(Color.white);
        textEventOutFile.setForeground(Color.black);
        pout.add(textEventOutFile);

        JButton bbrowseout = new JButton("Browse");
        pout.add(bbrowseout);
        bbrowseout.setActionCommand("boutstream");
        bbrowseout.addActionListener(this);

        JPanel pselect=new JPanel();
        pselect.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
        cp.add(pselect);
        JLabel ltd=new JLabel("Tape Device:");
        pselect.add(ltd);
        textDev=new JTextField(defaultTape);
        textDev.setColumns(12);
        textDev.setBackground(Color.white);
        textDev.setForeground(Color.black);
        pselect.add(textDev);

        ButtonGroup eventMode = new ButtonGroup();
        ctape=new JCheckBox("Events from Tape", false);
        eventMode.add(ctape);
        ctape.addItemListener(this);

        cdisk=new JCheckBox("Events from Disk", true);
        eventMode.add(cdisk);
        cdisk.addItemListener(this);

        pselect.add(ctape);
        pselect.add(cdisk);

        JPanel pb=new JPanel();
        pb.setLayout(new FlowLayout(FlowLayout.CENTER,5,5));
        cp.add(pb);

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
        //System.err.println(getClass().getName()+".actionPerformed(\""+command+"\") starting");
        try {
            if (command=="bsort") {
                sortFile=getSortFile();
                if (sortFile.endsWith(".class")) {
                	sortFile = sortFile.substring(0,sortFile.lastIndexOf(".class"));
                }
                textSortFile.setText("sort."+sortFile);
            } else if (command=="binstream") {
                //eventDirectory=getEventStream();
                textEventFile.setText(getEventStream());//note side effect: eventDirectory gets set
            } else if (command=="boutstream") {
                //eventDirectory=getEventStream();
                textEventOutFile.setText(getEventStream());//note side effect: eventDirectory gets set
            } else if (command=="ok"||command=="apply"){
                if (jamMain.canSetSortMode()) {
                    resetSort();//clear current data areas and kill daemons
                    loadNames();
                    loadSorter();
                    msgHandler.messageOutln("Loaded sort class '"+sortFile+
                    "', event instream class '"
                    +eventInFile+"', and event outstream class '"+eventOutFile+"'");
                    if (sortClass != null) {
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
                if(checkLock.isSelected()==false) {
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
        //System.err.println(getClass().getName()+".itemStateChanged(), arg = "+ie);
        if (ie.getItemSelectable()==ctape) {//set mode, disk or tape
            if(ctape.isSelected()){
                setMode(TAPE);
            }
        } else if (ie.getItemSelectable()==cdisk) {
            if(cdisk.isSelected()){
                setMode(DISK);
            }
        }
    }

    /**
     * Loads the names of objects entered in the dialog box into String objects.
     */
    private void loadNames() throws JamException {
        sortFile=textSortFile.getText().trim();
        eventInFile=textEventFile.getText().trim();
        eventOutFile=textEventOutFile.getText().trim();
        tapeDevice=textDev.getText();
    }

    /**
     * Resolves the String objects into class names and loads the sorting class
     * and event streams.
     */
    private void loadSorter() throws JamException {
        try {
            sortClass= (SortRoutine) Class.forName(sortFile).newInstance();// create sort class
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
        try {//create new event input stream class
            eventInput= (EventInputStream) Class.forName(eventInFile).newInstance();
            eventInput.setConsole(msgHandler);
        } catch (ClassNotFoundException ce) {
            eventInput=null;
            throw new JamException("Cannot find event input stream class: "+eventInFile+
            " [SetupSortOn]");
        } catch (InstantiationException ie) {
            eventInput=null;
            ie.printStackTrace();
            throw new JamException("Cannot instantize event input stream file: "+eventInFile);
        } catch (IllegalAccessException iae) {
            eventInput=null;
            throw new JamException(" Cannot access event input stream file: "+eventInFile);
        }
        try {//create new event output stream class
            eventOutput = (EventOutputStream) Class.forName(eventOutFile).newInstance();
        } catch (ClassNotFoundException ce) {
            eventInput=null;
            throw new JamException("Cannot find event output stream class: "+eventOutFile+
            " [SetupSortOn]");
        } catch (InstantiationException ie) {
            eventInput=null;
            throw new JamException("Cannot instantize event output stream file: "+eventOutFile);
        } catch (IllegalAccessException iae) {
            eventInput=null;
            throw new JamException(" Cannot access event output stream file: "+eventOutFile);
        }
    }

    /**
     * Sets up the offline sort.
     */
    private void setupSort() throws SortException, JamException, GlobalException {
        String deviceName;

        try {
            sortClass.initialize();
        } catch (Exception e) {
            throw new JamException("Exception in SortRoutine: "+sortClass.getClass().getName()
            +".initialize(); Message= '"+e.getClass().getName()+": "+e.getMessage()+"'");
        }
        //setup scaler, parameter, monitors, gate, dialog boxes
        DataControl.setupAll();
        //setup sorting
        sortDaemon=new SortDaemon( sortControl,  msgHandler);
        sortDaemon.setup(SortDaemon.OFFLINE, eventInput, sortClass.getEventSize());
        sortDaemon.load(sortClass);
        //eventInputStream to use get event size from sorting routine
        eventInput.setEventSize(sortClass.getEventSize());
        eventInput.setBufferSize(sortClass.BUFFER_SIZE);
        //give sortroutine output stream
        eventOutput.setEventSize(sortClass.getEventSize());
        eventOutput.setBufferSize(sortClass.BUFFER_SIZE);
        sortClass.setEventOutputStream(eventOutput);
        //always setup diskDaemon
        diskDaemon =new DiskDaemon(sortControl,  msgHandler);
        //FIXMEdiskDaemon.setDevice(null);
        diskDaemon.setupOff(eventInput, eventOutput);
        //setup source of data tape
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
        //tell run control about all, disk always to device
        sortControl.setup(this, sortDaemon, storageDaemon, diskDaemon, deviceName);
        //tell status to setup
        displayCounters.setupOff(sortDaemon, storageDaemon);
        //tell sortDaemon to update status
        sortDaemon.setObserver(broadcaster);
        //start sortDaemon which is then suspended by Sort control until files entered
        sortDaemon.start();
        //lock setup
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
            textDev.setBackground(Color.white);
        } else if (mode==DISK) {
            this.mode=DISK;
            sortControl.setDevice(SortControl.DISK);
            textDev.setEditable(false);
            textDev.setBackground(Color.lightGray);
        }
    }

    /**
     * Browses for the sort file.
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
            checkLock.setSelected(true);
            checkLock.setEnabled(true);
            textSortFile.setEditable(false);
            textSortFile.setBackground(Color.lightGray);
            textEventFile.setEditable(false);
            textEventFile.setBackground(Color.lightGray);
            textEventOutFile.setEditable(false);
            textEventOutFile.setBackground(Color.lightGray);
            textDev.setEditable(false);
            textDev.setBackground(Color.lightGray);
            cdisk.setEnabled(false);
            ctape.setEnabled(false);
            bok.setEnabled(false);
            bapply.setEnabled(false);
        } else{
            jamMain.setSortMode(JamMain.NO_ACQ);
            checkLock.setSelected(false);
            checkLock.setEnabled(false);
            textSortFile.setEditable(true);
            textSortFile.setBackground(Color.white);
            textEventFile.setEditable(true);
            textEventFile.setBackground(Color.white);
            textEventOutFile.setEditable(true);
            textEventOutFile.setBackground(Color.white);
            textDev.setEditable(true);
            textDev.setBackground(Color.white);
            cdisk.setEnabled(true);
            ctape.setEnabled(true);
            bok.setEnabled(true);
            bapply.setEnabled(true);
        }
    }
}