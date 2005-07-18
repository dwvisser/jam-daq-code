package jam.plot;

import jam.data.Histogram;
import jam.global.JamStatus;

import javax.swing.DefaultBoundedRangeModel;

/**
 * <code>Scroller</code> contains instance of this, and it is be handed to the
 * horizontal <code>JScrollBar</code>.<code>setFields()</code> is to be
 * called whenever the displayed <code>Histogram</code> changes.
 * 
 * @author Dale Visser
 * @version 1.2
 */
final class BoundedRangeModelX extends DefaultBoundedRangeModel {

	private Limits lim;

	private PlotContainer plot;
	
	private static final JamStatus STATUS=JamStatus.getSingletonInstance();

	BoundedRangeModelX(PlotContainer p) {
		super();
		setFields(p);
	}

	void setFields(PlotContainer p) {
		plot = p;
		final Histogram hist = (Histogram)STATUS.getCurrentHistogram();
		if (hist != null) {
			lim = Limits.getLimits(hist);
		}
		setXDisplayLimits();
	}

	void scrollBarMoved() {
		int maxX = getValue() + getExtent()/*-1*/;
		int minX = getValue();
		if (lim != null) {
			lim.setLimitsX(minX, maxX);
		}
	}

	/**
	 * Set model using values in Limits object.
	 */
	void setXDisplayLimits() {
		int min, max, extent, value;
		min = 0;
		max = plot.getSizeX() - 1;
		if (lim != null) {
			extent = lim.getMaximumX() - lim.getMinimumX()/* +1 */;
			value = lim.getMinimumX();
		} else {
			extent = max - min + 1;
			value = 0;
		}
		/*
		 * BoundedRangeModel method, throws appropriate event up to scroll bar.
		 */
		setRangeProperties(value, extent, min, max, true);
	}
}