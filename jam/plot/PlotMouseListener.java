package jam.plot;
import java.awt.Point;

/**
 * Interface that you must implement if you want to get
 * mouse click data point. 
 */
public interface PlotMouseListener{
	
    /** 
     * Indicates the mouse was clicked on the plot.
     * 
     * @param pChannel location of click in channel coordinates
     * @param pPixel location of click in graphics coordinates
     */
    void plotMousePressed(Bin pChannel, Point pPixel);
    
}