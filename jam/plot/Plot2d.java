package jam.plot;

import static jam.plot.color.ColorPrefs.COLOR_PREFS;
import jam.data.AbstractHist1D;
import jam.data.AbstractHistogram;
import jam.data.HistDouble2D;
import jam.data.HistInt2D;
import jam.data.HistogramType;
import jam.plot.color.ColorPrefs;
import jam.plot.color.DiscreteColorScale;
import jam.plot.color.PlotColorMap;
import jam.plot.common.Scale;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;

import javax.swing.SwingUtilities;

/**
 * Class to plot a 2-dimensional histogram.
 * 
 * @version 0.5
 * @author Ken Swartz
 */

final class Plot2d extends AbstractPlot {

	private static final double[][] EMPTY = new double[0][0];

	private static final String X_LABEL_2D = "Channels";

	private static final String Y_LABEL_2D = "Channels";

	/** areaMark is a rectangle in channel space */
	private transient final Rectangle areaMark = new Rectangle();

	// only used in one method, but don't want to keep creating
	private transient final Rectangle clipBounds = new Rectangle();// NOPMD

	private transient double[][] counts2d = EMPTY;

	/** last pixel point added to gate list */
	private transient final Point lastGatePoint = new Point();

	private transient final Object monitor = new Object();

	/** clip to use when repainting for mouse movement, in graphics coordinates */
	private transient final Polygon mouseMoveClip = new Polygon();

	private transient final PlotColorMap plotColorMap = PlotColorMap
			.getInstance();

	private transient boolean smoothScale = true;

	/**
	 * Creates a Plot object for displaying 2D histograms.
	 */
	Plot2d() {
		super();
		COLOR_PREFS.addPreferenceChangeListener(this);
		setSmoothColorScale(PlotPrefs.PREFS.getBoolean(ColorPrefs.SMOOTH_SCALE,
				true));
	}

	/**
	 * Add to the selection clip region, using the two given
	 * graphics-coordinates points to indicate the corners of a rectangular
	 * region of channels that needs to be included.
	 * 
	 * @param bin1
	 *            in plot coordinates
	 * @param bin2
	 *            in plot coordinates
	 */
	private void addToSelectClip(final Bin bin1, final Bin bin2) {
		synchronized (plotSelection.areaClip) {
			plotSelection.areaClip.add(painter
					.getRectangleOutline2d(bin1, bin2));
		}
	}

	@Override
	protected void copyCounts(final AbstractHistogram hist) {
		final HistogramType type = hist.getType();
		size = new Size(hist.getSizeX(), hist.getSizeY());
		counts2d = new double[size.getSizeX()][size.getSizeY()];
		if (type == HistogramType.TWO_DIM_INT) {
			copyCounts2dInt((HistInt2D) hist);
		} else {// must be floating point
			final double[][] counts2dDble = ((HistDouble2D) hist).getCounts();
			for (int i = 0; i < hist.getSizeX(); i++) {
				System.arraycopy(counts2dDble[i], 0, counts2d[i], 0, hist
						.getSizeY());
			}
		}
	}

	private void copyCounts2dInt(final HistInt2D hist) {
		final int[][] counts2dInt = hist.getCounts();
		for (int i = 0; i < hist.getSizeX(); i++) {
			for (int j = 0; j < hist.getSizeY(); j++) {
				counts2d[i][j] = counts2dInt[i][j];
			}
		}
	}

	@Override
	protected void displayFit(final double[][] signals,
			final double[] background, final double[] residuals,
			final int lowerLimit) {
		// NOP
	}

	@Override
	protected void displayHistogram(final AbstractHistogram hist) {
		synchronized (this) {
			if (hist == null) {
				counts2d = EMPTY;
			}
			super.displayHistogram(hist);
		}
	}

