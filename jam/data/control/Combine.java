package jam.data.control;

import jam.data.AbstractHist1D;
import jam.data.DataException;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.MessageHandler;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Combine histograms and also normalize a histogram
 * 
 * @author Dale Visser, Ken Swartz
 */
public class Combine extends AbstractManipulation implements Observer {

	private JComboBox cfrom1, cfrom2, cto;

	private JCheckBox cnorm, cplus, cminus, ctimes, cdiv;

	private double fac1;

	private double fac2;

	private AbstractHist1D hto;

	private JLabel lname, lWith;

	private final MessageHandler messageHandler;

	private JTextField ttextto, ttimes1, ttimes2;

	/**
	 * Construct a new "manipilate histograms" dialog.
	 * 
	 * @param mh
	 *            where to print messages
	 */
	public Combine(MessageHandler mh) {
		super("Manipulate 1-D Histograms", false);
		messageHandler = mh;
		setResizable(false);
		Dimension dim;
		final int hgap = 5;
		final int vgap = 5;
		int meanCharWidth;
		// UI
		final Container cdmanip = getContentPane();
		cdmanip.setLayout(new BorderLayout(hgap, vgap));
		setLocation(20, 50);

		// Labels panel
		JPanel pLabels = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pLabels.setBorder(new EmptyBorder(10, 10, 0, 0));
		cdmanip.add(pLabels, BorderLayout.WEST);
		lWith = new JLabel("With histogram", JLabel.RIGHT);
		pLabels.add(new JLabel("From  histogram", JLabel.RIGHT));
		pLabels.add(new JLabel("Operation", JLabel.RIGHT));
		pLabels.add(lWith);
		pLabels.add(new JLabel("To histogram", JLabel.RIGHT));

		// Entries Panel
		JPanel pEntries = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pEntries.setBorder(new EmptyBorder(10, 0, 0, 10));
		cdmanip.add(pEntries, BorderLayout.CENTER);

		// From Panel
		JPanel pfrom1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pfrom1);
		cfrom1 = new JComboBox();
		meanCharWidth = getMeanCharWidth(cfrom1
				.getFontMetrics(cfrom1.getFont()));
		dim = cfrom1.getPreferredSize();
		dim.width = CHAR_LENGTH * meanCharWidth;
		cfrom1.setPreferredSize(dim);

		cfrom1.addItem("1DHISTOGRAM1");
		pfrom1.add(cfrom1);
		pfrom1.add(new JLabel("x"));
		ttimes1 = new JTextField("1.0", 8);
		pfrom1.add(ttimes1);

