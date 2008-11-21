package jam.commands;

import jam.data.control.GroupNew;

import com.google.inject.Inject;

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
	@Inject
	ShowDialogNewGroup(final GroupNew groupNew) {
		super("New Group\u2026");
		dialog = groupNew;
	}
}
