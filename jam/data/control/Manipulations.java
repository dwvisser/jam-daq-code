package jam.data.control;

import jam.data.AbstractHist1D;
import jam.data.DataException;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.MessageHandler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Class for projecting 2-D histograms.
 * 
 * @author Dale Visser
 */
public class Manipulations extends DataControl implements ActionListener,
		ItemListener, WindowListener, Observer {

	private final Frame frame;

	private final MessageHandler messageHandler;

	private JComboBox cfrom1, cfrom2, cto;

	private JCheckBox cnorm, cplus, cminus, ctimes, cdiv;

	private JTextField ttextto, ttimes1, ttimes2;

	private JLabel lname;

	public Manipulations(MessageHandler mh) {
		super("Manipulate 1-D Histograms", false);
		frame = status.getFrame();
		messageHandler = mh;
		setResizable(false);
		final int CHOOSER_SIZE = 200;
		Dimension dim;
		final int hgap = 5;
		final int vgap = 5;
		//UI
		final Container cdmanip = getContentPane();
		cdmanip.setLayout(new BorderLayout(hgap, vgap));
		setLocation(20, 50);
		addWindowListener(this);

		//Labels panel
		JPanel pLabels = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pLabels.setBorder(new EmptyBorder(10, 10, 0, 0));
		cdmanip.add(pLabels, BorderLayout.WEST);
		pLabels.add(new JLabel("From  histogram", JLabel.RIGHT));
		pLabels.add(new JLabel("Operation", JLabel.RIGHT));
		pLabels.add(new JLabel("With histogram", JLabel.RIGHT));
		pLabels.add(new JLabel("To histogram", JLabel.RIGHT));

		//Entries Panel
		JPanel pEntries = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pEntries.setBorder(new EmptyBorder(10, 0, 0, 10));
		cdmanip.add(pEntries, BorderLayout.CENTER);

		//From Panel
		JPanel pfrom1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pfrom1);
		cfrom1 = new JComboBox();
		dim = cfrom1.getPreferredSize();
		dim.width = CHOOSER_SIZE;
		cfrom1.setPreferredSize(dim);

		cfrom1.addItem("1DHISTOGRAM1");
		cfrom1.addItemListener(this);
		pfrom1.add(cfrom1);
		pfrom1.add(new JLabel("x"));
		ttimes1 = new JTextField("1.0", 8);
		pfrom1.add(ttimes1);

		//Operation Panel
		JPanel pradio = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pradio);
		ButtonGroup cbg = new ButtonGroup();
		cnorm = new JCheckBox("Renormalize", true);
		cbg.add(cnorm);
		pradio.add(cnorm);
		cnorm.addItemListener(this);
		cplus = new JCheckBox("Add", false);
		cbg.add(cplus);
		pradio.add(cplus);
		cplus.addItemListener(this);
		cminus = new JCheckBox("Subtract", false);
		cbg.add(cminus);
		pradio.add(cminus);
		cminus.addItemListener(this);
		ctimes = new JCheckBox("Multiply", false);
		cbg.add(ctimes);
		pradio.add(ctimes);
		ctimes.addItemListener(this);
		cdiv = new JCheckBox("Divide", false);
		cbg.add(cdiv);
		pradio.add(cdiv);
		cdiv.addItemListener(this);

		//With panel
		JPanel pfrom2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pfrom2);
		cfrom2 = new JComboBox();
		dim = cfrom2.getPreferredSize();
		dim.width = CHOOSER_SIZE;
		cfrom2.setPreferredSize(dim);
		cfrom2.addItem("1DHISTOGRAM2");
		cfrom2.addItemListener(this);
		ttimes2 = new JTextField("1.0", 8);
		pfrom2.add(cfrom2);
		pfrom2.add(new JLabel("x"));
		pfrom2.add(ttimes2);
		setInput2(true);

		//To panel
		JPanel pto = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pto);
		cto = new JComboBox();
		dim = cto.getPreferredSize();
		dim.width = CHOOSER_SIZE;
		cto.setPreferredSize(dim);
		cto.addItem("New Histogram");
		cto.addItemListener(this);
		ttextto = new JTextField("new", 20);
		pto.add(cto);
		lname = new JLabel("Name");
		pto.add(lname);
		pto.add(ttextto);

		//button panel
		JPanel pFlowControl = new JPanel(new FlowLayout(FlowLayout.CENTER));
		cdmanip.add(pFlowControl, BorderLayout.SOUTH);
		JPanel pcontrol = new JPanel(new GridLayout(1, 0, hgap, vgap));
		pFlowControl.add(pcontrol);
		JButton bOK = new JButton("OK");
		bOK.setActionCommand("ok");
		bOK.addActionListener(this);
		pcontrol.add(bOK);
		JButton bApply = new JButton("Apply");
		bApply.setActionCommand("apply");
		bApply.addActionListener(this);
		pcontrol.add(bApply);
		JButton bCancel = new JButton("Cancel");
		bCancel.setActionCommand("cancel");
		bCancel.addActionListener(this);
		pcontrol.add(bCancel);
		pack();
	}

	/**
	 * Are we done setting gate and should we save it or has the gate setting
	 * been canceled.
	 *  
	 */
	public void actionPerformed(ActionEvent e) {
		final String command = e.getActionCommand();
		try {
			if (command == "ok" || command == "apply") {
				manipulate();
				broadcaster.broadcast(BroadcastEvent.Command.REFRESH);
				if (command == "ok") {
					dispose();
				}
			} else if (command == "cancel") {
				dispose();
			} else {
				throw new UnsupportedOperationException(
						"Not a recognized command: " + command);
			}
		} catch (DataException je) {
			messageHandler.errorOutln(je.getMessage());
		}
	}

	/**
	 * An item state change indicates that a gate has been chosen.
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == cnorm || ie.getSource() == cplus
				|| ie.getSource() == cminus || ie.getSource() == ctimes
				|| ie.getSource() == cdiv) {
			setInput2(cnorm.isSelected());
		} else if (ie.getSource() == cto) {
			if (cto.getSelectedItem() != null) {
				setUseNewHist(cto.getSelectedItem().equals("New Histogram"));
			}
		}
	}

	/**
	 * Implementation of Observable interface listeners for broadcast events.
	 * broadcast events where there are new histograms or histograms added.
	 */
	public void update(Observable observable, Object o) {
		BroadcastEvent be = (BroadcastEvent) o;

		if (be.getCommand() == BroadcastEvent.Command.HISTOGRAM_NEW) {
			setup();
		} else if (be.getCommand() == BroadcastEvent.Command.HISTOGRAM_ADD) {
			setup();
		}

	}

	/**
	 * Loads the list of gates and set co-ordinates as x y if 2d or lower upper
	 * if 1 d
	 *  
	 */
	public void setup() {
		String lfrom1, lfrom2, lto;

		lfrom1 = (String) cfrom1.getSelectedItem();
		lfrom2 = (String) cfrom2.getSelectedItem();
		lto = (String) cto.getSelectedItem();

		cfrom1.removeAllItems();
		addChooserHists(cfrom1, Histogram.Type.ONE_DIM_INT,
				Histogram.Type.ONE_D_DOUBLE);
		cfrom1.setSelectedItem(lfrom1);
		cfrom2.removeAllItems();
		addChooserHists(cfrom2, Histogram.Type.ONE_DIM_INT,
				Histogram.Type.ONE_D_DOUBLE);
		cfrom2.setSelectedItem(lfrom2);
		cto.removeAllItems();
		cto.addItem("New Histogram");
		addChooserHists(cto, Histogram.Type.ONE_DIM_INT,
				Histogram.Type.ONE_D_DOUBLE);
		cto.setSelectedItem(lto);
		if (lto.equals("New Histogram")) {
			setUseNewHist(true);
		} else {
			setUseNewHist(false);
		}
	}

	/**
	 * A second histogram is needed
	 */
	private void setInput2(boolean state) {
		cfrom2.setEnabled(!state);
		ttimes2.setEnabled(!state);
	}

	/**
	 * add histograms of type type1 and type2 to chooser
	 */
	private void addChooserHists(JComboBox c, Histogram.Type type1,
			Histogram.Type type2) {
		for (Iterator e = Histogram.getHistogramList().iterator(); e.hasNext();) {
			Histogram h = (Histogram) e.next();
			if ((h.getType() == type1) || (h.getType() == type2)) {
				c.addItem(h.getName());
			}
		}
	}

	/**
	 * Set dialog box for new histogram to be created
	 */
	private void setUseNewHist(boolean state) {
		lname.setEnabled(state);
		ttextto.setEnabled(state);
	}

	/**
	 * Does the work of manipulating histograms
	 */
	private void manipulate() throws DataException {
		final double fac1;
		try {//read information for first histogram
			fac1 = Double.valueOf(ttimes1.getText().trim()).doubleValue();
		} catch (NumberFormatException nfe) {
			throw new DataException(
					"First factor is not a valid number [Manipulations]");
		}
		final AbstractHist1D hfrom1 = (AbstractHist1D)Histogram.getHistogram((String) cfrom1
				.getSelectedItem());
		final double[] in1;
		if (hfrom1.getType() == Histogram.Type.ONE_DIM_INT) {
			in1 = toDoubleArray((int[]) hfrom1.getCounts());
		} else {
			in1 = (double[]) hfrom1.getCounts();
		}
		final double[] err1 = hfrom1.getErrors();
		final double fac2;
		try {//read in information for second histogram
			fac2 = Double.valueOf(ttimes2.getText().trim()).doubleValue();
		} catch (NumberFormatException nfe) {
			throw new DataException(
					"Second factor is not a valid number [Manipulations]");
		}
		final double[] in2, err2;
		if (cfrom2.isEnabled()) {
			final AbstractHist1D hfrom2 = (AbstractHist1D)Histogram.getHistogram((String) cfrom2
					.getSelectedItem());
			if (hfrom2.getType() == Histogram.Type.ONE_DIM_INT) {
				in2 = toDoubleArray((int[]) hfrom2.getCounts());
			} else {
				in2 = (double[]) hfrom2.getCounts();
			}
			err2 = hfrom2.getErrors();
		} else {
			in2 = null;
			err2 = null;
		}

		//read in information for to histogram
		String name = (String) cto.getSelectedItem();
		final AbstractHist1D hto;
		if (name.equals("New Histogram")) {
			name = ttextto.getText().trim();
			/*hto = new Histogram(name, Histogram.Type.ONE_D_DOUBLE, hfrom1
					.getSizeX(), name);*/
			hto = (AbstractHist1D)Histogram.createHistogram(
					new double[hfrom1.getSizeX()],name);
			broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
			messageHandler
					.messageOutln("New histogram of type double created, name: '"
							+ name + "'");
		} else {
			hto = (AbstractHist1D)Histogram.getHistogram(name);
		}
		hto.setZero();
		final double[] out = (hto.getType() == Histogram.Type.ONE_DIM_INT) ? toDoubleArray((int[]) hto
				.getCounts())
				: (double[]) hto.getCounts();
		double[] errOut = hto.getErrors();

		if (cnorm.isSelected()) {
			for (int i = 0; i < out.length; i++) {
				out[i] = fac1 * in1[i];
				errOut[i] = fac1 * err1[i];
			}
		} else if (cplus.isSelected()) {
			for (int i = 0; i < out.length; i++) {
				out[i] = fac1 * in1[i] + fac2 * in2[i];
				errOut[i] = Math.sqrt(fac1 * fac1 * err1[i] * err1[i] + fac2
						* fac2 * err2[i] * err2[i]);
			}
		} else if (cminus.isSelected()) {
			for (int i = 0; i < out.length; i++) {
				out[i] = fac1 * in1[i] - fac2 * in2[i];
				errOut[i] = Math.sqrt(fac1 * fac1 * err1[i] * err1[i] + fac2
						* fac2 * err2[i] * err2[i]);
			}
		} else if (ctimes.isSelected()) {
			for (int i = 0; i < out.length; i++) {
				out[i] = fac1 * in1[i] * fac2 * in2[i];
				errOut[i] = Math.sqrt(fac2 * fac2 * err1[i] * err1[i] + fac1
						* fac1 * err2[i] * err2[i]);
			}
		} else if (cdiv.isSelected()) {
			for (int i = 0; i < out.length; i++) {
				out[i] = fac1 * in1[i] / (fac2 * in2[i]);
				errOut[i] = in1[i]
						/ in2[i]
						* Math.sqrt(fac1 * fac1 / (err1[i] * err1[i]) + fac2
								* fac2 / (err2[i] * err2[i]));
			}
		}
		hto.setErrors(errOut);
		/* cast to int array if needed */
		if (hto.getType() == Histogram.Type.ONE_DIM_INT) {
			hto.setCounts(toIntArray(out));
		} else {
			hto.setCounts(out);
		}
	}

	/**
	 * Converts int array to double array
	 */
	private double[] toDoubleArray(int[] in) {
		final double[] out = new double[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = (double) in[i];
		}
		return out;
	}

	/**
	 * Converts double array to int array
	 */
	private int[] toIntArray(double[] in) {
		int[] out;

		out = new int[in.length];
		for (int i = 0; i < in.length; i++) {
			out[i] = (int) Math.round(in[i]);
		}
		return out;
	}

	/**
	 * Process window events If the window is active check that the histogram
	 * been displayed has not changed. If it has cancel the gate setting.
	 */
	public void windowActivated(WindowEvent e) {
		//no action
	}

	/**
	 * Window Events windowClosing only one used.
	 */
	public void windowClosing(WindowEvent e) {
		dispose();
	}

	/**
	 * Does nothing only windowClosing used.
	 */
	public void windowClosed(WindowEvent e) {
		/* does nothing for now */
	}

	/**
	 * Does nothing only windowClosing used.
	 */
	public void windowDeactivated(WindowEvent e) {
		/* does nothing for now */
	}

	/**
	 * Does nothing only windowClosing used.
	 */
	public void windowDeiconified(WindowEvent e) {
		/* does nothing for now */
	}

	/**
	 * Does nothing only windowClosing used.
	 */
	public void windowIconified(WindowEvent e) {
		/* does nothing for now */
	}

	/**
	 * removes list of gates when closed only windowClosing used.
	 */
	public void windowOpened(WindowEvent e) {
		setup();
	}

}