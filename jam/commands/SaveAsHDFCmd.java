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
	 * Save to a hdf, prompt for overwrite
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) throws CommandException {
		final HDFIO hdfio = new HDFIO(status.getFrame(), msghdlr);		
		if ( cmdParams==null) {//No file given		
			//int write = hdfio.writeFile();
			hdfio.writeFile();
			//if (write==JFileChooser.APPROVE_OPTION){
				//KBS FIXME setSaveEnabled(true);
				//??Is this the right instead of setSaveEnabled 
				//status.setSortMode(file);
			//}
			//throw new CommandException("Error executing command save");				
		} else {//File name given	
			//KBS check for valid file if not done in hdfio
			hdfio.writeFile((File)cmdParams[0]);			
		}			
	}

	/* 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
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
