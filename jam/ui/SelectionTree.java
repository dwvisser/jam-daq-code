package jam.ui;

import jam.data.Gate;
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
    
    private final MessageHandler MESSAGE_HANDLER;
    
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
        MESSAGE_HANDLER=STATUS.getMessageHandler();
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
    	TreePath [] overlayPaths=null;
        if (!isSyncEvent()) {
            /*
             * Remove so we don't get repeated callbacks while selecting other
             * objects
             */
            removeSelectionListener();
            final TreePath prime = paths[0];
            final Object firstNode = ((DefaultMutableTreeNode) prime
                    .getLastPathComponent()).getUserObject();
            
            //Histogram selected
            if (firstNode instanceof Histogram) {
                final Histogram hist = (Histogram) firstNode;
                STATUS.setHistName(hist.getName());
                STATUS.setCurrentGateName(null);
                
                //Do we have a overlay
                if (paths.length>1) {
                	//System.arraycopy(paths, 1, overlayPaths, 0, paths.length);                	
                	if (hist.getDimensionality()==1) {
                		selectOverlay(paths);
                	}else{
                		MESSAGE_HANDLER.errorOutln("Cannot overlay on a 2 D histograms");
                	}

                } else {
                	STATUS.clearOverlays();
                }
                
                BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT,hist);
                
            //Gate selected                
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
     * 
     * @param paths
     */
    private void selectOverlay(TreePath[] paths){
    	
    	DefaultMutableTreeNode overlayNode=null;
    	Object overlayObject=null;
    	Histogram overlayHistogram;
    	overlayNode=((DefaultMutableTreeNode) paths[0].getLastPathComponent());
    	/*
    	if (hist.getDimensionality()==1) {
    		selectOverlay(paths);
    	}else{
    		MESSAGE_HANDLER.errorOutln("Cannot overlay on a 2 D histograms");
    	}
    	*/
		for (int i=1;i<paths.length;i++) {
			overlayNode=((DefaultMutableTreeNode) paths[i].getLastPathComponent());
			overlayObject=overlayNode.getUserObject();
			if (overlayObject instanceof Histogram) {
				overlayHistogram=(Histogram)(overlayObject);
				//can only overlay 1 d hists
			if (overlayHistogram.getDimensionality()==1){
				STATUS.addOverlayHistogramName(overlayHistogram.getName());
			}else{                			
				tree.removeSelectionPath(paths[i]);
				MESSAGE_HANDLER.errorOutln("Cannot overlay 2 D histograms");
			}
		} else {
			//FIXME
		}
	}
    	
    }
    /**
     * Refresh the selected node.
     */
    private void refreshSelection() {
        final Histogram hist = STATUS.getCurrentHistogram();
        final Gate gate = Gate.getGate(STATUS.getCurrentGateName());
        final Histogram[] overlayHists=STATUS.getOverlayHistograms();
        
        final TreePath histTreePath = pathForDataObject(hist);
        tree.setSelectionPath(histTreePath);


        if (gate != null) {        	
            refreshGateSelection(gate, histTreePath);        	
            /* Set path */
        }
        if (0<overlayHists.length){
        	refreshOverlaySelection(overlayHists);
        }
        repaint();
        tree.repaint();
    }
    /**
     * 
     * @param gate
     * @param histTreePath
     */
    private void refreshGateSelection(Gate gate, TreePath histTreePath){
    	DefaultMutableTreeNode currentNode;
    	Object currentObject;
    	//Iterage over all nodes below histogram node 
    	final Enumeration nodeEnum = ((DefaultMutableTreeNode) histTreePath
                    .getLastPathComponent()).breadthFirstEnumeration();
        while (nodeEnum.hasMoreElements()) {
            currentNode = (DefaultMutableTreeNode) nodeEnum.nextElement();
            currentObject = currentNode.getUserObject();
            if (currentObject instanceof Gate && currentObject==gate) {
                final TreePath gateTreePath = new TreePath(currentNode.getPath());
                tree.addSelectionPath(gateTreePath);
                break;
            }
       } //End loop for all nodes    	
    }
    /**
     * 
     * @param overlayHists
     * @param histTreePath
     */
    private void refreshOverlaySelection(Histogram [] overlayHists){
    	DefaultMutableTreeNode currentNode;
    	Object currentObject;
    	DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
    	//Iterage over all nodes below root node 
    	final Enumeration nodeEnum = rootNode.breadthFirstEnumeration();
    	//Loop over nodes
        while (nodeEnum.hasMoreElements()) {
            currentNode = (DefaultMutableTreeNode) nodeEnum.nextElement();
            currentObject = currentNode.getUserObject();
            if (currentObject instanceof Histogram)
            	//Loop over histograms
            	for (int i=0; i<overlayHists.length; i++){
            		if(currentObject==overlayHists[i]) {
                        final TreePath gateTreePath = new TreePath(currentNode.getPath());
                        tree.addSelectionPath(gateTreePath);
                        break;                        
            	}//End loop histograms
            }
       } //End loop for all nodes    	

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