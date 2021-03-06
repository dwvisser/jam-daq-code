package jam.util;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * Set repaint manager to this to check whether Swing and event threads are
 * being used properly.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser </a>
 */
public final class ThreadCheckingRepaintManager extends RepaintManager {//NOPMD

	private static final Logger LOGGER = Logger
			.getLogger(ThreadCheckingRepaintManager.class.getPackage()
					.getName());

	public void addDirtyRegion(final JComponent component, final int xcoord,
			final int ycoord, final int width, final int height) {
		synchronized (this) {
			checkThread(component);
			super.addDirtyRegion(component, xcoord, ycoord, width, height);
		}
	}

	public void addInvalidComponent(final JComponent jComponent) {
		synchronized (this) {
			checkThread(jComponent);
			super.addInvalidComponent(jComponent);
		}
	}

	private void checkThread(final JComponent component) {
		if (!SwingUtilities.isEventDispatchThread() && component.isShowing()) {
			LOGGER.severe("Wrong Thread");
			Thread.dumpStack();
		}
	}
}
