package jam;
import javax.swing.*;
import jam.data.*;
import jam.global.JamStatus;

/**
 * Used by 'gateChooser' in class JamMain to display gate options.
 *
 * @author Dale Visser
 */
public class GateComboBoxModel extends DefaultComboBoxModel {

    String selection=null;
    public static final String NO_GATES="No Gates";
    public static final String CHOOSE_A_GATE="Choose a gate";
    JamCommand jc;
    private JamStatus status;

    public GateComboBoxModel(JamCommand jamCommand){
        super();
        jc=jamCommand;
        status=JamStatus.instance();
    }

    /**
     * Needs to be called after every action that changes the list of histograms.
     */
    void changeOccured(){
        fireContentsChanged(this,0,getSize());
    }

	/**
	 * Returns list elements for chooser.
	 */
    public Object getElementAt(int index){
    	Object rval = NO_GATES;//default value if no gates
        if (numGates()>0){
            if (index==0) {
                rval = CHOOSE_A_GATE;
            } else {
                rval = Histogram.getHistogram(status.getCurrentHistogramName()).getGates()[index-1].getName();
            }
        } 
        return rval;
    }

    /**
     * Number of list elements in chooser.
     */
    public int getSize(){
		return numGates()+1;
    }

    //for ComboBoxModel interface
    public void setSelectedItem(Object anItem) {
        //System.err.println(this.getClass().getName()+".setSelectedItem(\""+anItem+"\")");
        selection = (String) anItem;
    }

    //for ComboBoxModel interface
    public Object getSelectedItem() {
        return selection;
    }

    private int numGates(){
        int returnValue;

        Histogram hist=Histogram.getHistogram(status.getCurrentHistogramName());
        if (hist == null) {
            returnValue = 0;
        } else {
            Gate [] gates=hist.getGates();
            if (gates == null) {
                returnValue = 0;
            } else {
                returnValue = gates.length;
            }
        }
        return returnValue;
    }

}
