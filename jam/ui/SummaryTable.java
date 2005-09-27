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

	public static enum Selection {
		/**
		 * All groups are selected?
		 */
		ALL_GROUPS,

		/**
		 * One group is selected?
		 */
		SINGLE_GROUP
	}

	private transient final SummaryTableModel summaryTableModel = new SummaryTableModel();

	/**
	 * Default constructor.
	 * 
	 */
	public SummaryTable() {
		super();
		setLayout(new BorderLayout());
		JTable table = new JTable(summaryTableModel);
		JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane);
		final SummaryTableToolbar toolbar = new SummaryTableToolbar(
				summaryTableModel);
		summaryTableModel.setOptions(true, true, true);
		this.add(toolbar, BorderLayout.NORTH);
		Broadcaster.getSingletonInstance().addObserver(this);
	}

	/**
	 * Write out the table to a writer stream
	 * 
	 * @param outputStream
	 *            to write to
	 */
	public void writeTable(final OutputStream outputStream) {
		final PrintWriter writer = new PrintWriter(outputStream);
		final int numCols = summaryTableModel.getColumnCount();
		final int numRows = summaryTableModel.getRowCount();
		// write header
		for (int i = 0; i < numCols; i++) {
			writer.print(summaryTableModel.getColumnName(i));
			if (i < numCols - 1) {
				writer.print("\t");
			}
		}
		writer.println();
		// write data
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				writer.print(summaryTableModel.getValueAt(i, j));
				if (j < numCols - 1) {
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
	 * @param object
	 *            not sure
	 */
	public void update(final Observable observable, final Object object) {
		final BroadcastEvent event = (BroadcastEvent) object;
		if (event.getCommand() == BroadcastEvent.Command.ROOT_SELECT) {
			summaryTableModel.setSelectionType(Selection.ALL_GROUPS);
		} else if (event.getCommand() == BroadcastEvent.Command.GROUP_SELECT) {
			summaryTableModel.setSelectionType(Selection.SINGLE_GROUP);
			summaryTableModel.setGroup((Group) JamStatus.getSingletonInstance()
					.getCurrentGroup());
		}
	}

}
