package jam.plot;

import jam.data.*;
import jam.plot.common.Scale;

import javax.swing.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Stores the parameters on how the histograms are to be displayed. This
 * includes
 * <ul>
 * <li>x channel display limits</li>
 * <li>y channel display limits</li>
 * <li>count range display limits</li>
 * <li>whether scale is linear or log</li>
 * </ul>
 * There is a separate instance of <code>Limits</code> for every
 * <code>Histogram</code>. The class contains a <code>static 
 * Hashtable</code> referring to all the <code>Limits</code> objects by the
 * associated <code>Histogram</code> object.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version 1.4
 */
final class Limits {

	/**
	 * Lookup table for the display limits for the various histograms.
	 */
	private final static Map<String, Limits> TABLE = Collections
			.synchronizedMap(new HashMap<String, Limits>());

	private static final int INITLO = 0;

	private static final int INITHI = 100;

	private static final int DEFAULTMAXCOUNTS = 5;

	private transient final String histName;

	private transient final BoundedRangeModel rangeModelX = new DefaultBoundedRangeModel();

	private transient final BoundedRangeModel rangeModelY = new DefaultBoundedRangeModel();

	private int minimumX, maximumX;

	private int minimumY, maximumY;

	private int minimumCounts, maximumCounts;

	private transient final int sizeX; // translate to rangemodel min, max

	private transient final int sizeY; // translate to rangemodel min, max

	private Scale scale = Scale.LINEAR; // is it in log or linear

	/**
	 * Use for initial value instead of null reference.
	 */
	public static final Limits NULL = new Limits();

	/**
	 * Creates the display limits for the specified histogram, specifying
	 * whether to ignore first and/or last channels for auto-scaling.
	 * 
	 * @param hist
	 *            Histogram for which this object provides display limits
	 * @param ignoreZero
	 *            ignores channel zero for auto scaling histogram
	 * @param ignoreFull
	 *            ignores the last channel for auto scaling histogram
	 */
	private Limits(final AbstractHistogram hist, final boolean ignoreZero,
			final boolean ignoreFull) {
		super();
		if (hist == null) {
			throw new IllegalArgumentException(
					"Can't have null histogram reference in Limits constructor.");
		}
		histName = hist.getFullName();
		TABLE.put(histName, this);
		sizeX = hist.getSizeX() - 1;
		sizeY = hist.getSizeY() - 1;
		init(ignoreZero, ignoreFull);// set initial values
		/* update the bounded range models */
		updateModelX();
		updateModelY();
	}

	private Limits() {
		super();
		histName = "";
		sizeX = 0;
		sizeY = 0;
		updateModelX();
		updateModelY();
	}

	private static final Limits LIMITS_NULL = new Limits();

	/**
	 * Determines initial limit values, X and Y limits set to extremes for the
	 * given histogram, and the counts scale is auto-scaled.
	 * 
	 * @param ignoreZero
	 *            true if the zero channel is ignored for auto-scaling
	 * @param ignoreFull
	 *            true if the last channel is ignored for auto-scaling
	 */
	private void init(final boolean ignoreZero, final boolean ignoreFull) {
		final AbstractHistogram histogram = AbstractHistogram.getHistogram(histName);
		final int dim = histogram.getDimensionality();
		final int sizex = histogram.getSizeX();
		final int sizey = histogram.getSizeY();
		setLimitsX(INITLO, sizex - 1);
		if (dim == 1) {
			setLimitsY(INITLO, INITHI);
			setScale(Scale.LINEAR);
		} else {// 2-dim
			setLimitsY(INITLO, sizey - 1);
			setScale(Scale.LOG);
		}
		/* auto scale counts */
		int chminX = 0;
		int chminY = 0;
		if (ignoreZero) {
			chminX = 1;
			chminY = 1;
		}
		int chmaxX = sizex;
		int chmaxY = sizey;
		int diff = 1;
		if (ignoreFull) {
			diff = 2;
		}
		chmaxX -= diff;
		chmaxY -= diff;
		final int maxCounts = getMaxCounts(chminX, chmaxX, chminY, chmaxY);
		setLimitsCounts(INITLO, maxCounts);
	}

