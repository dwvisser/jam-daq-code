package jam.plot;

import jam.data.AbstractHist1D;
import jam.data.Histogram;
import jam.plot.color.ColorPrefs;
import jam.plot.color.DiscreteColorScale;
import jam.plot.color.PlotColorMap;

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

final class Plot2d extends AbstractPlot implements ColorPrefs {

	/** last pixel point added to gate list */
	private transient final Point lastGatePoint = new Point();

	/** areaMark is a rectangle in channel space */
	private transient final Rectangle areaMark = new Rectangle();

	private transient final PlotColorMap plotColorMap = PlotColorMap
			.getInstance();

	private transient boolean smoothScale = true;

	private static final String X_LABEL_2D = "Channels";

	private static final String Y_LABEL_2D = "Channels";

	private transient final Object monitor = new Object();

	/**
	 * Creates a Plot object for displaying 2D histograms.
	 */
	Plot2d() {
		super();
		COLOR_PREFS.addPreferenceChangeListener(this);
		setSmoothColorScale(PREFS.getBoolean(ColorPrefs.SMOOTH_SCALE, true));
	}

	public void preferenceChange(final PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		if (key.equals(ColorPrefs.SMOOTH_SCALE)) {
			setSmoothColorScale(Boolean.valueOf(pce.getNewValue())
					.booleanValue());
		} else {
			super.preferenceChange(pce);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				panel.repaint();
			}
		});
	}

	private void setSmoothColorScale(final boolean bool) {
		synchronized (monitor) {
			smoothScale = bool;
		}
	}

	private boolean getSmoothColorScale() {
		synchronized (monitor) {
			return smoothScale;
		}
	}

	protected void paintMarkedChannels(final Graphics graphics) {
		graphics.setColor(plotColorMap.getMark());
		for (Bin bin : markedChannels) {
			graph.markChannel2d(bin);
		}
	}

	/**
	 * Paint call while selecting an area.
	 */
	protected void paintSelectingArea(final Graphics context) {
		final Graphics2D context2d = (Graphics2D) context;
		context2d.setColor(plotColorMap.getArea());
		synchronized (lastMovePoint) {
			graph.markArea2dOutline(selectStart, Bin.create(lastMovePoint));
		}
		panel.setMouseMoved(false);
		clearSelectingAreaClip();
	}

	/**
	 * Mark a rectangular area on the plot.
	 * 
	 * @param bin1
	 *            a corner of the rectangle in plot coordinates
	 * @param bin2
	 *            a corner of the rectangle in plot coordinates
	 */
	void markArea(final Bin bin1, final Bin bin2) {
		synchronized (monitor) {
			panel.setAreaMarked((bin1 != null) && (bin2 != null));
		}
		if (panel.isAreaMarked()) {
			synchronized (areaMark) {
				areaMark.setSize(0, 0);
				areaMark.setLocation(bin1.getPoint());
				areaMark.add(bin2.getPoint());
			}
		}
	}

	protected void paintMarkArea(final Graphics context) {
		final Graphics2D context2d = (Graphics2D) context;
		context2d.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.5f));
		context.setColor(plotColorMap.getArea());
		graph.clipPlot();
		synchronized (areaMark) {
			graph.markArea2d(graph.getRectangleOutline2d(areaMark));
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
	void displaySetGate(final GateSetMode mode, final Bin pChannel,
			final Point pPixel) {
		if (mode == GateSetMode.GATE_NEW) {
			panel.setSettingGate(true);
			pointsGate.reset();
			panel.setListenToMouse(true);
			panel.setListenToMouseMotion(true);
		} else if (mode == GateSetMode.GATE_CONTINUE) {
			pointsGate.addPoint(pChannel.getX(), pChannel.getY());
			/* update variables */
			final Point tempP = graph.toViewLin(pChannel);
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
					final Point tempP = graph.toViewLin(lpoint);
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

	protected void paintSetGatePoints(final Graphics context) {
		context.setColor(plotColorMap.getGateDraw());
		graph.settingGate2d(pointsGate);
	}

	/**
	 * Called by mouse movement while setting a gate
	 * 
	 * @param context
	 *            graphics context
	 */
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

	private void setLastGatePoint(final Point point) {
		synchronized (lastGatePoint) {
			lastGatePoint.setLocation(point);
		}
	}

	/**
	 * Get the counts for a particular channel.
	 * 
	 * @param point
	 *            the channel to get counts for
	 * @return the counts at channel <code>p</code>
	 */
	protected double getCount(final Bin point) {
		return counts2d[point.getX()][point.getY()];
	}

	/**
	 * Caller should have checked 'isCalibrated' first.
	 */
	double getEnergy(final double channel) {
		return 0.0;
	}

	/**
	 * Get the counts for the displayed 2d histogram.
	 * 
	 * @return the counts for the displayed 2d histogram
	 */
	protected Object getCounts() {
		return counts2d;
	}

	/**
	 * Get the maximum counts in the region of currently displayed 2d Histogram.
	 * 
	 * @return the maximum counts in the region of currently displayed 2d
	 *         Histogram
	 */
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

	private int getChannelMin(final int channel) {
		int rval = channel;
		if ((channel == 0) && (ignoreChZero)) {
			rval = 1;
		}
		return rval;
	}

	private int getChannelMax(final int channel, final int sizeIn) {
		int rval = channel;
		if (((channel == sizeIn - 1)) && (ignoreChFull)) {
			rval = sizeIn - 2;
		}
		return rval;
	}

	/**
	 * Get the minimum counts in the region of currently displayed 2d Histogram.
	 * 
	 * @return the minimum counts in the region of currently displayed 2d
	 *         Histogram
	 */
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

	// only used in one method, but don't want to keep creating
	private transient final Rectangle clipBounds = new Rectangle();// NOPMD

	/**
	 * Called to draw a 2d histogram, including title, border, tickmarks,
	 * tickmark labels and last but not least update the scrollbars.
	 * 
	 * @param context
	 *            the graphics context to paint to
	 */
	protected void paintHistogram(final Graphics context) {
		final Histogram plotHist = getHistogram();
		final Scale scale = limits.getScale();
		context.setColor(plotColorMap.getHistogram());
		context.getClipBounds(clipBounds);
		final int minX = graph.toDataHorz((int) clipBounds.getMinX());
		final int maxX = graph.toDataHorz((int) clipBounds.getMaxX());
		final int minY = graph.toDataVert((int) clipBounds.getMaxY());
		final int maxY = graph.toDataVert((int) clipBounds.getMinY());
		final DiscreteColorScale dcs = DiscreteColorScale.getScale(scale);
		if (getSmoothColorScale()) {
			graph.drawHist2d(counts2d, minX, minY, maxX, maxY);
			context.setPaintMode();
			context.setColor(plotColorMap.getForeground());
			graph.drawScale2d();
		} else {
			graph.drawHist2d(counts2d, minX, minY, maxX, maxY, dcs);
			context.setPaintMode();
			context.setColor(plotColorMap.getForeground());
			graph.drawScale2d(dcs);
		}
		/* draw labels/ticks after histogram so they are on top */
		context.setColor(plotColorMap.getForeground());
		graph.drawTitle(plotHist.getTitle(), PlotGraphics.TOP);
		graph.drawNumber(plotHist.getNumber(), new int[0]);
		graph.drawTicks(PlotGraphics.BOTTOM);
		graph.drawLabels(PlotGraphics.BOTTOM);
		graph.drawTicks(PlotGraphics.LEFT);
		graph.drawLabels(PlotGraphics.LEFT);
		final String axisLabelX = plotHist.getLabelX();
		if (axisLabelX == null) {
			graph.drawAxisLabel(X_LABEL_2D, PlotGraphics.BOTTOM);
		} else {
			graph.drawAxisLabel(axisLabelX, PlotGraphics.BOTTOM);
		}
		final String axisLabelY = plotHist.getLabelY();
		if (axisLabelY == null) {
			graph.drawAxisLabel(Y_LABEL_2D, PlotGraphics.LEFT);
		} else {
			graph.drawAxisLabel(axisLabelY, PlotGraphics.LEFT);
		}
		context.setPaintMode();
		context.setColor(plotColorMap.getForeground());
	}

	/**
	 * Paint a gate as a set of blocks that are channels in the gate.
	 * 
	 * @param graphics
	 *            the graphics context to paint to
	 */
	protected void paintGate(final Graphics graphics) {
		final Graphics2D graphics2d = (Graphics2D) graphics;
		graphics2d.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.5f));
		graphics2d.setColor(plotColorMap.getGateShow());
		if (isNoFillMode()) {
			paintPolyGate(graphics2d);
		} else {
			graph.drawGate2d(currentGate.getLimits2d());
		}
	}

	/**
	 * Paint a gate as a polygon.
	 * 
	 * @param context
	 *            the graphics context to paint to
	 * @throws DataException
	 *             if there's a problem painting the gate
	 */
	void paintPolyGate(final Graphics context) {
		context.setPaintMode();
		context.setColor(plotColorMap.getGateShow());
		final Polygon gatePoints = currentGate.getBananaGate();
		if (gatePoints != null) {
			final int numberPoints = gatePoints.npoints;
			if (numberPoints > 0) {// avoids negative array indices
				graph.clipPlot();
				final int lastI = numberPoints - 1;
				for (int i = 0; i < lastI; i++) {
					final int xval1 = gatePoints.xpoints[i];
					final int yval1 = gatePoints.ypoints[i];
					final int xval2 = gatePoints.xpoints[i + 1];
					final int yval2 = gatePoints.ypoints[i + 1];
					graph.drawDataLine(xval1, yval1, xval2, yval2);
				}
				if (gatePoints.xpoints[0] != gatePoints.xpoints[lastI]) {
					final int xval1 = gatePoints.xpoints[0];
					final int yval1 = gatePoints.ypoints[0];
					final int xval2 = gatePoints.xpoints[lastI];
					final int yval2 = gatePoints.ypoints[lastI];
					graph.drawDataLine(xval1, yval1, xval2, yval2);
				}
			}
		}
	}

	/**
	 * Paint a fit; not used in 2d.
	 * 
	 * @param context
	 *            the graphics context to paint on
	 */
	protected void paintFit(final Graphics context) {
		error("Cannot plot fits with 2D histograms.");
	}

	/**
	 * Paint an overlay; not used in 2d.
	 * 
	 * @param context
	 *            the graphics context to paint with
	 */
	protected void paintOverlay(final Graphics context) {
		error("Cannot plot overlays with 2D histograms.");
	}

	/**
	 * Mouse moved so update drawing gate or marking area as appropriate For
	 * gate undo last line draw, and draw a new line.
	 * 
	 * @param event
	 *            created when the mouse pointer moves while in the plot
	 */
	protected void mouseMoved(final MouseEvent event) {
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
					addToSelectClip(selectStart, Bin.create(lastMovePoint));
				}
				lastMovePoint.setLocation(graph.toData(event.getPoint())
						.getPoint());
				addToSelectClip(selectStart, Bin.create(lastMovePoint));
			}
			panel.setMouseMoved(true);
			synchronized (selectingAreaClip) {
				panel.repaint(getClipBounds(selectingAreaClip, false));
			}
		}
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
		synchronized (selectingAreaClip) {
			selectingAreaClip.add(graph.getRectangleOutline2d(bin1, bin2));
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
		Rectangle box = clipShape.getBounds();
		if (shapeInChannelCoords) {// shape is in channel coordinates
			/* add one more plot channel around the edges */
			box.add(box.x + box.width + 1, box.y + box.height + 1);
			box.add(box.x - 1, box.y - 1);
			final Bin bin1 = Bin.create(box.getLocation());
			final Bin bin2 = Bin
					.create(bin1.getX() + box.width, bin1.getY() + box.height);
			/* now do conversion */
			box.setBounds(graph.getRectangleOutline2d(bin1, bin2));
			box.width += 1;
			box.height += 1;
		} else {
			/*
			 * The shape is in view coordinates. Recursively call back with a
			 * polygon using channel coordinates.
			 */
			final Polygon shape = new Polygon();
			final Bin bin1 = graph.toData(box.getLocation());
			final Bin bin2 = graph
					.toData(new Point(box.x + box.width, box.y + box.height));
			shape.addPoint(bin1.getX(), bin1.getY());
			shape.addPoint(bin2.getX(), bin2.getY());
			box = getClipBounds(shape, true);
		}
		return box;
	}

	void displayFit(final double[][] signals, final double[] background,
			final double[] residuals, final int lowerLimit) {
		// NOP
	}

	void overlayHistograms(final List<AbstractHist1D> overlayHists) {
		// NOP
	}

	void removeOverlays() {
		// NOP
	}

	int getChannel(final double energy) {
		return 0;
	}

}