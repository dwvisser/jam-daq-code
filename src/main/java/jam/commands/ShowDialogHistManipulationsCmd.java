package jam.commands;

import com.google.inject.Inject;

import jam.data.control.Combine;

/**
 * Command to show project histogram dialog
 * 
 * @author Ken Swartz
 */
@SuppressWarnings("serial")
final class ShowDialogHistManipulationsCmd extends AbstractShowDialog {

	@Inject
	ShowDialogHistManipulationsCmd(final Combine combine) {
		super("Combine\u2026");
		dialog = combine;
	}
}
