/*
 */
package jam.global;
import javax.swing.JFrame;
import java.text.*;
import java.util.*;
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
     * Set the application frame
     * @param frame
     */
    public void setFrame(JFrame f){
		frame=f;
    }
    /**
     * Get the application frame
     * @param frame
     * @return
     */
	public JFrame getFrame(){
		return frame;
	}
    
    /**
     * Set the acquisition status
     * @param as
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
