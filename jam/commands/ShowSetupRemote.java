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
final class ShowSetupRemote extends AbstractShowDialog implements Observer {
	
	ShowSetupRemote(){
		super("Observe Remote\u2026");
		dialog=new SetupRemote();
		enable();
	}

	private void enable() {
		setEnabled(false);
	}

	public void update(final Observable observe, final Object obj){
		enable();
	}
	

}
