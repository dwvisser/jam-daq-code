package jam.sort;

/**
 * Encapsulates the different ways the event size can be specified.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser </a>
 */
public class EventSizeMode {// NOPMD
	private static final int SET_BY_CNAF = 0;

	private static final int SET_EXPLICIT = 2;

	private static final int INIT_NO_MODE = 3;

	/**
	 * Indicates the parameter count has been set implicitly by using CNAF
	 * commands.
	 */
	public static final EventSizeMode CNAF = new EventSizeMode(SET_BY_CNAF);

	/**
	 * Indicates that the parameter count has been set explicitly.
	 */
	public static final EventSizeMode EXPLICIT = new EventSizeMode(SET_EXPLICIT);

	/**
	 * No method of defining the event size exists yet.
	 */
	public static final EventSizeMode INIT = new EventSizeMode(INIT_NO_MODE);

	/**
	 * Indicates that the parameter count hasn't been set by any means.
	 */

	private static final boolean[] IS_SET = { true, true, true, false };

	private static final int SET_VME_MAP = 1;

	/**
	 * Indicates the parameter count has been set implicitly by specifying a VME
	 * map.
	 */
	public static final EventSizeMode VME_MAP = new EventSizeMode(SET_VME_MAP);

	private final transient int mode;

	private EventSizeMode(final int value) {
		super();
		mode = value;
	}

	/**
	 * Returns whether this event size mode represents a properly set event
	 * size.
	 * 
	 * @return whether this event size mode represents a properly set event size
	 */
	public boolean isSet() {
		return IS_SET[mode];
	}
}