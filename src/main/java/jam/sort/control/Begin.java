package jam.sort.control;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.text.JTextComponent;

import injection.GuiceInjector;
import jam.global.JamException;
import jam.sort.SortException;
import jam.ui.Icons;

@SuppressWarnings("serial")
class Begin extends AbstractAction {

    private static final Logger LOGGER = Logger.getLogger(Begin.class
            .getPackage().getName());

    private transient final RunController runControl;

    private transient final Component parent;

    private transient final JTextComponent textRunTitle;

    Begin(final Component parent, final RunController runControl,
            final JTextComponent text) {
        super();
        this.parent = parent;
        this.runControl = runControl;
        textRunTitle = text;
        putValue(Action.NAME, "Begin Run");
        putValue(Action.SHORT_DESCRIPTION, "Begins the next run.");
        putValue(Action.SMALL_ICON,
                GuiceInjector.getObjectInstance(Icons.class).BEGIN);
        setEnabled(false);
    }

    public void actionPerformed(final ActionEvent event) {
        final String runTitle = textRunTitle.getText().trim();
        final boolean confirm = (JOptionPane.showConfirmDialog(parent,
                "Is this title OK? :\n" + runTitle, "Run Title Confirmation",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
        if (confirm) {
            try {
                runControl.beginRun();
            } catch (SortException | JamException se) {
                LOGGER.log(Level.SEVERE, se.getMessage(), se);
            }

        }
    }

}
