package jam.plot;

import jam.data.Histogram;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;

import javax.swing.SwingUtilities;

/**
 * Plots a 1-dimensional histogram.
 * 
 * @see jam.plot.Plot
 * @author Ken Swartz
 */
class Plot1d extends AbstractPlot {

	private double[] fitChannels, fitResiduals, fitBackground, fitTotal;

	private double[][] fitSignals;

	private int areaMark1, areaMark2;

	private List overlayHists;

	private Map countsOverlay = Collections.synchronizedMap(new HashMap());

	/**ss
	 * Constructor.
	 * 
	 * @param a
	 *            the
	 */
	Plot1d() {
		super();
		setPeakFind(prefs.getBoolean(AUTO_PEAK_FIND, true));
	}
	
	void setOverlayList(List list){
		overlayHists=list;
	}

	/**
	 * Overlay histograms.
	 */
	void overlayHistograms(List overlayHists) {
		displayingOverlay = true;
		/* retain any items in list in the map */
		countsOverlay.keySet().retainAll(overlayHists);
		final Iterator iter = overlayHists.iterator();
		while (iter.hasNext()) {
			final Integer key = (Integer) iter.next();
			final int num = key.intValue();
			final Histogram hOver = Histogram.getHistogram(num);
			final int sizex = hOver.getSizeX();
			final Object value = countsOverlay.get(key);
			final boolean create = value == null
					|| ((double[]) value).length != sizex;
			final double[] ctOver;
			if (create) {
				ctOver = new double[sizex];
				countsOverlay.put(key, ctOver);
			} else {
				ctOver = (double[]) value;
			}
			final Histogram.Type hoType = hOver.getType();
			if (hoType == Histogram.Type.ONE_DIM_INT) {
				final int[] countsInt = (int[]) hOver.getCounts();
				for (int j = 0; j < sizex; j++) {
					ctOver[j] = countsInt[j];
				}
			} else if (type == Histogram.Type.ONE_DIM_DOUBLE) {
				System.arraycopy(hOver.getCounts(), 0, ctOver, 0, sizex);
			}
		}
		repaint();
	}

	/**
	 * Show the making of a gate, point by point.
	 * 
	 * @param mode
	 *            GATE_NEW, GATE_CONTINUE, GATE_SAVE or GATE_CANCEL
	 * @param pChannel
	 *            channel coordinates of clicked channel
	 */
	void displaySetGate(GateSetMode mode, Bin pChannel, Point pPixel) {
		if (mode == GateSetMode.GATE_NEW) {
			pointsGate.reset();
			addMouseListener(mouseInputAdapter);
			addMouseMotionListener(mouseInputAdapter);
			setSettingGate(true);
		} else {
			if (mode == GateSetMode.GATE_CONTINUE) {
				pointsGate.addPoint(pChannel.getX(), pChannel.getY());
			} else if (mode == GateSetMode.GATE_SAVE) {
				pointsGate.reset();
				removeMouseListener(mouseInputAdapter);
				removeMouseMotionListener(mouseInputAdapter);
			} else if (mode == GateSetMode.GATE_CANCEL) {
				pointsGate.reset();
				setSettingGate(false);
				removeMouseListener(mouseInputAdapter);
				removeMouseMotionListener(mouseInputAdapter);
			}
			repaint();
		}
	}

	protected void paintSetGatePoints(Graphics g) {
		g.setColor(PlotColorMap.gateShow);
		graph.settingGate1d(graph.toView(pointsGate));
	}

	protected void paintSettingGate(Graphics g) {
		g.setColor(PlotColorMap.gateDraw);
		final int x1 = pointsGate.xpoints[pointsGate.npoints - 1];
		final int x2 = graph.toDataHorz(lastMovePoint.x);
		graph.markAreaOutline1d(x1, x2);
		setMouseMoved(false);
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
	void displayFit(double[][] signals, double[] background,
			double[] residuals, int ll) {
		this.fitBackground = null;
		this.fitChannels = null;
		this.fitResiduals = null;
		this.fitTotal = null;
		this.displayingFit = true;
		int length = 0;
		if (signals != null) {
			length = signals[0].length;
		} else {
			length = background.length;
		}
		fitChannels = new double[length];
		for (int i = 0; i < length; i++) {
			this.fitChannels[i] = ll + i + 0.5;
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
		if (residuals != null) {
			this.fitResiduals = new double[length];
			System.arraycopy(residuals, 0, fitResiduals, 0, length);
		}
		repaint();
	}

	protected void paintMarkedChannels(Graphics g) {
		g.setColor(PlotColorMap.mark);
		final Iterator it = markedChannels.iterator();
		while (it.hasNext()) {
			final int px = ((Bin) it.next()).getX();
			graph.markChannel1d(px, counts[px]);
		}
	}

	protected void paintSelectingArea(Graphics gc) {
		Graphics2D g = (Graphics2D) gc;
		g.setColor(PlotColorMap.area);
		graph.markAreaOutline1d(selectionStartPoint.getX(), lastMovePoint.x);
		setMouseMoved(false);
		clearSelectingAreaClip();
	}

	/**
	 * Mark Area. The y-values are ignored.
	 * 
	 * @param p1
	 *            one limit
	 * @param p2
	 *            the other limit
	 */
	public void markArea(Bin p1, Bin p2){
		synchronized (this) {
			markArea = (p1 != null) && (p2 != null);
			if (markArea) {
				final int x1 = p1.getX();
				final int x2 = p2.getX();
				areaMark1 = Math.min(x1, x2);
				areaMark2 = Math.max(x1, x2);
			}
		}
		repaint();
	}

	protected void paintMarkArea(Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		final Composite prev = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				0.5f));
		g.setColor(PlotColorMap.area);
		graph.update(g, viewSize, plotLimits);
		graph.markArea1d(areaMark1, areaMark2, counts);
		g2.setComposite(prev);
	}

