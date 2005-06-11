package jam.data.control;

import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.global.MessageHandler;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Zero histograms dialog
 */
public class HistogramZero extends AbstractControl {

	private final MessageHandler msghdlr;

	/**
	 * Construct a new "zero histograms" dialog.
	 * 
	 * @param mh where to print messages
	 */
	public HistogramZero(MessageHandler mh) {
		super("Zero Histograms", false);
		msghdlr = mh;
		/* zero histogram dialog box */
		final Container dzc = getContentPane();
		setResizable(false);
		setLocation(20, 50);		
		dzc.setLayout(new FlowLayout(FlowLayout.CENTER));
		final JPanel pButton = new JPanel(new GridLayout(1, 0, 10, 10));
		Border border = new EmptyBorder(10, 10, 10, 10);
		pButton.setBorder(border);

		final JButton one = new JButton("Displayed");
		one.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final Histogram currentHistogram =JamStatus.getSingletonInstance().getCurrentHistogram();
				currentHistogram.setZero();
				BROADCASTER.broadcast(BroadcastEvent.Command.REFRESH);
				msghdlr.messageOutln(
					"Zero Histogram: " + currentHistogram.getTitle());
				dispose();
			}
		});
		pButton.add(one);
		final JButton all = new JButton("   All   ");
		all.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				zeroAll();
				dispose();
			}			
		});
		pButton.add(all);
		final JButton cancel = new JButton(" Cancel ");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		});
		pButton.add(cancel);
		dzc.add(pButton);
		pack();
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
	}

	/**
	 * Zero all the histograms.
	 */
	public void zeroAll() {
		msghdlr.messageOut("Zero All", MessageHandler.NEW);
		final Iterator allHistograms = Histogram.getHistogramList().iterator();
		while (allHistograms.hasNext()) {
			final Histogram hist = ((Histogram) allHistograms.next());
			msghdlr.messageOut(" .", MessageHandler.CONTINUE);
			hist.setZero();
		}
		BROADCASTER.broadcast(BroadcastEvent.Command.REFRESH);
		msghdlr.messageOut(" done!", MessageHandler.END);
	}
	
	public void doSetup() {
		/* nothing to do */
	}
}
