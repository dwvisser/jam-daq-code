/* 
*/
package jam.plot;
import java.awt.*;
/**
 * Interface that you must implement if you want to get
 * mouse click data point. 
 * Data point in the channels and pixels. 
 *
 */

public interface PlotMouseListener{
    /** 
     * 
     */
    public void plotMousePressed(Point pChannel, Point pPixel);

}