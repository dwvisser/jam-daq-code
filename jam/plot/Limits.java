/*
 */
package jam.plot;
import java.util.Hashtable;
import jam.global.*;
import jam.data.Histogram;
import javax.swing.*;
/**
 * Stores the parameters on how the histograms are to be displayed.  This includes
 * <ul>
 * <li>x channel display limits</li>
 * <li>y channel display limits</li>
 * <li>count range display limits</li>
 * <li>whether scale is linear or log</li>
 * </ul>
 * There is a separate instance of <code>Limits</code> for every <code>Histogram</code>.
 * The class contains a <code>static Hashtable</code> referring to all the
 * <code>Limits</code> objects by the associated <code>Histogram</code> object.
 *
 *
 */
public class Limits {

    /** Lookup table for the display limits for the various histograms.
     */
    public static Hashtable limitsList=new Hashtable(137);//should be an prime number
    //XXX
    static BoundedRangeModelY brmy;
    static BoundedRangeModelX brmx;

    static final double FAKE_ZERO=0.5;//fake zero for Log scale  1/2 a count

    private int minimumX,maximumX;
    private int minimumY,maximumY;
    private int minimumCounts,maximumCounts;
    private int zeroX, sizeX;		//translate to rangemodel min, max
    private int zeroY, sizeY;		//translate to rangemodel min, max

    private int scale;        // is it in log or linear
    private Histogram histogram;

    private BoundedRangeModel rangeModelX;
    private BoundedRangeModel rangeModelY;
    private BoundedRangeModel rangeModelCount;
    /** Creates a new set of display limits for the specified histogram.
     * @param hist Histogram for which this instance provides display limits
     */
    public Limits(Histogram hist){
        this(hist, false, false);
    }

    /** Creates the display limits for the specified histogram, specifying whether to
     * ignore first and/or last channels for auto-scaling.
     *
     * @param hist Histogram for which this object provides display limits
     * @param ignoreZero ignores channel zero for auto scaling histogram
     * @param ignoreFull ignores the last channel for auto scaling histogram
     */
    public Limits(Histogram hist, boolean ignoreZero, boolean ignoreFull){
        histogram=hist;
        limitsList.put(hist.getName(),this);
        sizeX=hist.getSizeX()-1;
        zeroX=0;
        sizeY=hist.getSizeY()-1;
        zeroY=0;
        init(hist, ignoreZero, ignoreFull);//set initial values
        //create models for scroll bars
        rangeModelX=new DefaultBoundedRangeModel();
        rangeModelY=new DefaultBoundedRangeModel();
        rangeModelCount=new DefaultBoundedRangeModel();
        //update the bounded range models
        updateModelX();
        updateModelY();
        updateModelCounts();
    }

    /**
     * limits with no histogram
     */
    public Limits(){
        sizeX=100;
        zeroX=0;
        sizeY=100;
        zeroY=0;
        minimumX=0;
        maximumX=100;
        minimumY=0;
        maximumY=100;
        minimumCounts=0;
        maximumCounts=10;
        scale=PlotGraphics.LINEAR;
        rangeModelX=new DefaultBoundedRangeModel();
        rangeModelY=new DefaultBoundedRangeModel();
        rangeModelCount=new DefaultBoundedRangeModel();
        //update the bounded range models
        updateModelX();
        updateModelY();
        updateModelCounts();
    }
    
