package jam.plot;

import static jam.plot.Constants.BOTTOM;
import static jam.plot.Constants.LEFT;
import static jam.plot.Constants.TOP;
import jam.data.AbstractHist1D;
import jam.data.HistDouble1D;
import jam.data.HistInt1D;
import jam.data.Histogram;
import jam.plot.color.PlotColorMap;
import jam.util.NumberUtilities;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;

import javax.swing.SwingUtilities;

/**
 * Plots a 1-dimensional histogram.
 * 
 * @author Ken Swartz
 */
final class Plot1d extends AbstractPlot {

	private transient double[] fitChannels, fitResiduals, fitBackground,
			fitTotal;

	private transient double[][] fitSignals;

	private transient int areaMark1, areaMark2;

	private transient final List<Integer> overlayNumber = Collections
			.synchronizedList(new ArrayList<Integer>());

	private transient final List<double[]> overlayCounts = Collections
			.synchronizedList(new ArrayList<double[]>());

	private transient final PlotColorMap colorMap = PlotColorMap.getInstance();

	/**
	 * Bin width to use when plotting (1D only).
	 */
	private double binWidth = 1.0;

	private static double sensitivity = 3;

	private static double width = 12;

	private static boolean pfcal = true;

	private static final String X_LABEL_1D = "Channels";

	private static final String Y_LABEL_1D = "Counts";

	/** clip to use when repainting for mouse movement, in graphics coordinates */
	private transient final Polygon mouseMoveClip = new Polygon();

	/**
	 * 1D counts.
	 */
	private transient double[] counts;

	/**
	 * Constructor.
	 * 
	 */
	Plot1d() {
		super();
		setPeakFind(PlotPrefs.PREFS.getBoolean(PlotPrefs.AUTO_PEAK_FIND, true));
	}

	/**
	 * Overlay histograms.
	 */
	void overlayHistograms(final List<AbstractHist1D> overlayHists) {
		panel.setDisplayingOverlay(true);
		/* retain any items in list in the map Performance improvement */
		overlayCounts.clear();
		overlayNumber.clear();
		for (AbstractHist1D hOver : overlayHists) {
			final double[] ctOver = getOverlayCounts(hOver);
			overlayCounts.add(ctOver);
			overlayNumber.add(hOver.getNumber());
		}
		panel.repaint();
	}

	protected void copyCounts(final Histogram hist) {
		final Histogram.Type type = hist.getType();
		size = new Size(hist.getSizeX(), hist.getSizeY());
		if (type == Histogram.Type.ONE_DIM_INT) {
			final int[] temp = ((HistInt1D) hist).getCounts();
			counts = NumberUtilities.getInstance().intToDoubleArray(temp);
		} else {// must be floating point
			counts = ((HistDouble1D) hist).getCounts();
		}
	}

	void reset() {
		super.reset();
		setBinWidth(1.0);
	}

	void displayHistogram(final Histogram hist) {
		synchronized (this) {
			if (hist == null) {
				counts = new double[100];
			}
			super.displayHistogram(hist);
		}
	}

	/**
	 * @param hOver
	 * @return
	 */
	private double[] getOverlayCounts(final AbstractHist1D hOver) {
		final int sizex = hOver.getSizeX();
		double[] ctOver;
		final Histogram.Type hoType = hOver.getType();
		if (hoType == Histogram.Type.ONE_DIM_INT) {
			final int[] countsInt = ((HistInt1D) hOver).getCounts();
			ctOver = NumberUtilities.getInstance().intToDoubleArray(countsInt);
		} else {// (hoType == Histogram.Type.ONE_D_DOUBLE)
			ctOver = new double[sizex];
			System.arraycopy(((HistDouble1D) hOver).getCounts(), 0, ctOver, 0,
					sizex);
		}
		return ctOver;
	}

	void removeOverlays() {
		overlayCounts.clear();
		overlayNumber.clear();
	}

