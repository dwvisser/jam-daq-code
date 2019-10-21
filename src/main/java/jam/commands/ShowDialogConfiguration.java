package jam.commands;

import com.google.inject.Inject;

import jam.sort.control.ConfigurationDisplay;

/**
 * Show the dialog which displays configuration parameters.
 * 
 * @author Dale Visser
 * 
 */
@SuppressWarnings("serial")
public class ShowDialogConfiguration extends AbstractShowDialog {

	@Inject
	ShowDialogConfiguration(final ConfigurationDisplay configDisplay) {
		super("View Configuration\u2026");
		dialog = configDisplay;
	}
}
