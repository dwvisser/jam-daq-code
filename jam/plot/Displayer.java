package jam.plot;

/**
 * This interface for the display package 
 * It is implemented by <code>Display</code>.
 * <p>
 * It lists the methods that a dislay package or class should have
 * 
 * @version	0.5 April 98
 * @author 	Ken Swartz
 * @since       JDK1.1
 */
interface Displayer {
    /**
     * set the histogram to display
     *
     * @param   histogram  the list of histograms.
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    void displayData(Displayable data);
    
    /**
     * overlay a histogram on the display
     * 
     *
     * @param   histogram  must be 1 D and must be displayin 1 D
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    void overlayData(Displayable data);

    /**
     * Return the current displayed data.
     *
     * @param   histogram  the list of histograms.
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    Displayable getData();

    /**
     * What is the current plot
     * FIXME is this needed here?
     * @param   histogram  the list of histograms.
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    Plot getPlot();    
    
    /**
     * Add a plot mouse listener
     *
     * @param   histogram  the list of histograms.
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    void addPlotMouseListener(PlotMouseListener listener);    
    
    /**
     * remove a plot mouse listener
     *
     * @param   histogram  the list of histograms.
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    void removePlotMouseListener(PlotMouseListener listener);  
}
