package jam.plot;
import jam.data.Histogram;

import javax.swing.DefaultBoundedRangeModel;

/**
* <code>Scroller</code> contains instance of this, and it is be handed
* to the vertical <code>JScrollBar</code>.  <code>setFields()</code> is to
* be called whenever the displayed <code>Histogram</code> changes.
*
* @author Dale Visser
* @version 1.2
*/
final class BoundedRangeModelY extends DefaultBoundedRangeModel {


  Histogram hist;
  Limits lim;
  Plot plot;

  BoundedRangeModelY(Plot p){
    super();
    setFields(p);
  }

  void setFields(Plot p){
    plot=p;
    hist=plot.getHistogram();
    if (hist!=null){
      lim=Limits.getLimits(hist);
    }
    setYDisplayLimits();
  }

  void scrollBarMoved(){
    int minY,maxY;
    maxY=plot.getSizeY()-1-getValue();
    minY=maxY-getExtent()/*+1*/;
    if (lim !=null){
      lim.setLimitsY(minY,maxY);
    }
  }

  /**
   * Set model using values in Limits object.
   */
  void setYDisplayLimits() {
    int min,max,extent,value;
    min=0;
    max=plot.getSizeY()-1;
    if (lim !=null){
      extent=lim.getMaximumY()-lim.getMinimumY()/*+1*/;
      value=max-lim.getMaximumY();
    } else {
      extent = max-min+1;
      value=0;
    }
    setRangeProperties(value,extent,min,max,true);//BoundedRangeModel method
  }
}
