package jam.commands;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;

import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.global.MessageHandler;
import jam.io.ExtensionFileFilter;
import jam.io.ImpExpASCII;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.ui.SummaryTable;

/**
 * Export the summary table 
 * 
 * @author Kennneth Swartz
 *
 */
public class ExportSummaryTableCmd extends AbstractCommand implements Observer {

	protected final static int BUFFER_SIZE = 256 * 2;
	
	private static final String [] EXTS={"dat","txt"};	
	private static final ExtensionFileFilter FILTER=new ExtensionFileFilter(EXTS, 
	"Text file");
	
	//private 
	public void initCommand(){
		putValue(NAME,"Table");		
	}
			
	protected void execute(Object[] cmdParams) throws CommandException {
		File file=null;
		if (cmdParams!=null) {
			if (cmdParams.length>0){
				file =(File)cmdParams[0];
			}
		}
		//No file given
		if (file == null) { 		
			file = chooseFile();
		}
		//No file chosen		
		if (file!= null) { 
			saveTable(file);
		}		
	}	

	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
		// TODO Auto-generated method stub
	}
	
	private void saveTable (File file) {
		
		final SummaryTable summaryTable = STATUS.getTable();
		
    	try {
    		
            if (msghdlr != null) {
            	msghdlr.messageOut("Write out table to " + file, MessageHandler.NEW);
            }
    		
        	//Create writer stream		        	
            final FileOutputStream outStream = new FileOutputStream(file);
            final BufferedOutputStream buffStream = new BufferedOutputStream(
                    outStream, BUFFER_SIZE);
            
            summaryTable.writeTable(buffStream);
            
            buffStream.flush();
            outStream.flush();
            outStream.close();
            
            if (msghdlr != null) {
            	msghdlr.messageOut(" done!", MessageHandler.END);
            }
            
    	} catch (FileNotFoundException fnfe) {
    		msghdlr.errorOutln("Cannot open file: "+file);
    	} catch (IOException ioe) {
    		msghdlr.errorOutln("Writing table to file: "+file);
    	}

	}
	
	private File chooseFile() {
		File file=null;
		final JFileChooser jfile = new JFileChooser(HDFIO.getLastValidFile());

	    jfile.setFileFilter(FILTER);
	    final int option = jfile.showSaveDialog(STATUS.getFrame());
	    /* don't do anything if it was cancel */
	    if (option == JFileChooser.APPROVE_OPTION
	            && jfile.getSelectedFile() != null) {
	    	file =jfile.getSelectedFile();
	    }	     
	    
	    return file;
	}

	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if ( (command==BroadcastEvent.Command.GROUP_SELECT) || 
			 (command==BroadcastEvent.Command.ROOT_SELECT) ) {
			setEnabled(true);
		} else if ( (command==BroadcastEvent.Command.HISTOGRAM_SELECT) || 
				    (command==BroadcastEvent.Command.GATE_SELECT) ) {
			setEnabled(false);
		}
	}

}
