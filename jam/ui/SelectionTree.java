package jam.ui;

import jam.RunState;
import jam.data.Histogram;
import jam.data.Gate;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.plot.Display;

//import java.awt.*;
import java.awt.BorderLayout;
import java.util.Observable;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*; 
import javax.swing.tree.*;


/**
 * @author ken
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SelectionTree extends JPanel implements Observer {

	JTree histTree;
	
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
		//broadcaster.addObserver(this);
		
		 histTree = new JTree();
		//createHistTree();
 
	}	
	
	
	private void createHistTree(){

		histTree.removeAll();
		
		//String fileName=status.getOpenFile();
	
		 DefaultMutableTreeNode fileNode =
		      new DefaultMutableTreeNode("FileName");
		 
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
			
			 fileNode.add(histNode);
		 }

		 histTree = new JTree(fileNode);
		 histTree.setCellRenderer(new SelectionTreeCellRender());
		 setLayout(new BorderLayout());
		 this.add(new JScrollPane(histTree), BorderLayout.CENTER);
	
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
	}
	
	public void refresh(){
		createHistTree();
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
		}
		/*
			final String lastHistName = status.getHistName();
			selectHistogram(Histogram.getHistogram(lastHistName));
			dataChanged();
		} else if (command == BroadcastEvent.Command.HISTOGRAM_ADD) {
			dataChanged();
		} else if (command == BroadcastEvent.Command.HISTOGRAM_SELECT) {
			syncHistChooser();			
		} else if (command == BroadcastEvent.Command.GATE_ADD) {
			final String lastHistName = status.getHistName();
			selectHistogram(Histogram.getHistogram(lastHistName));
			gatesChanged();
		} else if (command == BroadcastEvent.Command.GATE_SET_SAVE
				|| command == BroadcastEvent.Command.GATE_SET_OFF) {
			gateChooser.repaint();
			histogramChooser.repaint();
		} else if (command == BroadcastEvent.Command.RUN_STATE_CHANGED) {
			setRunState((RunState) be.getContent());
		} else if (command==BroadcastEvent.Command.OVERLAY_OFF){
			setOverlaySelected(false);
		}
		*/
	}
	
}