	private int getMaxCounts(final int chminX, final int chmaxX,
			final int chminY, final int chmaxY) {
		int maxCounts;

		final int scaleUp = 110;
		final int scaleBackDown = 100;
		final AbstractHistogram histogram = AbstractHistogram.getHistogram(histName);
		if (histogram.getDimensionality() == 1) {
			maxCounts = getMaxCounts1D((AbstractHist1D) histogram, chminX,
					chmaxX);
		} else {// dim==2
			maxCounts = getMaxCounts2D((AbstractHist2D) histogram, chminX,
					chmaxX, chminY, chmaxY);
		}
		maxCounts *= scaleUp;
		maxCounts /= scaleBackDown;
		return maxCounts;
	}

	private int getMaxCounts1D(final AbstractHist1D hist, final int chminX,
			final int chmaxX) {
		int maxCounts = DEFAULTMAXCOUNTS;
		if (hist instanceof HistDouble1D) {
			final double[] countsD = ((HistDouble1D) hist).getCounts();
			for (int i = chminX; i <= chmaxX; i++) {
				maxCounts = Math.max(maxCounts, (int) countsD[i]);
			}
		} else {// int[]
			final int[] countsInt = ((HistInt1D) hist).getCounts();
			for (int i = chminX; i <= chmaxX; i++) {
				maxCounts = Math.max(maxCounts, countsInt[i]);
			}
		}
		return maxCounts;
	}

	private int getMaxCounts2D(final AbstractHist2D hist, final int chminX,
			final int chmaxX, final int chminY, final int chmaxY) {
		int maxCounts = DEFAULTMAXCOUNTS;
		if (hist instanceof HistDouble2D) {
			final double[][] counts2d = ((HistDouble2D) hist).getCounts();
			for (int i = chminX; i <= chmaxX; i++) {
				for (int j = chminY; j <= chmaxY; j++) {
					maxCounts = Math.max(maxCounts, (int) counts2d[i][j]);
				}
			}
		} else {// instanceof int [][]
			final int[][] counts2d = ((HistInt2D) hist).getCounts();
			for (int i = chminX; i <= chmaxX; i++) {
				for (int j = chminY; j <= chmaxY; j++) {
					maxCounts = Math.max(maxCounts, counts2d[i][j]);
				}
			}
		}
		return maxCounts;
	}

	/**
	 * Get the limits for a <code>Histogram</code>.
	 * 
	 * @param hist
	 *            Histogram to retrieve the limits for
	 * @return display limits for the specified histogram
	 */
	protected static Limits getLimits(final AbstractHistogram hist) {
		final Limits rval;
		if (hist == null) {
			rval = LIMITS_NULL;
		} else {
			final Object object = TABLE.get(hist.getFullName());
			if (object == null) {
				final Preferences prefs = PlotPreferences.PREFS;
				final boolean ignoreZero = prefs.getBoolean(
						PlotPreferences.AUTO_IGNORE_ZERO, true);
				final boolean ignoreFull = prefs.getBoolean(
						PlotPreferences.AUTO_IGNORE_FULL, true);
				rval = new Limits(hist, ignoreZero, ignoreFull);
			} else {
				rval = (Limits) object;
			}
		}
		return rval;
	}

	/**
	 * @return model for scrollbar attached to X-limits
	 */
	protected BoundedRangeModel getModelX() {
		return rangeModelX;
	}

	/**
	 * @return model for scrollbar attached to Y-limits
	 */
	protected BoundedRangeModel getModelY() {
		return rangeModelY;
	}

	/**
	 * Set the limits for the horizontal dimension.
	 * 
	 * @param minX
	 *            new minimum x value
	 * @param maxX
	 *            new maximum x value
	 */
	protected void setLimitsX(final int minX, final int maxX) {
		synchronized (this) {
			minimumX = minX;
			maximumX = maxX;
			updateModelX();
		}
	}

	/**
	 * Set the minimimum X limit.
	 * 
	 * @param minX
	 *            new minimum x value
	 */
	protected void setMinimumX(final int minX) {
		synchronized (this) {
			minimumX = minX;
			updateModelX();
		}
	}

	/**
	 * Sets the new maximum x value.
	 * 
	 * @param maxX
	 *            the new maximum x value
	 */
	protected void setMaximumX(final int maxX) {
		synchronized (this) {
			maximumX = maxX;
			updateModelX();
		}
	}

