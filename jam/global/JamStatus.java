/*
 */
package jam.global;
import jam.JamMain;

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
public  class JamStatus {
	
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
	 * @param jm the frame of the current Jam application
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
    public void setSortMode(SortMode mode) {
    	jamMain.setSortMode(mode); 
    }
	/**
	 * Forwards call to JamMain but some of the implementation should be
	 * here
	 *  
	 * @param fileName file name
	 */    
	public void setSortMode(File file) {
		jamMain.setSortMode(file);
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
