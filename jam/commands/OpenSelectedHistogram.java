/*
 * Created on Jun 3, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.commands;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.CommandListenerException;

import java.awt.Frame;

import com.google.inject.Inject;

/**
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version June 3, 2004
 */
final class OpenSelectedHistogram extends AbstractCommand {

	private transient final Frame frame;
	private transient final jam.io.control.OpenSelectedHistogram osh;
	private transient final Broadcaster broadcaster;

	@Inject
	OpenSelectedHistogram(final Frame frame,
			final jam.io.control.OpenSelectedHistogram osh,
			final Broadcaster broadcaster) {
		super("Open Additional Select Histograms\u2026");
		this.frame = frame;
		this.osh = osh;
		this.broadcaster = broadcaster;
	}

	/**
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	@Override
	protected void execute(final Object[] cmdParams) {
		osh.open();
		this.broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
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
