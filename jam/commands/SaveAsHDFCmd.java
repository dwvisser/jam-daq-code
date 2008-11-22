package jam.commands;

import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import com.google.inject.Inject;

/**
 * Save data to an hdf file.
 * 
 * @author Ken Swartz
 */
final class SaveAsHDFCmd extends AbstractCommand {

	private transient final HDFIO hdfio;
	private transient final Frame frame;

	@Inject
	SaveAsHDFCmd(final Frame frame, final HDFIO hdfio) {
		super("Save as\u2026");
		this.frame = frame;
		this.hdfio = hdfio;
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S,
				CTRL_MASK));
		final Icon iSaveAs = loadToolbarIcon("jam/ui/SaveAsHDF.png");
		putValue(Action.SMALL_ICON, iSaveAs);
		putValue(Action.SHORT_DESCRIPTION,
				"Save histograms to a new hdf data file.");

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
		saveHDFFile(file);
	}

	private void saveHDFFile(final File file) {
		if (file == null) { // No file given
			final JFileChooser jfile = new JFileChooser(HDFIO
					.getLastValidFile());
			jfile.setFileFilter(new HDFileFilter(true));
			final int option = jfile.showSaveDialog(frame);
			/* don't do anything if it was cancel */
			if (option == JFileChooser.APPROVE_OPTION
					&& jfile.getSelectedFile() != null) {
				final File selectedFile = jfile.getSelectedFile();
				hdfio.writeFile(selectedFile);

			}
		} else { // File name given
			hdfio.writeFile(file);
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
	protected void executeParse(final String[] cmdTokens) {
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
