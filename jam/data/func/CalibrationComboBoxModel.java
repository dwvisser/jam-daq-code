package jam.data.func;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Used anywhere a JComboBox is used to select from the available 
 * calibration functions.
 *
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @author Ken Swartz
 * @version 1.4.2 RC 3
 */
public final class CalibrationComboBoxModel implements ComboBoxModel {
		
	private transient Object selection;
	private transient final Object selectSync=new Object();
	private ListDataListener listener;

	/**
	 * @return list element at the specified index
	 * @param index the index of the desired element
	 */
	public Object getElementAt(int index) {
		synchronized (selectSync){
			Object obj = (String)AbstractCalibrationFunction.getListNames().get(index);
			return obj;
		}		
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
			String selectionNew;
			if (anItem==null){
				selectionNew=(String)AbstractCalibrationFunction.getListNames().get(0);
			} else if (anItem instanceof String){
				selectionNew=(String)anItem;				
			}else if (anItem instanceof AbstractCalibrationFunction) {
				selectionNew=((AbstractCalibrationFunction)anItem).getName();				
			} else {
				throw new IllegalArgumentException(getClass().getName()+
				": only AbstractCalibrationFunction, Strings or null");
			}

			if (!selectionNew.equals(selection)) {
				selection=selectionNew;
				ListDataEvent lde= new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED,  0, getSize());
				listener.contentsChanged(lde);				
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
	
	public void addListDataListener(ListDataListener l) {
		listener=l;
	}
	
	public void removeListDataListener(ListDataListener l) {
		listener=null;
	}

}
