package jam;

import jam.data.Gate;
import jam.data.Histogram;

import java.awt.Component;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
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

	private static final ClassLoader cl = ClassLoader.getSystemClassLoader();
	private static final ImageIcon stopIcon =
		new ImageIcon(cl.getResource("jam/stop.png"));
	private static final ImageIcon goIcon =
		new ImageIcon(cl.getResource("jam/go.png"));
	private static final ImageIcon cautionIcon =
		new ImageIcon(cl.getResource("jam/caution.png"));
	private static final ImageIcon clearIcon = 
	new ImageIcon(cl.getResource("jam/clear.png"));

	/**
	 * Creates a new <code>HistogramListCellRenderer</code>.
	 */
	public HistogramListCellRenderer() {
		super();
	}

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
			final Histogram h = (Histogram) value;
			final String name = h.getName();
			final int dimension = h.getDimensionality();
			List g = h.getGates();
			final boolean hasGates = g.size() > 0;
			boolean allGatesSet = hasGates;//possibly true if has gates
			boolean anyGatesSet = false;//possibly true if has gates
			if (hasGates) {
				for (int i = g.size() - 1; i >= 0; i--) {
					final boolean defined=((Gate) g.get(i)).isDefined();
					allGatesSet &= defined;
					anyGatesSet |= defined;
				}
			}
			final int number = h.getNumber();
			final StringBuffer text = new StringBuffer();
			text.append(number).append(". ").append(name).append("--");
			text.append(dimension).append('D');
			setText(text.toString());
			if (hasGates) {
				if (allGatesSet) {
					setIcon(goIcon);
				} else if (anyGatesSet) {
					setIcon(cautionIcon);
				} else {
					setIcon(stopIcon);
				}
			} else {
				setIcon(clearIcon);
			}
		} else { //String
			setText((String) value);
			setIcon(clearIcon);
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
