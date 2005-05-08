package jam.plot;
import jam.data.Histogram;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

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
final class Limits {


    /** 
     * Lookup table for the display limits for the various histograms.
     */
    private final static Map TABLE=Collections.synchronizedMap(new HashMap());

    private static final int INITLO=0;
    private static final int INITHI=100;
    private static final int DEFAULTMAXCOUNTS=5;

	private final String histName;
	private final BoundedRangeModel rangeModelX=new DefaultBoundedRangeModel();
	private final BoundedRangeModel rangeModelY=new DefaultBoundedRangeModel();

    private int minimumX,maximumX;
    private int minimumY,maximumY;
    private int minimumCounts,maximumCounts;
    private int zeroX=INITLO;
    private int sizeX;		//translate to rangemodel min, max
    private int zeroY=INITLO;
    private int sizeY;		//translate to rangemodel min, max
    private Scale scale=Scale.LINEAR;        // is it in log or linear
    
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
    private Limits(Histogram hist, boolean ignoreZero, boolean ignoreFull){
    	if (hist == null) {
			throw new IllegalArgumentException(
					"Can't have null histogram reference in Limits constructor.");
		}
        histName=hist.getFullName();
        TABLE.put(histName,this);
        sizeX=hist.getSizeX()-1;
        sizeY=hist.getSizeY()-1;
        init(ignoreZero, ignoreFull);//set initial values
        /* update the bounded range models */
        updateModelX();
        updateModelY();
    }
    
    private Limits(){
    	histName=null;
    	sizeX=0;
    	sizeY=0;
        updateModelX();
        updateModelY();
    }
    
    private static final Limits LIMITS_NULL=new Limits();
    
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
    private void init(boolean ignoreZero, 
    boolean ignoreFull) {
    	final Histogram histogram=Histogram.getHistogram(histName);
		final int dim=histogram.getDimensionality();
		final int sizex=histogram.getSizeX();
		final int sizey=histogram.getSizeY();
        setLimitsX(INITLO,sizex-1);
        if (dim==1) {
            setLimitsY(INITLO,INITHI);
            setScale(Scale.LINEAR);
        } else {//2-dim
            setLimitsY(INITLO,sizey-1);
            setScale(Scale.LOG);
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
    
    private int getMaxCounts(int chminX, int chmaxX, int chminY, 
    int chmaxY) {
		int maxCounts;
		
		final int scaleUp=110;
		final int scaleBackDown=100;
		final Histogram histogram=Histogram.getHistogram(histName);
		final Object counts=histogram.getCounts();
		if (histogram.getDimensionality()==1){
			maxCounts=getMaxCounts(counts,chminX,chmaxX);
		} else {//dim==2
			maxCounts=getMaxCounts(counts,chminX,chmaxX,chminY,chmaxY);
		} 
		maxCounts *= scaleUp;
		maxCounts /= scaleBackDown;
		return maxCounts;
    }
    
    private int getMaxCounts(Object counts, int chminX, int chmaxX){
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

    
    private int getMaxCounts(Object counts, int chminX, int chmaxX,
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
    
    /** 
     * Get the limits for a <code>Histogram</code>.
     * @param hist Histogram to retrieve the limits for
     * @return display limits for the specified histogram
     */
    static Limits getLimits(Histogram hist) {
		final Limits rval;
		if (hist == null) {
			rval = LIMITS_NULL;
		} else {
			final Object o = TABLE.get(hist.getFullName());
			if (o == null) {
				final Preferences prefs = PlotPrefs.PREFS;
				final boolean ignoreZero = prefs.getBoolean(
						PlotPrefs.AUTO_IGNORE_ZERO, true);
				final boolean ignoreFull = prefs.getBoolean(
						PlotPrefs.AUTO_IGNORE_FULL, true);
				rval = new Limits(hist, ignoreZero, ignoreFull);
			} else {
				rval = (Limits) o;
			}
		}
		return rval;
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
    
    /** Set the limits for the horizontal dimension.
     * @param minX new minimum x value
     * @param maxX new maximum x value
     */
    synchronized void setLimitsX(int minX, int maxX){
        minimumX=minX;
        maximumX=maxX;
        updateModelX();
    }

    /** Set the minimimum X limit.
     * @param minX new minimum x value
     */
    synchronized void setMinimumX(int minX){
        minimumX=minX;
        updateModelX();
    }

    /** Sets the new maximum x value.
     * @param maxX the new maximum x value
     */
    synchronized void setMaximumX(int maxX){
        maximumX=maxX;
        updateModelX();
    }

    /**
     * Set the Y limits.
     * 
     * @param minY minumum Y to display
     * @param maxY maximum Y to display
     */
    synchronized void setLimitsY(int minY, int maxY){
        minimumY=minY;
        maximumY=maxY;
        updateModelY();
    }

    /**
     * Set the minimimum Y limit.
     *
     * @param minY minumum Y to display
     */
    synchronized void setMinimumY(int minY){
        minimumY=minY;
        updateModelY();
    }

    /**
     * Set the maximum Y limit.
     *
     * @param maxY maximum Y to display
     */
    synchronized void setMaximumY(int maxY){
        maximumY=maxY;
        updateModelY();
    }

    /**
     * Set the Count limits.
     * 
     * @param minCounts the lowest count value to display
     * @param maxCounts the highest count value to display
     */
    private synchronized void setLimitsCounts(int minCounts, 
    int maxCounts){
        minimumCounts=minCounts;
        maximumCounts=maxCounts;
    }

    /**
     * Set the minimum count limit.
     * 
     * @param minCounts the lowest count value to display
     */
    void setMinimumCounts(int minCounts){
		synchronized(this){
			minimumCounts=minCounts;
		}
    }

    /**
     * Set the maximum count limit.
     * 
     * @param maxCounts the highest count value to display
     */
    void setMaximumCounts(int maxCounts){
        synchronized(this){
			maximumCounts=maxCounts;
        }
    }

    /**
     * Set the scale to log or linear.
     *
     * @param s one of <code>Limits.LINEAR</code> or <code>
     * Limits.LOG</code>
     */
    void setScale(Scale s){
    	synchronized(scale){
			scale=s;
    	}
    }
    
    /**
     * update the values from the model
     */
    synchronized void update(){
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
    synchronized int getMinimumX(){
        return minimumX;
    }

	/**
	 * @return the highest x-channel to display
	 */
    synchronized int getMaximumX(){
        return maximumX;
    }

    /**
     * @return the lowest y-channel to display
     */
    synchronized int getMinimumY(){
        return minimumY;
    }

	/**
	 * @return the highest y-channel to display
	 */
    synchronized int getMaximumY(){
        return maximumY;
    }

    /**
     * @return the minimum count level to be displayed
     */
    synchronized int getMinimumCounts(){
        return minimumCounts;
    }

    /**
     * @return the maximum count level to be displayed
     */
    synchronized int getMaximumCounts(){
        return maximumCounts;
    }

    /**
     * Get the scale, linear or Log.
     *
     * @return <code>PlotGraphics.LINEAR</code> or <code>
     * PlotGraphics.LOG</code>
     */
    synchronized Scale getScale(){
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
        temp.append(histName);
        temp.append(colon);
        temp.append(xmin);
        temp.append(getMinimumX());
        temp.append(xmax);
        temp.append(getMaximumX());
        return temp.toString();
    }
    
}

