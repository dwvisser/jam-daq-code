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
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


/**
 * @author ken
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SelectionTree extends JPanel implements Observer {

	JTree histTree;
	
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
		 	
		 histTree = new JTree(treeModel);
		 histTree.setCellRenderer(new SelectionTreeCellRender());
		 histTree.addTreeSelectionListener(new TreeSelectionListener(){
	 	    public void valueChanged(TreeSelectionEvent e) {
	 	        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					histTree.getLastSelectedPathComponent();
	 	        if (node == null) 
	 	        	return;
	 	        Object nodeInfo = node.getUserObject();
		 	    selection(nodeInfo);		 	        
	 	    }		 			 	
		 });
		 
		 this.add(new JScrollPane(histTree), BorderLayout.CENTER);
		 
		//createHistTree();
 
	}	
	/**
	 * Load the tree for the data objects
	 *
	 */	
	private void loadTree(){

		
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
		 
		 Iterator iter = Histogram.getHistogramList().iterator();
		 
		 while(iter.hasNext()){
		 	Histogram hist =(Histogram)iter.next();
		 	DefaultMutableTreeNode histNode =
			      new DefaultMutableTreeNode(hist);
		 		
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

	public void reload(){
		loadTree();
		repaint();
		histTree.repaint();
	}

	public void refresh(){
		repaint();
		histTree.repaint();
	}
	
	private void refreshSelection(){
		Histogram hist = status.getCurrentHistogram();
		
		//Loop through nodes and set appropriate node to selected
		//Object rootNode =treeModel.getRoot();
		//DefaultMutableTreeModel root =(DefaultMutableTreeModel)treeModel.getRoot();		
		//TreeSelectionModel tsm =histTree.getSelectionModel();
		
		repaint();
		histTree.repaint();
	}
	
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
		
		if (command == BroadcastEvent.Command.HISTOGRAM_SELECT) {
			refreshSelection();					
		}else if (command == BroadcastEvent.Command.HISTOGRAM_NEW) {
			loadTree();
		} else if (command == BroadcastEvent.Command.HISTOGRAM_ADD) {
			loadTree();
		} else if (command == BroadcastEvent.Command.GATE_ADD) {
			loadTree();
		} else if (command == BroadcastEvent.Command.GATE_SET_SAVE
				|| command == BroadcastEvent.Command.GATE_SET_OFF) {
			refresh();
		} else if (command == BroadcastEvent.Command.RUN_STATE_CHANGED) {
			loadTree();
		}
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
