/*
 */
package jam.data.control;
import jam.data.*;
import java.awt.*;

/**
 * Class that is a bar graph used by monitors
 *
 * @version 0.5
 * @author Ken Swartz
 */
public class PlotBar extends Canvas implements PlotBarLayout {


    // configuration for page plotting are set using printHistogram
    protected Dimension pageSize;
    private Monitor monitor;
    /**
     * Constructor
     */
    public PlotBar(Monitor monitor){
      this.monitor=monitor;
      this.setBackground(Color.lightGray);
      this.setForeground(Color.black);
    }

    /**
     * Sets the monitor object which is observed by this PlotBar.
     *
     * @param monitor to be observed
     */
    public void setMonitor(Monitor monitor){
      this.monitor=monitor;
    }

    /**
     * get the monitor that is ploted
     */
    public Monitor getMonitor(){
      return monitor;
    }

    /**
     * paint method that is called to redraw widget
     */
    public synchronized void paint(Graphics g){
      //overall properties
      int plotLength, thresholdLine,length,width;
      //user custom settings
      double value,threshold,maximum;
      Dimension dim;

      value=monitor.getValue();
      threshold=monitor.getThreshold();
      maximum=monitor.getMaximum();
      dim=getSize();

      //orientation of plot and size
      length=dim.width-2*BORDER_END;
      width=dim.height;

      //make sure input OK
      if(maximum>0){
        plotLength=(int)(length*value/maximum);
        thresholdLine=(int)(length*threshold/maximum);
      } else {
        plotLength=0;
        thresholdLine=0;
      }

      if(plotLength>=length){
        plotLength=length-1;
      }

      //clear area
      g.clearRect(BORDER_END, BORDER_SIDE, length, BAR_WIDTH);
      g.setColor(Color.lightGray);
      g.fillRect(BORDER_END, BORDER_SIDE, length, BAR_WIDTH);
      //draw outline
      g.setColor(Color.black);
      g.drawRect(BORDER_END, BORDER_SIDE, length, BAR_WIDTH);

      //Draw bar color depending on threshold and maximum
      if ((value<threshold)||(value>maximum)){
        g.setColor(Color.red);
      } else {
        g.setColor(Color.green);
      }
      g.fillRect(BORDER_END+1, BORDER_SIDE+1, plotLength, BAR_WIDTH-1);

      g.setColor(Color.black);
      //draw threshold
      g.drawLine(BORDER_END+thresholdLine, BORDER_SIDE+(BAR_WIDTH/2),
          BORDER_END+thresholdLine, BORDER_SIDE+BAR_WIDTH);
    }
}
