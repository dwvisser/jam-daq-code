package jam;

import jam.global.BroadcastEvent;
import jam.global.BroadcastEvent.Command;

import java.awt.CardLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

/**
 * Display to show plots or table
 * 
 * @author Ken Swartz
 * 
 */
public class Display extends JPanel implements Observer {

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
	 * @param broadcaster
	 *            broadcasts state changes we listen for
	 */
	public Display(final JPanel plotDisplay, final JPanel summaryTable) {
		super();
		cardLayout = new CardLayout();
		setLayout(cardLayout);
		add(KEY_PLOT, plotDisplay);
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

	public void update(final Observable observable, final Object object) {
		final BroadcastEvent event = (BroadcastEvent) object;
		final Command command = event.getCommand();
		if ((command == BroadcastEvent.Command.GROUP_SELECT)
				|| (command == BroadcastEvent.Command.ROOT_SELECT)) {
			showTable();
		} else if ((command == BroadcastEvent.Command.HISTOGRAM_SELECT)
				|| (command == BroadcastEvent.Command.GATE_SELECT)) {
			showPlot();
		}
	}
}
