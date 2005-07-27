/**
 * 
 */
package jam.plot;

import jam.data.Histogram;
import jam.global.JamStatus;

import javax.swing.DefaultBoundedRangeModel;

/**
 * @author Dale Visser
 *
 */
abstract class AbstractScrollBarRangeModel extends DefaultBoundedRangeModel {
	
	protected transient Limits lim;

	protected transient PlotContainer plot;
	
	protected static final JamStatus STATUS=JamStatus.getSingletonInstance();
	
	AbstractScrollBarRangeModel(PlotContainer container) {
		super();
		setFields(container);
	}

	private final void setFields(final PlotContainer container) {
		plot = container;
		final Histogram hist = (Histogram)STATUS.getCurrentHistogram();
		if (hist != null) {
			lim = Limits.getLimits(hist);
		}
		setDisplayLimits();
	}
	
	protected abstract void setDisplayLimits();

}