	void displaySetGate(final GateSetMode mode, final Bin pChannel,
			final Point pPixel) {
		if (mode == GateSetMode.GATE_NEW) {
			pointsGate.reset();
			panel.setListenToMouse(true);
			panel.setListenToMouseMotion(true);
			panel.setSettingGate(true);
		} else {
			if (mode == GateSetMode.GATE_CONTINUE) {
				pointsGate.addPoint(pChannel.getX(), pChannel.getY());
				setLastMovePoint(pPixel);
			} else if (mode == GateSetMode.GATE_SAVE) {
				pointsGate.reset();
				panel.setListenToMouse(false);
				panel.setListenToMouseMotion(false);
			} else if (mode == GateSetMode.GATE_CANCEL) {
				pointsGate.reset();
				panel.setSettingGate(false);
				panel.setListenToMouse(false);
				panel.setListenToMouseMotion(false);
			}
			panel.repaint();
		}
	}

	protected void paintSetGatePoints(final Graphics graphics) {
		graphics.setColor(colorMap.getGateShow());
		graph.settingGate1d(graph.toView(pointsGate));
	}

	protected void paintSettingGate(final Graphics graphics) {
		graphics.setColor(colorMap.getGateDraw());
		final int xValue1 = pointsGate.xpoints[pointsGate.npoints - 1];
		final int xValue2 = graph.toDataHorz(lastMovePoint.x);
		graph.markAreaOutline1d(xValue1, xValue2);
		panel.setMouseMoved(false);
		clearMouseMoveClip();
	}

	private void clearMouseMoveClip() {
		synchronized (mouseMoveClip) {
			mouseMoveClip.reset();
		}
	}

	/**
	 * Displays a fit, starting
	 */
	void displayFit(final double[][] signals, final double[] background,
			final double[] residuals, final int lowerLimit) {
		this.fitBackground = new double[0];
		this.fitChannels = new double[0];
		this.fitResiduals = new double[0];
		this.fitTotal = new double[0];
		panel.setDisplayingFit(true);
		int length = 0;
		length = (signals == null) ? background.length : signals[0].length;
		fitChannels = new double[length];
		for (int i = 0; i < length; i++) {
			this.fitChannels[i] = lowerLimit + i + 0.5;
		}
		if (signals != null) {
			this.fitSignals = new double[signals.length][length];
			this.fitTotal = new double[length];
			for (int sig = 0; sig < signals.length; sig++) {
				System.arraycopy(signals[sig], 0, this.fitSignals[sig], 0,
						length);
				for (int bin = 0; bin < length; bin++) {
					fitTotal[bin] += signals[sig][bin];
				}
			}
		}
		if (background != null) {
			setBackgroundAndSignals(signals, background, length);
		}
		if (residuals != null) {
			this.fitResiduals = new double[length];
			System.arraycopy(residuals, 0, fitResiduals, 0, length);
		}
		panel.repaint();
	}

	/**
	 * @param signals
	 * @param background
	 * @param length
	 */
	private void setBackgroundAndSignals(final double[][] signals,
			final double[] background, final int length) {
		this.fitBackground = new double[length];
		System.arraycopy(background, 0, fitBackground, 0, length);
		if (signals != null) {
			for (int bin = 0; bin < length; bin++) {
				fitTotal[bin] += background[bin];
				for (int sig = 0; sig < signals.length; sig++) {
					fitSignals[sig][bin] += background[bin];
				}
			}
		}
	}

	protected void paintMarkedChannels(final Graphics graphics) {
		graphics.setColor(colorMap.getMark());
		for (Bin bin : markedChannels) {
			final int xChannel = bin.getX();
			graph.markChannel1d(xChannel, counts[xChannel]);
		}
	}

	protected void paintSelectingArea(final Graphics graphics) {
		final Graphics2D graphics2D = (Graphics2D) graphics;
		graphics2D.setColor(colorMap.getArea());
		graph.markAreaOutline1d(plotSelection.start.getX(), lastMovePoint.x);
		panel.setMouseMoved(false);
		clearSelectingAreaClip();
	}

