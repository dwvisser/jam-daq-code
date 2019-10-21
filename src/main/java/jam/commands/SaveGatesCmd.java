package jam.commands;

import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;

import com.google.inject.Inject;

import jam.global.CommandListenerException;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

/**
 * Save gates and scalers
 * 
 * @author Ken Swartz
 * 
 */
@SuppressWarnings("serial")
final class SaveGatesCmd extends AbstractCommand {

	private transient final Frame frame;
	private transient final HDFIO hdfio;

	@Inject
	SaveGatesCmd(final Frame frame, final HDFIO hdfio) {
		super("Save gates & parameters as\u2026");
		this.frame = frame;
		this.hdfio = hdfio;
	}

	/**
	 * Save to an hdf file.
	 * 
	 * @param cmdParams
	 *            empty array or <code>null</code> to use a file dialog, or an
	 *            array with a <code>File</code> as the first element
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 * @see java.io.File
	 */
	@Override
	protected void execute(final Object[] cmdParams) {
		File file = null;
		if (cmdParams != null && cmdParams.length > 0) {
			file = (File) cmdParams[0];
		}
		saveGates(file);
	}

	private void saveGates(final File file) {
		if (file == null) { // No file given
			final JFileChooser jfile = new JFileChooser(HDFIO
					.getLastValidFile());
			jfile.setFileFilter(new HDFileFilter(true));
			final int option = jfile.showSaveDialog(frame);
			/* don't do anything if it was cancel */
			if (option == JFileChooser.APPROVE_OPTION
					&& jfile.getSelectedFile() != null) {
				hdfio.writeFile(jfile.getSelectedFile(), false, true);
			}
		} else { // File name given
			hdfio.writeFile(file, false, true);
		}
	}

	/**
	 * Save to an hdf file.
	 * 
	 * @param cmdTokens
	 *            empty array or <code>null</code> to use a file dialog, or the
	 *            name of a <code>File</code> as the first element
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 * @see java.io.File
	 */
	@Override
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		if (cmdTokens == null || cmdTokens.length == 0) {
			execute(null);
		} else {
			final Object[] cmdParams = new Object[1];
			final File file = new File(cmdTokens[0]);
			cmdParams[0] = file;
			execute(cmdParams);
		}
	}

}