	/**
	 * Show the setting of a gate.
	 * 
	 * @param mode
	 *            one of GATE_NEW, GATE_CONTINUE, GATE_REMOVE, GATE_SAVE or
	 *            GATE_CANCEL
	 * @param pChannel
	 *            the channel coordinates of the point
	 * @param pPixel
	 *            the plot coordinates of the point
	 */
	@Override
	protected void displaySetGate(final GateSetMode mode, final Bin pChannel,
			final Point pPixel) {
		if (mode == GateSetMode.GATE_NEW) {
			panel.setSettingGate(true);
			pointsGate.reset();
			panel.setListenToMouse(true);
			panel.setListenToMouseMotion(true);
		} else if (mode == GateSetMode.GATE_CONTINUE) {
			pointsGate.addPoint(pChannel.getX(), pChannel.getY());
			/* update variables */
			final Point tempP = painter.toViewLin(pChannel);
			setLastGatePoint(tempP);
			if (pPixel == null) {
				setLastMovePoint(lastGatePoint);
			} else {
				setLastMovePoint(pPixel);
			}
			panel.repaint(getClipBounds(pointsGate, true));
		} else if (mode == GateSetMode.GATE_REMOVE) {
			if (pointsGate.npoints > 0) {
				/* decide clip before removing point */
				final Rectangle clip = getClipBounds(pointsGate, true);
				pointsGate.npoints--;// effectively removes last point
				if ((pointsGate.npoints > 0)) {// go back a point
					final int last = pointsGate.npoints - 1;
					final Bin lpoint = Bin.create(pointsGate.xpoints[last],
							pointsGate.ypoints[last]);
					/* update variables */
					final Point tempP = painter.toViewLin(lpoint);
					setLastGatePoint(tempP);
					setLastMovePoint(lastGatePoint);
				}
				panel.repaint(clip);
			}
		} else if (mode == GateSetMode.GATE_SAVE
				|| mode == GateSetMode.GATE_CANCEL) { // draw a saved gate
			/* decide clip before clearing pointsGate */
			final Rectangle clip = getClipBounds(pointsGate, true);
			panel.setSettingGate(false);
			pointsGate.reset();
			panel.setListenToMouse(false);
			panel.setListenToMouseMotion(false);
			panel.repaint(clip);
		}
	}

	/**
	 * Get the maximum counts in the region of currently displayed 2d Histogram.
	 * 
	 * @return the maximum counts in the region of currently displayed 2d
	 *         Histogram
	 */
	@Override
	protected int findMaximumCounts() {
		int chminX = limits.getMinimumX();
		int chmaxX = limits.getMaximumX();
		int chminY = limits.getMinimumY();
		int chmaxY = limits.getMaximumY();
		int maxCounts = 0;
		chminX = getChannelMin(chminX);
		chminY = getChannelMin(chminY);
		chmaxX = getChannelMax(chmaxX, size.getSizeX());
		chmaxY = getChannelMax(chmaxY, size.getSizeY());
		for (int i = chminX; i <= chmaxX; i++) {
			for (int j = chminY; j <= chmaxY; j++) {
				if (counts2d[i][j] > maxCounts) {
					maxCounts = (int) counts2d[i][j];
				}
			}
		}
		return maxCounts;
	}

	/**
	 * Get the minimum counts in the region of currently displayed 2d Histogram.
	 * 
	 * @return the minimum counts in the region of currently displayed 2d
	 *         Histogram
	 */
	@Override
	protected int findMinimumCounts() {
		int chminX = limits.getMinimumX();
		int chmaxX = limits.getMaximumX();
		int chminY = limits.getMinimumY();
		int chmaxY = limits.getMaximumY();
		int minCounts = 0;
		chminX = getChannelMin(chminX);
		chminY = getChannelMin(chminY);
		chmaxX = getChannelMax(chmaxX, size.getSizeX());
		chmaxY = getChannelMax(chmaxY, size.getSizeY());
		for (int i = chminX; i <= chmaxX; i++) {
			for (int j = chminY; j <= chmaxY; j++) {
				if (counts2d[i][j] < minCounts) {
					minCounts = (int) counts2d[i][j];
				}
			}
		}
		return minCounts;
	}

