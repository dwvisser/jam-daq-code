package jam.commands;

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
abstract class LoaderHDF extends AbstractCommand implements Observer {
    
    /**
     * Mode under which to do the loading.
     */
    protected FileOpenMode fileOpenMode;

	protected final void execute(final Object[] cmdParams) {
		final Runnable r=new Runnable(){
			public void run(){
				addHDFFile(cmdParams); 			 	
			}
		};
		final Thread t=new Thread(r);
		t.run();
	}

	/**
	 * Read in an HDF file.
	 * 
	 * @param cmdParams a file reference or null
	 */ 
	protected final void addHDFFile(final Object[] cmdParams) {		
		Frame frame= status.getFrame();
		final HDFIO	hdfio = new HDFIO(frame, msghdlr);		
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
}
