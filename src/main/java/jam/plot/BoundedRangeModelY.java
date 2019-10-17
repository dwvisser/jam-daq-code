package jam.plot;

/**
 * <code>Scroller</code> contains instance of this, and it is be handed to the
 * vertical <code>JScrollBar</code>.<code>setFields()</code> is to be called
 * whenever the displayed <code>Histogram</code> changes.
 * 
 * @author Dale Visser
 * @version 1.2
 */
final class BoundedRangeModelY extends AbstractScrollBarRangeModel {

	BoundedRangeModelY(final PlotContainer container) {
		super(container);
	}

	protected void scrollBarMoved() {
		if (lim != null) {
			final int maxY = plot.getSizeY() - 1 - getValue();
			final int minY = maxY - getExtent();
			lim.setLimitsY(minY, maxY);
		}
	}

	/**
	 * Set model using values in Limits object.
	 */
	@Override
	protected void setDisplayLimits() {
		int min, max, extent, value;
		min = 0;
		max = plot.getSizeY() - 1;
		if (lim == null) {
			extent = max - min + 1;
			value = 0;
		} else {
			extent = lim.getMaximumY() - lim.getMinimumY();
			value = max - lim.getMaximumY();
		}
		setRangeProperties(value, extent, min, max, true);// BoundedRangeModel
		// method
	}
}