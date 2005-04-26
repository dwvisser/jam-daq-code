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

		private String KEY_PLOT="plot";
		
		private String KEY_TABLE="table";
		
		JPanel plotDisplay;
		
		JPanel summaryTable;
	
		private final CardLayout displaySwapPanelLayout;
		
		private transient final Broadcaster broadcaster = Broadcaster.getSingletonInstance();		
		
		public Display(JPanel plotDisplay, JPanel summaryTable) {

			displaySwapPanelLayout = new CardLayout();
			this.setLayout(displaySwapPanelLayout);
			this.add(KEY_PLOT, plotDisplay);

			this.add(KEY_TABLE, summaryTable);
			// Initial show plot
			displaySwapPanelLayout.show(this, KEY_PLOT);
			
			broadcaster.addObserver(this);
			
		}
		
		public void showPlot() {
			displaySwapPanelLayout.show(this, KEY_PLOT);			
		}
		
		public void showTable() {
			displaySwapPanelLayout.show(this, KEY_TABLE);
		}
		public void update(Observable observable, Object o) {
			BroadcastEvent be = (BroadcastEvent) o;
			if	(be.getCommand() == BroadcastEvent.Command.GROUP_SELECT) {
				showTable();
			}else if ( (be.getCommand() == BroadcastEvent.Command.HISTOGRAM_SELECT)||
					   (be.getCommand() == BroadcastEvent.Command.GATE_SELECT) ){
				showPlot();
			}
			
		}
		
}
