package jam.commands;

import jam.sort.control.ConfigurationDisplay;

/**
 * Show the dialog which displays configuration parameters.
 * 
 * @author Dale Visser
 * 
 */
public class ShowDialogConfiguration extends AbstractShowDialog {

	ShowDialogConfiguration() {
		super("View Configuration\u2026");
		dialog = new ConfigurationDisplay();
	}
}