	/**
	 * Draw the current histogram including title, border, tickmarks, tickmark
	 * labels and last but not least update the scrollbars
	 */
	protected void paintHistogram(Graphics g) {
		Histogram plotHist=getHistogram();
		if (plotHist.getDimensionality() != 1) {
			return;//not sure how this happens, but need to check
		}
		g.setColor(PlotColorMap.hist);
		graph.drawHist(counts, binWidth);
		if (autoPeakFind) {
			graph.drawPeakLabels(plotHist.findPeaks(sensitivity, width, pfcal));
		}
		/* draw ticks after histogram so they are on top */
		g.setColor(PlotColorMap.foreground);
		g.setColor(PlotColorMap.foreground);
		graph.drawTitle(plotHist.getTitle(), PlotGraphics.TOP);
		final int len = displayingOverlay ? overlayHists.size() : 0;
		final int[] overlays = new int[len];
		for (int i = 0; i < len; i++) {
			overlays[i] = ((Integer) overlayHists.get(i)).intValue();
		}
		graph.drawNumber(plotHist.getNumber(), overlays);
		graph.drawTicks(PlotGraphics.BOTTOM);
		graph.drawLabels(PlotGraphics.BOTTOM);
		graph.drawTicks(PlotGraphics.LEFT);
		graph.drawLabels(PlotGraphics.LEFT);
		final String axisLabelX = plotHist.getLabelX();
		if (axisLabelX != null) {
			graph.drawAxisLabel(axisLabelX, PlotGraphics.BOTTOM);
		} else {
			graph.drawAxisLabel(X_LABEL_1D, PlotGraphics.BOTTOM);
		}
		final String axisLabelY = plotHist.getLabelY();
		if (axisLabelY != null) {
			graph.drawAxisLabel(axisLabelY, PlotGraphics.LEFT);
		} else {
			graph.drawAxisLabel(Y_LABEL_1D, PlotGraphics.LEFT);
		}
	}

	private boolean autoPeakFind = true;

	private void setPeakFind(boolean which) {
		autoPeakFind = which;
	}


	/**
	 * Draw a overlay of another data set
	 */
	protected void paintOverlay(Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		/*
		 * I had compositing set here, but apparently, it's too many small draws
		 * using compositing, causing a slight performance issue.
		 */
		final Iterator iter = overlayHists.iterator();
		int i = 0;
		while (iter.hasNext()) {
			g2.setColor(PlotColorMap.overlay[i % PlotColorMap.overlay.length]);
			graph.drawHist((double[]) countsOverlay.get(iter.next()), binWidth);
			i++;
		}
	}

