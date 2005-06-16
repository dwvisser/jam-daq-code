package jam.global;

import jam.FrontEndCommunication;
import jam.JamPrefs;
import jam.RunState;
import jam.VMECommunication;
import jam.data.Gate;
import jam.data.Group;
import jam.data.Histogram;
import jam.plot.PlotDisplay;
import jam.ui.SummaryTable;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFrame;

/**
 * A global status class so that information is globally available.
 * 
 * @author Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public final class JamStatus {

	private static AcquisitionStatus acqStatus;

	private static Group currentGroup;

	private static Histogram currentHistogram;

	private static Gate currentGate;

	private static Set overlayNames = new HashSet();

	private static JFrame frame;

	private static PlotDisplay display;

	private static SummaryTable summaryTable;

	private static MessageHandler console;

	private static FrontEndCommunication frontEnd;

	private boolean showGUI = true;

	private SortMode sortMode = SortMode.NO_SORT;

	private File openFile = null;

	private String sortName = "";

	/**
	 * The one instance of JamStatus.
	 */
	static private final JamStatus INSTANCE = new JamStatus();

	private static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	/**
	 * Never meant to be called by outside world.
	 */
	private JamStatus() {
		super();
	}

	/**
	 * Return the one instance of this class, creating it if necessary.
	 * 
	 * @return the only instance of this class
	 */
	static public JamStatus getSingletonInstance() {
		return INSTANCE;
	}

	/**
	 * Set whether GUI components should be suppressed. Used in scripting mode
	 * to quietly run behind the scenes.
	 * 
	 * @param state
	 *            <code>false</code> if suppressin
	 */
	public synchronized void setShowGUI(boolean state) {
		showGUI = state;
	}

	/**
	 * Returns whether a GUI is available.
	 * 
	 * @return <code>true</code> if the Jam window is showing
	 */
	public synchronized boolean isShowGUI() {
		return showGUI;
	}

	/**
	 * Set the application frame.
	 * 
	 * @param f
	 *            the frame of the current Jam application
	 */
	public synchronized void setFrame(JFrame f) {
		frame = f;
	}

	/**
	 * Sets the display.
	 * 
	 * @param d
	 *            the display
	 */
	public synchronized void setDisplay(PlotDisplay d) {
		display = d;
	}

	/**
	 * Gets the display.
	 * 
	 * @return the display
	 */
	public synchronized PlotDisplay getDisplay() {
		return display;
	}

	/**
	 * Sets the display.
	 * 
	 * @param d
	 *            the display
	 */
	public synchronized void setTable(SummaryTable t) {
		summaryTable = t;
	}

	/**
	 * Gets the display.
	 * 
	 * @return the display
	 */
	public synchronized SummaryTable getTable() {
		return summaryTable;
	}

	/**
	 * Get the application frame.
	 * 
	 * @return the frame of the current Jam application
	 */
	public synchronized JFrame getFrame() {
		return frame;
	}

	/**
	 * Forwards call to JamMain but some of the implementation should be here
	 * 
	 * @param mode
	 *            sort mode
	 * @param sortName
	 *            the name of the current sort (?)
	 * @throws UnsupportedOperationException
	 *             if we can't change mode
	 */
	public void setSortMode(SortMode mode, String sortName) {
		this.sortName = sortName;
		synchronized (sortMode) {
			if (mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK
					|| mode == SortMode.OFFLINE || mode == SortMode.REMOTE) {
				if (!canSetup()) {
					final StringBuffer etext = new StringBuffer(
							"Can't setup, setup is locked for ");
					if (sortMode == SortMode.ONLINE_DISK
							|| sortMode == SortMode.ON_NO_DISK) {
						etext.append("online");
					} else if (sortMode == SortMode.OFFLINE) {
						etext.append("offline");
					} else { // SortMode.REMOTE
						etext.append("remote");
					}
					throw new UnsupportedOperationException(etext.toString());
				}
			}
			sortMode = mode;
		}
		BROADCASTER.broadcast(BroadcastEvent.Command.SORT_MODE_CHANGED);
	}

	/**
	 * Sets <code>FILE</code> sort mode, and stores the given file as the last
	 * file accessed.
	 * 
	 * @param file
	 *            the file just loaded or saved
	 */
	public void setOpenFile(File file) {
		synchronized (sortMode) {
			openFile = file;
			final String name = file == null ? "" : file.getPath();
			setSortMode(SortMode.FILE, name);
		}
	}

	/**
	 * @return the current sort mode
	 */
	public SortMode getSortMode() {
		synchronized (sortMode) {
			return sortMode;
		}
	}

	/**
	 * Set the current run state.
	 * 
	 * @param rs
	 *            new run state
	 */
	public void setRunState(RunState rs) {
		BROADCASTER.broadcast(BroadcastEvent.Command.RUN_STATE_CHANGED, rs);
	}

	/**
	 * @return true is the mode can be changed
	 */
	public boolean canSetup() {
		synchronized (sortMode) {
			return ((sortMode == SortMode.NO_SORT) || (sortMode == SortMode.FILE));
		}
	}

	/**
	 * Returns name of the sort or file.
	 * 
	 * @return name of the sort or file
	 */
	public String getSortName() {
		return sortName;
	}

	/**
	 * @return the most recent file corresponding to the currently loaded data
	 */
	public File getOpenFile() {
		synchronized (sortMode) {
			return openFile;
		}
	}

	/**
	 * Set the acquisition status.
	 * 
	 * @param as
	 *            the current status of the Jam application
	 */
	public void setAcqisitionStatus(AcquisitionStatus as) {
		acqStatus = as;
	}

	/**
	 * Returns whether online acquisition is set up.
	 * 
	 * @return whether online acquisition is set up
	 */
	public boolean isOnline() {
		synchronized (sortMode) {
			return sortMode.isOnline();
		}
	}

	/**
	 * Returns whether data is currently being taken.
	 * 
	 * @return whether data is currently being taken
	 */
	public boolean isAcqOn() {
		return acqStatus.isAcqOn();
	}

	/**
	 * Sets the current <code>Group</code>.
	 * 
	 * @param group
	 *            the current group
	 */
	public synchronized void setCurrentGroup(Group group) {
		currentGroup = group;
	}

	/**
	 * Gets the current histogram.
	 * 
	 * @return the current histogram
	 */
	public synchronized Group getCurrentGroup() {
		if (!Group.isValid(currentGroup)) {
			currentGroup = null;
		}
		return currentGroup;
	}

	/**
	 * Sets the current <code>Histogram</code>.
	 * 
	 * @param hist
	 *            the current histogram
	 */
	public synchronized void setCurrentHistogram(Histogram hist) {
		currentHistogram = hist;
		if (hist != null)
			currentGroup = hist.getGroup();
	}

	/**
	 * Gets the current histogram.
	 * 
	 * @return the current histogram
	 */
	public synchronized Histogram getCurrentHistogram() {
		if (!Histogram.isValid(currentHistogram)) {
			currentHistogram = null;
		}
		return currentHistogram;
	}

	/**
	 * Adds an overlay <code>Histogram</code> name.
	 * 
	 * @param name
	 *            name of a histogram to add to overlay
	 */
	public synchronized void addOverlayHistogramName(String name) {
		overlayNames.add(name);
	}

	/**
	 * Gets the overlay histograms.
	 * 
	 * @return the histograms being overlaid
	 */
	public synchronized Histogram[] getOverlayHistograms() {
		final Set rval = new HashSet();
		final Iterator iter = overlayNames.iterator();
		while (iter.hasNext()) {
			final String name = (String) iter.next();
			final Histogram hist = Histogram.getHistogram(name);
			if (hist != null) {
				rval.add(hist);
			}
		}
		final Histogram[] array = new Histogram[rval.size()];
		rval.toArray(array);
		return array;
	}

	/**
	 * Clear all overlay histogram names from list.
	 * 
	 */
	public synchronized void clearOverlays() {
		overlayNames.clear();
	}

	/**
	 * Sets the current <code>Gate</code> name.
	 * 
	 * @param name
	 *            of current gate
	 */
	public synchronized void setCurrentGate(Gate gate) {
		currentGate = gate;
	}

	/**
	 * Gets the current <code>Gate</code> name.
	 * 
	 * @return name of current gate
	 */
	public synchronized Gate getCurrentGate() {
		if (!Gate.isValid(currentGate)) {
			currentGate = null;
		}
		return currentGate;
	}

	/**
	 * Sets the global message handler.
	 * 
	 * @param mh
	 *            the message handler
	 * @throws IllegalStateException
	 *             if called a second time
	 */
	public synchronized void setMessageHandler(MessageHandler mh) {
		if (console != null) {
			throw new IllegalStateException("Can't set message handler twice!");
		}
		console = mh;
		frontEnd = new VMECommunication();
		JamPrefs.PREFS.addPreferenceChangeListener(frontEnd);
		BROADCASTER.addObserver(frontEnd);
	}

	/**
	 * Gets the global message handler.
	 * 
	 * @return the message handler
	 */
	public synchronized MessageHandler getMessageHandler() {
		return console;
	}

	/**
	 * Gets the front end communicator
	 * 
	 * @return the front end communicator
	 */
	public synchronized FrontEndCommunication getFrontEndCommunication() {
		return frontEnd;
	}
}
