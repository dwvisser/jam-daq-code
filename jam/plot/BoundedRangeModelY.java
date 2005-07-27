package jam.plot;


/**
 * <code>Scroller</code> contains instance of this, and it is be handed to the
 * vertical <code>JScrollBar</code>.<code>setFields()</code> is to be
 * called whenever the displayed <code>Histogram</code> changes.
 * 
 * @author Dale Visser
 * @version 1.2
 */
final class BoundedRangeModelY extends AbstractScrollBarRangeModel {

	BoundedRangeModelY(PlotContainer container) {
		super(container);
	}

	void scrollBarMoved() {
		int minY, maxY;
		maxY = plot.getSizeY() - 1 - getValue();
		minY = maxY - getExtent();
		if (lim != null) {
			lim.setLimitsY(minY, maxY);
		}
	}

	/**
	 * Set model using values in Limits object.
	 */
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
		setRangeProperties(value, extent, min, max, true);//BoundedRangeModel
														  // method
	}
}