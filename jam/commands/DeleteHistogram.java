package jam.commands;

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
		final String name=status.getCurrentHistogramName();
		if (JOptionPane.YES_OPTION==JOptionPane.showConfirmDialog(frame,
		"Delete "+name.trim()+"?","Delete histogram",JOptionPane.YES_NO_OPTION)){
			Histogram.deleteHistogram(name);
			broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
		}
		
	}
	
	/** 
	 * Execute command
	 * @see jam.commands.AbstractCommand#executeStrParam(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens) {
		execute(null);		
	}
	
	public void update(Observable observe, Object obj){
		enable();
	}
	
	private final List histogramList=Histogram.getHistogramList();

	protected final void enable() {
		final SortMode mode=status.getSortMode();
		setEnabled((!histogramList.isEmpty()) && 
		(mode==SortMode.FILE || mode==SortMode.NO_SORT));
	}

}
