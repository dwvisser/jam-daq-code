package jam.commands;

import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import java.awt.Frame;

/**
 * Save data to an hdf file.
 * 
 * @author Ken Swartz
 */
final class SaveAsHDFCmd extends AbstractCommand implements Commandable {

	public void initCommand() {
		putValue(NAME, "Save as\u2026");
		putValue(
			ACCELERATOR_KEY,
			KeyStroke.getKeyStroke(KeyEvent.VK_S, CTRL_MASK));
	}

	/**
	 * Save to an hdf file.
	 * 
	 * @param cmdParams empty array or <code>null</code> to use a 
	 * file dialog, or an array with a <code>File</code> as the first
	 * element
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 * @see java.io.File
	 */
	protected void execute(Object[] cmdParams) {
		
		Frame frame= STATUS.getFrame();
		final HDFIO hdfio = new HDFIO(STATUS.getFrame(), msghdlr);
		
		if (cmdParams == null || cmdParams.length==0) { //No file given		
	        final JFileChooser jfile = new JFileChooser(HDFIO.getLastValidFile());
	        jfile.setFileFilter(new HDFileFilter(true));
	        int option = jfile.showSaveDialog(frame);
	        /* don't do anything if it was cancel */
	        if (option == JFileChooser.APPROVE_OPTION
	                && jfile.getSelectedFile() != null) {
	            final File file = jfile.getSelectedFile();
	            hdfio.writeFile(true, true, true, true, file);
	        }
		} else { //File name given	
			hdfio.writeFile(true, true, true, true, (File) cmdParams[0]);
		}
	}

	/**
	 * Save to an hdf file.
	 * 
	 * @param cmdTokens empty array or <code>null</code> to use a 
	 * file dialog, or the name of a <code>File</code> as the first
	 * element
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 * @see java.io.File
	 */
	protected void executeParse(String[] cmdTokens) {
		if (cmdTokens==null || cmdTokens.length == 0) {
			execute(null);
		} else {
			final Object[] cmdParams = new Object[1];
			final File file = new File(cmdTokens[0]);
			cmdParams[0] = file;
			execute(cmdParams);
		}
	}
}
