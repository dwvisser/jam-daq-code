/*
 */
package jam.plot;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import jam.data.*;
import jam.global.JamProperties;

/**
 *  Class to plot a 2 dimensional histogram
 *
 *
 * last edit 17 july 98
 * @Version 0.5
 * Author Ken Swartz
 */

class Plot2d extends Plot implements MouseMotionListener, MouseListener {

    private Graphics graphicsSetGate;
    private int numberPointsSetGate;  //number of gate points set.
    private Point lastPoint;    //last data gate point
    private boolean needErase;    //need to erase the last tempory line drawen

    private int lastGateX;    //last pixel point added to gate list
    private int lastGateY;
    private int lastMoveX;    //last pixel point mouse moved to
    private int lastMoveY;


    /**
     * Construnctor just runs super
     */
    public Plot2d(){
        super();
        needErase=false;
        numberPointsSetGate=0;
    }

    /**
     * Mark a channel
     *
     */
    public void markChannel(int channelX, int channelY) {
        Graphics g=this.getGraphics();
        g.setColor(PlotColorMap.mark);
        graph.update(g,viewSize,plotLimits);  //so graph has all pertinent imfo
        graph.markChannel2d(channelX, channelY);
        g.dispose();
    }

    /**
     * Mark Area
     * @param minChanX the lower x channel
     * @param minchanY the lower y channel
     * @param maxChanX the upper x channel
     * @param maxchanY the upper y channel
     */
    public void markArea(int minChanX, int maxChanX, int minChanY, int maxChanY){
        int xll, xul;      //x lower and upper limits
        int yll, yul;      //y lower and upper limits

        if (minChanX<=maxChanX){
            xll=minChanX;
            xul=maxChanX;
        } else{
            xll=maxChanX;
            xul=minChanX;
        }
        if (minChanY<=maxChanY){
            yll=minChanY;
            yul=maxChanY;
        } else {
            yll=maxChanY;
            yul=minChanY;
        }
        Graphics g=this.getGraphics();
        g.setColor(PlotColorMap.area);
        graph.update(g,viewSize,plotLimits);  //so graph has all pertinent imfo
        graph.markArea2d(xll, xul, yll, yul);
        g.dispose();
    }

    /**
     * Histogram overlay, not implemented for 2d histograms.
     */
    public void overlay(Histogram overlayhist){
        System.out.println("Error cannot overlay 2d");
    }

    /**
     * Display a 2d gate.
     */
    public void displayGate(Gate gate) throws DataException{
        if (currentHist.hasGate(gate)){
            displayingGate=true;
            currentGate=gate;
            Graphics g=this.getGraphics();
            graph.update(g);  //so graph has all pertinent info
            paintGate(g);
            paintPolyGate(g);
            g.dispose();
        } else {
            System.err.println(getClass().getName()+": trying to display '"+
                gate+"' on histogram '"+currentHist+"'");
        }
    }

    /**
     * Display a 2d gate as a polygon.
     */
    public void displayPolyGate(Gate gate) throws DataException{
        currentGate=gate;
        Graphics g=this.getGraphics();
        graph.update(g);
        paintPolyGate(g);
        g.dispose();
    }

