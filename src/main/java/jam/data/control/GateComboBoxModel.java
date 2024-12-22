package jam.data.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

import jam.data.AbstractHistogram;
import jam.data.Gate;
import jam.global.Nameable;
import jam.ui.SelectionTree;

/**
 * Used anywhere a JComboBox is used to select from the available gates.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version 1.4.2 RC 3
 */

final class GateComboBoxModel extends DefaultComboBoxModel<Object> {

	/**
	 * Class representing the possible modes of GateComboBoxModel's. Only two
	 * modes exist now, which are accessible as static fields of this class.
	 * 
	 * @version 1.4.2 (RC3)
	 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
	 */
	public enum Mode {
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
	}

    private transient final List<Object> lastValue = Collections
			.synchronizedList(new ArrayList<>());

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
	GateComboBoxModel(final Mode listmode) {
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
	@Override
	public Object getElementAt(final int index) {
		final int nonNegIndex = Math.max(index, 0);
		Object rval = "No Gates"; // default value if no gates
		if (numGates() > 0) {
			if (index == 0) {
				rval = "Choose a gate";
			} else {
				final AbstractHistogram his = (AbstractHistogram) SelectionTree
						.getCurrentHistogram();
				final int which = index - 1;
				if (Mode.DISPLAYED_HIST.equals(mode)) {
					rval = his.getGateCollection().getGates().get(which);
				} else {
					final List<Gate> list = Gate.getGateList(his
							.getDimensionality());
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
	@Override
	public Object getSelectedItem() {
		return selection;
	}

	/**
	 * @return number of list elements in chooser.
	 */
	@Override
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
		if (named instanceof AbstractHistogram) {
			final AbstractHistogram hist = (AbstractHistogram) named;
			if (Mode.DISPLAYED_HIST.equals(mode)) {
				numG = hist.getGateCollection().getGates().size();
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
	@Override
	public void setSelectedItem(final Object anItem) {
		synchronized (this) {
			selection = anItem;
		}
	}
}
