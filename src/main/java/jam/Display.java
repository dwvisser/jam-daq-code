package jam;

import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import com.google.inject.Inject;

import jam.global.BroadcastEvent;
import jam.global.BroadcastEvent.Command;
import jam.plot.PlotDisplay;
import jam.plot.View;
import jam.ui.SummaryTable;

/**
 * Display to show plots or table
 * 
 * @author Ken Swartz
 * 
 */
@SuppressWarnings("serial")
public class Display extends JPanel implements PropertyChangeListener {

	private static final String KEY_PLOT = "plot";

	private static final String KEY_TABLE = "table";

	private transient final CardLayout cardLayout;

	/**
	 * Constructor.
	 * 
	 * @param plotDisplay
	 *            plot panel
	 * @param summaryTable
	 *            summary panel
	 */
	@Inject
	public Display(final PlotDisplay plotDisplay,
			final SummaryTable summaryTable) {
		super();
		cardLayout = new CardLayout();
		setLayout(cardLayout);
		add(KEY_PLOT, plotDisplay);
		plotDisplay.setView(View.SINGLE);
		add(KEY_TABLE, summaryTable);
		/* Initial show plot. */
		cardLayout.show(this, KEY_PLOT);
	}

	/**
	 * Show plots in the main panel
	 */
	public void showPlot() {
		cardLayout.show(this, KEY_PLOT);
	}

	/**
	 * Show a table in the main panel
	 */
	public void showTable() {
		cardLayout.show(this, KEY_TABLE);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		final Command command = ((BroadcastEvent) evt).getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			showTable();
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			showPlot();
		}
	}
}
