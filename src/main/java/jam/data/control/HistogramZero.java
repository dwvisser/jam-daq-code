package jam.data.control;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;

import jam.data.AbstractHistogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.ui.SelectionTree;

/**
 * Zero histograms dialog
 */

public class HistogramZero extends AbstractControl {

	/**
	 * Construct a new "zero histograms" dialog.
	 * 
	 * @param frame
	 *            application frame
	 * @param broadcaster
	 *            broadcasts state changes
	 */
	@Inject
	public HistogramZero(final Frame frame, final Broadcaster broadcaster) {
		super(frame, "Zero Histograms", false, broadcaster);
		/* zero histogram dialog box */
		final Container dzc = getContentPane();
		setResizable(false);
		setLocation(20, 50);
		dzc.setLayout(new FlowLayout(FlowLayout.CENTER));
		final JPanel pButton = new JPanel(new GridLayout(1, 0, 5, 10));
		final Border border = new EmptyBorder(10, 10, 10, 10);
		pButton.setBorder(border);

		final JButton one = new JButton("Displayed");
		one.addActionListener(actionEvent -> {
            final AbstractHistogram currentHistogram = (AbstractHistogram) SelectionTree
                    .getCurrentHistogram();
            currentHistogram.setZero();
            broadcaster.broadcast(BroadcastEvent.Command.REFRESH);
            LOGGER.info("Zero Histogram: " + currentHistogram.getTitle());
            dispose();
        });
		pButton.add(one);
		final JButton all = new JButton("   All   ");
		all.addActionListener(event -> {
            zeroAll();
            dispose();
        });
		pButton.add(all);
		final JButton cancel = new JButton(" Cancel ");
		cancel.addActionListener(event -> dispose());
		pButton.add(cancel);
		dzc.add(pButton);
		pack();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				dispose();
			}
		});
	}

	/**
	 * Zero all the histograms.
	 */
	public void zeroAll() {
		final String classname = getClass().getName();
		final String methname = "zeroAll";
		LOGGER.entering(classname, methname);
		LOGGER.info("Zero All");
		final List<AbstractHistogram> allHistograms = AbstractHistogram
				.getHistogramList();
		for (AbstractHistogram hist : allHistograms) {
			hist.setZero();
		}
		broadcaster.broadcast(BroadcastEvent.Command.REFRESH);
		LOGGER.exiting(classname, methname);
	}

	@Override
	public void doSetup() {
		/* nothing to do */
	}
}
