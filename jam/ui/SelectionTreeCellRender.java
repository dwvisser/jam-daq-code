package jam.ui;

import jam.data.Gate;
import jam.data.Histogram;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

/**
 * @author Ken Swartz
 * @version Nov 26, 2004
 */
public class SelectionTreeCellRender extends DefaultTreeCellRenderer implements
		TreeCellRenderer {

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (selected) {
			setBackground(Color.BLUE);
			setForeground(getTextSelectionColor());
			setForeground(Color.BLUE);			
		} else {
			setBackground(getBackgroundNonSelectionColor());
			setForeground(getTextNonSelectionColor());
			setForeground(Color.BLACK);	
		}
		Object nodeObject =((DefaultMutableTreeNode)value).getUserObject();
		if (nodeObject instanceof Histogram){
			Histogram hist =(Histogram)nodeObject;
			if (hist.getDimensionality()==1){
				setIcon(Icons.HIST1D);			
			}else{
				setIcon(Icons.HIST2D);
			}
			setText( ((Histogram)nodeObject).getName() );


		}else if (nodeObject instanceof Gate) {
			Gate gate =(Gate)nodeObject;

			setText( gate.getName() );
			if (gate.getDimensionality()==1){
				if (gate.isDefined()){
					setIcon(Icons.GATE_DEF1D);					
				}else {
					setIcon(Icons.GATE1D);
				}
			}else{
				if (gate.isDefined()){
					setIcon(Icons.GATE_DEF2D);					
				}else {
					setIcon(Icons.GATE2D);
				}
			}
		}else {
			String name =(String)nodeObject;
			setIcon(null);
			setText(name );			
		}
		return this;
	}
}