package jam.commands;

import injection.GuiceInjector;
import jam.data.Group;
import jam.global.CommandListenerException;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * * Command to save a group of histograms.
 * 
 * @author Ken Swartz
 * 
 */
public class SaveGroupHDFCmd extends AbstractCommand {

	SaveGroupHDFCmd() {
		super("Save select group as\u2026");
	}

	@Override
	protected void execute(final Object[] cmdParams) throws CommandException {
		File file = null;
		Group group = (Group) GuiceInjector.getJamStatus().getCurrentGroup();
		if (cmdParams != null) {
			if (cmdParams.length > 0) {
				file = (File) cmdParams[0];
			}
			if (cmdParams.length > 1) {
				group = (Group) cmdParams[1];
			}
		}
		saveGroup(file, group);
	}

	private void saveGroup(final File file, final Group group) {
		final JFrame frame = GuiceInjector.getFrame();
		final HDFIO hdfio = new HDFIO(frame);
		if (group == null) {
			LOGGER.severe("Need to select a group.");
		} else {
			if (file == null) { // No file given
				final JFileChooser jfile = new JFileChooser(HDFIO
						.getLastValidFile());
				jfile.setFileFilter(new HDFileFilter(true));
				final int option = jfile.showSaveDialog(frame);
				/* don't do anything if it was cancel */
				if (option == JFileChooser.APPROVE_OPTION
						&& jfile.getSelectedFile() != null) {
					hdfio.writeFile(jfile.getSelectedFile(), group);
				}
			} else {
				hdfio.writeFile(file, group);
			}
		}
	}

	@Override
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		// do nothing
	}

}
