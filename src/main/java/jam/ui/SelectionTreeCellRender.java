package jam.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.google.inject.Inject;

import jam.data.AbstractHistogram;
import jam.data.Gate;
import jam.data.Group;

/**
 * Renderer for Jam's hist/gate selection tree.
 * 
 * @author Ken Swartz
 * @version Nov 26, 2004
 */
@SuppressWarnings("serial")
public final class SelectionTreeCellRender extends DefaultTreeCellRenderer {

	private transient final Color defaultBackground;
	private transient final Icons icons;

	/**
	 * Constructs a new renderer.
	 * 
	 * @param icons
	 *            application internal icons
	 */
	@Inject
	public SelectionTreeCellRender(final Icons icons) {
		super();
		defaultBackground = getBackgroundSelectionColor();
		this.icons = icons;
	}

	/**
	 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree,
	 *      java.lang.Object, boolean, boolean, boolean, int, boolean)
	 */
	@Override
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
		} else {// must be String
			final String name = (String) nodeObject;
			setIcon(null);
			setText(name);
		}
		return this;
	}

	private void renderGroup(final Group group) {
		if (group.getType() == Group.Type.FILE) {
			setIcon(icons.GROUP_FILE);
		} else if (group.getType() == Group.Type.SORT) {
			setIcon(icons.GROUP_SORT);
		} else {
			setIcon(icons.GROUP_TEMP);
		}
		setText(group.getName());
	}

	private void renderGate(final Gate gate) {
		setBackgroundSelectionColor(defaultBackground);
		setText(gate.getName());
		if (gate.getDimensionality() == 1) {
			if (gate.isDefined()) {
				setIcon(icons.GATE_DEF1D);
			} else {
				setIcon(icons.GATE1D);
			}
		} else {
			if (gate.isDefined()) {
				setIcon(icons.GATE_DEF2D);
			} else {
				setIcon(icons.GATE2D);
			}
		}
	}

	private void renderHistogram(final AbstractHistogram hist) {
		setBackgroundSelectionColor(defaultBackground);
		final StringBuilder tip = new StringBuilder();
		tip.append(hist.getNumber()).append(". ").append(hist.getTitle());
		tip.append(" (").append(hist.getSizeX());
		if (hist.getDimensionality() == 1) {
			setIcon(icons.HIST1D);
		} else {
			setIcon(icons.HIST2D);
			tip.append('x').append(hist.getSizeY());
		}
		tip.append(')');
		setText(hist.getName());
		setToolTipText(tip.toString());
	}
}