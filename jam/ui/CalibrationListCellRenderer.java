package jam.ui;

import jam.data.func.CalibrationFunctionCollection;

import javax.swing.*;
import java.awt.*;

/**
 * Renders representations for a JComboBox list entry of a
 * <code>jam.data.Gate</code> object.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version 17 Dec 2003
 */
public class CalibrationListCellRenderer extends DefaultListCellRenderer {// NOPMD

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
		if (value == null) {
			setText("null");
			setIcon(null);
		} else {
			setText(value.toString());
			final ImageIcon icon = CalibrationFunctionCollection
					.getIcon(value.toString());
			setIcon(icon);
		}
		setBorder((cellHasFocus) ? UIManager
				.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
		return this;
	}

}