	@Override
	protected int getChannel(final double energy) {
		return 0;
	}

	private int getChannelMax(final int channel, final int sizeIn) {
		int rval = channel;
		if (((channel == sizeIn - 1)) && (options.isIgnoreChFull())) {
			rval = sizeIn - 2;
		}
		return rval;
	}

	private int getChannelMin(final int channel) {
		int rval = channel;
		if ((channel == 0) && (options.isIgnoreChZero())) {
			rval = 1;
		}
		return rval;
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
		Rectangle box = clipShape.getBounds();
		if (chanCoords) {// shape is in channel coordinates
			/* add one more plot channel around the edges */
			box.add(box.x + box.width + 1, box.y + box.height + 1);
			box.add(box.x - 1, box.y - 1);
			final Bin bin1 = Bin.create(box.getLocation());
			final Bin bin2 = Bin.create(bin1.getX() + box.width, bin1.getY()
					+ box.height);
			/* now do conversion */
			box.setBounds(painter.getRectangleOutline2d(bin1, bin2));
			box.width += 1;
			box.height += 1;
		} else {
			/*
			 * The shape is in view coordinates. Recursively call back with a
			 * polygon using channel coordinates.
			 */
			final Polygon shape = new Polygon();
			final Bin bin1 = painter.toData(box.getLocation());
			final Bin bin2 = painter.toData(new Point(box.x + box.width, box.y
					+ box.height));
			shape.addPoint(bin1.getX(), bin1.getY());
			shape.addPoint(bin2.getX(), bin2.getY());
			box = getClipBounds(shape, true);
		}
		return box;
	}

	/**
	 * Get the counts for a particular channel.
	 * 
	 * @param point
	 *            the channel to get counts for
	 * @return the counts at channel <code>p</code>
	 */
	@Override
	protected double getCount(final Bin point) {
		return counts2d[point.getX()][point.getY()];
	}

	/**
	 * Get the counts for the displayed 2d histogram.
	 * 
	 * @return the counts for the displayed 2d histogram
	 */
	public Object getCounts() {
		return counts2d;
	}

	public int getDimensionality() {
		return 2;
	}

	/**
	 * Caller should have checked 'isCalibrated' first.
	 */
	@Override
	protected double getEnergy(final double channel) {
		return 0.0;
	}

	private boolean isSmoothColorScale() {
		synchronized (monitor) {
			return smoothScale;
		}
	}

	/**
	 * Mark a rectangular area on the plot.
	 * 
	 * @param bin1
	 *            a corner of the rectangle in plot coordinates
	 * @param bin2
	 *            a corner of the rectangle in plot coordinates
	 */
	@Override
	protected void markArea(final Bin bin1, final Bin bin2) {
		// While storing the boolean condition could make the code a
		// few lines more compact, this form makes the code analysis
		// engine happier about the null check and has the additional
		// benefit of reducing operations in the sychronized block.
		if (bin1 == null || bin2 == null) {
			synchronized (monitor) {
				panel.setAreaMarked(false);
			}
		} else {
			synchronized (monitor) {
				panel.setAreaMarked(true);
			}
			synchronized (areaMark) {
				areaMark.setSize(0, 0);
				areaMark.setLocation(bin1.getPoint());
				areaMark.add(bin2.getPoint());
			}
		}
	}

