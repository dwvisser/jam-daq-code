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
public class AddHDFCmd extends AbstractCommand implements Commandable {

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	public void execute(Object[] cmdParams) {
		JFrame frame = status.getFrame();				
		final HDFIO	hdfio = new HDFIO(frame, msghdlr);
		hdfio.readFile(FileOpenMode.ADD);

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	public void executeParse(String[] cmdTokens)
		throws CommandListenerException {
			execute(null);

	}

}
