package jam.commands;

import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.awt.Frame;
import java.io.File;
import java.util.List;
import java.util.Observer;

import javax.swing.JFileChooser;

/**
 * Add counts to histograms in memory from histograms in an HDF file.
 * 
 * @author Ken Swartz
 */
abstract class AbstractLoaderHDF extends AbstractCommand implements Observer, HDFIO.AsyncListener {
    
	final HDFIO	hdfio;	
	Group loadGroup;
	
    /**
     * Mode under which to do the loading.
     */
    protected FileOpenMode fileOpenMode;

    AbstractLoaderHDF() {
    	Frame frame= STATUS.getFrame();    	
    	hdfio = new HDFIO(frame, msghdlr);    	
    }    

	/**
	 * Read in an HDF file.
	 * 
	 * @param file a file reference or null
	 * @param loadGroup 
	 */ 
	protected final void loadHDFFile(File file, Group loadGroup) {		
		this.loadGroup=loadGroup;			
		final boolean isFileReading;		

		if (file==null) {//No file given				
	        final JFileChooser jfile = new JFileChooser(HDFIO.getLastValidFile());
	        jfile.setFileFilter(new HDFileFilter(true));
	        final int option = jfile.showOpenDialog(STATUS.getFrame());
	        /* Don't do anything if it was cancel. */
	        if (option == JFileChooser.APPROVE_OPTION
	                && jfile.getSelectedFile() != null) {
	        	final File selectedFile = jfile.getSelectedFile();
	    		hdfio.setListener(this);
	    		isFileReading=hdfio.readFile(fileOpenMode, selectedFile, loadGroup);	        	
	        } else{
	        	isFileReading=false;
	        }	        	
		} else {
			isFileReading=hdfio.readFile(fileOpenMode, file,  loadGroup);
		}		
		/* File was not read in so no call back do notify here */
		//FIXME add statements inside if clause:
//		if (isFileReading){	
//		}
	}
	
	protected final void executeParse(String[] cmdTokens) {
		//LATER KBS needs to be implemented
	    //execute(null);	//has unhandled exception
	}	
	/**
	 * Called by HDFIO when asynchronized IO is completed  
	 */
	public void completedIO(String message, String errorMessage) {
		hdfio.removeListener();
		Histogram firstHist=null;
		/*
         * Set to sort group. Set the current histogram to the first opened
         * histogram.
         */
		if (loadGroup.getHistogramList().size()>0 ) {
			Group group =STATUS.getCurrentGroup();
			if (group!=null) {
				List histList =group.getHistogramList();
				if (histList.size()>0)
					firstHist = (Histogram)group.getHistogramList().get(0);
			}
		}					
		STATUS.setCurrentHistogram(firstHist);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, firstHist);
	}
	
}