    /**
     * Show the setting of a gate.
     */
    public void displaySetGate(int mode, Point pChannel, Point pPixel){
        if (mode==GATE_NEW){
            settingGate=true;
            pointsGate=new Vector(10,5);
            graphicsSetGate=this.getGraphics();
            graph.update(graphicsSetGate);        //so graph has all pertinent imfo
            numberPointsSetGate=0;
            needErase=false;
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
        } else if (mode==GATE_CONTINUE) {
            pointsGate.add(pChannel);
            //save data point
            lastPoint=pChannel;
            //erase last line
            if(needErase) {
                graphicsSetGate.setXORMode(Color.black);
                graphicsSetGate.drawLine(lastGateX, lastGateY, lastMoveX, lastMoveY);
                needErase=false;
            }
            numberPointsSetGate++;
            //update variables
            Point tempP=graph.toViewLin(pChannel);
            lastGateX=tempP.x;
            lastGateY=tempP.y;
            if(pPixel!=null){
                lastMoveX=pPixel.x;
                lastMoveY=pPixel.y;
            } else {
                lastMoveX=lastGateX;
                lastMoveY=lastGateY;
            }
            //draw permanent line
            graphicsSetGate.setPaintMode();
            graphicsSetGate.setColor(PlotColorMap.gateDraw);
            graph.update(graphicsSetGate);        //so graph has all pertinent imfo
            graph.settingGate2d(pointsGate);

        } else if(mode==GATE_REMOVE){
            //check there is at least one gate point
            if(!pointsGate.isEmpty()){

                //remove current gate lines
                graphicsSetGate.setPaintMode();
                graphicsSetGate.setColor(PlotColorMap.background);
                graph.update(graphicsSetGate);        //so graph has all pertinent imfo
                graph.settingGate2d(pointsGate);


                //remove last point
                if((numberPointsSetGate>=1)) {
                    pointsGate.remove(pointsGate.size()-1);
                    numberPointsSetGate--;
                }
                //go back a point
                if((numberPointsSetGate>=1)) {
                    lastPoint=(Point)pointsGate.get(pointsGate.size()-1);
                    //erase last moving line
                    if(needErase) {
                        graphicsSetGate.setXORMode(Color.black);
                        graphicsSetGate.drawLine(lastGateX, lastGateY, lastMoveX, lastMoveY);
                        needErase=false;
                    }
                    //update variables
                    Point tempP=graph.toViewLin((Point)pointsGate.get(pointsGate.size()-1));
                    lastGateX=tempP.x;
                    lastGateY=tempP.y;
                    lastMoveX=lastGateX;
                    lastMoveY=lastGateY;
                }

                //draw current gate lines
                graphicsSetGate.setPaintMode();
                graphicsSetGate.setColor(PlotColorMap.gateDraw);
                graph.update(graphicsSetGate);          //so graph has all pertinent imfo
                graph.settingGate2d(pointsGate);
            }
        } else if(mode==GATE_SAVE){//draw a saved gate
            settingGate=false;
            numberPointsSetGate=0;
            //draw the finished gate
            pointsGate.add((Point)pointsGate.get(0));
            graphicsSetGate.setPaintMode();
            graphicsSetGate.setColor(PlotColorMap.gateDraw);
            graph.update(graphicsSetGate);        //so graph has all pertinent imfo
            graph.settingGate2d(pointsGate);
            if(graphicsSetGate!=null) {
                graphicsSetGate.dispose();
            }
            this.removeMouseListener(this);
            this.removeMouseMotionListener(this);
        } else if(mode==GATE_CANCEL){//cancel a drawing gate
            settingGate=false;
            numberPointsSetGate=0;
            if(graphicsSetGate!=null) {
                graphicsSetGate.dispose();
            }
            this.removeMouseListener(this);
            this.removeMouseMotionListener(this);
        }
    }

    /**
     * Get the counts for a particular channel.
     */
    public double getCount(int channelX, int channelY){
        return counts2d[channelX][channelY];
    }

    /**
     * Return the counts for the displayed 2d histogram
     */
    public double [][] getCounts(){
        return counts2d;
    }

    /**
     * get the maximum counts in the region of currently displayed 2d Histogrma
     */
    public int findMaximumCounts() {

        int chminX=plotLimits.getMinimumX();
        int chmaxX=plotLimits.getMaximumX();
        int chminY=plotLimits.getMinimumY();
        int chmaxY=plotLimits.getMaximumY();

        int maxCounts=0;

        if ((chminX==0)&&(ignoreChZero)){
            chminX=1;
        }
        if ((chminY==0)&&(ignoreChZero)){
            chminY=1;
        }
        if (((chmaxX==sizeX-1))&&(ignoreChFull)){
            chmaxX=sizeX-2;
        }
        if ((chmaxY==sizeY-1)&&(ignoreChFull)){
            chmaxY=sizeY-2;
        }

        for(int i=chminX;i<=chmaxX;i++){
            for( int j=chminY;j<=chmaxY;j++){
                if (counts2d[i][j]>maxCounts) {
                    maxCounts=(int)counts2d[i][j];
                }
            }
        }
        return maxCounts;
    }
    /**
     * get the minimum counts in the region of currently displayed 2d Histogrma
     */
    public int findMinimumCounts() {

        int chminX=plotLimits.getMinimumX();
        int chmaxX=plotLimits.getMaximumX();
        int chminY=plotLimits.getMinimumY();
        int chmaxY=plotLimits.getMaximumY();

        int minCounts=0;

        if ((chminX==0)&&(ignoreChZero)){
            chminX=1;
        }
        if ((chminY==0)&&(ignoreChZero)){
            chminY=1;
        }
        if (((chmaxX==sizeX-1))&&(ignoreChFull)){
            chmaxX=sizeX-2;
        }
        if ((chmaxY==sizeY-1)&&(ignoreChFull)){
            chmaxY=sizeY-2;
        }

        for(int i=chminX;i<=chmaxX;i++){
            for( int j=chminY;j<=chmaxY;j++){
                if (counts2d[i][j]<minCounts) {
                    minCounts=(int)counts2d[i][j];
                }
            }
        }
        return minCounts;
    }

