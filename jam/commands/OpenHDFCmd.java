package jam.commands;

import jam.data.control.DataControl;
import jam.global.BroadcastEvent;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;

import java.io.File;

/**
 * Open a hdf file
 *  
 * @author Ken Swartz
 *
 */
final class OpenHDFCmd extends AbstractCommand {

	/* 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		final boolean isFileRead; 
		
		final HDFIO	hdfio = new HDFIO(status.getFrame(), msghdlr);		
		if ( cmdParams==null) {//No file given									
			isFileRead=hdfio.readFile(FileOpenMode.OPEN);//opens file dialog
		} else {
			isFileRead=hdfio.readFile(FileOpenMode.OPEN, (File)cmdParams[0]);
		}	
		if (isFileRead){//File was read in	
			notifyApp(hdfio.getFileOpen());
		}		
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) {		
		Object [] cmdParams = new Object[1]; 
		if (cmdTokens.length==0) {
			execute(null);
		} else {
			File file = new File(cmdTokens[0]); 
			cmdParams[0]=file;
			execute(cmdParams);
		}
	}

	private void notifyApp(File file) {
		status.setSortMode(file);
		DataControl.setupAll();
		broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
		status.getFrame().repaint();
	}							
}
