package jam.commands;

import jam.data.control.Combine;

import com.google.inject.Inject;

/**
 * Command to show project histogram dialog
 * 
 * @author Ken Swartz
 */
final class ShowDialogHistManipulationsCmd extends AbstractShowDialog {

	@Inject
	ShowDialogHistManipulationsCmd(final Combine combine) {
		super("Combine\u2026");
		dialog = combine;
	}
}
