package jam.commands;

import jam.data.control.GroupRename;

import com.google.inject.Inject;

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
