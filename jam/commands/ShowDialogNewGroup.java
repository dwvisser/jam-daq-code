package jam.commands;

import jam.data.control.GroupNew;

/**
 * Show the dialog for new group
 * 
 * @author Ken Swartz
 *
 */
public class ShowDialogNewGroup extends AbstractShowDialog {
    
	/**
	 * Initialize command
	 */
	public void initCommand(){
		putValue(NAME, "New Group\u2026");
		dialog= new GroupNew(); 
	}
}
