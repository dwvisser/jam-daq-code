package jam.commands;

import jam.global.CommandListenerException;
import jam.io.hdf.HDFIO;

import java.io.File;

/**
 * Save as hdf
 * 
 * @author Ken
 *
 */
final class SaveAsHDFCmd extends AbstractCommand implements Commandable {
	
	SaveAsHDFCmd(){
		putValue(NAME,"Save as\u2026");
	}

	/**
	 * Save to a hdf, prompt for overwrite.
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) throws CommandException {
		final HDFIO hdfio = new HDFIO(status.getFrame(), msghdlr);		
		if ( cmdParams==null) {//No file given		
			hdfio.writeFile();
		} else {//File name given	
			hdfio.writeFile((File)cmdParams[0]);			
		}			
	}

	protected void executeParse(String[] cmdTokens) throws CommandListenerException {
		try {
			Object [] cmdParams = new Object[1]; 
			if (cmdTokens.length==0) {
				execute(null);
			} else {
				File file = new File((String)cmdTokens[0]); 
				cmdParams[0]=(Object)file;
				execute(cmdParams);
			}
		} catch (CommandException e) {
			throw new CommandListenerException(e.getMessage());
		}
	}
}
