package jam.commands;

import jam.data.Group;
import jam.data.Histogram;
import jam.data.control.AbstractControl;
import jam.global.BroadcastEvent;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

/**
 * Open a hdf file
 *  
 * @author Ken Swartz
 *
 */
final class OpenHDFCmd extends AbstractCommand implements Observer {
	
	OpenHDFCmd(){
		putValue(NAME,"Open\u2026");
		putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(KeyEvent.VK_O, CTRL_MASK));
	}

	/* 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(final Object[] cmdParams) {
		//Run in another thread
		final Runnable r=new Runnable(){
			public void run(){
				readHDFFile(cmdParams);
			}
		};
		final Thread t=new Thread(r);
		t.run();
	}
	/**
	 * Read in a HDF file
	 * @param cmdParams
	 */ 
	private void readHDFFile(Object[] cmdParams) {
		Frame frame= STATUS.getFrame();
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
				isFileRead=hdfio.readFile(FileOpenMode.OPEN, file);	        	
	        } else {
	        	isFileRead=false;
	        }	
		} else {
			isFileRead=hdfio.readFile(FileOpenMode.OPEN, file);
		}
		if (isFileRead){//File was read in	
			//snotifyApp(HDFIO.getLastValidFile());
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
		
		Histogram firstHist;
		
		STATUS.setSortMode(file);
		AbstractControl.setupAll();
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		
		//Set the current histogram to the first opened histogram
		if (Group.getCurrentGroup().getHistogramList().size()>0 ) {
			firstHist = (Histogram)Group.getCurrentGroup().getHistogramList().get(0);
		}else{
			firstHist=null;
		}
			
		STATUS.setCurrentHistogram(firstHist);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, firstHist);
		STATUS.getFrame().repaint();
	}			
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if (command==BroadcastEvent.Command.SORT_MODE_CHANGED){
			enable();
		}
	}
	
	private void enable(){
		final SortMode mode=STATUS.getSortMode();
		setEnabled(mode==SortMode.FILE || mode==SortMode.NO_SORT);		
	}
}
