package jam.commands;

import javax.swing.JFrame;

import jam.io.hdf.HDFIO;
import jam.data.control.DataControl;
import jam.io.FileOpenMode;
import jam.global.BroadcastEvent;

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
		
		//No file given		
		if ( cmdParams==null) {		

			JFrame frame = status.getFrame();
							
			final HDFIO	hdfio = new HDFIO(frame, msghdlr);
						
			if (hdfio.readFile(FileOpenMode.OPEN)) { //true if successful
				status.setSortModeFile(hdfio.getFileNameOpen());
				DataControl.setupAll();
				broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
				frame.repaint();
				//KBS still needed 
				//setSaveEnabled(true);
			}
		}		
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	public void executeParse(String[] cmdTokens) {		
		
		if (cmdTokens==null) {
			execute(null);
		}
	}

}
