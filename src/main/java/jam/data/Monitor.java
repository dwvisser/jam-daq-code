package jam.data;

import java.util.*;

import javax.sound.sampled.Clip;

/**
 * This class is for monitoring the status of data acquisition. Monitors can
 * show the status of things like event rates, beam current, rate of growth in a
 * histogram, etc.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 0.5,0.9
 * @since JDK 1.1
 */
public final class Monitor {

	/** The update interval */
	private static int interval;

	/**
	 * List of all monitors.
	 */
	public static final List<Monitor> LIST = Collections
			.synchronizedList(new ArrayList<Monitor>());

	/**
	 * Lookup table for all monitors.
	 */
	public static final Map<String, Monitor> TABLE = Collections
			.synchronizedMap(new HashMap<String, Monitor>());

	/**
	 * Clears the list of monitors.
	 */
	public static void clearList() {
		TABLE.clear();
		LIST.clear();
	}

	/**
	 * Gets the interval in seconds at which updates occur.
	 * 
	 * @return interval in seconds
	 */
	public static int getInterval() {
		synchronized (Monitor.class) {
			return interval;
		}
	}

	/**
	 * Returns the list of monitors.
	 * 
	 * @return the list of monitors
	 */
	public static List<Monitor> getMonitorList() {
		return Collections.unmodifiableList(LIST);
	}

	/**
	 * Set the interval in seconds at which updates occur.
	 * 
	 * @param intervalIn
	 *            interval in seconds
	 */
	public static void setInterval(final int intervalIn) {
		synchronized (Monitor.class) {
			interval = intervalIn;
		}
	}

	/**
	 * Sets the list of monitor objects.
	 * 
	 * @param inMonList
	 *            must contain all <code>Monitor</code> objects
	 */
	public static void setMonitorList(final List<Monitor> inMonList) {
		clearList();
		for (Monitor monitor : inMonList) {
			final String name = monitor.getName();
			TABLE.put(name, monitor);
			LIST.add(monitor);
		}
	}

	private transient boolean alarm;

	private Clip audioClip;

	private double maximum;

	private transient final String name; // name

	private transient final Object source;

	private double threshold;

	private double value; // value for testing

	private transient double valueNew; // the newest value set

	// the previous value set
	private transient double valueOld; // NOPMD

	/**
	 * Constructs an object which monitors the rate of counts in a particular
	 * <code>Gate</code>.
	 * 
	 * @param monitorName
	 *            name of the monitor for display in dialog
	 * @param gate
	 *            the gate whose area is monitored
	 */
	public Monitor(final String monitorName, final Gate gate) {
		super();
		name = monitorName;
		source = gate;
		if (source == null) {
			throw new IllegalArgumentException("Monitor \"" + monitorName
					+ "\": source must be non-null.");
		}
		addToCollections();
	}

	/**
	 * Constructs an object which monitors rate of increase in the given
	 * <code>Scaler</code>.
	 * 
	 * @param monitorName
	 *            name of the monitor for display in dialog
	 * @param scaler
	 *            the scaler which is monitored
	 */
	public Monitor(final String monitorName, final Scaler scaler) {
		super();
		name = monitorName;
		source = scaler;
		if (source == null) {
			throw new IllegalArgumentException("Monitor \"" + monitorName
					+ "\": source must be non-null.");
		}
		addToCollections();
	}

	/**
	 * Constructs an monitor object which delegates to a given
	 * <code>Sorter</code> for the caluclation of its current value.
	 * 
	 * @param monitorName
	 *            name of the monitor for display in dialog
	 * @param sort
	 *            the sort routine which produces the monitor values
	 */
	public Monitor(final String monitorName, final Sorter sort) {
		super();
		name = monitorName;
		source = sort;
		if (source == null) {
			throw new IllegalArgumentException("Monitor \"" + monitorName
					+ "\": source must be non-null.");
		}
		addToCollections();
	}

	private void addToCollections() {
		TABLE.put(name, this);
		LIST.add(this);
	}

	/**
	 * Returns whether alarm is activated or not.
	 * 
	 * @return <code>true</code> if an audible alarm is desired,
	 *         <code>false</code> if not
	 */
	public boolean isAlarmActivated() {
		synchronized (this) {
			return alarm;
		}
	}

