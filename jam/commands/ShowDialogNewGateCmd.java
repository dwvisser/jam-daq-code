package jam.commands;

import jam.data.Histogram;
import jam.data.control.GateNew;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
/**
 * Show the new gate dialog
 */
public class ShowDialogNewGateCmd extends AbstractShowDialog implements
Observer {

	private final List histogramList=Histogram.getHistogramList();

	public void initCommand(){
		putValue(NAME, "New\u2026");
		/* Super class member next line */
		dialog= new GateNew(msghdlr);		
	}
	
	public void update(Observable observe, Object obj){
		setEnabled(!histogramList.isEmpty());
	}	
}
