/**
 * 
 */
package jam.plot;

import jam.data.AbstractHistogram;
import jam.global.Nameable;
import jam.ui.SelectionTree;

import javax.swing.*;

/**
 * @author Dale Visser
 * 
 */
abstract class AbstractScrollBarRangeModel extends DefaultBoundedRangeModel {

	/**
	 * plot domain and range to show
	 */
	protected transient Limits lim;

	/**
	 * holds reference to plots
	 */
	protected transient PlotContainer plot;

	AbstractScrollBarRangeModel(final PlotContainer container) {
		super();
		setFields(container);
	}

	private final void setFields(final PlotContainer container) {
		plot = container;
		final Nameable hist = SelectionTree.getCurrentHistogram();
		if (hist instanceof AbstractHistogram) {
			lim = Limits.getLimits((AbstractHistogram) hist);
		}
		setDisplayLimits();
	}

	/**
	 * Set the limits based on the model.
	 * 
	 */
	protected abstract void setDisplayLimits();

}
