package jam.data.func;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;

/**
 * Used anywhere a JComboBox is used to select from the available 
 * gates.
 *
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 1.4.2 RC 3
 */
public final class CalibrationComboBoxModel extends DefaultComboBoxModel {
		
	public static final List list=new ArrayList();
	static {
		list.add(LinearFunction.class.getName());
		list.add(SqrtEnergyFunction.class.getName());
	}
	
	private Object selection;
	private final Object selectionSync=new Object();

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
	 * Can be called with an instance of the desired class or
	 * a reference to the class itself.
	 * 
	 * @param anItem the item to set the selection to
	 * @throws IllegalArgumentException if not a String or null
	 */
	public void setSelectedItem(Object anItem) {
			final Object name;
			if (anItem==null){
				name=list.get(0);
			} else if (anItem instanceof String){
				name=anItem;
			} else {
				throw new IllegalArgumentException(getClass().getName()+
				": only Strings or null please");
			}
			for (Iterator it=list.iterator(); it.hasNext(); ){
				final Object cl=it.next();
				if (name.equals(cl)){
					synchronized (selectionSync) {
						selection = cl;
					}					
				}
			}
	}

	/**
	 * @return the currently selected item
	 */
	public Object getSelectedItem() {
		synchronized (selectionSync){
			return selection;
		}
	}

}
