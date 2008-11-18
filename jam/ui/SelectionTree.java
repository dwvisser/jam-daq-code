package jam.ui;

import jam.data.AbstractHist1D;
import jam.data.AbstractHistogram;
import jam.data.DataBase;
import jam.data.DataElement;
import jam.data.DataUtility;
import jam.data.Gate;
import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.Nameable;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.global.UnNamed;
import jam.global.Validator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import com.google.inject.Inject;

/**
 * Implements a <code>JTree</code> for selecting <code>Histogram</code>'s and
 * <code>Gate</code>'s to display.
 * 
 * @author Ken Swartz
 */
public final class SelectionTree extends JPanel implements Observer {
	private static final Broadcaster BROADCASTER = Broadcaster
			.getSingletonInstance();

	private static Nameable currentGate;

	private static Nameable currentHistogram;

	private static final Object LOCK = new Object();

	private static final Logger LOGGER = Logger.getLogger(SelectionTree.class
			.getPackage().getName());

	private transient final JamStatus status;

	private static final Validator validator = DataBase.getInstance();

	/**
	 * Gets the current <code>Gate</code>.
	 * 
	 * @return name of current gate
	 */
	public static Nameable getCurrentGate() {
		synchronized (LOCK) {
			if (!validator.isValid(currentGate)) {
				currentGate = UnNamed.getSingletonInstance();
			}
			return currentGate;
		}
	}

	/**
	 * Gets the current histogram.
	 * 
	 * @return the current histogram, or the singleton instance of the class
	 *         <code>UnNamed</code>
	 */
	public static Nameable getCurrentHistogram() {
		synchronized (LOCK) {
			if (!validator.isValid(currentHistogram)) {
				currentHistogram = UnNamed.getSingletonInstance();
			}
			return currentHistogram;
		}
	}

	/**
	 * Sets the current <code>Gate</code>.
	 * 
	 * @param gate
	 *            of current gate
	 */
	public static void setCurrentGate(final Nameable gate) {
		synchronized (LOCK) {
			currentGate = gate;
		}
	}

	/**
	 * Sets the current <code>Histogram</code>.
	 * 
	 * @param hist
	 *            the current histogram
	 */
	public static void setCurrentHistogram(final Nameable hist) {
		synchronized (LOCK) {
			currentHistogram = hist;
		}
	}

	private transient final TreeSelectionListener listener = new TreeSelectionListener() {
		public void valueChanged(final TreeSelectionEvent event) {
			final TreePath[] path = tree.getSelectionPaths();
			if (path != null) {
				select(path);
			}
		}
	};

	private transient DefaultMutableTreeNode rootNode;

	/** Is sync event so don't */
	private boolean syncEvent = false;

	private transient final JTree tree;

	private transient final DefaultTreeModel treeModel;

