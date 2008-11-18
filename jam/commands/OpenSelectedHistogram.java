/*
 * Created on Jun 3, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.commands;

import injection.GuiceInjector;
import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;

import javax.swing.JFrame;

/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Jun 3, 2004
 */
final class OpenSelectedHistogram extends AbstractCommand {

	OpenSelectedHistogram() {
		super("Open Additional Select Histograms\u2026");
	}

	/**
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	@Override
	protected void execute(final Object[] cmdParams) {
		final JFrame frame = GuiceInjector.getFrame();
		final jam.io.control.OpenSelectedHistogram osh = new jam.io.control.OpenSelectedHistogram(
				frame);
		osh.open();
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
		frame.repaint();
	}

	/**
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	@Override
	protected void executeParse(final String[] cmdTokens)
			throws CommandListenerException {
		execute(null);
	}
}
