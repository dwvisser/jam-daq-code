package jam.global;
import jam.FrontEndCommunication;
import jam.JamPrefs;
import jam.RunState;
import jam.VMECommunication;
import jam.data.Histogram;
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

	private static AcquisitionStatus acqStatus;
	private static String histName = "";
	private static String overlayName, gateName;
	private static JFrame frame;
	private static Display display;
	private static MessageHandler console;
	private static FrontEndCommunication frontEnd;
	private boolean showGUI=true;
	private SortMode sortMode = SortMode.NO_SORT;
	private File openFile = null;
	private String sortName="";
	

	/**
	 * The one instance of JamStatus.
	 */
	static private final JamStatus INSTANCE=new JamStatus();
	
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
		return INSTANCE;
	}
	
	/**
	 * Set whether GUI components should be suppressed. Used in
	 * scripting mode to quietly run behind the scenes.
	 * 
	 * @param state <code>false</code> if suppressin
	 */
	public synchronized void setShowGUI(boolean state){
		showGUI=state;
	}
	
	public synchronized boolean isShowGUI(){
		return showGUI;
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
	 * Forwards call to JamMain but some of the implementation should be
	 * here
	 *  
	 * @param mode sort mode
	 * @throws UnsupportedOperationException if we can't change mode
	 */
	public void setSortMode(SortMode mode, String sortName) {
		this.sortName=sortName;
		synchronized (sortMode) {
			if (mode == SortMode.ONLINE_DISK
				|| mode == SortMode.ON_NO_DISK
				|| mode == SortMode.OFFLINE
				|| mode == SortMode.REMOTE) {
				if (!canSetup()) {
					final StringBuffer etext =
						new StringBuffer("Can't setup, setup is locked for ");
					if (sortMode == SortMode.ONLINE_DISK
						|| sortMode == SortMode.ON_NO_DISK) {
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
		broadcaster.broadcast(BroadcastEvent.Command.SORT_MODE_CHANGED);
	}
	
	public void setRunState(RunState rs){
		broadcaster.broadcast(BroadcastEvent.Command.RUN_STATE_CHANGED,rs);
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
	 * @param file the file just loaded or saved
	 */
	public void setSortMode(File file) {
		synchronized (sortMode) {
			openFile = file;
			setSortMode(SortMode.FILE, file.getName());
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
	public String getSortName(){
		return sortName;		
	}
	/**
	 * @return the most recent file corresponding to the currently
	 * loaded data
	 */
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
		acqStatus = as;
	}

	/**
	 * Returns whether online acquisition is set up.
	 */
	public boolean isOnLine() {
		return acqStatus.isOnLine();
	}

	/**
	 * Returns whether data is currently being taken.
	 */
	public boolean isAcqOn() {
		return acqStatus.isAcqOn();
	}

	/**
	 * Sets the current Histogram name.
	 */
	public synchronized void setHistName(String name) {
		histName = name;
	}

	/**
	 * Gets the current Histogram name.
	 */
	public synchronized String getHistName() {
		return histName;
	}
	
	public synchronized Histogram getCurrentHistogram(){
		return Histogram.getHistogram(histName);
	}

	/**
	 * Sets the overlay Histogram name.
	 */
	public synchronized void setOverlayHistogramName(String histogramName) {
		overlayName = histogramName;
	}

	/**
	 * Gets the overlay Histogram name.
	 */
	public synchronized String getOverlayHistogramName() {
		return overlayName;
	}

	/**
	 * Sets the current Gate name.
	 */
	public synchronized void setCurrentGateName(String name) {
		gateName = name;
	}

	/**
	 * Gets the current Gate name.
	 */
	public synchronized String getCurrentGateName() {
		return gateName;
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
	
	public synchronized void setMessageHandler(MessageHandler mh){
		if (console != null){
			throw new IllegalStateException("Can't set message handler twice!");
		}
		console=mh;
		frontEnd=new VMECommunication();
		JamPrefs.PREFS.addPreferenceChangeListener(frontEnd);
		broadcaster.addObserver(frontEnd);
	}
	
	public synchronized MessageHandler getMessageHandler(){
		return console;
	}
	
	public synchronized FrontEndCommunication getFrontEndCommunication(){
		return frontEnd;
		
	}
}
