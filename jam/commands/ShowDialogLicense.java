package jam.commands;

import jam.Help;

import com.google.inject.Inject;

/**
 * Show the license dialog.
 * 
 * @author Ken Swartz
 */
final class ShowDialogLicense extends AbstractShowDialog {

	@Inject
	ShowDialogLicense(final Help help) {
		super("License\u2026");
		dialog = help;
	}
}
