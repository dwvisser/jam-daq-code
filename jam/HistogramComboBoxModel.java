package jam;
import jam.data.Histogram;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;

/**
 * This class takes care of properly displaying the histogram chooser
 * in Jam's main window.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 1.4.2 RC3
 */
public class HistogramComboBoxModel extends DefaultComboBoxModel {

	private Object selection = null;
	private int lastSize=0;
	
	/**
	 * Unmodifiable Collection.
	 */
	private Collection histograms=Histogram.getListSortedByNumber();

	/**
	 * Create a data model for any JComboBox wishing to display the available
	 * histograms.
	 */
	public HistogramComboBoxModel() {
		super();
	}

	/**
	 * Needs to be called after every action that changes the list of 
	 * histograms. Utility method added so that JamMain could notify 
	 * when things have changed.
	 */
	void changeOccured() {
		fireContentsChanged(this, 0, getSize());
	}

	/**
	 * @return the list element at the specified index
	 * @param index the index of the element to get
	 */
	public Object getElementAt(int index) {
		final String NO_HISTS = "No Histograms";
		Object rval = NO_HISTS; //default value if no histograms
		final int size = getSize();
		if (index < 0 || index >= size) {
			System.err.println(
				"WARNING: "
					+ getClass().getName()
					+ ".getElementAt("
					+ index
					+ "): index out of range.");
		} else if (size > 0) {
			final int numhists=numHists();
			if (size>1 || (size==1 && numhists==1)){
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
		if (rval != lastSize){
			synchronized (this){
				lastSize=rval;
			}
			changeOccured();
		}
		return rval;
	}

	/**
	 * Set the selected item in the list.
	 * 
	 * @param anItem the item to select
	 */
	public void setSelectedItem(Object anItem) {
		synchronized(this){
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
	
	Iterator it;
	private Object getHistogram(int index){
		Object rval=null;
		int n=index;
		for (it=histograms.iterator(); n>=0 && it.hasNext(); n--){
			rval=it.next();
		}
		return rval;
	}	
}
