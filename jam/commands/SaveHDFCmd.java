package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.global.SortMode;
import jam.io.hdf.HDFIO;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * Save to a hdf file
 *  
 * @author Ken Swartz
 *
 */
final class SaveHDFCmd extends AbstractCommand implements Observer {
	
	SaveHDFCmd(){
		putValue(NAME,"Save");
	}

	/**
	 * Save to a hdf, prompt for overwrite
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) throws CommandException {
		
		JFrame frame = status.getFrame();	
		final HDFIO	hdfio = new HDFIO(frame, msghdlr);		
		File file;
				
		//No file given		
		if ( cmdParams==null) {		
			file=status.getOpenFile();	
			if (file!=null) {
	 			//Prompt for overwrite
				if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
					frame ,"Replace the existing file? \n"+file.getName(),"Save "+file.getName(),
					JOptionPane.YES_NO_OPTION)){
						hdfio.writeFile(file);
					}
			//File null				
			} else {
				throw new CommandException("Error executing command save");
			}				
			
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
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final int command=be.getCommand();
		if (command==BroadcastEvent.SORT_MODE_CHANGED){
			final SortMode mode=status.getSortMode();
			setEnabled(mode==SortMode.FILE || mode==SortMode.NO_SORT);
		}
	}

}
