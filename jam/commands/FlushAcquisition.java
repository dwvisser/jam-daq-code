/*
 * Created on Jun 7, 2004
 */
package jam.commands;

import jam.RunControl;
import jam.RunState;
import jam.global.BroadcastEvent;
import jam.global.SortMode;

import java.util.Observable;
import java.util.Observer;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 7, 2004
 */
final class FlushAcquisition extends AbstractCommand implements Observer {

	private RunControl control;

	protected void initCommand(){
		putValue(NAME, "flush");
		putValue(SHORT_DESCRIPTION, "Flush the current data acquisition buffer.");
		control=RunControl.getSingletonInstance();
		setEnabled(false);
	}
	
	/**
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		control.flushAcq();
	}

	/**
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) {
		execute(null);
	}

	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		final BroadcastEvent be=(BroadcastEvent)arg;
		final int command=be.getCommand();
		boolean enable = online();
		if (command==BroadcastEvent.RUN_STATE_CHANGED){
			final RunState rs=(RunState)be.getContent();
			enable &= rs.isAcqOn();
		}
		setEnabled(enable);
	}

	protected final boolean online() {
		final SortMode mode=status.getSortMode();
		return mode == SortMode.ONLINE_DISK || 
		mode == SortMode.ONLINE_NO_DISK;
	}
}
