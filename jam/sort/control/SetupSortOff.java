package jam.sort.control;

import jam.JamException;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GoodThread;
import jam.global.JamProperties;
import jam.global.PropertyKeys;
import jam.global.SortMode;
import jam.sort.DiskDaemon;
import jam.sort.SortDaemon;
import jam.sort.SortException;
import jam.sort.AbstractSortRoutine;
import jam.sort.stream.AbstractEventInputStream;
import jam.sort.stream.AbstractEventOutputStream;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.logging.Level;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

;

/**
 * Class to setup the offline sort process.
 * 
 * @author Dale Visser
 * @author Ken Swartz
 * @version 1.1
 */
public final class SetupSortOff extends AbstractSetup {

	private static SetupSortOff instance = null;

	private final static String SETUP_LOCKED = "Setup Locked";

	/**
	 * Returns the only instance of this class.
	 * 
	 * @return the only instance of this class
	 */
	public static SetupSortOff getInstance() {
		if (instance == null) {
			instance = new SetupSortOff();
		}
		return instance;
	}

	/* dialog box widgets */
	private transient final JCheckBox checkLock = new JCheckBox(SETUP_LOCKED,
			false);

	/* handles we need */
	final transient private SortControl sortControl;

	private transient SortDaemon sortDaemon;


