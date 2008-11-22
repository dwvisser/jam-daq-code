package jam.commands;

import jam.data.Group;
import jam.global.CommandListenerException;
import jam.global.JamStatus;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;

import com.google.inject.Inject;

/**
 * * Command to save a group of histograms.
 * 
 * @author Ken Swartz
 * 
 */
public class SaveGroupHDFCmd extends AbstractCommand {

	private transient final Frame frame;
	private transient final JamStatus status;
	private transient final HDFIO hdfio;

	@Inject
	SaveGroupHDFCmd(final Frame frame, final JamStatus status, final HDFIO hdfio) {
		super("Save select group as\u2026");
		this.frame = frame;
		this.status = status;
		this.hdfio = hdfio;
	}

	@Override
	protected void execute(final Object[] cmdParams) throws CommandException {
		File file = null;
		Group group = (Group) status.getCurrentGroup();
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
