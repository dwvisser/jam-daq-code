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
import java.awt.Color;
import java.awt.Dimension;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Enumeration;
import java.util.Observer;

import javax.swing.border.EmptyBorder;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingConstants;
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
	
	/**Root node of the tree */
	DefaultMutableTreeNode rootNode;
	
	TreeSelectionListener treeSelectionListener=null;
	
	private final JLabel lrunState = new JLabel("   Welcome   ",
			SwingConstants.CENTER);
	
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
		
		//final Box pRunState = new Box(BoxLayout.X_AXIS, );
		final JPanel pRunState = new JPanel();
		pRunState.setBorder(new EmptyBorder(10,10,10,10));
		pRunState.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		pRunState.add(new JLabel(" Status: "));
		lrunState.setOpaque(true);
		pRunState.add(lrunState);
		 this.add(pRunState, BorderLayout.NORTH);

		//Default blank model
		treeModel= new DefaultTreeModel(new DefaultMutableTreeNode("No Data") );
		 	
		dataTree = new JTree(treeModel);
		dataTree.getSelectionModel().setSelectionMode( TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		//dataTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION);
		dataTree.setCellRenderer(new SelectionTreeCellRender());
		
		 
		addSelectionListener();
		 
		 this.add(new JScrollPane(dataTree), BorderLayout.CENTER);
 
	}
	private void addSelectionListener(){
		//Tree selection Listener
		if (treeSelectionListener==null){
			treeSelectionListener =new TreeSelectionListener(){
				public void valueChanged(TreeSelectionEvent e) {
		 	        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
						dataTree.getLastSelectedPathComponent();
		 	        if (node == null) 
		 	        	return;
		 	        Object nodeInfo = node.getUserObject();
			 	    selection(nodeInfo);		 	        
		 	    }		 			 	
			 };
		}
		dataTree.addTreeSelectionListener(treeSelectionListener);
	}
	private void removeSelectionListener(){
		dataTree.removeTreeSelectionListener(treeSelectionListener);
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
		//Remove so we don't get repeated callbacks while
		//selectiong other objects
		removeSelectionListener();
		
		if (nodeObject instanceof Histogram) {
			//dataTree.clearSelection();
			Histogram hist =(Histogram)nodeObject;
				status.setHistName(hist.getName());
				status.setCurrentGateName(null);
				broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hist);
		} else if (nodeObject instanceof Gate) {
			Gate gate =(Gate)nodeObject;
			Histogram hist =gate.getHistogram(); 
			
			TreePath [] selectTreePaths = new TreePath [2];
			selectTreePaths[0]= pathForDataObject(hist);
			selectTreePaths[1]= pathForDataObject(gate);
			dataTree.clearSelection();
			dataTree.setSelectionPaths(selectTreePaths);
			
			//Change selected histogram if needed
			//if (hist!=status.getCurrentHistogram()) {
				status.setHistName(hist.getName());
				broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hist);
			//}
			status.setCurrentGateName(gate.getName());
			broadcaster.broadcast(BroadcastEvent.Command.GATE_SELECT, gate);
			
		}
		//Re add listener now that we have set selection
		addSelectionListener();
	}	
	/**
	 * Refresh the selected node
	 *
	 */
	private void refreshSelection(){
		
		DefaultMutableTreeNode currentNode;
		Histogram hist = status.getCurrentHistogram();
		Gate gate = Gate.getGate(status.getCurrentGateName());
		TreePath histTreePath=null;
		TreePath gateTreePath=null;
		
		//Loop through all nodes to find selected nodes
		//histogram and gate
		Enumeration nodeEnum =rootNode.breadthFirstEnumeration();
		while(nodeEnum.hasMoreElements()){
			currentNode=(DefaultMutableTreeNode)nodeEnum.nextElement();
			TreeNode [] tnp=currentNode.getPath();
			
			Object obj=currentNode.getUserObject();
			if (obj instanceof Gate){
				if (obj==gate){
					final Histogram gateHist =gate.getHistogram();
					gateTreePath= pathForDataObject(gate);				
					histTreePath=pathForDataObject(gateHist);
				}
				
			}else if (obj instanceof Histogram) {
				if (obj==hist){
					histTreePath=pathForDataObject(hist);
				}
			}
		} //End loop for all nodes
		
		//Set path
		dataTree.clearSelection();
		if (gateTreePath!=null) {
			TreePath [] selectTreePaths = new TreePath [2];
			selectTreePaths[0]=histTreePath;
			selectTreePaths[1]=gateTreePath;
			dataTree.clearSelection();
			dataTree.setSelectionPaths(selectTreePaths);
		}else{
			dataTree.setSelectionPath(histTreePath);
		}
			

		
		repaint();
		dataTree.repaint();
	}
	/**
	 * Helper method to get TreePath for a data object
	 * @param ojc
	 * @return
	 */
	private TreePath pathForDataObject(Object dataObject){
		
		DefaultMutableTreeNode loopNode;
		TreePath treePath=null;
		Enumeration nodeEnum =rootNode.breadthFirstEnumeration();
		while(nodeEnum.hasMoreElements()){
			loopNode=(DefaultMutableTreeNode)nodeEnum.nextElement();
			Object obj=loopNode.getUserObject();
			if (dataObject==obj){
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
			//refreshSelection();
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
