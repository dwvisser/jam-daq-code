/*
 */
package jam.global;

import java.util.Date;

/**
 * Run information, including run number, title, etc.
 * 
 * @author Ken Swartz
 */
public class RunInfo {

	private RunInfo() {
		super();
	}

	private static final RunInfo INSTANCE = new RunInfo();

	/**
	 * @return the singleton instance
	 */
	public static RunInfo getInstance() {
		return INSTANCE;
	}

	/**
	 * Experiment name.
	 */
	public String experimentName;

	/**
	 * Online (true) or offline (false)
	 */
	public boolean online;

	/**
	 * Time when run ended.
	 */
	public Date runEndTime;

	/**
	 * Time when run ended, as a string.
	 */
	public String runEndTimeSt;

	/**
	 * Number of possible parameters per event.
	 */
	public int runEventSize;

	/**
	 * Run number.
	 */
	public int runNumber;

	/**
	 * Block size for event stream.
	 */
	public int runRecordLength;

	/**
	 * Run Title.
	 */
	public Date runStartTime;

	/**
	 * Time when run started, as a string.
	 */
	public String runStartTimeSt;

	/**
	 * Run State
	 */
	public boolean runState;

	/**
	 * Run Title.
	 */
	public String runTitle;
}