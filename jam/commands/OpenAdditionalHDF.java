package jam.commands;

import jam.data.control.AbstractControl;
import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.awt.Frame;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;

/**
 * @author ken
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OpenAdditionalHDF extends AbstractCommand implements Observer{

	OpenAdditionalHDF(){
		putValue(NAME,"Open Additional\u2026");
	}

	/* 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(final Object[] cmdParams) {
		final Runnable r=new Runnable(){
			public void run(){
				readAdditionalHDFFile(cmdParams);
			}
		};
		final Thread t=new Thread(r);
		t.run();
	}

	/**
	 * Read in a HDF file
	 * @param cmdParams
	 */ 
	private void readAdditionalHDFFile(Object[] cmdParams) {
		Frame frame= status.getFrame();
		final HDFIO	hdfio = new HDFIO(frame, msghdlr);		
		File file=null;
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
		status.setSortMode(file);
		AbstractControl.setupAll();
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		//Set the current histogram to the first opened histogram
		Histogram firstHist = (Histogram)Group.getCurrentGroup().getHistogramList().get(0);
		status.setCurrentHistogram(firstHist);
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, firstHist);
		status.getFrame().repaint();
	}			
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if (command==BroadcastEvent.Command.SORT_MODE_CHANGED){
			enable();
		}
	}
	
	private void enable(){
		final SortMode mode=status.getSortMode();
		setEnabled(mode==SortMode.FILE || mode==SortMode.NO_SORT);		
	}

}
