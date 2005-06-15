package jam;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;

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
public class Display extends JPanel implements Observer{

		private static final String KEY_PLOT="plot";
		
		private static final String KEY_TABLE="table";
		
		private transient final Broadcaster broadcaster = Broadcaster.getSingletonInstance();
		
		private final CardLayout displaySwapPanelLayout;
	
        /**
         * Constructor.
         * 
         * @param plotDisplay plot panel
         * @param summaryTable summary panel
         */
		public Display(JPanel plotDisplay, JPanel summaryTable) {
			displaySwapPanelLayout = new CardLayout();
			setLayout(displaySwapPanelLayout);
			add(KEY_PLOT, plotDisplay);
			add(KEY_TABLE, summaryTable);
			/* Initial show plot. */
			displaySwapPanelLayout.show(this, KEY_PLOT);
			broadcaster.addObserver(this);
		}
        
		/**
		 * Show plots in the main panel
		 */
		public void showPlot() {
			displaySwapPanelLayout.show(this, KEY_PLOT);			
		}
		/**
		 * Show a table in the main panel
		 */		
		public void showTable() {
			displaySwapPanelLayout.show(this, KEY_TABLE);
		}
		public void update(Observable observable, Object o) {
			BroadcastEvent be = (BroadcastEvent) o;
			if	( (be.getCommand() == BroadcastEvent.Command.GROUP_SELECT) ||
			      (be.getCommand() == BroadcastEvent.Command.ROOT_SELECT) ){
				showTable();
			}else if ( (be.getCommand() == BroadcastEvent.Command.HISTOGRAM_SELECT)||
					   (be.getCommand() == BroadcastEvent.Command.GATE_SELECT) ){
				showPlot();
			}
			
		}
		
}
