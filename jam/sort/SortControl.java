package jam.sort;

import jam.global.Sorter;
import jam.sort.stream.EventOutputStream;
import jam.sort.stream.EventInputStream;

/**
 * Controls the sorting, interface to sorting from 
 * other packages.
 * 
 * @author Ken Swartz
 */
public class SortControl {

	private Sorter sorter;
	private EventOutputStream eventOutputStream;
	private EventInputStream eventInputStream;
	private String experimentName;
	private String pathHistogram;
	private String pathEvent;
	private String pathLog;	
	private int sortSample;

	/* these private fields aren't in use yet. */
	//private NetDaemon netDaemon;
	//private DiskDaemon diskDaemon;
	
	public SortControl(){
	}
	
	/**
	 * Sets the sort class
	 * @param sorter
	 */
	public void setSortClass(Sorter sorter){
		this.sorter=sorter;
	}
	
	/**
	 * Sets the event input stream
	 * @param eventInputStream
	 */
	public void setInputStream(EventInputStream eventInputStream){
		this.eventInputStream=eventInputStream;
	}
	
	/**
	 * Sets the event output stream
	 * @param eventOutputStream
	 */
	public void setOutputStream(EventOutputStream eventOutputStream){
		this.eventOutputStream=eventOutputStream;
	}
	
	/**
	 * Sets the event output stream
	 * @param experimentName
	 */
	public void setExperimentName(String experimentName){
		this.experimentName=experimentName;
	}
	
	/**
	 * Sets the path to save the histogram files to
	 * @param pathHistogram
	 */
	public void setPathHistogram(String pathHistogram){
		this.pathHistogram=pathHistogram;
	}
	
	/**
	 * Sets the path to save the event files to
	 * @param pathEvent
	 */	
	public void setPathEvent(String pathEvent){
		this.pathEvent=pathEvent;
	}
	
	/**
	 * Sets the path to save the log files to
	 * @param pathLog
	 */
	public void setPathLog(String pathLog){
		this.pathLog=pathLog;
	}
	
	/**
	 * Sets the sort sample
	 * @param sortSample
	 */
	public void setSortSample(int sortSample){
		this.sortSample=sortSample;
	}
		
	/**
	 * Sets up online sorting
	 */
	public void setupOnlineSort(){
	}
	
	/**
	 * Resest, cancels online sorting
	 */	
	public void resetOnlineSort(){
	}

	/**
	 * Sets up offline sorting
	 */
	public void setupOfflineSort(){
	}
		
	/**
	 * Resest, cancels offline sorting
	 */
	public void resetOfflineSort(){
	}
	
	/**
	 * Begin run
	 */
	public void beginRun(){	
	}
	
	/**
	 * End run
	 */
	public void endRun(){	
	}
}
