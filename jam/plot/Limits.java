package jam.plot;
import jam.data.Histogram;
import java.util.Hashtable;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;

/**
 * Stores the parameters on how the histograms are to be displayed.  
 * This includes
 * <ul>
 * <li>x channel display limits</li>
 * <li>y channel display limits</li>
 * <li>count range display limits</li>
 * <li>whether scale is linear or log</li>
 * </ul>
 * There is a separate instance of <code>Limits</code> for every 
 * <code>Histogram</code>. The class contains a <code>static 
 * Hashtable</code> referring to all the <code>Limits</code> objects 
 * by the associated <code>Histogram</code> object.
 *
 * @author Ken Swartz
 * @author Dale Visser
 * @version 1.4
 */
public class Limits {

    static class ScaleType{
    	/**
    	 * Value representing linear counts scale.
    	 */
    	static public final ScaleType LINEAR=new ScaleType(0);
    	
    	/**
    	 * Value representing log counts scale.
    	 */
    	static public final ScaleType LOG=new ScaleType(1);
    	private int type;
    	
    	private ScaleType(int t){
    		type=t;	
    	}
    }

    /** 
     * Lookup table for the display limits for the various histograms.
     */
    private final static Hashtable TABLE=new Hashtable();

    private static final int INITLO=0;
    private static final int INITHI=100;
    private static final int DEFAULTMAXCOUNTS=5;

	private final Histogram histogram;
	private final BoundedRangeModel rangeModelX=new DefaultBoundedRangeModel();
	private final BoundedRangeModel rangeModelY=new DefaultBoundedRangeModel();
	private final BoundedRangeModel rangeModelCount=new DefaultBoundedRangeModel();

    private int minimumX,maximumX;
    private int minimumY,maximumY;
    private int minimumCounts,maximumCounts;
    private int zeroX, sizeX;		//translate to rangemodel min, max
    private int zeroY, sizeY;		//translate to rangemodel min, max
    private Limits.ScaleType scale;        // is it in log or linear
    
    /**
     * limits with no histogram
     */
    public Limits(){
        sizeX=INITHI;
        zeroX=INITLO;
        sizeY=INITHI;
        zeroY=INITLO;
        minimumX=INITLO;
        maximumX=INITHI;
        minimumY=INITLO;
        maximumY=INITHI;
        minimumCounts=INITLO;
        maximumCounts=DEFAULTMAXCOUNTS;
        scale=Limits.ScaleType.LINEAR;
        //update the bounded range models
        updateModelX();
        updateModelY();
        //updateModelCounts();
        histogram=null;
    }
    
    /** 
     * Creates a new set of display limits for the specified 
     * histogram.
     * @param hist Histogram for which this instance provides display 
     * limits
     */
    public Limits(Histogram hist){
        this(hist, false, false);
    }

    /** 
     * Creates the display limits for the specified histogram, 
     * specifying whether to ignore first and/or last channels for 
     * auto-scaling.
     *
     * @param hist Histogram for which this object provides display 
     * limits
     * @param ignoreZero ignores channel zero for auto scaling 
     * histogram
     * @param ignoreFull ignores the last channel for auto scaling 
     * histogram
     */
    public Limits(Histogram hist, boolean ignoreZero, boolean ignoreFull){
        histogram=hist;
        TABLE.put(hist.getName(),this);
        sizeX=hist.getSizeX()-1;
        zeroX=INITLO;
        sizeY=hist.getSizeY()-1;
        zeroY=INITLO;
        init(ignoreZero, ignoreFull);//set initial values
        //update the bounded range models
        updateModelX();
        updateModelY();
    }

    
    /**
     * Determines initial limit values, X and Y limits set to 
     * extremes for the given histogram, and the counts scale is 
     * auto-scaled.
     *
     * @param ignoreZero true if the zero channel is ignored for
     * auto-scaling
     * @param ignoreFull true if the last channel is ignored for
     * auto-scaling
     */
    private final void init(boolean ignoreZero, 
    boolean ignoreFull) {
		final int dim=histogram.getDimensionality();
		final int sizex=histogram.getSizeX();
		final int sizey=histogram.getSizeY();
        setLimitsX(INITLO,sizex-1);
        if (dim==1) {
            setLimitsY(INITLO,INITHI);
            setScale(Limits.ScaleType.LINEAR);
        } else {//2-dim
            setLimitsY(INITLO,sizey-1);
            setScale(Limits.ScaleType.LOG);
        }
        /* auto scale counts */
		int chminX=0;
		int chminY=0;
        if (ignoreZero){
            chminX=1;
            chminY=1;
        } 
        int chmaxX=sizex;
        int chmaxY=sizey;
        int diff=1;
        if (ignoreFull){
        	diff=2;
        } 
		chmaxX -= diff;
		chmaxY -= diff;
		final int maxCounts=getMaxCounts(chminX,chmaxX,chminY,chmaxY);
		setLimitsCounts(INITLO, maxCounts);
    }
    
