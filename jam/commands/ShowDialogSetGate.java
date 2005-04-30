package jam.commands;

import jam.data.Gate;
import jam.data.Histogram;
import jam.data.control.GateSet;
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
final class ShowDialogSetGate extends AbstractShowDialog implements
Observer {

	private final List gateList=Gate.getGateList();
	
	public void initCommand(){
		putValue(NAME,"Set\u2026");
		dialog=new GateSet();
	}

	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if ( (command==BroadcastEvent.Command.GROUP_SELECT) || 
			 (command==BroadcastEvent.Command.ROOT_SELECT) ) {
			setEnabled(false);			
		} else if ( (command==BroadcastEvent.Command.HISTOGRAM_SELECT) || 
				    (command==BroadcastEvent.Command.GATE_SELECT) ) {
			Histogram hist = STATUS.getCurrentHistogram();
			setEnabled(!hist.getGates().isEmpty());
		}
	}		
}
