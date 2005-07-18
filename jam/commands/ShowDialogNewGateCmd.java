package jam.commands;

import jam.data.Histogram;
import jam.data.control.GateNew;
import jam.global.BroadcastEvent;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
/**
 * Show the new gate dialog
 */
final class ShowDialogNewGateCmd extends AbstractShowDialog implements
Observer {

	private final List histogramList=Histogram.getHistogramList();

	public void initCommand(){
		putValue(NAME, "New\u2026");
		/* Super class member next line */
		dialog= new GateNew(msghdlr);		
	}
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if ( (command==BroadcastEvent.Command.GROUP_SELECT) || 
			 (command==BroadcastEvent.Command.ROOT_SELECT) ) {
			setEnabled(false);			
		} else if ( (command==BroadcastEvent.Command.HISTOGRAM_SELECT) || 
				    (command==BroadcastEvent.Command.GATE_SELECT) ) {
			final Histogram hist =(Histogram)STATUS.getCurrentHistogram();
			setEnabled(!histogramList.isEmpty() && hist!=null);
		} 
	}		
}
