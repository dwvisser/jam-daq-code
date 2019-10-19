package jam.ui;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Create a summary table for a group
 * 
 * @author Ken Swartz
 */
@Singleton
public class SummaryTable extends JPanel implements PropertyChangeListener {

	/**
	 * whether one group or all groups is selected
	 */
	public enum Selection {
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
		broadcaster.addPropertyChangeListener(this);
	}

	/**
	 * Implementation of Observable interface.
	 * 
	 * @param observable
	 *            not sure
	 * @param object
	 *            not sure
	 */
	// public void update(final Observable observable, final Object object) {
	public void propertyChange(PropertyChangeEvent evt) {
		final BroadcastEvent event = (BroadcastEvent) evt;
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
