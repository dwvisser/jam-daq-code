package jam.data.func;

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
public class CalibrationListCellRenderer
	extends DefaultListCellRenderer {

	private static final ImageIcon LINE_ICON;
	private static final ImageIcon POLY_ICON;
	private static final ImageIcon SQRT_ICON;

	static{
		final ClassLoader loader = ClassLoader.getSystemClassLoader();
		URL urlLine=loader.getResource("jam/data/func/line.png");
		URL urlPoly =loader.getResource("jam/data/func/poly.png");
		URL urlSqrt =loader.getResource("jam/data/func/sqrt.png");
		if (urlLine==null || urlPoly==null || urlSqrt==null) {
			JOptionPane.showMessageDialog(null, "Can't load resource: jam/(stop|go|clear).png");
			LINE_ICON=POLY_ICON=SQRT_ICON=null;
		} else {
			LINE_ICON=new ImageIcon(urlLine);
			POLY_ICON=new ImageIcon(urlPoly);
			SQRT_ICON=new ImageIcon(urlSqrt);
		}
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
		if (value==null){
			setText(null);
			setIcon(null);	
		} else if (value.equals(LinearFunction.class.getName())){
			setIcon(LINE_ICON);
			setText("Linear");
		} else if (value.equals(PolynomialFunction.class.getName())){
			setIcon(POLY_ICON);
			setText("Polynomial");
		} else if (value.equals(SqrtEnergyFunction.class.getName())){
			setIcon(SQRT_ICON);
			setText("Linear in Square Root");
		}
		setBorder(
			(cellHasFocus)
				? UIManager.getBorder("List.focusCellHighlightBorder")
				: noFocusBorder);
		return this;
	}

}
