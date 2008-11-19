package jam.data.control;

import jam.data.AbstractHist1D;
import jam.data.AbstractHistogram;
import jam.data.DataException;
import jam.data.DataUtility;
import jam.data.HistDouble1D;
import jam.data.HistInt1D;
import jam.data.HistogramType;
import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.ui.PanelOKApplyCancelButtons;
import jam.ui.SelectionTree;
import jam.util.NumberUtilities;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;

/**
 * Combine histograms and also normalize a histogram
 * 
 * @author Dale Visser, Ken Swartz
 */
public class Combine extends AbstractManipulation implements Observer {

	private transient final JComboBox cfrom1, cfrom2, cto;

	private transient JCheckBox cnorm;

	private transient final JCheckBox cplus, cminus, ctimes, cdiv;

	private transient double fac1;

	private transient double fac2;

	private transient AbstractHist1D hto;

	private transient final JLabel lname, lWith;

	private transient final JTextField ttextto, ttimes1, ttimes2;

	/**
	 * Construct a new "manipulate histograms" dialog.
	 * 
	 * @param status
	 *            application status
	 * 
	 * @param console
	 *            where to print messages
	 */
	@Inject
	public Combine(final JamStatus status) {
		super("Manipulate 1-D Histograms", false);
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
		final JPanel pLabels = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pLabels.setBorder(new EmptyBorder(10, 10, 0, 0));
		cdmanip.add(pLabels, BorderLayout.WEST);
		lWith = new JLabel("With histogram", SwingConstants.RIGHT);
		pLabels.add(new JLabel("From  histogram", SwingConstants.RIGHT));
		pLabels.add(new JLabel("Operation", SwingConstants.RIGHT));
		pLabels.add(lWith);
		pLabels.add(new JLabel("To histogram", SwingConstants.RIGHT));

		// Entries Panel
		final JPanel pEntries = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pEntries.setBorder(new EmptyBorder(10, 0, 0, 10));
		cdmanip.add(pEntries, BorderLayout.CENTER);

		// From Panel
		final JPanel pfrom1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
		final JPanel pradio = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pradio);
		final ButtonGroup cbg = new ButtonGroup();

		addNormCheckbox(pradio, cbg);

