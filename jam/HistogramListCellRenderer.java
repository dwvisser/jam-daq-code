/*
 * Created on Dec 17, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam;

import java.awt.Component;

import javax.swing.*;
import java.awt.BorderLayout;
import jam.data.Histogram;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class HistogramListCellRenderer implements ListCellRenderer {

	/* (non-Javadoc)
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
			rval.add(new JLabel(type,JLabel.RIGHT),BorderLayout.EAST);
		}
		return rval;
	}

}
