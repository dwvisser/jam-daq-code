package jam.ui;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.JamStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

/**
 * Used anywhere a JComboBox is used to select from the available 
 * gates.
 *
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 1.4.2 RC 3
 */
public class GateComboBoxModel extends DefaultComboBoxModel {

	/**
	 * Class representing the possible modes of GateComboBoxModel's.
	 * Only two modes exist now, which are accessible as static
	 * fields of this class.
	 *
	 * @version 1.4.2 (RC3)
	 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
	 */
	static public class Mode {
		private static final int I_DISP = 0;
		private static final int I_ALL = 1;

		/**
		 * The mode for which only gates belonging to the displayed
		 * histogram are listed.
		 */
		static final public Mode DISPLAYED_HIST = new Mode(I_DISP);

		/**
		 * The mode for which all gates of the same dimensionality
		 * of the displayed histogram.
		 */
		static final public Mode ALL = new Mode(I_ALL);

		private final int mode;

		private Mode(int i) {
			mode = i;
		}
		
		/**
		 * @see Object#equals(java.lang.Object)
		 */
		public boolean equals(Object object){
		    return object instanceof Mode ? mode==((Mode)object).mode : false;
		}
	}

	private Object selection = null;
	private JamStatus status;
	private final List<Object> lastValue =
		Collections.synchronizedList(new ArrayList<Object>());
	private final Mode mode;

	/**
	 * Create the default model that shows gates for the currently
	 * displayed histogram.
	 */
	public GateComboBoxModel() {
		super();
		status = JamStatus.getSingletonInstance();
		mode = Mode.DISPLAYED_HIST;
	}

	/**
	 * Create a new model for the given mode.
	 * 
	 * @param listmode whether just gates for the current histogram or 
	 * all histograms of the same dimensionality should be shown
	 */
	public GateComboBoxModel(Mode listmode) {
		super();
		status = JamStatus.getSingletonInstance();
		mode = listmode;
	}

	/**
	 * Needs to be called after every action that changes the list of 
	 * histograms.
	 */
	private void changeOccured() {
		fireContentsChanged(this, 0, getSize());
	}

	/**
	 * @return list element at the specified index
	 * @param index the index of the desired element
	 */
	public Object getElementAt(int index) {
		int i = Math.max(index, 0);
		final String NO_GATES = "No Gates";
		final String CHOOSE_A_GATE = "Choose a gate";
		Object rval = NO_GATES; //default value if no gates
		if (numGates() > 0) {
			if (index == 0) {
				rval = CHOOSE_A_GATE;
			} else {
				final Histogram his =status.getCurrentHistogram();
				final int which = index - 1;
				if (Mode.DISPLAYED_HIST.equals(mode)) {
					rval = (Gate) his.getGates().get(which);
				} else {
					final List list = Gate.getGateList(his.getDimensionality());
					rval = (Gate) (list.get(which));
				}
			}
		}
		if (!rval.equals(lastValue.get(i))) {
			changeOccured();
		} else {
			lastValue.set(index, rval);
		}
		return rval;
	}

	/**
	 * @return number of list elements in chooser.
	 */
	public int getSize() {
		final int rval = numGates() + 1;
		final int oldSize = lastValue.size();
		if (rval < oldSize) { //trim list back
			lastValue.subList(rval, oldSize).clear();
			changeOccured();
		} else if (rval > oldSize) { //expand list
			for (int i = oldSize; i < rval; i++) {
				lastValue.add(null);
			}
			changeOccured();
		}
		return rval;
	}

	/**
	 * @param anItem the item to set the selection to
	 */
	public void setSelectedItem(Object anItem) {
		synchronized (this) {
			selection = anItem;
		}
	}

	/**
	 * @return the currently selected item
	 */
	public Object getSelectedItem() {
		return selection;
	}

	private int numGates() {
		int numG = 0;
		final Histogram hist =status.getCurrentHistogram();
		if (hist != null) {
			if (Mode.DISPLAYED_HIST.equals(mode)) {
				numG = hist.getGates().size();
			} else { //ALL
				numG = Gate.getGateList(hist.getDimensionality()).size();
			}
		}
		return numG;
	}
}
