package jam.commands;

import jam.global.CommandListenerException;
import jam.io.control.SaveSelectedHistogram;

/**
 * Save selected histograms to a file
 * 
 * @author Ken Swartz
 *
 */
final class SaveSelectHistogramsHDFCmd extends AbstractCommand {
	
	private  SaveSelectedHistogram saveSelectedDialog;
	
	public void initCommand() {
		putValue(NAME, "Save select histograms\u2026");
		saveSelectedDialog = new SaveSelectedHistogram(status.getFrame(), msghdlr);
	}
	
	/**
	 * Show dialog to select histogram
	 */
	protected void execute(Object[] cmdParams){
		saveSelectedDialog.show();

	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
			throws CommandListenerException {
	    execute(null);
	}

}
