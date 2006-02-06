package jam.plot;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.ComponentPrintable;
import jam.global.RunInfo;
import jam.plot.color.PlotColorMap;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Abstract class for displayed plots.
 * 
 * @version 0.5
 * @see jam.plot.Plot1d
 * @see jam.plot.Plot2d
 * @since JDK 1.1
 * @author Ken Swartz
 */
abstract class AbstractPlot implements PlotPrefs, PreferenceChangeListener {

	class Size {
		private transient final int sizeX, sizeY;

		Size(int... size) {
			super();
			sizeX = size[0];
			sizeY = (size.length > 1) ? size[1] : 0;
		}

		int getSizeX() {
			return sizeX;
		}

		int getSizeY() {
			return sizeY;
		}
	}

	/**
	 * Bin width to use when plotting (1D only).
	 */
	protected double binWidth = 1.0;

	/**
	 * 1D counts.
	 */
	protected transient double[] counts;

	/**
	 * 2D counts.
	 */
	//protected transient double[][] counts2d;

	/* Histogram related stuff. */

	protected transient double [][] counts2d;

	/**
	 * The currently selected gate.
	 */
	protected transient Gate currentGate;

	/* Gate stuff. */

	/**
	 * Plot graphics handler.
	 */
	protected transient final PlotGraphics graph;

	/**
	 * Dont use full scale ch for auto scale
	 */
	protected transient boolean ignoreChFull;

	/**
	 * Dont use 0 ch for auto scale
	 */
	protected transient boolean ignoreChZero;

	/**
	 * last point mouse moved to, uses plot coordinates when selecting an area,
	 * and uses graphics coordinates when setting a gate (FIX?)
	 */
	protected transient final Point lastMovePoint = new Point();

	/**
	 * Channels that have been marked by clicking or typing.
	 */
	protected transient final List<Bin> markedChannels = new ArrayList<Bin>();

	/** clip to use when repainting for mouse movement, in graphics coordinates */
	protected transient final Polygon mouseMoveClip = new Polygon();

	private boolean noFillMode;

	/**
	 * The actual panel.
	 */
	protected transient final PlotPanel panel = new PlotPanel(this);

	/** Number of Histogram to plot */
	private transient int plotHistNum = -1;

	/**
	 * Descriptor of domain and range of histogram to plot.
	 */
	protected transient Limits plotLimits;

	/* Gives channels of mouse click. */
	transient final PlotMouse plotMouse;

	/** Gate points in plot coordinates (channels). */
	protected final Polygon pointsGate = new Polygon();

	transient boolean printing = false;

	private transient Scroller scrollbars;

	/**
	 * Repaint clip to use when repainting during area selection.
	 */
	protected transient final Rectangle selectingAreaClip = new Rectangle();

	/** selection start point in plot coordinates */
	protected transient Bin selectStart = Bin.create();

	/**
	 * Size of plot window in channels.
	 */
	protected transient Size size = new Size(0);

	/**
	 * configuration for screen plotting
	 */
	protected transient Dimension viewSize;

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

