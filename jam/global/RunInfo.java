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

    public static Date runEndTime;    
            
    public static String runStartTimeSt;

    public static String runEndTimeSt;
    
    public static int runEventSize;    
    
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