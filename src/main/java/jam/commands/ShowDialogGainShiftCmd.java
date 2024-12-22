package jam.commands;

import com.google.inject.Inject;

import jam.data.control.GainShift;

/**
 * Show the gain shift dialog
 * 
 * @author Ken
 */

final class ShowDialogGainShiftCmd extends AbstractShowDialog {

	@Inject
	ShowDialogGainShiftCmd(final GainShift gainShift) {
		super("Gain Shift\u2026");
		dialog = gainShift;
	}
}
