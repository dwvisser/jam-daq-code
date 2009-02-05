package jam.plot;

import jam.data.AbstractHist1D;
import jam.data.AbstractHistogram;
import jam.data.HistDouble1D;
import jam.data.HistInt1D;
import jam.data.HistogramType;
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

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.inject.Inject;

/**
 * Plots a 1-dimensional histogram.
 * 
 * @author Ken Swartz
 */
final class Plot1d extends AbstractPlot {

	private static boolean autoPeakFind = true;

	private static boolean pfcal = true;

	private static double sensitivity = 3;

	private static double width = 12;

	private static final String X_LABEL_1D = "Channels";

	private static final String Y_LABEL_1D = "Counts";

	private static void setPeakFind(final boolean which) {
		autoPeakFind = which;
	}

	protected static void setPeakFindDisplayCal(final boolean which) {
		pfcal = which;
	}

	protected static void setSensitivity(final double val) {
		sensitivity = val;
	}

	protected static void setWidth(final double val) {
		width = val;
	}

	private transient int areaMark1, areaMark2;

	/**
	 * Bin width to use when plotting (1D only).
	 */
	private double binWidth = 1.0;

	private transient final PlotColorMap colorMap = PlotColorMap.getInstance();

	/**
	 * 1D counts.
	 */
	private transient double[] counts;

	private transient double[] fitChannels, fitResiduals, fitBackground,
			fitTotal;

	private transient double[][] fitSignals;

	private transient final Object LOCK = new Object();

	/** clip to use when repainting for mouse movement, in graphics coordinates */
	private transient final Polygon mouseMoveClip = new Polygon();

	private transient final List<double[]> overlayCounts = Collections
			.synchronizedList(new ArrayList<double[]>());

	private transient final List<Integer> overlayNumber = Collections
			.synchronizedList(new ArrayList<Integer>());

