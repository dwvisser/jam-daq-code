/*
 */
package jam.global;
import java.util.Date;

/**
 * Run information, including run number, title, etc.
 *
 * @author Ken Swartz
 */
public  class RunInfo {
    /**
     * Experiment name.
     */
     public static String experimentName;
     
    /**
     * Run number.
     */
    public static int runNumber;
    
    /**
     * Run Title.
     */
    public static String runTitle;
    
    /**
     * Run Title.
     */    
    public static Date runStartTime;

    /**
     * Time when run ended.
     */
    public static Date runEndTime;    
    
    /**
     * Time when run started, as a string.
     */
    public static String runStartTimeSt;

    /**
     * Time when run ended, as a string.
     */
    public static String runEndTimeSt;
    
    /**
     * Number of possible parameters per event.
     */
    public static int runEventSize;    
    
    /**
     * Block size for event stream.
     */
    public static int runRecordLength;
    
    /**
     * Run State
     */
    public static boolean runState;
    
    /**
     * Online  (true) or offline (false)
     */
    public static boolean online;    
}