package jam.sort;

import jam.global.Sorter;
import jam.sort.stream.EventInputStream;
import jam.sort.stream.EventOutputStream;

import java.io.File;

/**
 * NOT IN USE...
 * Controls the sorting, interface to sorting from 
 * other packages.
 * 
 * @author Ken Swartz
 */
public abstract class AbstractSortControl {

	/* these private fields aren't in use yet. */
	private Sorter sorter;
	private EventOutputStream eventOutputStream;
	private EventInputStream eventInputStream;
	private String experimentName;
	private File pathHistogram;
	private File pathEvent;
	private File pathLog;	
	private int sortSample;

	//private NetDaemon netDaemon;
	//private DiskDaemon diskDaemon;
	
	/**
	 * Sets the sort class
	 * @param sorter
	 */
	public void setSorter(Sorter sorter){
		this.sorter=sorter;
	}
	
	/**
	 * Get the sorter.
	 * @return the sorter
	 */
	public Sorter getSorter(){
	    return sorter;
	}
	
	/**
	 * Sets the event input stream
	 * @param eventInputStream
	 */
	public void setInputStream(EventInputStream eventInputStream){
		this.eventInputStream=eventInputStream;
	}
	
	/**
	 * Get the event input stream.
	 * 
	 * @return the event input stream
	 */
	public EventInputStream getInputStream(){
	    return eventInputStream;
	}
	
	/**
	 * Sets the event output stream
	 * @param eventOutputStream
	 */
	public void setOutputStream(EventOutputStream eventOutputStream){
		this.eventOutputStream=eventOutputStream;
	}
	
	/**
	 * Get the event output stream.
	 * 
	 * @return the event output stream
	 */
	public EventOutputStream getOutputStream(){
	    return eventOutputStream;
	}

	/**
	 * Sets the event output stream
	 * @param experimentName
	 */
	public void setExperimentName(String experimentName){
		this.experimentName=experimentName;
	}
	
	/**
	 * Get the experiment name.
	 * @return the experiment name
	 */
	public String getExperimentName(){
	    return experimentName;
	}
	
	/**
	 * Sets the path to save the histogram files to
	 * @param pathHistogram
	 */
	public void setPathHistogram(File pathHistogram){
		this.pathHistogram=pathHistogram;
	}
	
	/**
	 * Get the path to HDF files.
	 * @return the path to HDF files
	 */
	public File getPathHistogram(){
	    return pathHistogram;
	}
	
	/**
	 * Sets the path to save the event files to
	 * @param pathEvent
	 */	
	public void setPathEvent(File pathEvent){
		this.pathEvent=pathEvent;
	}
	
	/**
	 * Get the path to event files.
	 * @return the path to event files
	 */
	public File getPathEvent(){
	    return pathEvent;
	}
	
	/**
	 * Sets the path to save the log files to
	 * @param pathLog
	 */
	public void setPathLog(File pathLog){
		this.pathLog=pathLog;
	}
	
	/**
	 * Get the path to log files.
	 * 
	 * @return the path to log files
	 */
	public File getPathLog(){
	    return pathLog;
	}
	
	/**
	 * Sets the sort sample
	 * @param sortSample
	 */
	public void setSortSample(int sortSample){
		this.sortSample=sortSample;
	}
	
	/**
	 * Get the sort sample number.
	 * 
	 * @return the sort sample number
	 */
	public int getSortSample(){
	    return sortSample;
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
