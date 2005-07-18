package jam.ui;

import jam.data.Gate;
import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.SortMode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Implements a <code>JTree</code> for selecting <code>Histogram</code>'s
 * and <code>Gate</code>'s to display.
 * 
 * @author Ken Swartz
 */
public final class SelectionTree extends JPanel implements Observer {

	private static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	private static final JamStatus STATUS = JamStatus.getSingletonInstance();

	private final TreeSelectionListener listener = new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent e) {
			final TreePath[] path = tree.getSelectionPaths();
			if (path != null) {
				select(path);
			}
		}
	};

	private final MessageHandler msgHandler;

	private DefaultMutableTreeNode rootNode;

	/** Is sync event so don't */
	private boolean syncEvent = false;

	private final JTree tree;

	private final DefaultTreeModel treeModel;

	/**
	 * Constructs a new <code>SelectionTree</code>.
	 * 
	 */
	public SelectionTree() {
		super(new BorderLayout());
		msgHandler = STATUS.getMessageHandler();
		BROADCASTER.addObserver(this);
		final Dimension dim = getMinimumSize();
		dim.width = 160;
		setPreferredSize(dim);
		add(RunStateBox.getInstance().getComponent(), BorderLayout.NORTH);
		/* Default blank model */
		treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("No Data"));
		tree = new JTree(treeModel);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.setCellRenderer(new SelectionTreeCellRender());
		ToolTipManager.sharedInstance().registerComponent(tree);
		addSelectionListener();
		add(new JScrollPane(tree), BorderLayout.CENTER);
	}

	private void addSelectionListener() {
		if (tree.getTreeSelectionListeners().length < 1) {
			tree.addTreeSelectionListener(listener);
		}
	}

	private Histogram getAssociatedHist(TreePath path) {
		if (!isGate(path)) {
			throw new IllegalArgumentException("Only call with a gate path.");
		}
		return (Histogram) ((DefaultMutableTreeNode) path.getPathComponent(path
				.getPathCount() - 2)).getUserObject();
	}

	private boolean isGate(TreePath path) {
		return ((DefaultMutableTreeNode) path.getLastPathComponent())
				.getUserObject() instanceof Gate;
	}

	private synchronized boolean isSyncEvent() {
		return syncEvent;
	}

	/**
	 * Load the tree for the data objects.
	 */
	private void loadTree() {

		final SortMode sortMode = STATUS.getSortMode();

		if (sortMode == SortMode.FILE) {
			final String fileName = STATUS.getSortName();
			rootNode = new DefaultMutableTreeNode("File: " + fileName);
		} else if (sortMode == SortMode.OFFLINE) {
			rootNode = new DefaultMutableTreeNode("Offline Sort");
		} else if ((sortMode == SortMode.ONLINE_DISK)
				|| (sortMode == SortMode.ONLINE_DISK)) {
			rootNode = new DefaultMutableTreeNode("Online Sort");
		} else if (sortMode == SortMode.NO_SORT) {
			rootNode = new DefaultMutableTreeNode("No Sort");
		} else {
			rootNode = new DefaultMutableTreeNode("No Data");
		}
		treeModel.setRoot(rootNode);

		// Loop through all groups
		final Iterator iterGroup = Group.getGroupList().iterator();
		while (iterGroup.hasNext()) {
			final Group group = (Group) iterGroup.next();
			final DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(
					group);
			// Loop through histograms and load them
			final Iterator iterHist = group.getHistogramList().iterator();
			while (iterHist.hasNext()) {
				final Histogram hist = (Histogram) iterHist.next();
				final DefaultMutableTreeNode histNode = new DefaultMutableTreeNode(
						hist);
				groupNode.add(histNode);
				// Loop through gates and load them
				final Iterator iterGate = hist.getGates().iterator();
				while (iterGate.hasNext()) {
					histNode.add(new DefaultMutableTreeNode(iterGate.next()));
				}
			}
			rootNode.add(groupNode);
		}
		tree.expandRow(tree.getRowCount() - 1);
	}

	/*
	 * non-javadoc: Helper method to get TreePath for a data object
	 */
	private TreePath pathForDataObject(Object dataObject) {
		TreePath treePath = null;
		final Enumeration nodeEnum = ((DefaultMutableTreeNode) treeModel
				.getRoot()).breadthFirstEnumeration();
		while (nodeEnum.hasMoreElements()) {
			final DefaultMutableTreeNode loopNode = (DefaultMutableTreeNode) nodeEnum
					.nextElement();
			final Object obj = loopNode.getUserObject();
			if (dataObject == obj) {
				treePath = new TreePath(loopNode.getPath());
				break;
			}
		}
		return treePath;
	}

	/*
	 * non-javadoc
	 */
	private void refreshGateSelection(Gate gate, TreePath histTreePath) {
		/* Iterate over all nodes below histogram node. */
		final Enumeration nodeEnum = ((DefaultMutableTreeNode) histTreePath
				.getLastPathComponent()).breadthFirstEnumeration();
		while (nodeEnum.hasMoreElements()) {
			final DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) nodeEnum
					.nextElement();
			final Object currentObj = currentNode.getUserObject();
			if (currentObj instanceof Gate && currentObj == gate) {
				final TreePath gateTreePath = new TreePath(currentNode
						.getPath());
				tree.addSelectionPath(gateTreePath);
				break;
			}
		} // End loop for all nodes
	}

	private void refreshOverlaySelection(List<Histogram> overlayHists) {
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree
				.getModel().getRoot();
		/* Iterate over all nodes below root node. */
		final Enumeration nodeEnum = root.breadthFirstEnumeration();
		while (nodeEnum.hasMoreElements()) {
			final DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) nodeEnum
					.nextElement();
			final Object currentObj = currentNode.getUserObject();
			if (currentObj instanceof Histogram) {
				// Loop over histograms
				for (int i = 0; i < overlayHists.size(); i++) {
					if (currentObj == overlayHists.get(i)) {
						final TreePath gateTreePath = new TreePath(currentNode
								.getPath());
						tree.addSelectionPath(gateTreePath);
					}
				}// End loop histograms
			}
		} // End loop for all nodes
	}

	/**
	 * Refresh the selected node.
	 */
	private void refreshSelection() {
		final Histogram hist = (Histogram) STATUS.getCurrentHistogram();
		final Gate gate = (Gate) STATUS.getCurrentGate();
		final List<Histogram> overlayHists = Histogram.getHistogramList(STATUS
				.getOverlayHistograms());
		final TreePath histTreePath = pathForDataObject(hist);
		tree.setSelectionPath(histTreePath);
		if (gate != null) {
			refreshGateSelection(gate, histTreePath);
		}
		if (!overlayHists.isEmpty()) {
			refreshOverlaySelection(overlayHists);
		}
		repaint();
		tree.repaint();
	}

	private void removeSelectionListener() {
		tree.removeTreeSelectionListener(listener);
	}

	/*
	 * non-javadoc: A node has been selected in the tree Set selected object
	 */
	private synchronized void select(TreePath[] paths) {
		/* Syncronize events should not fire events */
		if (!isSyncEvent()) {
			/*
			 * Remove listener so we don't get repeated callbacks while
			 * selecting other objects
			 */
			removeSelectionListener();
			final TreePath prime = paths[0];
			final DefaultMutableTreeNode firstNode = ((DefaultMutableTreeNode) prime
					.getLastPathComponent());
			final Object firstNodeObject = firstNode.getUserObject();

			if (firstNode == rootNode) {
				BROADCASTER.broadcast(BroadcastEvent.Command.ROOT_SELECT);
			} else if (firstNodeObject instanceof Group) {
				final Group group = (Group) firstNodeObject;
				// Group.setCurrentGroup(group);
				STATUS.setCurrentGroup(group);
				BROADCASTER.broadcast(BroadcastEvent.Command.GROUP_SELECT,
						group);
			} else if (firstNodeObject instanceof Histogram) {
				/* Histogram selected */
				final Histogram hist = (Histogram) firstNodeObject;
				STATUS.setCurrentGroup(hist.getGroup());
				STATUS.setCurrentHistogram(hist);
				STATUS.setCurrentGate(null);
				/* Do we have overlays ? */
				if (paths.length > 1) {
					if (hist.getDimensionality() == 1) {
						selectOverlay(paths);
					} else {
						msgHandler
								.errorOutln("Cannot overlay on a 2D histogram.");
					}
				} else {
					STATUS.clearOverlays();
				}
				BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
						hist);
			} else if (firstNodeObject instanceof Gate) {
				/* Gate selected */
				final Gate gate = (Gate) firstNodeObject;
				final Histogram hist = getAssociatedHist(prime);
				tree.addSelectionPath(pathForDataObject(hist));
				STATUS.setCurrentGroup(hist.getGroup());
				STATUS.setCurrentHistogram(hist);
				STATUS.setCurrentGate(gate);
				STATUS.clearOverlays();
				BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
						hist);
				selectGate(gate);
			}
			/* Re-add listener now that we have set selection. */
			addSelectionListener();
		}
	}

	/**
	 * A gate has been selected. Tell all appropriate classes, like Display and
	 * JamStatus.
	 * 
	 * @param gate
	 *            to select
	 * @see jam.data.Gate
	 */
	private void selectGate(Gate gate) {
		final String methodname = "selectGate(): ";
		try {
			STATUS.setCurrentGate(gate);
			BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SELECT, gate);
			final double area = gate.getArea();
			if (gate.getDimensionality() == 1) {
				final double centroid = ((int) (gate.getCentroid() * 100.0)) / 100.0;
				final int lowerLimit = gate.getLimits1d()[0];
				final int upperLimit = gate.getLimits1d()[1];
				msgHandler.messageOut("Gate: " + gate.getName() + ", Ch. "
						+ lowerLimit + " to " + upperLimit, MessageHandler.NEW);
				msgHandler.messageOut("  Area = " + area + ", Centroid = "
						+ centroid, MessageHandler.END);
			} else {
				msgHandler.messageOut("Gate " + gate.getName(),
						MessageHandler.NEW);
				msgHandler.messageOut(", Area = " + area, MessageHandler.END);
			}
		} catch (Exception de) {
			String classname = getClass().getName() + "--";
			msgHandler.errorOutln(classname + methodname + de.getMessage());
		}
	}

	/*
	 * non-javadoc
	 */
	private void selectOverlay(TreePath[] paths) {
		DefaultMutableTreeNode overlayNode = null;
		Object overlayObj = null;
		Histogram overlayHist;
		STATUS.clearOverlays();
		/* Loop from 2nd element to the end. */
		for (int i = 1; i < paths.length; i++) {
			overlayNode = ((DefaultMutableTreeNode) paths[i]
					.getLastPathComponent());
			overlayObj = overlayNode.getUserObject();
			/* Ignore any gates in the selection paths here. */
			if (overlayObj instanceof Histogram) {
				overlayHist = (Histogram) (overlayObj);
				if (overlayHist.getDimensionality() == 1) {
					STATUS.addOverlayHistogramName(overlayHist.getFullName());
				} else {
					tree.removeSelectionPath(paths[i]);
					msgHandler.errorOutln("Cannot overlay 2D histograms.");
				}
			}
		}
	}

	private synchronized void setSyncEvent(boolean state) {
		syncEvent = state;
	}

	/**
	 * Implementation of Observable interface.
	 * 
	 * @param observable
	 *            the sender
	 * @param object
	 *            the message
	 */
	public synchronized void update(Observable observable, Object object) {
		final BroadcastEvent event = (BroadcastEvent) object;
		final BroadcastEvent.Command command = event.getCommand();
		setSyncEvent(true);
		if (command == BroadcastEvent.Command.HISTOGRAM_SELECT
				|| command == BroadcastEvent.Command.GATE_SELECT) {
			refreshSelection();
		} else if (command == BroadcastEvent.Command.HISTOGRAM_NEW
				|| command == BroadcastEvent.Command.HISTOGRAM_ADD
				|| command == BroadcastEvent.Command.GATE_ADD
				|| command == BroadcastEvent.Command.RUN_STATE_CHANGED
				|| command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			loadTree();
		}
		setSyncEvent(false);
	}
}