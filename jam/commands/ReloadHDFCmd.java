package jam.commands;

import jam.global.CommandListenerException;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
/**
 *  Reload data from a hdf file
 * 
 * @author Ken Swartz
 *
 */
final class ReloadHDFCmd extends AbstractCommand {

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {			
		final HDFIO	hdfio = new HDFIO(status.getFrame(), msghdlr);
		hdfio.readFile(FileOpenMode.RELOAD);		
		//if (hdfio.readFile(FileOpenMode.RELOAD)) { //true if successful
			//KBS FIXME Call display Scalers command here
			//scalerControl.displayScalers();
		//}
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
			execute(null);
	}

}
