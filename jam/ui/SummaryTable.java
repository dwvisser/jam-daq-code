package jam.ui;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;

import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.JScrollPane;

/**
 * Create a summary table for a group
 *
 * @author Ken Swartz
 */
public class SummaryTable extends JPanel implements Observer {

	public final static int ALL_GROUPS_SELECTED=0;
	
	public final static int SINGLE_GROUP_SELECTED=1;
	
	private final Broadcaster broadcaster = Broadcaster.getSingletonInstance();
	
	private final JamStatus status = JamStatus.getSingletonInstance();	
	
	private SummaryTableToolbar summaryTableToolbar;
	
	private SummaryTableModel summaryTableModel;
	
	public SummaryTable() {
		setLayout(new BorderLayout());

		summaryTableModel = new SummaryTableModel();			
	    JTable table = new JTable(summaryTableModel);
	    JScrollPane scrollPane = new JScrollPane(table);	
	    this.add(scrollPane);
	    
		summaryTableToolbar = new SummaryTableToolbar(this, summaryTableModel);
		summaryTableModel.setOptions(true, true, true);
		
		this.add(summaryTableToolbar,BorderLayout.NORTH);
		broadcaster.addObserver(this);
	}
 
	/**
	 * Implementation of Observable interface.
	 * 
	 * @param observable
	 *            not sure
	 * @param o
	 *            not sure
	 */
	public void update(Observable observable, Object o) {
		BroadcastEvent be = (BroadcastEvent) o;
		if	(be.getCommand() == BroadcastEvent.Command.ROOT_SELECT) {
			summaryTableModel.setSelectionType(ALL_GROUPS_SELECTED);
		} else if	(be.getCommand() == BroadcastEvent.Command.GROUP_SELECT) {
			summaryTableModel.setSelectionType(SINGLE_GROUP_SELECTED);			
			summaryTableModel.setGroup(status.getCurrentGroup());
			
		}		
	}
	
}
