package jam.plot;
import java.awt.*;
import java.awt.event.*;
import jam.global.JamProperties;
import java.util.Iterator;

/**
 * Class to plot a 2-dimensional histogram.
 *
 * @version 0.5
 * @author Ken Swartz
 */

class Plot2d extends Plot implements MouseMotionListener, MouseListener {

	/* last data gate point */
	private final Point lastPoint = new Point();

	/* last pixel point added to gate list */
	private final Point lastGatePoint = new Point();

	/* last pixel point mouse moved to */
	private final Point lastMovePoint = new Point();

	/** areaMark is a rectangle in channel space */
	private final Rectangle areaMark = new Rectangle();

	/** A rectangle in pixel space while marking area*/
	private final Rectangle recMarking = new Rectangle();

	/**
	 * Creates a Plot object for displaying 2D histograms.
	 * 
	 * @param a the action toolbar
	 */
	Plot2d(Action a) {
		super(a);
	}

	
	void paintMarkedChannels(Graphics g){
		g.setColor(PlotColorMap.mark);
		final Iterator it=markedChannels.iterator();
		while (it.hasNext()){
			final Point p=(Point)it.next();
			graph.markChannel2d(p);
		}
	}

	/**
	 * Start marking an area.
	 * 
	 * @param p1 starting data point
	 */
	public void markingArea(Point p1) {
		//Copy points, don't construct new rectangles
		areaStartPoint.x=p1.x;
		areaStartPoint.y=p1.y;
		//set initial values out of range so we
		//know they are initial values
		lastMovePoint.x=-1;
		lastMovePoint.y=-1;
	} 
	/**
	 * Paint call while marking an area
	 */
	void paintMarkingArea(Graphics gc) {
		Graphics2D g=(Graphics2D)gc;
		g.setColor(PlotColorMap.area);		
		
		//FIXME KBS some of this should be moved to PlotData.
		final Point areaPointView=graph.toViewLin(areaStartPoint);
		if (lastMovePoint.x<0) {
			lastMovePoint.x=areaPointView.x;
			lastMovePoint.y=areaPointView.y;
		}											
		final int xll = Math.min(lastMovePoint.x, areaPointView.x);
		final int xul = Math.max(lastMovePoint.x, areaPointView.x);
		final int yll = Math.min(lastMovePoint.y, areaPointView.y);
		final int yul = Math.max(lastMovePoint.y, areaPointView.y);
		final int width=xul-xll;
		final int height=yul-yll;
		recMarking.setBounds(xll, yll, width, height);
		graph.markArea2dOutline(recMarking);
		setMouseMoved(false);
		
	}
	
	
	/**
	 * Mark a rectangular area on the plot.
	 *
	 * @param p1 one corner of the rectangle
	 * @param p2 another corner of the rectangle
	 */
	public void markArea(Point p1, Point p2) {
		synchronized (this) {
			markArea = (p1 != null) && (p2 != null);
		}
		if (markArea) {
			final int xll = Math.min(p1.x, p2.x);
			final int xul = Math.max(p1.x, p2.x);
			final int yll = Math.min(p1.y, p2.y);
			final int yul = Math.max(p1.y, p2.y);
			final int width=xul-xll;
			final int height=yul-yll;
			synchronized (areaMark) {
				areaMark.setBounds(xll,yll,width,height);
			}
			final Rectangle clip=graph.get2dAreaMark(xll,xul,yll,yul);
			clip.add(clip.getMaxX() + 1,clip.getMaxY() + 1);
			repaint(clip);
		}
	}

