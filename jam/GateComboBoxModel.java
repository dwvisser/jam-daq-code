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
    private int lastSize=0;
    private Object [] lastValue;

    public GateComboBoxModel(JamCommand jamCommand){
        super();
        jc=jamCommand;
        status=JamStatus.instance();
    }

    /**
     * Needs to be called after every action that changes the list of histograms.
     */
    private void changeOccured(){
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
        if (lastValue[index]==null){
        	lastValue[index]=rval;
        } else if (!lastValue[index].equals(rval)){
        	changeOccured();
        }
        return rval;
    }

    /**
     * Number of list elements in chooser.
     */
    public int getSize(){
    	final int rval=numGates()+1;
    	if (rval != lastSize){
			lastSize=rval;
			lastValue=new Object[rval];
    		changeOccured();
    	}
		return rval;
    }

    /**
     * for ComboBoxModel interface
     * 
     * @author <a href="mailto:dale@visser.name">Dale Visser</a>
     *
     * To change the template for this generated type comment go to
     * Window>Preferences>Java>Code Generation>Code and Comments
     */
    public void setSelectedItem(Object anItem) {
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
