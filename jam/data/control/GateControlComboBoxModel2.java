package jam.data.control;
import javax.swing.*;
import jam.global.*;
import jam.data.*;
import java.util.Vector;

/**
* Data Model for JcomboBox in Add gate dialog.
*
* @author Dale Visser
*/
public class GateControlComboBoxModel2 extends AbstractListModel implements ComboBoxModel {

  String selection=null;
  public static final String NO_GATES="No Gates";
  GateControl gc;
  boolean firstTime=true;

  public GateControlComboBoxModel2(GateControl control){
    gc=control;
  }

  public Object getElementAt(int index){
    if (numGates()>0){
      //return Histogram.getHistogram(JamStatus.getCurrentHistogramName()).getGates()[index].getName();
   	  Vector list=Gate.getGateList(Histogram.getHistogram(JamStatus.getCurrentHistogramName()).getDimensionality());
      return ((Gate)(list.elementAt(index))).getName();
    } else {
      return NO_GATES;
    }
  }

  public int getSize(){
    if (numGates()>0){
      return  numGates();
    } else {
      return 1;
    }
  }

  public void setSelectedItem(Object anItem) {
      selection = (String) anItem;
      //System.err.println("GC.setSelectedItem(\""+selection+"\")");
      if (firstTime) {
        firstTime=false;
      } else {
        gc.selectGate(selection);
      }
  }

  public Object getSelectedItem() {
    return selection;
  }

  private int numGates(){
    //System.err.println("numGates(): \""+JamStatus.getCurrentHistogramName()+"\"");
    Histogram h=Histogram.getHistogram(JamStatus.getCurrentHistogramName());
    if (h != null){
      return Gate.getGateList(h.getDimensionality()).size();
    } else {
      return 0;
    }
  }

}
