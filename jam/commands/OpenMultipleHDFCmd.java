package jam.commands;

import jam.global.CommandListenerException;
import jam.io.hdf.OpenMultipleFiles;

/**
 * Shows the open multiple files dialog
 * 
 * @author Ken Swartz
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OpenMultipleHDFCmd extends AbstractCommand {

	private OpenMultipleFiles openMultiple;
	
	OpenMultipleHDFCmd(){
		putValue(NAME,"Open Multiple\u2026");
		openMultiple=new OpenMultipleFiles(status.getFrame(), msghdlr);
	}
	
	protected void execute(Object[] cmdParams) throws CommandException {
		openMultiple.show();

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
			throws CommandListenerException {
		// TODO Auto-generated method stub

	}

}
