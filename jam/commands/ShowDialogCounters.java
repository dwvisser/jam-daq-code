package jam.commands;

import jam.DisplayCounters;
import jam.global.BroadcastEvent;
import jam.global.SortMode;

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
	public void initCommand(){
		putValue(NAME, "Buffer Counters\u2026");
		dialog=DisplayCounters.getSingletonInstance();
	}
		
	public void update(Observable observe, Object obj){
		final BroadcastEvent event=(BroadcastEvent)obj;
		if (event.getCommand()==BroadcastEvent.SORT_MODE_CHANGED){
			final SortMode mode=status.getSortMode();
			setEnabled(mode == SortMode.ONLINE_DISK || mode == SortMode.ONLINE_NO_DISK
			|| mode==SortMode.OFFLINE);
		}
	}	
}
