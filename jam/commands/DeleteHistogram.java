package jam.commands;

import jam.data.Group;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.SortMode;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
 *  Command for file menu new also clears
 * 
 * @author Ken Swartz
 *
 */
final class DeleteHistogram extends AbstractCommand implements Observer {
	
	DeleteHistogram(){
		super();
		putValue(NAME,"Delete\u2026");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, 
		CTRL_MASK));
		enable();
	}

	/**
	 * Excecute command
	 * 
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		final JFrame frame =status.getFrame();
		final Histogram hist=status.getCurrentHistogram();
		String name =hist.getFullName();
		Group group=hist.getGroup();	
		//Cannot delete sort histograms
		if (group.getType()== Group.Type.SORT) {
			msghdlr.errorOutln("Cannot delete '"+name.trim()+"', it is sort histogram.");
		} else {
			if (JOptionPane.YES_OPTION==JOptionPane.showConfirmDialog(frame,
					"Delete "+name.trim()+"?","Delete histogram",JOptionPane.YES_NO_OPTION)){
				Histogram.deleteHistogram(hist.getUniqueFullName());
				broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
			}
		}
		
	}
	
	protected void executeParse(String[] cmdTokens) {
		execute(null);		
	}
	
	public void update(Observable observe, Object obj){
		enable();
	}
	
	private final List histogramList=Histogram.getHistogramList();

	private final void enable() {
		final SortMode mode=status.getSortMode();
	}

}