	/**
	 * Mouse moved so update drawing gate or marking area as appropriate For
	 * gate undo last line draw, and draw a new line.
	 * 
	 * @param event
	 *            created when the mouse pointer moves while in the plot
	 */
	@Override
	public void mouseMoved(final MouseEvent event) {
		if (panel.isSettingGate()) {
			/* only if we have 1 or more */
			if (pointsGate.npoints > 0) {
				/* draw new line */
				synchronized (lastMovePoint) {
					if (mouseMoveClip.npoints == 0) {
						mouseMoveClip
								.addPoint(lastGatePoint.x, lastGatePoint.y);
						mouseMoveClip
								.addPoint(lastMovePoint.x, lastMovePoint.y);
					}
					lastMovePoint.setLocation(event.getPoint());
					mouseMoveClip.addPoint(lastMovePoint.x, lastMovePoint.y);
				}
				panel.setMouseMoved(true);
				panel.repaint(getClipBounds(mouseMoveClip, false));
			}
		} else if (panel.isSelectingArea()) {
			synchronized (lastMovePoint) {
				if (isSelectingAreaClipClear()) {
					addToSelectClip(plotSelection.start, Bin
							.create(lastMovePoint));
				}
				lastMovePoint.setLocation(painter.toData(event.getPoint())
						.getPoint());
				addToSelectClip(plotSelection.start, Bin.create(lastMovePoint));
			}
			panel.setMouseMoved(true);
			synchronized (plotSelection.areaClip) {
				panel.repaint(getClipBounds(plotSelection.areaClip, false));
			}
		}
	}

	@Override
	protected void overlayHistograms(final List<AbstractHist1D> overlayHists) {
		// NOP
	}

	/**
	 * Paint a fit; not used in 2d.
	 * 
	 * @param context
	 *            the graphics context to paint on
	 */
	@Override
	public void paintFit(final Graphics context) {
		error("Cannot plot fits with 2D histograms.");
	}

