package jam.plot.common;

/**
 * Plot package public constants that don't neatly fit into a single class.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 * @version 2004-12-13
 */
public final class ScaleCalculator {

	/**
	 * Maximum value to display on the counts scale.
	 */
	private static final int MAXIMUM_COUNTS = 1000000000;

	private final int range, scaleFactor;

	/**
	 * @param range
	 *            The full counts range.
	 * @param scaleFactor
	 *            The scale factor to use.
	 */
	public ScaleCalculator(int range, int scaleFactor) {
		super();
		this.range = range;
		this.scaleFactor = scaleFactor;
	}

	/**
	 * @param lowerLimit
	 *            the lower limit
	 * @param upperLimit
	 *            the upper limit
	 * @return the scale spacing
	 */
	public int compute(final int lowerLimit, final int upperLimit) {
		int result = 1;
		// loop trying successively bigger tick spacing
		for (int i = 1; i < MAXIMUM_COUNTS; i *= 10) {
			result = i;
			if ((result * scaleFactor) >= range) {
				break;
			}
			result = i * 2;
			if ((result * scaleFactor) >= range) {
				break;
			}
			result = i * 5;
			if ((result * scaleFactor) >= range) {
				break;
			}
		}
		return result;
	}
}
