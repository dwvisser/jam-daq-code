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
import javax.swing.border.Border;
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

	//LayoutType full plot
	static final int LAYOUT_TYPE_FULL = 0;

	//LayoutType tiled plots
	static final int LAYOUT_TYPE_TILED = 1;

	/**
	 * Specifies Zoom direction, zoom out
	 */
	public final static int ZOOM_OUT = 1; //zoom mode, in or out

	/**
	 * Specifies Zoom direction, zoom in
	 */
	public final static int ZOOM_IN = 2;

	public static int TYPE_1D = 1;

	public static int TYPE_2D = 2;

	private static final String KEY1 = "1D Plot";

	private static final String KEY2 = "2D Plot";

	private final Object plotLock = new Object();

	private int currentHistNumber;

	private int layoutType;

	private final CardLayout plotSwapPanelLayout;

	private Border selectBorder;

	private AbstractPlot currentSubPlot;
	
	private final Plot1d plot1d;

	private final Plot2d plot2d;

	private final Scroller scroller1d;
	
	private final Scroller scroller2d;	
	
	private int plotNumber; 

	private boolean isSelected;

	private final PlotSelectListener plotSelectListener;

	private final List overlays = Collections.synchronizedList(new ArrayList());

	public Plot(Action action, PlotGraphicsLayout graphicsLayout,
			PlotSelectListener plotSelect) {
		this.plotSelectListener = plotSelect;
		isSelected = false;

		//panel containing plots panel to holds 1d and 2d plots,
		//and swaps them
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
	}

	/**
	 * Select this plot as the current plot
	 * 
	 * @param selectedState
	 */
	void select(boolean selectedState) {
		if (layoutType == LAYOUT_TYPE_FULL) {
			setBorder(null);
		} else {
			if (selectedState) {
				setBorder(new LineBorder(Color.BLACK, 2));
			} else {
				setBorder(new EmptyBorder(2, 2, 2, 2));
			}
		}
	}

	/**
	 * Set the layout to include or not include the margins
	 * 
	 * @param type
	 */
	void setLayoutType(int type) {
		layoutType = type;
		if (type == LAYOUT_TYPE_FULL) {
			plot1d.setLayout(PlotGraphicsLayout.LAYOUT_TYPE_FULL);
			plot2d.setLayout(PlotGraphicsLayout.LAYOUT_TYPE_FULL);
			this.setBorder(null);
		} else if (type == LAYOUT_TYPE_TILED) {
			plot1d.setLayout(PlotGraphicsLayout.LAYOUT_TYPE_TILED);
			plot2d.setLayout(PlotGraphicsLayout.LAYOUT_TYPE_TILED);
			this.setBorder(new EmptyBorder(0, 0, 0, 0));
		}
	}

	/**
	 * Display a histogram,
	 * 
	 * @param hist
	 *            histogram to display
	 */
	void displayHistogram(Histogram hist) {
		select(true);
		final int dim = hist.getDimensionality();
		String key;
		if (dim == 1) {
			currentSubPlot = plot1d;
			key = KEY1;
		} else {
			currentSubPlot = plot2d;
			key = KEY2;
		}
		plotSwapPanelLayout.show(this, key);
		currentSubPlot.displayHistogram(hist);
		currentHistNumber = hist.getNumber();
	}

	/**
	 * Overlay histograms
	 * 
	 * @param num
	 *            the number of the histogram
	 */
	void overlayHistograms(int num) {
		final Histogram h = Histogram.getHistogram(num);
		final Integer value = new Integer(num);
		//Don't overlay histogram already displaye
		if (currentHistNumber != num && !overlays.contains(value)) {
			overlays.add(value);
		}
		currentSubPlot.overlayHistograms(overlays);
	}

	/**
	 * Clear overlays
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
		return currentSubPlot.getHistogram();
	}

	/**
	 * Type of plot 1D or 2D
	 * 
	 * @return plot type
	 */
	public int getType() {
		int plotType;
		if (currentSubPlot == plot1d)
			plotType = TYPE_1D;
		else
			plotType = TYPE_2D;

		return plotType;
	}
	
	void enableScrolling(boolean enable){
		scroller1d.enableScrolling(enable);
		scroller2d.enableScrolling(enable);
	}
	
	int getNumber(){
		return plotNumber;
	}
	void setNumber(int numIn){
		plotNumber=numIn;
	}
	
	Limits getLimits() {
		return currentSubPlot.getLimits();
	}

	void markChannel(Bin p) {
		currentSubPlot.markChannel(p);
	}

	void update() {
		currentSubPlot.update();
	}

	public AbstractPlot getPlot() {
		synchronized (plotLock) {
			return currentSubPlot;
		}
	}

	void displayFit(double[][] signals, double[] background,
			double[] residuals, int ll) {
		currentSubPlot.displayFit(signals, background, residuals, ll);
	}

	/**
	 * Caller should have checked 'isCalibrated' first.
	 */
	public double getEnergy(double channel) {
		return currentSubPlot.getEnergy(channel);
	}

	/**
	 * Get the counts in a bin
	 */
	protected double getCount(jam.plot.Bin p) {
		return currentSubPlot.getCount(p);
	}

	int getChannel(double energy) {
		return currentSubPlot.getChannel(energy);
	}

	protected Object getCounts() {
		return currentSubPlot.getCounts();
	}

	protected int findMinimumCounts() {
		return currentSubPlot.findMinimumCounts();
	}

	protected int findMaximumCounts() {
		return currentSubPlot.findMaximumCounts();
	}

	void zoom(int inOut) {
		currentSubPlot.zoom(inOut);
	}

	public void markArea(Bin p1, Bin p2) {
		currentSubPlot.markArea(p1, p2);
	}

	void setMarkArea(boolean tf) {
		currentSubPlot.setMarkArea(tf);
	}

	void autoCounts() {
		currentSubPlot.autoCounts();
	}

	void setRange(int limC1, int limC2) {
		currentSubPlot.setRange(limC1, limC2);
	}

	/**
	 * Expand a region
	 * 
	 * @param c1
	 * @param c2
	 */
	void expand(jam.plot.Bin c1, jam.plot.Bin c2) {
		currentSubPlot.expand(c1, c2);
	}

	/**
	 * Show full plot
	 */
	void setFull() {
		currentSubPlot.setFull();
	}

	/**
	 * Set the scale as linear
	 */
	void setLinear() {
		currentSubPlot.setLinear();
	}

	/**
	 * Set the scale as log
	 */
	void setLog() {
		currentSubPlot.setLog();
	}

	void setBinWidth(double x) {
		currentSubPlot.setBinWidth(x);
	}

	void setSelectingArea(boolean tf) {
		currentSubPlot.setSelectingArea(tf);
	}

	void setMarkingChannels(boolean mc) {
		currentSubPlot.setMarkingChannels(mc);
	}

	protected final void initializeSelectingArea(Bin p1) {
		currentSubPlot.initializeSelectingArea(p1);
	}

	void displayGate(Gate gate) {
		currentSubPlot.displayGate(gate);
	}

	void displaySetGate(GateSetMode mode, Bin pChannel, Point pPixel) {
		currentSubPlot.displaySetGate(mode, pChannel, pPixel);
	}

	void setRenderForPrinting(boolean rfp, PageFormat pf) {
		currentSubPlot.setRenderForPrinting(rfp, pf);
	}

	ComponentPrintable getComponentPrintable(int run, String d) {
		return currentSubPlot.getComponentPrintable(run, d);
	}

	int getSizeX() {
		return currentSubPlot.getSizeX();
	}

	int getSizeY() {
		return currentSubPlot.getSizeY();
	}

	//Paint methods
	/**
	 * 
	 * @param g
	 */
	protected void paintHistogram(Graphics g) {
		currentSubPlot.paintHistogram(g);
	}

	protected void paintSelectingArea(Graphics g) {
		currentSubPlot.paintSelectingArea(g);
	}

	protected void paintMarkedChannels(Graphics g) {
		currentSubPlot.paintMarkedChannels(g);
	}

	protected void paintOverlay(Graphics g) {
		currentSubPlot.paintOverlay(g);
	}

	protected void paintFit(Graphics g) {
		currentSubPlot.paintFit(g);
	}

	protected void paintGate(Graphics g) {
		currentSubPlot.paintGate(g);
	}

	protected void paintSettingGate(Graphics g) {
		currentSubPlot.paintSettingGate(g);
	}

	protected void paintSetGatePoints(Graphics g) {
		currentSubPlot.paintSetGatePoints(g);
	}

	public void paintMarkArea(Graphics g) {
		currentSubPlot.paintMarkArea(g);
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
		currentSubPlot.mouseMoved(me);
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
		return currentSubPlot.getIgnoreChZero();
	}

	boolean getIgnoreChFull() {
		return currentSubPlot.getIgnoreChFull();
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

		if (key.equals(PlotPrefs.ENABLE_SCROLLING)){
			enableScrolling(Boolean.valueOf(newValue).booleanValue());
		}		
		
		currentSubPlot.preferenceChange(pce);
	}

}