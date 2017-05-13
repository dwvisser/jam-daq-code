package jam.global;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A global status class so that information is globally available.
 * 
 * @author Ken Swartz
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser</a>
 */
@Singleton
public final class JamStatus implements AcquisitionStatus, QuerySortMode {

	private transient final Broadcaster broadcaster;

	private transient AcquisitionStatus acqStatus;

	private Nameable currentGroup;

	private File openFile = null;

	private transient final Set<String> overlayNames = new HashSet<String>();

	private QuerySortMode sortMode = SortMode.NO_SORT;

	private transient String sortName = "";

	@Inject
	protected JamStatus(final Broadcaster broadcaster) {
		this.broadcaster = broadcaster;
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
	public QuerySortMode getSortMode() {
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

	public boolean isOffline() {
		synchronized (sortMode) {
			return sortMode.isOffline();
		}
	}

	/**
	 * Set the acquisition status.
	 * 
	 * @param status
	 *            the current status of the Jam application
	 */
	public void setAcquisitionStatus(final AcquisitionStatus status) {
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
		broadcaster.broadcast(BroadcastEvent.Command.RUN_STATE_CHANGED,
				runState);
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
	public void setSortMode(final QuerySortMode mode, final String sortName) {
		this.sortName = sortName;
		synchronized (sortMode) {
			if ((mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK
					|| mode == SortMode.OFFLINE || mode == SortMode.REMOTE)
					&& !canSetup()) {
				final StringBuilder etext = new StringBuilder(
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
		broadcaster.broadcast(BroadcastEvent.Command.SORT_MODE_CHANGED);
	}
}
