package jam.data.func;

import jam.data.control.CalibrationFit;
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
		
	//private List LIST=CalibrationFunction.getNames();
	/*
	static {
		LIST.add(CalibrationFit.NOT_CALIBRATED);
		LIST.add(LinearFunction.getName());
		LIST.add(SqrtEnergyFunction.getName());
	}
	*/
	private transient Object selection;
	private transient final Object selectSync=new Object();

	/**
	 * @return list element at the specified index
	 * @param index the index of the desired element
	 */
	public Object getElementAt(int index) {
		return CalibrationFunction.getListNames().get(index);
	}

	/**
	 * @return number of list elements in chooser.
	 */
	public int getSize() {
		int size=CalibrationFunction.getListNames().size();
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
			Object name=null;
			if (anItem==null){
				selection=CalibrationFunction.getListNames().get(0);
			} else if (anItem instanceof String){
				name=anItem;				
			} else {
				throw new IllegalArgumentException(getClass().getName()+
				": only CalibrationFunction Strings or null please");
			}
			selection=name;
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
