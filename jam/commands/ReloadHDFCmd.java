package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.awt.Event;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
/**
 *  Reload data from a hdf file
 * 
 * @author Ken Swartz
 *
 */
final class ReloadHDFCmd extends AbstractCommand implements Observer {
	
	ReloadHDFCmd(){
		putValue(NAME,"Reload\u2026");
		putValue(ACCELERATOR_KEY,KeyStroke.getKeyStroke(
		KeyEvent.VK_O,
		CTRL_MASK | Event.SHIFT_MASK));
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(final Object[] cmdParams) {			
		
		final Runnable r=new Runnable(){
			public void run(){
				addHDFFile(cmdParams); 						
			}
		};
		
		final Thread t=new Thread(r);
		t.run();
	}
	/**
	 * Read in a HDF file
	 * @param cmdParams
	 */ 
	private void addHDFFile(final Object[] cmdParams) {
		
		Frame frame= status.getFrame();
		final HDFIO	hdfio = new HDFIO(frame, msghdlr);				
		File file=null;
		final boolean isFileRead;
		
		if (cmdParams!=null) {
			file =(File)cmdParams[0];
		} 
	
		if (file==null) {//No file given				
	        boolean outF = false; //default if not set to true later
	        final JFileChooser jfile = new JFileChooser(HDFIO.getLastValidFile());
	        jfile.setFileFilter(new HDFileFilter(true));
	        final int option = jfile.showOpenDialog(frame);
	        // dont do anything if it was cancel
	        if (option == JFileChooser.APPROVE_OPTION
	                && jfile.getSelectedFile() != null) {
	        	file = jfile.getSelectedFile();
				isFileRead=hdfio.readFile(FileOpenMode.ADD, file);	        	
	        } else {
	        	isFileRead=false;
	        }
	        	
		} else {
			isFileRead=hdfio.readFile(FileOpenMode.ADD, file);
		}		
	}
	/**
	 * Read in a HDF file
	 * @param cmdParams
	 */ 
	private void reloadHDFFile(final Object[] cmdParams) {
		
		Frame frame= status.getFrame();
		File file=null;
		
		if (cmdParams!=null) {
			file =(File)cmdParams[0];
		} 
	
		final boolean isFileRead; 
		final HDFIO	hdfio = new HDFIO(status.getFrame(), msghdlr);		
		if (file==null) {//No file given				
	        boolean outF = false; //default if not set to true later
	        final JFileChooser jfile = new JFileChooser(HDFIO.getLastValidFile());
	        jfile.setFileFilter(new HDFileFilter(true));
	        final int option = jfile.showOpenDialog(frame);
	        // dont do anything if it was cancel
	        if (option == JFileChooser.APPROVE_OPTION
	                && jfile.getSelectedFile() != null) {
	        	file = jfile.getSelectedFile();
				isFileRead=hdfio.readFile(FileOpenMode.ADD, file);	        	
	        } else {
	        	isFileRead=false;
	        }
	        	
		} else {
			isFileRead=hdfio.readFile(FileOpenMode.ADD, file);
		}		
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
			execute(null);
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
		final boolean online = mode == SortMode.ONLINE_DISK || 
		mode == SortMode.ON_NO_DISK;
		final boolean offline = mode == SortMode.OFFLINE;
		final boolean sorting = online || offline;
		setEnabled(sorting);
	}
}
