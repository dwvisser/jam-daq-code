package jam.commands;


import jam.global.CommandListenerException;
import jam.global.SortMode;
import jam.io.hdf.HDFIO;
import jam.io.hdf.HDFileFilter;
import jam.data.Group;

import java.awt.Frame;
import java.io.File;
import java.util.*;

import javax.swing.JFileChooser;

/**
 * Command to save the sort group of histograms.
 * 
 * @author Ken Swartz
 *
 */
public class SaveSortGroupHDFCmd extends AbstractCommand {

	public void initCommand() {
		putValue(NAME, "Save sort group as\u2026");
	}
	
	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) throws CommandException {
		
		Frame frame =status.getFrame();
		final HDFIO hdfio = new HDFIO(frame, msghdlr);
		SortMode mode =status.getSortMode();
		if (mode == SortMode.ONLINE_DISK ||
			mode == SortMode.ON_NO_DISK ||
		  	mode == SortMode.OFFLINE ) {

			//Find sort group
			Group sortGroup=Group.getSortGroup();
			
			/*
			List groupList=Group.getGroupList();
			Iterator iter =groupList.iterator();
			while (iter.hasNext()) {
				Group sortGroup=(Group)iter.next();
				if (group.getType() == Group.Type.SORT) {
					sortGroup=group;
					break;
				}
			}
			*/
			if (sortGroup!=null) {
				if (cmdParams == null || cmdParams.length==0) { //No file given		
			        final JFileChooser jfile = new JFileChooser(HDFIO.getLastValidFile());
			        jfile.setFileFilter(new HDFileFilter(true));
			        int option = jfile.showSaveDialog(frame);
			        /* don't do anything if it was cancel */
			        if (option == JFileChooser.APPROVE_OPTION
			                && jfile.getSelectedFile() != null) {
			            final File file = jfile.getSelectedFile();
			            
			            hdfio.writeFile(file, sortGroup.getHistogramList());
			        }
				}else {
					File file=(File)cmdParams[0];
					hdfio.writeFile(file, sortGroup.getHistogramList());
				}
			}
		//No sort group
		} else {
			throw new CommandException("Need to be in Sort mode to save sort group.");
		}
		
		

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
			throws CommandListenerException {
		// TODO Auto-generated method stub

	}

}
