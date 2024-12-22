package jam.commands;

import com.google.inject.Inject;

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
	@Inject
	ShowDialogRenameGroup(final GroupRename groupRename) {
		super("Rename Group\u2026");
		dialog = groupRename;
	}
}
