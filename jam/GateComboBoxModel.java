package jam;
import jam.data.Gate;
import jam.data.Histogram;
import jam.global.JamStatus;
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
		private static final int disp = 0;
		private static final int all = 1;

		/**
		 * The mode for which only gates belonging to the displayed
		 * histogram are listed.
		 */
		static final public Mode DISPLAYED_HIST = new Mode(disp);
		
		/**
		 * The mode for which all gates of the same dimensionality
		 * of the displayed histogram.
		 */
		static final public Mode ALL = new Mode(all);
		
		private final int mode;

		private Mode(int i) {
			mode = i;
		}
	}

	private String selection = null;
	private JamStatus status;
	private int lastSize = 0;
	private Object[] lastValue;
	private final Mode mode;

	/**
	 * Create the default model that shows gates for the currently
	 * displayed histogram.
	 */
	public GateComboBoxModel() {
		super();
		status = JamStatus.instance();
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
		status = JamStatus.instance();
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
		int i=Math.max(index,0);
		final String NO_GATES = "No Gates";
		final String CHOOSE_A_GATE = "Choose a gate";
		Object rval = NO_GATES; //default value if no gates
		if (numGates() > 0) {
			if (index == 0) {
				rval = CHOOSE_A_GATE;
			} else if (Mode.DISPLAYED_HIST.equals(mode)){
				rval =
					Histogram
						.getHistogram(status.getCurrentHistogramName())
						.getGates()[index
						- 1].getName();
			} else {
				final List list =
					Gate.getGateList(
					Histogram
						.getHistogram(status.getCurrentHistogramName())
						.getDimensionality());
				rval = ((Gate) (list.get(index-1))).getName();
			}
		}
		if (lastValue[i] != null) {
			if (!lastValue[i].equals(rval)) {
				changeOccured();
			}			
		} else {
			synchronized(this){
				lastValue[index] = rval;
			}
		}
		return rval;
	}

	/**
	 * @return number of list elements in chooser.
	 */
	public int getSize() {
		final int rval = numGates() + 1;
		if (rval != lastSize) {
			synchronized(this){
				lastSize = rval;
				lastValue = new Object[rval];
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
			selection = (String) anItem;
		}
	}

	/**
	 * @return the currently selected item
	 */
	public Object getSelectedItem() {
		return selection;
	}

	private int numGates() {
		int returnValue=0;
		final Histogram hist =
			Histogram.getHistogram(status.getCurrentHistogramName());
		if (hist == null) {
			returnValue = 0;
		} else if (Mode.DISPLAYED_HIST.equals(mode)) {
				final Gate[] gates = hist.getGates();
				if (gates == null) {
					returnValue = 0;
				} else {
					returnValue = gates.length;
				}
		} else {//ALL
			returnValue = Gate.getGateList(hist.getDimensionality()).size();
		}
		return returnValue;
	}
}
