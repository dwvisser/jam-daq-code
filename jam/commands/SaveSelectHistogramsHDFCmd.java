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

	SaveSelectHistogramsHDFCmd() {
		super("Save select histograms\u2026");
	}

	/**
	 * Show dialog to select histogram
	 */
	protected void execute(final Object[] cmdParams) {
		final SaveSelectedHistogram saveDlg = new SaveSelectedHistogram(STATUS
				.getFrame());
		saveDlg.show();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		execute(null);
	}

}