    private final int getMaxCounts(int chminX, int chmaxX, int chminY, 
    int chmaxY) {
		int maxCounts;
		
		final int scaleUp=110;
		final int scaleBackDown=100;
		final Object counts=histogram.getCounts();
		if (histogram.getDimensionality()==1){
			maxCounts=getMaxCounts(counts,chminX,chmaxX);
		} else {//dim==2
			//final int [][] counts2dInt=(int [][])counts;
			maxCounts=getMaxCounts(counts,chminX,chmaxX,chminY,chmaxY);
		} /*else {//htype==Histogram.TWO_DIM_DOUBLE
			final double [][] counts2d=(double [][])counts;
			maxCounts=getMaxCounts(counts2d,chminX,chmaxX,chminY,chmaxY);
		}*/
		maxCounts *= scaleUp;
		maxCounts /= scaleBackDown;
		return maxCounts;
    }
    
    private final int getMaxCounts(Object counts, int chminX, int chmaxX){
    	int maxCounts=DEFAULTMAXCOUNTS;
    	if (counts instanceof double[]){
    		final double [] countsD=(double [])counts;
			for(int i=chminX;i<=chmaxX;i++){
				maxCounts = Math.max(maxCounts, (int)countsD[i]);
			}
		} else {// int[]
			final int [] countsInt=(int [])counts;
			for(int i=chminX;i<=chmaxX;i++){
				maxCounts = Math.max(maxCounts,countsInt[i]);
			}
		}
		return maxCounts;
	}

    
    private final int getMaxCounts(Object counts, int chminX, int chmaxX,
    int chminY, int chmaxY){
   		int maxCounts=DEFAULTMAXCOUNTS;
    	if (counts instanceof double[][]){
    		final double [][] counts2d=(double [][])counts;
        	for(int i=chminX;i<=chmaxX;i++){
            	for(int j=chminY;j<=chmaxY;j++){
                	maxCounts=Math.max(maxCounts,(int)counts2d[i][j]);
            	}
        	}
        } else {//instanceof int [][]
        	final int [][] counts2d=(int [][])counts;
        	for(int i=chminX;i<=chmaxX;i++){
            	for(int j=chminY;j<=chmaxY;j++){
                	maxCounts=Math.max(maxCounts,counts2d[i][j]);
            	}
        	}
        }
        return maxCounts;
    }
    
    /*private final int getMaxCounts(int [][] counts2d, int chminX, int chmaxX,
    int chminY, int chmaxY){
    	int maxCounts=DEFAULTMAXCOUNTS;
        for(int i=chminX;i<=chmaxX;i++){
            for(int j=chminY;j<=chmaxY;j++){
                maxCounts=Math.max(maxCounts,counts2d[i][j]);
            }
        }
        return maxCounts;
    }*/
    
    /** Get the limits for a <code>Histogram</code>.
     * @param hist Histogram to retrieve the limits for
     * @return display limits for the specified histogram
     */
    public static Limits getLimits(Histogram hist) {
        return (Limits) TABLE.get(hist.getName());
    }

    /** Get the limits for a <code>Displayable</code> object.
     * @param data object which implements the Displayable interface
     * @return the display limits for the displayable object
     */
    public static Limits getLimits(Displayable data) {
        return (Limits) TABLE.get(data.getName());
    }

    /**
     * @return model for scrollbar attached to X-limits
     */
    BoundedRangeModel getModelX(){
        return rangeModelX;
    }
    
    /**
     * @return model for scrollbar attached to Y-limits
     */
    BoundedRangeModel getModelY(){
        return rangeModelY;
    }
    
    /**
     * @return model for scroller attached to counts limits
     */
    BoundedRangeModel getModelCount(){
        return rangeModelCount;
    }

    /** Set the limits for the horizontal dimension.
     * @param minX new minimum x value
     * @param maxX new maximum x value
     */
    public synchronized final void setLimitsX(int minX, int maxX){
        minimumX=minX;
        maximumX=maxX;
        updateModelX();
    }

