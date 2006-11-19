package jam.data.peaks;

/**
 * This class represents a gaussian peak, in terms of it's properties. Fields
 * are also provided for the error bars on these properties.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 2001-02-14
 */
final class Peak extends Object implements Comparable, Cloneable {

	private double position, area, width;

	private transient double perr, aerr, werr;

	/**
	 * Creates new Peak assuming no uncertainty in values.
	 * 
	 * @param position
	 *            position of the peak centroid
	 * @param area
	 *            total peak area
	 * @param width
	 *            Full width at half max of the peak
	 */
	private Peak(double position, double area, double width) {
		this(position, 0.0, area, 0.0, width, 0.0);
	}

	static Peak createPeak(final double position, final double area,
			final double width) {
		return new Peak(position, area, width);
	}

	/**
	 * Generates a peak with error bars on its parameters.
	 * 
	 * @param posn
	 *            position of peak centroid
	 * @param posnErr
	 *            error on position
	 * @param intensity
	 *            area of peak
	 * @param intErr
	 *            uncertainty in area
	 * @param wid
	 *            FWHM of peak
	 * @param widErr
	 *            uncertainty in FWHM
	 */
	private Peak(double posn, double posnErr, double intensity, double intErr,
			double wid, double widErr) {
		super();
		setPosition(posn, posnErr);
		setArea(intensity, intErr);
		setWidth(wid, widErr);
	}

	protected Object clone() throws CloneNotSupportedException {
		final Peak rval = (Peak) super.clone();
		rval.setPosition(position, perr);
		rval.setArea(area, aerr);
		rval.setWidth(width, werr);
		return rval;
	}

	Peak offset(final double correction) {
		return new Peak(position + correction, perr, area, aerr, width, werr);
	}

	/**
	 * @return centroid of peak
	 */
	double getPosition() {
		return position;
	}

	double getArea() {
		return area;
	}

	double getWidth() {
		return width;
	}

	private void setPosition(final double posn, final double unc) {
		position = posn;
		perr = unc;
	}

	void setArea(final double... intensity) {
		final int len = intensity.length;
		if (len > 0) {
			area = intensity[0];
			if (len > 1) {
				aerr = intensity[1];
			}
		}
	}

	private void setWidth(final double wid, final double widErr) {
		width = wid;
		werr = widErr;
	}

	public String toString() {
		final StringBuilder rval = new StringBuilder(58);
		rval.append("Peak\n  Position = ");
		rval.append(position).append(" +/- ");
		rval.append(perr);
		rval.append("\n  Area = ").append(area).append(" +/- ").append(aerr);
		rval.append("\n  FWHM = ").append(width).append(" +/- ").append(werr);
		rval.append('\n');
		return rval.toString();
	}

	public int compareTo(final Object object) {
		int rval = 0;// default return value
		if (getPosition() < ((Peak) object).getPosition()) {
			rval = -1;
		} else if (getPosition() > ((Peak) object).getPosition()) {
			rval = 1;
		}
		return rval;
	}

}