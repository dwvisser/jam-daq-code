package jam.util;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

/**
 * Set repaint manager to this to check whether Swing and event threads are
 * being used properly.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 */
public final class ThreadCheckingRepaintManager extends RepaintManager {

	public void addInvalidComponent(final JComponent jComponent) {
		synchronized (this) {
			checkThread(jComponent);
			super.addInvalidComponent(jComponent);
		}
	}

	private void checkThread(final JComponent component) {
		if (!SwingUtilities.isEventDispatchThread() && component.isShowing()) {
			System.err.println("Wrong Thread");
			Thread.dumpStack();
		}
	}

	public void addDirtyRegion(final JComponent component, final int xcoord,
			final int ycoord, final int width, final int height) {
		synchronized (this) {
			checkThread(component);
			super.addDirtyRegion(component, xcoord, ycoord, width, height);
		}
	}
}