		cplus = new JCheckBox("Add", false);
		cplus.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent event) {
				enableInputWith(true);
			}
		});
		cbg.add(cplus);
		pradio.add(cplus);

		cminus = new JCheckBox("Subtract", false);
		cminus.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent event) {
				enableInputWith(true);
			}
		});
		cbg.add(cminus);
		pradio.add(cminus);

		ctimes = new JCheckBox("Multiply", false);
		ctimes.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent event) {
				enableInputWith(true);
			}
		});
		cbg.add(ctimes);
		pradio.add(ctimes);

		cdiv = new JCheckBox("Divide", false);
		cdiv.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent event) {
				enableInputWith(true);
			}
		});
		cbg.add(cdiv);
		pradio.add(cdiv);

		// With panel
		final JPanel pfrom2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
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
		final JPanel pto = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pEntries.add(pto);
		cto = new JComboBox();
		meanCharWidth = getMeanCharWidth(cfrom1
				.getFontMetrics(cfrom1.getFont()));
		dim = cto.getPreferredSize();
		dim.width = CHAR_LENGTH * meanCharWidth;
		cto.setPreferredSize(dim);
		cto.addItem(NEW_HIST);
		cto.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent event) {
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
				new PanelOKApplyCancelButtons.AbstractListener(this) {
					public void apply() {
						try {
							combine();
							BROADCASTER
									.broadcast(BroadcastEvent.Command.REFRESH);
							SelectionTree.setCurrentHistogram(hto);
							status.setCurrentGroup(DataUtility.getGroup(hto));
							BROADCASTER.broadcast(
									BroadcastEvent.Command.HISTOGRAM_SELECT,
									hto);
						} catch (DataException je) {
							LOGGER.log(Level.SEVERE, je.getMessage(), je);
						}
					}
				});
		cdmanip.add(pButtons.getComponent(), BorderLayout.SOUTH);
		pack();
	}

	/**
	 * @param pradio
	 * @param cbg
	 */
	private void addNormCheckbox(final JPanel pradio, final ButtonGroup cbg) {
		cnorm = new JCheckBox("Renormalize", true);
		cnorm.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent event) {
				enableInputWith(false);
			}
		});
		cbg.add(cnorm);
		pradio.add(cnorm);
	}

	/*
	 * non-javadoc: Does the work of manipulating histograms
	 */
	private void combine() throws DataException {
		final double[] in1, err1;
		final double[] in2, err2;
		final double[] out, errOut;
		String operation = "";
		validateFactors();
		final AbstractHist1D hfrom1 = (AbstractHist1D) AbstractHistogram
				.getHistogram((String) cfrom1.getSelectedItem());
		AbstractHist1D hfrom2 = null;
		in1 = doubleCountsArray(hfrom1);
		err1 = hfrom1.getErrors();
		if (cfrom2.isEnabled()) {
			hfrom2 = (AbstractHist1D) AbstractHistogram
					.getHistogram((String) cfrom2.getSelectedItem());
			in2 = doubleCountsArray(hfrom2);
			err2 = hfrom2.getErrors();
		} else {
			in2 = null;
			err2 = null;
		}
		assignDestinationHistogram(hfrom1);
		hto.setZero();
		out = doubleCountsArray(hto);
		errOut = hto.getErrors();
		final int numChannels = getNumChannels(in1, in2, out);
		// Do calculation
		if (cnorm.isSelected()) {
			operation = normalize(in1, err1, out, errOut, numChannels);
		} else if (cplus.isSelected()) {
			operation = add(in1, err1, in2, err2, out, errOut, numChannels);
		} else if (cminus.isSelected()) {
			operation = subtract(in1, err1, in2, err2, out, errOut, numChannels);
		} else if (ctimes.isSelected()) {
			operation = multiply(in1, err1, in2, err2, out, errOut, numChannels);
		} else if (cdiv.isSelected()) {
			operation = divide(in1, err1, in2, err2, out, errOut, numChannels);
		}
		hto.setErrors(errOut);

		/* cast to int array if needed */
		if (hto.getType() == HistogramType.ONE_DIM_INT) {
			hto.setCounts(NumberUtilities.getInstance().doubleToIntArray(out));
		} else {
			hto.setCounts(out);
		}

		if (hfrom2 == null) {
			LOGGER.info("Normalize " + hfrom1.getFullName().trim() + " to "
					+ hto.getFullName());
		} else {
			LOGGER.info("Combine " + hfrom1.getFullName().trim() + operation
					+ hfrom2.getFullName().trim() + " to " + hto.getFullName());
		}
	}

	/**
	 * @param in1
	 * @param in2
	 * @param out
	 * @return
	 */
	private int getNumChannels(final double[] in1, final double[] in2,
			final double[] out) {
		// Minimum size of both out an in1
		int numChannels = Math.min(out.length, in1.length);
		// Minimum size of all out an in1 and in2
		if (!cnorm.isSelected()) {
			numChannels = Math.min(numChannels, in2.length);
		}
		return numChannels;
	}

	/**
	 * @param hfrom1
	 */
	private void assignDestinationHistogram(final AbstractHist1D hfrom1) {
		// read in information for to histogram
		final String name = (String) cto.getSelectedItem();
		if (isNewHistogram(name)) {
			final String histName = ttextto.getText().trim();
			final String groupName = parseGroupName(name);
			hto = (AbstractHist1D) createNewDoubleHistogram(groupName,
					histName, hfrom1.getSizeX());
			LOGGER.info("New Histogram created: '" + groupName + "/" + histName
					+ "'");
		} else {
			hto = (AbstractHist1D) AbstractHistogram.getHistogram(name);
		}
	}

	/**
	 * @param in1
	 * @param err1
	 * @param in2
	 * @param err2
	 * @param out
	 * @param errOut
	 * @param numChannels
	 * @return
	 */
	private String divide(final double[] in1, final double[] err1,
			final double[] in2, final double[] err2, final double[] out,
			final double[] errOut, final int numChannels) {
		String operation;
		for (int i = 0; i < numChannels; i++) {
			out[i] = fac1 * in1[i] / (fac2 * in2[i]);
			errOut[i] = in1[i]
					/ in2[i]
					* Math.sqrt(fac1 * fac1 / (err1[i] * err1[i]) + fac2 * fac2
							/ (err2[i] * err2[i]));
		}
		operation = " Divided by ";
		return operation;
	}

	/**
	 * @param in1
	 * @param err1
	 * @param in2
	 * @param err2
	 * @param out
	 * @param errOut
	 * @param numChannels
	 * @return
	 */
	private String multiply(final double[] in1, final double[] err1,
			final double[] in2, final double[] err2, final double[] out,
			final double[] errOut, final int numChannels) {
		String operation;
		for (int i = 0; i < numChannels; i++) {
			out[i] = fac1 * in1[i] * fac2 * in2[i];
			errOut[i] = Math.sqrt(fac2 * fac2 * err1[i] * err1[i] + fac1 * fac1
					* err2[i] * err2[i]);
		}
		operation = " Multiplied with ";
		return operation;
	}

	/**
	 * @param in1
	 * @param err1
	 * @param in2
	 * @param err2
	 * @param out
	 * @param errOut
	 * @param numChannels
	 * @return
	 */
	private String subtract(final double[] in1, final double[] err1,
			final double[] in2, final double[] err2, final double[] out,
			final double[] errOut, final int numChannels) {
		String operation;
		for (int i = 0; i < numChannels; i++) {
			out[i] = fac1 * in1[i] - fac2 * in2[i];
			errOut[i] = Math.sqrt(fac1 * fac1 * err1[i] * err1[i] + fac2 * fac2
					* err2[i] * err2[i]);
		}
		operation = " Subtracted from ";
		return operation;
	}

	/**
	 * @param in1
	 * @param err1
	 * @param in2
	 * @param err2
	 * @param out
	 * @param errOut
	 * @param numChannels
	 * @return
	 */
	private String add(final double[] in1, final double[] err1,
			final double[] in2, final double[] err2, final double[] out,
			final double[] errOut, final int numChannels) {
		String operation;
		for (int i = 0; i < numChannels; i++) {
			out[i] = fac1 * in1[i] + fac2 * in2[i];
			errOut[i] = Math.sqrt(fac1 * fac1 * err1[i] * err1[i] + fac2 * fac2
					* err2[i] * err2[i]);
		}
		operation = " Added with ";
		return operation;
	}

	/**
	 * @param in1
	 * @param err1
	 * @param out
	 * @param errOut
	 * @param numChannels
	 * @return
	 */
	private String normalize(final double[] in1, final double[] err1,
			final double[] out, final double[] errOut, final int numChannels) {
		String operation;
		for (int i = 0; i < numChannels; i++) {
			out[i] = fac1 * in1[i];
			errOut[i] = fac1 * err1[i];
		}
		operation = " Normalized ";
		return operation;
	}

	/**
	 * Loads the list of gates and set co-ordinates as x y if 2d or lower upper
	 * if 1 d
	 * 
	 */
	@Override
	public void doSetup() {
		String lfrom1, lfrom2, lto;

		lfrom1 = (String) cfrom1.getSelectedItem();
		lfrom2 = (String) cfrom2.getSelectedItem();
		lto = (String) cto.getSelectedItem();

		cfrom1.removeAllItems();
		loadAllHists(cfrom1, false, HistogramType.ONE_D);
		cfrom1.setSelectedItem(lfrom1);
		cfrom2.removeAllItems();
		loadAllHists(cfrom2, false, HistogramType.ONE_D);

		cfrom2.setSelectedItem(lfrom2);
		cto.removeAllItems();
		cto.addItem(NEW_HIST);
		loadAllHists(cto, true, HistogramType.ONE_D);
		cto.setSelectedItem(lto);
		setUseHist((String) cto.getSelectedItem());

		enableInputWith(!cnorm.isSelected());
	}

	private double[] doubleCountsArray(final AbstractHist1D hist) {
		double[] dCounts;
		if (hist.getType() == HistogramType.ONE_DIM_INT) {
			dCounts = NumberUtilities.getInstance().intToDoubleArray(
					((HistInt1D) hist).getCounts());
		} else {
			dCounts = ((HistDouble1D) hist).getCounts();
		}
		return dCounts;
	}

	/*
	 * non-javadoc: A second histogram is needed
	 */
	private void enableInputWith(final boolean state) {
		cfrom2.setEnabled(state);
		ttimes2.setEnabled(state);
		lWith.setEnabled(state);
	}

	/*
	 * non-javadoc: Set dialog box for new histogram to be created
	 */
	private void setUseHist(final String name) {
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
	@Override
	public void update(final Observable observable, final Object object) {
		final BroadcastEvent event = (BroadcastEvent) object;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.HISTOGRAM_NEW
				|| command == BroadcastEvent.Command.HISTOGRAM_ADD) {
			doSetup();
		}
	}

	protected boolean validateFactors() throws DataException {
		try {// read information for first histogram
			fac1 = Double.parseDouble(ttimes1.getText().trim());
		} catch (NumberFormatException nfe) {
			throw new DataException("First factor is not a valid number.", nfe);
		}
		try {// read in information for second histogram
			fac2 = Double.parseDouble(ttimes2.getText().trim());
		} catch (NumberFormatException nfe) {
			throw new DataException("Second factor is not a valid number.", nfe);
		}
		return true;
	}

}