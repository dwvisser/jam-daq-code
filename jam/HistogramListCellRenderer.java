package jam;

import java.awt.Component;

import javax.swing.*;
import java.awt.BorderLayout;
import jam.data.Gate;
import jam.data.Histogram;

/**
 * Renders representations for a JComboBox list entry of
 * a <code>jam.data.Histogram</code> object.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 17 Dec 2003
 */
public class HistogramListCellRenderer implements ListCellRenderer {
	
	private final JLabel gateIcon;
	private final JLabel gateNotSetIcon;
	
	/**
	 * Creates a new <code>HistogramListCellRenderer</code>.
	 */
	public HistogramListCellRenderer(){
		super();
		final ClassLoader cl=getClass().getClassLoader();
		ImageIcon ii=new 
		ImageIcon(cl.getResource("jam/gate.png"));
		ii.setDescription("[contains gate(s)]");
		gateIcon=new JLabel(ii);
		ii=new ImageIcon(cl.getResource("jam/gatenotset.png"));
		ii.setDescription("[at least one gate is not set]");
		gateNotSetIcon=new JLabel(ii);
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
		final JPanel rval=new JPanel(new BorderLayout());
		if (isSelected){
			rval.setBackground(list.getSelectionBackground());
			rval.setForeground(list.getSelectionForeground());
		}
		if (value instanceof String){
			rval.add(new JLabel((String)value,JLabel.CENTER),BorderLayout.CENTER);
		} else {//histogram
			final Histogram h=(Histogram)value;
			rval.add(new JLabel(Integer.toString(h.getNumber()),JLabel.LEFT),
			BorderLayout.WEST);
			rval.add(new JLabel(h.getName(),JLabel.CENTER),BorderLayout.CENTER);
			String type="<html><font color=";
			if (h.getDimensionality()==1){
				type += "blue>1D</font></html>";
			} else {//2
				type += "red>2D</font></html>";
			}
			final JLabel ltype=new JLabel(type,JLabel.RIGHT);
			final Gate [] g=h.getGates();
			if (g.length==0){
				rval.add(ltype,BorderLayout.EAST);
			} else {//display a gate icon too
				final JPanel east=new JPanel();
				east.setLayout(new BoxLayout(east,BoxLayout.X_AXIS));
				Component icon=gateIcon;
				for (int i=0; i<g.length; i++){
					if (!g[i].isDefined()){
						icon=gateNotSetIcon;
						break;
					}
				}
				east.add(icon);
				east.add(ltype);
				rval.add(east,BorderLayout.EAST);
			}
		}
		return rval;
	}
}
