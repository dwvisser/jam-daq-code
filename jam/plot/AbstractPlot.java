package jam.plot;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.ComponentPrintable;
import jam.plot.color.GraphicsModes;
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
abstract class AbstractPlot implements PlotPrefs, PreferenceChangeListener,
		GraphicsModes {

	final class PlotPanel extends JPanel {

		PlotPanel() {
			super(false);
		}

		/**
		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
		 */
		protected void paintComponent(final Graphics graphics) {
			super.paintComponent(graphics);
			final PlotColorMap pcm = PlotColorMap.getInstance();
			if (printing) { // output to printer
				// FIXME KBS font not set
				// graph.setFont(printFont);
				pcm.setColorMap(modes.PRINT);
				graph.setView(pageformat);
			} else { // output to screen
				// graph.setFont(screenFont);
				pcm.setColorMap(colorMode);
				graph.setView(null);
			}
			final Color foreground = pcm.getForeground();
			graphics.setColor(foreground); // color foreground
			this.setForeground(foreground);
			this.setBackground(pcm.getBackground());
			viewSize = getSize();
			graph.update(graphics, viewSize, plotLimits);
			/*
			 * give graph all pertinent info, draw outline, tickmarks, labels,
			 * and title
			 */
			final Histogram plotHist = getHistogram();
			if (plotHist != null) {
				paintHeader(graphics);
				if (binWidth > plotHist.getSizeX()) {
					binWidth = 1.0;
					warning("Bin width > hist size, so setting bin width back to 1.");
				}
				paintHistogram(graphics);
				paintAdditional(graphics);
			}
		}

		private void paintAdditional(final Graphics graphics) {
			if (displayingGate) { // are we to display a gate
				paintGate(graphics);
			}
			if (displayingOverlay) {
				paintOverlay(graphics);
			}
			if (displayingFit) {
				paintFit(graphics);
			}
			if (areaMarked) {
				paintMarkArea(graphics);
			}
			if (settingGate) {
				paintSetGatePoints(graphics);
			}
			if (markingChannels) {
				paintMarkedChannels(graphics);
			}
			if (mouseMove) {
				/* we handle selecting area or setting gate here */
				paintMouseMoved(graphics);
			}
		}

		/**
		 * @return the container class instance
		 */
		public AbstractPlot getPlot() {
			return AbstractPlot.this;
		}
	}

	/**
	 * The actual panel.
	 */
	protected transient final PlotPanel panel = new PlotPanel();

	/**
	 * Specifies how much to zoom, zoom is 1/ZOOM_FACTOR
	 */
	private final static int ZOOM_FACTOR = 10;

	private static final int FS_MIN = 5; // minumum that Counts can be set to

	/** Maximum that counts can be set to. */
	private static final int FS_MAX = 1000000;

	/** Number of Histogram to plot */
	private transient int plotHistNum;

	private transient Scroller scrollbars;

	/**
	 * Plot graphics handler.
	 */
	protected transient final PlotGraphics graph;

	/* Gives channels of mouse click. */
	private transient PlotMouse plotMouse;

	/**
	 * Descriptor of domain and range of histogram to plot.
	 */
	protected transient Limits plotLimits;

	private transient PageFormat pageformat = null;

	/* Histogram related stuff. */

	/**
	 * Number of x-channels.
	 */
	protected transient int sizeX;

	/**
	 * Number of y-channels.
	 */
	protected transient int sizeY;

	/**
	 * Descriptor of array type for histogram.
	 */
	protected transient Histogram.Type type;

	/**
	 * 1D counts.
	 */
	protected transient double[] counts;

	/**
	 * 2D counts.
	 */
	protected transient double[][] counts2d;

	private transient boolean hasAhist;

	/* Gate stuff. */

	/**
	 * The currently selected gate.
	 */
	protected transient Gate currentGate;

	/** Gate points in plot coordinates (channels). */
	protected final Polygon pointsGate = new Polygon();

	/** Currently setting a gate. */
	protected transient boolean settingGate = false;

	/** selection start point in plot coordinates */
	protected transient Bin selectStart = Bin.Factory.create();

	/** currently displaying a gate? */
	protected transient boolean displayingGate = false;

	/** currently displaying a fit? */
	protected transient boolean displayingFit = false;

	/** currently displaying an overlay? */
	protected transient boolean displayingOverlay = false;

	/** currently selecting an area? */
	protected transient boolean selectingArea = false;

	/** currently have an area already marked? */
	protected transient boolean areaMarked = false;

	/** currently have individual channels already marked? */
	protected transient boolean markingChannels = false;

	/**
	 * configuration for screen plotting
	 */
	protected transient Dimension viewSize;

	/**
	 * Channels that have been marked by clicking or typing.
	 */
	protected transient final List<Bin> markedChannels = new ArrayList<Bin>();

	// TODO don't handle change of fonts yet
	// protected Font screenFont;

	// protected Font printFont;

	/* Color mode for screen, one of PlotColorMap options. */
	private transient modes colorMode;

	private transient int runNumber;

	private transient String date;

	/**
	 * Bin width to use when plotting (1D only).
	 */
	protected double binWidth = 1.0;

	private boolean noFillMode;

	/**
	 * Dont use 0 ch for auto scale
	 */
	protected transient boolean ignoreChZero;

	/**
	 * Dont use full scale ch for auto scale
	 */
	protected transient boolean ignoreChFull;

	private transient boolean printing = false;

	/**
	 * Repaint clip to use when repainting during area selection.
	 */
	protected transient final Rectangle selectingAreaClip = new Rectangle();

	/**
	 * last point mouse moved to, uses plot coordinates when selecting an area,
	 * and uses graphics coordinates when setting a gate (FIX?)
	 */
	protected transient final Point lastMovePoint = new Point();

	/** clip to use when repainting for mouse movement, in graphics coordinates */
	protected transient final Polygon mouseMoveClip = new Polygon();

	private transient boolean mouseMove = false;

	/**
	 * Constructor
	 */
	protected AbstractPlot() {
		super();
		panel.setOpaque(true);
		panel.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		graph = new PlotGraphics(this);
		// Create plot mouse
		plotMouse = new PlotMouse(graph);
		panel.addMouseListener(plotMouse);
		// Setup preferences
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
	void setLayout(final int type) {
		graph.setLayout(type);
	}

	/*
	 * non-javadoc: Set the histogram to plot. If the plot limits are null, make
	 * one save all neccessary histogram parameters to local variables. Allows
	 * general use of data set.
	 */
	void displayHistogram(final Histogram hist) {
		synchronized (this) {
			plotLimits = Limits.getLimits(hist);
			if (hist == null) {// we have a null histogram so fake it
				hasAhist = false;
				plotHistNum = -1;
				counts = new double[100];
				counts2d = null;
				type = Histogram.Type.ONE_DIM_INT;
				sizeX = 100;
				sizeY = 0;
			} else {
				hasAhist = true;
				plotHistNum = hist.getNumber();
				copyCounts(hist); // copy hist counts
				/* Limits contains handle to Models */
				scrollbars.setLimits(plotLimits);
			}
			displayingGate = false;
			displayingOverlay = false;
			displayingFit = false;
		}
	}

	/**
	 * @see PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
	public void preferenceChange(final PreferenceChangeEvent pce) {
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
	boolean hasHistogram() {
		return hasAhist;
	}

	private void setNoFillMode(final boolean bool) {
		synchronized (this) {
			noFillMode = bool;
		}
	}

	/**
	 * @return if we are in the "no fill mode"
	 */
	protected final boolean isNoFillMode() {
		synchronized (this) {
			return noFillMode;
		}
	}

	private final boolean plotDataExists() {
		synchronized (this) {
			final Histogram plotHist = getHistogram();
			return plotHist != null && plotHist.getCounts() != null;
		}
	}

	/*
	 * non-javadoc: add scrollbars
	 */
	void addScrollBars(final Scroller scroller) {
		scrollbars = scroller;
	}

	/**
	 * Sets whether we are in the middle of defining a gate.
	 * 
	 * @param whether
	 *            <code>true</code> if we are defining a gate
	 */
	protected void setSettingGate(final boolean whether) {
		synchronized (this) {
			settingGate = whether;
		}
	}

	/**
	 * Sets whether the mouse is moving.
	 * 
	 * @param moved
	 *            <code>true</code> if the mouse is moving
	 */
	protected void setMouseMoved(final boolean moved) {
		synchronized (this) {
			mouseMove = moved;
		}
	}

	/**
	 * Show the making of a gate, point by point.
	 * 
	 * @param mode
	 *            GATE_NEW, GATE_CONTINUE, GATE_SAVE or GATE_CANCEL
	 * @param pChannel
	 *            channel coordinates of clicked channel
	 * @param pPixel
	 *            screen coordinates of click
	 */
	abstract void displaySetGate(GateSetMode mode, Bin pChannel, Point pPixel);

	abstract void overlayHistograms(List<Histogram> overlayHists);

	abstract void removeOverlays();

	/*
	 * non-javadoc: Copies the counts into the local array--needed by scroller.
	 */
	private final void copyCounts(final Histogram hist) {
		type = hist.getType();
		sizeX = hist.getSizeX();
		sizeY = hist.getSizeY();// 0 if 1-d
		if (type.getDimensionality() == 2) {
			counts2d = new double[sizeX][sizeY];
			if (type == Histogram.Type.TWO_DIM_INT) {
				copyCounts2dInt(hist);
			} else {// must be floating point
				final double[][] counts2dDble = (double[][]) hist.getCounts();
				for (int i = 0; i < hist.getSizeX(); i++) {
					System.arraycopy(counts2dDble[i], 0, counts2d[i], 0, hist
							.getSizeY());
				}
			}
		} else {// dim==1
			if (type == Histogram.Type.ONE_DIM_INT) {
				final int[] temp = (int[]) hist.getCounts();
				counts = new double[sizeX];
				/*
				 * NOT System.arraycopy() because of array type difference
				 */
				for (int i = 0; i < temp.length; i++) {
					counts[i] = temp[i];
				}
			} else {// must be floating point
				counts = ((double[]) hist.getCounts()).clone();
			}
		}
	}

	private final void copyCounts2dInt(final Histogram hist) {
		final int[][] counts2dInt = (int[][]) hist.getCounts();
		for (int i = 0; i < hist.getSizeX(); i++) {
			for (int j = 0; j < hist.getSizeY(); j++) {
				counts2d[i][j] = counts2dInt[i][j];
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
	 * @param bin
	 *            graphics coordinates on the plot where the channel is
	 */
	final void markChannel(final Bin bin) {
		markingChannels = true;
		markedChannels.add((Bin)bin.clone());
		panel.repaint();
	}

	abstract int getChannel(double energy);

	void setMarkingChannels(final boolean marking) {
		synchronized (this) {
			markingChannels = marking;
			if (!markingChannels) {
				markedChannels.clear();
			}
		}
	}

	/**
	 * Start marking an area.
	 * 
	 * @param bin
	 *            starting point in plot coordinates
	 */
	protected final void initializeSelectingArea(final Bin bin) {
		setSelectingArea(true);
		selectStart.setChannel(bin);
		setLastMovePoint(bin.getPoint());
	}

	/**
	 * Set the last point the cursor was moved to.
	 * 
	 * @param point
	 *            the last point
	 */
	protected final void setLastMovePoint(final Point point) {
		synchronized (lastMovePoint) {
			lastMovePoint.setLocation(point);
		}
	}

	void setSelectingArea(final boolean selecting) {
		synchronized (this) {
			selectingArea = selecting;
			if (selectingArea) {
				panel.addMouseMotionListener(mouseInputAdapter);
			} else {
				panel.removeMouseMotionListener(mouseInputAdapter);
				panel.repaint();
			}
		}
	}

	/**
	 * Mark an area on the plot.
	 * 
	 * @param bin1
	 *            a corner of the rectangle in plot coordinates
	 * @param bin2
	 *            a corner of the rectangle in plot coordinates
	 */
	abstract void markArea(Bin bin1, Bin bin2);

	void setMarkArea(final boolean marked) {
		synchronized (this) {
			areaMarked = marked;
		}
	}

	/*
	 * non-javadoc: Expand the region viewed.
	 */
	void expand(final Bin bin1, final Bin bin2) {
		final int xCoord1 = bin1.getX();
		final int xCoord2 = bin2.getX();
		final int yCoord1 = bin1.getY();
		final int yCoord2 = bin2.getY();
		int xll; // x lower limit
		int xul; // x upper limit
		int yll; // y lower limit
		int yul; // y upper limit
		if (xCoord1 <= xCoord2) {
			xll = xCoord1;
			xul = xCoord2;
		} else {
			xll = xCoord2;
			xul = xCoord1;
		}
		// check for beyond extremes and set to extremes
		if ((xll < 0) || (xll > sizeX - 1)) {
			xll = 0;
		}
		if ((xul < 0) || (xul > sizeX - 1)) {
			xul = sizeX - 1;
		}
		if (yCoord1 <= yCoord2) {
			yll = yCoord1;
			yul = yCoord2;
		} else {
			yll = yCoord2;
			yul = yCoord1;
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
	void zoom(final PlotContainer.Zoom inOut) {
		int xll = plotLimits.getMinimumX();
		int xul = plotLimits.getMaximumX();
		int yll = plotLimits.getMinimumY();
		int yul = plotLimits.getMaximumY();
		final int diffX = Math.max(1, (xul - xll) / ZOOM_FACTOR);
		final int diffY = Math.max(1, (yul - yll) / ZOOM_FACTOR);
		if (inOut == PlotContainer.Zoom.OUT) {// zoom out
			xll = xll - diffX;
			xul = xul + diffX;
			yll = yll - diffY;
			yul = yul + diffY;
		} else if (inOut == PlotContainer.Zoom.IN) {// zoom in
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
			final int temp = xll;
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
			final int temp = yll;
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
		final Histogram plotHist = getHistogram();
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
	void setRange(final int limC1, final int limC2) {
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
		final Histogram plotHist = getHistogram();
		copyCounts(plotHist);
		panel.repaint();
	}

	/**
	 * Updated the display, resetting so that fits, gates and overlays are no
	 * longer shown.
	 */
	void update() {
		reset();
		if (getCounts() != null) {
			refresh();
		}
	}

	/**
	 * Reset state
	 * 
	 */
	void reset() {
		synchronized (this) {
			displayingGate = false;
			displayingFit = false;
			displayingOverlay = false;
			selectingArea = false;
			areaMarked = false;
			setMarkingChannels(false);
			binWidth = 1.0;
		}
	}

	void setDisplayingGate(final boolean displaying) {
		synchronized (this) {
			displayingGate = displaying;
		}
	}

	void setBinWidth(final double width) {
		synchronized (this) {
			binWidth = width;
		}
	}

	double getBinWidth() {
		synchronized (this) {
			return binWidth;
		}
	}

	/**
	 * Get histogram counts at the specified point, which is given in channel
	 * coordinates.
	 * 
	 * @param bin
	 *            the channel
	 * @return the counts at the channel
	 */
	protected abstract double getCount(Bin bin);

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
		final Runnable task = new Runnable() {
			public void run() {
				final String plotErrorTitle = "Plot Error";
				JOptionPane.showMessageDialog(panel, mess, plotErrorTitle,
						JOptionPane.ERROR_MESSAGE);
			}
		};
		SwingUtilities.invokeLater(task);
	}

	void warning(final String mess) {
		final Runnable task = new Runnable() {
			public void run() {
				final String plotErrorTitle = "Plot Warning";
				JOptionPane.showMessageDialog(panel, mess, plotErrorTitle,
						JOptionPane.WARNING_MESSAGE);
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
	void displayGate(final Gate gate) {
		synchronized (this) {
			final Histogram plotHist = getHistogram();
			if (plotHist != null && plotHist.hasGate(gate)) {
				setDisplayingGate(true);
				setCurrentGate(gate);
				panel.repaint();
			} else {
				error("Can't display '" + gate + "' on histogram '" + plotHist
						+ "'.");
			}
		}
	}

	private void setCurrentGate(final Gate gate) {
		synchronized (this) {
			currentGate = gate;
		}
	}

	abstract void displayFit(double[][] signals, double[] background,
			double[] residuals, int lowerLimit);

	/*
	 * non-javadoc: Paints header for plot to screen and printer. Also sets
	 * colors and the size in pixels for a plot.
	 */
	private void paintHeader(final Graphics graphics) {
		graphics.setColor(PlotColorMap.getInstance().getForeground());
		if (printing) { // output to printer
			graph.drawDate(date); // date
			graph.drawRun(runNumber); // run number
		}
		graph.drawBorder();
	}

	/**
	 * Method for painting a area while it is being selected.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintSelectingArea(Graphics graphics);

	/**
	 * Method for painting a clicked area.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintMarkArea(Graphics graphics);

	/**
	 * Method for painting a clicked channel.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintMarkedChannels(Graphics graphics);

	/**
	 * Method overriden for 1 and 2 d plots for painting the histogram.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintHistogram(Graphics graphics);

	/**
	 * Method overriden for 1 and 2 d for painting the gate.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintGate(Graphics graphics);

	/**
	 * Method overriden for 1 and 2 d for painting overlays.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintOverlay(Graphics graphics);

	/**
	 * Method overriden for 1 and 2 d for painting fits.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintFit(Graphics graphics);

	/**
	 * Method for painting segments while setting a gate.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintSettingGate(Graphics graphics);

	/**
	 * Method for painting segments while setting a gate.
	 * 
	 * @param g
	 *            the graphics context
	 */
	abstract protected void paintSetGatePoints(Graphics graphics);

	/**
	 * Paint called if mouse moved is enabled.
	 * 
	 * @param graphics
	 *            the graphics context
	 */
	private final void paintMouseMoved(final Graphics graphics) {
		if (settingGate) {
			paintSettingGate(graphics);
		} else if (selectingArea) {
			paintSelectingArea(graphics);
		}
	}

	void setRenderForPrinting(final boolean rfp, final PageFormat format) {
		synchronized (this) {
			printing = rfp;
			pageformat = format;
		}
	}

	ComponentPrintable getComponentPrintable(final int run,
			final String dateString) {
		runNumber = run;
		date = dateString;
		return new ComponentPrintable(panel);
	}

	/*
	 * non-javadoc: ignore channel zero on auto scale
	 */
	private final void setIgnoreChZero(final boolean state) {
		ignoreChZero = state;
	}

	/*
	 * non-javadoc: ignore channel full scale on auto scale
	 */
	private final void setIgnoreChFull(final boolean state) {
		ignoreChFull = state;
	}

	/*
	 * non-javadoc: Set the color mode, color palette
	 */
	private final void setColorMode(final boolean color) {
		synchronized (this) {
			colorMode = color ? modes.W_ON_B : modes.B_ON_W;
		}
		panel.setBackground(PlotColorMap.getInstance().getBackground());
	}

	/* Plot mouse methods */

	/*
	 * non-javadoc: Add plot select listener
	 */
	void setPlotSelectListener(final PlotSelectListener selectListener) {
		plotMouse.setPlotSelectListener(selectListener);
	}

	/**
	 * Add a mouse listener.
	 * 
	 * @param listener
	 *            the listener to add
	 */
	void addPlotMouseListener(final PlotMouseListener listener) {
		plotMouse.addListener(listener);
	}

	/*
	 * non-javadoc: Remove a mouse listener.
	 */
	void removePlotMouseListener(final PlotMouseListener listener) {
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
	 * @param maxC
	 *            maximum counts
	 */
	void setMaximumCountsConstrained(final int maxC) {
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

	/*
	 * non-javadoc: get histogram x size need by scroller
	 */
	int getSizeX() {
		return sizeX;
	}

	/*
	 * non-javadoc: get histogram y size needed by scroller
	 */
	int getSizeY() {
		return sizeY;
	}

	Histogram getHistogram() {
		synchronized (this) {
			return plotHistNum < 0 ? null : Histogram.getHistogram(plotHistNum);
		}
	}

	/**
	 * Not used.
	 * 
	 * @param mouseEvent
	 *            created when the mouse is moved
	 */
	abstract protected void mouseMoved(MouseEvent mouseEvent);

	/**
	 * Anonymous implementation to handle mouse input.
	 */
	protected transient final MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
		/**
		 * Undo last temporary line drawn.
		 * 
		 * @param mouseEvent
		 *            created when mouse exits the plot
		 */
		public void mouseExited(final MouseEvent mouseEvent) {
			setMouseMoved(false);
			panel.repaint();
		}

		public void mouseMoved(final MouseEvent mouseEvent) {
			AbstractPlot.this.mouseMoved(mouseEvent);
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

	final Component getComponent() {
		return panel;
	}
}