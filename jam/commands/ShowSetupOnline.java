/*
 * Created on Jun 4, 2004
 */
package jam.commands;

import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.sort.control.SetupSortOn;

import java.util.Observable;
import java.util.Observer;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 4, 2004
 */
final class ShowSetupOnline extends AbstractShowDialog implements Observer {
	
	ShowSetupOnline(){
		super("Online sorting\u2026");
		dialog=SetupSortOn.getInstance().getDialog();
		enable();
	}

	private void enable() {
		final QuerySortMode mode=STATUS.getSortMode();
		setEnabled(!(mode == SortMode.OFFLINE || 
		mode == SortMode.REMOTE));
	}

	public void update(final Observable observe, final Object obj){
		enable();
	}
}
