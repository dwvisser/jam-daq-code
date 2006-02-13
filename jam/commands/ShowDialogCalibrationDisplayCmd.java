package jam.commands;

import jam.data.AbstractHist1D;
import jam.data.Histogram;
import jam.data.control.CalibrationDisplay;

import java.util.Observable;
import java.util.Observer;

;
/**
 * Shows the histogram calibration dialog.
 * 
 * @author Ken Swartz
 */
final class ShowDialogCalibrationDisplayCmd extends AbstractShowDialog
		implements Observer {

	ShowDialogCalibrationDisplayCmd() {
		super("Coefficients\u2026");
		dialog = new CalibrationDisplay();
		enable();
	}

	private void enable() {
		final Histogram hist = (Histogram) STATUS.getCurrentHistogram();
		AbstractHist1D h1d = null;
		if (hist instanceof AbstractHist1D) {
			h1d = (AbstractHist1D) hist;
		}
		setEnabled(h1d != null && h1d.isCalibrated());
	}

	public void update(final Observable observe, final Object obj) {
		enable();
	}
}
