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
import javax.swing.border.*;

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
    		doApply(bok.equals(ae.getSource()));
    	}
    }

    private void doApply(boolean dispose){
		try{
			if (jamMain.canSetSortMode()) {
				resetSort();//clear current data areas and kill daemons
				loadSorter();
				msgHandler.messageOutln("Loaded sort class '"+
				sortRoutine.getClass().getName()+
				"', event instream class '"
				+eventInput.getClass().getName()+
				"', and event outstream class '"+
				eventOutput.getClass().getName()+"'");
				if (sortRoutine != null) {
					setupSort();      //create data areas and daemons
					msgHandler.messageOutln("Daemons and dialogs initialized.");
				}
				broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
				if (dispose) {
					d.dispose();
				}
			} else {
				throw new JamException(classname+
				"Can't set up sorting, mode locked.");
			}
		} catch (Exception ex){
			msgHandler.errorOutln(ex.getMessage());
			ex.printStackTrace();
		}
    }

	private final static String OK="OK";
	private final static String Apply="Apply";
	private final static String Cancel="Cancel";
	private final static String SetupLocked="Setup Locked";

	private final String defaultSortPath, defaultSortRoutine,
	defaultEventInStream,
    defaultEventOutStream, defaultEventPath, defaultSpectra;

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

    //private File sortDirectory;

    /* dialog box widgets */
    private final  JDialog d;
    private final JTextField textSortPath;
    private final JCheckBox checkLock;
    private final JToggleButton defaultPath,specify;
    private final JButton bok, bapply, bbrowsef;
    private final JComboBox sortChoice, inStreamChooser, outStreamChooser;

    SetupSortOff(JamMain jm,  SortControl sc,
    DisplayCounters dc, Broadcaster b, MessageHandler mh) {


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

        final boolean useDefaultPath=(defaultSortPath.equals(
        JamProperties.DEFAULT_SORT_CLASSPATH));
        if (!useDefaultPath){
			sortClassPath=new File(defaultSortPath);
			//sortClassPath=sortDirectory;
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
        cp.setLayout(new BorderLayout(5,5));

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

		//final JPanel pCenter=new JPanel(new BorderLayout(5,0));
		//pCenter.setBorder(new EmptyBorder(0,10,10,10));
		//cp.add(pCenter,BorderLayout.CENTER);

		//Labels
		final JPanel pLabels = new JPanel(new GridLayout(0,1, 5,5));
		pLabels.setBorder(new EmptyBorder(2,10,0,0));	//down so browse button lines up
		cp.add(pLabels, BorderLayout.WEST);

        final JLabel lf=new JLabel("Sort classpath", JLabel.RIGHT);
        pLabels.add(lf);
        final JLabel ls =new JLabel("Sort Routine",JLabel.RIGHT);
        pLabels.add(ls);
        final JLabel lis= new JLabel("Event input stream",JLabel.RIGHT);
        pLabels.add(lis);
        final JLabel los =new JLabel("Event output stream",Label.RIGHT);
        pLabels.add(los);

		//Entry fields
		final JPanel pEntry = new JPanel(new GridLayout(0,1, 5,5));
		pEntry.setBorder(new EmptyBorder(2,0,0,0));//down so browse button lines up
		cp.add(pEntry, BorderLayout.CENTER);

		//Path
        textSortPath =new JTextField(defaultSortPath);
        textSortPath.setToolTipText("Use Browse button to change. \n"+
        "May fail if classes have unresolvable references."+
        "\n* use the sort.classpath property in your JamUser.ini "+
        "file to set this automatically.");
		textSortPath.setColumns(35);
		textSortPath.setEditable(false);
        pEntry.add(textSortPath);
        //Sort class
		sortChoice = new JComboBox();
		sortChoice.setToolTipText("Select sort routine class");
		sortChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				sortClass = (Class) sortChoice.getSelectedItem();
			}
		});
		selectSortRoutine(defaultSortRoutine,useDefaultPath);
 		pEntry.add(sortChoice);
 		//Input stream
        Set lhs=new LinkedHashSet(RTSI.find("jam.sort.stream",
        EventInputStream.class,false));
        lhs.remove(EventInputStream.class);
        inStreamChooser=new JComboBox(new Vector(lhs));
        inStreamChooser.setToolTipText("Select input event data format.");
		Iterator it=lhs.iterator();
		while (it.hasNext()) {
			final Class cl=(Class)it.next();
			final String name=cl.getName();
			final boolean match = name.equals(defaultEventInStream);
			if (match){
				inStreamChooser.setSelectedItem(cl);
				break;
			}
		}
        pEntry.add(inStreamChooser);
        //Output stream
		lhs=new LinkedHashSet(RTSI.find("jam.sort.stream",EventOutputStream.class,false));
		lhs.remove(EventOutputStream.class);
		outStreamChooser=new JComboBox(new Vector(lhs));
		outStreamChooser.setToolTipText("Select output event format.");
		it=lhs.iterator();
		while (it.hasNext()) {
			final Class cl=(Class)it.next();
			final String name=cl.getName();
			final boolean match = name.equals(defaultEventOutStream);
			if (match){
				outStreamChooser.setSelectedItem(cl);
				break;
			}
		}
		pEntry.add(outStreamChooser);

		final JPanel pBrowse = new JPanel(new GridLayout(4,1, 0,0));
		pBrowse.setBorder(new EmptyBorder(0,0,0,10));
		cp.add(pBrowse, BorderLayout.EAST);

		bbrowsef = new JButton("Browse...");
		pBrowse.add(bbrowsef);
		bbrowsef.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				//sortClassPath=getSortPath();
				//sortChoice.setModel(new DefaultComboBoxModel(
				//getSortClasses(sortClassPath)));
				//if (sortChoice.getModel().getSize()>0){
				//	sortChoice.setSelectedIndex(0);
				//}
				//textSortPath.setText(sortClassPath.getAbsolutePath());
				setSortClassPath(getSortPath());
			}
		});

		//Button Panel
        final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        final JPanel pb=new JPanel();
        pb.setLayout(new GridLayout(1,0,space,space));
        pbutton.add(pb);
        cp.add(pbutton,BorderLayout.SOUTH);
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
        d.pack();
    }

    private void setSortClassPath(File f){
    	if (f.exists()){
			sortClassPath=f;
			sortChoice.setModel(new DefaultComboBoxModel(
			new Vector(getSortClasses(sortClassPath))));
			if (sortChoice.getModel().getSize()>0){
				sortChoice.setSelectedIndex(0);
			}
			textSortPath.setText(sortClassPath.getAbsolutePath());
    	}
    }

	private Set getSortClasses(File path) {
		return RTSI.find(path, jam.sort.SortRoutine.class);
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

	private java.util.List setChooserDefault(boolean isDefault) {
		final Vector v= new Vector();
		if (isDefault) {
			final Set set = new LinkedHashSet();
			set.addAll(RTSI.find("help", SortRoutine.class, true));
			set.addAll(RTSI.find("sort", SortRoutine.class, true));
			v.addAll(set);
		} else {
			v.addAll(getSortClasses(sortClassPath));
		}
		sortChoice.setModel(new DefaultComboBoxModel(v));
		return v;
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
        sortDaemon.setSortRoutine(sortRoutine);
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
        /* tell run control about all, disk always to device */
        sortControl.setup(sortDaemon, storageDaemon, diskDaemon);
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
    private void resetSort() throws JamException {
        if (sortDaemon != null) {
            sortDaemon.setState(GoodThread.STOP);
            sortDaemon.setSortRoutine(null);
        }
        sortRoutine=null;
        DataBase.getInstance().clearAllLists();
        broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
        lockMode(false);
    }

    /**
     * Browses for the sort file.
     *
     * @return the directory to look in for event files
     */
    private File getSortPath(){
    	File rval=sortClassPath;
        final JFileChooser fd =new JFileChooser(sortClassPath);
        fd.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        final int option = fd.showOpenDialog(jamMain);
        /* save current values */
        if (option == JFileChooser.APPROVE_OPTION &&
        fd.getSelectedFile() != null){
            synchronized (this){
            	rval=fd.getSelectedFile();//save current directory
            }
        }
        return rval;
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
    	textSortPath.setEnabled(notLock);
    	inStreamChooser.setEnabled(notLock);
    	outStreamChooser.setEnabled(notLock);
    	bok.setEnabled(notLock);
    	bapply.setEnabled(notLock);
    	specify.setEnabled(notLock);
    	defaultPath.setEnabled(notLock);
    	sortChoice.setEnabled(notLock);
        if(lock){
            jamMain.setSortMode(JamMain.OFFLINE_DISK);
            bbrowsef.setEnabled(false);
        } else{
            jamMain.setSortMode(JamMain.NO_SORT);
            bbrowsef.setEnabled(specify.isSelected());
        }
    }

    void setupSort(File classPath, String sortRoutineName,
	Class inStream, Class outStream){
		/*sortClassPath=classPath;
		textSortPath.setText(sortClassPath.getAbsolutePath());
		sortChoice.setModel(new DefaultComboBoxModel(
		getSortClasses(sortClassPath)));*/
		setSortClassPath(classPath);
		selectSortRoutine(sortRoutineName, false);
		inStreamChooser.setSelectedItem(inStream);
		outStreamChooser.setSelectedItem(outStream);
		doApply(false);
	}

	private final void selectSortRoutine(String srName, boolean useDefaultPath){
		final java.util.List sortClassList=setChooserDefault(useDefaultPath);
		Iterator it = sortClassList.iterator();
		while (it.hasNext()) {
			Class c = (Class) it.next();
			String name = c.getName();
			if (name.equals(srName)) {
				sortChoice.setSelectedItem(c);
				break;
			}
		}
	}
}