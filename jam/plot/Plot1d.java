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
import java.util.Iterator;
import java.util.prefs.PreferenceChangeEvent;

/**
 * Plots a 1-dimensional histogram.
 *
 * @see jam.plot.Plot
 * @author Ken Swartz
 */
class Plot1d extends Plot {

	private double[] fitChannels, fitResiduals, fitBackground, fitTotal;
	private double[][] fitSignals;
	private int areaMark1, areaMark2;
	private Histogram [] overlayHists;
	private double[][] countsOverlay;

	/**
	 * Constructor
	 */
	Plot1d(Action a) {
		super(a);
		setPeakFind(prefs.getBoolean(AUTO_PEAK_FIND,true));
	}

	/**
	 * Overlay histograms.
	 */
	void overlayHistograms(Histogram [] hists) {
		displayingOverlay = true;
		overlayHists = hists;
		final int len=hists.length;
		countsOverlay=new double[len][];
		for (int i=0; i<len; i++){
			final Histogram hOver=hists[i];
			final int sizex = hOver.getSizeX();
			countsOverlay[i] = new double[sizex];
			if (hOver.getType() == Histogram.ONE_DIM_INT) {
				final int[] countsInt = (int[]) hOver.getCounts();
				for (int j = 0; j < sizex; j++) {
					countsOverlay[i][j] = countsInt[j];
				}
			} else if (hOver.getType() == Histogram.ONE_DIM_DOUBLE) {
				final double[] countsDble = (double[]) hOver.getCounts();
				System.arraycopy(countsDble, 0, countsOverlay[i], 0, sizex);
			}
		}
		repaint();
	}

