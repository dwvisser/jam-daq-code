package jam.data.control;

import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.global.MessageHandler;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * Zero histograms dialog
 */
public class HistogramZeroControl extends DataControl {

	private static final Broadcaster broadcaster =
		Broadcaster.getSingletonInstance();
	private final Frame frame;
	private final MessageHandler msghdlr;
	private final JDialog dialogZero;

	/**
	 * Constructor
	 */
	public HistogramZeroControl(Frame f, MessageHandler mh) {
		super();
		frame = f;
		msghdlr = mh;
		/* zero histogram dialog box */
		dialogZero = new JDialog(frame, "Zero Histograms", false);
		final Container dzc = dialogZero.getContentPane();
		dialogZero.setResizable(false);
		dzc.setLayout(new FlowLayout(FlowLayout.CENTER));
		final JPanel pButton = new JPanel(new GridLayout(1, 0, 5, 5));
		dialogZero.setLocation(20, 50);
		final JButton one = new JButton("Displayed");
		one.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final Histogram currentHistogram =
					Histogram.getHistogram(
						JamStatus.instance().getCurrentHistogramName());
				currentHistogram.setZero();
				broadcaster.broadcast(BroadcastEvent.REFRESH);
				msghdlr.messageOutln(
					"Zero Histogram: " + currentHistogram.getTitle());
				dialogZero.dispose();
			}
		});
		pButton.add(one);
		final JButton all = new JButton("   All   ");
		all.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				zeroAll();
				dialogZero.dispose();
			}
			
			/**
			 * Zero all the histograms.
			 */
			private void zeroAll() {
				msghdlr.messageOut("Zero All", MessageHandler.NEW);
				final Iterator allHistograms = Histogram.getHistogramList().iterator();
				while (allHistograms.hasNext()) {
					final Histogram hist = ((Histogram) allHistograms.next());
					msghdlr.messageOut(" .", MessageHandler.CONTINUE);
					hist.setZero();
				}
				broadcaster.broadcast(BroadcastEvent.REFRESH);
				msghdlr.messageOut(" done!", MessageHandler.END);
			}
		});
		pButton.add(all);
		final JButton cancel = new JButton(" Cancel ");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dialogZero.dispose();
			}
		});
		pButton.add(cancel);
		dzc.add(pButton);
		dialogZero.pack();
		dialogZero.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dialogZero.dispose();
			}
		});
	}

	public void setup() {
		/* nothing to do */
	}

	public void show() {
		dialogZero.show();
	}
}