	/**
	 * Paint a gate as a set of blocks that are channels in the gate.
	 * 
	 * @param graphics
	 *            the graphics context to paint to
	 */
	@Override
	public void paintGate(final Graphics graphics) {
		final Graphics2D graphics2d = (Graphics2D) graphics;
		graphics2d.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.5f));
		graphics2d.setColor(plotColorMap.getGateShow());
		if (options.isNoFillMode()) {
			paintPolyGate(graphics2d);
		} else {
			painter.drawGate2d(currentGate.getLimits2d());
		}
	}

	/**
	 * Called to draw a 2d histogram, including title, border, tickmarks,
	 * tickmark labels and last but not least update the scrollbars.
	 * 
	 * @param context
	 *            the graphics context to paint to
	 */
	@Override
	public void paintHistogram(final Graphics context) {
		final AbstractHistogram plotHist = getHistogram();
		final Scale scale = limits.getScale();
		context.setColor(plotColorMap.getHistogram());
		context.getClipBounds(clipBounds);
		final int minX = painter.toDataHorz((int) clipBounds.getMinX());
		final int maxX = painter.toDataHorz((int) clipBounds.getMaxX());
		final int minY = painter.toDataVert((int) clipBounds.getMaxY());
		final int maxY = painter.toDataVert((int) clipBounds.getMinY());
		final DiscreteColorScale dcs = DiscreteColorScale.getScale(scale);
		if (isSmoothColorScale()) {
			painter.drawHist2d(counts2d, minX, minY, maxX, maxY);
			context.setPaintMode();
			context.setColor(plotColorMap.getForeground());
			painter.drawScale2d();
		} else {
			painter.drawHist2d(counts2d, minX, minY, maxX, maxY, dcs);
			context.setPaintMode();
			context.setColor(plotColorMap.getForeground());
			painter.drawScale2d(dcs);
		}
		/* draw labels/ticks after histogram so they are on top */
		context.setColor(plotColorMap.getForeground());
		paintTextAndTicks(plotHist);
		final String axisLabelX = plotHist.getLabelX();
		painter.drawAxisLabel(axisLabelX == null ? X_LABEL_2D : axisLabelX,
				javax.swing.SwingConstants.BOTTOM);
		final String axisLabelY = plotHist.getLabelY();
		painter.drawAxisLabel(axisLabelY == null ? Y_LABEL_2D : axisLabelY,
				javax.swing.SwingConstants.LEFT);
		context.setPaintMode();
		context.setColor(plotColorMap.getForeground());
	}

	@Override
	public void paintMarkArea(final Graphics context) {
		final Graphics2D context2d = (Graphics2D) context;
		context2d.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.5f));
		context.setColor(plotColorMap.getArea());
		painter.clipPlot();
		synchronized (areaMark) {
			painter.markArea2d(painter.getRectangleOutline2d(areaMark));
		}
	}

	@Override
	public void paintMarkedChannels(final Graphics graphics) {
		graphics.setColor(plotColorMap.getMark());
		for (Bin bin : markedChannels) {
			painter.markChannel2d(bin);
		}
	}

	/**
	 * Paint an overlay; not used in 2d.
	 * 
	 * @param context
	 *            the graphics context to paint with
	 */
	@Override
	public void paintOverlay(final Graphics context) {
		error("Cannot plot overlays with 2D histograms.");
	}

	/**
	 * Paint a gate as a polygon.
	 * 
	 * @param context
	 *            the graphics context to paint to
	 */
	protected void paintPolyGate(final Graphics context) {
		context.setPaintMode();
		context.setColor(plotColorMap.getGateShow());
		final Polygon gatePoints = currentGate.getBananaGate();
		if (gatePoints != null) {
			final int numberPoints = gatePoints.npoints;
			if (numberPoints > 0) {// avoids negative array indices
				painter.clipPlot();
				final int lastI = numberPoints - 1;
				for (int i = 0; i < lastI; i++) {
					final int xval1 = gatePoints.xpoints[i];
					final int yval1 = gatePoints.ypoints[i];
					final int xval2 = gatePoints.xpoints[i + 1];
					final int yval2 = gatePoints.ypoints[i + 1];
					painter.drawDataLine(xval1, yval1, xval2, yval2);
				}
				if (gatePoints.xpoints[0] != gatePoints.xpoints[lastI]) {
					final int xval1 = gatePoints.xpoints[0];
					final int yval1 = gatePoints.ypoints[0];
					final int xval2 = gatePoints.xpoints[lastI];
					final int yval2 = gatePoints.ypoints[lastI];
					painter.drawDataLine(xval1, yval1, xval2, yval2);
				}
			}
		}
	}

	/**
	 * Paint call while selecting an area.
	 */
	@Override
	protected void paintSelectingArea(final Graphics context) {
		final Graphics2D context2d = (Graphics2D) context;
		context2d.setColor(plotColorMap.getArea());
		synchronized (lastMovePoint) {
			painter.markArea2dOutline(plotSelection.start, Bin
					.create(lastMovePoint));
		}
		panel.setMouseMoved(false);
		clearSelectingAreaClip();
	}

	@Override
	public void paintSetGatePoints(final Graphics context) {
		context.setColor(plotColorMap.getGateDraw());
		painter.settingGate2d(pointsGate);
	}

	/**
	 * Called by mouse movement while setting a gate
	 * 
	 * @param context
	 *            graphics context
	 */
	@Override
	protected void paintSettingGate(final Graphics context) {
		final Graphics2D context2d = (Graphics2D) context;
		context2d.setColor(plotColorMap.getGateDraw());
		context2d.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.8f));
		synchronized (lastMovePoint) {
			context2d.drawLine(lastGatePoint.x, lastGatePoint.y,
					lastMovePoint.x, lastMovePoint.y);
		}
		panel.setMouseMoved(false);
		mouseMoveClip.reset();
	}

	@Override
	public void preferenceChange(final PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		if (key.equals(ColorPrefs.SMOOTH_SCALE)) {
			setSmoothColorScale(Boolean.parseBoolean(pce.getNewValue()));
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
		// NOP
	}

	private void setLastGatePoint(final Point point) {
		synchronized (lastGatePoint) {
			lastGatePoint.setLocation(point);
		}
	}

	private void setSmoothColorScale(final boolean bool) {
		synchronized (monitor) {
			smoothScale = bool;
		}
	}

}