	/**
	 * Mark Area. The y-values are ignored.
	 * 
	 * @param bin1
	 *            one limit
	 * @param bin2
	 *            the other limit
	 */
	public void markArea(final Bin bin1, final Bin bin2) {
		synchronized (this) {
			panel.setAreaMarked((bin1 != null) && (bin2 != null));
			if (panel.isAreaMarked()) {
				final int xValue1 = bin1.getX();
				final int xValue2 = bin2.getX();
				areaMark1 = Math.min(xValue1, xValue2);
				areaMark2 = Math.max(xValue1, xValue2);
			}
		}
		panel.repaint();
	}

	protected void paintMarkArea(final Graphics graphics) {
		final Graphics2D graphics2D = (Graphics2D) graphics;
		final Composite prev = graphics2D.getComposite();
		graphics2D.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.5f));
		graphics.setColor(colorMap.getArea());
		graph.update(graphics, viewSize, limits);
		graph.markArea1d(areaMark1, areaMark2, counts);
		graphics2D.setComposite(prev);
	}

	/**
	 * Draw the current histogram including title, border, tickmarks, tickmark
	 * labels and last but not least update the scrollbars
	 */
	protected void paintHistogram(final Graphics graphics) {
		final Histogram plotHist = getHistogram();
		if (plotHist.getDimensionality() != 1) {
			return;// not sure how this happens, but need to check
		}
		graphics.setColor(colorMap.getHistogram());
		graph.drawHist(counts, getBinWidth());
		if (autoPeakFind) {
			graph.drawPeakLabels(((AbstractHist1D) plotHist).findPeaks(
					sensitivity, width, pfcal));
		}
		/* draw ticks after histogram so they are on top */
		graphics.setColor(colorMap.getForeground());
		graph.drawTitle(plotHist.getTitle(), TOP);

		graph.drawTicks(BOTTOM);
		graph.drawLabels(BOTTOM);
		graph.drawTicks(LEFT);
		graph.drawLabels(LEFT);
		final String axisLabelX = plotHist.getLabelX();
		if (axisLabelX == null) {
			graph.drawAxisLabel(X_LABEL_1D, BOTTOM);
		} else {
			graph.drawAxisLabel(axisLabelX, BOTTOM);
		}
		final String axisLabelY = plotHist.getLabelY();
		if (axisLabelY == null) {
			graph.drawAxisLabel(Y_LABEL_1D, LEFT);
		} else {
			graph.drawAxisLabel(axisLabelY, LEFT);
		}
	}

	private static boolean autoPeakFind = true;

	private static void setPeakFind(final boolean which) {
		autoPeakFind = which;
	}

	/**
	 * Draw a overlay of another data set
	 */
	protected void paintOverlay(final Graphics graphics) {
		final Graphics2D graphics2d = (Graphics2D) graphics;
		int index = 0;
		/*
		 * I had compositing set here, but apparently, it's too many small draws
		 * using compositing, causing a slight performance issue.
		 */
		final int len = panel.isDisplayingOverlay() ? overlayNumber.size() : 0;
		if (len > 0) {
			final int[] overlayInts = new int[len];
			for (int num : overlayNumber) {
				overlayInts[index] = num;
				graphics2d.setColor(colorMap.getOverlay(index));
				graph.drawHist(overlayCounts.get(index), getBinWidth());
				index++;
			}
			final Histogram plotHist = getHistogram();
			graph.drawNumber(plotHist.getNumber(), overlayInts);
		}

	}

	double getBinWidth() {
		synchronized (this) {
			return binWidth;
		}
	}

	void setBinWidth(final double width) {
		synchronized (this) {
			binWidth = width;
		}
	}

	/**
	 * Paint a gate on the give graphics object
	 */
	protected void paintGate(final Graphics graphics) {
		final Graphics2D graphics2D = (Graphics2D) graphics;
		final Composite prev = graphics2D.getComposite();
		final boolean noFill = options.isNoFillMode();
		if (!noFill) {
			graphics2D.setComposite(AlphaComposite.getInstance(
					AlphaComposite.SRC_OVER, 0.5f));
		}
		graphics.setColor(colorMap.getGateShow());
		final int lowerLimit = currentGate.getLimits1d()[0];
		final int upperLimit = currentGate.getLimits1d()[1];
		graph.drawGate1d(lowerLimit, upperLimit, noFill);
		graphics2D.setComposite(prev);
	}

	/**
	 * paints a fit to a given graphics
	 */
	protected void paintFit(final Graphics graphics) {
		if (fitChannels != null) {
			if (fitBackground != null) {
				graphics.setColor(colorMap.getFitBackground());
				graph.drawLine(fitChannels, fitBackground);
			}
			if (fitResiduals != null) {
				graphics.setColor(colorMap.getFitResidual());
				graph.drawLine(fitChannels, fitResiduals);
			}
			if (fitSignals != null) {
				graphics.setColor(colorMap.getFitSignal());
				for (int sig = 0; sig < fitSignals.length; sig++) {
					graph.drawLine(fitChannels, fitSignals[sig]);
				}
			}
			if (fitTotal != null) {
				graphics.setColor(colorMap.getFitTotal());
				graph.drawLine(fitChannels, fitTotal);
			}
		}
	}

	/**
	 * Get the counts in a X channel, Y channel ignored.
	 */
	protected double getCount(final Bin bin) {
		return counts[bin.getX()];
	}

	/**
	 * Get the array of counts for the current histogram
	 */
	protected Object getCounts() {
		return counts;
	}

	/**
	 * Find the maximum counts in the part of the histogram displayed
	 */
	protected int findMaximumCounts() {
		int chmax = limits.getMaximumX();
		int chmin = limits.getMinimumX();
		double maxCounts = 0;
		if ((chmin == 0) && (options.isIgnoreChZero())) {
			chmin = 1;
		}
		final int sizeX = size.getSizeX();
		if ((chmax == (sizeX - 1)) && (options.isIgnoreChFull())) {
			chmax = sizeX - 2;
		}
		for (int i = chmin; i <= chmax; i++) {
			if (counts[i] > maxCounts) {
				maxCounts = counts[i];
			}
		}
		return (int) maxCounts;
	}

	/**
	 * Find the minimum counts in the part of the histogram displayed
	 */
	protected int findMinimumCounts() {
		int chmax = limits.getMaximumX();
		int chmin = limits.getMinimumX();
		int minCounts = 0;
		if ((chmin == 0) && (options.isIgnoreChZero())) {
			chmin = 1;
		}
		final int sizeX = size.getSizeX();
		if ((chmax == (sizeX - 1)) && (options.isIgnoreChFull())) {
			chmax = sizeX - 2;
		}
		for (int i = chmin; i <= chmax; i++) {
			if (counts[i] < minCounts) {
				minCounts = (int) counts[i];
			}
		}
		return minCounts;
	}

	/**
	 * 
	 * @param hist
	 */
	void copy2dCounts(@SuppressWarnings("unused")
	Histogram hist) {
		throw new IllegalStateException("Should never be called.");
	}

	/*
	 * non-javadoc: Caller should have checked 'isCalibrated' first.
	 */
	double getEnergy(final double channel) {
		final AbstractHist1D plotHist = (AbstractHist1D) getHistogram();
		return plotHist.getCalibration().getValue(channel);
	}

	/**
	 * Caller should have checked 'isCalibrated' first.
	 */
	int getChannel(final double energy) {
		final AbstractHist1D plotHist = (AbstractHist1D) getHistogram();
		return (int) Math.round(plotHist.getCalibration().getChannel(energy));
	}

	/**
	 * Called when the mouse has moved
	 */
	protected void mouseMoved(final MouseEvent event) {
		panel.setMouseMoved(true);
		if (panel.isSelectingArea()) {
			if (isSelectingAreaClipClear()) {
				synchronized (plotSelection.areaClip) {
					plotSelection.areaClip.setBounds(graph
							.getRectangleOutline1d(plotSelection.start.getX(),
									lastMovePoint.x));
				}
			}
			setLastMovePoint(graph.toData(event.getPoint()).getPoint());
			addToSelectClip(plotSelection.start, Bin.create(lastMovePoint));
			synchronized (plotSelection.areaClip) {
				panel.repaint(getClipBounds(plotSelection.areaClip, false));
			}
		} else if (panel.isSettingGate() && pointsGate.npoints > 0) {
			/* draw new line */
			synchronized (lastMovePoint) {
				if (isMouseMoveClipClear()) {
					final Point point1 = graph.toViewLin(Bin.create(
							pointsGate.xpoints[pointsGate.npoints - 1],
							pointsGate.ypoints[pointsGate.npoints - 1]));
					addToMouseMoveClip(point1.x, point1.y);
					if (pointsGate.npoints > 1) {
						final Point point2 = graph.toViewLin(Bin.create(
								pointsGate.xpoints[pointsGate.npoints - 2],
								pointsGate.ypoints[pointsGate.npoints - 2]));
						addToMouseMoveClip(point2.x, point2.y);
					}
					addToMouseMoveClip(lastMovePoint.x, lastMovePoint.y);
				}
				lastMovePoint.setLocation(event.getPoint());
				addToMouseMoveClip(lastMovePoint.x, lastMovePoint.y);
			}
			synchronized (mouseMoveClip) {
				panel.repaint(getClipBounds(mouseMoveClip, false));
			}
		}
	}

	private boolean isMouseMoveClipClear() {
		synchronized (mouseMoveClip) {
			return mouseMoveClip.npoints == 0;
		}
	}

	private void addToMouseMoveClip(final int xcoord, final int ycoord) {
		synchronized (mouseMoveClip) {
			mouseMoveClip.addPoint(xcoord, ycoord);
		}
	}

	/**
	 * Add to the selection clip region, using the two given plot-coordinates
	 * points to indicate the corners of a rectangular region of channels that
	 * needs to be included.
	 * 
	 * @param bin1
	 *            in plot coordinates
	 * @param bin2
	 *            in plot coordinates
	 */
	private void addToSelectClip(final Bin bin1, final Bin bin2) {
		synchronized (plotSelection.areaClip) {
			plotSelection.areaClip.add(graph.getRectangleOutline1d(bin1.getX(),
					bin2.getX()));
		}
	}

	/**
	 * Given a shape, return the bounding rectangle which includes all pixels in
	 * the rectangle bounding the given shape, plus some extra space given by
	 * the plot channels just outside the edge of this rectangle.
	 * 
	 * @param clipShape
	 *            the shape we want to cover with a rectangular clip region
	 * @param shapeInChannelCoords
	 *            if <code>true</code>, the given shape is assumed given in
	 *            channel coordinates, otherwise it is assumed given in graphics
	 *            coordinates
	 * @return a bounding rectangle in the graphics coordinates
	 */
	private Rectangle getClipBounds(final Shape clipShape,
			final boolean shapeInChannelCoords) {
		final Rectangle rval = clipShape.getBounds();
		if (shapeInChannelCoords) {// shape is in channel coordinates
			/* add one more plot channel around the edges */
			/* now do conversion */
			rval.setBounds(graph.getRectangleOutline1d(rval.x - 2, (int) rval
					.getMaxX() + 2));
			rval.width += 1;
			rval.height += 1;
		} else {
			/*
			 * Shape is in view coordinates. Recursively call back with a
			 * polygon using channel coordinates.
			 */
			final Polygon shape = new Polygon();
			final Bin bin1 = graph.toData(rval.getLocation());
			final Bin bin2 = graph.toData(new Point(rval.x + rval.width, rval.y
					+ rval.height));
			shape.addPoint(bin1.getX(), bin1.getY());
			shape.addPoint(bin2.getX(), bin2.getY());
			rval.setBounds(getClipBounds(shape, true));
		}
		return rval;
	}

	/**
	 * @see java.util.prefs.PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
	public void preferenceChange(final PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		if (key.equals(PlotPrefs.AUTO_PEAK_FIND)) {
			setPeakFind(Boolean.valueOf(pce.getNewValue()).booleanValue());
		} else {
			super.preferenceChange(pce);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				panel.repaint();
			}
		});
	}

	/* Preferences */

	static void setSensitivity(final double val) {
		sensitivity = val;
	}

	static void setWidth(final double val) {
		width = val;
	}

	static void setPeakFindDisplayCal(final boolean which) {
		pfcal = which;
	}

}
