package jam.commands;

import jam.sort.control.ConfigurationDisplay;

import com.google.inject.Inject;

/**
 * Show the dialog which displays configuration parameters.
 * 
 * @author Dale Visser
 * 
 */
public class ShowDialogConfiguration extends AbstractShowDialog {

	@Inject
	ShowDialogConfiguration(final ConfigurationDisplay configDisplay) {
		super("View Configuration\u2026");
		dialog = configDisplay;
	}
}
