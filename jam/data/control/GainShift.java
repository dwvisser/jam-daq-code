package jam.data.control;

import jam.data.AbstractHist1D;
import jam.data.DataException;
import jam.data.DataUtility;
import jam.data.AbstractHistogram;
import jam.data.HistogramType;
import jam.global.BroadcastEvent;
import jam.ui.SelectionTree;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.util.Observable;
import java.util.logging.Level;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Class for adjusting the gain of 1d spectra.
 * 
 * @author Dale Visser, Ken Swartz
 * @version JDK 1.1
 */
public class GainShift extends AbstractManipulation implements ItemListener {

	private transient final JCheckBox cchan, ccoeff;

	private transient final JComboBox cfrom;

	private transient double chan1i, chan2i, chan1f, chan2f;

	private transient final JComboBox cto;

	private transient AbstractHist1D hfrom;

	private transient AbstractHist1D hto;

	private transient double intercept1, slope1, intercept2, slope2;

	private final transient JLabel label1, label2, label3, label4;

	private transient final JLabel lname;

	private transient int mlo, mhi;

	private transient final JTextField text1, text2, text3, text4, ttextto;

	private transient double x2lo, x2hi;

	/**
	 * Constructs a gain shift dialog.
	 * 
	 * @param mh
	 *            where to print messages
	 */
	public GainShift() {
		super("Gain Shift 1-D Histogram", false);
		chan1i = 0.0;
		chan2i = 1.0;
		chan1f = 0.0;
		chan2f = 1.0;
		final int hgap = 5;
		final int vgap = 10;
		int meanCharWidth;
		Dimension dim;
		// UI Layout
		setResizable(false);
		final Container cdgain = getContentPane();
		cdgain.setLayout(new BorderLayout(hgap, vgap));
		setLocation(20, 50);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent event) {
				dispose();
			}

