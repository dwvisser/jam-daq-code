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
		
	private transient Object selection;
	private transient final Object selectSync=new Object();

	/**
	 * @return list element at the specified index
	 * @param index the index of the desired element
	 */
	public Object getElementAt(int index) {
		return AbstractCalibrationFunction.getListNames().get(index);
	}

	/**
	 * @return number of list elements in chooser.
	 */
	public int getSize() {
		int size=AbstractCalibrationFunction.getListNames().size();
		return size;
	}

	/**
	 * Can be called with an instance of the desired class or
	 * a reference to the class itself.
	 * 
	 * @param anItem the item to set the selection to
	 * @throws IllegalArgumentException if not a String or null
	 */
	public void setSelectedItem(Object anItem) {
		synchronized (selectSync){
			if (anItem==null){
				selection=AbstractCalibrationFunction.getListNames().get(0);
			} else if (anItem instanceof String){
				selection=anItem;				
			} else {
				throw new IllegalArgumentException(getClass().getName()+
				": only CalibrationFunction Strings or null please");
			}
		}
			//FIXME KBS
			/*
			if (name!=null) {
				for (Iterator it=CalibrationFunction.getListNames().iterator(); it.hasNext(); ){
					final Object clazz=it.next();
					if (name.equals(clazz)){
						synchronized (selectSync) {
							selection = clazz;
						}					
					}
				}
			}
			*/
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
