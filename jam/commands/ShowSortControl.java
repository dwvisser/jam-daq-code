package jam.commands;

import jam.SortControl;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * Show the sort control dialog.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 2004-06-04
 */
final class ShowSortControl extends AbstractShowDialog implements Observer {
	
	public void initCommand(){
		putValue(NAME, "Sort\u2026");
		dialog=SortControl.getInstance();
		enable();
	}

	private void enable() {
		final SortMode mode=status.getSortMode();
		setEnabled(mode == SortMode.OFFLINE);
	}

	public void update(Observable observe, Object obj){
		enable();
	}
	

}
