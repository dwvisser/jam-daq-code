package jam.commands;

import jam.data.control.AbstractControl;
import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;

/**
 * Open an additional hdf file
 * 
 * @author Ken Swartz
 *
 */
public class OpenAdditionalHDF extends AbstractCommand implements HDFIO.AsyncListener {

	private File file=null;
	final HDFIO	hdfio;		
	
	
	OpenAdditionalHDF(){
		putValue(NAME,"Open Additional\u2026");
		Frame frame= STATUS.getFrame();
		hdfio = new HDFIO(frame, msghdlr);				
	}

	/* 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(final Object[] cmdParams) {
		readAdditionalHDFFile(cmdParams);
	}

	/**
	 * Read in a HDF file
	 * @param cmdParams
	 */ 
	private void readAdditionalHDFFile(Object[] cmdParams) {
		Frame frame= STATUS.getFrame();		
		hdfio.setListener(this);
		final boolean isFileRead;
		if (cmdParams!=null) {
			file =(File)cmdParams[0];
		} 	
		if (file==null) {//No file given				
	        final JFileChooser jfile = new JFileChooser(HDFIO.getLastValidFile());
	        jfile.setFileFilter(new HDFileFilter(true));
	        final int option = jfile.showOpenDialog(frame);
	        // dont do anything if it was cancel
	        if (option == JFileChooser.APPROVE_OPTION
	                && jfile.getSelectedFile() != null) {
	        	file = jfile.getSelectedFile();
				isFileRead=hdfio.readFile(FileOpenMode.OPEN_ADDITIONAL, file);	        	
	        } else {
	        	isFileRead=false;
	        }
		} else {
			isFileRead=hdfio.readFile(FileOpenMode.OPEN_ADDITIONAL, file);
		}
		if (isFileRead){//File was read in	
			notifyApp(HDFIO.getLastValidFile());
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
		//Update app status
		STATUS.setSortMode(file);
		AbstractControl.setupAll();
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		
		//FIXME KBS need a way to get first addtional readin histogram
		//Set the current histogram to the first opened histogram
		Histogram firstHist = (Histogram)Group.getCurrentGroup().getHistogramList().get(0);
		STATUS.setCurrentHistogram(firstHist);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, firstHist);
	}			

	/**
	 * Called by HDFIO when asynchronized IO is completed  
	 */
	public void CompletedIO(String message, String errorMessage) {
		hdfio.removeListener();
		notifyApp(file);
		file=null;		
	}
	
}
