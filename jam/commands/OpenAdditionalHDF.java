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
		File file =null;
		if (cmdParams!=null) {
			if (cmdParams.length>0){
				file =(File)cmdParams[0];
			}
		}
		readAdditionalHDFFile(file);
	}

	/*
	 * Read in an HDF file
	 */ 
	private void readAdditionalHDFFile(final File file) {
		final Frame frame= STATUS.getFrame();		
		hdfio.setListener(this);
		final boolean isReading;
		if (file==null) {//No file given				
	        final JFileChooser jfile = new JFileChooser(HDFIO.getLastValidFile());
	        jfile.setFileFilter(new HDFileFilter(true));
	        final int option = jfile.showOpenDialog(frame);
	        // dont do anything if it was cancel
	        if (option == JFileChooser.APPROVE_OPTION
	                && jfile.getSelectedFile() != null) {
	        	final File selectedFile = jfile.getSelectedFile();
				isReading=hdfio.readFile(FileOpenMode.OPEN_MORE, selectedFile);	        	
	        } else {
	        	isReading=false;
	        }
		} else {
			isReading=hdfio.readFile(FileOpenMode.OPEN_MORE, file);
		}
		if (!isReading){//File was read in so no callback	
			notifyApp();
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
			final File file = new File(cmdTokens[0]); 
			cmdParams[0]=file;
			execute(cmdParams);
		}
	}

	private void notifyApp() {
		Histogram firstHist = null;
		
		//Update app status		
		AbstractControl.setupAll();
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		
		//Set the current histogram to the first opened histogram
		Group firstGroup =hdfio.getFirstLoadGroup();
        if (firstGroup!=null) {
            STATUS.setCurrentGroup(firstGroup);
        }
        /* Set the current histogram to the first opened histogram. */
        if (STATUS.getCurrentGroup().getHistogramList().size() > 0) {
            firstHist = (Histogram) STATUS.getCurrentGroup().getHistogramList()
                    .get(0);
        }
		
		STATUS.setCurrentHistogram(firstHist);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, firstHist);
	}			

	/**
	 * Called by HDFIO when asynchronized IO is completed  
	 */
	public void completedIO(String message, String errorMessage) {
		hdfio.removeListener();
		notifyApp();
		file=null;		
	}
}