	void paintMarkArea(Graphics g) {
		final Graphics2D g2=(Graphics2D)g;
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
		0.5f));
		g.setColor(PlotColorMap.area);
		graph.clipPlot();
		graph.markArea2d(graph.get2dAreaMark(areaMark));
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
	public void displaySetGate(
		GateSetMode mode,
		Point pChannel,
		Point pPixel) {
		if (mode == GateSetMode.GATE_NEW) {
			setSettingGate(true);
			pointsGate.reset();
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
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
			removeMouseListener(this);
			removeMouseMotionListener(this);
			repaint(clip);
		} 
	}

	void paintSetGate(Graphics g) {
		g.setColor(PlotColorMap.gateDraw);
		graph.settingGate2d(pointsGate);
	}
	/**
	 * Called by mouse movement while setting a gate
	 * @param g
	 */	
	void paintSettingGate(Graphics gc) {
		Graphics2D g=(Graphics2D)gc;
		g.setColor(PlotColorMap.gateDraw);
		g.setComposite(AlphaComposite.getInstance(
		AlphaComposite.SRC_OVER,0.8f));
		g.drawLine(
			lastGatePoint.x,
			lastGatePoint.y,
			lastMovePoint.x,
			lastMovePoint.y);
		setMouseMoved(false);
		mouseMoveClip.reset();
		
	}

	private void setLastGatePoint(Point p) {
		synchronized (lastGatePoint) {
			lastGatePoint.setLocation(p);
		}
	}

	private void setLastMovePoint(Point p) {
		synchronized (lastMovePoint) {
			lastMovePoint.setLocation(p);
		}
	}

	/**
	 * Get the counts for a particular channel.
	 *
	 * @param p the channel to get counts for
	 * @return the counts at channel <code>p</code>
	 */
	public double getCount(Point p) {
		return counts2d[p.x][p.y];
	}

	/**
	 * Get the counts for the displayed 2d histogram.
	 *
	 * @return the counts for the displayed 2d histogram
	 */
	public double[][] getCounts() {
		return counts2d;
	}

	/**
	 * Get the maximum counts in the region of currently displayed 
	 * 2d Histogram.
	 *
	 * @return the maximum counts in the region of currently 
	 * displayed 2d Histogram
	 */
	public int findMaximumCounts() {
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
	public int findMinimumCounts() {
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

	final Rectangle clipBounds = new Rectangle();
	/**
	 * Called to draw a 2d histogram, including title, border, 
	 * tickmarks, tickmark labels and last but not least update the 
	 * scrollbars.
	 *
	 * @param g the graphics context to paint to
	 */
	public void paintHistogram(Graphics g) {
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
		graph.drawNumber(number,-1);
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
	void paintGate(Graphics gc) {
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
	void paintFit(Graphics g) {
		error("Cannot plot fits with 2D histograms.");
	}

	/**
	 * Paint an overlay; not used in 2d.
	 *
	 * @param g the graphics context to paint with
	 */
	void paintOverlay(Graphics g) {
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
	public void mouseMoved(MouseEvent me) {		
		//settting gate
		if (settingGate) {
			/* only if we have 1 or more */
			if (pointsGate.npoints > 0) {
				//draw new line
				if (mouseMoveClip.npoints == 0) {
					mouseMoveClip.addPoint(lastGatePoint.x, lastGatePoint.y);
					mouseMoveClip.addPoint(lastMovePoint.x, lastMovePoint.y);
				}
				lastMovePoint.setLocation(me.getX(), me.getY());
				mouseMoveClip.addPoint(lastMovePoint.x, lastMovePoint.y);
				setMouseMoved(true);
				this.repaint(getClipBounds(mouseMoveClip,false));
			}
		//marking area	
		} else if (markingArea) {			
			lastMovePoint.setLocation(me.getX(), me.getY());
			setMouseMoved(true);
			this.repaint();
			//this.repaint(getClipBounds(mouseMoveClip,false));			
		}
	}
	
	/**
	 * Given a polygon, return the bounding rectangle + 1 pixel
	 * more on each side.
	 * 
	 * @param pin the polygon to use
	 * @param convert if the polygon should first be converted from
	 * channel coordinates to view coordinates
	 * @return the bounding rectangle + 1 pixel
	 * more on each side
	 */
	private Rectangle getClipBounds(Polygon pin, boolean convert){
		Polygon p=new Polygon(pin.xpoints,pin.ypoints,pin.npoints);
		if (convert){
			p=graph.toView(p);
		}
		final Rectangle r = p.getBounds();
		r.add(r.getMaxX() + 1, r.getMaxY() + 1);
		r.add(r.getX() - 1, r.getY() - 1);
		return r;
	}
	/**
	 * Paint called if mouse moved is enabled
	 */
	void paintMouseMoved(Graphics gc) {

		//Setting gate
		if (settingGate) {
			paintSettingGate(gc);			
		} else if (markingArea) {
			paintMarkingArea( gc);
			
		}		
	}



}
