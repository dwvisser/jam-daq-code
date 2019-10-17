package jam.commands;

import com.google.inject.Inject;
import jam.global.CommandListenerException;
import jam.io.control.SaveSelectedHistogram;

/**
 * Save selected histograms to a file
 * 
 * @author Ken Swartz
 * 
 */
final class SaveSelectHistogramsHDFCmd extends AbstractCommand {

	private transient final SaveSelectedHistogram saveDlg;

	@Inject
	SaveSelectHistogramsHDFCmd(final SaveSelectedHistogram saveDlg) {
		super("Save select histograms\u2026");
		this.saveDlg = saveDlg;
	}

	/**
	 * Show dialog to select histogram
	 */
	@Override
	protected void execute(final Object[] cmdParams) {
		saveDlg.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	@Override
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		execute(null);
	}

}
