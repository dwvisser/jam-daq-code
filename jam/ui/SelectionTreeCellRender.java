/*
 * Created on Nov 26, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jam.ui;

import jam.data.*;
import java.awt.Component;
import java.awt.Color;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

/**
 * @author ken
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SelectionTreeCellRender extends DefaultTreeCellRenderer implements TreeCellRenderer {

	private static final ClassLoader cl = ClassLoader.getSystemClassLoader();
	
	private static final ImageIcon histIcon1D =
		new ImageIcon(cl.getResource("jam/ui/hist1D.png"));
	private static final ImageIcon gateIcon1D =
		new ImageIcon(cl.getResource("jam/ui/gate1D.png"));
	
	private static final ImageIcon histIcon2D =
		new ImageIcon(cl.getResource("jam/ui/hist2D.png"));
	
	private static final ImageIcon gateIcon2D =
		new ImageIcon(cl.getResource("jam/ui/gate2D.png"));
	
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		
		if (selected) {
			setBackground(getBackgroundSelectionColor());
			setBackground(Color.BLUE);
			setForeground(getTextSelectionColor());
		} else {
			setBackground(getBackgroundNonSelectionColor() );
			setBackground(Color.WHITE);
			setForeground(getTextNonSelectionColor());
		}
		setBackground(Color.BLUE);
		setForeground(Color.BLUE);
		
		Object nodeObject =((DefaultMutableTreeNode)value).getUserObject();
		if (nodeObject instanceof Histogram){
			Histogram hist =(Histogram)nodeObject;
			if (hist.getDimensionality()==1){
				setIcon(histIcon1D);			
			}else{
				setIcon(histIcon2D);
			}
			setText( ((Histogram)nodeObject).getName() );


		}else if (nodeObject instanceof Gate) {
			Gate gate =(Gate)nodeObject;
			setText( gate.getName() );
			if (gate.getDimensionality()==1){
				setIcon(gateIcon1D);		
			}else{
				setIcon(gateIcon2D);
			}
		}else {
			setText( "File Test" );			
//			setText( (String)usrObj );
		}
		return this;
	}

}
