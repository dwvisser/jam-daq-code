package jam.data.control;

import jam.data.Gate;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.MessageHandler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Ken
 *
 */
public class GateNew extends DataControl {

	/* new gate dialog box */
	private final JTextField textNew;
	private final MessageHandler messageHandler;

	public GateNew(MessageHandler mh) {
		super("New Gate", false);
		messageHandler = mh;
		final Container cdnew = getContentPane();
		setResizable(false);
		cdnew.setLayout(new BorderLayout(5, 5));
		setLocation(20, 50);
		/* panel with chooser */
		final JPanel ptnew = new JPanel();
		ptnew.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
		cdnew.add(ptnew, BorderLayout.CENTER);
		ptnew.add(new JLabel("Name"));
		textNew = new JTextField("", 20);
		ptnew.add(textNew);
		/*  panel for buttons */
		final JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
		cdnew.add(pbutton, BorderLayout.SOUTH);
		final JPanel pbnew = new JPanel();
		pbnew.setLayout(new GridLayout(1, 0, 5, 5));
		pbutton.add(pbnew, BorderLayout.SOUTH);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				makeGate();
				dispose();
			}
		});
		pbnew.add(bok);
		final JButton bapply = new JButton("Apply");
		bapply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				makeGate();
			}
		});
		pbnew.add(bapply);
		final JButton bcancel = new JButton("Cancel");
		bcancel.setActionCommand("cancelnew");
		bcancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		});
		pbnew.add(bcancel);
		pack();
	}

	/**
	 * Make a new gate, and add it to the current histogram.
	 *
	 * @throws GlobalException if there's a problem
	 */
	private void makeGate() {
		final Histogram hist =
			Histogram.getHistogram(status.getCurrentHistogramName());
		new Gate(textNew.getText(), hist);
		broadcaster.broadcast(BroadcastEvent.Command.GATE_ADD);
		messageHandler.messageOutln(
			"New gate "
				+ textNew.getText()
				+ " created for histogram "
				+ hist.getName());
	}

	public void setup() {
		/* nothing needed here */
	}

}
