package jam;

import jam.data.Gate;

import java.awt.Component;
import java.net.URL;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.UIManager;


/**
 * Renders representations for a JComboBox list entry of
 * a <code>jam.data.Gate</code> object.
 *
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 17 Dec 2003
 */
public class GateListCellRenderer
	extends DefaultListCellRenderer {

	private static final ClassLoader cl = ClassLoader.getSystemClassLoader();

	private static ImageIcon stopIcon;
	private static ImageIcon goIcon;
	private static ImageIcon clearIcon;

	static{
		URL urlStop=cl.getResource("jam/stop.png");
		URL urlGo =cl.getResource("jam/go.png");
		URL urlClear =cl.getResource("jam/clear.png");
		if (urlStop==null || urlGo==null || urlClear==null) {
			JOptionPane.showMessageDialog(null, "Can't load resource: jam/(stop|go|clear).png");
			System.exit(0);
		}
	}

	/**
	 * Creates a new <code>HistogramListCellRenderer</code>.
	 */
	public GateListCellRenderer() {
		super();

	}

	/**
	 * Returns a <code>JLabel</code> for the gate, with name and number, and a red
	 * or green icon indicating if the gate is set.
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
		if (value instanceof Gate) {
			final Gate g = (Gate) value;
			final String name = g.getName();
			setText(name);
			if (g.isDefined()) {
				setIcon(goIcon);
			} else {
				setIcon(stopIcon);
			}
		} else { //String
			setText((String) value);
			setIcon(clearIcon);
		}
		final boolean hasGates = (list.getModel().getSize()>1);
		list.setEnabled(hasGates);
		setEnabled(hasGates);
		setBorder(
			(cellHasFocus)
				? UIManager.getBorder("List.focusCellHighlightBorder")
				: noFocusBorder);

		return this;
	}

}
