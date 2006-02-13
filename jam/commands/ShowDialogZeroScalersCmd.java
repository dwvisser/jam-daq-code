package jam.commands;

import jam.data.Scaler;
import jam.data.control.ScalerZero;

import java.util.Observable;
import java.util.Observer;

/**
 * Show the zero scalers dialog.
 * 
 * @author Ken Swartz
 *
 */
final class ShowDialogZeroScalersCmd extends AbstractShowDialog 
implements Observer {
	
	ShowDialogZeroScalersCmd(){
		super("Zero Scalers\u2026");
		dialog=new ScalerZero();
	}
	
	public void update(final Observable observe, final Object obj){
		setEnabled(!Scaler.getScalerList().isEmpty());
	}	

}