    /**
     * Determines initial limit Values
     *
     * X and Y set to extremes
     * Auto scale counts
     */
    private void init(Histogram hist, boolean ignoreZero, boolean ignoreFull) {
        int chminX,chmaxX,chminY,chmaxY;
        double [] counts;
        double [][] counts2d;
        int [] countsInt;
        int [][] counts2dInt;

        //minimum and maximum channels
        
        int maxCounts=0;
        if((hist.getType()==Histogram.ONE_DIM_INT)||
        (hist.getType()==Histogram.ONE_DIM_DOUBLE)) {
            minimumX=0;
            maximumX=hist.getSizeX()-1;  //last channel
            minimumY=0;
            maximumY=100;
            scale=PlotGraphics.LINEAR;
        } else if ((hist.getType()==Histogram.TWO_DIM_INT)||
        (hist.getType()==Histogram.TWO_DIM_DOUBLE)) {
            minimumX=0;
            maximumX=hist.getSizeX()-1;      //last channel
            minimumY=0;
            maximumY=hist.getSizeY()-1;      //last channnel
            scale=PlotGraphics.LOG;
        } else {
            System.err.println("Error unreconized histogram type [Limits]");
        }
        //auto scale counts
        if (ignoreZero){
            chminX=1;
            chminY=1;
        } else {
            chminX=0;
            chminY=0;
        }
        if (ignoreFull){
            chmaxX=hist.getSizeX()-2;
            chmaxY=hist.getSizeY()-2;
        } else {
            chmaxX=hist.getSizeX()-1;
            chmaxY=hist.getSizeX()-1;
        }
        if (hist.getType()==Histogram.ONE_DIM_INT){
            countsInt=(int [])hist.getCounts();
            for(int i=chminX;i<=chmaxX;i++){
                if (countsInt[i]>maxCounts) {
                    maxCounts=countsInt[i];
                }
            }
        } else if (hist.getType()==Histogram.ONE_DIM_DOUBLE){
            counts=(double [])hist.getCounts();
            for(int i=chminX;i<=chmaxX;i++){
                if (counts[i]>maxCounts) {
                    maxCounts=(int)counts[i];
                }
            }
        } else if (hist.getType()==Histogram.TWO_DIM_INT){
            counts2dInt=(int [][])hist.getCounts();
            for(int i=chminX;i<=chmaxX;i++){
                for( int j=chminY;j<=chmaxY;j++){
                    if (counts2dInt[i][j]>maxCounts) {
                        maxCounts=counts2dInt[i][j];
                    }
                }
            }
        } else if (hist.getType()==Histogram.TWO_DIM_DOUBLE){
            counts2d=(double [][])hist.getCounts();
            for(int i=chminX;i<=chmaxX;i++){
                for( int j=chminY;j<=chmaxY;j++){
                    if (counts2d[i][j]>maxCounts) {
                        maxCounts=(int)counts2d[i][j];
                    }
                }
            }
        }
        minimumCounts=0;
        //set to max between 5 and auto
        if(maxCounts>5){
            maximumCounts=110*maxCounts/100;
        } else {
            maximumCounts=5;
        }
    }

    /** Get the limits for a <code>Histogram</code>.
     * @param hist Histogram to retrieve the limits for
     * @return display limits for the specified histogram
     */
    public static Limits getLimits(Histogram hist) {
        return (Limits) limitsList.get(hist.getName());
    }

    /** Get the limits for a <code>Displayable</code> object.
     * @param data object which implements the Displayable interface
     * @return the display limits for the displayable object
     */
    public static Limits getLimits(Displayable data) {
        return (Limits) limitsList.get(data.getName());
    }

    static void setBoundedRangeModelY(BoundedRangeModelY b){
        brmy=b;
    }

    static BoundedRangeModelY getBoundedRangeModelY(){
        return brmy;
    }

    static void setBoundedRangeModelX(BoundedRangeModelX b){
        brmx=b;
    }

    static BoundedRangeModelX getBoundedRangeModelX(){
        return brmx;
    }

    //KBS
    /**
     * scroller attaced to limits for X
     */
    BoundedRangeModel getModelX(){
        return rangeModelX;
    }
    /**
     * scroller attaced to limits for Y
     */
    BoundedRangeModel getModelY(){
        return rangeModelY;
    }
    /**
     * scroller attaced to limits for Y
     */
    BoundedRangeModel getModelCount(){
        return rangeModelCount;
    }

    /** Set the limits for the horizontal dimension.
     * @param minX new minimum x value
     * @param maxX new maximum x value
     */
    public void setLimitsX(int minX, int maxX){
        minimumX=minX;
        maximumX=maxX;
        updateModelX();
    }