			@Override
			public void windowOpened(final WindowEvent event) {
				doSetup();
			}
		});
		// Labels panel
		final JPanel pLabels = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pLabels.setBorder(new EmptyBorder(20, 10, 0, 0));
		cdgain.add(pLabels, BorderLayout.WEST);
		pLabels.add(new JLabel("Shift histogram", SwingConstants.RIGHT));
		pLabels.add(new JLabel("Using", SwingConstants.RIGHT));
		label1 = new JLabel("", SwingConstants.RIGHT); // set by setUILabels
		pLabels.add(label1);
		label3 = new JLabel("", SwingConstants.RIGHT); // set by setUILabels
		pLabels.add(label3);
		pLabels.add(new JLabel("To  histogram", SwingConstants.RIGHT));
		// Entries Panel
		final JPanel pEntries = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pEntries.setBorder(new EmptyBorder(20, 0, 0, 10));
		cdgain.add(pEntries, BorderLayout.CENTER);
		final JPanel pfrom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		cfrom = new JComboBox(new HistogramComboBoxModel(
				HistogramComboBoxModel.Mode.ONE_D));
		meanCharWidth = getMeanCharWidth(cfrom.getFontMetrics(cfrom.getFont()));
		dim = cfrom.getPreferredSize();
		dim.width = CHAR_LENGTH * meanCharWidth;
		cfrom.setPreferredSize(dim);
		cfrom.setEditable(false);
		pfrom.add(cfrom);
		pEntries.add(pfrom);
		final JPanel pradio = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		final ButtonGroup cbg = new ButtonGroup();
		cchan = new JCheckBox("Channels", false);
		cbg.add(cchan);
		cchan.addItemListener(this);
		ccoeff = new JCheckBox("Coeffiecients", true);
		cbg.add(ccoeff);
		ccoeff.addItemListener(this);
		pradio.add(cchan);
		pradio.add(ccoeff);
		pEntries.add(pradio);
		final JPanel pinfields = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,
				0));
		text1 = new JTextField("0.0", 8);
		pinfields.add(text1);
		label2 = new JLabel(""); // set by setUILabels
		pinfields.add(label2);
		text2 = new JTextField("1.0", 8);
		pinfields.add(text2);
		pEntries.add(pinfields);
		final JPanel poutfields = new JPanel(new FlowLayout(FlowLayout.LEFT,
				10, 0));
		text3 = new JTextField("0.0", 8);
		poutfields.add(text3);
		label4 = new JLabel(""); // set by setUILabels
		poutfields.add(label4);
		text4 = new JTextField("1.0", 8);
		poutfields.add(text4);
		pEntries.add(poutfields);
		final JPanel pto = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		cto = new JComboBox();
		meanCharWidth = getMeanCharWidth(cto.getFontMetrics(cto.getFont()));
		dim = cto.getPreferredSize();
		dim.width = CHAR_LENGTH * meanCharWidth;
		cto.setPreferredSize(dim);
		cto.addItem("New Histogram");
		cto.addItemListener(this);
		pto.add(cto);
		lname = new JLabel("Name");
		pto.add(lname);
		ttextto = new JTextField("gainshift", TEXT_LENGTH);
		pto.add(ttextto);
		pEntries.add(pto);
		/* button panel */
		final jam.ui.PanelOKApplyCancelButtons pButtons = new jam.ui.PanelOKApplyCancelButtons(
				new jam.ui.PanelOKApplyCancelButtons.AbstractListener(this) {
					public void apply() {
						try {
							doGainShift();
							BROADCASTER
									.broadcast(BroadcastEvent.Command.REFRESH);
							SelectionTree.setCurrentHistogram(hto);
							STATUS.setCurrentGroup(DataUtility.getGroup(hto));
							BROADCASTER.broadcast(
									BroadcastEvent.Command.HISTOGRAM_SELECT,
									hto);
						} catch (DataException je) {
							LOGGER.log(Level.SEVERE, je.getMessage(), je);
						}
					}
				});
		cdgain.add(pButtons.getComponent(), BorderLayout.SOUTH);
		pack();
		cfrom.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				final Object selected = cfrom.getSelectedItem();
				if (selected == null || selected instanceof String) {
					hfrom = null; // NOPMD
					pButtons.setButtonsEnabled(false, false, true);
				} else {
					hfrom = (AbstractHist1D) selected;
					pButtons.setButtonsEnabled(true, true, true);
				}
			}
		});
		cfrom.setSelectedIndex(0);
		pack();
		setUILabels(true);
	}

	/**
	 * calculate channels if given coeff.
	 */
	private void calculateChannels() {
		chan1f = (intercept1 + slope1 * chan1i - intercept2) / slope2;
		chan2f = (intercept1 + slope1 * chan2i - intercept2) / slope2;
	}

	/**
	 * calculate coeff. if given channels
	 */
	private void calculateCoefficients() {

		intercept1 = 0.0; // if using channels, gain just gives channel
		slope1 = 1.0; // see line above
		intercept2 = (chan2f * chan1i - chan1f * chan2i) / (chan2f - chan1f);
		if (chan1f == 0.0) {// avoid divide by zero errors
			slope2 = (chan2i - intercept2) / chan2f;
		} else {
			slope2 = (chan1i - intercept2) / chan1f;
		}

	}

	/**
	 * @param countsIn
	 * @param countsOut
	 * @param index
	 */
	private void calculateCountsInsideRange(final double[] countsIn,
			double[] countsOut, final int index) {
		for (int i = mlo + 1; i <= mhi - 1; i++) {
			countsOut[i] = countsOut[i] + countsIn[index] / (x2hi - x2lo);
		}
		countsOut[mlo] = countsOut[mlo] + countsIn[index] * (mlo + 0.5 - x2lo)
				/ (x2hi - x2lo);
		countsOut[mhi] = countsOut[mhi] + countsIn[index] * (x2hi - mhi + 0.5)
				/ (x2hi - x2lo);
	}

	/**
	 * @param interceptIn
	 * @param slopeIn
	 * @param interceptOut
	 * @param slopeOut
	 * @param npts2
	 * @param index
	 */
	private void calculateIntermediateValues(final double interceptIn,
			final double slopeIn, final double interceptOut,
			final double slopeOut, final int npts2, final int index) {
		final double e1lo = interceptIn + slopeIn * (index - 0.5); // energy at
		// lower
		// edge
		// of
		// spec#1
		// channel
		final double e1hi = interceptIn + slopeIn * (index + 0.5); // energy at
		// upper
		// edge
		// of
		// spec#1
		// channel
		x2lo = (e1lo - interceptOut) / slopeOut; // fractional chan#2
		// corresponding to
		// e1lo
		x2hi = (e1hi - interceptOut) / slopeOut; // fractional chan#2
		// corresponding to
		// e1hi
		mlo = (int) (x2lo + 0.5);
		mhi = (int) (x2hi + 0.5);
		mlo = Math.max(mlo, 0);
		mhi = Math.max(mhi, 0);
		mlo = Math.min(mlo, npts2 - 1);
		mhi = Math.min(mhi, npts2 - 1);
	}

	/*
	 * non-javadoc: Does the work of manipulating histograms
	 */
	private void doGainShift() throws DataException {
		/* Get coefficients or channels. */
		if (cchan.isSelected()) {
			getChannels();
			calculateCoefficients();
		} else {
			getCoefficients();
		}
		final jam.util.NumberUtilities numbers = jam.util.NumberUtilities
				.getInstance();
		/* Get input histogram. */
		final HistogramType oneDi = HistogramType.ONE_DIM_INT;
		final double[] countsIn = (hfrom.getType() == oneDi) ? numbers
				.intToDoubleArray(((jam.data.HistInt1D) hfrom).getCounts())
				: ((jam.data.HistDouble1D) hfrom).getCounts();
		final double[] errIn = hfrom.getErrors();
		getOrCreateOutputHistogram();
		hto.setZero();
		final int countLen = hto.getType() == oneDi ? ((jam.data.HistInt1D) hto)
				.getCounts().length
				: ((jam.data.HistDouble1D) hto).getCounts().length;
		final double[] out = gainShift(countsIn, intercept1, slope1,
				intercept2, slope2, countLen);
		final double[] errOut = errorGainShift(errIn, intercept1, slope1,
				intercept2, slope2, hto.getErrors().length);
		if (hto.getType() == oneDi) {
			hto.setCounts(numbers.doubleToIntArray(out));
		} else {
			hto.setCounts(out);
		}
		hto.setErrors(errOut);

		LOGGER.info("Gain shift " + hfrom.getFullName().trim() + " with gain: "
				+ format(intercept1) + " + " + format(slope1) + " x ch; to "
				+ hto.getFullName() + " with gain: " + format(intercept2)
				+ " + " + format(slope2) + " x ch");
	}

	private void getOrCreateOutputHistogram() {
		final String name = (String) cto.getSelectedItem();
		if (isNewHistogram(name)) {
			final String histName = ttextto.getText().trim();
			final String groupName = parseGroupName(name);
			hto = (AbstractHist1D) createNewDoubleHistogram(groupName,
					histName, hfrom.getSizeX());
			LOGGER.info("New Histogram created: '" + groupName + "/" + histName
					+ "'");
		} else {
			hto = (AbstractHist1D) AbstractHistogram.getHistogram(name);
		}
	}

	/**
	 * Loads the list of gates and set co-ordinates as x y if 2d or lower upper
	 * if 1 d
	 * 
	 */
	@Override
	public void doSetup() {
		final String lto = (String) cto.getSelectedItem();
		cto.removeAllItems();
		loadAllHists(cto, true, HistogramType.ONE_D);
		cto.setSelectedItem(lto);
		setUseHist((String) cto.getSelectedItem());
		cfrom.setSelectedIndex(0);
	}

	/**
	 * Error terms gain-shifting subroutine adapted from Fortran code written
	 * and used at the Nuclear Physics Laboratory at University of Washington,
	 * Seattle.
	 * 
	 * @param countsIn
	 *            input array of counts
	 * @param interceptIn
	 *            constant calibration coefficient of countsIn
	 * @param slopeIn
	 *            linear calibration coefficient of countsIn
	 * @param interceptOut
	 *            constant calibration coefficient for output array
	 * @param slopeOut
	 *            linear calibration coefficient for output array
	 * @param npts2
	 *            desired size of output array
	 * @return new array of size <code>npts</code> re-binned for new gain
	 *         coefficients
	 * @throws DataException
	 *             if there's a problem
	 */
	private double[] errorGainShift(final double[] countsIn,
			final double interceptIn, final double slopeIn,
			final double interceptOut, final double slopeOut, final int npts2)
			throws DataException {
		double[] countsOut = new double[npts2];
		for (int n = 0; n < countsIn.length; n++) {
			calculateIntermediateValues(interceptIn, slopeIn, interceptOut,
					slopeOut, npts2, n);
			if ((mlo >= 0) && (mhi < npts2)) {
				if (mhi == mlo) { // sp#1 chan fits within one sp#2 chan
					countsOut[mlo] = countsOut[mlo] + countsIn[n];
				} else if (mhi == mlo + 1) { // sp#1 chan falls into two sp#2
					// chans
					countsOut[mlo] = Math.sqrt(countsOut[mlo]
							* countsOut[mlo]
							+ Math.pow(countsIn[n] * (mlo + 0.5 - x2lo)
									/ (x2hi - x2lo), 2.0));
					countsOut[mhi] = countsOut[mhi] + countsIn[n]
							* (x2hi - mhi + 0.5) / (x2hi - x2lo);
				} else if (mhi > mlo + 1) { // sp#1 chan covers several sp#2
					// chans
					calculateCountsInsideRange(countsIn, countsOut, n);
				} else {
					throw new DataException("Something is wrong: n = " + n
							+ ", mlo = " + mlo + ", mhi = " + mhi);
				}
			}
		}
		return countsOut;
	}

	/*
	 * format a number
	 */
	private String format(final double value) {
		int integer, fraction;
		NumberFormat fval;

		integer = (int) log10(Math.abs(value));
		integer = Math.max(integer, 1);
		fraction = Math.max(7 - integer, 0);
		fval = NumberFormat.getInstance();
		fval.setGroupingUsed(false);
		fval.setMinimumFractionDigits(fraction);
		fval.setMinimumFractionDigits(fraction);
		fval.setMinimumIntegerDigits(integer);
		return fval.format(value);
	}

	/**
	 * Gain-shifting subroutine adapted from Fortran code written and used at
	 * the Nuclear Physics Laboratory at University of Washigton, Seattle.
	 * 
	 * @param countsIn
	 *            input array of counts
	 * @param interceptIn
	 *            constant calibration coefficient of countsIn
	 * @param slopeIn
	 *            linear calibration coefficient of countsIn
	 * @param interceptOut
	 *            constant calibration coefficient for output array
	 * @param slopeOut
	 *            linear calibration coefficient for output array
	 * @param npts2
	 *            desired size of output array
	 * @return new array of size <code>npts</code> re-binned for new gain
	 *         coefficients
	 * @throws DataException
	 *             if there's a problem
	 */
	private double[] gainShift(final double[] countsIn,
			final double interceptIn, final double slopeIn,
			final double interceptOut, final double slopeOut, final int npts2)
			throws DataException {
		double[] countsOut = new double[npts2];// language specifies elements
		// initialized to
		// zero
		// loop for each channel of original array
		for (int n = 0; n < countsIn.length; n++) {
			calculateIntermediateValues(interceptIn, slopeIn, interceptOut,
					slopeOut, npts2, n);
			// treat the 3 cases below
			if ((mlo >= 0) && (mhi < npts2)) {
				// sp#1 chan fits within one sp#2 chan
				if (mhi == mlo) {
					countsOut[mlo] = countsOut[mlo] + countsIn[n];
					// sp#1 chan falls into two sp#2 chans
				} else if (mhi == (mlo + 1)) {
					countsOut[mlo] = countsOut[mlo] + countsIn[n]
							* (mlo + 0.5 - x2lo) / (x2hi - x2lo);
					countsOut[mhi] = countsOut[mhi] + countsIn[n]
							* (x2hi - mhi + 0.5) / (x2hi - x2lo);
					// sp#1 chan covers several sp#2 chans
				} else if (mhi > mlo + 1) {
					calculateCountsInsideRange(countsIn, countsOut, n);
				} else {
					throw new DataException("Something is wrong: n = " + n
							+ ", mlo = " + mlo + ", mhi = " + mhi);
				}
			}
		}
		// test debug
		double sum = 0;
		for (int i = 0; i < countsOut.length; i++) {
			sum = sum + countsOut[i];
		}

		return countsOut;
	}

	/*
	 * non-javadoc: get the channels from the text fields
	 */
	private void getChannels() throws DataException {
		try {
			chan1i = Double.parseDouble(text1.getText().trim());
			chan1f = Double.parseDouble(text2.getText().trim());
			chan2i = Double.parseDouble(text3.getText().trim());
			chan2f = Double.parseDouble(text4.getText().trim());
		} catch (NumberFormatException nfe) {
			throw new DataException("A Channel is not a valid number.", nfe);
		}
	}

	/*
	 * non-javadoc: get the coeff. from the text fields
	 */
	private void getCoefficients() throws DataException {
		try {
			intercept1 = Double.parseDouble(text1.getText().trim());
			slope1 = Double.parseDouble(text2.getText().trim());
			intercept2 = Double.parseDouble(text3.getText().trim());
			slope2 = Double.parseDouble(text4.getText().trim());
		} catch (NumberFormatException nfe) {
			throw new DataException(
					"A Coefficient is not a valid number [GainShift]", nfe);
		}

	}

	/**
	 * A item state change indicates that a gate has been chosen.
	 */
	public void itemStateChanged(final ItemEvent event) {
		if (event.getSource() == ccoeff || event.getSource() == cchan) {
			try {
				setUseCoeff(ccoeff.isSelected());
			} catch (DataException de) {
				LOGGER.log(Level.SEVERE, de.getMessage(), de);
			}
		} else if (event.getSource() == cto && (cto.getSelectedItem() != null)) {
			setUseHist((String) cto.getSelectedItem());
		}
	}

	private double log10(final double arg) {
		return Math.log(arg) / Math.log(10.0);
	}

	/*
	 * non-javadoc: Change the label in the UI depending on the gain shift type.
	 */
	private void setUILabels(final boolean state) {
		if (state) {
			label1.setText("Input Gain");
			label2.setText("+ channel x");
			label3.setText("Output Gain");
			label4.setText("+ channel x");
		} else {
			label1.setText("Map Channel");
			label2.setText("to");
			label3.setText("Map Channel");
			label4.setText("to");
		}
	}

	private void setUseCoeff(final boolean state) throws DataException {
		if (state) {
			setUILabels(state);
			getChannels();
			calculateCoefficients();
			text1.setText(format(intercept1));
			text2.setText(format(slope1));
			text3.setText(format(intercept2));
			text4.setText(format(slope2));
		} else {
			setUILabels(state);
			getCoefficients();
			calculateChannels();
			text1.setText(format(chan1i));
			text2.setText(format(chan1f));
			text3.setText(format(chan2i));
			text4.setText(format(chan2f));
		}
	}

	/*
	 * non-javadoc: setup if using a new histogram
	 */
	private void setUseHist(final String name) {
		if (isNewHistogram(name)) {
			lname.setEnabled(true);
			ttextto.setEnabled(true);
			ttextto.setEditable(true);
		} else {
			lname.setEnabled(false);
			ttextto.setEnabled(false);
			ttextto.setEditable(false);

		}
	}

	/**
	 * Implementation of Observable interface to receive broad cast events.
	 * Listen for histograms new, histogram added
	 */
	@Override
	public void update(final Observable observable, final Object event) {
		final BroadcastEvent jamEvent = (BroadcastEvent) event;
		final BroadcastEvent.Command com = jamEvent.getCommand();
		if (com == BroadcastEvent.Command.HISTOGRAM_NEW
				|| com == BroadcastEvent.Command.HISTOGRAM_ADD) {
			doSetup();
		}
	}

}