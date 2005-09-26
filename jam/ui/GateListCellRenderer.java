package jam.ui;

import jam.data.Gate;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;

/**
 * Renders representations for a JComboBox list entry of a
 * <code>jam.data.Gate</code> object.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 17 Dec 2003
 */
public final class GateListCellRenderer extends DefaultListCellRenderer {

	/**
	 * Returns a <code>JLabel</code> for the gate, with name and number, and a
	 * red or green icon indicating if the gate is set.
	 * 
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
	 *      java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(final JList list,
			final Object value, final int index, final boolean isSelected,
			final boolean cellHasFocus) {
		setComponentOrientation(list.getComponentOrientation());
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		final Icons icons = Icons.getInstance();
		if (value instanceof Gate) {
			final Gate gate = (Gate) value;
			final String name = gate.getName();
			setText(name);
			if (gate.isDefined()) {
				setIcon(icons.GO_GREEN);
			} else {
				setIcon(icons.STOP);
			}
		} else { // String
			setText((String) value);
			setIcon(icons.CLEAR);
		}
		final boolean hasGates = (list.getModel().getSize() > 1);
		list.setEnabled(hasGates);
		setEnabled(hasGates);
		setBorder((cellHasFocus) ? UIManager
				.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
		return this;
	}

}