	/**
	 * NOT YET IMPLEMENTED, Gets the current <code>AudioClip</code> object to be
	 * played for alarms if the alarm is enabled. Currently, the plan is to
	 * fully implement this when the JDK 1.2 <code>javax.media</code> packeage
	 * is available.
	 * 
	 * @return the sound clip for this monitor's alarm, <code>null</code>
	 *         indicates that a default system beep is desired
	 */
	public Clip getAudioClip() {
		synchronized (this) {
			return audioClip;
		}
	}

	/**
	 * Returns the maximum value for this monitor.
	 * 
	 * @return the maximum value for this monitor
	 */
	public double getMaximum() {
		synchronized (this) {
			return maximum;
		}
	}

	/**
	 * Returns this monitor's name. The name is used in display and to retrieve
	 * the monitor.
	 * 
	 * @return this montor's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the threshold value for this monitor.
	 * 
	 * @return the threshold value
	 */
	public double getThreshold() {
		synchronized (this) {
			return threshold;
		}
	}

	/**
	 * Returns this monitor's current value.
	 * 
	 * @return this monitor's current value
	 */
	public double getValue() {
		synchronized (this) {
			return value;
		}
	}

	/**
	 * Gets whether this monitor value falls within the user-specified
	 * acceptable range.
	 * 
	 * @return <code>true</code> if acceptable
	 */
	public boolean isAcceptable() {
		synchronized (this) {
			return value > maximum || value < threshold;
		}
	}

	/**
	 * Sets this monitor's value to zero.
	 */
	public void reset() {
		synchronized (this) {
			value = 0;
		}
	}

	/**
	 * Sets whether the alarm is activated. If the alarm is not activated,
	 * <code>MonitorControl</code> simply turns the indicator bar red when the
	 * value is below threshold or above the maximum. If it is activated, an
	 * alarm sound is issued too.
	 * 
	 * @param inAlarm
	 *            <code>true</code> if an audible alarm is desired,
	 *            <code>false</code> if not
	 */
	public void setAlarm(final boolean inAlarm) {
		synchronized (this) {
			alarm = inAlarm;
		}
	}

	/**
	 * NOT YET IMPLEMENTED, Sets an <code>AudioClip</code> object to be played
	 * for alarms if the alarm is enabled. Currently, the plan is to fully
	 * implement this when the JDK 1.2 <code>javax.media</code> package is
	 * available.
	 * 
	 * @param clip
	 *            sound to play for alarm
	 */
	public void setAudioClip(final Clip clip) {
		synchronized (this) {
			audioClip = clip;
		}
	}

	/**
	 * Sets the maximum value, which is the maximum value for a monitor to have
	 * without <code>MonitorControl</code> issuing a warning beep.
	 * 
	 * @param inMaximum
	 *            the new maximum
	 * @see jam.data.control.MonitorControl
	 */
	public void setMaximum(final double inMaximum) {
		synchronized (this) {
			maximum = inMaximum;
		}
	}

	/**
	 * Sets the threshold value, which is the minimum value for a monitor to
	 * have without <code>MonitorControl</code> issuing a warning beep.
	 * 
	 * @param inThreshold
	 *            the new minimum
	 * @see jam.data.control.MonitorControl
	 */
	public void setThreshold(final double inThreshold) {
		synchronized (this) {
			threshold = inThreshold;
		}
	}

	/**
	 * Sets this monitor's latest value.
	 * 
	 * @param valueIn
	 *            the new value
	 */
	public void setValue(final int valueIn) {
		synchronized (this) {
			valueNew = valueIn;
		}
	}

	/**
	 * Updates this monitor, calculating the latest monitor values. Keeps the
	 * most recent value, too, for rate determination.
	 */
	public void update() {
		synchronized (this) {
			if (source instanceof Scaler) {
				valueNew = ((Scaler) source).getValue();
				value = (valueNew - valueOld) / interval;
				valueOld = valueNew;
			} else if (source instanceof Gate) {
				valueNew = ((Gate) source).getArea();
				value = (valueNew - valueOld) / interval;
				valueOld = valueNew;
			} else if (source instanceof Sorter) {
				value = ((Sorter) source).monitor(name);
			}
		}
	}
}