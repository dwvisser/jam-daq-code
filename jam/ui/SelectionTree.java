package jam.ui;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.SortMode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
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
public class SelectionTree extends JPanel implements Observer {

    private final JTree tree;

    private final DefaultTreeModel treeModel;

    private static final JamStatus STATUS = JamStatus.instance();

    private static final Broadcaster BROADCASTER = Broadcaster
            .getSingletonInstance();

    /** Is sync event so don't */
    private boolean syncEvent = false;

    /**
     * Constructs a new <code>SelectionTree</code>.
     *  
     */
    public SelectionTree() {
        super(new BorderLayout());
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
        addSelectionListener();
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

    private final TreeSelectionListener listener = new TreeSelectionListener() {
        public void valueChanged(TreeSelectionEvent e) {
            final TreePath[] path = tree.getSelectionPaths();
            if (path != null) {
                select(path);
            }
        }
    };

    private void addSelectionListener() {
        if (tree.getTreeSelectionListeners().length < 1) {
            tree.addTreeSelectionListener(listener);
        }
    }

    private void removeSelectionListener() {
        tree.removeTreeSelectionListener(listener);
    }

    /**
     * Load the tree for the data objects.
     */
    private void loadTree() {
        final SortMode sortMode = STATUS.getSortMode();
        final String sortName = STATUS.getSortName();
        final DefaultMutableTreeNode rootNode;
        if (sortMode == SortMode.FILE) {
            rootNode = new DefaultMutableTreeNode(sortName);
        } else if (sortMode == SortMode.OFFLINE) {
            rootNode = new DefaultMutableTreeNode(sortName);
        } else if ((sortMode == SortMode.ONLINE_DISK)
                || (sortMode == SortMode.ONLINE_DISK)) {
            rootNode = new DefaultMutableTreeNode(sortName);
        } else if (sortMode == SortMode.NO_SORT) {
            rootNode = new DefaultMutableTreeNode(sortName);
        } else {
            rootNode = new DefaultMutableTreeNode("No Data");
        }
        treeModel.setRoot(rootNode);
        /* Loop through histograms and load them */
        final Iterator iter = Histogram.getHistogramList().iterator();
        while (iter.hasNext()) {
            final Histogram hist = (Histogram) iter.next();
            final DefaultMutableTreeNode histNode = new DefaultMutableTreeNode(
                    hist);
            /* Loop through histograms and load them */
            final Iterator iterGate = hist.getGates().iterator();
            while (iterGate.hasNext()) {
                histNode.add(new DefaultMutableTreeNode(iterGate.next()));
            }
            rootNode.add(histNode);
        }
        tree.expandRow(tree.getRowCount()-1);
    }

    /*
     * non-javadoc: A node has been selected in the tree Set selected object
     */
    private synchronized void select(TreePath[] paths) {
        /* Syncronize events should not fire events */
        if (!isSyncEvent()) {
            /*
             * Remove so we don't get repeated callbacks while selecting other
             * objects
             */
            removeSelectionListener();
            final TreePath prime = paths[0];
            final Object firstNode = ((DefaultMutableTreeNode) prime
                    .getLastPathComponent()).getUserObject();
            if (firstNode instanceof Histogram) {
                final Histogram hist = (Histogram) firstNode;
                STATUS.setHistName(hist.getName());
                STATUS.setCurrentGateName(null);
                BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
                        hist);
            } else if (firstNode instanceof Gate) {
                final Gate gate = (Gate) firstNode;
                final Histogram hist = getAssociatedHist(prime);
                tree.addSelectionPath(pathForDataObject(hist));
                STATUS.setHistName(hist.getName());
                BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,
                        hist);
                STATUS.setCurrentGateName(gate.getName());
                BROADCASTER.broadcast(BroadcastEvent.Command.GATE_SELECT, gate);
            }
            //Re add listener now that we have set selection
            addSelectionListener();
        }
    }

    /**
     * Refresh the selected node.
     */
    private void refreshSelection() {
        final Histogram hist = STATUS.getCurrentHistogram();
        final TreePath histTreePath = pathForDataObject(hist);
        tree.setSelectionPath(histTreePath);
        final Gate gate = Gate.getGate(STATUS.getCurrentGateName());
        if (gate != null) {
            final Enumeration nodeEnum = ((DefaultMutableTreeNode) histTreePath
                    .getLastPathComponent()).breadthFirstEnumeration();
            while (nodeEnum.hasMoreElements()) {
                final DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) nodeEnum
                        .nextElement();
                Object obj = currentNode.getUserObject();
                if (obj instanceof Gate) {
                    if (obj == gate) {
                        final TreePath gateTreePath = new TreePath(currentNode.getPath());
                        tree.addSelectionPath(gateTreePath);
                        break;
                    }
                }
            } //End loop for all nodes
            /* Set path */
        }
        repaint();
        tree.repaint();
    }

    /*
     * non-javadoc: Helper method to get TreePath for a data object
     */
    private TreePath pathForDataObject(Object dataObject) {
        DefaultMutableTreeNode loopNode;
        TreePath treePath = null;
        final Enumeration nodeEnum = ((DefaultMutableTreeNode) treeModel
                .getRoot()).breadthFirstEnumeration();
        while (nodeEnum.hasMoreElements()) {
            loopNode = (DefaultMutableTreeNode) nodeEnum.nextElement();
            Object obj = loopNode.getUserObject();
            if (dataObject == obj) {
                treePath = new TreePath(loopNode.getPath());
                break;
            }
        }
        return treePath;
    }

    /**
     * Implementation of Observable interface.
     * 
     * @param observable
     *            the sender
     * @param o
     *            the message
     */
    public synchronized void update(Observable observable, Object o) {
        final BroadcastEvent be = (BroadcastEvent) o;
        final BroadcastEvent.Command command = be.getCommand();
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

    private synchronized void setSyncEvent(boolean state) {
        syncEvent = state;
    }

    private synchronized boolean isSyncEvent() {
        return syncEvent;
    }

    private boolean isGate(TreePath path) {
        return ((DefaultMutableTreeNode) path.getLastPathComponent())
                .getUserObject() instanceof Gate;
    }

    private Histogram getAssociatedHist(TreePath path) {
        if (!isGate(path)) {
            throw new IllegalArgumentException("Only call with a gate path.");
        }
        return (Histogram) ((DefaultMutableTreeNode) path.getPathComponent(path
                .getPathCount() - 2)).getUserObject();
    }

    /*
     * private boolean isHist(TreePath path){ return ((DefaultMutableTreeNode)
     * path.getLastPathComponent()) .getUserObject() instanceof Histogram; }
     */

}