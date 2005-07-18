package jam.ui;

import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;

import java.awt.BorderLayout;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Create a summary table for a group
 *
 * @author Ken Swartz
 */
public class SummaryTable extends JPanel implements Observer {

    /**
     * All groups are selected?
     */
	public final static int ALL_GROUPS_SELECTED=0;
	
    /**
     * One group is selected?
     */
	public final static int SINGLE_GROUP_SELECTED=1;
	
	private final Broadcaster broadcaster = Broadcaster.getSingletonInstance();
	
	private final JamStatus status = JamStatus.getSingletonInstance();	
	
	private SummaryTableToolbar summaryTableToolbar;
	
	private SummaryTableModel summaryTableModel;
	
    /**
     * Default constructor.
     *
     */
	public SummaryTable() {
        super();
		setLayout(new BorderLayout());
		summaryTableModel = new SummaryTableModel();			
	    JTable table = new JTable(summaryTableModel);
	    JScrollPane scrollPane = new JScrollPane(table);	
	    this.add(scrollPane);
		summaryTableToolbar = new SummaryTableToolbar(summaryTableModel);
		summaryTableModel.setOptions(true, true, true);
		this.add(summaryTableToolbar,BorderLayout.NORTH);
		broadcaster.addObserver(this);
	}
    
	/**
	 * Write out the table to a writer stream
	 * @param outputStream to write to
	 */
	public void writeTable(OutputStream outputStream) {
		final PrintWriter writer = new PrintWriter(outputStream);
		int numCols = summaryTableModel.getColumnCount();
		int numRows = summaryTableModel.getRowCount();
		//write header
		for (int i=0;i<numCols;i++) {
			writer.print(summaryTableModel.getColumnName(i) );
			if (i<numCols-1) {
				writer.print("\t");
			}
		}
		writer.println();
		//write data
		for (int i=0;i<numRows;i++) {
			for (int j=0;j<numCols;j++) {
				writer.print(summaryTableModel.getValueAt(i,j));
				if (j<numCols-1) {
					writer.print("\t");
				}
			}
			writer.println();			
		}
		writer.flush();
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
			summaryTableModel.setGroup((Group)status.getCurrentGroup());			
		}		
	}
	
}
