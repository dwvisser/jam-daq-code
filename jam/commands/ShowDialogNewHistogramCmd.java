package jam.commands;

import jam.data.control.HistogramNew;

import com.google.inject.Inject;

/**
 * Show the histogram dialog
 * 
 * @author Ken
 * 
 */
final class ShowDialogNewHistogramCmd extends AbstractShowDialog {

	/**
	 * Initialize command
	 */
	@Inject
	ShowDialogNewHistogramCmd(final HistogramNew histogramNew) {
		super("New\u2026");
		dialog = histogramNew;
	}
}