	/**
	 * Show the making of a gate, point by point.
	 * 
	 * @param mode GATE_NEW, GATE_CONTINUE, GATE_SAVE or GATE_CANCEL
	 * @param pChannel channel coordinates of clicked channel
	 */
	void displaySetGate(GateSetMode mode, Point pChannel, Point pPixel) {
		if (mode == GateSetMode.GATE_NEW) {
			pointsGate.reset();
			addMouseListener(mouseInputAdapter);
			addMouseMotionListener(mouseInputAdapter);
			setSettingGate(true);
		} else {
			if (mode == GateSetMode.GATE_CONTINUE) {
				pointsGate.addPoint(pChannel.x, pChannel.y);
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
	
	protected void paintSettingGate(Graphics g){
		g.setColor(PlotColorMap.gateDraw);
		final int x1=pointsGate.xpoints[pointsGate.npoints-1];
		final int x2=graph.toDataHorz(lastMovePoint.x);
		graph.markAreaOutline1d(x1,x2);
		setMouseMoved(false);
		clearMouseMoveClip();
	}
	
	private void clearMouseMoveClip(){
		synchronized (mouseMoveClip){
			mouseMoveClip.reset();
		}
	}
	
	/**
	 * Displays a fit, starting
	 */
	void displayFit(
		double[][] signals,
		double[] background,
		double[] residuals,
		int ll) {
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
				System.arraycopy(
					signals[sig],
					0,
					this.fitSignals[sig],
					0,
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
			final Point p = (Point) it.next();
			graph.markChannel1d(p.x, counts[p.x]);
		}
	}

	protected void paintSelectingArea(Graphics gc) {
		Graphics2D g = (Graphics2D) gc;
		g.setColor(PlotColorMap.area);
		graph.markAreaOutline1d(selectionStartPoint.x, lastMovePoint.x);
		setMouseMoved(false);
		clearSelectingAreaClip();
	}

	/**
	 * Mark Area. The y-values are ignored.
	 * 
	 * @param minChanX the lower x channel
	 * @param minchanY the lower y channel
	 * @param maxChanX the upper x channel
	 * @param maxchanY the upper y channel
	 */
	void markArea(Point p1, Point p2) {
		synchronized (this) {
			markArea = (p1 != null) && (p2 != null);
			if (markArea) {
				areaMark1 = Math.min(p1.x, p2.x);
				areaMark2 = Math.max(p1.x, p2.x);
			}
		}
		repaint();
	}

	protected void paintMarkArea(Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		final Composite prev=g2.getComposite();
		g2.setComposite(
			AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
		g.setColor(PlotColorMap.area);
		graph.update(g, viewSize, plotLimits);
		graph.markArea1d(areaMark1, areaMark2, counts);
		g2.setComposite(prev);
	}

	/**
	 * Draw the current histogram
	 * including title, border, tickmarks, tickmark labels
	 * and last but not least update the scrollbars
	 */
	protected void paintHistogram(Graphics g) {
		g.setColor(PlotColorMap.hist);
		graph.drawHist(counts, binWidth);
		if (autoPeakFind) {
			graph.drawPeakLabels(
				currentHist.findPeaks(sensitivity, width, pfcal));
		}
		/* draw ticks after histogram so they are on top */
		g.setColor(PlotColorMap.foreground);
		g.setColor(PlotColorMap.foreground);
		graph.drawTitle(title, PlotGraphics.TOP);
		//final int nOverlay = displayingOverlay ? overlayHists[0].getNumber() : -1;
		final int len=displayingOverlay ? overlayHists.length : 0;
		final int [] overlays=new int[len];
		for (int i=0; i<len; i++){
			overlays[i]=overlayHists[i].getNumber();
		}
		graph.drawNumber(number, overlays);
		graph.drawTicks(PlotGraphics.BOTTOM);
		graph.drawLabels(PlotGraphics.BOTTOM);
		graph.drawTicks(PlotGraphics.LEFT);
		graph.drawLabels(PlotGraphics.LEFT);
		if (axisLabelX != null) {
			graph.drawAxisLabel(axisLabelX, PlotGraphics.BOTTOM);
		} else {
			graph.drawAxisLabel(X_LABEL_1D, PlotGraphics.BOTTOM);
		}
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

	private double sensitivity = 3;
	void setSensitivity(double val) {
		sensitivity = val;
	}

	private double width = 12;
	void setWidth(double val) {
		width = val;
	}

	private boolean pfcal = true;
	void setPeakFindDisplayCal(boolean which) {
		pfcal = which;
	}

	/**
	 * Draw a overlay of another data set
	 */
	protected void paintOverlay(Graphics g) {
		final Graphics2D g2=(Graphics2D)g;
		final Composite prev=g2.getComposite();
		g2.setComposite(
			AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
		final int len=countsOverlay.length;
		for (int i=0; i<len; i++){
			g2.setColor(PlotColorMap.overlay[i%PlotColorMap.overlay.length]);
			graph.drawHist(countsOverlay[i], binWidth);
		}
		g2.setComposite(prev);
	}

	/**
	 * Paint a gate on the give graphics object
	 */
	protected void paintGate(Graphics g) {
		final Graphics2D g2 = (Graphics2D) g;
		Composite prev=g2.getComposite();
		final boolean noFill=isNoFillMode();
		if (!noFill) {
			g2.setComposite(
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
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
	 * Get the counts in a X channel,
	 * Y channel ignored.
	 */
	double getCount(Point p) {
		return counts[p.x];
	}

	/**
	 * Get the array of counts for the current histogram
	 */
	double[] getCounts() {
		return counts;
	}

	/**
	 * Find the maximum counts in the part of the histogram displayed
	 */
	protected final int findMaximumCounts() {
		int chmax = plotLimits.getMaximumX();
		int chmin = plotLimits.getMinimumX();
		int maxCounts = 0;
		if ((chmin == 0) && (ignoreChZero)) {
			chmin = 1;
		}
		if ((chmax == (sizeX - 1)) && (ignoreChFull)) {
			chmax = sizeX - 2;
		}
		for (int i = chmin; i <= chmax; i++) {
			if (counts[i] > maxCounts) {
				maxCounts = (int) counts[i]; //FIXME
			}
		}
		return (maxCounts);
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
	double getEnergy(double channel) {
		return currentHist.getCalibration().getCalculatedEnergy(channel);
	}

	/**
	 * Caller should have checked 'isCalibrated' first.
	 */
	int getChannel(double energy) {
		return (int) Math.round(
			currentHist.getCalibration().getChannel(energy));
	}

	/**
	 * Called when the mouse has moved
	 */
	protected void mouseMoved(MouseEvent me) {
		setMouseMoved(true);
		if (selectingArea) {
			if (isSelectingAreaClipClear()) {
				synchronized(selectingAreaClip){
					selectingAreaClip.setBounds(graph.getRectangleOutline1d(
					selectionStartPoint.x,lastMovePoint.x));
				}
				//addToSelectClip(selectionStartPoint, lastMovePoint);
			}
			setLastMovePoint(graph.toData(me.getPoint()));
			addToSelectClip(selectionStartPoint, lastMovePoint);
			synchronized (selectingAreaClip) {
				repaint(getClipBounds(selectingAreaClip, false));
			}
		} else if (settingGate){
			/* only if we have 1 or more */
			if (pointsGate.npoints > 0) {
				/* draw new line */
				synchronized (lastMovePoint){
					if (isMouseMoveClipClear()) {
						final Point p1=graph.toViewLin(new Point(pointsGate.xpoints[pointsGate.npoints-1], 
						pointsGate.ypoints[pointsGate.npoints-1]));
						addToMouseMoveClip(p1.x,p1.y);
						if (pointsGate.npoints>1){
							final Point p2=graph.toViewLin(new Point(pointsGate.xpoints[pointsGate.npoints-2], 
							pointsGate.ypoints[pointsGate.npoints-2]));
							addToMouseMoveClip(p2.x,p2.y);							
						}
						addToMouseMoveClip(lastMovePoint.x, lastMovePoint.y);
					}
					lastMovePoint.setLocation(me.getPoint());
					addToMouseMoveClip(lastMovePoint.x, lastMovePoint.y);
				}
				synchronized (mouseMoveClip){
					repaint(getClipBounds(mouseMoveClip,false));
				}
			}
		}
	}
	
	private boolean isMouseMoveClipClear(){
		synchronized(mouseMoveClip){
			return mouseMoveClip.npoints == 0;
		}
	}
	
	private void addToMouseMoveClip(int x, int y){
		synchronized(mouseMoveClip){
			mouseMoveClip.addPoint(x,y);
		}
	}

	/**
	 * Add to the selection clip region, using the two 
	 * given graphics-coordinates points to indicate the corners
	 * of a rectangular region of channels that needs to be included.
	 * 
	 * @param p1 in graphics coordinates
	 * @param p2 in graphics coordinates
	 */
	private final void addToSelectClip(Point p1, Point p2) {
		synchronized (selectingAreaClip) {
			selectingAreaClip.add(graph.getRectangleOutline1d(p1.x, p2.x));
		}
	}
	
	/**
	 * Given a shape, return the bounding rectangle which includes
	 * all pixels in the rectangle bounding the given shape, plus
	 * some extra space given by the plot channels just outside the
	 * edge of this rectangle.
	 * 
	 * @param clipShape the shape we want to cover with a 
	 * rectangular clip region
	 * @param shapeInChannelCoords if <code>true</code>, the given shape
	 * is assumed given in channel coordinates, otherwise it is assumed
	 * given in graphics coordinates
	 * @return a bounding rectangle in the graphics coordinates
	 */
	private Rectangle getClipBounds(Shape clipShape, boolean shapeInChannelCoords){
		final Rectangle r=clipShape.getBounds();
		if (shapeInChannelCoords){//shape is in channel coordinates
			/* add one more plot channel around the edges */
			/* now do conversion */
			r.setBounds(graph.getRectangleOutline1d(r.x-2,(int)r.getMaxX()+2));
			return r;
		} else {//shape is in view coordinates
			/* Recursively call back with a polygon using channel
			 * coordinates. */
			final Polygon p=new Polygon();
			final Point p1=graph.toData(r.getLocation());
			final Point p2=graph.toData(new Point(r.x+r.width,r.y+r.height));
			p.addPoint(p1.x,p1.y);
			p.addPoint(p2.x,p2.y);
			return getClipBounds(p,true);
		}
	}
	
	public void preferenceChange(PreferenceChangeEvent pce){
		final String key=pce.getKey();
		if (key.equals(AUTO_PEAK_FIND)){
			setPeakFind(Boolean.valueOf(pce.getNewValue()).booleanValue());
		} else {
			super.preferenceChange(pce);
		}
		repaint();
	}

}

