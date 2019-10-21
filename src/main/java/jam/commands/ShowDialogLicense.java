package jam.commands;

import com.google.inject.Inject;

import jam.Help;

/**
 * Show the license dialog.
 * 
 * @author Ken Swartz
 */
@SuppressWarnings("serial")
final class ShowDialogLicense extends AbstractShowDialog {

	@Inject
	ShowDialogLicense(final Help help) {
		super("License\u2026");
		dialog = help;
	}
}
