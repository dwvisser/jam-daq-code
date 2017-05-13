package jam.data.peaks;

/**
 * This class represents a gaussian peak, in terms of it's properties. Fields
 * are also provided for the error bars on these properties.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version 2001-02-14
 */
public final class Peak implements Comparable<Peak>, Cloneable {

	private static final String AREA = "\n  Area = ";

	private static final String FWHM = "\n  FWHM = ";

	private static final String PEAK_POSITION = "Peak\n  Position = ";

	private static final String PLUSMINUS = " +/- ";

	/**
	 * Peak factory method.
	 * 
	 * @param position
	 *            of centroid
	 * @param area
	 *            of peak
	 * @param width
	 *            FWHM of peak
	 * @return a new Peak object
	 */
	public static Peak createPeak(final double position, final double area,
			final double width) {
		return new Peak(position, area, width);
	}

	private transient double perr, aerr, werr;

	private double position, area, width;

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
	private Peak(final double position, final double area, final double width) {
		this(position, 0.0, area, 0.0, width, 0.0);
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
	private Peak(final double posn, final double posnErr,
			final double intensity, final double intErr, final double wid,
			final double widErr) {
		super();
		setPosition(posn, posnErr);
		setArea(intensity, intErr);
		setWidth(wid, widErr);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		final Peak rval = (Peak) super.clone();
		rval.setPosition(position, perr);
		rval.setArea(area, aerr);
		rval.setWidth(width, werr);
		return rval;
	}

	public int compareTo(final Peak other) {
		return (int) Math.signum(getPosition() - other.getPosition());
	}

	@Override
	public boolean equals(final Object object) {
		boolean rval = false;
		if (object instanceof Peak) {
			final Peak other = (Peak) object;
			rval = getPosition() == other.getPosition();
		}
		return rval;
	}

	double getArea() {// NOPMD
		return area;
	}

	/**
	 * @return centroid of peak
	 */
	double getPosition() {// NOPMD
		return position;
	}

	double getWidth() {// NOPMD
		return width;
	}

	@Override
	public int hashCode() {
		return Double.valueOf(getPosition()).hashCode();
	}

	Peak offset(final double correction) {// NOPMD
		return new Peak(position + correction, perr, area, aerr, width, werr);
	}

	void setArea(final double... intensity) {// NOPMD
		final int len = intensity.length;
		if (len > 0) {
			area = intensity[0];
			if (len > 1) {
				aerr = intensity[1];
			}
		}
	}

	private void setPosition(final double posn, final double unc) {
		position = posn;
		perr = unc;
	}

	private void setWidth(final double wid, final double widErr) {
		width = wid;
		werr = widErr;
	}

	@Override
	public String toString() {
		final StringBuilder rval = new StringBuilder(58);
		rval.append(PEAK_POSITION);
		rval.append(position).append(PLUSMINUS);
		rval.append(perr);
		rval.append(AREA).append(area).append(PLUSMINUS).append(aerr);
		rval.append(FWHM).append(width).append(PLUSMINUS).append(werr);
		rval.append('\n');
		return rval.toString();
	}

}