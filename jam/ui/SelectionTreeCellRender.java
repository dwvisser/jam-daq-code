package jam.ui;

import jam.data.Gate;
import jam.data.AbstractHistogram;
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
public final class SelectionTreeCellRender extends DefaultTreeCellRenderer {

	private transient final Color defaultBackground;
	private static final Icons ICONS = Icons.getInstance();

	/**
	 * Constructs a new renderer.
	 */
	public SelectionTreeCellRender() {
		super();
		defaultBackground = getBackgroundSelectionColor();
	}

	/**
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
	 *      java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	public Component getTreeCellRendererComponent(final JTree tree,
			final Object value, final boolean isSelected,
			final boolean expanded, final boolean leaf, final int row,
			final boolean hasTheFocus) {
		super.getTreeCellRendererComponent(tree, value, isSelected, expanded,
				leaf, row, hasTheFocus);
		final Object nodeObject = ((DefaultMutableTreeNode) value)
				.getUserObject();
		if (nodeObject instanceof Group) {
			renderGroup((Group) nodeObject);
		} else if (nodeObject instanceof AbstractHistogram) {
			renderHistogram((AbstractHistogram) nodeObject);
		} else if (nodeObject instanceof Gate) {
			renderGate((Gate) nodeObject);
		} else {//must be String
			final String name = (String) nodeObject;
			setIcon(null);
			setText(name);
		}
		return this;
	}
	
	private void renderGroup(final Group group){
		if (group.getType() == Group.Type.FILE) {
			setIcon(ICONS.GROUP_FILE);
		} else if (group.getType() == Group.Type.SORT) {
			setIcon(ICONS.GROUP_SORT);
		} else {
			setIcon(ICONS.GROUP_TEMP);
		}
		setText(group.getName());
	}
	
	private void renderGate(final Gate gate) {
		setBackgroundSelectionColor(defaultBackground);
		setText(gate.getName());
		if (gate.getDimensionality() == 1) {
			if (gate.isDefined()) {
				setIcon(ICONS.GATE_DEF1D);
			} else {
				setIcon(ICONS.GATE1D);
			}
		} else {
			if (gate.isDefined()) {
				setIcon(ICONS.GATE_DEF2D);
			} else {
				setIcon(ICONS.GATE2D);
			}
		}
	}
	
	private void renderHistogram(final AbstractHistogram hist) {
		setBackgroundSelectionColor(defaultBackground);
		final StringBuffer tip = new StringBuffer();
		tip.append(hist.getNumber()).append(". ").append(hist.getTitle());
		tip.append(" (").append(hist.getSizeX());
		if (hist.getDimensionality() == 1) {
			setIcon(ICONS.HIST1D);
		} else {
			setIcon(ICONS.HIST2D);
			tip.append('x').append(hist.getSizeY());
		}
		tip.append(')');
		setText(hist.getName());
		setToolTipText(tip.toString());
	}
}