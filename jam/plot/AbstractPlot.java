package jam.plot;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.ComponentPrintable;
import jam.plot.color.PlotColorMap;

import java.awt.Color;
import java.awt.Component;
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
abstract class AbstractPlot implements PlotPrefs,
		PreferenceChangeListener {
    
    final class PlotPanel extends JPanel {
        
        PlotPanel(){
            super(false);
        }
        
    	/**
    	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
    	 */
    	protected void paintComponent(Graphics graphics) {
    		super.paintComponent(graphics);
    		final PlotColorMap pcm=PlotColorMap.getInstance();
    		if (printing) { //output to printer
    			//FIXME KBS font not set
    			//graph.setFont(printFont);
    			pcm.setColorMap(PlotColorMap.PRINT);
    			graph.setView(pageformat);
    		} else { //output to screen
    			//graph.setFont(screenFont);
    			pcm.setColorMap(colorMode);
    			graph.setView(null);
    		}
    		final Color foreground=pcm.getForeground();
    		graphics.setColor(foreground); //color foreground
    		this.setForeground(foreground);
    		this.setBackground(pcm.getBackground());
    		viewSize = getSize();
    		graph.update(graphics, viewSize, plotLimits);
    		/*
    		 * give graph all pertinent info, draw outline, tickmarks, labels, and
    		 * title
    		 */
    		final Histogram plotHist=getHistogram();
    		if (plotHist != null) {
    			paintHeader(graphics);
    			if (binWidth > plotHist.getSizeX()) {
    				binWidth = 1.0;
    				warning("Bin width > hist size, so setting bin width back to 1.");
    			}
    			paintHistogram(graphics);
    			if (displayingGate) { //are we to display a gate
    				paintGate(graphics);
    			}
    			if (displayingOverlay) {
    				paintOverlay(graphics);
    			}
    			if (displayingFit) {
    				paintFit(graphics);
    			}
    			if (markArea) {
    				paintMarkArea(graphics);
    			}
    			if (settingGate) {
    				paintSetGatePoints(graphics);
    			}
    			if (markingChannels) {
    				paintMarkedChannels(graphics);
    			}
    			if (mouseMoved) {
    				/* we handle selecting area or setting gate here */
    				paintMouseMoved(graphics);
    			}
    		}
    	}
    	
    	/**
    	 * @return the container class instance
    	 */
    	public AbstractPlot getPlot(){
    	    return AbstractPlot.this;
    	}
    }
    
    /**
     * The actual panel.
     */
    protected final PlotPanel panel=new PlotPanel();

	/**
	 * Specifies how much to zoom, zoom is 1/ZOOM_FACTOR
	 */
	private final static int ZOOM_FACTOR = 10;

	private static final int FS_MIN = 5; //minumum that Counts can be set to

	/** Maximum that counts can be set to. */
	private static final int FS_MAX = 1000000;

	/** Number of Histogram to plot */
	private int plotHistNum;
	
	private Scroller scrollbars;

	/**
	 * Plot graphics handler.
	 */
	protected final PlotGraphics graph;

	/* Gives channels of mouse click. */
	private PlotMouse plotMouse;

	/**
	 * Descriptor of domain and range of histogram to plot.
	 */
	protected Limits plotLimits;

	private PageFormat pageformat = null;

	/* Histogram related stuff. */

	/**
	 * Number of x-channels.
	 */
	protected int sizeX;

	/**
	 * Number of y-channels.
	 */
	protected int sizeY;

	/**
	 * Descriptor of array type for histogram.
	 */
	protected Histogram.Type type;

	/**
	 * 1D counts.
	 */
	protected double[] counts;

	/**
	 * 2D counts.
	 */
	protected double[][] counts2d;
	
	private boolean hasHistogram;

	/* Gate stuff. */

	/**
	 * The currently selected gate.
	 */
	protected Gate currentGate;

	/** Gate points in plot coordinates (channels). */
	protected final Polygon pointsGate = new Polygon();

	/** Currently setting a gate. */
	protected boolean settingGate = false;

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

	/**
	 *  configuration for screen plotting 
	 */
	protected Dimension viewSize;

	/**
	 * Channels that have been marked by clicking or typing.
	 */
	protected final List markedChannels = new ArrayList();

	//TODO don't handle change of fonts yet
	//protected Font screenFont;

	//protected Font printFont;

	/* Color mode for screen, one of PlotColorMap options. */
	private int colorMode;

	private int runNumber;

	private String date;

	/**
	 * Bin width to use when plotting (1D only).
	 */
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

	private boolean printing = false;

	/**
	 * Repaint clip to use when repainting during area selection.
	 */
	protected final Rectangle selectingAreaClip = new Rectangle();

	/**
	 * last point mouse moved to, uses plot coordinates when selecting an area,
	 * and uses graphics coordinates when setting a gate (FIX?)
	 */
	protected final Point lastMovePoint = new Point();

	/** clip to use when repainting for mouse movement, in graphics coordinates */
	protected final Polygon mouseMoveClip = new Polygon();

	private boolean mouseMoved = false;

	/**
	 * Constructor
	 */
	protected AbstractPlot() {
		panel.setOpaque(true);
		panel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		graph = new PlotGraphics(this);
		//Create plot mouse
		plotMouse = new PlotMouse(graph);
		panel.addMouseListener(plotMouse);
		//Setup preferences
		initPrefs();
		PREFS.addPreferenceChangeListener(this);
	}

	private final void initPrefs() {
		setIgnoreChFull(PREFS.getBoolean(AUTO_IGNORE_FULL, true));
		setIgnoreChZero(PREFS.getBoolean(AUTO_IGNORE_ZERO, true));
		setColorMode(PREFS.getBoolean(BLACK_BACKGROUND, false));
		setNoFillMode(!PREFS.getBoolean(HIGHLIGHT_GATE_CHANNELS, true));
	} 
	
	/*
     * non-javadoc: Update layout.
     */
	void setLayout(int type) {
		graph.setLayout(type);
	}
	
	/*
     * non-javadoc: Set the histogram to plot. If the plot limits are null, make
     * one save all neccessary histogram parameters to local variables. Allows
     * general use of data set.
     */
	synchronized void displayHistogram(Histogram hist) {
		plotLimits=Limits.getLimits(hist);
		if (hist != null) {
			hasHistogram=true;
			plotHistNum=hist.getNumber();			
			copyCounts(hist); //copy hist counts
			/* Limits contains handle to Models */
			scrollbars.setLimits(plotLimits);
		} else { //we have a null histogram so fake it
			hasHistogram=false;
			plotHistNum=-1;
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
	
	/**
	 * @see PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
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
	
	/*
     * non-javadoc: Plot has a valid histogram
     *  
     */
	boolean hasHistogram(){
		return hasHistogram;
	}
	
	private synchronized void setNoFillMode(boolean bool) {
		noFillMode = bool;
	}

	/**
	 * @return if we are in the "no fill mode"
	 */
	protected final synchronized boolean isNoFillMode() {
		return noFillMode;
	}

	private synchronized final boolean plotDataExists() {
		Histogram plotHist=getHistogram();
		return plotHist != null && plotHist.getCounts() != null;
	}

	/*
	 * non-javadoc: add scrollbars
	 */
	void addScrollBars(Scroller scrollbars) {
		this.scrollbars = scrollbars;
	}

	/**
	 * Sets whether we are in the middle of defining a gate.
	 * @param sg <code>true</code> if we are defining a gate
	 */
	protected void setSettingGate(boolean sg) {
		synchronized (this) {
			settingGate = sg;
		}
	}

	/**
	 * Sets whether the mouse is moving.
	 * 
	 * @param mm <code>true</code> if the mouse is moving
	 */
	protected void setMouseMoved(boolean mm) {
		synchronized (this) {
			mouseMoved = mm;
		}
	}

	/**
	 * Show the making of a gate, point by point.
	 * 
	 * @param mode
	 *            GATE_NEW, GATE_CONTINUE, GATE_SAVE or GATE_CANCEL
	 * @param pChannel
	 *            channel coordinates of clicked channel
	 * @param pPixel screen coordinates of click
	 */
	abstract void displaySetGate(GateSetMode mode, Bin pChannel, Point pPixel);
	
	abstract void overlayHistograms(Histogram [] overlayHists);
	
	abstract void removeOverlays();
	
	/*
     * non-javadoc: Copies the counts into the local array--needed by scroller.
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
		} else if (type == Histogram.Type.ONE_D_DOUBLE) {
			System.arraycopy(hist.getCounts(), 0, counts, 0,
					hist.getSizeX());
		} else if (type == Histogram.Type.TWO_DIM_INT) {
			int[][] counts2dInt = (int[][]) hist.getCounts();
			for (int i = 0; i < hist.getSizeX(); i++) {
				for (int j = 0; j < hist.getSizeY(); j++) {
					counts2d[i][j] = counts2dInt[i][j];
				}
			}
		} else if (type == Histogram.Type.TWO_D_DOUBLE) {
			double[][] counts2dDble = (double[][]) hist.getCounts();
			for (int i = 0; i < hist.getSizeX(); i++) {
				System.arraycopy(counts2dDble[i], 0, counts2d[i], 0,
						hist.getSizeY());
			}
		}
	}

	/**
	 * @return limits are how the histogram is to be drawn
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
		panel.repaint();
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

	/**
	 * Set the last point the cursor was moved to.
	 * @param p the last point
	 */
	protected final void setLastMovePoint(Point p) {
		synchronized (lastMovePoint) {
			lastMovePoint.setLocation(p);
		}
	}

	synchronized void setSelectingArea(boolean tf) {
		selectingArea = tf;
		if (selectingArea) {
			panel.addMouseMotionListener(mouseInputAdapter);
		} else {
			panel.removeMouseMotionListener(mouseInputAdapter);
			panel.repaint();
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

	/*
     * non-javadoc: Expand the region viewed.
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

	/*
	 * Non-javadoc: Zoom the region viewed.
	 */
	void zoom(PlotContainer.Zoom inOut) {
		int xll = plotLimits.getMinimumX();
		int xul = plotLimits.getMaximumX();
		int yll = plotLimits.getMinimumY();
		int yul = plotLimits.getMaximumY();
		final int diffX = Math.max(1, (xul - xll) / ZOOM_FACTOR);
		final int diffY = Math.max(1, (yul - yll) / ZOOM_FACTOR);
		if (inOut == PlotContainer.Zoom.OUT) {//zoom out
			xll = xll - diffX;
			xul = xul + diffX;
			yll = yll - diffY;
			yul = yul + diffY;
		} else if (inOut == PlotContainer.Zoom.IN) {//zoom in
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
		plotLimits.setScale(Scale.LINEAR);
		refresh();
	}

	/**
	 * Set the scale to log scale
	 */
	void setLog() {
		plotLimits.setScale(Scale.LOG);
		panel.repaint();
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
		panel.repaint();
	}

	/*
	 * non-javadoc: method to set Counts scale
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
		panel.repaint();
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
		binWidth=1.0;
	}
	
	void setDisplayingGate(boolean dg) {
		synchronized (this) {
			displayingGate = dg;
		}
	}

	synchronized void setBinWidth(double x) {
		binWidth = x;
	}
	synchronized double getBinWidth() {
		return binWidth;
	}

	/**
	 * Get histogram counts at the specified point, which is given in channel
	 * coordinates.
	 * 
	 * @param p the channel
	 * @return the counts at the channel
	 */
	protected abstract double getCount(jam.plot.Bin p);

	/**
     * @return the counts array for the displayed histogram
     */
    protected abstract Object getCounts();

	 /**
      * Get the energy for a channel.
      * 
      * @param channel
      *            the channel
      * @return the energy for the channel
      */
    abstract double getEnergy(double channel);

	/**
	 * Find the maximum number of counts in the region of interest.
	 * 
	 * @return the maximum number of counts in the region of interest
	 */
	protected abstract int findMaximumCounts();

	/**
	 * Find the minimum number of counts in the region of interest.
	 * 
	 * @return the minimum number of counts in the region of interest
	 */
	protected abstract int findMinimumCounts();


	void error(final String mess) {
		Runnable task = new Runnable() {
			public void run() {
				final String plotErrorTitle = "Plot Error";
				JOptionPane.showMessageDialog(panel, mess,
						plotErrorTitle, JOptionPane.ERROR_MESSAGE);
			}
		};
		SwingUtilities.invokeLater(task);
	}

	void warning(final String mess) {
		Runnable task = new Runnable() {
			public void run() {
				final String plotErrorTitle = "Plot Warning";
				JOptionPane.showMessageDialog(panel, mess,
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
	 */
	synchronized void displayGate(Gate gate) {
		Histogram plotHist=getHistogram();
		if (plotHist != null && plotHist.hasGate(gate)) {
			setDisplayingGate(true);
			setCurrentGate(gate);
			panel.repaint();
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

	/*
     * non-javadoc: Paints header for plot to screen and printer. Also sets
     * colors and the size in pixels for a plot.
     */
	private void paintHeader(Graphics g) {
		g.setColor(PlotColorMap.getInstance().getForeground());
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
	 * Method overriden for 1 and 2 d plots for painting the histogram.
	 * 
	 * @param g the graphics context
	 */
	abstract protected void paintHistogram(Graphics g);

	/**
	 * Method overriden for 1 and 2 d for painting the gate.
	 * 
	 * @param g the graphics context
	 */
	abstract protected void paintGate(Graphics g);

	/**
	 * Method overriden for 1 and 2 d for painting overlays.
	 * 
	 * @param g the graphics context
	 */
	abstract protected void paintOverlay(Graphics g);

	/**
	 * Method overriden for 1 and 2 d for painting fits.
	 * 
	 * @param g the graphics context
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
	 * Paint called if mouse moved is enabled.
	 * 
	 * @param gc the graphics context
	 */
	private final void paintMouseMoved(Graphics gc) {
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
		return new ComponentPrintable(panel);
	}

	/* non-javadoc:
	 * ignore channel zero on auto scale
	 */
	private final void setIgnoreChZero(boolean state) {
		ignoreChZero = state;
	}

	/* non-javadoc:
	 * ignore channel full scale on auto scale
	 */
	private final void setIgnoreChFull(boolean state) {
		ignoreChFull = state;
	}

	/* non-javadoc:
	 * Set the color mode, color palette
	 */
	private final void setColorMode(boolean cm) {
		synchronized (this) {
			colorMode = cm ? PlotColorMap.W_ON_B
					: PlotColorMap.B_ON_W;
		}
		panel.setBackground(PlotColorMap.getInstance().getBackground());
	}
	
	/* Plot mouse methods */
	
	/* non-javadoc: 
	 * Add plot select listener
	 */
	void setPlotSelectListener(PlotSelectListener plotSelectListener) {
		plotMouse.setPlotSelectListener(plotSelectListener);
	}
	
	/**
	 * Add a mouse listener.
	 * 
	 * @param listener the listener to add
	 */
	void addPlotMouseListener(PlotMouseListener listener) {
		plotMouse.addListener(listener);
	}

	/* non-javadoc:
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
	
	/* End Plot mouse methods */

	/**
	 * Set the maximum counts limit on the scale, but constrained for scrolling.
	 * 
	 * @param maxC maximum counts
	 */
	void setMaximumCountsConstrained(int maxC) {
		int temp = maxC;
		/* Don't go too small. */
		if (temp < FS_MIN) {
			temp = FS_MIN;
		}
		/* Don't go too big. */
		if (temp > FS_MAX) {
			temp = FS_MAX;
		}
		plotLimits.setMaximumCounts(temp);
	}

	/* non-javadoc:
	 * get histogram x size need by scroller
	 */
	int getSizeX() {
		return sizeX;
	}

	/* non-javadoc: 
	 * get histogram y size needed by scroller
	 */
	int getSizeY() {
		return sizeY;
	}

	synchronized Histogram getHistogram(){
		return plotHistNum<0 ?  null : Histogram.getHistogram(plotHistNum);
	}
		
	/**
	 * Not used.
	 * 
	 * @param me
	 *            created when the mouse is moved
	 */
	abstract protected void mouseMoved(MouseEvent me);

	/**
	 * Anonymous implementation to handle mouse input.
	 */
	protected final MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
		/**
		 * Undo last temporary line drawn.
		 * 
		 * @param e
		 *            created when mouse exits the plot
		 */
		public void mouseExited(MouseEvent e) {
			setMouseMoved(false);
			panel.repaint();
		}

		public void mouseMoved(MouseEvent e) {
			AbstractPlot.this.mouseMoved(e);
		}
	};

	/**
	 * @return <code>true</code> if the area selection clip is clear
	 */
	protected final boolean isSelectingAreaClipClear() {
		synchronized (selectingAreaClip) {
			return selectingAreaClip.height == 0;
		}
	}

	/**
	 * Clears the area selection clip.
	 *
	 */
	protected final void clearSelectingAreaClip() {
		synchronized (selectingAreaClip) {
			selectingAreaClip.setSize(0, 0);
		}
	}
	
	final Component getComponent(){
	    return panel;
	}
}