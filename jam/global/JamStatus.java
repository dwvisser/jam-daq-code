/*
 */
package jam.global;
import java.text.*;
import java.util.*;
/**
 * A class with static methods so thatimformation is globally available.
 * It extends Observable so that class can register that the would like
 * to be imformed of a change of state.
 *
 * @author Ken Swartz
 */
public  class JamStatus  {
    private static AcquisitionStatus acquisitionStatus;
    private static String currentHistogramName="";
    private static String overlayHistogramName, currentGateName;
    private static Broadcaster broadcaster;

    public JamStatus(AcquisitionStatus acquisitionStatus){
        this.acquisitionStatus=acquisitionStatus;
        this.broadcaster= broadcaster;
    }
    
    /**
     * Are we Online
     */
    public static boolean isOnLine() {
        return acquisitionStatus.isOnLine();
    }

    /**
     * Are we currently taking data
     */
    public static boolean isAcqOn() {
        return acquisitionStatus.isAcqOn();
    }

    /**
     * set the current Histogram name
     */
    public synchronized  static void setCurrentHistogramName(String histogramName){
        //System.err.println("JamStatus.setCurrentHistName(\""+histogramName+"\")");
        currentHistogramName=histogramName;
    }

    /**
     * Get the current Histogram name
     */
    public synchronized static String getCurrentHistogramName(){
        //ystem.err.println("JamStatus.getCurrentHistogramName() = \""+currentHistogramName+"\"");
        return currentHistogramName;
    }
    
    /**
     * set the current Histogram name
     */
    public synchronized  static void setOverlayHistogramName(String histogramName){
        overlayHistogramName=histogramName;
    }

    /**
     * Get the current Histogram name
     */
    public synchronized static String getOverlayHistogramName(){
        return overlayHistogramName;
    }

    /**
     * set the current Gate name
     */
    public synchronized  static void setCurrentGateName(String gateName){
        currentGateName=gateName;
    }

    /**
     * Get the current Gate name
     */
    public synchronized static String getCurrentGateName(){
        return currentGateName;
    }

    /**
     * Get the data for printing of histogram
     */
    public static String getDate(){
        Date date=new Date();        //getDate and time
        DateFormat datef=DateFormat.getDateTimeInstance();    //default format
        datef.setTimeZone(TimeZone.getDefault());  //set time zone
        String sdate=datef.format(date);      //format date
        return sdate;
    }




}
