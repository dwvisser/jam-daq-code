package jam.commands;

import com.google.inject.Inject;
import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.ui.ExtensionFileFilter;
import jam.ui.SummaryTable;

import javax.swing.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.logging.Level;

/**
 * Export the summary table
 * 
 * @author Kennneth Swartz
 * 
 */
public class ExportSummaryTableCmd extends AbstractCommand implements PropertyChangeListener {

	private final static int BUFFER_SIZE = 256 * 2;

	private static final String[] EXTS = { "dat", "txt" };

	private static final ExtensionFileFilter FILTER = new ExtensionFileFilter(
			EXTS, "Text file");

	private transient final JFrame frame;

	private transient final SummaryTable summaryTable;

	private File chooseFile() {
		File file = null;
		final JFileChooser jfile = new JFileChooser();

		jfile.setFileFilter(FILTER);
		final int option = jfile.showSaveDialog(this.frame);
		/* don't do anything if it was cancel */
		if (option == JFileChooser.APPROVE_OPTION
				&& jfile.getSelectedFile() != null) {
			file = jfile.getSelectedFile();
		}

		return file;
	}

	@Override
	protected void execute(final Object[] cmdParams) throws CommandException {
		File file = null;
		if (cmdParams != null && cmdParams.length > 0) {
			file = (File) cmdParams[0];
		}
		// No file given
		if (file == null) {
			file = chooseFile();
		}
		// No file chosen
		if (file != null) {
			saveTable(file);
		}
	}

	@Inject
	ExportSummaryTableCmd(final JFrame frame, final SummaryTable summaryTable) {
		super("Table");
		this.frame = frame;
		this.summaryTable = summaryTable;
	}

	private void saveTable(final File file) {
		try {
			LOGGER.info("Starting to write out table to " + file);
			// Create writer stream
			final FileOutputStream outStream = new FileOutputStream(file);
			final BufferedOutputStream buffStream = new BufferedOutputStream(
					outStream, BUFFER_SIZE);
			summaryTable.writeTable(buffStream);
			buffStream.flush();
			outStream.flush();
			outStream.close();
			LOGGER.info("Done writing out table.");
		} catch (FileNotFoundException fnfe) {
			LOGGER.severe("Cannot find file: " + file);
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE,
					"IOException while writing table to file: " + file, ioe);
		}

	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final BroadcastEvent.Command command = ((BroadcastEvent) evt).getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(true);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			setEnabled(false);
		}
	}

	@Override
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		// Auto-generated method stub
	}

}
