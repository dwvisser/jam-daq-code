package jam.commands;

import jam.global.CommandListenerException;
import jam.io.hdf.HDFIO;

import java.io.File;

/** 
 * Save gates and scalers 
 * 
 * @author Ken Swartz
 *
 */
final class SaveGatesCmd extends AbstractCommand implements Commandable {

	public void initCommand() {
		putValue(NAME, "Save gates, scalers & parameters as\u2026");
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
		final HDFIO hdfio = new HDFIO(status.getFrame(), msghdlr);
		if (cmdParams == null || cmdParams.length==0) { //No file given		
			hdfio.writeFile(false, true, true, true);
		} else { //File name given	
			hdfio.writeFile(false, true, true, true, (File) cmdParams[0]);
		}
	}

	/**
	 * Save to an hdf file.
	 * 
	 * @param cmdParams empty array or <code>null</code> to use a 
	 * file dialog, or the name of a <code>File</code> as the first
	 * element
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 * @see java.io.File
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
		if (cmdTokens == null || cmdTokens.length == 0) {
			execute(null);
		} else {
			final Object[] cmdParams = new Object[1];
			final File file = new File((String) cmdTokens[0]);
			cmdParams[0] = file;
			execute(cmdParams);
		}
	}

}
