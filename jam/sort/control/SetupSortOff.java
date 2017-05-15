package jam.sort.control;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jam.global.*;
import jam.sort.AbstractSortRoutine;
import jam.sort.DiskDaemon;
import jam.sort.SortDaemon;
import jam.sort.SortException;
import jam.sort.stream.AbstractEventInputStream;
import jam.sort.stream.AbstractEventOutputStream;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.logging.Level;

/**
 * Class to setup the offline sort process.
 * 
 * @author Dale Visser
 * @author Ken Swartz
 * @version 1.1
 */
@Singleton
public final class SetupSortOff extends AbstractSetup {

	private final static String SETUP_LOCKED = "Setup Locked";

	/* dialog box widgets */
	private transient final JCheckBox checkLock = new JCheckBox(SETUP_LOCKED,
			false);

	/* handles we need */
	final transient private SortControl sortControl;

	private transient SortDaemon sortDaemon;

	private transient final JamStatus status;

	private transient final DisplayCounters displayCounters;

	@Inject
	protected SetupSortOff(final SortControl sortControl,
			final JamStatus status, final DisplayCounters displayCounters,
			final Broadcaster broadcaster) {
		super("Setup Offline", broadcaster);
		this.status = status;
		this.sortControl = sortControl;
		this.displayCounters = displayCounters;
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
		checkLock.addItemListener(event -> {
            if (!checkLock.isSelected()) {
                try {
                    resetSort();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        });
		panelB.add(checkLock);
		dialog
				.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
	}

	@Override
	protected void doApply(final boolean dispose) {
		try {
			if (this.status.canSetup()) {
				resetSort();// clear current data areas and kill daemons
				sortChooser.loadSorter(btnSpecifyPath.isSelected());
				loadEventInput();
				loadEventOutput();
				final AbstractSortRoutine sortRoutine = sortChooser
						.getSortRoutine();
				if (sortRoutine != null) {
					LOGGER.info("Loaded sort class '"
							+ sortRoutine.getClass().getName()
							+ "', event instream class '"
							+ inStream.getClass().getName()
							+ "', and event outstream class '"
							+ outStream.getClass().getName() + "'");
					setupSort(); // create data areas and daemons
					LOGGER.info("Daemons and dialogs initialized.");
				}
				selectFirstSortHistogram();
				if (dispose) {
					dialog.dispose();
				}
			} else {
				final JamException exception = new JamException(
						"Can't set up sorting, mode locked.");
				LOGGER.throwing("SetupSortOff", "doApply", exception);
				throw exception;
			}
		} catch (JamException | SortException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadEventInput() throws JamException {
		try {// create new event input stream class
			synchronized (this) {
				final Class<? extends AbstractEventInputStream> class1 = (Class<? extends AbstractEventInputStream>) inChooser
						.getSelectedItem();
				inStream = class1.newInstance();
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

	@SuppressWarnings("unchecked")
	private void loadEventOutput() throws JamException {
		try {// create new event output stream class
			synchronized (this) {
				outStream = ((Class<? extends AbstractEventOutputStream>) outChooser
						.getSelectedItem()).newInstance();
			}
		} catch (InstantiationException ie) {
			throw new JamException("Cannot instantize event output stream: "
					+ outStream.getClass().getName(), ie);
		} catch (IllegalAccessException iae) {
			throw new JamException("Cannot access event output stream: "
					+ outStream.getClass().getName(), iae);
		}
	}

	@Override
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
			status.setSortMode(SortMode.OFFLINE, sortChooser.getSortRoutine()
					.getClass().getName());
			bbrowsef.setEnabled(false);
		} else {
			status.setSortMode(SortMode.NO_SORT, "No Sort");
			bbrowsef.setEnabled(btnSpecifyPath.isSelected());
		}
	}

	/**
	 * Resets offline data aquisition. Kills sort daemon. Clears all data areas:
	 * histograms, gates, scalers and monitors.
	 */
	public void resetSort() {
		if (sortDaemon != null) {
			sortDaemon.setState(GoodThread.State.STOP);
			sortDaemon.setSorter(null);
		}
		sortChooser.forgetSortRoutine();
		jam.data.DataBase.getInstance().clearAllLists();
		this.broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_NEW);
		lockMode(false);
	}

	@Override
	protected void setupSort() throws SortException, JamException {
		initializeSorter();
		/* setup sorting */
		synchronized (this) {
			sortDaemon = new SortDaemon(sortControl, this.broadcaster);
		}
		final AbstractSortRoutine sortRoutine = sortChooser.getSortRoutine();
		sortDaemon.setup(inStream, sortRoutine.getEventSize());
		sortDaemon.setSorter(sortRoutine);
		/* eventInputStream to use get event size from sorting routine */
		inStream.setEventSize(sortRoutine.getEventSize());
		inStream.setBufferSize(sortRoutine.getBufferSize());
		/* give sortroutine output stream */
		outStream.setEventSize(sortRoutine.getEventSize());
		sortRoutine.setEventOutputStream(outStream);
		/* always setup diskDaemon */
		final DiskDaemon diskDaemon = new DiskDaemon(sortControl);
		diskDaemon.setupOff(inStream, outStream);
		/* tell run control about all, disk always to device */
		sortControl.setup(sortDaemon, diskDaemon, diskDaemon);
		/* tell status to setup */
		this.displayCounters.setupOff(sortDaemon, diskDaemon);
		/*
		 * start sortDaemon which is then suspended by Sort control until files
		 * entered
		 */
		LOGGER.info("Starting sort daemon.");
		sortDaemon.start();
		/* lock setup */
		lockMode(true);
	}
}
