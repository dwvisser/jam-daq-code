package jam.data.control;

import com.google.inject.Inject;
import jam.data.Gate;
import jam.ui.Icons;

import javax.swing.*;
import java.awt.*;

/**
 * Renders representations for a JComboBox list entry of a
 * <code>jam.data.Gate</code> object.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version 17 Dec 2003
 */
final class GateListCellRenderer extends DefaultListCellRenderer {

	private transient final Icons icons;

	@Inject
	GateListCellRenderer(final Icons icons) {
		super();
		this.icons = icons;
	}

	/**
	 * Returns a <code>JLabel</code> for the gate, with name and number, and a
	 * red or green icon indicating if the gate is set.
	 * 
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
	 *      java.lang.Object, int, boolean, boolean)
	 */
	@Override
	public Component getListCellRendererComponent(final JList<?> list,
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
