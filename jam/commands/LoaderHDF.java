package jam.commands;

import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.awt.Frame;
import java.io.File;
import java.util.Observer;

import javax.swing.JFileChooser;

/**
 * Add counts to histograms in memory from histograms in an HDF file.
 * 
 * @author Ken Swartz
 */
abstract class LoaderHDF extends AbstractCommand implements Observer, HDFIO.AsyncListener {
    
	final HDFIO	hdfio;	
    /**
     * Mode under which to do the loading.
     */
    protected FileOpenMode fileOpenMode;

    LoaderHDF() {
    	Frame frame= STATUS.getFrame();    	
    	hdfio = new HDFIO(frame, msghdlr);    	
    }
    
	protected final void execute(final Object[] cmdParams) {
		addHDFFile(cmdParams);
	}

	/**
	 * Read in an HDF file.
	 * 
	 * @param cmdParams a file reference or null
	 */ 
	protected final void addHDFFile(final Object[] cmdParams) {		
		Frame frame= STATUS.getFrame();	
		hdfio.setListener(this);
		File file=null;
		if (cmdParams!=null) {
			file =(File)cmdParams[0];
		} 	
		if (file==null) {//No file given				
	        final JFileChooser jfile = new JFileChooser(HDFIO.getLastValidFile());
	        jfile.setFileFilter(new HDFileFilter(true));
	        final int option = jfile.showOpenDialog(frame);
	        /* Don't do anything if it was cancel. */
	        if (option == JFileChooser.APPROVE_OPTION
	                && jfile.getSelectedFile() != null) {
	        	file = jfile.getSelectedFile();
				hdfio.readFile(fileOpenMode, file);	        	
	        } 
		} else {
			hdfio.readFile(fileOpenMode, file);
		}		
	}
	
	protected final void executeParse(String[] cmdTokens) {
	    execute(null);
	}	
	/**
	 * Called by HDFIO when asynchronized IO is completed  
	 */
	public void CompletedIO(String message, String errorMessage) {
		hdfio.removeListener();
		
		Histogram firstHist;
		
		//Set to sort group
		//Set the current histogram to the first opened histogram
		if (Group.getCurrentGroup().getHistogramList().size()>0 ) {
			firstHist = (Histogram)Group.getCurrentGroup().getHistogramList().get(0);
		}else{
			firstHist=null;
		}					
		STATUS.setCurrentHistogram(firstHist);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, firstHist);
	}
	
}
