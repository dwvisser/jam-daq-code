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
import java.util.*;
import javax.swing.*;

/**
 * Class to setup the offline sort process.
 *
 * @author Dale Visser
 * @author Ken Swartz
 * @version 1.1
 */
class SetupSortOff  implements ItemListener {

    class ApplyActionListener implements ActionListener{
    
    	/**
    	 * Perform setup tasks when OK or APPLY is clicked.
    	 *
    	 * @param ae the event created by clicking OK or APPLY
    	 */
    	public void actionPerformed(ActionEvent ae){
    		try{
                if (jamMain.canSetSortMode()) {
                    resetSort();//clear current data areas and kill daemons
                    loadNames();
                    loadSorter();
                    msgHandler.messageOutln("Loaded sort class '"+
                    sortRoutine.getClass().getName()+
                    "', event instream class '"
                    +eventInput.getClass().getName()+
                    "', and event outstream class '"+
                    eventOutput.getClass().getName()+"'");
                    if (sortRoutine != null) {
                        setupSort();      //create data areas and daemons
                        msgHandler.messageOutln("Offline sorting setup");
                    }
                    jamMain.dataChanged();
                    if (bok.equals(ae.getSource())){
                    	d.dispose();
                    }
                } else {
                    throw new JamException(classname+
                    "Can't set up sorting, mode locked.");
                }
        	} catch (Exception ex){
            	msgHandler.errorOutln(ex.getMessage());
            }
    	}
    }

    /**
     * Use for mode when sorting from disk.
     */
    public static final int DISK=0;
    
    /**
     * Use for mode when sorting from tape.
     */
    public static final int TAPE=1;
	private final static String OK="OK";
	private final static String Apply="Apply";
	private final static String Cancel="Cancel";
	private final static String SetupLocked="Setup Locked";
    
	private final String defaultSortPath, defaultSortRoutine, 
	defaultEventInStream, 
    defaultEventOutStream, defaultEventPath, defaultSpectra, defaultTape;

    /* handles we need */
    final private JamMain jamMain;
    final private SortControl sortControl;
    final private DisplayCounters displayCounters;
    final private Broadcaster broadcaster;
    final private MessageHandler msgHandler;
    private SortDaemon sortDaemon;
    
    private final String classname;

    /**
     * User sort routine must extend this abstract class
     */
    private SortRoutine sortRoutine;//the actual sort routine
    private File sortClassPath;//path to base of sort routines' classpath
    private Class sortClass;

    /** Input stream, how tells how to read an event */
    private EventInputStream eventInput;

    /** Output stream, tells how to write an event */
    private EventOutputStream eventOutput;

    private File sortDirectory;

    /**
     * Indicates event source: from DISK or TAPE.
     */
    private int mode;
    
    /**
     * The path to the tape device.
     */
    private String tapeDevice;

    /* dialog box widgets */
    private final  JDialog d;
    private final JTextField textSortPath, textDev;
    private final JCheckBox checkLock;
    private final JToggleButton ctape, cdisk,defaultPath,specify;
    private final JButton bok, bapply, bbrowsef;
    private final JComboBox sortChoice, inStreamChooser, outStreamChooser;
	
