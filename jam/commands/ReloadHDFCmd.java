package jam.commands;

import jam.io.hdf.HDFIO;
import jam.io.FileOpenMode;
import jam.global.CommandListenerException;

import javax.swing.JFrame;
/**
 *  Reload data from a hdf file
 * 
 * @author Ken Swartz
 *
 */
public class ReloadHDFCmd extends AbstractCommand implements Commandable {

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	public void execute(Object[] cmdParams) {
		
		JFrame frame = status.getFrame();				
		final HDFIO	hdfio = new HDFIO(frame, msghdlr);
		
		if (hdfio.readFile(FileOpenMode.RELOAD)) { //true if successful
			//KBS FIXME Call display Scalers command here
			//scalerControl.displayScalers();
		}


	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	public void executeParse(String[] cmdTokens)
		throws CommandListenerException {
			execute(null);
	}

}
