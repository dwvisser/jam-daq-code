package jam.commands;

import com.google.inject.Inject;
import jam.global.CommandListenerException;
import jam.global.JamProperties;
import jam.global.PropertyKeys;
import jam.ui.ExtensionFileFilter;
import jam.util.YaleCAENgetScalers;

import javax.swing.*;
import java.io.File;

/**
 * Open a file with YaleCAEN scalers.
 * 
 * @author Ken Swartz
 */
final class OpenScalersYaleCAEN extends AbstractCommand {

	private transient final YaleCAENgetScalers ycs;

	@Inject
	OpenScalersYaleCAEN(final YaleCAENgetScalers ycs) {
		super("Display scalers from YaleCAEN event file\u2026");
		this.ycs = ycs;
	}

	@Override
	protected void execute(final Object[] cmdParams) {
		final File file;
		if (cmdParams == null) {
			file = getFile();
		} else {
			file = (File) cmdParams[0];
		}

		if (file != null) {
			ycs.processEventFile(file);
		}
	}

	@Override
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		execute(null);
	}

	private transient File lastFile = new File(JamProperties// NOPMD
			.getPropString(PropertyKeys.EVENT_INPATH));

	/**
	 * Get a *.evn file from a JFileChooser.
	 * 
	 * @return a <code>File</code> chosen by the user, null if dialog cancelled
	 */
	private File getFile() {
		File file = null;
		int option;
		final JFileChooser jfile = new JFileChooser(lastFile);
		jfile.setDialogTitle("Select an Event File");
		jfile.setFileFilter(new ExtensionFileFilter("evn"));
		option = jfile.showOpenDialog(null);
		/* don't do anything if it was cancel */
		if (option == JFileChooser.APPROVE_OPTION) {
			file = jfile.getSelectedFile();
			lastFile = file;
		}
		return file;
	}
}
