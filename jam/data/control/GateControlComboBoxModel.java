package jam.data.control;
import javax.swing.*;
import jam.global.*;
import jam.data.*;

/**
 * Data Model for JcomboBox in Gate set dialog.
 *
 * @author Dale Visser
 */
public class GateControlComboBoxModel extends AbstractListModel implements ComboBoxModel {

    String selection=null;
    public static final String CHOOSE_A_GATE="Choose a gate";
    public static final String NO_GATES="No Gates";
    GateControl gc;
    boolean firstTime=true;

    public GateControlComboBoxModel(GateControl control){
        gc=control;
    }

    /**
     * Needs to be called after every action that changes the list of histograms.
     */
    void changeOccured(){
        fireContentsChanged(this,0,getSize());
    }

    public Object getElementAt(int index){
        if (numGates()>0){
            if (index==0) {
                return CHOOSE_A_GATE;
            } else {
                return Histogram.getHistogram(JamStatus.getCurrentHistogramName()).getGates()[index-1].getName();
            }
        } else {
            return NO_GATES;
        }
    }

    public int getSize(){
        if (numGates()>0){
            return  numGates()+1;
        } else {
            return 1;
        }
    }

    public void setSelectedItem(Object anItem) {
        selection = (String) anItem;
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
            return h.getGates().length;
        } else {
            return 0;
        }
    }

}
