package jam.data.control;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.JamStatus;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
* Data Model for JComboBox in Add gate dialog.
*
* @author <a href="mailto:dale@visser.name">Dale Visser</a>
*/
public class GateControlComboBoxModel2
	extends AbstractListModel
	implements ComboBoxModel {

	String selection = null;
	public static final String NO_GATES = "No Gates";
    public static final String CHOOSE_A_GATE="Choose a gate";
	GateControl gc;
	private JamStatus status;

	public GateControlComboBoxModel2(GateControl control) {
		gc = control;
		status = JamStatus.instance();
	}

	public Object getElementAt(int index) {
		Object rval = NO_GATES; //default return value
		if (numGates() > 0) {
			if (index==0) {
				rval = CHOOSE_A_GATE;
			} else {
				List list =
					Gate.getGateList(
					Histogram
						.getHistogram(status.getCurrentHistogramName())
						.getDimensionality());
				rval = ((Gate) (list.get(index-1))).getName();
			}
		} 
		return rval;
	}

	public int getSize() {
		int n = numGates();
		return Math.max(1, n+1);
	}

	public void setSelectedItem(Object anItem) {
		selection = (String) anItem;
		if ((!selection.equals(CHOOSE_A_GATE)) && 
		(!selection.equals(NO_GATES))){
			gc.selectGateAdd(selection);
		}
	}

	public Object getSelectedItem() {
		return selection;
	}

	private int numGates() {
		int rval=0;//default return value
		Histogram h =
			Histogram.getHistogram(status.getCurrentHistogramName());
		if (h != null) {
			rval = Gate.getGateList(h.getDimensionality()).size();
		} 
		return rval;
	}
	
    /**
     * Needs to be called after every action that changes the list of histograms.
     */
    void changeOccured(){
        fireContentsChanged(this,0,getSize());
    }

}
