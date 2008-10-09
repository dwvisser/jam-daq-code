package jam.plot;

/**
 * Used to access the current displayed plot.
 * 
 * @author Dale Visser
 * 
 */
public interface CurrentPlotAccessor extends Updatable {
	/**
	 * @return the current displayed plot
	 */
	PlotContainer getPlotContainer();
}
