package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.SortMode;
import jam.io.hdf.HDFIO;

import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

/**
 * Save to a hdf file
 *  
 * @author Ken Swartz
 *
 */
final class SaveHDFCmd extends AbstractCommand implements Observer {

	public void initCommand() {
		putValue(NAME, "Save");
		putValue(
			ACCELERATOR_KEY,
			KeyStroke.getKeyStroke(
				KeyEvent.VK_S,
				CTRL_MASK | KeyEvent.SHIFT_MASK));
	    final Icon iSave = loadToolbarIcon("jam/ui/SaveHDF.png");
	    putValue(Action.SMALL_ICON, iSave);
		putValue(Action.SHORT_DESCRIPTION, "Save histograms to a hdf data file.");	    
		
		enable(); //depending on sort mode
	}

	/**
	 * Save to the last file opened.
	 * 
	 * @param cmdParams not used
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		//No command options used
		final JFrame frame = STATUS.getFrame();
		final HDFIO hdfio = new HDFIO(frame, msghdlr);
		final File file = STATUS.getOpenFile();
		if (file != null) {			
			hdfio.writeFile(file, true, true);
		} else { //File null, shouldn't be.	
			throw new IllegalStateException("Expected a reference for the previously accessed file.");
		}
	}
	/**
	 * Save to the last file opened.
	 * 
	 * @param cmdTokens not used
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) {
		execute(null);
	}

	/**
	 * Listens to bradcaster messages to enable/disable this action.
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable observe, Object obj) {
		final BroadcastEvent be = (BroadcastEvent) obj;
		final BroadcastEvent.Command command = be.getCommand();
		if (command == BroadcastEvent.Command.SORT_MODE_CHANGED) {
			enable();
		}
	}

	private void enable() {
		final SortMode mode = STATUS.getSortMode();
		final boolean file = STATUS.getOpenFile() != null;
		setEnabled(file && (mode == SortMode.FILE || mode == SortMode.NO_SORT));
	}
}
