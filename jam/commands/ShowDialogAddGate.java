package jam.commands;

import jam.data.Gate;
import jam.data.control.GateAdd;

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
	
	protected void initCommand(){
		putValue(NAME,"Add Gate\u2026");
		dialog=new GateAdd(msghdlr);
	}

	public void update(Observable observe, Object obj){
		setEnabled(!gateList.isEmpty());
	}	
}
