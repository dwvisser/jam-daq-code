/*
 */
package jam.data.control;
import jam.data.*;
import java.awt.*;
import javax.swing.*;

/**
 * Class that is a bar graph used by monitors
 *
 * @version 0.5
 * @author Ken Swartz
 */
public class PlotBar extends JPanel implements PlotBarLayout {


    // configuration for page plotting are set using printHistogram
    protected Dimension pageSize;
    private Monitor monitor;
    /**
     * Constructor
     */
    public PlotBar(Monitor monitor){
      this.monitor=monitor;
      this.setBackground(SystemColor.control);
      this.setForeground(SystemColor.controlHighlight);
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
    public synchronized void paintComponent(Graphics g){
		//paint background first
		super.paintComponent(g);

      	//overall properties
      int plotLength, thresholdLine;
      int length, height;
      //user custom settings
      double value,threshold,maximum;
      Dimension dim;

      value=monitor.getValue();
      threshold=monitor.getThreshold();
      maximum=monitor.getMaximum();
      dim=getSize();

      //orientation of plot and size
      length=dim.width-2*BORDER_END;
	  height =dim.height-2*BORDER_SIDE;

      //make sure input is OK
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
      g.clearRect(BORDER_END, BORDER_SIDE, length, height-1);
      g.setColor(SystemColor.control);
      g.fillRect(BORDER_END, BORDER_SIDE, length, height-1);

      //draw outline
      g.setColor(SystemColor.textText);
      //g.setColor(Color.black);
      g.drawRect(BORDER_END, BORDER_SIDE, length, height-1);

      //Draw bar color depending on threshold and maximum
      if ((value<threshold)||(value>maximum)){
        g.setColor(Color.red);
      } else {
        g.setColor(Color.green);
      }

      g.fillRect(BORDER_END+1, BORDER_SIDE+1, plotLength, height-2);

	  //draw threshold
      g.setColor(SystemColor.textText);
	 //g.setColor(Color.black);
      g.drawLine(BORDER_END+thresholdLine, BORDER_SIDE+(height/2),
          BORDER_END+thresholdLine, BORDER_SIDE+height-1);
    }
}
