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
    	Object rval=NO_GATES;//default return value
        if (numGates()>0){
            if (index==0) {
                rval = CHOOSE_A_GATE;
            } else {
                rval = Histogram.getHistogram(JamStatus.getCurrentHistogramName()).getGates()[index-1].getName();
            }
        } 
        return rval;
    }

	/**
	 * Returns number of list elements in chooser.
	 */
    public int getSize(){
		return numGates()+1;
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
    	int rval=0;//default return value
        Histogram h=Histogram.getHistogram(JamStatus.getCurrentHistogramName());
        if (h != null){
            rval = h.getGates().length;
        } 
        return rval;
    }

}
