package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.awt.Frame;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Add counts to histograms in memory from histograms in an HDF file.
 * 
 * @author Ken Swartz
 */
final class AddHDFCmd extends AbstractCommand implements Observer {

	public void initCommand(){
		putValue(NAME,"Add counts\u2026");
	}

	protected void execute(final Object[] cmdParams) {
		JFrame frame = status.getFrame();				
		new HDFIO(frame, msghdlr);//FIXME is this line needed?
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
				hdfio.readFile(FileOpenMode.ADD, file);	        	
	        } 
		} else {
			hdfio.readFile(FileOpenMode.ADD, file);
		}		
	}
	
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
			execute(null);
	}
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if (command==BroadcastEvent.Command.SORT_MODE_CHANGED){
			final SortMode mode=status.getSortMode();
			setEnabled(mode != SortMode.REMOTE);
		}
	}
}
