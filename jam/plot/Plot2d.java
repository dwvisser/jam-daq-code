package jam.plot;
import jam.global.JamProperties;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.util.Iterator;

/**
 * Class to plot a 2-dimensional histogram.
 *
 * @version 0.5
 * @author Ken Swartz
 */

class Plot2d extends Plot {

	/** last data gate point */
	private final Point lastPoint = new Point();

	/** last pixel point added to gate list */
	private final Point lastGatePoint = new Point();

	/** areaMark is a rectangle in channel space */
	private final Rectangle areaMark = new Rectangle();

	/**
	 * Creates a Plot object for displaying 2D histograms.
	 * 
	 * @param a the action toolbar
	 */
	Plot2d(Action a) {
		super(a);
	}
	
	protected void paintMarkedChannels(Graphics g){
		g.setColor(PlotColorMap.mark);
		final Iterator it=markedChannels.iterator();
		while (it.hasNext()){
			final Point p=(Point)it.next();
			graph.markChannel2d(p);
		}
	}
	
	/**
	 * Paint call while selecting an area.
	 */
	protected void paintSelectingArea(Graphics gc) {
		final Graphics2D g=(Graphics2D)gc;
		g.setColor(PlotColorMap.area);	
		synchronized (lastMovePoint){	
			graph.markArea2dOutline(selectionStartPoint,lastMovePoint);
		}
		setMouseMoved(false);	
		clearSelectingAreaClip();
	}
	
	/**
	 * Mark a rectangular area on the plot.
	 *
	 * @param p1 a corner of the rectangle in plot coordinates
	 * @param p2 a corner of the rectangle in plot coordinates
	 */
	void markArea(Point p1, Point p2) {
		synchronized (this) {
			markArea = (p1 != null) && (p2 != null);
		}
		if (markArea) {
			synchronized (areaMark){
				areaMark.setSize(0,0);
				areaMark.setLocation(p1);
				areaMark.add(p2);
			}
		}
	}

