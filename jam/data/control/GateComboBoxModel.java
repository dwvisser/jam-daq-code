package jam.data.control;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.Nameable;
import jam.ui.SelectionTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

/**
 * Used anywhere a JComboBox is used to select from the available gates.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 1.4.2 RC 3
 */
final class GateComboBoxModel extends DefaultComboBoxModel {

	/**
	 * Class representing the possible modes of GateComboBoxModel's. Only two
	 * modes exist now, which are accessible as static fields of this class.
	 * 
	 * @version 1.4.2 (RC3)
	 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
	 */
	static public enum Mode {
		/**
		 * The mode for which all gates of the same dimensionality of the
		 * displayed histogram.
		 */
		ALL,

		/**
		 * The mode for which only gates belonging to the displayed histogram
		 * are listed.
		 */
		DISPLAYED_HIST
	};

	private transient final List<Object> lastValue = Collections
			.synchronizedList(new ArrayList<Object>());

	private transient final Mode mode;

	private transient Object selection = null;

	/**
	 * Create the default model that shows gates for the currently displayed
	 * histogram.
	 */
	GateComboBoxModel() {
		this(Mode.DISPLAYED_HIST);
	}

	/**
	 * Create a new model for the given mode.
	 * 
	 * @param listmode
	 *            whether just gates for the current histogram or all histograms
	 *            of the same dimensionality should be shown
	 */
	GateComboBoxModel(Mode listmode) {
		super();
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
	 * @param index
	 *            the index of the desired element
	 */
	public Object getElementAt(final int index) {
		final int nonNegIndex = Math.max(index, 0);
		final String NO_GATES = "No Gates";
		final String CHOOSE_A_GATE = "Choose a gate";
		Object rval = NO_GATES; // default value if no gates
		if (numGates() > 0) {
			if (index == 0) {
				rval = CHOOSE_A_GATE;
			} else {
				final Histogram his = (Histogram) SelectionTree
						.getCurrentHistogram();
				final int which = index - 1;
				if (Mode.DISPLAYED_HIST.equals(mode)) {
					rval = his.getGates().get(which);
				} else {
					final List<Gate> list = Gate.getGateList(his.getDimensionality());
					rval = list.get(which);
				}
			}
		}
		if (rval.equals(lastValue.get(nonNegIndex))) {
			lastValue.set(index, rval);
		} else {
			changeOccured();
		}
		return rval;
	}

	/**
	 * @return the currently selected item
	 */
	public Object getSelectedItem() {
		return selection;
	}

	/**
	 * @return number of list elements in chooser.
	 */
	public int getSize() {
		final int rval = numGates() + 1;
		final int oldSize = lastValue.size();
		if (rval < oldSize) { // trim list back
			lastValue.subList(rval, oldSize).clear();
			changeOccured();
		} else if (rval > oldSize) { // expand list
			for (int i = oldSize; i < rval; i++) {
				lastValue.add(null);
			}
			changeOccured();
		}
		return rval;
	}

	private int numGates() {
		int numG = 0;
		final Nameable named = SelectionTree.getCurrentHistogram();
		if (named instanceof Histogram) {
			final Histogram hist = (Histogram) named;
			if (Mode.DISPLAYED_HIST.equals(mode)) {
				numG = hist.getGates().size();
			} else { // ALL
				numG = Gate.getGateList(hist.getDimensionality()).size();
			}
		}
		return numG;
	}

	/**
	 * @param anItem
	 *            the item to set the selection to
	 */
	public void setSelectedItem(final Object anItem) {
		synchronized (this) {
			selection = anItem;
		}
	}
}
