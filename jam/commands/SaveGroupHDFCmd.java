package jam.commands;

import jam.data.Group;
import jam.global.CommandListenerException;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *  * Command to save a group of histograms.
 * 
 * @author Ken Swartz
 *
 */
public class SaveGroupHDFCmd extends AbstractCommand {

	public void initCommand() {
		putValue(NAME, "Save select group as\u2026");
	}
	
	protected void execute(Object[] cmdParams) throws CommandException {
		File file=null;
		Group group=STATUS.getCurrentGroup();
		if (cmdParams!=null) {
			if (cmdParams.length>0){
				file =(File)cmdParams[0];
			}
			if (cmdParams.length>1){
				group =(Group)cmdParams[1];
			}
		}		
		saveGroup(file, group);		
	}

	private void saveGroup (final File file, Group group) {
		final HDFIO hdfio = new HDFIO(STATUS.getFrame(), msghdlr);
		if (group==null) {
			msghdlr.errorOutln("Need to select a group.");
		} else {
			if (file== null) { //No file given		
		        final JFileChooser jfile = new JFileChooser(HDFIO.getLastValidFile());
		        jfile.setFileFilter(new HDFileFilter(true));
		        final int option = jfile.showSaveDialog(STATUS.getFrame());
		        /* don't do anything if it was cancel */
		        if (option == JFileChooser.APPROVE_OPTION
		                && jfile.getSelectedFile() != null) {
		            hdfio.writeFile(jfile.getSelectedFile(), group);
		        }
			}else {
				hdfio.writeFile(file, group);
			}
		} 
	}
	
	protected void executeParse(String[] cmdTokens)
			throws CommandListenerException {
		// TODO Auto-generated method stub

	}

}
