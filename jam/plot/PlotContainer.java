package jam.plot;

import jam.data.AbstractHist1D;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.ComponentPrintable;
import jam.plot.AbstractPlot.Size;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.print.PageFormat;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * class for displayed plots.
 * 
 * @version 0.5
 * @see jam.plot.Plot1d
 * @see jam.plot.Plot2d
 * @since JDK 1.1
 * @author Ken Swartz
 */
public final class PlotContainer implements PlotPrefs, PlotSelectListener {

	enum LayoutType {
		/** Layout with axis labels without border */
		LABELS,
		/** Layout with axis labels and border */
		LABELS_BORDER,
		/** Layout not axis labels without border */
		NO_LABELS,
		/** Layout not axis labels but with border */
		NO_LABELS_BORDER
	};

	/**
	 * Zoom direction.
	 * 
	 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
	 */
	enum Zoom {
		/**
		 * Zoom in.
		 */
		IN,

		/**
		 * Zoom out.
		 */
		OUT
	}

	private static final String KEY1 = "1D Plot";

	private static final String KEY2 = "2D Plot";

	/*
	 * any access to currentSubPlot should be syn synchronized on this
	 */
	private transient final Object plotLock = new Object();

	private transient final CardLayout plotSwapPanelLayout;

	private transient final Plot1d plot1d;

	private transient final Plot2d plot2d;

	private transient final Scroller scroller1d, scroller2d;

	private transient boolean hasData;

	private transient final PlotSelectListener plotSelectListener;

	private transient LayoutType layoutType;

	private transient int plotNumber;

	private transient AbstractPlot currentSubPlot;

	private transient final JPanel panel = new JPanel();

	/**
	 * @param plotSelect
	 *            place to send selection messages
	 */
	private PlotContainer(PlotSelectListener plotSelect) {
		plotSelectListener = plotSelect;
		/*
		 * panel containing plots panel to holds 1d and 2d plots, and swaps them
		 */
		plotSwapPanelLayout = new CardLayout();
		panel.setLayout(plotSwapPanelLayout);
		/* panel 1d plot and its scroll bars */
		plot1d = new Plot1d();
		scroller1d = new Scroller(plot1d);
		panel.add(KEY1, scroller1d);
		plot1d.getPlotMouse().setPlotSelectListener(this);
		/* panel 2d plot and its scroll bars */
		plot2d = new Plot2d();
		scroller2d = new Scroller(plot2d);
		panel.add(KEY2, scroller2d);
		plot2d.getPlotMouse().setPlotSelectListener(this);
		/* Initial show plot1d */
		plotSwapPanelLayout.show(panel, KEY1);
		currentSubPlot = plot1d;
	}

	static PlotContainer createPlotContainer(final PlotSelectListener psl) {
		return new PlotContainer(psl);
	}

	/**
	 * Select this plot as the current plot
	 * 
	 * @param selectedState
	 */
	void select(final boolean selectedState) {
		Border border = null;
		if ((layoutType == LayoutType.LABELS_BORDER)
				|| (layoutType == LayoutType.NO_LABELS_BORDER)) {
			border = selectedState ? BorderFactory.createLineBorder(
					Color.BLACK, 2) : BorderFactory.createEmptyBorder(2, 2, 2,
					2);
		}
		panel.setBorder(border);
	}

	/**
	 * Set the layout to include or not include the margins
	 * 
	 * @param type
	 */
	void setLayoutType(final LayoutType type) {
		layoutType = type;
		// Plot labels
		if ((type == LayoutType.LABELS_BORDER) || (type == LayoutType.LABELS)) {
			plot1d.setLayout(PlotGraphicsLayout.Type.WITH_LABELS);
			plot2d.setLayout(PlotGraphicsLayout.Type.WITH_LABELS);
		} else if ((type == LayoutType.NO_LABELS_BORDER)
				|| (type == LayoutType.NO_LABELS)) {
			plot1d.setLayout(PlotGraphicsLayout.Type.WO_LABELS);
			plot2d.setLayout(PlotGraphicsLayout.Type.WO_LABELS);

		}
		// Plot border
		final int borderWidth = (type == LayoutType.LABELS_BORDER || type == LayoutType.NO_LABELS_BORDER) ? 2
				: 0;
		final Border border = BorderFactory.createEmptyBorder(borderWidth,
				borderWidth, borderWidth, borderWidth);
		panel.setBorder(border);
	}

