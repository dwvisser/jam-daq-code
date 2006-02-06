/**
 * 
 */
package jam.plot;

import jam.data.Histogram;
import jam.global.JamStatus;
import jam.global.Nameable;

import javax.swing.DefaultBoundedRangeModel;

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
	 * holds ref to plots
	 */
	protected transient PlotContainer plot;

	AbstractScrollBarRangeModel(PlotContainer container) {
		super();
		setFields(container);
	}

	private final void setFields(final PlotContainer container) {
		plot = container;
		final Nameable hist = JamStatus.getSingletonInstance()
				.getCurrentHistogram();
		if (hist instanceof Histogram) {
			lim = Limits.getLimits((Histogram)hist);
		}
		setDisplayLimits();
	}

	/**
	 * Set the limits based on the model.
	 *
	 */
	protected abstract void setDisplayLimits();

}