	/**
	 * Paint a gate on the give graphics object
	 */
	protected void paintGate(Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		Composite prev = g2.getComposite();
		final boolean noFill = isNoFillMode();
		if (!noFill) {
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					0.5f));
		}
		g.setColor(PlotColorMap.gateShow);
		int ll = currentGate.getLimits1d()[0];
		int ul = currentGate.getLimits1d()[1];
		graph.drawGate1d(ll, ul, noFill);
		g2.setComposite(prev);
	}

	/**
	 * paints a fit to a given graphics
	 */
	protected void paintFit(Graphics g) {
		if (fitChannels != null) {
			if (fitBackground != null) {
				g.setColor(PlotColorMap.fitBackground);
				graph.drawLine(fitChannels, fitBackground);
			}
			if (fitResiduals != null) {
				g.setColor(PlotColorMap.fitResidual);
				graph.drawLine(fitChannels, fitResiduals);
			}
			if (fitSignals != null) {
				g.setColor(PlotColorMap.fitSignal);
				for (int sig = 0; sig < fitSignals.length; sig++) {
					graph.drawLine(fitChannels, fitSignals[sig]);
				}
			}
			if (fitTotal != null) {
				g.setColor(PlotColorMap.fitTotal);
				graph.drawLine(fitChannels, fitTotal);
			}
		}
	}

	/**
	 * Get the counts in a X channel, Y channel ignored.
	 */
	protected double getCount(Bin p) {
		return counts[p.getX()];
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
	protected final int findMaximumCounts() {
		int chmax = plotLimits.getMaximumX();
		int chmin = plotLimits.getMinimumX();
		double maxCounts = 0;
		if ((chmin == 0) && (ignoreChZero)) {
			chmin = 1;
		}
		if ((chmax == (sizeX - 1)) && (ignoreChFull)) {
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
	protected final int findMinimumCounts() {
		int chmax = plotLimits.getMaximumX();
		int chmin = plotLimits.getMinimumX();
		int minCounts = 0;
		if ((chmin == 0) && (ignoreChZero)) {
			chmin = 1;
		}
		if ((chmax == (sizeX - 1)) && (ignoreChFull)) {
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
	 * Caller should have checked 'isCalibrated' first.
	 */
	public double getEnergy(double channel) {
		Histogram plotHist=getHistogram();
		return plotHist.getCalibration().getCalculatedEnergy(channel);
	}

	/**
	 * Caller should have checked 'isCalibrated' first.
	 */
	int getChannel(double energy) {
		Histogram plotHist=getHistogram();
		return (int) Math.round(plotHist.getCalibration()
				.getChannel(energy));
	}

	/**
	 * Called when the mouse has moved
	 */
	protected void mouseMoved(MouseEvent me) {
		setMouseMoved(true);
		if (selectingArea) {
			if (isSelectingAreaClipClear()) {
				synchronized (selectingAreaClip) {
					selectingAreaClip.setBounds(graph.getRectangleOutline1d(
							selectionStartPoint.getX(), lastMovePoint.x));
				}
			}
			setLastMovePoint(graph.toData(me.getPoint()).getPoint());
			addToSelectClip(selectionStartPoint, Bin.Factory
					.create(lastMovePoint));
			synchronized (selectingAreaClip) {
				repaint(getClipBounds(selectingAreaClip, false));
			}
		} else if (settingGate) {
			/* only if we have 1 or more */
			if (pointsGate.npoints > 0) {
				/* draw new line */
				synchronized (lastMovePoint) {
					if (isMouseMoveClipClear()) {
						final Point p1 = graph.toViewLin(Bin.Factory.create(
								pointsGate.xpoints[pointsGate.npoints - 1],
								pointsGate.ypoints[pointsGate.npoints - 1]));
						addToMouseMoveClip(p1.x, p1.y);
						if (pointsGate.npoints > 1) {
							final Point p2 = graph
									.toViewLin(Bin.Factory
											.create(
													pointsGate.xpoints[pointsGate.npoints - 2],
													pointsGate.ypoints[pointsGate.npoints - 2]));
							addToMouseMoveClip(p2.x, p2.y);
						}
						addToMouseMoveClip(lastMovePoint.x, lastMovePoint.y);
					}
					lastMovePoint.setLocation(me.getPoint());
					addToMouseMoveClip(lastMovePoint.x, lastMovePoint.y);
				}
				synchronized (mouseMoveClip) {
					repaint(getClipBounds(mouseMoveClip, false));
				}
			}
		}
	}

	private boolean isMouseMoveClipClear() {
		synchronized (mouseMoveClip) {
			return mouseMoveClip.npoints == 0;
		}
	}

	private void addToMouseMoveClip(int x, int y) {
		synchronized (mouseMoveClip) {
			mouseMoveClip.addPoint(x, y);
		}
	}

	/**
	 * Add to the selection clip region, using the two given plot-coordinates
	 * points to indicate the corners of a rectangular region of channels that
	 * needs to be included.
	 * 
	 * @param p1
	 *            in plot coordinates
	 * @param p2
	 *            in plot coordinates
	 */
	private final void addToSelectClip(Bin p1, Bin p2) {
		synchronized (selectingAreaClip) {
			selectingAreaClip.add(graph.getRectangleOutline1d(p1.getX(), p2
					.getX()));
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
	private Rectangle getClipBounds(Shape clipShape,
			boolean shapeInChannelCoords) {
		final Rectangle r = clipShape.getBounds();
		if (shapeInChannelCoords) {//shape is in channel coordinates
			/* add one more plot channel around the edges */
			/* now do conversion */
			r.setBounds(graph.getRectangleOutline1d(r.x - 2,
					(int) r.getMaxX() + 2));
			return r;
		} else {//shape is in view coordinates
			/*
			 * Recursively call back with a polygon using channel coordinates.
			 */
			final Polygon p = new Polygon();
			final Bin c1 = graph.toData(r.getLocation());
			final Bin c2 = graph
					.toData(new Point(r.x + r.width, r.y + r.height));
			p.addPoint(c1.getX(), c1.getY());
			p.addPoint(c2.getX(), c2.getY());
			return getClipBounds(p, true);
		}
	}

	public void preferenceChange(PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		if (key.equals(AUTO_PEAK_FIND)) {
			setPeakFind(Boolean.valueOf(pce.getNewValue()).booleanValue());
		} else {
			super.preferenceChange(pce);
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				repaint();
			}
		});
	}

}