	/**
	 * Display a histogram,
	 * 
	 * @param hist
	 *            histogram to display
	 */
	void displayHistogram(final Histogram hist) {
		synchronized (plotLock) {
			select(true);
			if (hist == null) {
				/* Histogram is null set to blank 1D */
				currentSubPlot = plot1d;
				hasData = false;
				plotSwapPanelLayout.show(panel, KEY1);
			} else {
				hasData = true;
				final int dim = hist.getDimensionality();
				final String key;
				if (dim == 1) {
					currentSubPlot = plot1d;
					key = KEY1;
				} else {
					currentSubPlot = plot2d;
					key = KEY2;
				}
				plotSwapPanelLayout.show(panel, key);
			}
			currentSubPlot.displayHistogram(hist);
		}
	}

	/**
	 * Overlay histograms
	 * 
	 * @param hists
	 *            to overlay
	 */
	void overlayHistograms(final List<AbstractHist1D> hists) {
		currentSubPlot.overlayHistograms(hists);
	}

	/**
	 * Clear overlays.
	 */
	void removeOverlays() {
		currentSubPlot.removeOverlays();
	}

	/**
	 * Get the current histogram
	 * 
	 * @return the histogram displayed in the current subplot
	 */
	Histogram getHistogram() {
		return getPlot().getHistogram();
	}

	/**
	 * Returns whether this plot has a valid histogram.
	 * 
	 * @return <code>true</code> if this plot contains a histogram
	 */
	boolean hasHistogram() {
		return hasData;
	}

	/**
	 * Type of plot 1D or 2D
	 * 
	 * @return plot type
	 */
	int getDimensionality() {
		return (getPlot() == plot1d) ? 1 : 2;
	}

	void enableScrolling(final boolean enable) {
		scroller1d.enableScrolling(enable);
		scroller2d.enableScrolling(enable);
	}

	int getNumber() {
		return plotNumber;
	}

	void setNumber(final int numIn) {
		plotNumber = numIn;
	}

	Limits getLimits() {
		return getPlot().getLimits();
	}

	void markChannel(final Bin bin) {
		getPlot().markChannel(bin);
	}

	/**
	 * Only update plot if it has a histogram
	 * 
	 */
	void update() {
		if (hasData) {
			getPlot().update();
		}
	}

	/**
	 * @return currently selected subplot
	 */
	private AbstractPlot getPlot() {
		synchronized (plotLock) {
			return currentSubPlot;
		}
	}

	void displayFit(final double[][] signals, final double[] background,
			final double[] residuals, final int lowerLimit) {
		getPlot().displayFit(signals, background, residuals, lowerLimit);
	}

	/**
	 * Caller should have checked 'isCalibrated' first.
	 * 
	 * @param channel
	 *            to check energy for
	 * @return energy value for the channel
	 */
	double getEnergy(final double channel) {
		return getPlot().getEnergy(channel);
	}

	/**
	 * Get the counts in a bin.
	 * 
	 * @param channel
	 *            bin to grab counts from
	 * @return counts in the bin
	 */
	double getCount(final Bin channel) {
		return getPlot().getCount(channel);
	}

	int getChannel(final double energy) {
		return getPlot().getChannel(energy);
	}

	/**
	 * @return array of counts in the currently selected plot
	 */
	Object getCounts() {
		return getPlot().getCounts();
	}

	/**
	 * 
	 * @param value
	 *            value to check
	 * @param size
	 *            x-range to stay within
	 * @param useMax
	 *            whether to use 0 or xMax if out of range
	 * @return input value if in range, 0 or xMax if not
	 */
	private int constrainValue(final int value, final int size,
			final boolean useMax) {
		final int max = size - 1;
		int rval = value;
		if ((value < 0) || (value > max)) {
			rval = useMax ? max : 0;
		}
		return rval;
	}

