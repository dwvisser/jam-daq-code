package jam.data.control;

import jam.data.Histogram;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

/**
 * This class takes care of properly displaying the histogram chooser in Jam's
 * main window.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 1.4.2 RC3
 */
class HistogramComboBoxModel extends DefaultComboBoxModel {

	private transient Object selection = null;

	private transient int lastSize = 0;// NOPMD

	/**
	 * The possible modes for a histogram combo box.
	 * 
	 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
	 */
	static public enum Mode {
		/**
		 * Show 1D histograms only.
		 */
		ONE_D,

		/**
		 * Show 2D histograms only.
		 */
		TWO_D,

		/**
		 * Show all histograms.
		 */
		ALL
	}

	/**
	 * Unmodifiable Collection.
	 */
	private transient final Collection<Histogram> histograms;

	/**
	 * Create a data model for any JComboBox wishing to display the available
	 * histograms.
	 */
	HistogramComboBoxModel() {
		this(Mode.ALL);
	}

	/**
	 * Constructs a histogram combo box model for the given mode.
	 * 
	 * @param mode
	 *            which histograms to display
	 */
	HistogramComboBoxModel(Mode mode) {
		super();
		if (Mode.ALL.equals(mode)) {
			histograms = Histogram.getListSortedByNumber();
		} else if (Mode.ONE_D.equals(mode)) {
			histograms = Histogram.getHistogramList(1);
		} else {// TWO_D
			histograms = Histogram.getHistogramList(2);
		}
	}

	/**
	 * Needs to be called after every action that changes the list of
	 * histograms. Utility method added so that JamMain could notify when things
	 * have changed.
	 */
	void changeOccured() {
		fireContentsChanged(this, 0, getSize());
	}

	/**
	 * @return the list element at the specified index
	 * @param index
	 *            the index of the element to get
	 */
	public Object getElementAt(final int index) {
		final String NO_HISTS = "No Histograms";
		Object rval = NO_HISTS; // default value if no histograms
		final int size = getSize();
		if (index < 0 || index >= size) {
			JOptionPane.showMessageDialog(null, "WARNING: "
					+ getClass().getName() + ".getElementAt(" + index
					+ "): index out of range.", getClass().getName(),
					JOptionPane.WARNING_MESSAGE);
		} else if (size > 0) {
			final int numhists = numHists();
			if (size > 1 || (size == 1 && numhists == 1)) {
				rval = getHistogram(index);
			}
		}
		return rval;
	}

	/**
	 * The size of the list is guaranteed to be >=1.
	 * 
	 * @return the number of list elements in the chooser
	 */
	public int getSize() {
		final int rval = Math.max(1, numHists());
		synchronized (this) {
			if (rval != lastSize) {
				lastSize = rval;
				changeOccured();
			}
		}
		return rval;
	}

	/**
	 * Set the selected item in the list.
	 * 
	 * @param anItem
	 *            the item to select
	 */
	public void setSelectedItem(final Object anItem) {
		synchronized (this) {
			selection = anItem;
		}
	}

	/**
	 * @return the selected item
	 */
	public Object getSelectedItem() {
		return selection;
	}

	private int numHists() {
		return histograms.size();
	}

	private Object getHistogram(final int index) {
		Object rval = null;
		int countDown = index;
		for (final Iterator it = histograms.iterator(); countDown >= 0
				&& it.hasNext(); countDown--) {
			rval = it.next();
		}
		return rval;
	}
}
