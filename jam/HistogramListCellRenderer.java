package jam;

import jam.data.Gate;
import jam.data.Histogram;

import java.awt.Component;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.DefaultListCellRenderer;
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
		new ImageIcon(cl.getResource("stop.png"));
	private static final ImageIcon goIcon =
		new ImageIcon(cl.getResource("go.png"));
	private static final ImageIcon cautionIcon =
		new ImageIcon(cl.getResource("caution.png"));
	private static final ImageIcon clearIcon = 
	new ImageIcon(cl.getResource("clear.png"));

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
		setEnabled(list.isEnabled());
		setBorder(
			(cellHasFocus)
				? UIManager.getBorder("List.focusCellHighlightBorder")
				: noFocusBorder);

		return this;
	}

	/*class HistogramListCell extends JLabel {
		String name;
		int dimension;
		boolean hasGates = false;
		boolean allGatesSet = false;
		int number;

		HistogramListCell(Object o) {
			super();
			this.setHorizontalTextPosition(JLabel.LEFT);
			this.setFont(Font.getFont("Monospaced"));
			if (o instanceof Histogram) {
				final Histogram h = (Histogram) o;
				name = h.getName();
				dimension = h.getDimensionality();
				List g = h.getGates();
				hasGates = g.size() > 0;
				if (hasGates) {
					allGatesSet = true;
					for (int i = g.size() - 1; i >= 0; i--) {
						allGatesSet &= ((Gate) g.get(i)).isDefined();
					}
				}
				number = h.getNumber();
				setText();
				if (hasGates) {
					if (allGatesSet) {
						setIcon(gateIcon);
					} else {
						setIcon(gateNotSetIcon);
					}
				}
			} else { //String
				name = (String) o;
				setText(name);
			}
		}

		private void setText() {
			final StringBuffer text = new StringBuffer();
			text.append(number).append(". ").append(name).append("--");
			text.append(dimension).append('D');
			setText(text.toString());
		}

		void updateValues(Object o) {
			boolean change = false;
			if (o instanceof Histogram) {
				final Histogram h = (Histogram) o;
				final String n = h.getName();
				if (!n.equals(name)) {
					name = n;
					change = true;
				}
				final int d = h.getDimensionality();
				if (d != dimension) {
					dimension = d;
					change = true;
				}
				final List g = h.getGates();
				final boolean hg = g.size() > 0;
				boolean ags = false;
				if (hg) {
					ags = true;
					for (int i = g.size() - 1; i >= 0; i--) {
						ags &= ((Gate) g.get(i)).isDefined();
					}
				}
				if (hasGates != hg || allGatesSet != ags) {
					hasGates = hg;
					allGatesSet = ags;
					if (hasGates) {
						if (allGatesSet) {
							setIcon(gateIcon);
						} else {
							setIcon(gateNotSetIcon);
						}
					} else {
						setIcon(null);
					}
				}
				final int num = h.getNumber();
				if (number != num) {
					number = num;
					change = true;
				}
				if (change) {
					setText();
				}
			} else { //instanceof String
				String n = (String) o;
				if (!n.equals(name)) {
					name = n;
					setText(name);
					setIcon(null);
				}
			}
		}
	}*/
}
