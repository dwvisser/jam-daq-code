/**
 * 
 */
package jam.fit;

import jam.plot.Bin;
import jam.plot.PlotDisplay;

import java.awt.Point;
import java.util.Iterator;

final class PlotInteraction implements jam.plot.PlotMouseListener {
	private transient final PlotDisplay plotDisplay;

	private transient final ParameterList parameterList;

	/**
	 * <code>Enumeration</code> of parameters
	 */
	private transient Iterator<Parameter<?>> parameterIter;

	PlotInteraction(final PlotDisplay display, final ParameterList parameters) {
		this.plotDisplay = display;
		this.parameterList = parameters;
	}

	protected void resetIterator() {
		this.parameterIter = this.parameterList.iterator();
	}

	public void plotMousePressed(final Bin bin, final Point pPixel) {
		while (this.parameterIter.hasNext()) {
			final Parameter<?> parameter = this.parameterIter.next();
			if (parameter.isMouseClickable() && (!parameter.isFixed())) {
				parameterList.setParameterText(bin, parameter);
				break;
			}
		}
		if (!this.parameterIter.hasNext()) {
			this.setMouseActive(false);
		}
	}

	protected void setMouseActive(final boolean state) {
		if (state) {
			this.plotDisplay.addPlotMouseListener(this);
		} else {
			this.plotDisplay.removePlotMouseListener(this);
		}
	}

	protected void displayFit(final double[][] signals,
			final double[] background, final double[] residuals,
			final int lowerLimit) {
		plotDisplay.getPlotContainer().displayFit(signals, background,
				residuals, lowerLimit);
	}

	protected void clear() {
		this.parameterList.clearGUI();
		setMouseActive(false);
	}

	/**
	 * Resets all the parameter to default values, which are zero for now.
	 * 
	 */
	protected void reset() {
		this.parameterList.resetGUI();
		clear();
	}

	protected void updateParametersFromDialog() throws FitException {
		for (Parameter<?> parameter : this.parameterList) {
			try {
				this.parameterList.updateParameterFromDialog(parameter);
			} catch (NumberFormatException nfe) {
				this.clear();
				throw new FitException("Invalid input, parameter: "
						+ parameter.getName(), nfe);
			}
		}
	}
}