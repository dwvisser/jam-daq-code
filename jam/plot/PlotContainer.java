package jam.plot;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.ComponentPrintable;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.Collections;
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

	/* Layout with axis labels without border */
	static final int LAYOUT_TYPE_LABELS = 0;
	/* Layout with axis labels and border */ 
	static final int LAYOUT_TYPE_LABELS_BORDER = 1;
	/* Layout not axis labels without border */
	static final int LAYOUT_TYPE_NO_LABELS = 2;
	/* Layout not axis labels but with border */
	static final int LAYOUT_TYPE_NO_LABELS_BORDER = 3;
	
	/**
	 * Zoom direction.
	 * 
	 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
	 */
	static final class Zoom {
		private static final int INT_IN = 1;

		private static final int INT_OUT = 2;

		/**
		 * Zoom in.
		 */
		public static final Zoom IN = new Zoom(INT_IN);

		/**
		 * Zoom out.
		 */
		public static final Zoom OUT = new Zoom(INT_OUT);

		private final int direction;

		private Zoom(int i) {
			direction = i;
		}
		
		/**
		 * @see Object#equals(java.lang.Object)
		 */
		public boolean equals(Object other){
		    return other instanceof Zoom ? ((Zoom)other).direction==direction : false;
		}
	}

	private static final String KEY1 = "1D Plot";

	private static final String KEY2 = "2D Plot";

	/*
	 * any access to currentSubPlot should be syn synchronized on this
	 */
	private final Object plotLock = new Object();

	private final CardLayout plotSwapPanelLayout;

	private final Plot1d plot1d;

	private final Plot2d plot2d;

	private final Scroller scroller1d, scroller2d;

	private boolean hasHistogram;

	private final PlotSelectListener plotSelectListener;

	private final List overlays = Collections.synchronizedList(new ArrayList());

	private int layoutType, plotNumber;

	private AbstractPlot currentSubPlot;
	
	private final JPanel panel=new JPanel();
	
	/**
	 * @param graphicsLayout handles graphics details
	 * @param plotSelect place to send selection messages
	 */
	public PlotContainer(PlotGraphicsLayout graphicsLayout,
			PlotSelectListener plotSelect) {
		this.plotSelectListener = plotSelect;
		//isSelected = false;
		/*
		 * panel containing plots panel to holds 1d and 2d plots, and swaps them
		 */
		plotSwapPanelLayout = new CardLayout();
		panel.setLayout(plotSwapPanelLayout);
		/* panel 1d plot and its scroll bars */
		plot1d = new Plot1d();
		plot1d.setOverlayList(Collections.unmodifiableList(overlays));
		scroller1d = new Scroller(plot1d);
		panel.add(KEY1, scroller1d);
		plot1d.setPlotSelectListener(this);
		/* panel 2d plot and its scroll bars */
		plot2d = new Plot2d();
		scroller2d = new Scroller(plot2d);
		panel.add(KEY2, scroller2d);
		plot2d.setPlotSelectListener(this);
		/* Initial show plot1d */
		plotSwapPanelLayout.show(panel, KEY1);
		currentSubPlot = plot1d;
		//FIXME KBS temporary test
		//enableScrolling(false);
		//Enable prefernce
		//PREFS.addPreferenceChangeListener(this);
	}

	/**
	 * Select this plot as the current plot
	 * 
	 * @param selectedState
	 */
	void select(boolean selectedState) {
	    Border border = null;
        if ((layoutType == LAYOUT_TYPE_LABELS_BORDER)
                || (layoutType == LAYOUT_TYPE_NO_LABELS_BORDER)) {
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
	void setLayoutType(int type) {
        layoutType = type;
        //Plot labels
        if ((type == LAYOUT_TYPE_LABELS_BORDER) || (type == LAYOUT_TYPE_LABELS)) {
            plot1d.setLayout(PlotGraphicsLayout.LAYOUT_TYPE_LABELS);
            plot2d.setLayout(PlotGraphicsLayout.LAYOUT_TYPE_LABELS);
        } else if ((type == LAYOUT_TYPE_NO_LABELS_BORDER)
                || (type == LAYOUT_TYPE_NO_LABELS)) {
            plot1d.setLayout(PlotGraphicsLayout.LAYOUT_TYPE_NO_LABELS);
            plot2d.setLayout(PlotGraphicsLayout.LAYOUT_TYPE_NO_LABELS);

        }
        //Plot border
        final Border border = type == LAYOUT_TYPE_LABELS_BORDER
                || type == LAYOUT_TYPE_NO_LABELS_BORDER ? BorderFactory
                .createEmptyBorder(2, 2, 2, 2) : null;
        panel.setBorder(border);
    }

	/**
	 * Display a histogram,
	 * 
	 * @param hist
	 *            histogram to display
	 */
	void displayHistogram(Histogram hist) {
        synchronized (plotLock) {
            select(true);
            if (hist != null) {
                hasHistogram = true;
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
            } else {
                /* Histogram is null set to blank 1D */
                currentSubPlot = plot1d;
                hasHistogram = false;
                plotSwapPanelLayout.show(panel, KEY1);
            }
            currentSubPlot.displayHistogram(hist);
        }
    }

	/**
	 * Overlay histograms
	 * 
	 * @param num
	 *            the number of the histogram
	 */
	void overlayHistograms(int num) {
		final Integer value = new Integer(num);
		synchronized (plotLock) {
			/* Don't overlay histogram already displayed. */
			Histogram hist=currentSubPlot.getHistogram();
			if (hist!=null && 
				hist.getNumber() != num && 
				!overlays.contains(value)) {
				overlays.add(value);
			}
			currentSubPlot.overlayHistograms(overlays);
		}
	}
	
	/**
	 * Clear overlays.
	 */
	void removeOverlays() {
		overlays.clear();
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
	boolean hasHistogram(){
		return hasHistogram;
	}

	/**
	 * Type of plot 1D or 2D
	 * 
	 * @return plot type
	 */
	int getDimensionality() {
		return (getPlot() == plot1d) ? 1 : 2;
	}
	
	void enableScrolling(boolean enable){
		scroller1d.enableScrolling(enable);
		scroller2d.enableScrolling(enable);
	}

	int getNumber() {
		return plotNumber;
	}

	void setNumber(int numIn) {
		plotNumber = numIn;
	}

	Limits getLimits() {
		return getPlot().getLimits();
	}

	void markChannel(Bin p) {
		getPlot().markChannel(p);
	}

	/**
	 * Only update plot if it has a histogram
	 *
	 */
	void update() {
		if (hasHistogram)
			getPlot().update();
	}

	/**
	 * @return currently selected subplot
	 */
	AbstractPlot getPlot() {
		synchronized (plotLock) {
			return currentSubPlot;
		}
	}

	void displayFit(double[][] signals, double[] background,
			double[] residuals, int ll) {
		getPlot().displayFit(signals, background, residuals, ll);
	}

	/**
	 * Caller should have checked 'isCalibrated' first.
	 * 
	 * @param channel to check energy for
	 * @return energy value for the channel
	 */
	double getEnergy(double channel) {
		return getPlot().getEnergy(channel);
	}

	/**
	 * Get the counts in a bin.
	 * 
	 * @param p bin to grab counts from
	 * @return counts in the bin
	 */
	double getCount(Bin p) {
		return getPlot().getCount(p);
	}

	int getChannel(double energy) {
		return getPlot().getChannel(energy);
	}

	/**
	 * @return array of counts in the currently selected plot
	 */
	Object getCounts() {
		return getPlot().getCounts();
	}
	
	void zoom(Zoom inOut) {
		getPlot().zoom(inOut);
	}

	void markArea(Bin p1, Bin p2) {
		getPlot().markArea(p1, p2);
	}

	void setMarkArea(boolean tf) {
		getPlot().setMarkArea(tf);
	}

	void autoCounts() {
		getPlot().autoCounts();
	}

	void setRange(int limC1, int limC2) {
		getPlot().setRange(limC1, limC2);
	}

	/**
	 * Expand a region
	 * 
	 * @param c1
	 * @param c2
	 */
	void expand(Bin c1, Bin c2) {
		getPlot().expand(c1, c2);
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

	void setBinWidth(double x) {
		getPlot().setBinWidth(x);
	}
	
	double getBinWidth(){
		return getPlot().getBinWidth();
	}
	
	void setSelectingArea(boolean tf) {
		getPlot().setSelectingArea(tf);
	}

	void setMarkingChannels(boolean mc) {
		getPlot().setMarkingChannels(mc);
	}

	void initializeSelectingArea(Bin p1) {
		getPlot().initializeSelectingArea(p1);
	}

	void displayGate(Gate gate) {
		getPlot().displayGate(gate);
	}

	void displaySetGate(GateSetMode mode, Bin pChannel, Point pPixel) {
		getPlot().displaySetGate(mode, pChannel, pPixel);
	}

	void setRenderForPrinting(boolean rfp, PageFormat pf) {
		getPlot().setRenderForPrinting(rfp, pf);
	}

	ComponentPrintable getComponentPrintable(int run, String d) {
		return getPlot().getComponentPrintable(run, d);
	}

	int getSizeX() {
		return getPlot().getSizeX();
	}

	int getSizeY() {
		return getPlot().getSizeY();
	}

	/**
	 * Add a mouse listener.
	 * 
	 * @param listener the listener to add
	 */
	void addPlotMouseListener(PlotMouseListener listener) {
		plot1d.addPlotMouseListener(listener);
		plot2d.addPlotMouseListener(listener);
	}

	/* Mouse methods */
	
	/* non-javadoc:
	 * Remove a mouse listener.
	 */
	void removePlotMouseListener(PlotMouseListener listener) {
		plot1d.removePlotMouseListener(listener);
		plot2d.removePlotMouseListener(listener);
	}

	void removeAllPlotMouseListeners() {
		plot1d.removeAllPlotMouseListeners();
		plot2d.removeAllPlotMouseListeners();
	}

	/**
	 * @see PlotSelectListener#plotSelected(Object)
	 */
	public void plotSelected(Object source) {
		plotSelectListener.plotSelected(this);
	}

	//End Mouse methods
	void reset() {
		plot1d.reset();
		plot2d.reset();
	}

	/* Preferences */
	
	void setSensitivity(double val) {
		//FIXME KBS
		//sensitivity = val;
	}

	void setWidth(double val) {
		//FIXME KBS
		//width = val;
	}

	void setPeakFindDisplayCal(boolean which) {
		//FIXME KBS
		//pfcal = which;
	}
	
	void repaint(){
	    panel.repaint();
	}
	
	Component getComponent(){
	    return panel;
	}
}