package jam.commands;

import jam.data.Group;
import jam.global.CommandListenerException;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.io.File;
import java.awt.Frame;
import javax.swing.JFileChooser;


/**
 *  * Command to save a group of histograms.
 * 
 * @author Ken Swartz
 *
 */
public class SaveGroupHDFCmd extends AbstractCommand {

	public void initCommand() {
		putValue(NAME, "Save group as\u2026");
	}
	
	protected void execute(Object[] cmdParams) throws CommandException {
		
		Frame frame =status.getFrame();
		final HDFIO hdfio = new HDFIO(frame, msghdlr);
		
		Group group=status.getCurrentGroup();
		
		if (group!=null) {
			if (cmdParams == null || cmdParams.length==0) { //No file given		
		        final JFileChooser jfile = new JFileChooser(HDFIO.getLastValidFile());
		        jfile.setFileFilter(new HDFileFilter(true));
		        int option = jfile.showSaveDialog(frame);
		        /* don't do anything if it was cancel */
		        if (option == JFileChooser.APPROVE_OPTION
		                && jfile.getSelectedFile() != null) {
		            final File file = jfile.getSelectedFile();
		            //FIMXE need to create a new call
		            hdfio.writeFile(true, true, true, true, file);
		        }
			}else {
				//hdfio.write
			}
		} else {
			throw new CommandException("Need to select a group");
		}
			

	}

	protected void executeParse(String[] cmdTokens)
			throws CommandListenerException {
		// TODO Auto-generated method stub

	}

}
