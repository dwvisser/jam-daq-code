package jam.commands;

import com.google.inject.Inject;
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
	@Inject
	ShowDialogNewGroup(final GroupNew groupNew) {
		super("New Group\u2026");
		dialog = groupNew;
	}
}