	/**
	 * Constructs a new <code>SelectionTree</code>.
	 * 
	 * @param status
	 *            application status
	 */
	@Inject
	public SelectionTree(final JamStatus status) {
		super(new BorderLayout());
		this.status = status;
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

	private void addGroupNodes(final Group group) {
		final DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(
				group);
		// Loop through histograms and load them
		for (AbstractHistogram hist : group.histograms.getList()) {
			addHistNodes(groupNode, hist);
		}
		rootNode.add(groupNode);
	}

	private void addHistNodes(final DefaultMutableTreeNode groupNode,
			final AbstractHistogram hist) {
		final DefaultMutableTreeNode histNode = new DefaultMutableTreeNode(hist);
		groupNode.add(histNode);
		// Loop through gates and load them
		for (DataElement gate : hist.getGateCollection().getGates()) {
			histNode.add(new DefaultMutableTreeNode(gate));// NOPMD
		}
	}

	private void addSelectionListener() {
		if (tree.getTreeSelectionListeners().length < 1) {
			tree.addTreeSelectionListener(listener);
		}
	}

	private void addSelectionPath(final DefaultMutableTreeNode currentNode) {
		final TreePath gateTreePath = new TreePath(currentNode.getPath());
		tree.addSelectionPath(gateTreePath);
	}

	private AbstractHistogram getAssociatedHist(final TreePath path) {
		if (!isGate(path)) {
			throw new IllegalArgumentException("Only call with a gate path.");
		}
		return (AbstractHistogram) ((DefaultMutableTreeNode) path
				.getPathComponent(path.getPathCount() - 2)).getUserObject();
	}

	private boolean isGate(final TreePath path) {
		return ((DefaultMutableTreeNode) path.getLastPathComponent())
				.getUserObject() instanceof Gate;
	}

	private boolean isSyncEvent() {
		synchronized (this) {
			return syncEvent;
		}
	}

	/**
	 * Load the tree for the data objects.
	 */
	private void loadTree() {
		final QuerySortMode sortMode = status.getSortMode();
		if (sortMode == SortMode.FILE) {
			final String fileName = status.getSortName();
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
		for (Group group : jam.data.Warehouse.getGroupCollection().getList()) {
			addGroupNodes(group);
		}
		tree.expandRow(tree.getRowCount() - 1);
	}

	/*
	 * non-javadoc: Helper method to get TreePath for a data object
	 */
	private TreePath pathForDataObject(final Object dataObject) {
		TreePath treePath = null;
		final Enumeration<?> nodeEnum = ((DefaultMutableTreeNode) treeModel
				.getRoot()).breadthFirstEnumeration();
		while (nodeEnum.hasMoreElements()) {
			final DefaultMutableTreeNode loopNode = (DefaultMutableTreeNode) nodeEnum
					.nextElement();
			final Object obj = loopNode.getUserObject();
			if (dataObject == obj) {// NOPMD
				treePath = new TreePath(loopNode.getPath());// NOPMD
				break;
			}
		}
		return treePath;
	}

	/*
	 * non-javadoc
	 */
	private void refreshGateSelection(final Gate gate,
			final TreePath histTreePath) {
		/* Iterate over all nodes below histogram node. */
		final Enumeration<?> nodeEnum = ((DefaultMutableTreeNode) histTreePath
				.getLastPathComponent()).breadthFirstEnumeration();
		while (nodeEnum.hasMoreElements()) {
			final DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) nodeEnum
					.nextElement();
			final Object currentObj = currentNode.getUserObject();
			if (currentObj instanceof Gate && currentObj.equals(gate)) {
				addSelectionPath(currentNode);
				break;
			}
		} // End loop for all nodes
	}

	private void refreshOverlaySelection(final List<AbstractHist1D> overlayHists) {
		final DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree
				.getModel().getRoot();
		/* Iterate over all nodes below root node. */
		final Enumeration<?> nodeEnum = root.breadthFirstEnumeration();
		while (nodeEnum.hasMoreElements()) {
			final DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) nodeEnum
					.nextElement();
			final Object currentObj = currentNode.getUserObject();
			if (currentObj instanceof AbstractHistogram) {
				// Loop over histograms
				for (AbstractHistogram hist : overlayHists) {
					if (currentObj == hist) {
						addSelectionPath(currentNode);
					}
				}// End loop histograms
			}
		} // End loop for all nodes
	}

