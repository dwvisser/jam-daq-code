/*
 */
package jam.global;
import javax.swing.JFrame;
import java.text.*;
import java.util.*;
import jam.JamMain;
/**
 * A global status class so that information is globally available.
 *
 * @author Ken Swartz
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public  class JamStatus {
	
	/**
	 * Sort Mode--No sort file loaded.
	 */
	static public final int NO_SORT = 0;

	/**
	 * Sort Mode--Set to sort online data to disk.
	 */
	static public final int ONLINE_DISK = 1;

	/**
	 * Sort Mode--Set to sort online data to tape.
	 */
	static public final int ONLINE_NODISK = 2;

	/**
	 * Sort Mode--Set to sort offline data from disk.
	 */
	static public final int OFFLINE_DISK = 3;

	/**
	 * Sort Mode--Acting as a client to a remote Jam process.
	 */
	static public final int REMOTE = 5;

	/**
	 * Sort Mode--Just read in a data file.
	 */
	static public final int FILE = 6; //we have read in a file

    private static AcquisitionStatus acquisitionStatus;
    private static String currentHistogramName="";
    private static String overlayHistogramName, currentGateName;
    private static JFrame frame;

    /** For fowarding class */
    private static JamMain jamMain;
    
    /**
     * The one instance of JamStatus.
     */
    static private JamStatus _instance;

	/**
	 * Never meant to be called by outside world.
	 */
    protected JamStatus(){
    }
    
    /**
     * Return the one instance of this class, creating
     * it if necessary.
     */
    static public JamStatus instance() {
    	return (_instance==null) ? new JamStatus() : _instance;
    }
    
    /**
     * Set the application frame.
     * 
     * @param f the frame of the current Jam application
     */
    public void setFrame(JFrame f){
		frame=f;
    }
    
    /**
     * Get the application frame.
     *
     * @return the frame of the current Jam application
     */
	public JFrame getFrame(){
		return frame;
	}
    
	/**
	 * Handle to JamMain to set sort Status
	 * 
	 * @param f the frame of the current Jam application
	 */
	public void setJamMain(JamMain jm){
		jamMain=jm;
	}    
    /**
     * Forwards call to JamMain but some of the implementation should be
     * here
     *  
     * @param mode sort mode
     */
    public void setSortMode(int mode) {
    	jamMain.setSortMode(mode); 
    }
	/**
	 * Forwards call to JamMain but some of the implementation should be
	 * here
	 *  
	 * @param fileName file name
	 */    
	public void setSortModeFile(String fileName) {
		jamMain.setSortModeFile(fileName);
	}
    /**
     * Set the acquisition status.
     * 
     * @param as the current status of the Jam application
     */
    public void setAcqisitionStatus(AcquisitionStatus as){
        acquisitionStatus=as;
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
    public synchronized  void setCurrentHistogramName(String histogramName){
        currentHistogramName=histogramName;
    }

    /**
     * Gets the current Histogram name.
     */
    public synchronized String getCurrentHistogramName(){
        return currentHistogramName;
    }
    
    /**
     * Sets the overlay Histogram name.
     */
    public synchronized  void setOverlayHistogramName(String histogramName){
        overlayHistogramName=histogramName;
    }

    /**
     * Gets the overlay Histogram name.
     */
    public synchronized String getOverlayHistogramName(){
        return overlayHistogramName;
    }

    /**
     * Sets the current Gate name.
     */
    public synchronized void setCurrentGateName(String gateName){
        currentGateName=gateName;
    }

    /**
     * Gets the current Gate name.
     */
    public synchronized String getCurrentGateName(){
        return currentGateName;
    }

    /**
     * Gets the current date and time as a String.
     */
    public String getDate(){
        Date date=new Date();        //getDate and time
        DateFormat datef=DateFormat.getDateTimeInstance();    //default format
        datef.setTimeZone(TimeZone.getDefault());  //set time zone
        String sdate=datef.format(date);      //format date
        return sdate;
    }
}