	/**
	 * Set the Y limits.
	 * 
	 * @param minY
	 *            minumum Y to display
	 * @param maxY
	 *            maximum Y to display
	 */
	protected void setLimitsY(final int minY, final int maxY) {
		synchronized (this) {
			minimumY = minY;
			maximumY = maxY;
			updateModelY();
		}
	}

	/**
	 * Set the minimimum Y limit.
	 * 
	 * @param minY
	 *            minumum Y to display
	 */
	protected void setMinimumY(final int minY) {
		synchronized (this) {
			minimumY = minY;
			updateModelY();
		}
	}

	/**
	 * Set the maximum Y limit.
	 * 
	 * @param maxY
	 *            maximum Y to display
	 */
	protected void setMaximumY(final int maxY) {
		synchronized (this) {
			maximumY = maxY;
			updateModelY();
		}
	}

	/**
	 * Set the Count limits.
	 * 
	 * @param minCounts
	 *            the lowest count value to display
	 * @param maxCounts
	 *            the highest count value to display
	 */
	private void setLimitsCounts(final int minCounts, final int maxCounts) {
		setMinimumCounts(minCounts);
		setMaximumCounts(maxCounts);
	}

	/**
	 * Set the minimum count limit.
	 * 
	 * @param minCounts
	 *            the lowest count value to display
	 */
	protected void setMinimumCounts(final int minCounts) {
		synchronized (this) {
			minimumCounts = minCounts;
		}
	}

	/**
	 * Set the maximum count limit.
	 * 
	 * @param maxCounts
	 *            the highest count value to display
	 */
	protected void setMaximumCounts(final int maxCounts) {
		synchronized (this) {
			maximumCounts = maxCounts;
		}
	}

	/**
	 * Set the scale to log or linear.
	 * 
	 * @param newScale
	 *            one of <code>Limits.LINEAR</code> or <code>
	 * Limits.LOG</code>
	 */
	protected void setScale(final Scale newScale) {
		synchronized (scale) {
			scale = newScale;
		}
	}

	/**
	 * update the values from the model
	 */
	protected void update() {
		synchronized (this) {
			minimumX = rangeModelX.getValue();
			maximumX = minimumX + rangeModelX.getExtent();
			minimumY = sizeY - rangeModelY.getValue() - rangeModelY.getExtent();
			maximumY = sizeY - rangeModelY.getValue();
		}
	}

	/**
	 * update range model X this updates the scroll bars
	 */
	private void updateModelX() {
		final int value = minimumX;
		final int extent = maximumX - minimumX;
		rangeModelX.setRangeProperties(value, extent, INITLO, sizeX, false);
	}

	/**
	 * update range model Y this updates the scroll bars
	 */
	private void updateModelY() {
		final int value = sizeY - maximumY;
		final int extent = maximumY - minimumY;
		rangeModelY.setRangeProperties(value, extent, INITLO, sizeY, false);
	}

	/**
	 * @return the lowest x-channel to display
	 */
	protected int getMinimumX() {
		synchronized (this) {
			return minimumX;
		}
	}

	/**
	 * @return the highest x-channel to display
	 */
	protected int getMaximumX() {
		synchronized (this) {
			return maximumX;
		}
	}

	/**
	 * @return the lowest y-channel to display
	 */
	protected int getMinimumY() {
		synchronized (this) {
			return minimumY;
		}
	}

	/**
	 * @return the highest y-channel to display
	 */
	protected int getMaximumY() {
		synchronized (this) {
			return maximumY;
		}
	}

	/**
	 * @return the minimum count level to be displayed
	 */
	protected int getMinimumCounts() {
		synchronized (this) {
			return minimumCounts;
		}
	}

	/**
	 * @return the maximum count level to be displayed
	 */
	protected int getMaximumCounts() {
		synchronized (this) {
			return maximumCounts;
		}
	}

	/**
	 * Get the scale, linear or Log.
	 * 
	 * @return <code>PlotGraphics.LINEAR</code> or <code>
	 * PlotGraphics.LOG</code>
	 */
	protected Scale getScale() {
		synchronized (scale) {
			return scale;
		}
	}

	/**
	 * @return text giving the histogram and the X-limits
	 */
	@Override
	public String toString() {
        String temp = "Limits of \"" + histName +
                "\": " +
                "MinX: " +
                getMinimumX() +
                ", MaxX: " +
                getMaximumX();
        return temp;
	}

}