	/*
	 * non-javadoc: add scrollbars
	 */
	void addScrollBars(final Scroller scroller) {
		scrollbars = scroller;
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

	/**
	 * Clears the area selection clip.
	 * 
	 */
	protected final void clearSelectingAreaClip() {
		synchronized (selectingAreaClip) {
			selectingAreaClip.setSize(0, 0);
		}
	}
	
	/*
	 * non-javadoc: Copies the counts into the local array--needed by scroller.
	 */
	private final void copyCounts(final Histogram hist) {
		final Histogram.Type type = hist.getType();
		size = new Size(hist.getSizeX(), hist.getSizeY());
		if (type.getDimensionality() == 2) {
			counts2d = new double[size.getSizeX()][size.getSizeY()];
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
				counts = new double[size.getSizeX()];
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

	abstract void displayFit(double[][] signals, double[] background,
			double[] residuals, int lowerLimit);

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
				panel.setDisplayingGate(true);
				setCurrentGate(gate);
				panel.repaint();
			} else {
				error("Can't display '" + gate + "' on histogram '" + plotHist
						+ "'.");
			}
		}
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
				plotHistNum = -1;
				counts = new double[100];
				counts2d = null;// NOPMD
				size = new Size(100);
			} else {
				plotHistNum = hist.getNumber();
				copyCounts(hist); // copy hist counts
				/* Limits contains handle to Models */
				scrollbars.setLimits(plotLimits);
			}
			panel.setDisplayingGate(false);
			panel.setDisplayingOverlay(false);
			panel.setDisplayingFit(false);
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
		if ((xll < 0) || (xll > size.getSizeX() - 1)) {
			xll = 0;
		}
		if ((xul < 0) || (xul > size.getSizeX() - 1)) {
			xul = size.getSizeX() - 1;
		}
		if (yCoord1 <= yCoord2) {
			yll = yCoord1;
			yul = yCoord2;
		} else {
			yll = yCoord2;
			yul = yCoord1;
		}
		/* check for beyond extremes and set to extremes */
		if ((yll < 0) || (yll > size.getSizeY() - 1)) {
			yll = 0;
		}
		if ((yul < 0) || (yul > size.getSizeY() - 1)) {
			yul = size.getSizeY() - 1;
		}
		plotLimits.setMinimumX(xll);
		plotLimits.setMaximumX(xul);
		plotLimits.setMinimumY(yll);
		plotLimits.setMaximumY(yul);
		refresh();
	}

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

	double getBinWidth() {
		synchronized (this) {
			return binWidth;
		}
	}

	abstract int getChannel(double energy);

	final Component getComponent() {
		return panel;
	}

	ComponentPrintable getComponentPrintable() {
		return new ComponentPrintable(panel);
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

	/*
	 * non-javadoc: Gets the current date and time as a String.
	 */
	private String getDate() {
		final Date date = new Date(); // getDate and time
		final DateFormat datef = DateFormat.getDateTimeInstance(); // default
		// format
		datef.setTimeZone(TimeZone.getDefault()); // set time zone
		return datef.format(date); // format date
	}

	/**
	 * Get the energy for a channel.
	 * 
	 * @param channel
	 *            the channel
	 * @return the energy for the channel
	 */
	abstract double getEnergy(double channel);

	Histogram getHistogram() {
		synchronized (this) {
			return plotHistNum < 0 ? null : Histogram.getHistogram(plotHistNum);// NOPMD
		}
	}

	/**
	 * @return limits are how the histogram is to be drawn
	 */
	Limits getLimits() {
		return plotLimits;
	}

	/*
	 * non-javadoc: Plot has a valid histogram
	 * 
	 */
	boolean hasHistogram() {
		return plotHistNum >= 0;
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

	private final void initPrefs() {
		setIgnoreChFull(PREFS.getBoolean(AUTO_IGNORE_FULL, true));
		setIgnoreChZero(PREFS.getBoolean(AUTO_IGNORE_ZERO, true));
		panel.setColorMode(PREFS.getBoolean(BLACK_BACKGROUND, false));
		setNoFillMode(!PREFS.getBoolean(HIGHLIGHT_GATE, true));
	}

	/**
	 * @return if we are in the "no fill mode"
	 */
	protected final boolean isNoFillMode() {
		synchronized (this) {
			return noFillMode;
		}
	}

	/**
	 * @return <code>true</code> if the area selection clip is clear
	 */
	protected final boolean isSelectingAreaClipClear() {
		synchronized (selectingAreaClip) {
			return selectingAreaClip.height == 0;
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

	/**
	 * Mark a channel on the plot.
	 * 
	 * @param bin
	 *            graphics coordinates on the plot where the channel is
	 */
	final void markChannel(final Bin bin) {
		setMarkingChannels(true);
		markedChannels.add((Bin) bin.clone());
		panel.repaint();
	}

	/**
	 * Not used.
	 * 
	 * @param mouseEvent
	 *            created when the mouse is moved
	 */
	abstract protected void mouseMoved(MouseEvent mouseEvent);

	abstract void overlayHistograms(List<Histogram> overlayHists);

	/**
	 * Method overriden for 1 and 2 d for painting fits.
	 * 
	 * @param graphics
	 *            the graphics context
	 */
	abstract protected void paintFit(Graphics graphics);

	/**
	 * Method overriden for 1 and 2 d for painting the gate.
	 * 
	 * @param graphics
	 *            the graphics context
	 */
	abstract protected void paintGate(Graphics graphics);

	/*
	 * non-javadoc: Paints header for plot to screen and printer. Also sets
	 * colors and the size in pixels for a plot.
	 */
	void paintHeader(final Graphics graphics) {
		graphics.setColor(PlotColorMap.getInstance().getForeground());
		if (printing) { // output to printer
			graph.drawDate(getDate()); // date
			graph.drawRun(RunInfo.runNumber); // run number
		}
		graph.drawBorder();
	}

	/**
	 * Method overriden for 1 and 2 d plots for painting the histogram.
	 * 
	 * @param graphics
	 *            the graphics context
	 */
	abstract protected void paintHistogram(Graphics graphics);

	/**
	 * Method for painting a clicked area.
	 * 
	 * @param graphics
	 *            the graphics context
	 */
	abstract protected void paintMarkArea(Graphics graphics);

	/**
	 * Method for painting a clicked channel.
	 * 
	 * @param graphics
	 *            the graphics context
	 */
	abstract protected void paintMarkedChannels(Graphics graphics);

	/**
	 * Paint called if mouse moved is enabled.
	 * 
	 * @param graphics
	 *            the graphics context
	 */
	final void paintMouseMoved(final Graphics graphics) {
		if (panel.isSettingGate()) {
			paintSettingGate(graphics);
		} else if (panel.isSelectingArea()) {
			paintSelectingArea(graphics);
		}
	}

	/**
	 * Method overriden for 1 and 2 d for painting overlays.
	 * 
	 * @param graphics
	 *            the graphics context
	 */
	abstract protected void paintOverlay(Graphics graphics);

	/**
	 * Method for painting a area while it is being selected.
	 * 
	 * @param graphics
	 *            the graphics context
	 */
	abstract protected void paintSelectingArea(Graphics graphics);

	/**
	 * Method for painting segments while setting a gate.
	 * 
	 * @param graphics
	 *            the graphics context
	 */
	abstract protected void paintSetGatePoints(Graphics graphics);

	/**
	 * Method for painting segments while setting a gate.
	 * 
	 * @param graphics
	 *            the graphics context
	 */
	abstract protected void paintSettingGate(Graphics graphics);

	private final boolean plotDataExists() {
		synchronized (this) {
			final Histogram plotHist = getHistogram();
			return plotHist != null && plotHist.getCounts() != null;
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
			panel.setColorMode(Boolean.valueOf(newValue).booleanValue());
		} else if (key.equals(PlotPrefs.HIGHLIGHT_GATE)) {
			setNoFillMode(!Boolean.valueOf(newValue).booleanValue());
		}
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

	abstract void removeOverlays();

	/**
	 * Reset state
	 * 
	 */
	void reset() {
		synchronized (this) {
			panel.setDisplayingGate(false);
			panel.setDisplayingFit(false);
			panel.setDisplayingOverlay(false);
			panel.setSelectingArea(false);
			panel.setAreaMarked(false);
			setMarkingChannels(false);
			binWidth = 1.0;
		}
	}

	void setBinWidth(final double width) {
		synchronized (this) {
			binWidth = width;
		}
	}

	private void setCurrentGate(final Gate gate) {
		synchronized (this) {
			currentGate = gate;
		}
	}

	/**
	 * set full range X
	 */
	void setFull() {
		plotLimits.setMinimumX(0);
		plotLimits.setMaximumX(size.getSizeX() - 1);
		plotLimits.setMinimumY(0);
		plotLimits.setMaximumY(size.getSizeY() - 1);
		refresh();
	}

	/*
	 * non-javadoc: ignore channel full scale on auto scale
	 */
	private final void setIgnoreChFull(final boolean state) {
		ignoreChFull = state;
	}

	/*
	 * non-javadoc: ignore channel zero on auto scale
	 */
	private final void setIgnoreChZero(final boolean state) {
		ignoreChZero = state;
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

	/*
	 * non-javadoc: Update layout.
	 */
	void setLayout(final PlotGraphicsLayout.Type type) {
		graph.setLayout(type);
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

	/* Plot mouse methods */

	void setMarkArea(final boolean marked) {
		panel.setAreaMarked(marked);
	}

	void setMarkingChannels(final boolean marking) {
		panel.setMarkingChannels(marking);
		if (!panel.isMarkingChannels()) {
			markedChannels.clear();
		}
	}

	/**
	 * Set the maximum counts limit on the scale, but constrained for scrolling.
	 * 
	 * @param maxC
	 *            maximum counts
	 */
	void setMaximumCountsConstrained(final int maxC) {
		int temp = maxC;
		/* Don't go too small. */
		final int FS_MIN = 5; // minumum that Counts can be set to
		if (temp < FS_MIN) {
			temp = FS_MIN;
		}
		/* Don't go too big. */
		/** Maximum that counts can be set to. */
		final int FS_MAX = 1000000;
		if (temp > FS_MAX) {
			temp = FS_MAX;
		}
		plotLimits.setMaximumCounts(temp);
	}

	/* End Plot mouse methods */

	private void setNoFillMode(final boolean bool) {
		synchronized (this) {
			noFillMode = bool;
		}
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

	void setRenderForPrinting(final boolean rfp, final PageFormat format) {
		synchronized (this) {
			printing = rfp;
			panel.setPageFormat(format);
		}
	}

	void setSelectingArea(final boolean selecting) {
		panel.setSelectingArea(selecting);
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

	/*
	 * Non-javadoc: Zoom the region viewed.
	 */
	void zoom(final PlotContainer.Zoom inOut) {
		int xll = plotLimits.getMinimumX();
		int xul = plotLimits.getMaximumX();
		int yll = plotLimits.getMinimumY();
		int yul = plotLimits.getMaximumY();
		// Specifies how much to zoom, zoom is 1/ZOOM_FACTOR
		final int ZOOM_FACTOR = 10;
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
		if ((xll < 0) || (xll > size.getSizeX() - 1)) {
			xll = 0;
		}
		if ((xul < 0) || (xul > size.getSizeX() - 1)) {
			xul = size.getSizeX() - 1;
		}
		if (xll > xul) {
			final int temp = xll;
			xll = xul - 1;
			xul = temp + 1;
		}
		if ((yll < 0) || (yll > size.getSizeY() - 1)) {
			yll = 0;
		}
		if ((yul < 0) || (yul > size.getSizeY() - 1)) {
			yul = size.getSizeY() - 1;
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
}