package jam.plot;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.ComponentPrintable;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * class for displayed plots.
 * 
 * @version 0.5
 * @see jam.plot.Plot1d
 * @see jam.plot.Plot2d
 * @since JDK 1.1
 * @author Ken Swartz
 */
public class Plot extends JPanel implements PlotPrefs, PlotSelectListener,
		PreferenceChangeListener {

	//Layout with axis labels without border
	static final int LAYOUT_TYPE_LABELS = 0;
	//Layout with axis labels and border 
	static final int LAYOUT_TYPE_LABELS_BORDER = 1;
	//Layout not axis labels without border
	static final int LAYOUT_TYPE_NO_LABELS = 2;
//	Layout not axis labels but with border
	static final int LAYOUT_TYPE_NO_LABELS_BORDER = 3;
	/**
	 * Zoom direction.
	 * 
	 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
	 */
	public static final class Zoom {
		private static final int in = 1;

		private static final int out = 2;

		/**
		 * Zoom in.
		 */
		public static final Zoom IN = new Zoom(in);

		/**
		 * Zoom out.
		 */
		public static final Zoom OUT = new Zoom(out);

		private final int direction;

		private Zoom(int i) {
			direction = i;
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

	private boolean isSelected;
	
	private boolean isScrolling;
	
	private boolean hasHistogram;

	private final PlotSelectListener plotSelectListener;

	private final List overlays = Collections.synchronizedList(new ArrayList());

	private int layoutType, plotNumber;

	private AbstractPlot currentSubPlot;
	
	private int currentHistNumber;

	public Plot(PlotGraphicsLayout graphicsLayout,
			PlotSelectListener plotSelect) {
		this.plotSelectListener = plotSelect;
		isSelected = false;
		/*
		 * panel containing plots panel to holds 1d and 2d plots, and swaps them
		 */
		plotSwapPanelLayout = new CardLayout();
		setLayout(plotSwapPanelLayout);
		/* panel 1d plot and its scroll bars */
		plot1d = new Plot1d();
		plot1d.setOverlayList(Collections.unmodifiableList(overlays));
		scroller1d = new Scroller(plot1d);
		add(KEY1, scroller1d);
		plot1d.addPlotSelectListener(this);
		/* panel 2d plot and its scroll bars */
		plot2d = new Plot2d();
		scroller2d = new Scroller(plot2d);
		add(KEY2, scroller2d);
		plot2d.addPlotSelectListener(this);
		/* Initial show plot1d */
		plotSwapPanelLayout.show(this, KEY1);
		currentSubPlot = plot1d;
		//FIXME KBS tempory test
		//enableScrolling(false);
		//Enable prefernce
		prefs.addPreferenceChangeListener(this);
	}

	/**
	 * Select this plot as the current plot
	 * 
	 * @param selectedState
	 */
	void select(boolean selectedState) {
		if ((layoutType == LAYOUT_TYPE_LABELS_BORDER)||
			(layoutType == LAYOUT_TYPE_NO_LABELS_BORDER)) {
			if (selectedState) {
				setBorder(new LineBorder(Color.BLACK, 2));
			} else {
				setBorder(new EmptyBorder(2, 2, 2, 2));
			}			
		} else {
			setBorder(null);
		}
	}

	/**
	 * Set the layout to include or not include the margins
	 * 
	 * @param type
	 */
	void setLayoutType(int type) {
		layoutType = type;
		//Plot labels
		if ((type == LAYOUT_TYPE_LABELS_BORDER)||
			(type == LAYOUT_TYPE_LABELS))	{
			plot1d.setLayout(PlotGraphicsLayout.LAYOUT_TYPE_LABELS);
			plot2d.setLayout(PlotGraphicsLayout.LAYOUT_TYPE_LABELS);
		} else if ((type == LAYOUT_TYPE_NO_LABELS_BORDER)||
				  (type == LAYOUT_TYPE_NO_LABELS)) {
			plot1d.setLayout(PlotGraphicsLayout.LAYOUT_TYPE_NO_LABELS);
			plot2d.setLayout(PlotGraphicsLayout.LAYOUT_TYPE_NO_LABELS);

		}
		//Plot border
		if ((type == LAYOUT_TYPE_LABELS_BORDER)||
			(type == LAYOUT_TYPE_NO_LABELS_BORDER))	{
			setBorder(new EmptyBorder(2, 2, 2, 2));
		} else {
			setBorder(null);
		}
	}

	/**
	 * Display a histogram,
	 * 
	 * @param hist
	 *            histogram to display
	 */
	void displayHistogram(Histogram hist) {
		String key;
		select(true);
		if (hist!=null){
			hasHistogram=true;
			final int dim = hist.getDimensionality();
			if (dim == 1) {
				currentSubPlot = plot1d;
				key = KEY1;
			} else {
				currentSubPlot = plot2d;
				key = KEY2;
			}
			plotSwapPanelLayout.show(this, key);
			currentHistNumber = hist.getNumber();					
		}else {
			//Histogram is null set to blank 1D
			currentSubPlot = plot1d;
			hasHistogram=false;
			plotSwapPanelLayout.show(this, KEY1);
			currentHistNumber=-1;
		}
		currentSubPlot.displayHistogram(hist);	
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
	 * @return
	 */
	Histogram getHistogram() {
		return getPlot().getHistogram();
	}
	/**
	 * Returns whether this plot has a valid histogram.
	 * 
	 * @return <code>true</code> if this plot contains a histogram
	 */
	protected boolean HasHistogram(){
		return hasHistogram;
	}

	/**
	 * Type of plot 1D or 2D
	 * 
	 * @return plot type
	 */
	public int getDimensionality() {
		return (getPlot() == plot1d) ? 1 : 2;
	}
	
	void enableScrolling(boolean enable){
		isScrolling=enable;
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

	public AbstractPlot getPlot() {
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
	 */
	public double getEnergy(double channel) {
		return getPlot().getEnergy(channel);
	}

	/**
	 * Get the counts in a bin
	 */
	protected double getCount(jam.plot.Bin p) {
		return getPlot().getCount(p);
	}

	int getChannel(double energy) {
		return getPlot().getChannel(energy);
	}

	protected Object getCounts() {
		return getPlot().getCounts();
	}

	protected int findMinimumCounts() {
		return getPlot().findMinimumCounts();
	}

	protected int findMaximumCounts() {
		return getPlot().findMaximumCounts();
	}

	void zoom(Zoom inOut) {
		getPlot().zoom(inOut);
	}

	public void markArea(Bin p1, Bin p2) {
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

	void setSelectingArea(boolean tf) {
		getPlot().setSelectingArea(tf);
	}

	void setMarkingChannels(boolean mc) {
		getPlot().setMarkingChannels(mc);
	}

	protected final void initializeSelectingArea(Bin p1) {
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

	//Paint methods
	/**
	 * 
	 * @param g
	 */
	protected void paintHistogram(Graphics g) {
		getPlot().paintHistogram(g);
	}

	protected void paintSelectingArea(Graphics g) {
		getPlot().paintSelectingArea(g);
	}

	protected void paintMarkedChannels(Graphics g) {
		getPlot().paintMarkedChannels(g);
	}

	protected void paintOverlay(Graphics g) {
		getPlot().paintOverlay(g);
	}

	protected void paintFit(Graphics g) {
		getPlot().paintFit(g);
	}

	protected void paintGate(Graphics g) {
		getPlot().paintGate(g);
	}

	protected void paintSettingGate(Graphics g) {
		getPlot().paintSettingGate(g);
	}

	protected void paintSetGatePoints(Graphics g) {
		getPlot().paintSetGatePoints(g);
	}

	public void paintMarkArea(Graphics g) {
		getPlot().paintMarkArea(g);
	}

	//End Paint methods

	/**
	 * Add a mouse listener.
	 */
	void addPlotMouseListener(PlotMouseListener listener) {
		plot1d.addPlotMouseListener(listener);
		plot2d.addPlotMouseListener(listener);
	}

	//Mouse methods
	/**
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

	protected void mouseMoved(MouseEvent me) {
		getPlot().mouseMoved(me);
	}

	/**
	 * Forward callback
	 */
	public void plotSelected(Object source) {
		plotSelectListener.plotSelected(this);
	}

	//End Mouse methods
	void reset() {
		plot1d.reset();
		plot2d.reset();
	}

	//Preferences
	/**
	 * are we ignoring channel zero on auto scale
	 */
	boolean getIgnoreChZero() {
		return getPlot().getIgnoreChZero();
	}

	boolean getIgnoreChFull() {
		return getPlot().getIgnoreChFull();
	}

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

	public void preferenceChange(PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		final String newValue = pce.getNewValue();
	}

}