	protected void paintMarkArea(Graphics g) {
		final Graphics2D g2=(Graphics2D)g;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
		0.5f));
		g.setColor(PlotColorMap.area);
		graph.clipPlot();
		synchronized(areaMark){
			graph.markArea2d(graph.getRectangleOutline2d(areaMark));
		}
	}

	private void setLastPoint(Point lp) {
		synchronized (lastPoint) {
			lastPoint.setLocation(lp);
		}
	}
	
	/**
	 * Show the setting of a gate.
	 *
	 * @param mode one of GATE_NEW, GATE_CONTINUE, GATE_REMOVE, 
	 * GATE_SAVE or GATE_CANCEL
	 * @param pChannel the channel coordinates of the point
	 * @param pPixel the plot coordinates of the point
	 */
	void displaySetGate(
		GateSetMode mode,
		Point pChannel,
		Point pPixel) {
		if (mode == GateSetMode.GATE_NEW) {
			setSettingGate(true);
			pointsGate.reset();
			addMouseListener(mouseInputAdapter);
			addMouseMotionListener(mouseInputAdapter);
		} else if (mode == GateSetMode.GATE_CONTINUE) {
			pointsGate.addPoint(pChannel.x,pChannel.y);
			setLastPoint(pChannel); //save data point
			/* update variables */
			final Point tempP = graph.toViewLin(pChannel);
			setLastGatePoint(tempP);
			if (pPixel != null) {
				setLastMovePoint(pPixel);
			} else {
				setLastMovePoint(lastGatePoint);
			}
			repaint(getClipBounds(pointsGate,true));
		} else if (mode == GateSetMode.GATE_REMOVE) {
			if (pointsGate.npoints>0) {
				/* decide clip before removing point */
				final Rectangle clip=getClipBounds(pointsGate,true);
				pointsGate.npoints--;//effectively removes last point
				if ((pointsGate.npoints > 0)) {//go back a point 
					final int last=pointsGate.npoints-1;
					final Point lpoint=new Point(pointsGate.xpoints[last],
					pointsGate.ypoints[last]);
					setLastPoint(lpoint);
					/* update variables */
					final Point tempP =
						graph.toViewLin(lpoint);
					setLastGatePoint(tempP);
					setLastMovePoint(lastGatePoint);
				}
				repaint(clip);
			}
		} else if (mode == GateSetMode.GATE_SAVE ||
		mode==GateSetMode.GATE_CANCEL) { //draw a saved gate
			/* decide clip before clearing pointsGate */
			final Rectangle clip=getClipBounds(pointsGate,true);
			setSettingGate(false);
			pointsGate.reset();
			removeMouseListener(mouseInputAdapter);
			removeMouseMotionListener(mouseInputAdapter);
			repaint(clip);
		} 
	}

	protected void paintSetGatePoints(Graphics g) {
		g.setColor(PlotColorMap.gateDraw);
		graph.settingGate2d(pointsGate);
	}
	
	/**
	 * Called by mouse movement while setting a gate
	 * @param g
	 */	
	protected void paintSettingGate(Graphics gc) {
		//gc.setColor(PlotColorMap.gateDraw);
		//graph.settingGate2d(pointsGate);
		final Graphics2D g=(Graphics2D)gc;
		g.setColor(PlotColorMap.gateDraw);
		g.setComposite(AlphaComposite.getInstance(
		AlphaComposite.SRC_OVER,0.8f));
		synchronized (lastMovePoint){
			g.drawLine(
				lastGatePoint.x,
				lastGatePoint.y,
				lastMovePoint.x,
				lastMovePoint.y);
		}
		setMouseMoved(false);
		mouseMoveClip.reset();
	}

	private void setLastGatePoint(Point p) {
		synchronized (lastGatePoint) {
			lastGatePoint.setLocation(p);
		}
	}


	/**
	 * Get the counts for a particular channel.
	 *
	 * @param p the channel to get counts for
	 * @return the counts at channel <code>p</code>
	 */
	double getCount(Point p) {
		return counts2d[p.x][p.y];
	}

	/**
	 * Get the counts for the displayed 2d histogram.
	 *
	 * @return the counts for the displayed 2d histogram
	 */
	double[][] getCounts() {
		return counts2d;
	}

	/**
	 * Get the maximum counts in the region of currently displayed 
	 * 2d Histogram.
	 *
	 * @return the maximum counts in the region of currently 
	 * displayed 2d Histogram
	 */
	protected int findMaximumCounts() {
		int chminX = plotLimits.getMinimumX();
		int chmaxX = plotLimits.getMaximumX();
		int chminY = plotLimits.getMinimumY();
		int chmaxY = plotLimits.getMaximumY();
		int maxCounts = 0;
		chminX = getChannelMin(chminX);
		chminY = getChannelMin(chminY);
		chmaxX = getChannelMax(chmaxX, sizeX);
		chmaxY = getChannelMax(chmaxY, sizeY);
		for (int i = chminX; i <= chmaxX; i++) {
			for (int j = chminY; j <= chmaxY; j++) {
				if (counts2d[i][j] > maxCounts) {
					maxCounts = (int) counts2d[i][j];
				}
			}
		}
		return maxCounts;
	}

	private int getChannelMin(final int ch) {
		int rval = ch;
		if ((ch == 0) && (ignoreChZero)) {
			rval = 1;
		}
		return rval;
	}

	private int getChannelMax(final int ch, final int size) {
		int rval = ch;
		if (((ch == size - 1)) && (ignoreChFull)) {
			rval = size - 2;
		}
		return rval;
	}

	/**
	 * Get the minimum counts in the region of currently displayed 
	 * 2d Histogram.
	 *
	 * @return the minimum counts in the region of currently 
	 * displayed 2d Histogram
	 */
	protected int findMinimumCounts() {
		int chminX = plotLimits.getMinimumX();
		int chmaxX = plotLimits.getMaximumX();
		int chminY = plotLimits.getMinimumY();
		int chmaxY = plotLimits.getMaximumY();
		int minCounts = 0;
		chminX = getChannelMin(chminX);
		chminY = getChannelMin(chminY);
		chmaxX = getChannelMax(chmaxX, sizeX);
		chmaxY = getChannelMax(chmaxY, sizeY);
		for (int i = chminX; i <= chmaxX; i++) {
			for (int j = chminY; j <= chmaxY; j++) {
				if (counts2d[i][j] < minCounts) {
					minCounts = (int) counts2d[i][j];
				}
			}
		}
		return minCounts;
	}

	private void setScale(Limits.ScaleType s) {
		synchronized (this) {
			scale = s;
		}
	}

	private final Rectangle clipBounds = new Rectangle();
	/**
	 * Called to draw a 2d histogram, including title, border, 
	 * tickmarks, tickmark labels and last but not least update the 
	 * scrollbars.
	 *
	 * @param g the graphics context to paint to
	 */
	protected void paintHistogram(Graphics g) {
		setScale(plotLimits.getScale());
		g.setColor(PlotColorMap.hist);
		g.getClipBounds(clipBounds);
		final int minX = graph.toDataHorz((int) clipBounds.getMinX());
		final int maxX = graph.toDataHorz((int) clipBounds.getMaxX());
		final int minY = graph.toDataVert((int) clipBounds.getMaxY());
		final int maxY = graph.toDataVert((int) clipBounds.getMinY());
		if (JamProperties.getBooleanProperty(JamProperties.GRADIENT_SCALE)) {
			graph.drawHist2d(counts2d, minX, minY, maxX, maxY);
			g.setPaintMode();
			g.setColor(PlotColorMap.foreground);
			graph.drawScale2d();
		} else {
			graph.drawHist2d(
				counts2d,
				minX,
				minY,
				maxX,
				maxY,
				PlotColorMap.getColorScale());
			g.setPaintMode();
			g.setColor(PlotColorMap.foreground);
			graph.drawScale2d(PlotColorMap.getColorScale());
		}
		/* draw labels/ticks after histogram so they are on top */
		g.setColor(PlotColorMap.foreground);
		graph.drawTitle(title, PlotGraphics.TOP);
		graph.drawNumber(number,new int[0]);
		graph.drawTicks(PlotGraphics.BOTTOM);
		graph.drawLabels(PlotGraphics.BOTTOM);
		graph.drawTicks(PlotGraphics.LEFT);
		graph.drawLabels(PlotGraphics.LEFT);
		if (axisLabelX != null) {
			graph.drawAxisLabel(axisLabelX, PlotGraphics.BOTTOM);
		} else {
			graph.drawAxisLabel(X_LABEL_2D, PlotGraphics.BOTTOM);
		}
		if (axisLabelY != null) {
			graph.drawAxisLabel(axisLabelY, PlotGraphics.LEFT);
		} else {
			graph.drawAxisLabel(Y_LABEL_2D, PlotGraphics.LEFT);
		}
		g.setPaintMode();
		g.setColor(PlotColorMap.foreground);
	}

	/**
	 * Paint a gate as a set of blocks that are
	 * channels in the gate.
	 *
	 * @param gc the graphics context to paint to
	 * @throws DataException if there's a problem painting the gate
	 */
	protected void paintGate(Graphics gc) {
		Graphics2D g=(Graphics2D)gc;
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
		0.5f));
		g.setColor(PlotColorMap.gateShow);
		final boolean noFillMode =
			JamProperties.getBooleanProperty(JamProperties.NO_FILL_GATE);
		if (noFillMode) {
			paintPolyGate(g);
		} else {
			graph.drawGate2d(currentGate.getLimits2d());
		}
	}

	/**
	 * Paint a gate as a polygon.
	 *
	 * @param g the graphics context to paint to
	 * @throws DataException if there's a problem painting the gate
	 */
	void paintPolyGate(Graphics g) {
		g.setPaintMode();
		g.setColor(PlotColorMap.gateShow);
		final Polygon gatePoints = currentGate.getBananaGate();
		if (gatePoints != null) {
			final int numberPoints = gatePoints.npoints;
			graph.clipPlot();
			final int lastI=numberPoints-1;
			for (int i = 0; i < lastI; i++) {
				final int x1 = gatePoints.xpoints[i];
				final int y1 = gatePoints.ypoints[i];
				final int x2 = gatePoints.xpoints[i + 1];
				final int y2 = gatePoints.ypoints[i + 1];
				graph.drawDataLine(x1, y1, x2, y2);
			}
			if (gatePoints.xpoints[0] != 
			gatePoints.xpoints[lastI]){
				final int x1 = gatePoints.xpoints[0];
				final int y1 = gatePoints.ypoints[0];
				final int x2 = gatePoints.xpoints[lastI];
				final int y2 = gatePoints.ypoints[lastI];
				graph.drawDataLine(x1, y1, x2, y2);
			}
		}
	}

	/**
	 * Paint a fit; not used in 2d.
	 *
	 * @param g the graphics context to paint on
	 */
	protected void paintFit(Graphics g) {
		error("Cannot plot fits with 2D histograms.");
	}

	/**
	 * Paint an overlay; not used in 2d.
	 *
	 * @param g the graphics context to paint with
	 */
	protected void paintOverlay(Graphics g) {
		error("Cannot plot overlays with 2D histograms.");
	}

	/**
	 * Mouse moved so update drawing gate or marking area 
	 * as appropriate 
	 * For gate undo last line draw, and draw a new line.
	 *
	 * @param me created when the mouse pointer moves while in the 
	 * plot
	 */
	protected void mouseMoved(MouseEvent me) {		
		if (settingGate) {
			/* only if we have 1 or more */
			if (pointsGate.npoints > 0) {
				/* draw new line */
				synchronized (lastMovePoint){
					if (mouseMoveClip.npoints == 0) {
						mouseMoveClip.addPoint(lastGatePoint.x, lastGatePoint.y);
						mouseMoveClip.addPoint(lastMovePoint.x, lastMovePoint.y);
					}
					lastMovePoint.setLocation(me.getPoint());
					mouseMoveClip.addPoint(lastMovePoint.x, lastMovePoint.y);
				}
				setMouseMoved(true);
				repaint(getClipBounds(mouseMoveClip,false));
			}
		} else if (selectingArea) {
			synchronized (lastMovePoint){
				if (isSelectingAreaClipClear()){	
					addToSelectClip(selectionStartPoint,lastMovePoint);
				}
				lastMovePoint.setLocation(graph.toData(me.getPoint()));
				addToSelectClip(selectionStartPoint,	lastMovePoint);
			}
			setMouseMoved(true);
			synchronized (selectingAreaClip){
				repaint(getClipBounds(selectingAreaClip,false));			
			}
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
	private final void addToSelectClip(Point p1, Point p2){
		synchronized (selectingAreaClip){
			selectingAreaClip.add(graph.getRectangleOutline2d(p1,p2));
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
			r.add(r.x+r.width+1, r.y+r.height+1);
			r.add(r.x-1, r.y - 1);
			final Point p1=r.getLocation();
			final Point p2=new Point(p1.x+r.width,p1.y+r.height);
			/* now do conversion */
			r.setBounds(graph.getRectangleOutline2d(p1,p2));
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
	
}