    /**
     * Called to draw a 2d histogram
     *
     * including title, border, tickmarks, tickmark labels
     * and last but not least update the scrollbars
     *
     */
    public void paintHistogram(Graphics g) {
        scale=plotLimits.getScale();
        g.setColor(PlotColorMap.hist);
        if (JamProperties.getBooleanProperty(JamProperties.GRADIENT_SCALE)){
        	graph.drawHist2d(counts2d);
			g.setPaintMode();
			g.setColor(PlotColorMap.foreground);
			graph.drawScale2d();
        }else {
			graph.drawHist2d(counts2d, PlotColorMap.getColorScale());
			g.setPaintMode();
			g.setColor(PlotColorMap.foreground);
			graph.drawScale2d(PlotColorMap.getColorScale());
        }
        //draw labels and ticks after histogram so they are on top
        g.setColor(PlotColorMap.foreground);
        graph.drawTitle(title,PlotGraphics.TOP);
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
            needErase=false;
            if(numberPointsSetGate>0){
                Point tempP=graph.toViewLin(lastPoint);
                lastGateX=tempP.x;
                lastGateY=tempP.y;
            }
        }
    }

    /**
     * Paint a gate as a set of blocks that are
     * channels in the gate.
     */
    void paintGate(Graphics g) throws DataException{
        g.setXORMode(Color.black);
        g.setColor(PlotColorMap.gateShow);
        boolean noFillMode=g instanceof PrintGraphics ||
        	JamProperties.getBooleanProperty(JamProperties.NO_FILL_2D);
        if (noFillMode) {
            paintPolyGate(g);
        } else {
            graph.drawGate2d(currentGate.getLimits2d());
        }
    }

    /**
     * Paint a gate as a polygon.
     */
    void paintPolyGate(Graphics g) throws DataException{
        Polygon gatePoints;
        int numberPoints;
        int x1,y1,x2,y2;

        g.setPaintMode();
        g.setColor(PlotColorMap.gateShow);
        gatePoints=currentGate.getBananaGate();
        if(gatePoints!=null){
            numberPoints=gatePoints.npoints;
            graph.clipPlot();
            for (int i=0; i<numberPoints-1; i++){
                x1=gatePoints.xpoints[i];
                y1=gatePoints.ypoints[i];
                x2=gatePoints.xpoints[i+1];
                y2=gatePoints.ypoints[i+1];
                graph.drawDataLine(x1, y1, x2, y2);
            }
        }
    }

    /**
     * paint a fit not used in 2d
     */
    void paintFit(Graphics g){
        /* does nothing for now */
    }
    /**
     * paint a overlay not used in 2d
     */
    void paintOverlay(Graphics g){
        /* does nothing for now */
    }

    /**
     * Mouse moved in drawing gate mode so
     * undo last line and draw a new line
     */
    public void mouseMoved(MouseEvent me){

        //only if we have 1 only
        if(numberPointsSetGate>=1){

            //remove old lines lines
            if(needErase) {
                graphicsSetGate.setXORMode(Color.black);
                graphicsSetGate.drawLine(lastGateX, lastGateY, lastMoveX, lastMoveY);
            }

            //draw new line
            lastMoveX=me.getX();
            lastMoveY=me.getY();
            graphicsSetGate.setXORMode(Color.black);
            graphicsSetGate.drawLine(lastGateX, lastGateY, lastMoveX, lastMoveY);
            needErase=true;
        }
    }
    /**
     * Not used
     */
    public void mouseDragged(MouseEvent me){
    }
    /**
     * Not Used
     */
    public void  mousePressed(MouseEvent me) {
    }
    /**
     * Not Used
     */
    public void  mouseClicked(MouseEvent e){
    }

    /**
     * Not Used
     */
    public void  mouseEntered(MouseEvent e){
    }

    /**
     * Undo last temporary line drawn.
     */
    public void  mouseExited(MouseEvent e){
        //remove tempory lines only if we need to and have 1 or more point
        if (needErase) {
            //undo last line
            graphicsSetGate.setXORMode(Color.black);
            graphicsSetGate.drawLine(lastGateX, lastGateY, lastMoveX, lastMoveY);
            needErase=false;
        }
    }

    /**
     * Not used
     */
    public void  mouseReleased(MouseEvent e){
    }
}