		// Operation Panel
		JPanel pradio = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pradio);
		ButtonGroup cbg = new ButtonGroup();

		cnorm = new JCheckBox("Renormalize", true);
		cnorm.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				enableInputWith(false);
			}
		});
		cbg.add(cnorm);
		pradio.add(cnorm);

		cplus = new JCheckBox("Add", false);
		cplus.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				enableInputWith(true);
			}
		});
		cbg.add(cplus);
		pradio.add(cplus);

		cminus = new JCheckBox("Subtract", false);
		cminus.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				enableInputWith(true);
			}
		});
		cbg.add(cminus);
		pradio.add(cminus);

		ctimes = new JCheckBox("Multiply", false);
		ctimes.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				enableInputWith(true);
			}
		});
		cbg.add(ctimes);
		pradio.add(ctimes);

		cdiv = new JCheckBox("Divide", false);
		cdiv.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				enableInputWith(true);
			}
		});
		cbg.add(cdiv);
		pradio.add(cdiv);

		// With panel
		JPanel pfrom2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pfrom2);
		cfrom2 = new JComboBox();
		meanCharWidth = getMeanCharWidth(cfrom2
				.getFontMetrics(cfrom2.getFont()));
		dim = cfrom2.getPreferredSize();
		dim.width = CHAR_LENGTH * meanCharWidth;
		cfrom2.setPreferredSize(dim);
		cfrom2.addItem("1DHISTOGRAM2");
		ttimes2 = new JTextField("1.0", 8);
		pfrom2.add(cfrom2);
		pfrom2.add(new JLabel("x"));
		pfrom2.add(ttimes2);
		enableInputWith(true);

		// To panel
		JPanel pto = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pto);
		cto = new JComboBox();
		meanCharWidth = getMeanCharWidth(cfrom1
				.getFontMetrics(cfrom1.getFont()));
		dim = cto.getPreferredSize();
		dim.width = CHAR_LENGTH * meanCharWidth;
		cto.setPreferredSize(dim);
		cto.addItem(NEW_HIST);
		cto.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (cto.getSelectedItem() != null) {
					setUseHist((String) cto.getSelectedItem());
				}
			}
		});
		ttextto = new JTextField("combine", TEXT_LENGTH);
		pto.add(cto);
		lname = new JLabel("Name");
		pto.add(lname);
		pto.add(ttextto);
		/* button panel */
		final PanelOKApplyCancelButtons pButtons = new PanelOKApplyCancelButtons(
				new PanelOKApplyCancelButtons.DefaultListener(this) {
					public void apply() {
						try {
							combine();
							BROADCASTER
									.broadcast(BroadcastEvent.Command.REFRESH);
							STATUS.setCurrentHistogram(hto);
							BROADCASTER.broadcast(
									BroadcastEvent.Command.HISTOGRAM_SELECT,
									hto);
						} catch (DataException je) {
							messageHandler.errorOutln(je.getMessage());
						}
					}
				});
		cdmanip.add(pButtons.getComponent(), BorderLayout.SOUTH);
		pack();
	}

	/*
	 * non-javadoc: Does the work of manipulating histograms
	 */
	private void combine() throws DataException {

		final double[] in1, err1;
		final double[] in2, err2;
		final double[] out, errOut;
		String operation = "";
		int numChannels;

		validateFactors();

		final AbstractHist1D hfrom1 = (AbstractHist1D) Histogram
				.getHistogram((String) cfrom1.getSelectedItem());
		AbstractHist1D hfrom2 = null;

		in1 = doubleCountsArray(hfrom1);
		err1 = hfrom1.getErrors();

		if (cfrom2.isEnabled()) {
			hfrom2 = (AbstractHist1D) Histogram.getHistogram((String) cfrom2
					.getSelectedItem());
			in2 = doubleCountsArray(hfrom2);
			err2 = hfrom2.getErrors();
		} else {
			in2 = null;
			err2 = null;
		}

		// read in information for to histogram
		String name = (String) cto.getSelectedItem();
		if (isNewHistogram(name)) {
			String histName = ttextto.getText().trim();
			String groupName = parseGroupName(name);
			hto = (AbstractHist1D) createNewHistogram(groupName, histName,
					hfrom1.getSizeX());
			messageHandler.messageOutln("New Histogram created: '" + groupName
					+ "/" + histName + "'");
		} else {
			hto = (AbstractHist1D) Histogram.getHistogram(name);
		}
		hto.setZero();

		out = doubleCountsArray(hto);
		errOut = hto.getErrors();

		// Minimun size of both out an in1
		numChannels = Math.min(out.length, in1.length);

		// Minimun size of all out an in1 and in2
		if (!cnorm.isSelected()) {
			numChannels = Math.min(numChannels, in2.length);
		}

		// Do calculation
		if (cnorm.isSelected()) {
			for (int i = 0; i < numChannels; i++) {
				out[i] = fac1 * in1[i];
				errOut[i] = fac1 * err1[i];
			}
			operation = " Normalized ";
		} else if (cplus.isSelected()) {
			for (int i = 0; i < numChannels; i++) {
				out[i] = fac1 * in1[i] + fac2 * in2[i];
				errOut[i] = Math.sqrt(fac1 * fac1 * err1[i] * err1[i] + fac2
						* fac2 * err2[i] * err2[i]);
			}
			operation = " Added with ";
		} else if (cminus.isSelected()) {
			for (int i = 0; i < numChannels; i++) {
				out[i] = fac1 * in1[i] - fac2 * in2[i];
				errOut[i] = Math.sqrt(fac1 * fac1 * err1[i] * err1[i] + fac2
						* fac2 * err2[i] * err2[i]);
			}
			operation = " Subtracted from ";
		} else if (ctimes.isSelected()) {
			for (int i = 0; i < numChannels; i++) {
				out[i] = fac1 * in1[i] * fac2 * in2[i];
				errOut[i] = Math.sqrt(fac2 * fac2 * err1[i] * err1[i] + fac1
						* fac1 * err2[i] * err2[i]);
			}
			operation = " Multiplied with ";
		} else if (cdiv.isSelected()) {
			for (int i = 0; i < numChannels; i++) {
				out[i] = fac1 * in1[i] / (fac2 * in2[i]);
				errOut[i] = in1[i]
						/ in2[i]
						* Math.sqrt(fac1 * fac1 / (err1[i] * err1[i]) + fac2
								* fac2 / (err2[i] * err2[i]));
			}
			operation = " Divided by ";
		}
		hto.setErrors(errOut);

		/* cast to int array if needed */
		if (hto.getType() == Histogram.Type.ONE_DIM_INT) {
			hto.setCounts(doubleToIntArray(out));
		} else {
			hto.setCounts(out);
		}

		if (hfrom2 != null) {
			messageHandler.messageOutln("Combine "
					+ hfrom1.getFullName().trim() + operation
					+ hfrom2.getFullName().trim() + " to " + hto.getFullName());
		} else {
			messageHandler.messageOutln("Normalize "
					+ hfrom1.getFullName().trim() + " to " + hto.getFullName());

		}

	}

	/**
	 * Loads the list of gates and set co-ordinates as x y if 2d or lower upper
	 * if 1 d
	 * 
	 */
	public void doSetup() {
		String lfrom1, lfrom2, lto;

		lfrom1 = (String) cfrom1.getSelectedItem();
		lfrom2 = (String) cfrom2.getSelectedItem();
		lto = (String) cto.getSelectedItem();

		cfrom1.removeAllItems();
		loadAllHists(cfrom1, false, Histogram.Type.ONE_D);
		cfrom1.setSelectedItem(lfrom1);
		cfrom2.removeAllItems();
		loadAllHists(cfrom2, false, Histogram.Type.ONE_D);

		cfrom2.setSelectedItem(lfrom2);
		cto.removeAllItems();
		cto.addItem(NEW_HIST);
		loadAllHists(cto, true, Histogram.Type.ONE_D);
		cto.setSelectedItem(lto);
		setUseHist((String) cto.getSelectedItem());

		enableInputWith(!cnorm.isSelected());
	}

	private double[] doubleCountsArray(Histogram hist) {
		double[] dCounts;
		if (hist.getType() == Histogram.Type.ONE_DIM_INT) {
			dCounts = intToDoubleArray((int[]) hist.getCounts());
		} else {
			dCounts = (double[]) hist.getCounts();
		}
		return dCounts;
	}

	/*
	 * non-javadoc: A second histogram is needed
	 */
	private void enableInputWith(boolean state) {
		cfrom2.setEnabled(state);
		ttimes2.setEnabled(state);
		lWith.setEnabled(state);
	}

	/*
	 * non-javadoc: Set dialog box for new histogram to be created
	 */
	private void setUseHist(String name) {
		if (isNewHistogram(name)) {
			lname.setEnabled(true);
			ttextto.setEnabled(true);
		} else {
			lname.setEnabled(false);
			ttextto.setEnabled(false);

		}
	}

	/**
	 * Implementation of Observable interface listeners for broadcast events.
	 * broadcast events where there are new histograms or histograms added.
	 */
	public void update(Observable observable, Object o) {
		final BroadcastEvent be = (BroadcastEvent) o;
		final BroadcastEvent.Command command = be.getCommand();
		if (command == BroadcastEvent.Command.HISTOGRAM_NEW
				|| command == BroadcastEvent.Command.HISTOGRAM_ADD) {
			doSetup();
		}
	}

	boolean validateFactors() throws DataException {
		try {// read information for first histogram
			fac1 = Double.valueOf(ttimes1.getText().trim()).doubleValue();
		} catch (NumberFormatException nfe) {
			throw new DataException(
					"First factor is not a valid number [Manipulations]");
		}
		try {// read in information for second histogram
			fac2 = Double.valueOf(ttimes2.getText().trim()).doubleValue();
		} catch (NumberFormatException nfe) {
			throw new DataException(
					"Second factor is not a valid number [Manipulations]");
		}
		return true;
	}

}