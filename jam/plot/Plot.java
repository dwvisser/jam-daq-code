package jam.plot;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.ComponentPrintable;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
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
abstract class Plot extends JPanel implements PlotPrefs, 
PreferenceChangeListener {

	/**
	 * Specifies Zoom direction, zoom out
	 */
	public final static int ZOOM_OUT = 1; //zoom mode, in or out

	/**
	 * Specifies Zoom direction, zoom in
	 */
	public final static int ZOOM_IN = 2;

	/**
	 * Specifies how much to zoom,
	 * zoom is 1/ZOOM_FACTOR
	 */
	public final static int ZOOM_FACTOR = 10;

	/**
	 * Type of histogram being plotted this is 1 dimensional int array
	 */
	public final static int ONE_DIM_INT = 1;
	/**
	 * Type of histogram being plotted this is 1 dimensional double array
	 */
	public final static int ONE_DIM_DOUBLE = 2;
	/**
	 *Type of histogram being plotted this is 2 dimensional int array
	 */
	public final static int TWO_DIM_INT = 3;
	/**
	 * Type of histogram being plotted this is 2 dimensional double array
	 */
	public final static int TWO_DIM_DOUBLE = 4;

	static final int FULL_SCALE_MIN = 5; //minumum that Counts can be set to
	static final int FULL_SCALE_MAX = 1000000;
	//maximum that counts can be set to

	//constant strings

	static final String X_LABEL_1D = "Channels";
	static final String Y_LABEL_1D = "Counts";
	static final String X_LABEL_2D = "Channels";
	static final String Y_LABEL_2D = "Channels";

	//scroll bars
	protected Scroller scrollbars;
	//graphics class that draws a histogram
	protected PlotGraphics graph;
	//gives channes of mouse click
	protected PlotMouse plotMouse;
	//limits for plot
	protected Limits plotLimits;
	protected PageFormat pageformat = null;

	// histogram related stuff.
	protected Histogram currentHist; //the currently displayed histogram
	protected int sizeX;
	protected int sizeY;
	protected int type;
	protected int number;
	protected String title;
	protected String axisLabelX;
	protected String axisLabelY;
	protected Limits.ScaleType scale;
	protected boolean isCalibrated;
	protected double[] counts;
	protected double[][] counts2d;


	//gate stuff
	protected Gate currentGate;
	
	/** gate points in plot coordinates (channels) */
	protected final Polygon pointsGate = new Polygon();
	boolean settingGate = false;

	/** selection start point in plot coordinates */
	protected Point selectionStartPoint= new Point();
	
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

	//configuration for screen plotting
	protected Dimension viewSize;
	
	protected final List markedChannels=new ArrayList();

	// configuration for page plotting are set using printHistogram
	protected Dimension pageSize;
	protected int pagedpi;

	protected Font screenFont;
	protected Font printFont;

	//color mode for screen, one of PlotColorMap options
	protected int colorMode;

	private int runNumber;
	private String date;
	
	protected double binWidth=1.0;
	
	/**
	 * Dont use 0 ch for auto scale
	 */
	protected boolean ignoreChZero;
	/**
	 * Dont use full scale ch for auto scale
	 */
	protected boolean ignoreChFull;

	Action action;

	protected boolean printing = false;
	
	protected final Rectangle selectingAreaClip=new Rectangle();

	/** last point mouse moved to, uses plot coordinates when selecting
	 * an area, and uses graphics coordinates when setting a gate (FIX?) */
	protected final Point lastMovePoint = new Point();	

	/** clip to use when repainting for mouse movement, in graphics coordinates */
	protected final Polygon mouseMoveClip = new Polygon();
	protected boolean mouseMoved = false;

	/**
	 * Constructor
	 */
	protected Plot(Action a) {
		super(false);
		final String fontclass = "Serif";
		action = a;
		setOpaque(true);
		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		//some initial layout stuff
		Insets viewBorder =
			new Insets(
				PlotGraphics.BORDER_TOP,
				PlotGraphics.BORDER_LEFT,
				PlotGraphics.BORDER_BOTTOM,
				PlotGraphics.BORDER_RIGHT);
		screenFont =
			new Font(
				fontclass,
				Font.BOLD,
				(int) PlotGraphicsLayout.SCREEN_FONT_SIZE);
		printFont =
			new Font(fontclass, Font.PLAIN, PlotGraphicsLayout.PRINT_FONT_SIZE);
		graph = new PlotGraphics(this, viewBorder, screenFont);
		plotMouse = new PlotMouse(graph, action);
		addMouseListener(plotMouse);
		initPrefs();
		prefs.addPreferenceChangeListener(this);
	}
	
	private final void initPrefs(){
		setIgnoreChFull(prefs.getBoolean(AUTO_IGNORE_FULL,true));
		setIgnoreChZero(prefs.getBoolean(AUTO_IGNORE_ZERO,true));
		setColorMode(prefs.getBoolean(BLACK_BACKGROUND,false));
	}

	public void preferenceChange(PreferenceChangeEvent pce){
		final String key=pce.getKey();
		final String newValue=pce.getNewValue();
		if (key.equals(PlotPrefs.AUTO_IGNORE_ZERO)){
			setIgnoreChZero(Boolean.valueOf(newValue).booleanValue());
			if (plotDataExists()){
				autoCounts();
			}
		} else if (key.equals(PlotPrefs.AUTO_IGNORE_FULL)){
			setIgnoreChFull(Boolean.valueOf(newValue).booleanValue());
			if (plotDataExists()){
				autoCounts();
			}
		} else if (key.equals(PlotPrefs.BLACK_BACKGROUND)){
			setColorMode(Boolean.valueOf(newValue).booleanValue());
		}
	}
	
	private final boolean plotDataExists(){
		return currentHist != null && currentHist.getCounts() != null;
	}
	
	/**
	 * add scrollbars
	 */
	void addScrollBars(Scroller scrollbars) {
		this.scrollbars = scrollbars;
	}

	/**
	 * Set current histogram, doing nothing else--this is a hack to let scroll bars work.
	 */
	private synchronized void setHistogram(Histogram hist) {
		currentHist = hist;
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
	 * Set the histogram to plot. If the plot limits are null, make one
	 * save all neccessary histogram parameters to local variables.
	 * Allows general use of data set.
	 */
	void displayHistogram(Histogram hist) {
		setHistogram(hist);
		if (hist != null) {
			plotLimits = Limits.getLimits(hist);
			number = hist.getNumber();
			title = hist.getTitle();
			axisLabelX = hist.getLabelX();
			axisLabelY = hist.getLabelY();
			isCalibrated = hist.isCalibrated();
			if (plotLimits == null) {
				JOptionPane.showMessageDialog(null,
				"Tried to plot histogram with null Limits.",
				getClass().getName(),JOptionPane.ERROR_MESSAGE);
			}
			if (hist.getType() == Histogram.ONE_DIM_INT) {
				type = ONE_DIM_INT;
				sizeX = hist.getSizeX();
				sizeY = 0;
				counts = new double[hist.getSizeX()];
			} else if (hist.getType() == Histogram.ONE_DIM_DOUBLE) {
				type = ONE_DIM_DOUBLE;
				sizeX = hist.getSizeX();
				sizeY = 0;
				counts = new double[hist.getSizeX()];
			} else if (hist.getType() == Histogram.TWO_DIM_INT) {
				type = TWO_DIM_INT;
				sizeX = hist.getSizeX();
				sizeY = hist.getSizeY();
				counts2d = new double[hist.getSizeX()][hist.getSizeY()];
			} else if (hist.getType() == Histogram.TWO_DIM_DOUBLE) {
				type = TWO_DIM_DOUBLE;
				sizeX = hist.getSizeX();
				sizeY = hist.getSizeY();
				counts2d = new double[hist.getSizeX()][hist.getSizeY()];
			}
			copyCounts(); //copy hist counts
			scrollbars.setLimits(plotLimits);
			//Limits contains handle to Models
		} else { //we have a null histogram so fake it
			counts = new double[100];
			type = ONE_DIM_INT;
			title = "No Histogram";
			sizeX = 100;
			counts2d = null;
		}
		displayingGate = false;
		displayingOverlay = false;
		displayingFit = false;
	}

	/**
	 * Show the setting of a gate
	 * mode are we starting a new gate or continue
	 * or saving on
	 */
	abstract void displaySetGate(
		GateSetMode mode,
		Point pChannel,
		Point pPixel);

	/**
	 * Copies the counts into the local array--needed by scroller.
	 */
	final void copyCounts() {
		if (type == ONE_DIM_INT) {
			int [] temp=(int [])currentHist.getCounts();
			/* NOT System.arraycopy() because of array type difference 
			 */
			for (int i =0; i<temp.length; i++){
				counts[i]=temp[i];
			}
		} else if (type == ONE_DIM_DOUBLE) {
			System.arraycopy((double[]) currentHist.getCounts(), 0, counts, 0,
			currentHist.getSizeX());
		} else if (type == TWO_DIM_INT) {
			int [][] counts2dInt = (int[][]) currentHist.getCounts();
			for (int i = 0; i < currentHist.getSizeX(); i++) {
				for (int j = 0; j < currentHist.getSizeY(); j++) {
					counts2d[i][j] = counts2dInt[i][j];
				}
			}
		} else if (type == TWO_DIM_DOUBLE) {
			double [][] counts2dDble = (double[][]) currentHist.getCounts();
			for (int i = 0; i < currentHist.getSizeX(); i++) {
				System.arraycopy(
					counts2dDble[i],
					0,
					counts2d[i],
					0,
					currentHist.getSizeY());
			}
		}
	}

	/**
	 * get the histogram the is ploted
	 */
	synchronized Histogram getHistogram() {
		return currentHist;
	}

	/**
	 * get plot Limits method
	 * limits are how the histogram is to be drawn
	 */
	Limits getLimits() {
		return plotLimits;
	}

	/**
	 * Mark a channel on the plot.
	 *
	 * @param p graphics coordinates on the plot where the channel is
	 */
	final void markChannel(Point p) {
		markingChannels=true;
		markedChannels.add(p);
		repaint();
	}
	
	synchronized void setMarkingChannels(boolean mc){
		markingChannels=mc;
		if (!markingChannels){
			markedChannels.clear();
		}
	}

	/**
	 * Start marking an area.
	 * 
	 * @param p1 starting point in plot coordinates
	 */
	protected final void initializeSelectingArea(Point p1) {
		setSelectingArea(true);
		selectionStartPoint.setLocation(p1);
		setLastMovePoint(p1);
	} 
	
	protected final void setLastMovePoint(Point p) {
		synchronized (lastMovePoint) {
			lastMovePoint.setLocation(p);
		}
	}

	synchronized void setSelectingArea(boolean tf){
		selectingArea=tf;
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
	 * @param p1 a corner of the rectangle in plot coordinates
	 * @param p2 a corner of the rectangle in plot coordinates
	 */
	abstract void markArea(Point p1, Point p2);
	
	synchronized void setMarkArea(boolean tf){
		markArea=tf;
	}

	/**
	 *Expand the region viewed.
	 */
	void expand(Point p1, Point p2) {
		int xll; // x lower limit
		int xul; // x upper limit
		int yll; // y lower limit
		int yul; // y upper limit

		if (p1.x <= p2.x) {
			xll = p1.x;
			xul = p2.x;
		} else {
			xll = p2.x;
			xul = p1.x;
		}
		// check for beyond extremes and set to extremes
		if ((xll < 0) || (xll > sizeX - 1)) {
			xll = 0;
		}
		if ((xul < 0) || (xul > sizeX - 1)) {
			xul = sizeX - 1;
		}
		if (p1.y <= p2.y) {
			yll = p1.y;
			yul = p2.y;
		} else {
			yll = p2.y;
			yul = p1.y;
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
	void zoom(int inOut) {
		int xll = plotLimits.getMinimumX();
		int xul = plotLimits.getMaximumX();
		int yll = plotLimits.getMinimumY();
		int yul = plotLimits.getMaximumY();
		final int diffX = Math.max(1,(xul - xll)/ZOOM_FACTOR);
		final int diffY = Math.max(1,(yul - yll)/ZOOM_FACTOR);
		if (inOut == ZOOM_OUT) {//zoom out
			xll = xll - diffX;
			xul = xul + diffX;
			yll = yll - diffY;
			yul = yul + diffY;
		} else if (inOut == ZOOM_IN) {//zoom in
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
	 *  Autoscale the counts scale.
	 *  Set maximum scale to 110 percent of maximum number of counts in view.
	 *  Can't call refresh because we need to use the counts before refreshing.
	 */
	final void autoCounts() {
		copyCounts();
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
		copyCounts();
		repaint();
	}

	/**
	 * Updated the display, resetting so that fits, gates and 
	 * overlays are no longer shown.
	 */
	void update() {
		displayingGate = false;
		displayingFit = false;
		displayingOverlay = false;
		selectingArea = false;
		markArea = false;
		setMarkingChannels(false);
		refresh();
	}

	void setDisplayingGate(boolean dg) {
		synchronized (this) {
			displayingGate = dg;
		}
	}
	
	synchronized void setBinWidth(double x){
		binWidth=x;
	}

	/**
	 *methods for getting histogram data
	 */
	abstract double getCount(Point p);
	
	/**
	 * Find the maximum number of counts in the region of interest
	 */
	protected abstract int findMaximumCounts();
	
	/**
	 * Find the minimum number of counts in the region of interest
	 */
	protected abstract int findMinimumCounts();

	/**
	 * Routine that draws the histograms.
	 * Overrides <code>Canvas</code> method.
	 */
	protected synchronized void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (printing) { //output to printer
			graph.setFont(printFont);
			PlotColorMap.setColorMap(PlotColorMap.PRINT);
			graph.setView(pageformat);
		} else { //output to screen
			graph.setFont(screenFont);
			PlotColorMap.setColorMap(colorMode);
			graph.setView(null);
		}
		g.setColor(PlotColorMap.foreground); //color foreground
		this.setForeground(PlotColorMap.foreground);
		this.setBackground(PlotColorMap.background);
		viewSize = getSize();
		graph.update(g, viewSize, plotLimits);
		/* give graph all pertinent info, draw outline, tickmarks, 
		 * labels, and title
		 */
		if (this.currentHist != null) {
			paintHeader(g);
			if (binWidth > currentHist.getSizeX()){
				binWidth=1.0;
				warning(
				"Bin width > hist size, so setting bin width back to 1.");
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
			if (settingGate){
				paintSetGatePoints(g);
			}
			if (markingChannels){
				paintMarkedChannels(g);
			}
			if (mouseMoved) {
				/* we handle selecting area or setting gate here */
				paintMouseMoved(g);
			}
		}
	}

	void error(final String mess) {
		Runnable task=new Runnable(){
			public void run(){
				final String plotErrorTitle = "Plot Error";
				JOptionPane.showMessageDialog(
					Plot.this,
					mess,
					plotErrorTitle,
					JOptionPane.ERROR_MESSAGE);
			}
		};
		SwingUtilities.invokeLater(task);
	}
	
	void warning(final String mess){
		Runnable task=new Runnable(){
			public void run(){
		final String plotErrorTitle = "Plot Warning";
		JOptionPane.showMessageDialog(
			Plot.this,
			mess,
			plotErrorTitle,
			JOptionPane.WARNING_MESSAGE);
			}
		};
		SwingUtilities.invokeLater(task);
	}
	
	/**
	 * Displays a gate on the plot.
	 *
	 * @param gate  the gate to be displayed
	 * @throws DataException thrown if there is an unrecoverable errer accessing the <code>Gate</code>
	 */
	void displayGate(Gate gate) {
		if (currentHist != null && currentHist.hasGate(gate)) {
			setDisplayingGate(true);
			setCurrentGate(gate);
			repaint();
		} else {
			error(
				"Can't display '"
					+ gate
					+ "' on histogram '"
					+ currentHist
					+ "'.");
		}
	}

	private void setCurrentGate(Gate g) {
		synchronized (this) {
			currentGate = g;
		}
	}	

	/**
	 * Paints header for plot to screen and printer.
	 * Also sets colors and the size in pixels for a plot.
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
	 * @param g the graphics context
	 */
	abstract protected void paintSelectingArea(Graphics g);

	/**
	 * Method for painting a clicked area.
	 * 
	 * @param g the graphics context
	 */
	abstract protected void paintMarkArea(Graphics g);
	
	/**
	 * Method for painting a clicked channel.
	 * 
	 * @param g the graphics context
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
	 * @param g the graphics context
	 */
	abstract protected void paintSettingGate(Graphics g);

	/**
	 * Method for painting segments while setting a gate.
	 * 
	 * @param g the graphics context
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
			colorMode = cm ? PlotColorMap.WHITE_ON_BLACK : 
			PlotColorMap.BLACK_ON_WHITE;
		}
		setBackground(PlotColorMap.background);
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
	 * Get the plot graphics for this plot
	 * need for plot mouse
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
	 * @param limC1 first limit for counts, upper or lower
	 * @param limC2 second limit for counts
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
	 * get histogram x size
	 *  need by scroller
	 */
	int getSizeX() {
		return sizeX;
	}

	/**
	 * get histogram y size
	 * needed by scroller
	 */
	int getSizeY() {
		return sizeY;
	}

	/**
	 * Not used.
	 * 
	 * @param me created when the mouse is moved
	 */
	abstract protected void mouseMoved(MouseEvent me);
	
	protected final MouseInputAdapter mouseInputAdapter=new MouseInputAdapter(){
		/**
		 * Undo last temporary line drawn.
		 * 
		 * @param e created when mouse exits the plot
		 */	
		public void mouseExited(MouseEvent e) {
			setMouseMoved(false);
			repaint();
		}
		
		public void mouseMoved(MouseEvent e) {
			Plot.this.mouseMoved(e);
		}
	};
	
	protected final boolean isSelectingAreaClipClear(){
		synchronized(selectingAreaClip){
			return selectingAreaClip.height==0;
		}
	}
	
	protected final void clearSelectingAreaClip(){
		synchronized (selectingAreaClip){
			selectingAreaClip.setSize(0,0);	
		}
	}
}
