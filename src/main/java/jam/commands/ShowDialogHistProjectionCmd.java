package jam.commands;

import com.google.inject.Inject;

import jam.data.control.Projections;

/**
 * Command to show project histgram dialog
 * 
 * @author Ken Swartz
 */

final class ShowDialogHistProjectionCmd extends AbstractShowDialog {

	@Inject
	ShowDialogHistProjectionCmd(final Projections projections) {
		super("Projections\u2026");
		dialog = projections;
	}

}
