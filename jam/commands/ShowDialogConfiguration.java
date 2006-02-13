package jam.commands;

import jam.sort.control.ConfigurationDisplay;

/**
 * Show the dialog which displays configuration parameters.
 * 
 * @author dvk
 * 
 */
public class ShowDialogConfiguration extends AbstractShowDialog {

	ShowDialogConfiguration() {
		super("Configuration\u2026");
		dialog = new ConfigurationDisplay();
		// enable();
	}
}
