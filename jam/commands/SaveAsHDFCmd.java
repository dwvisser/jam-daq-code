package jam.commands;

import jam.io.hdf.HDFIO;
import jam.global.CommandListenerException;

import java.io.File;
import javax.swing.JFrame;
import javax.swing.JFileChooser;


/**
 * Save as hdf
 * 
 * @author Ken
 *
 */
public class SaveAsHDFCmd extends AbstractCommand implements Commandable {

	/**
	 * Save to a hdf, prompt for overwrite
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	public void execute(Object[] cmdParams) throws CommandException {
		
		JFrame frame = status.getFrame();	
		final HDFIO	hdfio = new HDFIO(frame, msghdlr);		
		File file;
				
		//No file given		
		if ( cmdParams==null) {		
			
			int write = hdfio.writeFile();
			if (write==JFileChooser.APPROVE_OPTION){
				
				//KBS FIXME setSaveEnabled(true);
				//??Is this the right instead of setSaveEnabled 
				//status.setSortMode(file);
			}
			
			//throw new CommandException("Error executing command save");
				
			
		//File name given								
		} else {
			
			file =(File)cmdParams[0];
			//KBS check for valid file if not done in hdfio
			hdfio.writeFile(file);			
		}			


	}

	/* 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	public void executeParse(String[] cmdTokens) throws CommandListenerException {
		
		
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
