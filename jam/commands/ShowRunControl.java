/*
 * Created on Jun 4, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.commands;

import jam.RunControl;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
public class ShowRunControl extends AbstractShowDialog implements Observer {
	
	protected void initCommand(){
		putValue(NAME, "Run\u2026");
		dialog=RunControl.getSingletonInstance();
		enable();
	}

	protected final void enable() {
		final SortMode mode=status.getSortMode();
		setEnabled(mode == SortMode.ONLINE_DISK || 
		mode == SortMode.ONLINE_NO_DISK);
	}

	public void update(Observable observe, Object obj){
		enable();
	}
	

}
