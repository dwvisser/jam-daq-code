package jam.data.control;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.JamStatus;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
* Data Model for JcomboBox in Add gate dialog.
*
* @author Dale Visser
*/
public class GateControlComboBoxModel2
	extends AbstractListModel
	implements ComboBoxModel {

	String selection = null;
	public static final String NO_GATES = "No Gates";
    public static final String CHOOSE_A_GATE="Choose a gate";
	GateControl gc;

	public GateControlComboBoxModel2(GateControl control) {
		gc = control;
	}

	public Object getElementAt(int index) {
		if (numGates() > 0) {
			if (index==0) {
				return this.CHOOSE_A_GATE;
			} else {
				Vector list =
					Gate.getGateList(
					Histogram
						.getHistogram(JamStatus.getCurrentHistogramName())
						.getDimensionality());
				return ((Gate) (list.elementAt(index-1))).getName();
			}
		} else {
			return NO_GATES;
		}
	}

	public int getSize() {
		int n = numGates();
		return Math.max(1, n+1);
	}

	public void setSelectedItem(Object anItem) {
		selection = (String) anItem;
		if ((!selection.equals(this.CHOOSE_A_GATE)) && 
		(!selection.equals(this.NO_GATES))){
			gc.selectGateAdd(selection);
		}
	}

	public Object getSelectedItem() {
		return selection;
	}

	private int numGates() {
		Histogram h =
			Histogram.getHistogram(JamStatus.getCurrentHistogramName());
		if (h != null) {
			return Gate.getGateList(h.getDimensionality()).size();
		} else {
			return 0;
		}
	}
	
    /**
     * Needs to be called after every action that changes the list of histograms.
     */
    void changeOccured(){
        fireContentsChanged(this,0,getSize());
    }

}
