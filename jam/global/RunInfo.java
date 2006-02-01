/*
 */
package jam.global;

import java.util.Date;

/**
 * Run information, including run number, title, etc.
 * 
 * @author Ken Swartz
 */
public class RunInfo {// NOPMD
	/**
	 * Experiment name.
	 */
	public static String experimentName;

	/**
	 * Online (true) or offline (false)
	 */
	public static boolean online;

	/**
	 * Time when run ended.
	 */
	public static Date runEndTime;

	/**
	 * Time when run ended, as a string.
	 */
	public static String runEndTimeSt;

	/**
	 * Number of possible parameters per event.
	 */
	public static int runEventSize;

	/**
	 * Run number.
	 */
	public static int runNumber;

	/**
	 * Block size for event stream.
	 */
	public static int runRecordLength;

	/**
	 * Run Title.
	 */
	public static Date runStartTime;

	/**
	 * Time when run started, as a string.
	 */
	public static String runStartTimeSt;

	/**
	 * Run State
	 */
	public static boolean runState;

	/**
	 * Run Title.
	 */
	public static String runTitle;
}