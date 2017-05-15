package jam.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Panel with OK, Apply and Cancel buttons for dialogs.
 * @author Ken Swartz
 */
public final class PanelOKApplyCancelButtons {

    private final transient Listener callback;
    private final transient JPanel panel = new JPanel(new FlowLayout(
            FlowLayout.CENTER));
    private final transient JButton bok = new JButton("OK");
    private final transient JButton bapply = new JButton("Apply");
    private final transient JButton bcancel;

    /**
     * Constructs a Swing component which has OK, Apply, and Cancel buttons.
     * @param listener
     *            object with methods to be called when the buttons get pressed
     */
    public PanelOKApplyCancelButtons(final Listener listener) {
        super();
        callback = listener;
        final JPanel grid = new JPanel(new GridLayout(1, 0, 5, 5));
        panel.add(grid);
        bok.addActionListener(actionEvent -> callback.doOK());
        grid.add(bok);
        bapply.addActionListener(actionEvent -> callback.apply());
        grid.add(bapply);
        bcancel = new JButton(new WindowCancelAction(callback));
        grid.add(bcancel);
    }

    /**
     * Returns the Swing button panel.
     * @return the Swing button panel
     */
    public JComponent getComponent() {
        return panel;
    }

    /**
     * Set the enabled state of the buttons.
     * @param okEnable
     *            the enable state of "OK" button
     * @param apply
     *            the enable state of "Apply" button
     * @param cancel
     *            the enable state of "Cancel" button
     */
    public void setButtonsEnabled(final boolean okEnable, final boolean apply,
            final boolean cancel) {
        bok.setEnabled(okEnable);
        bapply.setEnabled(apply);
        bcancel.setEnabled(cancel);
    }

    /**
     * Handler for OK, apply and cancel methods which are called when their
     * associated buttons are pressed.
     * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser</a>
     */
    public interface Listener extends Canceller {

        /**
         * To be called when the user clicks the OK button.
         */
        void doOK();

        /**
         * To be called when the user clicks the Apply button.
         */
        void apply();
    }

    /**
     * Default implementation of <code>Listener</code> has <code>doOK()</code>
     * execute <code>apply()</code>, then <code>cancel</code>, which makes the
     * given <code>Window</code> invisible.
     * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser</a>
     */
    public abstract static class AbstractListener implements Listener {
        private final transient Window parent;

        /**
         * Constructs a listener.
         * @param window
         *            the ultimate container we wish to make disappear on cancel
         *            and OK
         */
        public AbstractListener(final Window window) {
            super();
            parent = window;
        }

        /**
         * For OK button.
         */
        public final void doOK() {
            apply();
            cancel();
        }

        /**
         * For Cancel button.
         */
        public void cancel() {
            parent.dispose();
        }
    }

}