    /** Set the minimimum X limit.
     * @param minX new minimum x value
     */
    public void setMinimumX(int minX){
        minimumX=minX;
        updateModelX();
    }

    /*
     * Set the maximum X limit
     */
    /** Sets the new maximum x value.
     * @param maxX the new maximum x value
     */
    public void setMaximumX(int maxX){
        maximumX=maxX;
        updateModelX();
    }

    /**
     * set the Y limits
     */
    public void setLimitsY(int minY, int maxY){
        minimumY=minY;
        maximumY=maxY;
        updateModelY();
    }

    /**
     * Set the minimimum Y limit
     */
    public void setMinimumY(int minY){
        //System.err.println("minY: "+minY);
        minimumY=minY;
        updateModelY();
        //XXXKBSif (!delayAction) brmy.setYDisplayLimits();
    }

    /**
     * Set the maximum Y limit
     */
    public void setMaximumY(int maxY){
        maximumY=maxY;
        updateModelY();
    }

    /**
     * set the Count limits
     */
    public void setLimitsCounts(int minCounts, int maxCounts){
        minimumCounts=minCounts;
        maximumCounts=maxCounts;
        updateModelCounts();
    }

    /**
     * set the minimum Count limits
     */
    public void setMinimumCounts(int minCounts){
        minimumCounts=minCounts;
        updateModelCounts();
    }

    /**
     * set the maximum Count limits
     */
    public void setMaximumCounts(int maxCounts){
        maximumCounts=maxCounts;
        updateModelCounts();
    }

    /**
     * Set the scale to
     *  linear  <code> Limits.LINEAR</code>
     *  log  <code> Limits.LOG </code>
     */
    public void setScale(int s){
        scale=s;
    }
    /**
     * update the values from the model
     */
    public void update(){

        minimumX=rangeModelX.getValue();
        maximumX=minimumX+rangeModelX.getExtent();

        //y scroller goes the wrong way.
        minimumY=sizeY-rangeModelY.getValue()-rangeModelY.getExtent();
        maximumY=sizeY-rangeModelY.getValue();

        minimumCounts=minimumCounts;
        maximumCounts=maximumCounts;
    }

    /**
     * update range model X
     * this updates the scroll bars
     */
    private void updateModelX(){
        int value=minimumX;
        int extent=maximumX-minimumX;
        rangeModelX.setRangeProperties(value, extent, zeroX, sizeX, false);

    }
    /**
     * update range model Y
     * this updates the scroll bars
     */
    private void updateModelY(){
        //Y scroll bar move oppersite direction to what we want
        int value=sizeY-maximumY;
        int extent=maximumY-minimumY;
        rangeModelY.setRangeProperties(value, extent, zeroY, sizeY, false);
    }
    
    /**
     * update range model x
     * this updates the scroll bars
     */
    private void updateModelCounts(){
        /*int value=minimumCounts;
        int extent=maximumCounts-minimumCounts;*/
    }

    /**
     *
     */
    public int getMinimumX(){
        return(minimumX);
    }

    public int getMaximumX(){
        return(maximumX);
    }

    /**
     * get lower y limit
     */
    public int getMinimumY(){
        return minimumY;
    }

    public int getMaximumY(){
        return maximumY;
    }

    /**
     * Counts limits
     */
    public int getMinimumCounts(){
        return minimumCounts;
    }

    /**
     * Counts limits
     */
    public int getMaximumCounts(){
        return maximumCounts;
    }

    /**
     * Get the scale, linear or Log.
     *
     * @return <code>Limits.LINEAR</code> or <code>Limits.LOG</code>
     */
    public int getScale(){
        return scale;
    }

    public String toString(){
        String temp;
        temp="Limits of \""+histogram.getName()+"\": ";
        temp += "MinX: "+getMinimumX()+", MaxX: "+getMaximumX();
        return temp;
    }
}

