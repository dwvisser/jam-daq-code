/*
 * Created on Jun 3, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 3, 2004
 */
final class OpenSelectedHistogram extends AbstractCommand {
	
	private jam.io.control.OpenSelectedHistogram osh;
	
	public void initCommand(){
		putValue(NAME, "Open Additional Select Histograms\u2026");
		osh=new jam.io.control.OpenSelectedHistogram(STATUS.getFrame(), msghdlr);
	}

	/**
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) {
		osh.open();
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		STATUS.getFrame().repaint();
	}

	/**
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
		throws CommandListenerException {
		execute(null);
	}
}
