package jam;
import jam.data.DataBase;
import jam.data.control.DataControl;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GoodThread;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.RTSI;
import jam.global.SortMode;
import jam.sort.DiskDaemon;
import jam.sort.SortDaemon;
import jam.sort.SortException;
import jam.sort.SortRoutine;
import jam.sort.stream.EventInputStream;
import jam.sort.stream.EventOutputStream;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.ItemSelectable;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

/**
 * Class to setup the offline sort process.
 *
 * @author Dale Visser
 * @author Ken Swartz
 * @version 1.1
 */
public final class SetupSortOff extends JDialog implements ItemListener {
	
	private static final JamStatus STATUS=JamStatus.instance();

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
			if (STATUS.canSetup()) {
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
				BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
				if (dispose) {
					dispose();
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

	private final static String OK_TEXT="OK";
	private final static String APPLY="Apply";
	private final static String CANCEL="Cancel";
	private final static String SETUP_LOCKED="Setup Locked";

	private transient final String defSortPath, defSortRout,
	defInStream,
    defOutStream;

    /* handles we need */
    final transient private Frame frame;
    final transient private SortControl sortControl;
    final transient private DisplayCounters dispCount;
    final static private Broadcaster BROADCASTER=Broadcaster.getSingletonInstance();
    final transient private MessageHandler msgHandler;
    private transient SortDaemon sortDaemon;

    private transient final String classname;

    /**
     * User sort routine must extend this abstract class
     */
    private transient SortRoutine sortRoutine;//the actual sort routine
    private transient File classPath;//path to base of sort routines' classpath
    private transient Class sortClass;

    /** Input stream, how tells how to read an event */
    private transient EventInputStream eventInput;

    /** Output stream, tells how to write an event */
    private transient EventOutputStream eventOutput;

    //private File sortDirectory;

    /* dialog box widgets */
    private transient final JTextField textSortPath;
    private transient final JCheckBox checkLock;
    private transient final JToggleButton defaultPath,specify;
    private transient final JButton bok, bapply, bbrowsef;
    private transient final JComboBox sortChoice, inChooser, outChooser;

	private static SetupSortOff instance=null;
	public static SetupSortOff getSingletonInstance(){
		if (instance==null){
			instance=new SetupSortOff();
		}
		return instance;
	}

    private SetupSortOff() {
		super(STATUS.getFrame(),"Setup Offline",false);  //dialog box
		classname=getClass().getName()+"--";
        defSortRout = JamProperties.getPropString(
        JamProperties.SORT_ROUTINE);
        defSortPath = JamProperties.getPropString(
        JamProperties.SORT_CLASSPATH);
        defInStream=JamProperties.getPropString(
        JamProperties.EVENT_INSTREAM);
        defOutStream=JamProperties.getPropString(
        JamProperties.EVENT_OUTSTREAM);
        final boolean useDefault=(defSortPath.equals(
        JamProperties.DEFAULT_SORT_CLASSPATH));
        if (!useDefault){
			classPath=new File(defSortPath);
        }
        frame=STATUS.getFrame();
        sortControl=SortControl.getSingletonInstance();
        dispCount=DisplayCounters.getSingletonInstance();
        msgHandler=STATUS.getMessageHandler();
        final Container contents=getContentPane();
        setResizable(false);
        final int posx=20;
        final int posy=50;
        setLocation(posx,posy);
        contents.setLayout(new BorderLayout(5,5));
		final int space=5;
		final LayoutManager verticalGrid=new GridLayout(0,1,space,space);
		final JPanel pNorth=new JPanel(verticalGrid);
		contents.add(pNorth,BorderLayout.NORTH);
		final JPanel pradio=new JPanel(new FlowLayout(FlowLayout.CENTER,space,space));
		final ButtonGroup pathType=new ButtonGroup();
		defaultPath=new JRadioButton("Use help.* and sort.* in default classpath",
		useDefault);
		specify=new JRadioButton("Specify a classpath",!useDefault);
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
		/* Labels */
		final JPanel pLabels = new JPanel(new GridLayout(0,1, 5,5));
		pLabels.setBorder(new EmptyBorder(2,10,0,0));	//down so browse button lines up
		contents.add(pLabels, BorderLayout.WEST);
        pLabels.add(new JLabel("Sort classpath", JLabel.RIGHT));
        pLabels.add(new JLabel("Sort Routine",JLabel.RIGHT));
        final JLabel lis= new JLabel("Event input stream",JLabel.RIGHT);
        pLabels.add(lis);
        final JLabel los =new JLabel("Event output stream",Label.RIGHT);
        pLabels.add(los);
		/* Entry fields */
		final JPanel pEntry = new JPanel(new GridLayout(0,1, 5,5));
		pEntry.setBorder(new EmptyBorder(2,0,0,0));//down so browse button lines up
		contents.add(pEntry, BorderLayout.CENTER);
		/* Path */
        textSortPath =new JTextField(defSortPath);
        textSortPath.setToolTipText("Use Browse button to change. \n"+
        "May fail if classes have unresolvable references."+
        "\n* use the sort.classpath property in your JamUser.ini "+
        "file to set this automatically.");
		textSortPath.setColumns(35);
		textSortPath.setEditable(false);
        pEntry.add(textSortPath);
        /* Sort class */
		sortChoice = new JComboBox();
		sortChoice.setToolTipText("Select sort routine class");
		sortChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				sortClass = (Class) sortChoice.getSelectedItem();
			}
		});
		selectSortRoutine(defSortRout,useDefault);
 		pEntry.add(sortChoice);
 		/* Input stream */
        Set lhs=new LinkedHashSet(RTSI.find("jam.sort.stream",
        EventInputStream.class,false));
        lhs.remove(EventInputStream.class);
        inChooser=new JComboBox(new Vector(lhs));
        inChooser.setToolTipText("Select input event data format.");
		Iterator iter=lhs.iterator();
		while (iter.hasNext()) {
			final Class clazz=(Class)iter.next();
			final String name=clazz.getName();
			final boolean match = name.equals(defInStream);
			if (match){
				inChooser.setSelectedItem(clazz);
				break;
			}
		}
        pEntry.add(inChooser);
        //Output stream
		lhs=new LinkedHashSet(RTSI.find("jam.sort.stream",EventOutputStream.class,false));
		lhs.remove(EventOutputStream.class);
		outChooser=new JComboBox(new Vector(lhs));
		outChooser.setToolTipText("Select output event format.");
		iter=lhs.iterator();
		while (iter.hasNext()) {
			final Class clazz=(Class)iter.next();
			final String name=clazz.getName();
			final boolean match = name.equals(defOutStream);
			if (match){
				outChooser.setSelectedItem(clazz);
				break;
			}
		}
		pEntry.add(outChooser);
		final JPanel pBrowse = new JPanel(new GridLayout(4,1, 0,0));
		pBrowse.setBorder(new EmptyBorder(0,0,0,10));
		contents.add(pBrowse, BorderLayout.EAST);
		bbrowsef = new JButton("Browse...");
		pBrowse.add(bbrowsef);
		bbrowsef.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				setSortClassPath(getSortPath());
			}
		});
		/* Button Panel */
        final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
        final JPanel panelB=new JPanel();
        panelB.setLayout(new GridLayout(1,0,space,space));
        pbutton.add(panelB);
        contents.add(pbutton,BorderLayout.SOUTH);
        bok  =   new JButton(OK_TEXT);
        panelB.add(bok);
        ApplyActionListener aal=new ApplyActionListener();
        bok.addActionListener(aal);
        bapply = new JButton(APPLY);
        panelB.add(bapply);
        bapply.addActionListener(aal);
        final JButton bcancel =new JButton(new AbstractAction(CANCEL){
        	public void actionPerformed(ActionEvent event){
        		dispose();
        	}
        });
        panelB.add(bcancel);
        checkLock =new JCheckBox(SETUP_LOCKED, false );
        checkLock.setEnabled(false);
        checkLock.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent event){
        		if (!checkLock.isSelected()){
        			try {
						resetSort();
        			} catch (Exception e){
        				msgHandler.errorOutln(classname+e.getMessage());
        			}
        		}
        	}
        });
        panelB.add(checkLock);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
    }

    private void setSortClassPath(File file){
    	if (file.exists()){
			classPath=file;
			sortChoice.setModel(new DefaultComboBoxModel(
			new Vector(getSortClasses(classPath))));
			if (sortChoice.getModel().getSize()>0){
				sortChoice.setSelectedIndex(0);
			}
			textSortPath.setText(classPath.getAbsolutePath());
    	}
    }

	private Set getSortClasses(File path) {
		return RTSI.find(path, jam.sort.SortRoutine.class);
	}

    /**
     * Choice to unlock setup or
     * choice between tape and disk.
     *
     * @param event the event indicating an item has changed
     */
    public void itemStateChanged(ItemEvent event){
    	final ItemSelectable selectedItem=event.getItemSelectable();
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
		final Vector vector= new Vector();
		if (isDefault) {
			final Set set = new LinkedHashSet();
			set.addAll(RTSI.find("help", SortRoutine.class, true));
			set.addAll(RTSI.find("sort", SortRoutine.class, true));
			vector.addAll(set);
		} else {
			vector.addAll(getSortClasses(classPath));
		}
		sortChoice.setModel(new DefaultComboBoxModel(vector));
		return vector;
	}

    /**
     * Resolves the String objects into class names and loads the
     * sorting class and event streams.
     *
     * @throws JamException if there's a problem
     */
    private void loadSorter() throws JamException {
    	if (sortClass==null){
    		sortClass=(Class)sortChoice.getSelectedItem();
    	}
        try {
        	if (specify.isSelected()){
        		/* we call loadClass() in order to guarantee latest version */
				synchronized (this){
					sortRoutine= (SortRoutine)RTSI.loadClass(classPath,
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
            	inChooser.getSelectedItem()).newInstance();
            }
            eventInput.setConsole(msgHandler);
        } catch (InstantiationException ie) {
            ie.printStackTrace();
            throw new JamException(classname+
            "Cannot instantize event input stream: "+
            inChooser.getSelectedItem());
        } catch (IllegalAccessException iae) {
            throw new JamException(classname+
            "Cannot access event input stream: "+
            inChooser.getSelectedItem());
        }
    }

    private void loadEventOutput() throws JamException {
        try {//create new event output stream class
        	synchronized (this){
            	eventOutput = (EventOutputStream) ((Class)
            	outChooser.getSelectedItem()).newInstance();
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
        sortDaemon.setup(SortMode.OFFLINE, eventInput,
        sortRoutine.getEventSize());
        sortDaemon.setSortRoutine(sortRoutine);
        /* eventInputStream to use get event size from sorting routine */
        eventInput.setEventSize(sortRoutine.getEventSize());
        eventInput.setBufferSize(sortRoutine.getBufferSize());
        /* give sortroutine output stream */
        eventOutput.setEventSize(sortRoutine.getEventSize());
        eventOutput.setBufferSize(sortRoutine.getBufferSize());
		sortRoutine.setEventOutputStream(eventOutput);
        /* always setup diskDaemon */
        final DiskDaemon diskDaemon =new DiskDaemon(sortControl,  msgHandler);
        diskDaemon.setupOff(eventInput, eventOutput);
        /* tell run control about all, disk always to device */
        sortControl.setup(sortDaemon, diskDaemon, diskDaemon);
        /* tell status to setup */
        dispCount.setupOff(sortDaemon, diskDaemon);
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
        BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
        lockMode(false);
    }

    /**
     * Browses for the sort file.
     *
     * @return the directory to look in for event files
     */
    private File getSortPath(){
    	File rval=classPath;
        final JFileChooser chooser =new JFileChooser(classPath);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        final int option = chooser.showOpenDialog(frame);
        /* save current values */
        if (option == JFileChooser.APPROVE_OPTION &&
        chooser.getSelectedFile() != null){
            synchronized (this){
            	rval=chooser.getSelectedFile();//save current directory
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
    	inChooser.setEnabled(notLock);
    	outChooser.setEnabled(notLock);
    	bok.setEnabled(notLock);
    	bapply.setEnabled(notLock);
    	specify.setEnabled(notLock);
    	defaultPath.setEnabled(notLock);
    	sortChoice.setEnabled(notLock);
        if(lock){
            STATUS.setSortMode(SortMode.OFFLINE);
            bbrowsef.setEnabled(false);
        } else{
            STATUS.setSortMode(SortMode.NO_SORT);
            bbrowsef.setEnabled(specify.isSelected());
        }
    }

    void setupSort(File classPath, String sortName,
	Class inStream, Class outStream){
		setSortClassPath(classPath);
		selectSortRoutine(sortName, false);
		inChooser.setSelectedItem(inStream);
		outChooser.setSelectedItem(outStream);
		doApply(false);
	}

	private final void selectSortRoutine(String srName, boolean useDefault){
		final java.util.List sortList=setChooserDefault(useDefault);
		final Iterator iter = sortList.iterator();
		while (iter.hasNext()) {
			final Class clazz = (Class) iter.next();
			final String name = clazz.getName();
			if (name.equals(srName)) {
				sortChoice.setSelectedItem(clazz);
				break;
			}
		}
	}
}