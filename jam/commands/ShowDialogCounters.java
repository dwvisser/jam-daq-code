package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.QuerySortMode;
import jam.global.SortMode;
import jam.sort.control.DisplayCounters;

import java.util.Observable;
import java.util.Observer;


/**
 * Show parameters dialog.
 * 
 * @author Dale Visser
 *
 */
final class ShowDialogCounters extends AbstractShowDialog 
implements Observer {

	/**
	 * Initialize command
	 */
	ShowDialogCounters(){
		super("Buffer Counters\u2026");
		dialog=DisplayCounters.getSingletonInstance();
	}
		
	public void update(final Observable observe, final Object obj){
		final BroadcastEvent event=(BroadcastEvent)obj;
		if (event.getCommand()==BroadcastEvent.Command.SORT_MODE_CHANGED){
			final QuerySortMode mode=STATUS.getSortMode();
			setEnabled(mode == SortMode.ONLINE_DISK || mode == SortMode.ON_NO_DISK
			|| mode==SortMode.OFFLINE);
		}
	}	
}
