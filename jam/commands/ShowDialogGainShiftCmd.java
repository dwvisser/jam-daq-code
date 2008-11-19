package jam.commands;

import jam.data.control.GainShift;

import com.google.inject.Inject;

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