    /** Set the minimimum X limit.
     * @param minX new minimum x value
     */
    public synchronized void setMinimumX(int minX){
        minimumX=minX;
        updateModelX();
    }

    /*
     * Set the maximum X limit
     */
    /** Sets the new maximum x value.
     * @param maxX the new maximum x value
     */
    public synchronized void setMaximumX(int maxX){
        maximumX=maxX;
        updateModelX();
    }

    /**
     * Set the Y limits.
     * 
     * @param minY minumum Y to display
     * @param maxY maximum Y to display
     */
    public final synchronized void setLimitsY(int minY, int maxY){
        minimumY=minY;
        maximumY=maxY;
        updateModelY();
    }

    /**
     * Set the minimimum Y limit.
     *
     * @param minY minumum Y to display
     */
    public synchronized void setMinimumY(int minY){
        minimumY=minY;
        updateModelY();
    }

    /**
     * Set the maximum Y limit.
     *
     * @param maxY maximum Y to display
     */
    public synchronized void setMaximumY(int maxY){
        maximumY=maxY;
        updateModelY();
    }

    /**
     * Set the Count limits.
     * 
     * @param minCounts the lowest count value to display
     * @param maxCounts the highest count value to display
     */
    public synchronized final void setLimitsCounts(int minCounts, 
    int maxCounts){
        minimumCounts=minCounts;
        maximumCounts=maxCounts;
    }

    /**
     * Set the minimum count limit.
     * 
     * @param minCounts the lowest count value to display
     */
    public synchronized final void setMinimumCounts(int minCounts){
        minimumCounts=minCounts;
    }

    /**
     * Set the maximum count limit.
     * 
     * @param maxCounts the highest count value to display
     */
    public synchronized final void setMaximumCounts(int maxCounts){
        maximumCounts=maxCounts;
    }

    /**
     * Set the scale to log or linear.
     *
     * @param s one of <code>Limits.LINEAR</code> or <code>
     * Limits.LOG</code>
     */
    public synchronized final void setScale(Limits.ScaleType s){
        scale=s;
    }
    
    /**
     * update the values from the model
     */
    public synchronized void update(){
        minimumX=rangeModelX.getValue();
        maximumX=minimumX+rangeModelX.getExtent();
        minimumY=sizeY-rangeModelY.getValue()-rangeModelY.getExtent();
        maximumY=sizeY-rangeModelY.getValue();
    }

    /**
     * update range model X
     * this updates the scroll bars
     */
    private void updateModelX(){
        final int value=minimumX;
        final int extent=maximumX-minimumX;
        rangeModelX.setRangeProperties(value, extent, zeroX, sizeX, false);
    }
    
    /**
     * update range model Y
     * this updates the scroll bars
     */
    private void updateModelY(){
        final int value=sizeY-maximumY;
        final int extent=maximumY-minimumY;
        rangeModelY.setRangeProperties(value, extent, zeroY, sizeY, false);
    }
    
    /**
     * @return the lowest x-channel to display
     */
    public int getMinimumX(){
        return(minimumX);
    }

	/**
	 * @return the highest x-channel to display
	 */
    public int getMaximumX(){
        return(maximumX);
    }

    /**
     * @return the lowest y-channel to display
     */
    public int getMinimumY(){
        return minimumY;
    }

	/**
	 * @return the highest y-channel to display
	 */
    public int getMaximumY(){
        return maximumY;
    }

    /**
     * @return the minimum count level to be displayed
     */
    public int getMinimumCounts(){
        return minimumCounts;
    }

    /**
     * @return the maximum count level to be displayed
     */
    public int getMaximumCounts(){
        return maximumCounts;
    }

    /**
     * Get the scale, linear or Log.
     *
     * @return <code>PlotGraphics.LINEAR</code> or <code>
     * PlotGraphics.LOG</code>
     */
    public Limits.ScaleType getScale(){
        return scale;
    }

	/**
	 * @return text giving the histogram and the X-limits
	 */
    public String toString(){
    	final String limitsof="Limits of \"";
    	final String colon="\": ";
    	final String xmin="MinX: ";
    	final String xmax=", MaxX: ";
        final StringBuffer temp=new StringBuffer(limitsof);
        temp.append(histogram.getName());
        temp.append(colon);
        temp.append(xmin);
        temp.append(getMinimumX());
        temp.append(xmax);
        temp.append(getMaximumX());
        return temp.toString();
    }
    
}

