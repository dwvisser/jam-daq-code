package jam.commands;

import jam.data.Histogram;
import jam.data.control.CalibrationDisplay;

import java.util.Observable;
import java.util.Observer;
;
/**
 * @author Ken Swartz
 *
 */
final class ShowDialogCalibrationDisplayCmd
	extends AbstractShowDialog implements Observer {

		public void initCommand(){
			putValue(NAME,"Coefficients\u2026");			
			dialog=new CalibrationDisplay(msghdlr);
			enable();
		}

		protected final void enable() {
			final Histogram h=Histogram.getHistogram(status.getHistName());
			setEnabled(h != null && h.isCalibrated());
		}

		public void update(Observable observe, Object obj){
			enable();
		}
}
