package jam.commands;

import jam.data.control.GroupRename;
/**
 * Show the dialog for new group
 * 
 * @author Ken Swartz
 *
 */
public class ShowDialogRenameGroup extends AbstractShowDialog {
    
	/**
	 * Initialize command
	 */
	public void initCommand(){
		putValue(NAME, "Rename Group\u2026");
		dialog= new GroupRename(); 
	}
}
