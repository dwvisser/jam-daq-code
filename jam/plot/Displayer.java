package jam.plot;
import jam.data.Gate;

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
     * refresh the display should be a observer
     *
     * @param   histogram  the list of histograms.
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    /**
     * set the histogram to display
     *
     * @param   histogram  the list of histograms.
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    public void displayData(Displayable data);
    /**
     * overlay a histogram on the display
     * 
     *
     * @param   histogram  must be 1 D and must be displayin 1 D
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    public void overlayData(Displayable data);

    /**
     * What is the current histogram
     *
     * @param   histogram  the list of histograms.
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    public Displayable getData();

    /**
     * display a gate
     * 
     *
     * @param   gate
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    public void displayGate(Gate gate);    
    /**
     * What is the current plot
     * FIXME is this needed here?
     * @param   histogram  the list of histograms.
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    public Plot getPlot();    
    
    /**
     * Add a plot mouse listener
     *
     * @param   histogram  the list of histograms.
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    public void addPlotMouseListener(PlotMouseListener listener);    
    
    /**
     * remove a plot mouse listener
     *
     * @param   histogram  the list of histograms.
     * @return  <code>void</code> 
     * @since Version 0.5
     */    
    public void removePlotMouseListener(PlotMouseListener listener);  
    /**
     * tell Display to dispay a gate been made 
     */
//    public void setMakingGate(boolean on, Vector points);
    /**
     *  display setting of gate 	    
     */
//    public void showMakingGate();

}
