package jam;
import jam.data.Histogram;

import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

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
	private Mode mode;
	
	static public class Mode{
		final int value;
		
		private Mode(int i){
			value=i;
		}
		
		static final public Mode ONE_D = new Mode(1);
		static final public Mode TWO_D = new Mode(2);
		static final public Mode ALL = new Mode(0);
		
		boolean acceptHistogram(Histogram h){
			boolean rval= value==0;
			if (h != null){
				if (value > 0){
					rval = h.getDimensionality()==value;
				}
			}
			return rval;
		}
	}
	
	/**
	 * Unmodifiable Collection.
	 */
	private final Collection histograms;

	/**
	 * Create a data model for any JComboBox wishing to display the available
	 * histograms.
	 */
	public HistogramComboBoxModel() {
		this(Mode.ALL);
	}
	
	public HistogramComboBoxModel(Mode m){
		super();
		mode=m;
		if (Mode.ALL.equals(m)){
			histograms=Histogram.getListSortedByNumber();
		} else {
			histograms=Histogram.getHistogramList(m.value);
		}
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
			JOptionPane.showMessageDialog(null,
				"WARNING: "
					+ getClass().getName()
					+ ".getElementAt("
					+ index
					+ "): index out of range.",getClass().getName(),
					JOptionPane.WARNING_MESSAGE);
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
	
	private Object getHistogram(int index){
		Object rval=null;
		int n=index;
		for (Iterator it=histograms.iterator(); n>=0 && it.hasNext(); n--){
			rval=it.next();
		}
		return rval;
	}	
}
