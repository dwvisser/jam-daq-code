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
 * Add counts to histograms in memory from histograms in an HDF file.
 * 
 * @author Ken Swartz
 */
final class AddHDFCmd extends AbstractCommand implements Observer {

	public void initCommand(){
		putValue(NAME,"Add counts\u2026");
	}

	protected void execute(Object[] cmdParams) {
		JFrame frame = status.getFrame();				
		final HDFIO	hdfio = new HDFIO(frame, msghdlr);
		hdfio.readFile(FileOpenMode.ADD);

	}

	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
			execute(null);
	}
	
	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final BroadcastEvent.Command command=be.getCommand();
		if (command==BroadcastEvent.Command.SORT_MODE_CHANGED){
			final SortMode mode=status.getSortMode();
			setEnabled(mode != SortMode.REMOTE);
		}
	}
}
