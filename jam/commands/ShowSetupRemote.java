/*
 * Created on Jun 4, 2004
 */
package jam.commands;

import jam.SetupRemote;

import java.util.Observable;
import java.util.Observer;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
public class ShowSetupRemote extends AbstractShowDialog implements Observer {
	
	public void initCommand(){
		putValue(NAME, "Observe Remote\u2026");
		dialog=new SetupRemote();
		enable();
	}

	protected final void enable() {
		setEnabled(false);
	}

	public void update(Observable observe, Object obj){
		enable();
	}
	

}
