package jam.commands;

import jam.PeakFindDialog;

import com.google.inject.Inject;

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
