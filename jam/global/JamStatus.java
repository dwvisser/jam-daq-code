package jam.global;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

/**
 * A global status class so that information is globally available.
 * 
 * @author Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public final class JamStatus implements AcquisitionStatus{

	private static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	/**
	 * The one instance of JamStatus.
	 */
	static private final JamStatus INSTANCE = new JamStatus();

	/**
	 * Return the one instance of this class, creating it if necessary.
	 * 
	 * @return the only instance of this class
	 */
	static public JamStatus getSingletonInstance() {
		return INSTANCE;
	}

	private transient AcquisitionStatus acqStatus;

	private Nameable currentGroup;

	private JFrame frame;

	private File openFile = null;

	private transient final Set<String> overlayNames = new HashSet<String>();

	private boolean showGUI = true;

	private SortMode sortMode = SortMode.NO_SORT;

	private transient String sortName = "";

	/**
	 * Never meant to be called by outside world.
	 */
	private JamStatus() {
		super();
	}

	/**
	 * Adds an overlay <code>Histogram</code> name.
	 * 
	 * @param name
	 *            name of a histogram to add to overlay
	 */
	public void addOverlayHistogramName(final String name) {
		synchronized (this) {
			overlayNames.add(name);
		}
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
	 * Clear all overlay histogram names from list.
	 * 
	 */
	public void clearOverlays() {
		synchronized (this) {
			overlayNames.clear();
		}
	}

	/**
	 * Gets the current histogram.
	 * 
	 * @return the current histogram
	 */
	public Nameable getCurrentGroup() {
		synchronized (this) {
			return currentGroup;
		}
	}

	/**
	 * Get the application frame.
	 * 
	 * @return the frame of the current Jam application
	 */
	public JFrame getFrame() {
		synchronized (this) {
			return frame;
		}
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
	 * Gets the overlay histograms.
	 * 
	 * @return the histograms being overlaid
	 */
	public List<String> getOverlayHistograms() {
		synchronized (this) {
			return Collections.unmodifiableList(new ArrayList<String>(
					overlayNames));
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
	 * Returns name of the sort or file.
	 * 
	 * @return name of the sort or file
	 */
	public String getSortName() {
		return sortName;
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
	 * Returns whether a GUI is available.
	 * 
	 * @return <code>true</code> if the Jam window is showing
	 */
	public boolean isShowGUI() {
		synchronized (this) {
			return showGUI;
		}
	}

	/**
	 * Set the acquisition status.
	 * 
	 * @param status
	 *            the current status of the Jam application
	 */
	public void setAcqisitionStatus(final AcquisitionStatus status) {
		acqStatus = status;
	}

	/**
	 * Sets the current <code>Group</code>.
	 * 
	 * @param group
	 *            the current group
	 */
	public void setCurrentGroup(final Nameable group) {
		synchronized (this) {
			currentGroup = group;
		}
	}

	/**
	 * Set the application frame.
	 * 
	 * @param window
	 *            the frame of the current Jam application
	 */
	public void setFrame(final JFrame window) {
		synchronized (this) {
			frame = window;
		}
	}

	/**
	 * Sets <code>FILE</code> sort mode, and stores the given file as the last
	 * file accessed.
	 * 
	 * @param file
	 *            the file just loaded or saved
	 */
	public void setOpenFile(final File file) {
		synchronized (sortMode) {
			openFile = file;
			final String name = (file == null) ? "" : file.getPath();
			setSortMode(SortMode.FILE, name);
		}
	}

	/**
	 * Set the current run state.
	 * 
	 * @param runState
	 *            new run state
	 */
	public void setRunState(final RunState runState) {
		BROADCASTER.broadcast(BroadcastEvent.Command.RUN_STATE_CHANGED,
				runState);
	}

	/**
	 * Set whether GUI components should be suppressed. Used in scripting mode
	 * to quietly run behind the scenes.
	 * 
	 * @param state
	 *            <code>false</code> if suppressin
	 */
	public void setShowGUI(final boolean state) {
		synchronized (this) {
			showGUI = state;
		}
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
	public void setSortMode(final SortMode mode, final String sortName) {
		this.sortName = sortName;
		synchronized (sortMode) {
			if ((mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK
					|| mode == SortMode.OFFLINE || mode == SortMode.REMOTE)
					&& !canSetup()) {
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
			sortMode = mode;
		}
		BROADCASTER.broadcast(BroadcastEvent.Command.SORT_MODE_CHANGED);
	}
}
