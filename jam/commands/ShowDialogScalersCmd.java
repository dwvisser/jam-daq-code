package jam.commands;

import jam.data.Scaler;
import jam.data.control.ScalerDisplay;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Show the scalers dialog box
 * 
 * @author Ken Swartz
 *
 */
final class ShowDialogScalersCmd extends AbstractShowDialog implements
Observer {

	private final List scalerList=Scaler.getScalerList();
	
	protected void initCommand(){
		putValue(NAME,"Display Scalers\u2026");
		dialog=new ScalerDisplay();
	}

	public void update(Observable observe, Object obj){
		setEnabled(!scalerList.isEmpty());
	}	
}
