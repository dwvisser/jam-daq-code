package jam.ui;

import jam.data.Gate;
import jam.data.Histogram;
import jam.data.Group;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Renderer for Jam's hist/gate selection tree.
 * 
 * @author Ken Swartz
 * @version Nov 26, 2004
 */
public class SelectionTreeCellRender extends DefaultTreeCellRenderer {

    private Color defaultBackgroundColor;

    /**
     * Constructs a new renderer.
     */
    public SelectionTreeCellRender() {
        super();
        defaultBackgroundColor = getBackgroundSelectionColor();
    }

    /**
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
     *      java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded,
                leaf, row, hasFocus);
        Object nodeObject = ((DefaultMutableTreeNode) value).getUserObject();
        if (nodeObject instanceof Group) {
        	  Group group = (Group)nodeObject;
        	  if (group.getType()==Group.Type.FILE) {
        	  	setIcon(Icons.GROUP);
        	  }else{
        	  	setIcon(Icons.SORT);        	  	
        	  }
        	  setText(group.getName());
        } else if (nodeObject instanceof Histogram) {
            Histogram hist = (Histogram) nodeObject;
            setBackgroundSelectionColor(defaultBackgroundColor);
            final StringBuffer tip=new StringBuffer();
            tip.append(hist.getNumber()).append(". ").append(hist.getTitle());
            tip.append(" (").append(hist.getSizeX());
            if (hist.getDimensionality() == 1) {
                setIcon(Icons.HIST1D);
            } else {
                setIcon(Icons.HIST2D);
                tip.append('x').append(hist.getSizeY());
            }
            tip.append(')');
            setText(hist.getName());
            setToolTipText(tip.toString());
        } else if (nodeObject instanceof Gate) {
            Gate gate = (Gate) nodeObject;
            setBackgroundSelectionColor(Color.CYAN);
            setText(gate.getName());
            if (gate.getDimensionality() == 1) {
                if (gate.isDefined()) {
                    setIcon(Icons.GATE_DEF1D);
                } else {
                    setIcon(Icons.GATE1D);
                }
            } else {
                if (gate.isDefined()) {
                    setIcon(Icons.GATE_DEF2D);
                } else {
                    setIcon(Icons.GATE2D);
                }
            }
        } else {
            String name = (String) nodeObject;
            setIcon(null);
            setText(name);
        }
        return this;
    }
}