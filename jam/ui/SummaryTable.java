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

import com.google.inject.Inject;

/**
 * Create a summary table for a group
 * 
 * @author Ken Swartz
 */
public class SummaryTable extends JPanel implements Observer {

	/**
	 * whether one group or all groups is selected
	 */
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

	/**
	 * The current table the system looks up.
	 */
	private static SummaryTable currentTable;

	/**
	 * Private mutex for the current table.
	 */
	private static final Object LOCK = new Object();

	/**
	 * Gets the display.
	 * 
	 * @return the display
	 */
	public static SummaryTable getTable() {
		synchronized (LOCK) {
			return currentTable;
		}
	}

	/**
	 * Sets the table.
	 * 
	 * @param table
	 *            the table
	 */
	public static void setTable(final SummaryTable table) {
		synchronized (LOCK) {
			currentTable = table;
		}
	}

	private transient final SummaryTableModel summaryTableModel = new SummaryTableModel();

	private transient final JamStatus status;

	/**
	 * Default constructor.
	 * 
	 * @param status
	 *            Application status
	 * @param broadcaster
	 *            broadcasts state changes
	 * 
	 */
	@Inject
	public SummaryTable(final JamStatus status, final Broadcaster broadcaster) {
		super();
		this.status = status;
		setLayout(new BorderLayout());
		final JTable table = new JTable(summaryTableModel);
		final JScrollPane scrollPane = new JScrollPane(table);
		add(scrollPane);
		final SummaryTableToolbar toolbar = new SummaryTableToolbar(
				summaryTableModel);
		summaryTableModel.setOptions(true, true, true);
		this.add(toolbar, BorderLayout.NORTH);
		broadcaster.addObserver(this);
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
			summaryTableModel.setGroup((Group) status.getCurrentGroup());
		}
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

}