	/**
	 * Refresh the selected node.
	 */
	private void refreshSelection() {
		final Nameable hist = getCurrentHistogram();
		final Nameable gate = getCurrentGate();
		final List<AbstractHist1D> overlayHists = AbstractHistogram
				.getHistogramList(status.getOverlayHistograms(),
						AbstractHist1D.class);
		final TreePath histTreePath = pathForDataObject(hist);
		tree.setSelectionPath(histTreePath);
		if (gate instanceof Gate) {
			refreshGateSelection((Gate) gate, histTreePath);
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
	private void select(final TreePath[] paths) {
		synchronized (this) {
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

				if (firstNode == rootNode) {// NOPMD
					BROADCASTER.broadcast(BroadcastEvent.Command.ROOT_SELECT);
				} else if (firstNodeObject instanceof Group) {
					final Group group = (Group) firstNodeObject;
					// Group.setCurrentGroup(group);
					status.setCurrentGroup(group);
					BROADCASTER.broadcast(BroadcastEvent.Command.GROUP_SELECT,
							group);
				} else if (firstNodeObject instanceof AbstractHistogram) {
					/* Histogram selected */
					final AbstractHistogram hist = (AbstractHistogram) firstNodeObject;
					status.setCurrentGroup(DataUtility.getGroup(hist));
					setCurrentHistogram(hist);
					setCurrentGate(null);
					/* Do we have overlays ? */
					if (paths.length > 1) {
						if (hist.getDimensionality() == 1) {
							selectOverlay(paths);
						} else {
							LOGGER.severe("Cannot overlay on a 2D histogram.");
						}
					} else {
						status.clearOverlays();
					}
					BROADCASTER.broadcast(
							BroadcastEvent.Command.HISTOGRAM_SELECT, hist);
				} else if (firstNodeObject instanceof Gate) {
					/* Gate selected */
					final Gate gate = (Gate) firstNodeObject;
					final AbstractHistogram hist = getAssociatedHist(prime);
					tree.addSelectionPath(pathForDataObject(hist));
					status.setCurrentGroup(DataUtility.getGroup(hist));
					setCurrentHistogram(hist);
					setCurrentGate(gate);
					status.clearOverlays();
					BROADCASTER.broadcast(
							BroadcastEvent.Command.HISTOGRAM_SELECT, hist);
					selectGate(gate);
				}
				/* Re-add listener now that we have set selection. */
				addSelectionListener();
			}
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
	private void selectGate(final Gate gate) {
		try {
			setCurrentGate(gate);
			BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SELECT, gate);
			final double area = gate.getArea();
			final StringBuilder message = new StringBuilder();
			if (gate.getDimensionality() == 1) {
				final double centroid = ((int) (gate.getCentroid() * 100.0)) / 100.0;
				final int lowerLimit = gate.getLimits1d()[0];
				final int upperLimit = gate.getLimits1d()[1];
				message.append("Gate: ").append(gate.getName())
						.append(", Ch. ").append(lowerLimit).append(" to ")
						.append(upperLimit).append("  Area = ").append(area)
						.append(", Centroid = ").append(centroid);
			} else {
				message.append("Gate ").append(gate.getName()).append(
						", Area = ").append(area);
			}
			LOGGER.info(message.toString());
		} catch (Exception de) {
			LOGGER.log(Level.SEVERE, de.getMessage(), de);
		}
	}

	/*
	 * non-javadoc
	 */
	private void selectOverlay(final TreePath[] paths) {
		DefaultMutableTreeNode overlayNode = null;
		Object overlayObj = null;
		AbstractHistogram overlayHist;
		status.clearOverlays();
		/* Loop from 2nd element to the end. */
		for (int i = 1; i < paths.length; i++) {
			overlayNode = ((DefaultMutableTreeNode) paths[i]
					.getLastPathComponent());
			overlayObj = overlayNode.getUserObject();
			/* Ignore any gates in the selection paths here. */
			if (overlayObj instanceof AbstractHistogram) {
				overlayHist = (AbstractHistogram) (overlayObj);
				if (overlayHist.getDimensionality() == 1) {
					status.addOverlayHistogramName(overlayHist.getFullName());
				} else {
					tree.removeSelectionPath(paths[i]);
					LOGGER.warning("Cannot overlay 2D histograms.");
				}
			}
		}
	}

	private void setSyncEvent(final boolean state) {
		synchronized (this) {
			syncEvent = state;
		}
	}

	/**
	 * Implementation of Observable interface.
	 * 
	 * @param observable
	 *            the sender
	 * @param object
	 *            the message
	 */
	public void update(final Observable observable, final Object object) {
		synchronized (this) {
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
}