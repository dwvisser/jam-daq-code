package jam.data.func;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;

/**
 * Used anywhere a JComboBox is used to select from the available 
 * gates.
 *
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 1.4.2 RC 3
 */
public class CalibrationComboBoxModel extends DefaultComboBoxModel {

	private Object selection = null;
		
	public static final List list=new ArrayList();
	static {
		list.add(LinearFunction.class);
		list.add(PolynomialFunction.class);
		list.add(SqrtEnergyFunction.class);
	}

	/**
	 * Create the default model that shows gates for the currently
	 * displayed histogram.
	 */
	public CalibrationComboBoxModel() {
		super();
	}

	/**
	 * @return list element at the specified index
	 * @param index the index of the desired element
	 */
	public Object getElementAt(int index) {
		return list.get(index);
	}

	/**
	 * @return number of list elements in chooser.
	 */
	public int getSize() {
		return list.size();
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

}
