package jam.data.control;

import jam.data.func.AbstractCalibrationFunction;
import jam.data.func.CalibrationFunctionCollection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * Used anywhere a JComboBox is used to select from the available calibration
 * functions.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @author Ken Swartz
 * @version 1.4.2 RC 3
 */
final class CalibrationComboBoxModel implements ComboBoxModel {

	private transient Object selection;

	private transient final Object selectSync = new Object();

	private transient final List<ListDataListener> listeners = Collections
			.synchronizedList(new ArrayList<ListDataListener>());

	CalibrationComboBoxModel() {
		super();
	}

	/**
	 * @return list element at the specified index
	 * @param index
	 *            the index of the desired element
	 */
	public Object getElementAt(final int index) {
		synchronized (selectSync) {
			return CalibrationFunctionCollection.getListNames().get(
					index);
		}
	}

	/**
	 * @return number of list elements in chooser.
	 */
	public int getSize() {
		synchronized (selectSync) {
			return CalibrationFunctionCollection.getListNames().size();
		}
	}

	/**
	 * Can be called with an instance of the desired class or a reference to the
	 * class itself.
	 * 
	 * @param anItem
	 *            the item to set the selection to
	 * @throws IllegalArgumentException
	 *             if not a String or null
	 */
	public void setSelectedItem(final Object anItem) {
		synchronized (selectSync) {
			String selectionNew;
			if (anItem == null) {
				selectionNew = CalibrationFunctionCollection
						.getListNames().get(0);
			} else if (anItem instanceof String) {
				selectionNew = (String) anItem;
			} else if (anItem instanceof AbstractCalibrationFunction) {
				selectionNew = ((AbstractCalibrationFunction) anItem).getName();
			} else {
				throw new IllegalArgumentException(getClass().getName()
						+ ": only AbstractCalibrationFunction, Strings or null");
			}

			if (!selectionNew.equals(selection)) {
				selection = selectionNew;
				final ListDataEvent lde = new ListDataEvent(this,
						ListDataEvent.CONTENTS_CHANGED, 0, getSize());
				for (ListDataListener listener : listeners) {
					listener.contentsChanged(lde);
				}
			}
		}
	}

	/**
	 * @return the currently selected item
	 */
	public Object getSelectedItem() {
		synchronized (selectSync) {
			return selection;
		}
	}

	public void addListDataListener(final ListDataListener ldl) {
		listeners.add(ldl);
	}

	public void removeListDataListener(final ListDataListener ldl) {
		listeners.remove(ldl);
	}

}
