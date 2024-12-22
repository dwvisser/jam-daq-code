package jam.commands;

import com.google.inject.Inject;

import jam.data.control.HistogramNew;

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
