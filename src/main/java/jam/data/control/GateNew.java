package jam.data.control;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.google.inject.Inject;

import jam.data.AbstractHistogram;
import jam.data.Gate;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.ui.PanelOKApplyCancelButtons;
import jam.ui.SelectionTree;

/**
 * A dialog for defining new gates.
 * @author Ken Swartz
 */
@SuppressWarnings("serial")
public class GateNew extends AbstractControl {

    /* new gate dialog box */
    private final transient JTextField textNew;

    /**
     * Construct a new "new gate" dialog.
     * @param frame
     *            application frame
     * @param broadcaster
     *            broadcasts state changes
     */
    @Inject
    public GateNew(final Frame frame, final Broadcaster broadcaster) {
        super(frame, "New Gate", false, broadcaster);
        final Container cdnew = getContentPane();
        setResizable(false);
        final int smallGap = 5;
        cdnew.setLayout(new BorderLayout(smallGap, smallGap));
        final int xPosition = 20;
        final int yPosition = 50;
        setLocation(xPosition, yPosition);
        /* panel with chooser */
        final JPanel ptnew = new JPanel();
        final int bigGap = 20;
        ptnew.setLayout(new FlowLayout(FlowLayout.LEFT, bigGap, bigGap));
        cdnew.add(ptnew, BorderLayout.CENTER);
        ptnew.add(new JLabel("Name"));
        final int columns = 20;
        textNew = new JTextField("", columns);
        ptnew.add(textNew);
        /* panel for buttons */
        final PanelOKApplyCancelButtons.Listener callback = new PanelOKApplyCancelButtons.AbstractListener(
                this) {
            public void apply() {
                makeGate();
            }
        };

        final PanelOKApplyCancelButtons pbutton = new PanelOKApplyCancelButtons(
                callback);
        cdnew.add(pbutton.getComponent(), BorderLayout.SOUTH);
        pack();
    }

    /**
     * Make a new gate, and add it to the current histogram.
     */
    private void makeGate() {
        final AbstractHistogram hist = (AbstractHistogram) SelectionTree
                .getCurrentHistogram();
        new Gate(textNew.getText(), hist);
        broadcaster.broadcast(BroadcastEvent.Command.GATE_ADD);
        LOGGER.info("New gate " + textNew.getText()
                + " created for histogram " + hist.getFullName());
    }

    /**
     * Nothing to do.
     */
    @Override
    public void doSetup() {
        /* nothing needed here */
    }

}
