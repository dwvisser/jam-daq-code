package jam.commands;

import jam.data.Gate;
import jam.data.Histogram;
import jam.data.control.GateAdd;
import jam.global.BroadcastEvent;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Show the scalers dialog box
 * 
 * @author Ken Swartz
 *
 */
final class ShowDialogAddGate extends AbstractShowDialog implements
Observer {

	private final List gateList=Gate.getGateList();
	
	public void initCommand(){
		putValue(NAME,"Add\u2026");
		dialog=new GateAdd();
	}

	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if ( (command==BroadcastEvent.Command.GROUP_SELECT) || 
			 (command==BroadcastEvent.Command.ROOT_SELECT) ) {
			setEnabled(false);
		} else if ( (command==BroadcastEvent.Command.HISTOGRAM_SELECT) || 
				    (command==BroadcastEvent.Command.GATE_SELECT) ) {
			Histogram hist =(Histogram)STATUS.getCurrentHistogram();
			setEnabled(!gateList.isEmpty() && hist!=null);
		} 
	}	
}