	private SetupSortOff() {
		super("Setup Offline");
		
		//Build GUI
		sortControl = SortControl.getInstance();
		final java.awt.Container contents = dialog.getContentPane();
		dialog.setResizable(false);
		final int posx = 20;
		final int posy = 50;
		dialog.setLocation(posx, posy);
		contents.setLayout(new BorderLayout(5, 5));
		final int space = 5;
		final JPanel pNorth = new JPanel(new GridLayout(0, 1, space, space));
		contents.add(pNorth, BorderLayout.NORTH);
		final JPanel pradio = new JPanel(new FlowLayout(FlowLayout.CENTER,
				space, space));
		final ButtonGroup pathType = new ButtonGroup();
		pathType.add(btnDefaultPath);
		pathType.add(btnSpecifyPath);
		pradio.add(btnDefaultPath);
		pradio.add(btnSpecifyPath);
		pNorth.add(pradio);
		/* Labels */
		final JPanel pLabels = new JPanel(new GridLayout(0, 1, space, space));
		pLabels.setBorder(new EmptyBorder(2, 10, 0, 0)); // down so browse
		// button lines up
		contents.add(pLabels, BorderLayout.WEST);
		pLabels.add(new JLabel("Sort classpath", SwingConstants.RIGHT));
		pLabels.add(new JLabel("Sort Routine", SwingConstants.RIGHT));
		final JLabel lis = new JLabel("Event input stream",
				SwingConstants.RIGHT);
		pLabels.add(lis);
		final JLabel los = new JLabel("Event output stream",
				SwingConstants.RIGHT);
		pLabels.add(los);
		/* Entry fields */
		final JPanel pEntry = new JPanel(new GridLayout(0, 1, space, space));
		pEntry.setBorder(new EmptyBorder(2, 0, 0, 0));// down so browse button
		// lines up
		contents.add(pEntry, BorderLayout.CENTER);
		/* Path */
		pEntry.add(textSortPath);
		pEntry.add(sortChooser);
		pEntry.add(inChooser);
		pEntry.add(outChooser);
		final JPanel pBrowse = new JPanel(new GridLayout(4, 1, 0, 0));
		pBrowse.setBorder(new EmptyBorder(0, 0, 0, 10));
		contents.add(pBrowse, BorderLayout.EAST);
		pBrowse.add(bbrowsef);
		/* Button Panel */
		final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final JPanel panelB = new JPanel();
		panelB.setLayout(new GridLayout(1, 0, space, space));
		pbutton.add(panelB);
		contents.add(pbutton, BorderLayout.SOUTH);
		panelB.add(bok);
		panelB.add(bapply);
		final JButton bcancel = new JButton(new jam.ui.WindowCancelAction(
				dialog));
		panelB.add(bcancel);
		checkLock.setEnabled(false);
		checkLock.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(final ItemEvent event) {
				if (!checkLock.isSelected()) {
					try {
						resetSort();
					} catch (Exception e) {
						LOGGER.log(Level.SEVERE, e.getMessage(), e);
					}
				}
			}
		});
		panelB.add(checkLock);
		dialog
				.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
	}

	protected void doApply(final boolean dispose) {
		try {
			if (STATUS.canSetup()) {
				resetSort();// clear current data areas and kill daemons
				sortChooser.loadSorter(btnSpecifyPath.isSelected());
				loadEventInput();
				loadEventOutput();
				final AbstractSortRoutine sortRoutine = sortChooser.getSortRoutine();
				LOGGER.info("Loaded sort class '"
						+ sortRoutine.getClass().getName()
						+ "', event instream class '"
						+ inStream.getClass().getName()
						+ "', and event outstream class '"
						+ outStream.getClass().getName() + "'");
				if (sortRoutine != null) {
					setupSort(); // create data areas and daemons
					LOGGER.info("Daemons and dialogs initialized.");
				}
				STATUS.selectFirstSortHistogram();
				if (dispose) {
					dialog.dispose();
				}
			} else {
				final JamException exception = new JamException(
						"Can't set up sorting, mode locked.");
				LOGGER.throwing("SetupSortOff", "doApply", exception);
				throw exception;
			}
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	private void loadEventInput() throws JamException {
		try {// create new event input stream class
			synchronized (this) {
				inStream = (AbstractEventInputStream) ((Class) inChooser
						.getSelectedItem()).newInstance();
			}
			inStream.setConsoleExists(true);
		} catch (InstantiationException ie) {
			final String msg = "Cannot instantize event input stream: "
					+ inChooser.getSelectedItem();
			LOGGER.log(Level.SEVERE, msg, ie);
			throw new JamException(msg, ie);
		} catch (IllegalAccessException iae) {
			final String msg = "Cannot access event input stream: "
					+ inChooser.getSelectedItem();
			LOGGER.log(Level.SEVERE, msg, iae);
			throw new JamException(msg, iae);
		}
	}

	private void loadEventOutput() throws JamException {
		try {// create new event output stream class
			synchronized (this) {
				outStream = (AbstractEventOutputStream) ((Class) outChooser
						.getSelectedItem()).newInstance();
			}
		} catch (InstantiationException ie) {
			throw new JamException("Cannot instantize event output stream: "
					+ outStream.getClass().getName());
		} catch (IllegalAccessException iae) {
			throw new JamException("Cannot access event output stream: "
					+ outStream.getClass().getName());
		}
	}

	protected void lockMode(final boolean lock) {
		final boolean notLock = !lock;
		checkLock.setEnabled(lock);
		checkLock.setSelected(lock);
		textSortPath.setEnabled(notLock);
		inChooser.setEnabled(notLock);
		outChooser.setEnabled(notLock);
		bok.setEnabled(notLock);
		bapply.setEnabled(notLock);
		btnSpecifyPath.setEnabled(notLock);
		btnDefaultPath.setEnabled(notLock);
		sortChooser.setEnabled(notLock);
		if (lock) {
			STATUS.setSortMode(SortMode.OFFLINE, sortChooser.getSortRoutine()
					.getClass().getName());
			bbrowsef.setEnabled(false);
		} else {
			STATUS.setSortMode(SortMode.NO_SORT, "No Sort");
			bbrowsef.setEnabled(btnSpecifyPath.isSelected());
		}
	}

	/**
	 * Resets offline data aquisition. Kills sort daemon. Clears all data areas:
	 * histograms, gates, scalers and monitors.
	 */
	private void resetSort() {
		if (sortDaemon != null) {
			sortDaemon.setState(GoodThread.State.STOP);
			sortDaemon.setSorter(null);
		}
		sortChooser.forgetSortRoutine();
		jam.data.DataBase.getInstance().clearAllLists();
		Broadcaster.getSingletonInstance().broadcast(BroadcastEvent.Command.HISTOGRAM_NEW);
		lockMode(false);
	}

	protected void setupSort() throws SortException, JamException {
		initializeSorter();
		/* setup sorting */
		synchronized (this) {
			sortDaemon = new SortDaemon(sortControl);
		}
		final AbstractSortRoutine sortRoutine = sortChooser.getSortRoutine();
		sortDaemon.setup(inStream, sortRoutine.getEventSize());
		sortDaemon.setSorter(sortRoutine);
		/* eventInputStream to use get event size from sorting routine */
		inStream.setEventSize(sortRoutine.getEventSize());
		inStream.setBufferSize(sortRoutine.getBufferSize());
		/* give sortroutine output stream */
		outStream.setEventSize(sortRoutine.getEventSize());
		outStream.setBufferSize(sortRoutine.getBufferSize());
		sortRoutine.setEventOutputStream(outStream);
		/* always setup diskDaemon */
		final DiskDaemon diskDaemon = new DiskDaemon(sortControl);
		diskDaemon.setupOff(inStream, outStream);
		/* tell run control about all, disk always to device */
		sortControl.setup(sortDaemon, diskDaemon, diskDaemon);
		/* tell status to setup */
		DisplayCounters.getSingletonInstance().setupOff(sortDaemon, diskDaemon);
		/*
		 * start sortDaemon which is then suspended by Sort control until files
		 * entered
		 */
		sortDaemon.start();
		/* lock setup */
		lockMode(true);
	}

	/**
	 * Provided so setup offline sort can be scriptable.
	 * 
	 * @param classPath
	 *            path to sort routine classpath base
	 * @param sortName
	 *            name of sort routine class
	 * @param inStream
	 *            event input stream class
	 * @param outStream
	 *            event output stream class
	 */
	public void setupSort(final java.io.File classPath, final String sortName,
			final Class inStream, final Class outStream) {
		sortChooser.loadChooserClassPath(classPath);
		sortChooser.selectSortClass(sortName);
		inChooser.setSelectedItem(inStream);
		outChooser.setSelectedItem(outStream);
		doApply(false);
	}
}
