package jam.commands;

import jam.data.Scaler;
import jam.data.control.ScalerZero;

import java.util.List;
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
	
	private final List scalerList=Scaler.getScalerList();
	
	protected void initCommand(){
		putValue(NAME,"Zero Scalers\u2026");
		dialog=new ScalerZero();
	}
	
	public void update(Observable observe, Object obj){
		setEnabled(!scalerList.isEmpty());
	}	

}
