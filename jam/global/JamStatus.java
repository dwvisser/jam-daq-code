package jam.global;
import jam.JamMain;
import jam.RunState;
import jam.plot.Display;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JFrame;
/**
 * A global status class so that information is globally available.
 *
 * @author Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public final class JamStatus {

	private static AcquisitionStatus acquisitionStatus;
	private static String currentHistogramName = "";
	private static String overlayHistogramName, currentGateName;
	private static JFrame frame;
	private static Display display;

	/** For fowarding class */
	private static JamMain jamMain;

	/**
	 * The one instance of JamStatus.
	 */
	static private JamStatus _instance=new JamStatus();
	
	private final Broadcaster broadcaster=Broadcaster.getSingletonInstance();

	/**
	 * Never meant to be called by outside world.
	 */
	private JamStatus() {
		super();
	}

	/**
	 * Return the one instance of this class, creating
	 * it if necessary.
	 */
	static public JamStatus instance() {
		return _instance;
	}

	/**
	 * Set the application frame.
	 * 
	 * @param f the frame of the current Jam application
	 */
	public synchronized void setFrame(JFrame f) {
		frame = f;
	}
	
	public synchronized void setDisplay(Display d){
		display=d;
	}
	
	public synchronized Display getDisplay(){
		return display;
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
	 * Handle to JamMain to set sort Status
	 * 
	 * @param jm the frame of the current Jam application
	 */
	public synchronized void setJamMain(JamMain jm) {
		jamMain = jm;
	}

	/**
	 * Forwards call to JamMain but some of the implementation should be
	 * here
	 *  
	 * @param mode sort mode
	 * @throws UnsupportedOperationException if we can't change mode
	 */
	public void setSortMode(SortMode mode) {
		synchronized (sortMode) {
			if (mode == SortMode.ONLINE_DISK
				|| mode == SortMode.ONLINE_NO_DISK
				|| mode == SortMode.OFFLINE
				|| mode == SortMode.REMOTE) {
				if (!canSetup()) {
					final StringBuffer etext =
						new StringBuffer("Can't setup, setup is locked for ");
					if (sortMode == SortMode.ONLINE_DISK
						|| sortMode == SortMode.ONLINE_NO_DISK) {
						etext.append("online");
					} else if (sortMode == SortMode.OFFLINE) {
						etext.append("offline");
					} else { //SortMode.REMOTE
						etext.append("remote");
					}
					throw new UnsupportedOperationException(etext.toString());
				}
			}
			sortMode = mode;
		}
		broadcaster.broadcast(BroadcastEvent.SORT_MODE_CHANGED);
	}
	
	public void setRunState(RunState rs){
		broadcaster.broadcast(BroadcastEvent.RUN_STATE_CHANGED,rs);
	}
	
	/**
	 * @return true is the mode can be changed
	 */
	public boolean canSetup() {
		synchronized (sortMode) {
			return (
				(sortMode == SortMode.NO_SORT) || (sortMode == SortMode.FILE));
		}
	}

	/**
	 * Sets <code>FILE</code> sort mode, and stores the given file as
	 * the last file accessed.
	 *  
	 * @param file the file just loaded
	 */
	public void setSortMode(File file) {
		synchronized (sortMode) {
			openFile = file;
			setSortMode(SortMode.FILE);
		}
	}

	private SortMode sortMode = SortMode.NO_SORT;
	private File openFile = null;

	public SortMode getSortMode() {
		synchronized (sortMode) {
			return sortMode;
		}
	}
	
	public File getOpenFile(){
		synchronized(sortMode){
			return openFile;
		}
	}

	/**
	 * Set the acquisition status.
	 * 
	 * @param as the current status of the Jam application
	 */
	public void setAcqisitionStatus(AcquisitionStatus as) {
		acquisitionStatus = as;
	}

	/**
	 * Returns whether online acquisition is set up.
	 */
	public boolean isOnLine() {
		return acquisitionStatus.isOnLine();
	}

	/**
	 * Returns whether data is currently being taken.
	 */
	public boolean isAcqOn() {
		return acquisitionStatus.isAcqOn();
	}

	/**
	 * Sets the current Histogram name.
	 */
	public synchronized void setCurrentHistogramName(String histogramName) {
		currentHistogramName = histogramName;
	}

	/**
	 * Gets the current Histogram name.
	 */
	public synchronized String getCurrentHistogramName() {
		return currentHistogramName;
	}

	/**
	 * Sets the overlay Histogram name.
	 */
	public synchronized void setOverlayHistogramName(String histogramName) {
		overlayHistogramName = histogramName;
	}

	/**
	 * Gets the overlay Histogram name.
	 */
	public synchronized String getOverlayHistogramName() {
		return overlayHistogramName;
	}

	/**
	 * Sets the current Gate name.
	 */
	public synchronized void setCurrentGateName(String gateName) {
		currentGateName = gateName;
	}

	/**
	 * Gets the current Gate name.
	 */
	public synchronized String getCurrentGateName() {
		return currentGateName;
	}

	/**
	 * Gets the current date and time as a String.
	 */
	public String getDate() {
		Date date = new Date(); //getDate and time
		DateFormat datef = DateFormat.getDateTimeInstance(); //default format
		datef.setTimeZone(TimeZone.getDefault()); //set time zone
		String sdate = datef.format(date); //format date
		return sdate;
	}
}
