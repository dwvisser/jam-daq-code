package jam.commands;

import com.google.inject.Inject;

import jam.PeakFindDialog;

/**
 * Command to add fit
 * 
 * @author Ken Swartz
 * 
 */

final class ShowDialogPeakFind extends AbstractShowDialog {

	@Inject
	ShowDialogPeakFind(final PeakFindDialog peakFind) {
		super("Peak Find Properties\u2026");
		dialog = peakFind;
	}

}
