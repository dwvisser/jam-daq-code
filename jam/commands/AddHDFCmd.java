package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;
import jam.global.SortMode;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
/**
 * Add counts from a histogram
 * 
 * @author Ken Swartz
 */
final class AddHDFCmd extends AbstractCommand implements Observer {

	AddHDFCmd(){
		putValue(NAME,"Add counts...");
	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		JFrame frame = status.getFrame();				
		final HDFIO	hdfio = new HDFIO(frame, msghdlr);
		hdfio.readFile(FileOpenMode.ADD);

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
			execute(null);
	}
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final int command=be.getCommand();
		if (command==BroadcastEvent.SORT_MODE_CHANGED){
			final SortMode mode=status.getSortMode();
			setEnabled(mode != SortMode.REMOTE);
		}
	}

}
