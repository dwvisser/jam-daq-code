package jam.commands;

import javax.swing.JFrame;
import java.io.File;

import jam.io.hdf.HDFIO;
import jam.data.control.DataControl;
import jam.io.FileOpenMode;
import jam.global.BroadcastEvent;
import jam.global.JamStatus;


/**
 * Open a hdf file
 *  
 * @author Ken Swartz
 *
 */
public class OpenHDFCmd extends AbstractCommand implements Commandable {

	/* 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	public void execute(Object[] cmdParams) {
		
		JFrame frame = status.getFrame();						
		final HDFIO	hdfio = new HDFIO(frame, msghdlr);
		
		boolean isFileRead =false; 
		
		//No file given		
		if ( cmdParams==null) {		
						
			if (hdfio.readFile(FileOpenMode.OPEN)) { //true if successful
				isFileRead=true;
			}
			
		//File given	
		} else {
			if (hdfio.readFile(FileOpenMode.OPEN, (File)cmdParams[0])) {
				isFileRead=true;
			}
		}

		//File was read in
		if (isFileRead)		
			notifyApp(hdfio.getFileNameOpen());
		
		
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	public void executeParse(String[] cmdTokens) {		
		
		Object [] cmdParams = new Object[1]; 
		if (cmdTokens.length==0) {
			execute(null);
		} else {
			File file = new File(cmdTokens[0]); 
			cmdParams[0]=file;
			execute(cmdParams);
		}
	}

	private void notifyApp(String fileName) {
		 
		JFrame frame = status.getFrame();				
				
		status.setSortMode(JamStatus.FILE, fileName);
		DataControl.setupAll();
		broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
		frame.repaint();
		
	}							

}
