package jam.plot;

import static jam.plot.PlotPrefs.AUTO_IGNORE_FULL;
import static jam.plot.PlotPrefs.AUTO_IGNORE_ZERO;
import static jam.plot.PlotPrefs.BLACK_BACKGROUND;
import static jam.plot.PlotPrefs.HIGHLIGHT_GATE;
import static jam.plot.PlotPrefs.PREFS;
import jam.data.AbstractHist1D;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.RunInfo;
import jam.plot.color.PlotColorMap;
import jam.plot.common.Scale;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
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
abstract class AbstractPlot implements PreferenceChangeListener {

	protected transient final Options options = new Options();

	/**
	 * The currently selected gate.
	 */
	protected transient Gate currentGate;

	/**
	 * Plot graphics handler.
	 */
	protected transient final Painter graph;

	/**
	 * last point mouse moved to, uses plot coordinates when selecting an area,
	 * and uses graphics coordinates when setting a gate (FIX?)
	 */
	protected transient final Point lastMovePoint = new Point();

	/**
	 * Descriptor of domain and range of histogram to plot.
	 */
	protected transient Limits limits;

	/**
	 * Channels that have been marked by clicking or typing.
	 */
	protected transient final List<Bin> markedChannels = new ArrayList<Bin>();

	/**
	 * The actual panel.
	 */
	protected transient final PlotPanel panel = new PlotPanel(this);

	/** Number of Histogram to plot */
	private transient int plotHistNum = -1;

	/* Gives channels of mouse click. */
	protected transient final PlotMouse plotMouse;

	protected transient final PlotSelection plotSelection = new PlotSelection();

	/** Gate points in plot coordinates (channels). */
	protected final Polygon pointsGate = new Polygon();

	private transient Scroller scrollbars;

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
		graph = new Painter(this);
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
		limits.setMinimumCounts(110 * findMinimumCounts() / 100);
		if (findMaximumCounts() > 5) {
			limits.setMaximumCounts(110 * findMaximumCounts() / 100);
		} else {
			limits.setMaximumCounts(5);
		}
		/* scroll bars do not always reset on their own */
		scrollbars.update();
		panel.repaint();
	}

	/**
	 * Clears the area selection clip.
	 * 
	 */
	protected final void clearSelectingAreaClip() {
		synchronized (plotSelection.areaClip) {
			plotSelection.areaClip.setSize(0, 0);
		}
	}

	protected abstract void copyCounts(Histogram hist);

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
			if (plotHist != null && plotHist.getGateCollection().hasGate(gate)) {
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
			limits = Limits.getLimits(hist);
			if (hist == null) {// we have a null histogram so fake it
				plotHistNum = -1;
				size = new Size(100);
			} else {
				plotHistNum = hist.getNumber();
				copyCounts(hist); // copy hist counts
				/* Limits contains handle to Models */
				scrollbars.setLimits(limits);
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
		int xll = Math.min(xCoord1, xCoord2);// x lower limit
		int xul = Math.max(xCoord1, xCoord2);// x upper limit
		// check for beyond extremes and set to extremes
		if ((xll < 0) || (xll > size.getSizeX() - 1)) {
			xll = 0;
		}
		if ((xul < 0) || (xul > size.getSizeX() - 1)) {
			xul = size.getSizeX() - 1;
		}
		int yll = Math.min(yCoord1, yCoord2);// y lower limit
		int yul = Math.max(yCoord1, yCoord2);// y upper limit
		/* check for beyond extremes and set to extremes */
		if ((yll < 0) || (yll > size.getSizeY() - 1)) {
			yll = 0;
		}
		if ((yul < 0) || (yul > size.getSizeY() - 1)) {
			yul = size.getSizeY() - 1;
		}
		limits.setMinimumX(xll);
		limits.setMaximumX(xul);
		limits.setMinimumY(yll);
		limits.setMaximumY(yul);
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
		return limits;
	}

	PlotMouse getPlotMouse() {
		return plotMouse;
	}

	Size getSize() {
		return size;
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
		plotSelection.start.setChannel(bin);
		setLastMovePoint(bin.getPoint());
	}

	private final void initPrefs() {
		options.setIgnoreChFull(PREFS.getBoolean(AUTO_IGNORE_FULL, true));
		options.setIgnoreChZero(PREFS.getBoolean(AUTO_IGNORE_ZERO, true));
		panel.setColorMode(PREFS.getBoolean(BLACK_BACKGROUND, false));
		options.setNoFillMode(!PREFS.getBoolean(HIGHLIGHT_GATE, true));
	}

	/**
	 * @return <code>true</code> if the area selection clip is clear
	 */
	protected final boolean isSelectingAreaClipClear() {
		synchronized (plotSelection.areaClip) {
			return plotSelection.areaClip.height == 0;
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

	abstract void overlayHistograms(List<AbstractHist1D> overlayHists);

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
		if (options.isPrinting()) { // output to printer
			graph.drawDate(getDate()); // date
			graph.drawRun(RunInfo.getInstance().runNumber); // run number
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
			return plotHist != null && !plotHist.isClear();
		}
	}

	/**
	 * @see PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
	public void preferenceChange(final PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		final String newValue = pce.getNewValue();
		if (key.equals(PlotPrefs.AUTO_IGNORE_ZERO)) {
			options.setIgnoreChZero(Boolean.valueOf(newValue).booleanValue());
			if (plotDataExists()) {
				autoCounts();
			}
		} else if (key.equals(PlotPrefs.AUTO_IGNORE_FULL)) {
			options.setIgnoreChFull(Boolean.valueOf(newValue).booleanValue());
			if (plotDataExists()) {
				autoCounts();
			}
		} else if (key.equals(PlotPrefs.BLACK_BACKGROUND)) {
			panel.setColorMode(Boolean.valueOf(newValue).booleanValue());
		} else if (key.equals(PlotPrefs.HIGHLIGHT_GATE)) {
			options.setNoFillMode(!Boolean.valueOf(newValue).booleanValue());
		}
	}

	/**
	 * Refresh the display.
	 */
	void refresh() {
		if (scrollbars != null) {
			scrollbars.update();
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
		limits.setMinimumX(0);
		limits.setMaximumX(size.getSizeX() - 1);
		limits.setMinimumY(0);
		limits.setMaximumY(size.getSizeY() - 1);
		refresh();
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
	void setLayout(final GraphicsLayout.Type type) {
		graph.setLayout(type);
	}

	/**
	 * Set the scale to linear scale
	 */
	void setLinear() {
		limits.setScale(Scale.LINEAR);
		refresh();
	}

	/**
	 * Set the scale to log scale
	 */
	void setLog() {
		limits.setScale(Scale.LOG);
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
		final int FS_MIN = 5; // minumum that Counts can be set to
		int temp = Math.max(maxC, FS_MIN);
		/** Maximum that counts can be set to. */
		final int FS_MAX = 1000000;
		temp = Math.min(temp, FS_MAX);
		limits.setMaximumCounts(temp);
	}

	/* End Plot mouse methods */

	/*
	 * non-javadoc: method to set Counts scale
	 */
	void setRange(final int limC1, final int limC2) {
		if (limC1 <= limC2) {
			limits.setMinimumCounts(limC1);
			limits.setMaximumCounts(limC2);
		} else {
			limits.setMinimumCounts(limC2);
			limits.setMaximumCounts(limC1);
		}
		refresh();
	}

	void setRenderForPrinting(final boolean rfp, final PageFormat format) {
		synchronized (this) {
			options.setPrinting(rfp);
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
}