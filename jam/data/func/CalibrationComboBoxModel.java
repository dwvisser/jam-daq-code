package jam.data.func;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

/**
 * Used anywhere a JComboBox is used to select from the available 
 * calibration functions.
 *
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 1.4.2 RC 3
 */
public final class CalibrationComboBoxModel extends DefaultComboBoxModel {
		
	private static final List LIST=new ArrayList();
	static {
		LIST.add(LinearFunction.class.getName());
		LIST.add(SqrtEnergyFunction.class.getName());
	}
	
	private transient Object selection;
	private transient final Object selectSync=new Object();

	/**
	 * @return list element at the specified index
	 * @param index the index of the desired element
	 */
	public Object getElementAt(int index) {
		return LIST.get(index);
	}

	/**
	 * @return number of list elements in chooser.
	 */
	public int getSize() {
		return LIST.size();
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
				name=LIST.get(0);
			} else if (anItem instanceof String){
				name=anItem;
			} else {
				throw new IllegalArgumentException(getClass().getName()+
				": only Strings or null please");
			}
			for (Iterator it=LIST.iterator(); it.hasNext(); ){
				final Object clazz=it.next();
				if (name.equals(clazz)){
					synchronized (selectSync) {
						selection = clazz;
					}					
				}
			}
	}

	/**
	 * @return the currently selected item
	 */
	public Object getSelectedItem() {
		synchronized (selectSync){
			return selection;
		}
	}

}
