package jam.plot;
import jam.data.*;
import javax.swing.*;

/**
* <code>Scroller</code> contains instance of this, and it is be handed
* to the horizontal <code>JScrollBar</code>.  <code>setFields()</code> is to
* be called whenever the displayed <code>Histogram</code> changes.
*
* @author Dale Visser
* @version 1.2
*/
public final class BoundedRangeModelX extends DefaultBoundedRangeModel {


  Histogram hist;
  Limits lim;
  Plot plot;

  BoundedRangeModelX(Plot p){
    super();
    setFields(p);
  }

  void setFields(Plot p){
    plot=p;
    hist=plot.getHistogram();
    if (hist!=null){
      lim=Limits.getLimits(hist);
    }
    setXDisplayLimits();
  }

  void scrollBarMoved(){
    int maxX=getValue()+getExtent()/*-1*/;
    int minX=getValue();
    if (lim !=null){
      lim.setLimitsX(minX,maxX);
    }
  }

  /**
   * Set model using values in Limits object.
   */
  void setXDisplayLimits() {
    int min,max,extent,value;
    min=0;
    max=plot.getSizeX()-1;
    if (lim != null) {
      extent = lim.getMaximumX()-lim.getMinimumX()/*+1*/;
      value = lim.getMinimumX();
    } else {
      extent = max-min+1;
      value=0;
    }
    /* BoundedRangeModel method, throws appropriate event up to 
     * scroll bar.
     */
    setRangeProperties(value,extent,min,max,true);
  }
}
