/*
 * Created on Jun 4, 2004
 */
package jam.commands;

import jam.SetupSortOn;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
final class ShowSetupOnline extends AbstractShowDialog implements Observer {
	
	public void initCommand(){
		putValue(NAME, "Online sorting\u2026");
		dialog=SetupSortOn.getInstance().getDialog();
		enable();
	}

	private void enable() {
		final SortMode mode=status.getSortMode();
		setEnabled(!(mode == SortMode.OFFLINE || 
		mode == SortMode.REMOTE));
	}

	public void update(Observable observe, Object obj){
		enable();
	}
	

}
