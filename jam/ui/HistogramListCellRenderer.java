package jam.ui;

import jam.data.Gate;
import jam.data.Histogram;

import java.awt.Component;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.UIManager;

/**
 * Renders representations for a JComboBox list entry of
 * a <code>jam.data.Histogram</code> object.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 17 Dec 2003
 */
public class HistogramListCellRenderer
	extends DefaultListCellRenderer {

	/**
	 * <p>Returns a <code>JPanel</code> representing the given object.
	 * Given a <code>Histogram</code>, returns a 
	 * <code>JPanel</code> with the following information:</p>
	 * <ul>
	 * <li>histogram number, name and dimensionality</li>
	 * <li>an icon if any gates are associated with it</li>
	 * <li>an X on the icon if the gates aren't all defined</li>
	 * </ul> 
	 * <p>Given a <code>String</code>,
	 * returns a JPanel with the words centered as a <code>JLabel</code>.</p>
	 * 
	 * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
	 */
	public Component getListCellRendererComponent(
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean cellHasFocus) {
		setComponentOrientation(list.getComponentOrientation());
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		if (value instanceof Histogram) {
			final Histogram histogram = (Histogram) value;
			final String name = histogram.getName();
			final int dimension = histogram.getDimensionality();
			final List gates = histogram.getGates();
			final boolean hasGates = gates.size() > 0;
			boolean allGatesSet = hasGates;//possibly true if has gates
			boolean anyGatesSet = false;//possibly true if has gates
			if (hasGates) {
				for (int i = gates.size() - 1; i >= 0; i--) {
					final boolean defined=((Gate) gates.get(i)).isDefined();
					allGatesSet &= defined;
					anyGatesSet |= defined;
				}
			}
			final int number = histogram.getNumber();
			final StringBuffer text = new StringBuffer();
			text.append(number).append(". ").append(name).append("--");
			text.append(dimension).append('D');
			setText(text.toString());
			if (hasGates) {
				if (allGatesSet) {
					setIcon(Icons.GO);
				} else if (anyGatesSet) {
					setIcon(Icons.CAUTION);
				} else {
					setIcon(Icons.STOP);
				}
			} else {
				setIcon(Icons.CLEAR);
			}
		} else { //String
			setText((String) value);
			setIcon(Icons.CLEAR);
		}
		boolean enable=Histogram.getHistogramList().size()>0;
		list.setEnabled(enable);
		setEnabled(enable);
		setBorder(
			(cellHasFocus)
				? UIManager.getBorder("List.focusCellHighlightBorder")
				: noFocusBorder);
		return this;
	}
}
