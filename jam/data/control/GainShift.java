package jam.data.control;

import jam.data.AbstractHist1D;
import jam.data.DataException;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.MessageHandler;
import jam.ui.HistogramComboBoxModel;
import jam.ui.PanelOKApplyCancelButtons;

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
import java.util.Observer;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Class for adjusting the gain of 1d spectra.
 * 
 * @author Dale Visser, Ken Swartz
 * @version JDK 1.1
 */
public class GainShift extends AbstractManipulation implements ItemListener,
		Observer {

	private final JCheckBox cchan, ccoeff;

	private final JComboBox cfrom;

	private double chan1i, chan2i, chan1f, chan2f, a1, b1, a2, b2;

	private final JComboBox cto;

	private AbstractHist1D hfrom;

	private AbstractHist1D hto;

	private final JLabel label1, label2, label3, label4;

	private final JLabel lname;

	private final MessageHandler messageHandler;

	private final JTextField text1, text2, text3, text4, ttextto;

	/**
	 * Constructs a gain shift dialog.
	 * 
	 * @param mh
	 *            where to print messages
	 */
	public GainShift(MessageHandler mh) {
		super("Gain Shift 1-D Histogram", false);
		chan1i = 0.0;
		chan2i = 1.0;
		chan1f = 0.0;
		chan2f = 1.0;
		messageHandler = mh;
		int hgap = 5;
		int vgap = 10;
		int meanCharWidth;
		Dimension dim;
		// UI Layout
		setResizable(false);
		final Container cdgain = getContentPane();
		cdgain.setLayout(new BorderLayout(hgap, vgap));
		setLocation(20, 50);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}

			public void windowOpened(WindowEvent e) {
				doSetup();
			}
		});
		// Labels panel
		JPanel pLabels = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pLabels.setBorder(new EmptyBorder(20, 10, 0, 0));
		cdgain.add(pLabels, BorderLayout.WEST);
		pLabels.add(new JLabel("Shift histogram", JLabel.RIGHT));
		pLabels.add(new JLabel("Using", JLabel.RIGHT));
		label1 = new JLabel("", JLabel.RIGHT); // set by setUILabels
		pLabels.add(label1);
		label3 = new JLabel("", JLabel.RIGHT); // set by setUILabels
		pLabels.add(label3);
		pLabels.add(new JLabel("To  histogram", JLabel.RIGHT));
		// Entries Panel
		JPanel pEntries = new JPanel(new GridLayout(0, 1, hgap, vgap));
		pEntries.setBorder(new EmptyBorder(20, 0, 0, 10));
		cdgain.add(pEntries, BorderLayout.CENTER);
		JPanel pfrom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		cfrom = new JComboBox(new HistogramComboBoxModel(
				HistogramComboBoxModel.Mode.ONE_D));
		meanCharWidth = getMeanCharWidth(cfrom.getFontMetrics(cfrom.getFont()));
		dim = cfrom.getPreferredSize();
		dim.width = CHAR_LENGTH * meanCharWidth;
		cfrom.setPreferredSize(dim);
		cfrom.setEditable(false);
		pfrom.add(cfrom);
		pEntries.add(pfrom);
		JPanel pradio = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		ButtonGroup cbg = new ButtonGroup();
		cchan = new JCheckBox("Channels", false);
		cbg.add(cchan);
		cchan.addItemListener(this);
		ccoeff = new JCheckBox("Coeffiecients", true);
		cbg.add(ccoeff);
		ccoeff.addItemListener(this);
		pradio.add(cchan);
		pradio.add(ccoeff);
		pEntries.add(pradio);
		JPanel pinfields = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		text1 = new JTextField("0.0", 8);
		pinfields.add(text1);
		label2 = new JLabel(""); // set by setUILabels
		pinfields.add(label2);
		text2 = new JTextField("1.0", 8);
		pinfields.add(text2);
		pEntries.add(pinfields);
		JPanel poutfields = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		text3 = new JTextField("0.0", 8);
		poutfields.add(text3);
		label4 = new JLabel(""); // set by setUILabels
		poutfields.add(label4);
		text4 = new JTextField("1.0", 8);
		poutfields.add(text4);
		pEntries.add(poutfields);
		JPanel pto = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
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
		final PanelOKApplyCancelButtons pButtons = new PanelOKApplyCancelButtons(
				new PanelOKApplyCancelButtons.DefaultListener(this) {
					public void apply() {
						try {
							doGainShift();
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
		cdgain.add(pButtons.getComponent(), BorderLayout.SOUTH);
		pack();
		cfrom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object selected = cfrom.getSelectedItem();
				if (selected == null || selected instanceof String) {
					hfrom = null;
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
		chan1f = (a1 + b1 * chan1i - a2) / b2;
		chan2f = (a1 + b1 * chan2i - a2) / b2;
	}

	/**
	 * calculate coeff. if given channels
	 */
	private void calculateCoefficients() {

		a1 = 0.0; // if using channels, gain just gives channel
		b1 = 1.0; // see line above
		a2 = (chan2f * chan1i - chan1f * chan2i) / (chan2f - chan1f);
		if (chan1f != 0.0) {// avoid divide by zero errors
			b2 = (chan1i - a2) / chan1f;
		} else {
			b2 = (chan2i - a2) / chan2f;
		}

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
		/* Get input histogram. */
		final double[] in = (hfrom.getType() == Histogram.Type.ONE_DIM_INT) ? intToDoubleArray((int[]) hfrom
				.getCounts())
				: (double[]) hfrom.getCounts();
		final double[] errIn = hfrom.getErrors();
		/* Get or create output histogram. */
		String name = (String) cto.getSelectedItem();

		if (isNewHistogram(name)) {
			String histName = ttextto.getText().trim();
			String groupName = parseGroupName(name);
			hto = (AbstractHist1D) createNewHistogram(groupName, histName,
					hfrom.getSizeX());
			messageHandler.messageOutln("New Histogram created: '" + groupName
					+ "/" + histName + "'");

		} else {
			hto = (AbstractHist1D) Histogram.getHistogram(name);

		}
		hto.setZero();
		final int countLen = hto.getType() == Histogram.Type.ONE_DIM_INT ? ((int[]) hto
				.getCounts()).length
				: ((double[]) hto.getCounts()).length;
		final double[] out = gainShift(in, a1, b1, a2, b2, countLen);
		final double[] errOut = errorGainShift(errIn, a1, b1, a2, b2, hto
				.getErrors().length);
		if (hto.getType() == Histogram.Type.ONE_DIM_INT) {
			hto.setCounts(doubleToIntArray(out));
		} else {
			hto.setCounts(out);
		}
		hto.setErrors(errOut);

		messageHandler.messageOutln("Gain shift " + hfrom.getFullName().trim()
				+ " with gain: " + format(a1) + " + " + format(b1)
				+ " x ch; to " + hto.getFullName() + " with gain: "
				+ format(a2) + " + " + format(b2) + " x ch");
	}

	/**
	 * Loads the list of gates and set co-ordinates as x y if 2d or lower upper
	 * if 1 d
	 * 
	 */
	public void doSetup() {
		String lto = (String) cto.getSelectedItem();
		cto.removeAllItems();
		loadAllHists(cto, true, Histogram.Type.ONE_D);
		cto.setSelectedItem(lto);
		setUseHist((String) cto.getSelectedItem());
		cfrom.setSelectedIndex(0);
	}

	/**
	 * Error terms gain-shifting subroutine adapted from Fortran code written
	 * and used at the Nuclear Physics Laboratory at University of Washigton,
	 * Seattle.
	 * 
	 * @param y1
	 *            input array of counts
	 * @param constIn
	 *            constant calibration coefficient of y1
	 * @param slopeIn
	 *            linear calibration coefficient of y1
	 * @param constOut
	 *            constant calibration coefficient for output array
	 * @param slopeOut
	 *            linear calibration coefficient for output array
	 * @param npts2
	 *            desired size of output array
	 * @return new array of size <code>npts</code> re-binned for new gain
	 *         coefficients
	 */
	private double[] errorGainShift(double[] y1, double constIn, double slopeIn,
			double constOut, double slopeOut, int npts2) throws DataException {
		int i, n;
		double[] y2;
		double e1lo, e1hi, x2lo, x2hi;
		int mlo, mhi;
		y2 = new double[npts2];//lang spec says elements init to zero
		for (n = 0; n < y1.length; n++) {
			e1lo = constIn + slopeIn * (n - 0.5); // energy at lower edge of spec#1
			// channel
			e1hi = constIn + slopeIn * (n + 0.5); // energy at upper edge of spec#1
			// channel
			x2lo = (e1lo - constOut) / slopeOut; // fractional chan#2 corresponding to
			// e1lo
			x2hi = (e1hi - constOut) / slopeOut; // fractional chan#2 corresponding to
			// e1hi
			mlo = (int) (x2lo + 0.5);
			mhi = (int) (x2hi + 0.5);
			if (mlo < 0)
				mlo = 0;
			if (mhi < 0)
				mhi = 0;
			if (mlo >= npts2)
				mlo = npts2 - 1;
			if (mhi >= npts2)
				mhi = npts2 - 1;
			if ((mlo >= 0) && (mhi < npts2)) {
				if (mhi == mlo) { // sp#1 chan fits within one sp#2 chan
					y2[mlo] = y2[mlo] + y1[n];
				} else if (mhi == mlo + 1) { // sp#1 chan falls into two sp#2
					// chans
					y2[mlo] = Math.sqrt(y2[mlo]
							* y2[mlo]
							+ Math.pow(y1[n] * (mlo + 0.5 - x2lo)
									/ (x2hi - x2lo), 2.0));
					y2[mhi] = y2[mhi] + y1[n] * (x2hi - mhi + 0.5)
							/ (x2hi - x2lo);
				} else if (mhi > mlo + 1) { // sp#1 chan covers several sp#2
					// chans
					for (i = mlo + 1; i <= mhi - 1; i++) {
						y2[i] = y2[i] + y1[n] / (x2hi - x2lo);
					}
					y2[mlo] = y2[mlo] + y1[n] * (mlo + 0.5 - x2lo)
							/ (x2hi - x2lo);
					y2[mhi] = y2[mhi] + y1[n] * (x2hi - mhi + 0.5)
							/ (x2hi - x2lo);
				} else {
					throw new DataException("Something is wrong: n = " + n
							+ ", mlo = " + mlo + ", mhi = " + mhi);
				}
			}
		}
		return y2;
	}

	/*
	 * format a number
	 */
	private String format(double value) {
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
	 * @param y1
	 *            input array of counts
	 * @param constIn
	 *            constant calibration coefficient of y1
	 * @param slopeIn
	 *            linear calibration coefficient of y1
	 * @param constOut
	 *            constant calibration coefficient for output array
	 * @param slopeOut
	 *            linear calibration coefficient for output array
	 * @param npts2
	 *            desired size of output array
	 * @return new array of size <code>npts</code> re-binned for new gain
	 *         coefficients
	 */
	private double[] gainShift(double[] y1, double constIn, double slopeIn,
			double constOut, double slopeOut, int npts2) throws DataException {
		double[] y2;
		double e1lo, e1hi, x2lo, x2hi;
		int mlo, mhi;
		// create and zero new array
		y2 = new double[npts2];// language specifies elements initialized to
		// zero
		// loop for each channel of original array
		for (int n = 0; n < y1.length; n++) {
			e1lo = constIn + slopeIn * (n - 0.5); // energy at lower edge of
			// spec#1 channel
			e1hi = constIn + slopeIn * (n + 0.5); // energy at upper edge of
			// spec#1 channel
			x2lo = (e1lo - constOut) / slopeOut; // fractional chan#2
												// corresponding to
			// e1lo
			x2hi = (e1hi - constOut) / slopeOut; // fractional chan#2
												// corresponding to
			// e1hi
			mlo = (int) (x2lo + 0.5); // channel corresponding to x2low
			mhi = (int) (x2hi + 0.5); // channel corresponding to x2hi

			// if beyond limits of array set to limit.
			if (mlo < 0)
				mlo = 0;
			if (mhi < 0)
				mhi = 0;
			if (mlo >= npts2)
				mlo = npts2 - 1;
			if (mhi >= npts2)
				mhi = npts2 - 1;

			// treat the 3 cases below
			if ((mlo >= 0) && (mhi < npts2)) {

				// sp#1 chan fits within one sp#2 chan
				if (mhi == mlo) {
					y2[mlo] = y2[mlo] + y1[n];

					// sp#1 chan falls into two sp#2 chans
				} else if (mhi == (mlo + 1)) {
					y2[mlo] = y2[mlo] + y1[n] * (mlo + 0.5 - x2lo)
							/ (x2hi - x2lo);
					y2[mhi] = y2[mhi] + y1[n] * (x2hi - mhi + 0.5)
							/ (x2hi - x2lo);

					// sp#1 chan covers several sp#2 chans
				} else if (mhi > mlo + 1) {
					for (int i = mlo + 1; i <= mhi - 1; i++) {
						y2[i] = y2[i] + y1[n] / (x2hi - x2lo);
					}
					y2[mlo] = y2[mlo] + y1[n] * (mlo + 0.5 - x2lo)
							/ (x2hi - x2lo);
					y2[mhi] = y2[mhi] + y1[n] * (x2hi - mhi + 0.5)
							/ (x2hi - x2lo);
				} else {
					throw new DataException("Something is wrong: n = " + n
							+ ", mlo = " + mlo + ", mhi = " + mhi);
				}
			}
		}
		// test debug
		double sum = 0;
		for (int i = 0; i < y2.length; i++) {
			sum = sum + y2[i];
		}

		return y2;
	}

	/*
	 * non-javadoc: get the channels from the text fields
	 */
	private void getChannels() throws DataException {
		try {
			chan1i = Double.valueOf(text1.getText().trim()).doubleValue();
			chan1f = Double.valueOf(text2.getText().trim()).doubleValue();
			chan2i = Double.valueOf(text3.getText().trim()).doubleValue();
			chan2f = Double.valueOf(text4.getText().trim()).doubleValue();
		} catch (NumberFormatException nfe) {
			throw new DataException(
					"A Channel is not a valid number [GainShift]");
		}
	}

	/*
	 * non-javadoc: get the coeff. from the text fields
	 */
	private void getCoefficients() throws DataException {
		try {
			a1 = Double.valueOf(text1.getText().trim()).doubleValue();
			b1 = Double.valueOf(text2.getText().trim()).doubleValue();
			a2 = Double.valueOf(text3.getText().trim()).doubleValue();
			b2 = Double.valueOf(text4.getText().trim()).doubleValue();
		} catch (NumberFormatException nfe) {
			throw new DataException(
					"A Coefficient is not a valid number [GainShift]");
		}

	}

	/**
	 * A item state change indicates that a gate has been chosen.
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == ccoeff || ie.getSource() == cchan) {
			try {
				setUseCoeff(ccoeff.isSelected());
			} catch (DataException de) {
				messageHandler.errorOutln(de.getMessage());
			}
		} else if (ie.getSource() == cto) {
			if (cto.getSelectedItem() != null) {
				setUseHist((String) cto.getSelectedItem());
			}
		}
	}

	private double log10(double x) {
		return Math.log(x) / Math.log(10.0);
	}

	/*
	 * non-javadoc: Change the label in the UI depending on the gain shift type.
	 */
	private void setUILabels(boolean state) {
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

	private void setUseCoeff(boolean state) throws DataException {
		if (state) {
			setUILabels(state);
			getChannels();
			calculateCoefficients();
			text1.setText(format(a1));
			text2.setText(format(b1));
			text3.setText(format(a2));
			text4.setText(format(b2));
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
	private void setUseHist(String name) {
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
	public void update(Observable observable, Object o) {
		final BroadcastEvent be = (BroadcastEvent) o;
		final BroadcastEvent.Command com = be.getCommand();
		if (com == BroadcastEvent.Command.HISTOGRAM_NEW
				|| com == BroadcastEvent.Command.HISTOGRAM_ADD) {
			doSetup();
		}
	}

}