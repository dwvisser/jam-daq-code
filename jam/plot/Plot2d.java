package jam.plot;
import java.awt.*;
import java.awt.event.*;
import jam.data.*;
import jam.global.JamProperties;
import javax.swing.JOptionPane;

/**
 * Class to plot a 2-dimensional histogram.
 *
 * @version 0.5
 * @author Ken Swartz
 */

class Plot2d extends Plot implements MouseMotionListener, MouseListener {

    private Graphics graphicsSetGate;
    
    /* last data gate point */
    private final Point lastPoint=new Point();
    
	/* need to erase the last temporary line drawn */
    private boolean needErase=false;    

	/* last pixel point added to gate list */
    private final Point lastGatePoint=new Point();
    
    /* last pixel point mouse moved to */
    private final Point lastMovePoint=new Point();

    /**
     * Creates a Plot object for displaying 2D histograms.
     * 
     * @param a the action toolbar
     */
    Plot2d(Action a){
        super(a);
    }
    
    /**
     * Mark a channel on the plot.
     *
     * @param p graphics coordinates on the plot where the channel is
     */
    public void markChannel(Point p) {
        final Graphics g=getGraphics();
        g.setColor(PlotColorMap.mark);
        graph.update(g,viewSize,plotLimits);  //so graph has all pertinent imfo
        graph.markChannel2d(p);
        g.dispose();
    }

    /**
     * Mark a rectangular area on the plot.
     *
     * @param p1 one corner of the rectangle
     * @param p2 another corner of the rectangle
     */
    public void markArea(Point p1, Point p2){
		final int xll=Math.min(p1.x,p2.x);
		final int xul=Math.max(p1.x,p2.x);
		final int yll=Math.min(p1.y,p2.y);
		final int yul=Math.max(p1.y,p2.y);
       	final Graphics g=this.getGraphics();
        g.setColor(PlotColorMap.area);
        graph.update(g,viewSize,plotLimits);  //so graph has all pertinent imfo
        graph.markArea2d(xll, xul, yll, yul);
        g.dispose();
    }

	private void setDisplayingGate(boolean dg){
		synchronized(this){
			displayingGate=dg;
		}
	}
	
	private void setCurrentGate(Gate g){
		synchronized(this){
			currentGate=g;
		}
	}

    /**
     * Display a 2d gate.
     *
     * @param gate the gate to display
     * @throws DataException if there's a problem displaying the gate
     */
    public void displayGate(Gate gate) throws DataException{
        if (currentHist.hasGate(gate)){
            setDisplayingGate(true);
            setCurrentGate(gate);
            final Graphics g=this.getGraphics();
            graph.update(g);  //so graph has all pertinent info
            paintGate(g);
            paintPolyGate(g);
            g.dispose();
        } else {
            error("Can't display '"+
                gate+"' on histogram '"+currentHist+"'.");
        }
    }

    /**
     * Display a 2d gate as a polygon.
     *
     * @param gate the gate to display
     * @throws DataException if there's a problem displaying the gate
     */
    public void displayPolyGate(Gate gate) throws DataException{
        setCurrentGate(gate);
        final Graphics g=this.getGraphics();
        graph.update(g);
        paintPolyGate(g);
        g.dispose();
    }
    
    private void setSettingGate(boolean sg){
    	synchronized(this){
    		settingGate=sg;
    	}
    }
    
    private void initGraphicsSetGate(){
    	synchronized(this){
    		graphicsSetGate=getGraphics();
    	}
    }
    
    private void setNeedErase(boolean ne){
    	synchronized(this){
    		needErase=ne;
    	}
    }
    
