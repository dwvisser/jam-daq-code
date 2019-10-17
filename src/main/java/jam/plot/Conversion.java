package jam.plot;

/**
 * Gives the number of pixels per channel for each screen dimension.
 * 
 * @author Dale Visser
 */
final class Conversion {

	private transient final double xFactor, yFactor, yLogFactor;

	Conversion(final double xfac, final double yfac, final double ylogfac) {
		super();
		xFactor = xfac;
		yFactor = yfac;
		yLogFactor = ylogfac;
	}

	protected double getX() {
		return xFactor;
	}

	protected double getY() {
		return yFactor;
	}

	protected double getYLog() {
		return yLogFactor;
	}
}