package jam.util;

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

public class ThreadCheckingRepaintManager extends RepaintManager {
    public synchronized void addInvalidComponent(JComponent jComponent) {
        checkThread(jComponent);
        super.addInvalidComponent(jComponent);
    }

    private void checkThread(JComponent c) {
        if (!SwingUtilities.isEventDispatchThread() && c.isShowing()) {
            System.out.println("Wrong Thread");
            Thread.dumpStack();
        }
    }

    public synchronized void addDirtyRegion(JComponent jComponent, int i, int i1, int i2, int i3) {
        checkThread(jComponent);
        super.addDirtyRegion(jComponent, i, i1, i2, i3);
    }
}
