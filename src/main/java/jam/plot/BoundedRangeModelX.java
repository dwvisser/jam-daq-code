package jam.plot;

/**
 * <code>Scroller</code> contains instance of this, and it is be handed to the
 * horizontal <code>JScrollBar</code>.<code>setFields()</code> is to be called
 * whenever the displayed <code>Histogram</code> changes.
 * 
 * @author Dale Visser
 * @version 1.2
 */
final class BoundedRangeModelX extends AbstractScrollBarRangeModel {

	BoundedRangeModelX(final PlotContainer container) {
		super(container);
	}

	protected void scrollBarMoved() {
		if (lim != null) {
			final int minX = getValue();
			final int maxX = minX + getExtent();
			lim.setLimitsX(minX, maxX);
		}
	}

	/**
	 * Set model using values in Limits object.
	 */
	@Override
	protected void setDisplayLimits() {
		int min, max, extent, value;
		min = 0;
		max = plot.getSizeX() - 1;
		if (lim == null) {
			extent = max - min + 1;
			value = 0;
		} else {
			extent = lim.getMaximumX() - lim.getMinimumX();
			value = lim.getMinimumX();
		}
		/*
		 * BoundedRangeModel method, throws appropriate event up to scroll bar.
		 */
		setRangeProperties(value, extent, min, max, true);
	}
}