	/*
	 * Non-javadoc: Zoom the region viewed.
	 */
	void zoom(final PlotContainer.Zoom inOut) {
		final Limits limits = getLimits();
		int xll = limits.getMinimumX();
		int xul = limits.getMaximumX();
		int yll = limits.getMinimumY();
		int yul = limits.getMaximumY();
		// Specifies how much to zoom, zoom is 1/ZOOM_FACTOR
		final int ZOOM_FACTOR = 10;
		final int diffX = Math.max(1, (xul - xll) / ZOOM_FACTOR);
		final int diffY = Math.max(1, (yul - yll) / ZOOM_FACTOR);
		final int zoomSign = (inOut == PlotContainer.Zoom.IN) ? 1 : -1;
		xll += zoomSign * diffX;
		xul -= zoomSign * diffX;
		yll += zoomSign * diffY;
		yul -= zoomSign * diffY;
		final AbstractPlot plot = getPlot();
		final Size size = plot.getSize();
		/* check if beyond extremes, if so, set to extremes */
		xll = constrainValue(xll, size.getSizeX(), false);
		xul = constrainValue(xul, size.getSizeX(), true);
		if (xll > xul) {
			final int temp = xll;
			xll = xul - 1;
			xul = temp + 1;
		}
		yll = constrainValue(yll, size.getSizeY(), false);
		yul = constrainValue(yul, size.getSizeY(), true);
		if (yll > yul) {
			final int temp = yll;
			yll = yul - 1;
			yul = temp + 1;
		}
		limits.setLimitsX(xll, xul);
		limits.setLimitsY(yll, yul);
		plot.refresh();
	}

	void markArea(final Bin bin1, final Bin bin2) {
		getPlot().markArea(bin1, bin2);
	}

	void setMarkArea(final boolean isMarkingArea) {
		getPlot().setMarkArea(isMarkingArea);
	}

	void autoCounts() {
		getPlot().autoCounts();
	}

	void setRange(final int limC1, final int limC2) {
		getPlot().setRange(limC1, limC2);
	}

	/**
	 * Expand a region
	 * 
	 * @param channel1
	 * @param channel2
	 */
	void expand(final Bin channel1, final Bin channel2) {
		getPlot().expand(channel1, channel2);
	}

	/**
	 * Show full plot
	 */
	void setFull() {
		getPlot().setFull();
	}

	/**
	 * Set the scale as linear
	 */
	void setLinear() {
		getPlot().setLinear();
	}

	/**
	 * Set the scale as log
	 */
	void setLog() {
		getPlot().setLog();
	}

	void setBinWidth(final double width) {
		getPlot().setBinWidth(width);
	}

	double getBinWidth() {
		return getPlot().getBinWidth();
	}

	void setSelectingArea(final boolean isSelectingArea) {
		getPlot().setSelectingArea(isSelectingArea);
	}

	void setMarkingChannels(final boolean isMarkingChannels) {
		getPlot().setMarkingChannels(isMarkingChannels);
	}

	void initializeSelectingArea(final Bin bin) {
		getPlot().initializeSelectingArea(bin);
	}

	void displayGate(final Gate gate) {
		getPlot().displayGate(gate);
	}

	void displaySetGate(final GateSetMode mode, final Bin pChannel,
			final Point pPixel) {
		getPlot().displaySetGate(mode, pChannel, pPixel);
	}

	void setRenderForPrinting(final boolean printing, final PageFormat format) {
		getPlot().setRenderForPrinting(printing, format);
	}

	ComponentPrintable getComponentPrintable() {
		return getPlot().getComponentPrintable();
	}

	int getSizeX() {
		return getPlot().size.getSizeX();
	}

	int getSizeY() {
		return getPlot().size.getSizeY();
	}

	/**
	 * Add a mouse listener.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	void addPlotMouseListener(final PlotMouseListener listener) {
		plot1d.getPlotMouse().addListener(listener);
		plot2d.getPlotMouse().addListener(listener);
	}

	/* Mouse methods */

	/*
	 * non-javadoc: Remove a mouse listener.
	 */
	void removePlotMouseListener(final PlotMouseListener listener) {
		plot1d.getPlotMouse().removeListener(listener);
		plot2d.getPlotMouse().removeListener(listener);
	}

	void removeAllPlotMouseListeners() {
		plot1d.getPlotMouse().removeAllListeners();
		plot2d.getPlotMouse().removeAllListeners();
	}

	/**
	 * @see PlotSelectListener#plotSelected(Object)
	 */
	public void plotSelected(final Object source) {
		plotSelectListener.plotSelected(this);
	}

	// End Mouse methods
	void reset() {
		plot1d.reset();
		plot2d.reset();
	}

	void repaint() {
		panel.repaint();
	}

	Component getComponent() {
		return panel;
	}
}