    SetupSortOff(JamMain jm,  SortControl sc,
    DisplayCounters dc, Broadcaster b, MessageHandler mh ) {
		classname=getClass().getName()+"--";
        defaultSortRoutine = JamProperties.getPropString(
        JamProperties.SORT_ROUTINE);
        defaultSortPath = JamProperties.getPropString(
        JamProperties.SORT_CLASSPATH);
        defaultEventInStream=JamProperties.getPropString(
        JamProperties.EVENT_INSTREAM);
        defaultEventOutStream=JamProperties.getPropString(
        JamProperties.EVENT_OUTSTREAM);
        defaultEventPath =JamProperties.getPropString(
        JamProperties.EVENT_INPATH);
        defaultSpectra=JamProperties.getPropString(JamProperties.HIST_PATH);
        defaultTape   =JamProperties.getPropString(JamProperties.TAPE_DEV);
        final boolean useDefaultPath=(defaultSortPath.equals(
        JamProperties.DEFAULT_SORT_CLASSPATH));
        if (!useDefaultPath){
			sortDirectory=new File(defaultSortPath);  
			sortClassPath=sortDirectory;      	
        }
        this.jamMain=jm;
        this.sortControl=sc;
        this.displayCounters=dc;
        this.broadcaster=b;
        this.msgHandler=mh;
        d = new JDialog (jamMain,"Setup Offline",false);  //dialog box
        final Container cp=d.getContentPane();
        d.setResizable(false);
        final int posx=20;
        final int posy=50;
        d.setLocation(posx,posy);
        cp.setLayout(new BorderLayout());
		final int space=5;
		final LayoutManager verticalGrid=new GridLayout(0,1,space,space);
		final JPanel pNorth=new JPanel(verticalGrid);
		cp.add(pNorth,BorderLayout.NORTH);
		final JPanel pradio=new JPanel(new FlowLayout(FlowLayout.CENTER,space,space));
		final ButtonGroup pathType=new ButtonGroup();
		defaultPath=new JRadioButton("Use help.* and sort.* in default classpath",
		useDefaultPath);
		specify=new JRadioButton("Specify a classpath",!useDefaultPath);
		defaultPath.setToolTipText("Don't include your sort routines in the default"+
		" classpath if you want to be able to edit, recompile and reload them"+
		" without first quitting Jam.");
		specify.setToolTipText("Specify a path to load your sort routine from.");
		pathType.add(defaultPath);
		pathType.add(specify);
		defaultPath.addItemListener(this);
		specify.addItemListener(this);
		pradio.add(defaultPath);
		pradio.add(specify);
		pNorth.add(pradio);
		final JPanel pCenter=new JPanel(new BorderLayout());
		cp.add(pCenter,BorderLayout.CENTER);
		final JPanel pf = new JPanel(new BorderLayout());
		pCenter.add(pf,BorderLayout.NORTH);
        final JLabel lf=new JLabel("Sort classpath", JLabel.RIGHT);
        pf.add(lf,BorderLayout.WEST);
        textSortPath =new JTextField(defaultSortPath);
        textSortPath.setToolTipText("Use Browse button to change. \n"+
        "May fail if classes have unresolvable references."+
        "\n* use the sort.classpath property in your JamUser.ini "+
        "file to set this automatically.");
		textSortPath.setColumns(35);
		textSortPath.setEditable(false);
        pf.add(textSortPath,BorderLayout.CENTER);
		bbrowsef = new JButton("Browse");
		pf.add(bbrowsef,BorderLayout.EAST);
		bbrowsef.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				sortClassPath=getSortPath();
				sortChoice.setModel(new DefaultComboBoxModel(
				getSortClasses(sortClassPath)));
				sortChoice.setSelectedIndex(0);
				textSortPath.setText(sortClassPath.getAbsolutePath());
			}
		});
       
		final JPanel pChooserArea =new JPanel(new BorderLayout());
		final JPanel pChooserLabels=new JPanel(verticalGrid);
		pChooserArea.add(pChooserLabels,BorderLayout.WEST);
		final JPanel pChoosers=new JPanel(verticalGrid);
		pChooserArea.add(pChoosers,BorderLayout.CENTER);
		pCenter.add(pChooserArea);

        pChooserLabels.add(new JLabel("Sort Routine",JLabel.RIGHT),
        BorderLayout.WEST);
        final Vector v=getSortClasses(sortDirectory);
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
        	final Class cl=(Class)it.next();
        	final String name=cl.getName();
        	final boolean match = name.equals(defaultSortRoutine);
        	if (match){
				sortChoice.setSelectedItem(cl);
        	} 
        	notDone = (!match) && it.hasNext();
        }
        pChoosers.add(sortChoice);
        pChooserLabels.add(new JLabel("Event input stream",JLabel.RIGHT));
        Set lhs=new LinkedHashSet(RTSI.find("jam.sort.stream",
        EventInputStream.class,false));
        lhs.remove(EventInputStream.class);
        inStreamChooser=new JComboBox(new Vector(lhs));
        inStreamChooser.setToolTipText("Select your input event data format.");
		it=lhs.iterator();
		notDone=it.hasNext();
		while (notDone) {
			final Class cl=(Class)it.next();
			final String name=cl.getName();
			final boolean match = name.equals(defaultEventInStream);
			if (match){
				inStreamChooser.setSelectedItem(cl);
			} 
			notDone = (!match) && it.hasNext();
		}
        pChoosers.add(inStreamChooser);

        pChooserLabels.add(new JLabel("Event output stream",Label.RIGHT));

		lhs=new LinkedHashSet(RTSI.find("jam.sort.stream",
		EventOutputStream.class,false));
		lhs.remove(EventOutputStream.class);
		outStreamChooser=new JComboBox(new Vector(lhs));
		outStreamChooser.setToolTipText("Select your output event format.");
		it=lhs.iterator();
		notDone=it.hasNext();
		while (notDone) {
			final Class cl=(Class)it.next();
			final String name=cl.getName();
			final boolean match = name.equals(defaultEventOutStream);
			if (match){
				outStreamChooser.setSelectedItem(cl);
			} 
			notDone = (!match) && it.hasNext();
		}
		pChoosers.add(outStreamChooser);
        final JPanel pselect=new JPanel();
        pselect.setLayout(new FlowLayout(FlowLayout.CENTER,space,space));
        pChooserArea.add(pselect,BorderLayout.SOUTH);
        pselect.add(new JLabel("Tape Device:"));
        textDev=new JTextField(defaultTape);
        textDev.setColumns(12);
        pselect.add(textDev);
        final ButtonGroup eventMode = new ButtonGroup();
        ctape=new JRadioButton("Events from Tape", false);
        ctape.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent ie){
        		if (ctape.isSelected()){
        			setMode(TAPE);
        		}
        	}
        });
        ctape.setEnabled(false);
        ctape.setToolTipText("Not implemented.");
        eventMode.add(ctape);
        //ctape.addItemListener(this);
        cdisk=new JRadioButton("Events from Disk", true);
        cdisk.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent ie){
        		if (cdisk.isSelected()){
        			setMode(DISK);
        		}
        	}
        });
        cdisk.setToolTipText("The only option for now.");
        eventMode.add(cdisk);
        pselect.add(ctape);
        pselect.add(cdisk);
        final JPanel pb=new JPanel();
        pb.setLayout(new GridLayout(1,0,space,space));
        cp.add(pb,BorderLayout.SOUTH);
        bok  =   new JButton(OK);
        pb.add(bok);
        ApplyActionListener aal=new ApplyActionListener();
        bok.addActionListener(aal);
        bapply = new JButton(Apply);
        pb.add(bapply);
        bapply.addActionListener(aal);
        final JButton bcancel =new JButton(new AbstractAction(Cancel){
        	public void actionPerformed(ActionEvent ae){
        		d.dispose();
        	}
        });
        pb.add(bcancel);
        checkLock =new JCheckBox(SetupLocked, false );
        checkLock.setEnabled(false);
        checkLock.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent ie){
        		if (!checkLock.isSelected()){
        			try {
						resetSort();
        			} catch (Exception e){
        				msgHandler.errorOutln(classname+e.getMessage());
        			}
        		}
        	}
        });
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
     * Choice to unlock setup or
     * choice between tape and disk.
     * 
     * @param ie the event indicating an item has changed 
     */
    public void itemStateChanged(ItemEvent ie){
    	final ItemSelectable selectedItem=ie.getItemSelectable();
        if (selectedItem.equals(defaultPath) && 
        defaultPath.isSelected()){
        	bbrowsef.setEnabled(false);
        	setChooserDefault(true);
        } else if (selectedItem.equals(specify) && specify.isSelected()){
        	bbrowsef.setEnabled(true);
        	setChooserDefault(false);
        }
    }
    
    private void setChooserDefault(boolean isDefault){
    	if (isDefault){
    		final String package1="help";
    		final String package2="sort";
    		final Set set=new LinkedHashSet();
    		set.addAll(RTSI.find(package1,SortRoutine.class,true));
    		set.addAll(RTSI.find(package2,SortRoutine.class,true));
    		final Vector v=new Vector();
    		v.addAll(set);
    		sortChoice.setModel(new DefaultComboBoxModel(v));
    	} else {
			sortChoice.setModel(new DefaultComboBoxModel(getSortClasses(sortClassPath)));
    	}
    }

    /**
     * Loads the names of objects entered in the dialog box into 
     * String objects.
     */
    private void loadNames() {
        synchronized (this){
        	tapeDevice=textDev.getText();
        }
    }

    /**
     * Resolves the String objects into class names and loads the 
     * sorting class and event streams.
     *
     * @throws JamException if there's a problem
     */
    private void loadSorter() throws JamException {
        try {
        	if (specify.isSelected()){
        		/* we call loadClass() in order to guarantee latest version */
				synchronized (this){
					sortRoutine= (SortRoutine)RTSI.loadClass(sortClassPath,
					sortClass.getName()).newInstance();// create sort class
				}
        	} else {//use default loader
        		synchronized (this){
        			sortRoutine=(SortRoutine)sortClass.newInstance();
        		}
        	}
        } catch (InstantiationException ie) {
            throw new JamException(classname+
            "Cannot instantiate sort routine: "+sortClass.getName());
        } catch (IllegalAccessException iae) {
            throw new JamException(classname+"Cannot access sort routine: "+
            sortClass.getName());
        }
        loadEventInput();
        loadEventOutput();
    }
    
    private void loadEventInput() throws JamException {
        try {//create new event input stream class
            synchronized(this){
            	eventInput= (EventInputStream) ((Class)
            	inStreamChooser.getSelectedItem()).newInstance();
            }
            eventInput.setConsole(msgHandler);
        } catch (InstantiationException ie) {
            ie.printStackTrace();
            throw new JamException(classname+
            "Cannot instantize event input stream: "+
            inStreamChooser.getSelectedItem());
        } catch (IllegalAccessException iae) {
            throw new JamException(classname+
            "Cannot access event input stream: "+
            inStreamChooser.getSelectedItem());
        }
    }
    
    private void loadEventOutput() throws JamException {
        try {//create new event output stream class
        	synchronized (this){
            	eventOutput = (EventOutputStream) ((Class)
            	outStreamChooser.getSelectedItem()).newInstance();
            }
        } catch (InstantiationException ie) {
            throw new JamException(classname+
            "Cannot instantize event output stream: "+
            eventOutput.getClass().getName());
        } catch (IllegalAccessException iae) {
            throw new JamException(classname+
            "Cannot access event output stream: "+
            eventOutput.getClass().getName());
        }
    
    }

    /**
     * Sets up the offline sort.
     * 
     * @throws SortException if there's a problem
     * @throws JamException if there's a problem
     */
    private void setupSort() throws SortException, JamException {
        String deviceName;
        try {
            sortRoutine.initialize();
        } catch (Exception e) {
            throw new JamException(classname+"Exception in SortRoutine: "+
            sortRoutine.getClass().getName()+".initialize(); Message= '"+
            e.getClass().getName()+": "+e.getMessage()+"'");
        }
        /* setup scaler, parameter, monitors, gate, dialog boxes */
        DataControl.setupAll();
        /* setup sorting */
        synchronized(this){
        	sortDaemon=new SortDaemon( sortControl,  msgHandler);
        }
        sortDaemon.setup(SortDaemon.OFFLINE, eventInput, 
        sortRoutine.getEventSize());
        sortDaemon.load(sortRoutine);
        /* eventInputStream to use get event size from sorting routine */
        eventInput.setEventSize(sortRoutine.getEventSize());
        eventInput.setBufferSize(sortRoutine.BUFFER_SIZE);
        /* give sortroutine output stream */
        eventOutput.setEventSize(sortRoutine.getEventSize());
        eventOutput.setBufferSize(sortRoutine.BUFFER_SIZE);
		sortRoutine.setEventOutputStream(eventOutput);
        /* always setup diskDaemon */
        final DiskDaemon diskDaemon =new DiskDaemon(sortControl,  msgHandler);
        diskDaemon.setupOff(eventInput, eventOutput);
        StorageDaemon storageDaemon=diskDaemon;
        /* setup source of data tape */
        if(mode==TAPE){
            final TapeDaemon tapeDaemon = new TapeDaemon(sortControl, 
            msgHandler);
            tapeDaemon.setDevice(tapeDevice);
            tapeDaemon.setupOff(eventInput, eventOutput);
            deviceName=tapeDevice;
            storageDaemon=tapeDaemon;
        } else {
            deviceName="Disk";
            //storageDaemon=diskDaemon;
        }
        /* tell run control about all, disk always to device */
        sortControl.setup(this, sortDaemon, storageDaemon, 
        diskDaemon, deviceName);
        /* tell status to setup */
        displayCounters.setupOff(sortDaemon, storageDaemon);
        /* tell sortDaemon to update status */
        sortDaemon.setObserver(broadcaster);
        /* start sortDaemon which is then suspended by Sort control until files 
         * entered */
        sortDaemon.start();
        /* lock setup */
        lockMode(true);
    }

    /**
     * Resets offline data aquisition.
     * Kills sort daemon. Clears all data areas: histograms, gates, 
     * scalers and monitors.
     *
     * @throws JamException if there's a problem
     * @throws GlobalException if there's a thread problem
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
     * 
     * @param m either DISK or TAPE
     * @see #DISK
     * @see #TAPE
     */
    private void setMode(int m) {
        if (m==TAPE) {
        	synchronized (this) {
            	this.mode=TAPE;
            }
            sortControl.setDevice(SortControl.TAPE);
            textDev.setEditable(true);
        }
        if (m==DISK) {
            synchronized (this) {
            	this.mode=DISK;
            }
            sortControl.setDevice(SortControl.DISK);
            textDev.setEditable(false);
        }
    }

    /**
     * Browses for the sort file.
     * 
     * @return the directory to look in for event files
     */
    private File getSortPath(){
        final JFileChooser fd =new JFileChooser(sortDirectory);
        fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        final int option = fd.showOpenDialog(jamMain);
        /* save current values */
        if (option == JFileChooser.APPROVE_OPTION && 
        fd.getSelectedFile() != null){
            synchronized (this){
            	sortDirectory=fd.getSelectedFile();//save current directory
            }
        }
        return sortDirectory;
    }
	
    /**
     * Lock the setup if it is unlocked than the sort is stopped
     * Set the title bar to indicate offline sort and wether from tape
     * or disk
     *
     * @throws JamException if there's a problem
     * @param lock true if the locking the dialog, false if unlocking
     */
    private void lockMode(boolean lock) throws JamException {
    	final boolean notLock=!lock;
    	checkLock.setEnabled(lock);
    	checkLock.setSelected(lock);
    	inStreamChooser.setEnabled(notLock);
    	outStreamChooser.setEnabled(notLock);
    	cdisk.setEnabled(notLock);
    	bok.setEnabled(notLock);
    	bapply.setEnabled(notLock);
    	specify.setEnabled(notLock);
    	defaultPath.setEnabled(notLock);
    	sortChoice.setEnabled(notLock);
        if(lock){
            if (mode==DISK) {
                jamMain.setSortMode(JamMain.OFFLINE_DISK);
            } else {
                jamMain.setSortMode(JamMain.OFFLINE_TAPE);
            }
            bbrowsef.setEnabled(false);
        } else{
            jamMain.setSortMode(JamMain.NO_ACQ);
            bbrowsef.setEnabled(specify.isSelected());
        }
    }
}