    private void setLastPoint(Point lp){
    	synchronized(lastPoint) {
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
    public void displaySetGate(int mode, Point pChannel, Point pPixel){
        if (mode==GATE_NEW){
            setSettingGate(true);
            pointsGate.clear();
            initGraphicsSetGate();
            graph.update(graphicsSetGate);//so graph has all pertinent info
            setNeedErase(false);
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
        } else if (mode==GATE_CONTINUE) {
            pointsGate.add(pChannel);
            setLastPoint(pChannel);//save data point
            if(needErase) {//erase last line
				drawToggleDragLine();
				setNeedErase(false);
            }
            /* update variables */
            final Point tempP=graph.toViewLin(pChannel);
            setLastGatePoint(tempP);
            if(pPixel!=null){
                setLastMovePoint(pPixel);
            } else {
                setLastMovePoint(lastGatePoint);
            }
            /* draw permanent line */
            graphicsSetGate.setPaintMode();
            graphicsSetGate.setColor(PlotColorMap.gateDraw);
            graph.update(graphicsSetGate);//so graph has all pertinent info
            graph.settingGate2d(pointsGate);
        } else if(mode==GATE_REMOVE){
            if(!pointsGate.isEmpty()){
                /* remove current gate lines */
                graphicsSetGate.setPaintMode();
                graphicsSetGate.setColor(PlotColorMap.background);
                graph.update(graphicsSetGate);//so graph has all pertinent info
                graph.settingGate2d(pointsGate);
                /* remove last point */
                if ((pointsGate.size()>0)) {
                    pointsGate.remove(pointsGate.size()-1);
                }
                /* go back a point */
                if((pointsGate.size()>0)) {
                    setLastPoint((Point)pointsGate.get(pointsGate.size()-1));
                    if(needErase) {//erase last moving line
						drawToggleDragLine();
                        setNeedErase(false);
                    }
                    /* update variables */
                    final Point tempP=graph.toViewLin((Point)pointsGate.get(
                    pointsGate.size()-1));
                    setLastGatePoint(tempP);
                    setLastMovePoint(lastGatePoint);
                }
                /* draw current gate lines */
                graphicsSetGate.setPaintMode();
                graphicsSetGate.setColor(PlotColorMap.gateDraw);
                graph.update(graphicsSetGate);//so graph has all pertinent info
                graph.settingGate2d(pointsGate);
            }
        } else if(mode==GATE_SAVE){//draw a saved gate
            setSettingGate(false);
            /* draw the finished gate */
            pointsGate.add((Point)pointsGate.get(0));
            graphicsSetGate.setPaintMode();
            graphicsSetGate.setColor(PlotColorMap.gateDraw);
            graph.update(graphicsSetGate);//so graph has all pertinent info
            graph.settingGate2d(pointsGate);
            pointsGate.clear();
            if(graphicsSetGate!=null) {
                graphicsSetGate.dispose();
            }
            this.removeMouseListener(this);
            this.removeMouseMotionListener(this);
        } else if(mode==GATE_CANCEL){//cancel a drawing gate
            setSettingGate(false);
            pointsGate.clear();
            if(graphicsSetGate!=null) {
                graphicsSetGate.dispose();
            }
            removeMouseListener(this);
            removeMouseMotionListener(this);
        }
    }
    
    private void setLastGatePoint(Point p){
    	synchronized(lastGatePoint){
    		lastGatePoint.setLocation(p);
    	}
    }
    
    private void setLastMovePoint(Point p){
    	synchronized(lastMovePoint){
    		lastMovePoint.setLocation(p);
    	}
    }

    /**
     * Get the counts for a particular channel.
     *
     * @param p the channel to get counts for
     * @return the counts at channel <code>p</code>
     */
    public double getCount(Point p){
        return counts2d[p.x][p.y];
    }

    /**
     * Get the counts for the displayed 2d histogram.
     *
     * @return the counts for the displayed 2d histogram
     */
    public double [][] getCounts(){
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
        int chminX=plotLimits.getMinimumX();
        int chmaxX=plotLimits.getMaximumX();
        int chminY=plotLimits.getMinimumY();
        int chmaxY=plotLimits.getMaximumY();
        int maxCounts=0;
        chminX=getChannelMin(chminX);
        chminY=getChannelMin(chminY);
        chmaxX=getChannelMax(chmaxX,sizeX);
        chmaxY=getChannelMax(chmaxY,sizeY);
        for(int i=chminX;i<=chmaxX;i++){
            for( int j=chminY;j<=chmaxY;j++){
                if (counts2d[i][j]>maxCounts) {
                    maxCounts=(int)counts2d[i][j];
                }
            }
        }
        return maxCounts;
    }
    
    private int getChannelMin(final int ch){
    	int rval=ch;
        if ((ch==0)&&(ignoreChZero)){
            rval=1;
        }
        return rval;
    }
    
    private int getChannelMax(final int ch, final int size){
    	int rval=ch;
        if (((ch==size-1))&&(ignoreChFull)){
            rval=size-2;
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
        int chminX=plotLimits.getMinimumX();
        int chmaxX=plotLimits.getMaximumX();
        int chminY=plotLimits.getMinimumY();
        int chmaxY=plotLimits.getMaximumY();
        int minCounts=0;
        chminX=getChannelMin(chminX);
        chminY=getChannelMin(chminY);
        chmaxX=getChannelMax(chmaxX,sizeX);
        chmaxY=getChannelMax(chmaxY,sizeY);
        for(int i=chminX;i<=chmaxX;i++){
            for( int j=chminY;j<=chmaxY;j++){
                if (counts2d[i][j]<minCounts) {
                    minCounts=(int)counts2d[i][j];
                }
            }
        }
        return minCounts;
    }
    
    private void setScale(Limits.ScaleType s){
    	synchronized(this){
    		scale=s;
    	}
    }

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
		graph.update(g);
        if (JamProperties.getBooleanProperty(JamProperties.GRADIENT_SCALE)){
        	graph.drawHist2d(counts2d);
			g.setPaintMode();
			g.setColor(PlotColorMap.foreground);
			graph.drawScale2d();
        } else {
			graph.drawHist2d(counts2d, PlotColorMap.getColorScale());
			g.setPaintMode();
			g.setColor(PlotColorMap.foreground);
			graph.drawScale2d(PlotColorMap.getColorScale());
        }
        //draw labels and ticks after histogram so they are on top
        g.setColor(PlotColorMap.foreground);
        graph.drawTitle(title,PlotGraphics.TOP);
        graph.drawNumber(number);
        graph.drawTicks(PlotGraphics.BOTTOM);
        graph.drawLabels(PlotGraphics.BOTTOM);
        graph.drawTicks (PlotGraphics.LEFT);
        graph.drawLabels(PlotGraphics.LEFT);
        if(axisLabelX!=null){
            graph.drawAxisLabel(axisLabelX,PlotGraphics.BOTTOM);
        } else {
            graph.drawAxisLabel(X_LABEL_2D,PlotGraphics.BOTTOM);
        }
        if(axisLabelY!=null){
            graph.drawAxisLabel(axisLabelY, PlotGraphics.LEFT);
        } else {
            graph.drawAxisLabel(Y_LABEL_2D,PlotGraphics.LEFT);
        }
        g.setPaintMode();
        g.setColor(PlotColorMap.foreground);
        if (settingGate){
            graph.settingGate2d(pointsGate);
            setNeedErase(false);
            if(pointsGate.size()>0){
                final Point tempP=graph.toViewLin(lastPoint);
                setLastGatePoint(tempP);
            }
        }
    }

    /**
     * Paint a gate as a set of blocks that are
     * channels in the gate.
     *
     * @param g the graphics context to paint to
     * @throws DataException if there's a problem painting the gate
     */
    void paintGate(Graphics g) throws DataException{
        g.setXORMode(Color.black);
        g.setColor(PlotColorMap.gateShow);
        final boolean noFillMode=JamProperties.getBooleanProperty(
        JamProperties.NO_FILL_2D);
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
    void paintPolyGate(Graphics g) throws DataException{
        g.setPaintMode();
        g.setColor(PlotColorMap.gateShow);
        final Polygon gatePoints=currentGate.getBananaGate();
        if(gatePoints!=null){
            final int numberPoints=gatePoints.npoints;
            graph.clipPlot();
            for (int i=0; i<numberPoints-1; i++){
                final int x1=gatePoints.xpoints[i];
                final int y1=gatePoints.ypoints[i];
                final int x2=gatePoints.xpoints[i+1];
                final int y2=gatePoints.ypoints[i+1];
                graph.drawDataLine(x1, y1, x2, y2);
            }
        }
    }

	/**
     * Paint a fit; not used in 2d.
     *
     * @param g the graphics context to paint on
     */
    void paintFit(Graphics g){
		error("Cannot plot fits with 2D histograms.");
    }
    
    private void error(String mess){
		final String plotErrorTitle="Plot Error";
		JOptionPane.showMessageDialog(this,
		mess,
		plotErrorTitle,
		JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Paint an overlay; not used in 2d.
     *
     * @param g the graphics context to paint with
     */
    void paintOverlay(Graphics g){
        error("Cannot plot overlays with 2D histograms.");
    }

    /**
     * Mouse moved in drawing gate mode so
     * undo last line draw, and draw a new line.
     *
     * @param me created when the mouse pointer moves while in the 
     * plot
     */
    public void mouseMoved(MouseEvent me){
        /* only if we have 1 or more */
        if(pointsGate.size()>0){
            //remove old lines lines
            if(needErase) {
				drawToggleDragLine();
            }
            //draw new line
            lastMovePoint.setLocation(me.getX(),me.getY());
			drawToggleDragLine();
            setNeedErase(true);
        }
    }
    
    private void drawToggleDragLine(){
		final Color xorColor=Color.BLACK;
		graphicsSetGate.setXORMode(xorColor);
		graphicsSetGate.drawLine(lastGatePoint.x, lastGatePoint.y, 
		lastMovePoint.x, lastMovePoint.y);
    }
    
    /**
     * Not used.
     * 
     * @param me created when the mouse is dragged across the plot
     * with the button down
     */
    public void mouseDragged(MouseEvent me){
    }
    
    /**
     * Not used.
     * 
     * @param me created when the mouse button is pressed on the plot
     */
    public void  mousePressed(MouseEvent me) {
    }
    
    /**
     * Not used.
     *
     * @param e created when the mouse is clicked on the plot
     */
    public void  mouseClicked(MouseEvent e){
    }

    /**
     * Not used.
     *
     * @param e created when the mouse pointer enters the plot
     */
    public void  mouseEntered(MouseEvent e){
    }

    /**
     * Undo last temporary line drawn.
     * 
     * @param e created when mouse exits the plot
     */
    public void  mouseExited(MouseEvent e){
        /* remove temporay lines only if we need to and 
         * have 1 or more point */
        if (needErase) {
			drawToggleDragLine();
            setNeedErase(false);
        }
    }

    /**
     * Not used.
     *
     * @param e created when the mouse is released
     */
    public void  mouseReleased(MouseEvent e){
    }
}
