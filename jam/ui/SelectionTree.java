package jam.ui;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.SortMode;
import jam.plot.Display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Enumeration;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;


/**
 * @author ken
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SelectionTree extends JPanel implements Observer {

	JTree dataTree;
	
	DefaultTreeModel treeModel;
	
	DefaultMutableTreeNode rootNode;
	
	private final MessageHandler console;

	private final JamStatus status;

	private final Broadcaster broadcaster;

	private final Display display;
	
	/** Is sync event so don't */
	private boolean isSyncEvent=false;
	
	public SelectionTree(){

		//Global objects
		status = JamStatus.instance();
		broadcaster = Broadcaster.getSingletonInstance();
		console = status.getMessageHandler();
		display = status.getDisplay();
		broadcaster.addObserver(this);
		
		Dimension dim;
		dim=getMinimumSize();
		dim.width=160;
		//setMinimumSize(dim);
		setPreferredSize(dim);
		 setLayout(new BorderLayout());
		
		 treeModel= new DefaultTreeModel(new DefaultMutableTreeNode("No Data") );
		 	
		 dataTree = new JTree(treeModel);
		 dataTree.setCellRenderer(new SelectionTreeCellRender());
		 dataTree.addTreeSelectionListener(new TreeSelectionListener(){
	 	    public void valueChanged(TreeSelectionEvent e) {
	 	        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					dataTree.getLastSelectedPathComponent();
	 	        if (node == null) 
	 	        	return;
	 	        Object nodeInfo = node.getUserObject();
		 	    selection(nodeInfo);		 	        
	 	    }		 			 	
		 });
		 
		 this.add(new JScrollPane(dataTree), BorderLayout.CENTER);
 
	}	
	/**
	 * Load the tree for the data objects
	 *
	 */	
	public void loadTree(){

		SortMode sortMode =status.getSortMode();		
		String sortName=status.getSortName();
		
		if (sortMode==SortMode.FILE){
			rootNode = new DefaultMutableTreeNode(sortName);
		}else if (sortMode==SortMode.OFFLINE){
			rootNode = new DefaultMutableTreeNode(sortName);
		} else if ((sortMode==SortMode.ONLINE_DISK) ||(sortMode==SortMode.ONLINE_DISK)){
			rootNode = new DefaultMutableTreeNode(sortName);
		} else if (sortMode ==SortMode.NO_SORT) {
			rootNode = new DefaultMutableTreeNode(sortName);
		} else {
			rootNode = new DefaultMutableTreeNode("No Data ");
		}

		//Loop through histograms and load them
		Iterator iter = Histogram.getHistogramList().iterator();
		while(iter.hasNext()){
			Histogram hist =(Histogram)iter.next();
		 	DefaultMutableTreeNode histNode =
			      new DefaultMutableTreeNode(hist);
		 		
		 	//Loop through histograms and load them
			List listGates =hist.getGates();
			Iterator iterGate=listGates.iterator();
		 	while(iterGate.hasNext()){
		 		Gate gate= (Gate)iterGate.next();
		 		DefaultMutableTreeNode gateNode =
				      new DefaultMutableTreeNode(gate);
		 		histNode.add(gateNode);
		 	}
			
			 rootNode.add(histNode);
		}

		treeModel.setRoot(rootNode);		
		 
	}
	/**
	 * A node has been selected in the tree 
	 * Set selected object
	 * @param nodeObject
	 */
	private void selection(Object nodeObject){

		//Syncronize events should not fire events
		if (isSyncEvent)
			return;
		
		if (nodeObject instanceof Histogram) {			
			Histogram hist =(Histogram)nodeObject;
				status.setHistName(hist.getName());
				broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hist);
		} else if (nodeObject instanceof Gate) {
			Gate gate =(Gate)nodeObject;			
			Histogram hist =gate.getHistogram();
			//Change selected histogram if needed
			if (hist!=status.getCurrentHistogram()) {
				status.setHistName(hist.getName());
				broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hist);
			}
			
			status.setCurrentGateName(gate.getName());
			broadcaster.broadcast(BroadcastEvent.Command.GATE_SELECT, gate);
			
		}
	}	

	/**
	 * Refresh the selected node
	 *
	 */
	private void refreshSelection(){
		
		DefaultMutableTreeNode currentNode;
		Histogram hist = status.getCurrentHistogram();
		
		//if (rootNode==null)
	//		return;
		
		//Loop through all nodes to find select histogram
		Enumeration nodeEnum =rootNode.breadthFirstEnumeration();
		while(nodeEnum.hasMoreElements()){
			currentNode=(DefaultMutableTreeNode)nodeEnum.nextElement();
			TreeNode [] tnp=currentNode.getPath();
			
			Object obj=currentNode.getUserObject();
			if (obj instanceof Histogram) {
				if (obj==hist){
					TreeNode []tnpath=currentNode.getPath();
					TreePath tp = new TreePath(tnpath);
					dataTree.setSelectionPath(tp);
					break;
				}
			}	
		}		
		repaint();
		dataTree.repaint();
	}
	/**
	 * Implementation of Observable interface.
	 * 
	 * @param observable
	 *            the sender
	 * @param o
	 *            the message
	 */
	public void update(Observable observable, Object o) {
		final BroadcastEvent be = (BroadcastEvent) o;
		final BroadcastEvent.Command command = be.getCommand();
		isSyncEvent=true;		
		if (command == BroadcastEvent.Command.HISTOGRAM_SELECT) {
			refreshSelection();
		}else if (command == BroadcastEvent.Command.HISTOGRAM_NEW) {
			loadTree();
		} else if (command == BroadcastEvent.Command.HISTOGRAM_ADD) {
			loadTree();
		} else if (command == BroadcastEvent.Command.GATE_SELECT) {			
			refreshSelection();
		} else if (command == BroadcastEvent.Command.GATE_ADD) {
			loadTree();
		} else if (command == BroadcastEvent.Command.GATE_SET_SAVE
				|| command == BroadcastEvent.Command.GATE_SET_OFF) {
			refreshSelection();
		} else if (command == BroadcastEvent.Command.RUN_STATE_CHANGED) {
			loadTree();
		}
		isSyncEvent=false;
		/*
			final String lastHistName = status.getHistName();
			selectHistogram(Histogram.getHistogram(lastHistName));
			dataChanged();
			syncHistChooser();			
		} else if (command==BroadcastEvent.Command.OVERLAY_OFF){
			setOverlaySelected(false);
		}
		*/
	}
	
}
