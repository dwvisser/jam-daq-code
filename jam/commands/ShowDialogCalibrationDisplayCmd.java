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
final class ShowDialogCalibrationDisplayCmd
	extends AbstractShowDialog implements Observer {

		public void initCommand(){
			putValue(NAME,"Coefficients\u2026");			
			dialog=new CalibrationDisplay(msghdlr);
			enable();
		}

		private void enable() {
			final Histogram hist= (Histogram)STATUS.getCurrentHistogram();
			final AbstractHist1D h1d=hist !=null && hist instanceof AbstractHist1D ? 
					(AbstractHist1D)hist : null;
			setEnabled(h1d != null && h1d.isCalibrated());
		}

		public void update(Observable observe, Object obj){
			enable();
		}
}
