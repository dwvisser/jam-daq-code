package jam.commands;

import jam.data.Gate;
import jam.data.control.GateSet;

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
	
	protected void initCommand(){
		putValue(NAME,"Set Gate\u2026");
		dialog=new GateSet(msghdlr);
	}

	public void update(Observable observe, Object obj){
		setEnabled(!gateList.isEmpty());
	}	
}
