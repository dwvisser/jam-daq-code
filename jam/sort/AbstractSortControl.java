package jam.sort;

import jam.global.Sorter;
import jam.sort.stream.AbstractEventInputStream;
import jam.sort.stream.AbstractEventOutputStream;

import java.io.File;

/**
 * NOT IN USE... Controls the sorting, interface to sorting from other packages.
 * 
 * @author Ken Swartz
 */
public abstract class AbstractSortControl {

	/* these private fields aren't in use yet. */
	private Sorter sorter;

	private transient AbstractEventOutputStream eventOutputStream;

	private transient AbstractEventInputStream eventInputStream;

	private String experimentName;

	private File pathHistogram;

	private File pathEvent;

	private File pathLog;

	private int sortSample;

	protected AbstractSortControl() {
		super();
	}

	/**
	 * Sets the sort class
	 * 
	 * @param sorter
	 */
	public void setSorter(final Sorter sorter) {
		this.sorter = sorter;
	}

	/**
	 * Get the sorter.
	 * 
	 * @return the sorter
	 */
	public Sorter getSorter() {
		return sorter;
	}

	/**
	 * Sets the event input stream
	 * 
	 * @param eventInputStream
	 */
	public void setInputStream(final AbstractEventInputStream eventInputStream) {
		this.eventInputStream = eventInputStream;
	}

	/**
	 * Get the event input stream.
	 * 
	 * @return the event input stream
	 */
	public AbstractEventInputStream getInputStream() {
		return eventInputStream;
	}

	/**
	 * Sets the event output stream
	 * 
	 * @param eventOutputStream
	 */
	public void setOutputStream(
			final AbstractEventOutputStream eventOutputStream) {
		this.eventOutputStream = eventOutputStream;
	}

	/**
	 * Get the event output stream.
	 * 
	 * @return the event output stream
	 */
	public AbstractEventOutputStream getOutputStream() {
		return eventOutputStream;
	}

	/**
	 * Sets the event output stream
	 * 
	 * @param experimentName
	 */
	public void setExperimentName(final String experimentName) {
		this.experimentName = experimentName;
	}

	/**
	 * Get the experiment name.
	 * 
	 * @return the experiment name
	 */
	public String getExperimentName() {
		return experimentName;
	}

	/**
	 * Sets the path to save the histogram files to
	 * 
	 * @param pathHistogram
	 */
	public void setPathHistogram(final File pathHistogram) {
		this.pathHistogram = pathHistogram;
	}

	/**
	 * Get the path to HDF files.
	 * 
	 * @return the path to HDF files
	 */
	public File getPathHistogram() {
		return pathHistogram;
	}

	/**
	 * Sets the path to save the event files to
	 * 
	 * @param pathEvent
	 */
	public void setPathEvent(final File pathEvent) {
		this.pathEvent = pathEvent;
	}

	/**
	 * Get the path to event files.
	 * 
	 * @return the path to event files
	 */
	public File getPathEvent() {
		return pathEvent;
	}

	/**
	 * Sets the path to save the log files to
	 * 
	 * @param pathLog
	 */
	public void setPathLog(final File pathLog) {
		this.pathLog = pathLog;
	}

	/**
	 * Get the path to log files.
	 * 
	 * @return the path to log files
	 */
	public File getPathLog() {
		return pathLog;
	}

	/**
	 * Sets the sort sample
	 * 
	 * @param sortSample
	 */
	public void setSortSample(final int sortSample) {
		this.sortSample = sortSample;
	}

	/**
	 * Get the sort sample number.
	 * 
	 * @return the sort sample number
	 */
	public int getSortSample() {
		return sortSample;
	}
}
