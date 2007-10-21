package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.ui.ExtensionFileFilter;
import jam.ui.SummaryTable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;

import javax.swing.JFileChooser;

/**
 * Export the summary table
 * 
 * @author Kennneth Swartz
 * 
 */
public class ExportSummaryTableCmd extends AbstractCommand implements Observer {

	private final static int BUFFER_SIZE = 256 * 2;

	private static final String[] EXTS = { "dat", "txt" };

	private static final ExtensionFileFilter FILTER = new ExtensionFileFilter(
			EXTS, "Text file");

	private File chooseFile() {
		File file = null;
		final JFileChooser jfile = new JFileChooser();

		jfile.setFileFilter(FILTER);
		final int option = jfile.showSaveDialog(STATUS.getFrame());
		/* don't do anything if it was cancel */
		if (option == JFileChooser.APPROVE_OPTION
				&& jfile.getSelectedFile() != null) {
			file = jfile.getSelectedFile();
		}

		return file;
	}

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

	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		// TODO Auto-generated method stub
	}

	ExportSummaryTableCmd() {
		super("Table");
	}

	private void saveTable(final File file) {
		final SummaryTable summaryTable = SummaryTable.getTable();
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

	public void update(final Observable observe, final Object obj) {
		final BroadcastEvent event = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = event.getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			setEnabled(true);
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			setEnabled(false);
		}
	}

}
