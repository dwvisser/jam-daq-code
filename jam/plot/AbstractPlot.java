package jam.plot;

import jam.data.DataException;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.ComponentPrintable;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

/**
 * Abstract class for displayed plots.
 * 
 * @version 0.5
 * @see jam.plot.Plot1d
 * @see jam.plot.Plot2d
 * @since JDK 1.1
 * @author Ken Swartz
 */
abstract class AbstractPlot extends JPanel implements PlotPrefs,
		PreferenceChangeListener {

	/**
	 * Specifies how much to zoom, zoom is 1/ZOOM_FACTOR
	 */
	public final static int ZOOM_FACTOR = 10;

	static final int FULL_SCALE_MIN = 5; //minumum that Counts can be set to

	/** Maximum that counts can be set to. */
	static final int FULL_SCALE_MAX = 1000000;

	static final String X_LABEL_1D = "Channels";

	static final String Y_LABEL_1D = "Counts";

	static final String X_LABEL_2D = "Channels";

	static final String Y_LABEL_2D = "Channels"; 

	protected static final String NO_HIST_TITLE = "No Histogram";
	/** Number of Histogram to plot */
	protected int plotHistNumber;
	
	protected Scroller scrollbars;

	protected PlotGraphics graph;

	/* Gives channels of mouse click. */
	protected PlotMouse plotMouse;

	protected Limits plotLimits;

	protected PageFormat pageformat = null;

	/* Histogram related stuff. */

	protected int sizeX;

	protected int sizeY;

	protected Histogram.Type type;

	protected Limits.ScaleType scale;

	protected double[] counts;

	protected double[][] counts2d;
	
	protected boolean hasHistogram;

	/* Gate stuff. */

	protected Gate currentGate;

	/** Gate points in plot coordinates (channels). */
	protected final Polygon pointsGate = new Polygon();

	boolean settingGate = false;

	/** selection start point in plot coordinates */
	protected Bin selectionStartPoint = Bin.Factory.create();

	/** currently displaying a gate? */
	protected boolean displayingGate = false;

	/** currently displaying a fit? */
	protected boolean displayingFit = false;

	/** currently displaying an overlay? */
	protected boolean displayingOverlay = false;

	/** currently selecting an area? */
	protected boolean selectingArea = false;

	/** currently have an area already marked? */
	protected boolean markArea = false;

	/** currently have individual channels already marked? */
	protected boolean markingChannels = false;

	protected GateSetMode gateSetMode = GateSetMode.GATE_CANCEL;

	/* configuration for screen plotting */
	protected Dimension viewSize;

	protected final List markedChannels = new ArrayList();

	/* configuration for page plotting are set using printHistogram */
	protected Dimension pageSize;

	protected int pagedpi;

	//FIXME don't handle change of fonts yet
	//protected Font screenFont;

	//protected Font printFont;

	/* Color mode for screen, one of PlotColorMap options. */
	protected int colorMode;

	private int runNumber;

	private String date;

	protected double binWidth = 1.0;

	private boolean noFillMode;

	/**
	 * Dont use 0 ch for auto scale
	 */
	protected boolean ignoreChZero;

	/**
	 * Dont use full scale ch for auto scale
	 */
	protected boolean ignoreChFull;

	protected boolean printing = false;

	protected final Rectangle selectingAreaClip = new Rectangle();

	/**
	 * last point mouse moved to, uses plot coordinates when selecting an area,
	 * and uses graphics coordinates when setting a gate (FIX?)
	 */
	protected final Point lastMovePoint = new Point();

	/** clip to use when repainting for mouse movement, in graphics coordinates */
	protected final Polygon mouseMoveClip = new Polygon();

	protected boolean mouseMoved = false;

	protected double sensitivity = 3;

	protected double width = 12;

	protected boolean pfcal = true;

	/**
	 * Constructor
	 */
	protected AbstractPlot() {
		super(false);			

		setOpaque(true);
		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

		graph = new PlotGraphics(this);
		//Create plot mouse
		plotMouse = new PlotMouse(graph);
		addMouseListener(plotMouse);
		//Setup preferences
		initPrefs();
		prefs.addPreferenceChangeListener(this);
	}

	private final void initPrefs() {
		setIgnoreChFull(prefs.getBoolean(AUTO_IGNORE_FULL, true));
		setIgnoreChZero(prefs.getBoolean(AUTO_IGNORE_ZERO, true));
		setColorMode(prefs.getBoolean(BLACK_BACKGROUND, false));
		setNoFillMode(!prefs.getBoolean(HIGHLIGHT_GATE_CHANNELS, true));
	} 
	/**
	 * Udate layout
	 *
	 */
	void setLayout(int type) {
		graph.setLayout(type);
	}
	/**
	 * Set the histogram to plot. If the plot limits are null, make one save all
	 * neccessary histogram parameters to local variables. Allows general use of
	 * data set.
	 */
	void displayHistogram(Histogram hist) {
		plotLimits=Limits.getLimits(hist);
		if (hist != null) {
			hasHistogram=true;
			plotHistNumber=hist.getNumber();			
			copyCounts(hist); //copy hist counts
			/* Limits contains handle to Models */
			scrollbars.setLimits(plotLimits);
		} else { //we have a null histogram so fake it
			hasHistogram=false;
			counts = new double[100];
			counts2d = null;			
			type = Histogram.Type.ONE_DIM_INT;
			sizeX = 100;
			sizeY=0;
		}
		displayingGate = false;
		displayingOverlay = false;
		displayingFit = false;
	}
	
	public void preferenceChange(PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		final String newValue = pce.getNewValue();
		if (key.equals(PlotPrefs.AUTO_IGNORE_ZERO)) {
			setIgnoreChZero(Boolean.valueOf(newValue).booleanValue());
			if (plotDataExists()) {
				autoCounts();
			}
		} else if (key.equals(PlotPrefs.AUTO_IGNORE_FULL)) {
			setIgnoreChFull(Boolean.valueOf(newValue).booleanValue());
			if (plotDataExists()) {
				autoCounts();
			}
		} else if (key.equals(PlotPrefs.BLACK_BACKGROUND)) {
			setColorMode(Boolean.valueOf(newValue).booleanValue());
		} else if (key.equals(PlotPrefs.HIGHLIGHT_GATE_CHANNELS)) {
			setNoFillMode(!Boolean.valueOf(newValue).booleanValue());
		}
			
	}
	/**
	 * Plot has a valid histogram
	 * @return
	 */
	protected boolean HasHistogram(){
		return hasHistogram;
	}
	private synchronized void setNoFillMode(boolean bool) {
		noFillMode = bool;
	}

	protected synchronized boolean isNoFillMode() {
		return noFillMode;
	}

	private synchronized final boolean plotDataExists() {
		Histogram plotHist=getHistogram();
		return plotHist != null && plotHist.getCounts() != null;
	}

	/**
	 * s add scrollbars
	 */
	void addScrollBars(Scroller scrollbars) {
		this.scrollbars = scrollbars;
	}

	protected void setSettingGate(boolean sg) {
		synchronized (this) {
			settingGate = sg;
		}
	}

	protected void setMouseMoved(boolean mm) {
		synchronized (this) {
			mouseMoved = mm;
		}
	}


	/**
	 * Show the setting of a gate mode are we starting a new gate or continue or
	 * saving on
	 */
	abstract void displaySetGate(GateSetMode mode, Bin pChannel, Point pPixel);
	
	abstract void overlayHistograms(List overlayHists);
	
	/**
	 * Copies the counts into the local array--needed by scroller.
	 */
	private final void copyCounts(Histogram hist) {	
		type = hist.getType();
		sizeX = hist.getSizeX();
		sizeY = hist.getSizeY();//0 if 1-d
		if (type.getDimensionality() == 1) {
			counts = new double[sizeX];
		} else {//2-d
			counts2d = new double[sizeX][sizeY];
		}
		if (type == Histogram.Type.ONE_DIM_INT) {
			int[] temp = (int[]) hist.getCounts();
			/*
			 * NOT System.arraycopy() because of array type difference
			 */
			for (int i = 0; i < temp.length; i++) {
				counts[i] = temp[i];
			}
		} else if (type == Histogram.Type.ONE_DIM_DOUBLE) {
			System.arraycopy((double[]) hist.getCounts(), 0, counts, 0,
					hist.getSizeX());
		} else if (type == Histogram.Type.TWO_DIM_INT) {
			int[][] counts2dInt = (int[][]) hist.getCounts();
			for (int i = 0; i < hist.getSizeX(); i++) {
				for (int j = 0; j < hist.getSizeY(); j++) {
					counts2d[i][j] = counts2dInt[i][j];
				}
			}
		} else if (type == Histogram.Type.TWO_DIM_DOUBLE) {
			double[][] counts2dDble = (double[][]) hist.getCounts();
			for (int i = 0; i < hist.getSizeX(); i++) {
				System.arraycopy(counts2dDble[i], 0, counts2d[i], 0,
						hist.getSizeY());
			}
		}
	}

	/**
	 * get plot Limits method limits are how the histogram is to be drawn
	 */
	Limits getLimits() {
		return plotLimits;
	}

	/**
	 * Mark a channel on the plot.
	 * 
	 * @param p
	 *            graphics coordinates on the plot where the channel is
	 */
	final void markChannel(Bin p) {
		markingChannels = true;
		markedChannels.add(Bin.copy(p));
		repaint();
	}

	abstract int getChannel(double energy);

	synchronized void setMarkingChannels(boolean mc) {
		markingChannels = mc;
		if (!markingChannels) {
			markedChannels.clear();
		}
	}

	/**
	 * Start marking an area.
	 * 
	 * @param p1
	 *            starting point in plot coordinates
	 */
	protected final void initializeSelectingArea(Bin p1) {
		setSelectingArea(true);
		selectionStartPoint.setChannel(p1);
		setLastMovePoint(p1.getPoint());
	}

	protected final void setLastMovePoint(Point p) {
		synchronized (lastMovePoint) {
			lastMovePoint.setLocation(p);
		}
	}

	synchronized void setSelectingArea(boolean tf) {
		selectingArea = tf;
		if (selectingArea) {
			addMouseMotionListener(mouseInputAdapter);
		} else {
			removeMouseMotionListener(mouseInputAdapter);
			repaint();
		}
	}

	/**
	 * Mark an area on the plot.
	 * 
	 * @param p1
	 *            a corner of the rectangle in plot coordinates
	 * @param p2
	 *            a corner of the rectangle in plot coordinates
	 */
	abstract void markArea(Bin p1, Bin p2);

	synchronized void setMarkArea(boolean tf) {
		markArea = tf;
	}

	/**
	 * Expand the region viewed.
	 */
	void expand(jam.plot.Bin c1, jam.plot.Bin c2) {
		final int x1 = c1.getX();
		final int x2 = c2.getX();
		final int y1 = c1.getY();
		final int y2 = c2.getY();
		int xll; // x lower limit
		int xul; // x upper limit
		int yll; // y lower limit
		int yul; // y upper limit
		if (x1 <= x2) {
			xll = x1;
			xul = x2;
		} else {
			xll = x2;
			xul = x1;
		}
		// check for beyond extremes and set to extremes
		if ((xll < 0) || (xll > sizeX - 1)) {
			xll = 0;
		}
		if ((xul < 0) || (xul > sizeX - 1)) {
			xul = sizeX - 1;
		}
		if (y1 <= y2) {
			yll = y1;
			yul = y2;
		} else {
			yll = y2;
			yul = y1;
		}
		/* check for beyond extremes and set to extremes */
		if ((yll < 0) || (yll > sizeY - 1)) {
			yll = 0;
		}
		if ((yul < 0) || (yul > sizeY - 1)) {
			yul = sizeY - 1;
		}
		plotLimits.setMinimumX(xll);
		plotLimits.setMaximumX(xul);
		plotLimits.setMinimumY(yll);
		plotLimits.setMaximumY(yul);
		refresh();
	}

	/**
	 * Zoom the region viewed.
	 */
	void zoom(Plot.Zoom inOut) {
		int xll = plotLimits.getMinimumX();
		int xul = plotLimits.getMaximumX();
		int yll = plotLimits.getMinimumY();
		int yul = plotLimits.getMaximumY();
		final int diffX = Math.max(1, (xul - xll) / ZOOM_FACTOR);
		final int diffY = Math.max(1, (yul - yll) / ZOOM_FACTOR);
		if (inOut == Plot.Zoom.OUT) {//zoom out
			xll = xll - diffX;
			xul = xul + diffX;
			yll = yll - diffY;
			yul = yul + diffY;
		} else if (inOut == Plot.Zoom.IN) {//zoom in
			xll = xll + diffX;
			xul = xul - diffX;
			yll = yll + diffY;
			yul = yul - diffY;
		}
		/* check if beyond extremes, if so, set to extremes */
		if ((xll < 0) || (xll > sizeX - 1)) {
			xll = 0;
		}
		if ((xul < 0) || (xul > sizeX - 1)) {
			xul = sizeX - 1;
		}
		if (xll > xul) {
			int temp = xll;
			xll = xul - 1;
			xul = temp + 1;
		}
		if ((yll < 0) || (yll > sizeY - 1)) {
			yll = 0;
		}
		if ((yul < 0) || (yul > sizeY - 1)) {
			yul = sizeY - 1;
		}
		if (yll > yul) {
			int temp = yll;
			yll = yul - 1;
			yul = temp + 1;
		}
		plotLimits.setLimitsX(xll, xul);
		plotLimits.setLimitsY(yll, yul);
		refresh();
	}

	/**
	 * set full range X
	 */
	void setFull() {
		plotLimits.setMinimumX(0);
		plotLimits.setMaximumX(sizeX - 1);
		plotLimits.setMinimumY(0);
		plotLimits.setMaximumY(sizeY - 1);
		refresh();
	}

	/**
	 * Set the scale to linear scale
	 */
	void setLinear() {
		plotLimits.setScale(Limits.ScaleType.LINEAR);
		refresh();
	}

	/**
	 * Set the scale to log scale
	 */
	void setLog() {
		plotLimits.setScale(Limits.ScaleType.LOG);
		repaint();
	}

	/**
	 * Autoscale the counts scale. Set maximum scale to 110 percent of maximum
	 * number of counts in view. Can't call refresh because we need to use the
	 * counts before refreshing.
	 */
	final void autoCounts() {
		Histogram plotHist=getHistogram();
		copyCounts(plotHist);
		plotLimits.setMinimumCounts(110 * findMinimumCounts() / 100);
		if (findMaximumCounts() > 5) {
			plotLimits.setMaximumCounts(110 * findMaximumCounts() / 100);
		} else {
			plotLimits.setMaximumCounts(5);
		}
		/* scroll bars do not always reset on their own */
		scrollbars.update(Scroller.COUNT);
		repaint();
	}

	/**
	 * method to set Counts scale
	 */
	void setRange(int limC1, int limC2) {
		if (limC1 <= limC2) {
			plotLimits.setMinimumCounts(limC1);
			plotLimits.setMaximumCounts(limC2);
		} else {
			plotLimits.setMinimumCounts(limC2);
			plotLimits.setMaximumCounts(limC1);
		}
		refresh();
	}

	/**
	 * Refresh the display.
	 */
	void refresh() {
		if (scrollbars != null) {
			scrollbars.update(Scroller.COUNT);
			/* scroll bars do not always reset on their own */
			scrollbars.update(Scroller.ALL);
		}
		Histogram plotHist=getHistogram();
		copyCounts(plotHist);
		repaint();
	}

	/**
	 * Updated the display, resetting so that fits, gates and overlays are no
	 * longer shown.
	 */
	void update() {
		reset();
		if (getCounts()!=null)
			refresh();		
	}
	/**
	 * Reset state 
	 *
	 */
	synchronized void reset() {
		displayingGate = false;
		displayingFit = false;
		displayingOverlay = false;
		selectingArea = false;
		markArea = false;
		setMarkingChannels(false);		
	}
	
	void setDisplayingGate(boolean dg) {
		synchronized (this) {
			displayingGate = dg;
		}
	}

	synchronized void setBinWidth(double x) {
		binWidth = x;
	}

	/**
	 * Get histogram counts at the specified point, which is given in channel
	 * coordinates.
	 */
	protected abstract double getCount(jam.plot.Bin p);

	/**
	 */
	 protected abstract Object getCounts();

	 /** Get the energy for a channel
	 */
	abstract double getEnergy(double channel);

	/**
	 * Find the maximum number of counts in the region of interest
	 */
	protected abstract int findMaximumCounts();

	/**
	 * Find the minimum number of counts in the region of interest
	 */
	protected abstract int findMinimumCounts();

	/**
	 * Routine that draws the histograms. Overrides <code>Canvas</code>
	 * method.
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (printing) { //output to printer
			//FIXME KBS font not set
			//graph.setFont(printFont);
			PlotColorMap.setColorMap(PlotColorMap.PRINT);
			graph.setView(pageformat);
		} else { //output to screen
			//graph.setFont(screenFont);
			PlotColorMap.setColorMap(colorMode);
			graph.setView(null);
		}
		g.setColor(PlotColorMap.foreground); //color foreground
		this.setForeground(PlotColorMap.foreground);
		this.setBackground(PlotColorMap.background);
		viewSize = getSize();
		graph.update(g, viewSize, plotLimits);
		/*
		 * give graph all pertinent info, draw outline, tickmarks, labels, and
		 * title
		 */
		final Histogram plotHist=getHistogram();
		if (plotHist != null) {
			paintHeader(g);
			if (binWidth > plotHist.getSizeX()) {
				binWidth = 1.0;
				warning("Bin width > hist size, so setting bin width back to 1.");
			}
			paintHistogram(g);
			if (displayingGate) { //are we to display a gate
				paintGate(g);
			}
			if (displayingOverlay) {
				paintOverlay(g);
			}
			if (displayingFit) {
				paintFit(g);
			}
			if (markArea) {
				paintMarkArea(g);
			}
			if (settingGate) {
				paintSetGatePoints(g);
			}
			if (markingChannels) {
				paintMarkedChannels(g);
			}
			if (mouseMoved) {
				/* we handle selecting area or setting gate here */
				paintMouseMoved(g);
			}
		}
	}

	void error(final String mess) {
		Runnable task = new Runnable() {
			public void run() {
				final String plotErrorTitle = "Plot Error";
				JOptionPane.showMessageDialog(AbstractPlot.this, mess,
						plotErrorTitle, JOptionPane.ERROR_MESSAGE);
			}
		};
		SwingUtilities.invokeLater(task);
	}

	void warning(final String mess) {
		Runnable task = new Runnable() {
			public void run() {
				final String plotErrorTitle = "Plot Warning";
				JOptionPane.showMessageDialog(AbstractPlot.this, mess,
						plotErrorTitle, JOptionPane.WARNING_MESSAGE);
			}
		};
		SwingUtilities.invokeLater(task);
	}

	/**
	 * Displays a gate on the plot.
	 * 
	 * @param gate
	 *            the gate to be displayed
	 * @throws DataException
	 *             thrown if there is an unrecoverable errer accessing the
	 *             <code>Gate</code>
	 */
	synchronized void displayGate(Gate gate) {

		Histogram plotHist=getHistogram();
		if (plotHist != null && plotHist.hasGate(gate)) {
			setDisplayingGate(true);
			setCurrentGate(gate);
			repaint();
		} else {
			error("Can't display '" + gate + "' on histogram '" + plotHist
					+ "'.");
		}
	}

	private synchronized void setCurrentGate(Gate g) {
		currentGate = g;
	}

	abstract void displayFit(double[][] signals, double[] background,
			double[] residuals, int ll);

	/**
	 * Paints header for plot to screen and printer. Also sets colors and the
	 * size in pixels for a plot.
	 */
	protected void paintHeader(Graphics g) {
		g.setColor(PlotColorMap.foreground);
		if (printing) { //output to printer
			graph.drawDate(date); //date
			graph.drawRun(runNumber); //run number
		}
		graph.drawBorder();
	}

	/**
	 * Method for painting a area while it is being selected.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintSelectingArea(Graphics g);

	/**
	 * Method for painting a clicked area.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintMarkArea(Graphics g);

	/**
	 * Method for painting a clicked channel.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintMarkedChannels(Graphics g);

	/**
	 * method overriden for 1 and 2 d plots
	 */
	abstract protected void paintHistogram(Graphics g);

	/**
	 * method overriden for 1 and 2 d for painting fits
	 */
	abstract protected void paintGate(Graphics g);

	/**
	 * method overriden for 1 and 2 d for painting fits
	 */
	abstract protected void paintOverlay(Graphics g);

	/**
	 * method overriden for 1 and 2 d for painting fits
	 */
	abstract protected void paintFit(Graphics g);

	/**
	 * Method for painting segments while setting a gate.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintSettingGate(Graphics g);

	/**
	 * Method for painting segments while setting a gate.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintSetGatePoints(Graphics g);

	/**
	 * Paint called if mouse moved is enabled
	 */
	protected final void paintMouseMoved(Graphics gc) {
		if (settingGate) {
			paintSettingGate(gc);
		} else if (selectingArea) {
			paintSelectingArea(gc);
		}
	}

	synchronized void setRenderForPrinting(boolean rfp, PageFormat pf) {
		printing = rfp;
		pageformat = pf;
	}

	ComponentPrintable getComponentPrintable(int run, String d) {
		runNumber = run;
		date = d;
		return new ComponentPrintable(this);
	}

	/**
	 * ignore channel zero on auto scale
	 */
	private final void setIgnoreChZero(boolean state) {
		ignoreChZero = state;
	}

	/**
	 * are we ignoring channel zero on auto scale
	 */
	boolean getIgnoreChZero() {
		return ignoreChZero;
	}

	/**
	 * ignore channel full scale on auto scale
	 */
	private final void setIgnoreChFull(boolean state) {
		ignoreChFull = state;
	}

	/**
	 * are we ignoring channel full scale on auto scale
	 */
	boolean getIgnoreChFull() {
		return ignoreChFull;
	}

	/**
	 * Set the color mode, color palette
	 */
	private final void setColorMode(boolean cm) {
		synchronized (this) {
			colorMode = cm ? PlotColorMap.WHITE_ON_BLACK
					: PlotColorMap.BLACK_ON_WHITE;
		}
		setBackground(PlotColorMap.background);
	}
	//Plot mouse methods
	/**
	 * Add plot select listener
	 */
	void addPlotSelectListener(PlotSelectListener plotSelectListener) {
		plotMouse.addPlotSelectListener(plotSelectListener);
	}
	/**
	 * Add a mouse listener.
	 */
	void addPlotMouseListener(PlotMouseListener listener) {
		plotMouse.addListener(listener);
	}

	/**
	 * Remove a mouse listener.
	 */
	void removePlotMouseListener(PlotMouseListener listener) {
		plotMouse.removeListener(listener);
	}
	/**
	 * Remove all plot mouse listeners
	 *
	 */
	void removeAllPlotMouseListeners() {
		plotMouse.removeAllListeners();
	}
	//End Plot mouse methods	
	/**
	 * Get the plot graphics for this plot need for plot mouse
	 */
	PlotGraphics getPlotGraphics() {
		return graph;
	}

	/**
	 * Sets x-axis limits for scrolling.
	 */
	void setLimitsX(int limX1, int limX2) {
		if (limX1 <= limX2) {
			plotLimits.setMinimumX(limX1);
			plotLimits.setMaximumX(limX2);
		} else {
			plotLimits.setMinimumX(limX2);
			plotLimits.setMaximumX(limX1);
		}
	}

	/**
	 * Sets y-axis limits for scrolling.
	 */
	void setLimitsY(int limY1, int limY2) {
		if (limY1 <= limY2) {
			plotLimits.setMinimumY(limY1);
			plotLimits.setMaximumY(limY2);
		} else {
			plotLimits.setMinimumY(limY2);
			plotLimits.setMaximumY(limY1);
		}
	}

	/**
	 * Sets limits of counts scale.
	 * 
	 * @param limC1
	 *            first limit for counts, upper or lower
	 * @param limC2
	 *            second limit for counts
	 */
	void setLimitsCounts(int limC1, int limC2) {
		if (limC1 <= limC2) {
			plotLimits.setMinimumCounts(limC1);
			plotLimits.setMaximumCounts(limC2);
		} else {
			plotLimits.setMinimumCounts(limC2);
			plotLimits.setMaximumCounts(limC1);
		}
	}

	/**
	 * Set the maximum counts limit on the scale, but constrained for scrolling.
	 */
	void setMaximumCountsConstrained(int maxC) {
		int temp = maxC;
		/* Don't go too small. */
		if (temp < FULL_SCALE_MIN) {
			temp = FULL_SCALE_MIN;
		}
		/* Don't go too big. */
		if (temp > FULL_SCALE_MAX) {
			temp = FULL_SCALE_MAX;
		}
		plotLimits.setMaximumCounts(temp);
	}

	/**
	 * get histogram x size need by scroller
	 */
	int getSizeX() {
		return sizeX;
	}

	/**
	 * get histogram y size needed by scroller
	 */
	int getSizeY() {
		return sizeY;
	}

	Histogram getHistogram(){
		if (plotHistNumber<0)
			return null;
		else 
			return Histogram.getHistogram(plotHistNumber);
	}
		
	/**
	 * Not used.
	 * 
	 * @param me
	 *            created when the mouse is moved
	 */
	abstract protected void mouseMoved(MouseEvent me);

	protected final MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
		/**
		 * Undo last temporary line drawn.
		 * 
		 * @param e
		 *            created when mouse exits the plot
		 */
		public void mouseExited(MouseEvent e) {
			setMouseMoved(false);
			repaint();
		}

		public void mouseMoved(MouseEvent e) {
			AbstractPlot.this.mouseMoved(e);
		}
	};

	protected final boolean isSelectingAreaClipClear() {
		synchronized (selectingAreaClip) {
			return selectingAreaClip.height == 0;
		}
	}

	protected final void clearSelectingAreaClip() {
		synchronized (selectingAreaClip) {
			selectingAreaClip.setSize(0, 0);
		}
	}
}