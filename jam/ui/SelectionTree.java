package jam.ui;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.SortMode;
import jam.global.MessageHandler;
import jam.plot.Display;

//import java.awt.*;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;


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
	private boolean isSync=false;
	
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
	
	
	private void createHistTree(){

		
		SortMode sortMode =status.getSortMode();
		
		if (sortMode==SortMode.FILE){
			String fileName=status.getOpenFile().getName();
			rootNode = new DefaultMutableTreeNode(fileName);
		}else if (sortMode==SortMode.OFFLINE){
			rootNode = new DefaultMutableTreeNode("Offline Sort ");
		} else if ((sortMode==SortMode.ONLINE_DISK) ||(sortMode==SortMode.ONLINE_DISK)){
			rootNode = new DefaultMutableTreeNode("Online Sort ");
		} else if (sortMode ==SortMode.NO_SORT) {
			rootNode = new DefaultMutableTreeNode("Histograms ");
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
	
	public void refresh(){
		createHistTree();
		repaint();
		histTree.repaint();
	}
	
	private void selection(Object nodeObject){
		if (nodeObject instanceof Histogram) {
			
			Histogram hist =(Histogram)nodeObject;
			status.setHistName(hist.getName());
			broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, hist);
		} else if (nodeObject instanceof Gate) {
			Gate gate =(Gate)nodeObject;
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
		
		if (command == BroadcastEvent.Command.HISTOGRAM_NEW) {
			createHistTree();
		} else if (command == BroadcastEvent.Command.HISTOGRAM_ADD) {
			createHistTree();
		} else if (command == BroadcastEvent.Command.GATE_ADD) {
			createHistTree();
		} else if (command == BroadcastEvent.Command.GATE_SET_SAVE
				|| command == BroadcastEvent.Command.GATE_SET_OFF) {
			refresh();
		}
		/*
			final String lastHistName = status.getHistName();
			selectHistogram(Histogram.getHistogram(lastHistName));
			dataChanged();
		} else if (command == BroadcastEvent.Command.HISTOGRAM_ADD) {
			dataChanged();
		} else if (command == BroadcastEvent.Command.HISTOGRAM_SELECT) {
			syncHistChooser();			
		} else if (command == BroadcastEvent.Command.RUN_STATE_CHANGED) {
			setRunState((RunState) be.getContent());
		} else if (command==BroadcastEvent.Command.OVERLAY_OFF){
			setOverlaySelected(false);
		}
		*/
	}
	
}
