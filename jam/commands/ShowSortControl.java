/*
 * Created on Jun 4, 2004
 */
package jam.commands;

import jam.SortControl;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
final class ShowSortControl extends AbstractShowDialog implements Observer {
	
	public void initCommand(){
		putValue(NAME, "Sort\u2026");
		dialog=SortControl.getInstance();
		enable();
	}

	protected final void enable() {
		final SortMode mode=status.getSortMode();
		setEnabled(mode == SortMode.OFFLINE);
	}

	public void update(Observable observe, Object obj){
		enable();
	}
	

}
