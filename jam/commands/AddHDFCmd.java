package jam.commands;

import jam.io.hdf.HDFIO;
import jam.io.FileOpenMode;
import jam.global.CommandListenerException;

import javax.swing.JFrame;
/**
 * Add counts from a histogram
 * 
 * @author Ken Swartz
 */
final class AddHDFCmd extends AbstractCommand implements Commandable {

	AddHDFCmd(){
		putValue(NAME,"Add counts...");
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		JFrame frame = status.getFrame();				
		final HDFIO	hdfio = new HDFIO(frame, msghdlr);
		hdfio.readFile(FileOpenMode.ADD);

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
			execute(null);
	}

}
