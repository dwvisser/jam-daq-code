package jam.global;

import java.awt.Color;

/**
 * represents the possible run states of Jam.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version 1.4, 2003-12-31
 */
public final class RunState implements AcquisitionStatus {

	private static final String[] NAMES = { "NO_ACQ", "ACQ_ON", "ACQ_OFF",
			"RUN_ON", "REMOTE" };
	private static final String STOPPED = "   Stopped   ";
	private static final String[] LABELS = { "   Welcome   ", "   Started   ",
			STOPPED, "", "   Remote   " };
	private static final Color[] COLORS = { Color.LIGHT_GRAY, Color.ORANGE,
			Color.RED, Color.GREEN, Color.LIGHT_GRAY };
	private static final boolean[] ACQUIRE_ON = { false, true, false, true,
			false };
	private static final boolean[] ACQUIRE_MODE = { false, true, true, true,
			false };

	private transient final String name;
	private String label;
	private transient final Color color;
	private transient final boolean acquireOn, acquireMode;

	private RunState(final int index) {
		super();
		name = NAMES[index];
		label = LABELS[index];
		color = COLORS[index];
		acquireOn = ACQUIRE_ON[index];
		acquireMode = ACQUIRE_MODE[index];
	}

	/**
	 * Acquisition is not set up.
	 */
	public static final RunState NO_ACQ = new RunState(0);

	/**
	 * Actively sorting online or offline data, but not within the context of a
	 * run.
	 * 
	 * @see #runOnline(int)
	 */
	public static final RunState ACQ_ON = new RunState(1);

	/**
	 * Online or offline acquisition is just setup, or we just ended an online
	 * or offline run/sort.
	 */
	public static final RunState ACQ_OFF = new RunState(2);

	/**
	 * Getting our display data from a remote session.
	 */
	public static final RunState REMOTE = new RunState(4);

	/**
	 * Online acquisition is set up, running and storing events to disk.
	 * 
	 * @param run
	 *            run number
	 * @return a new state object
	 */
	public static RunState runOnline(final int run) {
		final RunState rval = new RunState(3);
		rval.setLabel("   Run " + run + "   ");
		return rval;
	}

	private void setLabel(final String string) {
		label = string;
	}

	/**
	 * @return text of the label to show for this run state
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return background color for the lable to show for this run state
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * 
	 * @return <code>true</code> if this mode represents a state of actively
	 *         sorting data
	 * @see #ACQ_ON
	 * @see #runOnline(int)
	 */
	public boolean isAcqOn() {
		return acquireOn;
	}

	/**
	 * 
	 * @return <code>true</code> if this mode represents an online or offline
	 *         sorting mode, regardless of whether data is actively being sorted
	 *         at the moment
	 * @see #ACQ_ON
	 * @see #runOnline(int)
	 * @see #ACQ_OFF
	 */
	public boolean isAcquireMode() {
		return acquireMode;
	}
}
