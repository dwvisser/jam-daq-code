/*
 * Created on Jun 4, 2004
 */
package jam.commands;

import jam.SetupSortOff;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
final class ShowSetupOffline extends AbstractShowDialog implements Observer {
	
	public void initCommand(){
		putValue(NAME, "Offline sorting\u2026");
		dialog=SetupSortOff.getSingletonInstance();
		enable();
	}

	protected final void enable() {
		final SortMode mode=status.getSortMode();
		setEnabled(!(mode == SortMode.ONLINE_DISK || 
		mode == SortMode.ONLINE_NO_DISK || 
		mode == SortMode.REMOTE));
	}

	public void update(Observable observe, Object obj){
		enable();
	}
	

}