	/**
	 * Constructor.
	 * 
	 */
	@Inject
	Plot1d(final PlotSelection plotSelection) {
		super(plotSelection);
		setPeakFind(PlotPreferences.PREFS.getBoolean(
				PlotPreferences.AUTO_PEAK_FIND, true));
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
			plotSelection.areaClip.add(painter.getRectangleOutline1d(bin1
					.getX(), bin2.getX()));
		}
	}

	private void clearMouseMoveClip() {
		synchronized (mouseMoveClip) {
			mouseMoveClip.reset();
		}
	}

	@Override
	protected void copyCounts(final AbstractHistogram hist) {
		final HistogramType type = hist.getType();
		size = new Size(hist.getSizeX(), hist.getSizeY());
		if (type == HistogramType.ONE_DIM_INT) {
			final int[] temp = ((HistInt1D) hist).getCounts();
			counts = NumberUtilities.getInstance().intToDoubleArray(temp);
		} else {// must be floating point
			counts = ((HistDouble1D) hist).getCounts();
		}
	}

	/**
	 * Displays a fit, starting
	 */
	@Override
	protected void displayFit(final double[][] signals,
			final double[] background, final double[] residuals,
			final int lowerLimit) {
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

	@Override
	protected void displayHistogram(final AbstractHistogram hist) {
		synchronized (LOCK) {
			if (hist == null) {
				counts = new double[100];
			}
			super.displayHistogram(hist);
		}
	}

	@Override
	protected void displaySetGate(final GateSetMode mode, final Bin pChannel,
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

	/**
	 * Find the maximum counts in the part of the histogram displayed
	 */
	@Override
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
	@Override
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

	protected double getBinWidth() {
		synchronized (LOCK) {
			return binWidth;
		}
	}

	/**
	 * Caller should have checked 'isCalibrated' first.
	 */
	@Override
	protected int getChannel(final double energy) {
		final AbstractHist1D plotHist = (AbstractHist1D) getHistogram();
		return (int) Math.round(plotHist.getCalibration().getChannel(energy));
	}

	/**
	 * Given a shape, return the bounding rectangle which includes all pixels in
	 * the rectangle bounding the given shape, plus some extra space given by
	 * the plot channels just outside the edge of this rectangle.
	 * 
	 * @param clipShape
	 *            the shape we want to cover with a rectangular clip region
	 * @param chanCoords
	 *            if <code>true</code>, the given shape is assumed given in
	 *            channel coordinates, otherwise it is assumed given in graphics
	 *            coordinates
	 * @return a bounding rectangle in the graphics coordinates
	 */
	private Rectangle getClipBounds(final Shape clipShape,
			final boolean chanCoords) {
		final Rectangle rval = clipShape.getBounds();
		if (chanCoords) {// shape is in channel coordinates
			/* add one more plot channel around the edges */
			/* now do conversion */
			rval.setBounds(painter.getRectangleOutline1d(rval.x - 2, (int) rval
					.getMaxX() + 2));
			rval.width += 1;
			rval.height += 1;
		} else {
			/*
			 * Shape is in view coordinates. Recursively call back with a
			 * polygon using channel coordinates.
			 */
			final Polygon shape = new Polygon();
			final Bin bin1 = painter.toData(rval.getLocation());
			final Bin bin2 = painter.toData(new Point(rval.x + rval.width,
					rval.y + rval.height));
			shape.addPoint(bin1.getX(), bin1.getY());
			shape.addPoint(bin2.getX(), bin2.getY());
			rval.setBounds(getClipBounds(shape, true));
		}
		return rval;
	}

	/**
	 * Get the counts in a X channel, Y channel ignored.
	 */
	@Override
	protected double getCount(final Bin bin) {
		return counts[bin.getX()];
	}

	/**
	 * Get the array of counts for the current histogram
	 */
	public Object getCounts() {
		return counts;
	}

	public int getDimensionality() {
		return 1;
	}

	/*
	 * non-javadoc: Caller should have checked 'isCalibrated' first.
	 */
	@Override
	protected double getEnergy(final double channel) {
		final AbstractHist1D plotHist = (AbstractHist1D) getHistogram();
		return plotHist.getCalibration().getValue(channel);
	}

	/**
	 * @param hOver
	 * @return
	 */
	private double[] getOverlayCounts(final AbstractHist1D hOver) {
		final int sizex = hOver.getSizeX();
		double[] ctOver;
		final HistogramType hoType = hOver.getType();
		if (hoType == HistogramType.ONE_DIM_INT) {
			final int[] countsInt = ((HistInt1D) hOver).getCounts();
			ctOver = NumberUtilities.getInstance().intToDoubleArray(countsInt);
		} else {// (hoType == Histogram.Type.ONE_D_DOUBLE)
			ctOver = new double[sizex];
			System.arraycopy(((HistDouble1D) hOver).getCounts(), 0, ctOver, 0,
					sizex);
		}
		return ctOver;
	}

	private boolean isMouseMoveClipClear() {
		synchronized (mouseMoveClip) {
			return mouseMoveClip.npoints == 0;
		}
	}

	/**
	 * Mark Area. The y-values are ignored.
	 * 
	 * @param bin1
	 *            one limit
	 * @param bin2
	 *            the other limit
	 */
	@Override
	public void markArea(final Bin bin1, final Bin bin2) {
		// While storing the boolean condition could make the code a
		// few lines more compact, this form makes the code analysis
		// engine happier about the null check and has the additional
		// benefit of reducing operations in the sychronized block.
		if (bin1 == null || bin2 == null) {
			synchronized (LOCK) {
				panel.setAreaMarked(false);
			}
		} else {
			final int xValue1 = bin1.getX();
			final int xValue2 = bin2.getX();
			synchronized (LOCK) {
				panel.setAreaMarked(true);
				areaMark1 = Math.min(xValue1, xValue2);
				areaMark2 = Math.max(xValue1, xValue2);
			}
		}
		panel.repaint();
	}

	/**
	 * Called when the mouse has moved
	 */
	@Override
	public void mouseMoved(final MouseEvent event) {
		panel.setMouseMoved(true);
		if (panel.isSelectingArea()) {
			if (isSelectingAreaClipClear()) {
				synchronized (plotSelection.areaClip) {
					plotSelection.areaClip.setBounds(painter
							.getRectangleOutline1d(plotSelection.start.getX(),
									lastMovePoint.x));
				}
			}
			setLastMovePoint(painter.toData(event.getPoint()).getPoint());
			addToSelectClip(plotSelection.start, Bin.create(lastMovePoint));
			synchronized (plotSelection.areaClip) {
				panel.repaint(getClipBounds(plotSelection.areaClip, false));
			}
		} else if (panel.isSettingGate() && pointsGate.npoints > 0) {
			/* draw new line */
			synchronized (lastMovePoint) {
				if (isMouseMoveClipClear()) {
					final Point point1 = painter.toViewLin(Bin.create(
							pointsGate.xpoints[pointsGate.npoints - 1],
							pointsGate.ypoints[pointsGate.npoints - 1]));
					addToMouseMoveClip(point1.x, point1.y);
					if (pointsGate.npoints > 1) {
						final Point point2 = painter.toViewLin(Bin.create(
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

	/**
	 * Overlay histograms.
	 */
	@Override
	protected void overlayHistograms(final List<AbstractHist1D> overlayHists) {
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

	/**
	 * paints a fit to a given graphics
	 */
	@Override
	public void paintFit(final Graphics graphics) {
		if (fitChannels != null && fitChannels.length > 0) {
			if (fitBackground != null) {
				graphics.setColor(colorMap.getFitBackground());
				painter.drawLine(fitChannels, fitBackground);
			}
			if (fitResiduals != null && fitResiduals.length > 0) {
				graphics.setColor(colorMap.getFitResidual());
				painter.drawLine(fitChannels, fitResiduals);
			}
			paintFitSignals(graphics);
			if (fitTotal != null && fitTotal.length > 0) {
				graphics.setColor(colorMap.getFitTotal());
				painter.drawLine(fitChannels, fitTotal);
			}
		}
	}

	/**
	 * @param graphics
	 */
	private void paintFitSignals(final Graphics graphics) {
		if (fitSignals != null) {
			graphics.setColor(colorMap.getFitSignal());
			for (int sig = 0; sig < fitSignals.length; sig++) {
				painter.drawLine(fitChannels, fitSignals[sig]);
			}
		}
	}

	/**
	 * Paint a gate on the give graphics object
	 */
	@Override
	public void paintGate(final Graphics graphics) {
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
		painter.drawGate1d(lowerLimit, upperLimit, noFill);
		graphics2D.setComposite(prev);
	}

	/**
	 * Draw the current histogram including title, border, tickmarks, tickmark
	 * labels and last but not least update the scrollbars
	 */
	@Override
	public void paintHistogram(final Graphics graphics) {
		final AbstractHistogram plotHist = this.getHistogram();
		if (plotHist.getDimensionality() == 1) {
			if (this.getBinWidth() > plotHist.getSizeX()) {
				this.setBinWidth(1.0);
				this
						.warning("Bin width > hist size, so setting bin width back to 1.");
			}
			graphics.setColor(colorMap.getHistogram());
			this.painter.drawHist(counts, getBinWidth());
			if (Plot1d.autoPeakFind) {
				this.painter.drawPeakLabels(((AbstractHist1D) plotHist)
						.findPeaks(Plot1d.sensitivity, Plot1d.width,
								Plot1d.pfcal));
			}
			/* draw ticks after histogram so they are on top */
			graphics.setColor(this.colorMap.getForeground());
			this.paintTextAndTicks(plotHist);
			final String axisLabelX = plotHist.getLabelX();
			this.painter.drawAxisLabel(axisLabelX == null ? Plot1d.X_LABEL_1D
					: axisLabelX, javax.swing.SwingConstants.BOTTOM);
			final String axisLabelY = plotHist.getLabelY();
			this.painter.drawAxisLabel(axisLabelY == null ? Plot1d.Y_LABEL_1D
					: axisLabelY, javax.swing.SwingConstants.LEFT);
		}
	}

	@Override
	public void paintMarkArea(final Graphics graphics) {
		final Graphics2D graphics2D = (Graphics2D) graphics;
		final Composite prev = graphics2D.getComposite();
		graphics2D.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.5f));
		graphics.setColor(colorMap.getArea());
		painter.update(graphics, viewSize, limits);
		painter.markArea1d(areaMark1, areaMark2, counts);
		graphics2D.setComposite(prev);
	}

	@Override
	public void paintMarkedChannels(final Graphics graphics) {
		graphics.setColor(colorMap.getMark());
		for (Bin bin : markedChannels) {
			final int xChannel = bin.getX();
			painter.markChannel1d(xChannel, counts[xChannel]);
		}
	}

	/**
	 * Draw a overlay of another data set
	 */
	@Override
	public void paintOverlay(final Graphics graphics) {
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
				painter.drawHist(overlayCounts.get(index), getBinWidth());
				index++;
			}
			final AbstractHistogram plotHist = getHistogram();
			painter.drawNumber(plotHist.getNumber(), overlayInts);
		}

	}

	@Override
	protected void paintSelectingArea(final Graphics graphics) {
		final Graphics2D graphics2D = (Graphics2D) graphics;
		graphics2D.setColor(colorMap.getArea());
		painter.markAreaOutline1d(plotSelection.start.getX(), lastMovePoint.x);
		panel.setMouseMoved(false);
		clearSelectingAreaClip();
	}

	@Override
	public void paintSetGatePoints(final Graphics graphics) {
		graphics.setColor(colorMap.getGateShow());
		painter.settingGate1d(painter.toView(pointsGate));
	}

	@Override
	protected void paintSettingGate(final Graphics graphics) {
		graphics.setColor(colorMap.getGateDraw());
		final int xValue1 = pointsGate.xpoints[pointsGate.npoints - 1];
		final int xValue2 = painter.toDataHorz(lastMovePoint.x);
		painter.markAreaOutline1d(xValue1, xValue2);
		panel.setMouseMoved(false);
		clearMouseMoveClip();
	}

	/**
	 * @see java.util.prefs.PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
	@Override
	public void preferenceChange(final PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		if (key.equals(PlotPreferences.AUTO_PEAK_FIND)) {
			setPeakFind(Boolean.parseBoolean(pce.getNewValue()));
		} else {
			super.preferenceChange(pce);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				panel.repaint();
			}
		});
	}

	@Override
	protected void removeOverlays() {
		overlayCounts.clear();
		overlayNumber.clear();
	}

	@Override
	protected void reset() {
		super.reset();
		setBinWidth(1.0);
	}

	/* Preferences */

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

	protected void setBinWidth(final double width) {
		synchronized (LOCK) {
			binWidth = width;
		}
	}

	private void warning(final String mess) {
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
