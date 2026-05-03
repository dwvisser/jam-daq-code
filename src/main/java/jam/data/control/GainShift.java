package jam.data.control;

import com.google.inject.Inject;
import jam.data.AbstractHist1D;
import jam.data.AbstractHistogram;
import jam.data.DataException;
import jam.data.DataUtility;
import jam.data.HistogramType;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamStatus;
import jam.ui.PanelOKApplyCancelButtons;
import jam.ui.SelectionTree;
import jam.util.NumberUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.text.NumberFormat;
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

  private static final double BIN_CENTER_OFFSET = 0.5;

  private final transient JCheckBox channels;
  private final transient JCheckBox coefficients;
  private final transient JComboBox<Object> from;
  private final transient JComboBox<Object> to;
  private transient double chan1i;
  private transient double chan2i;
  private transient double chan1f;
  private transient double chan2f;

  private transient AbstractHist1D from_histogram;

  private transient AbstractHist1D to_histogram;

  private transient double intercept1;
  private transient double slope1;
  private transient double intercept2;
  private transient double slope2;
  private final transient JLabel label1;
  private final transient JLabel label2;
  private final transient JLabel label3;
  private final transient JLabel label4;

  private final transient JLabel lname;

  private transient int mlo;
  private transient int mhi;
  private final transient JTextField text1;
  private final transient JTextField text2;
  private final transient JTextField text3;
  private final transient JTextField text4;
  private final transient JTextField ttextto;
  private transient double x2lo;
  private transient double x2hi;

  private final transient NumberUtilities numberUtilities;

  /**
   * Constructs a gain shift dialog.
   *
   * @param frame application frame
   * @param status application status
   * @param broadcaster broadcasts state changes
   * @param numberUtilities number utility object
   */
  @Inject
  public GainShift(
      final Frame frame,
      final JamStatus status,
      final Broadcaster broadcaster,
      final NumberUtilities numberUtilities) {
    super(frame, "Gain Shift 1-D Histogram", false, broadcaster);
    this.numberUtilities = numberUtilities;
    chan1i = 0.0;
    chan2i = 1.0;
    chan1f = 0.0;
    chan2f = 1.0;
    int meanCharWidth;
    Dimension dim;
    // UI Layout
    setResizable(false);
    final var contents = getContentPane();
    final int horizontalGap = 5;
    final int verticalGap = 10;
    contents.setLayout(new BorderLayout(horizontalGap, verticalGap));
    setLocation(20, 50);
    addWindowListener(
        new WindowAdapter() {
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
    final JPanel pLabels = new JPanel(new GridLayout(0, 1, horizontalGap, verticalGap));
    int thickInset = 20;
    int thinInset = 10;
    int zeroInset = 0;
    pLabels.setBorder(new EmptyBorder(thickInset, thinInset, zeroInset, zeroInset));
    contents.add(pLabels, BorderLayout.WEST);
    pLabels.add(new JLabel("Shift histogram", SwingConstants.RIGHT));
    pLabels.add(new JLabel("Using", SwingConstants.RIGHT));
    label1 = new JLabel("", SwingConstants.RIGHT); // set by setUILabels
    pLabels.add(label1);
    label3 = new JLabel("", SwingConstants.RIGHT); // set by setUILabels
    pLabels.add(label3);
    pLabels.add(new JLabel("To  histogram", SwingConstants.RIGHT));
    // Entries Panel
    final var pEntries = new JPanel(new GridLayout(0, 1, horizontalGap, verticalGap));
    pEntries.setBorder(new EmptyBorder(thickInset, zeroInset, zeroInset, thinInset));
    contents.add(pEntries, BorderLayout.CENTER);
    final var fromPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    from = new JComboBox<>(new HistogramComboBoxModel(HistogramComboBoxModel.Mode.ONE_D));
    meanCharWidth = getMeanCharWidth(from.getFontMetrics(from.getFont()));
    dim = from.getPreferredSize();
    dim.width = CHAR_LENGTH * meanCharWidth;
    from.setPreferredSize(dim);
    from.setEditable(false);
    fromPanel.add(from);
    pEntries.add(fromPanel);
    final JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    final ButtonGroup cbg = new ButtonGroup();
    channels = new JCheckBox("Channels", false);
    cbg.add(channels);
    channels.addItemListener(this);
    coefficients = new JCheckBox("Coefficients", true);
    cbg.add(coefficients);
    coefficients.addItemListener(this);
    checkboxPanel.add(channels);
    checkboxPanel.add(coefficients);
    pEntries.add(checkboxPanel);
    final var inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    int textColumns = 8;
    text1 = new JTextField("0.0", textColumns);
    inputPanel.add(text1);
    label2 = new JLabel(""); // set by setUILabels
    inputPanel.add(label2);
    text2 = new JTextField("1.0", textColumns);
    inputPanel.add(text2);
    pEntries.add(inputPanel);
    final var outputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    text3 = new JTextField("0.0", textColumns);
    outputPanel.add(text3);
    label4 = new JLabel(""); // set by setUILabels
    outputPanel.add(label4);
    text4 = new JTextField("1.0", textColumns);
    outputPanel.add(text4);
    pEntries.add(outputPanel);
    final JPanel pto = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    to = new JComboBox<>();
    meanCharWidth = getMeanCharWidth(to.getFontMetrics(to.getFont()));
    dim = to.getPreferredSize();
    dim.width = CHAR_LENGTH * meanCharWidth;
    to.setPreferredSize(dim);
    to.addItem("New Histogram");
    to.addItemListener(this);
    pto.add(to);
    lname = new JLabel("Name");
    pto.add(lname);
    ttextto = new JTextField("gainshift", TEXT_LENGTH);
    pto.add(ttextto);
    pEntries.add(pto);
    /* button panel */
    final PanelOKApplyCancelButtons pApply =
        new PanelOKApplyCancelButtons(
            new PanelOKApplyCancelButtons.AbstractListener(this) {
              public void apply() {
                try {
                  doGainShift();
                  broadcaster.broadcast(BroadcastEvent.Command.REFRESH);
                  SelectionTree.setCurrentHistogram(to_histogram);
                  status.setCurrentGroup(DataUtility.getGroup(to_histogram));
                  broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_SELECT, to_histogram);
                } catch (DataException je) {
                  LOGGER.log(Level.SEVERE, je.getMessage(), je);
                }
              }
            });
    contents.add(pApply.getComponent(), BorderLayout.SOUTH);
    pack();
    from.addActionListener(
        event -> {
          final Object selected = from.getSelectedItem();
          if (selected == null || selected instanceof String) {
            from_histogram = null;
            pApply.setButtonsEnabled(false, false, true);
          } else {
            from_histogram = (AbstractHist1D) selected;
            pApply.setButtonsEnabled(true, true, true);
          }
        });
    from.setSelectedIndex(0);
    pack();
    setUILabels(true);
  }

  /** calculate channels if given coefficients */
  private void calculateChannels() {
    chan1f = (intercept1 + slope1 * chan1i - intercept2) / slope2;
    chan2f = (intercept1 + slope1 * chan2i - intercept2) / slope2;
  }

  /** calculate coefficients if given channels */
  private void calculateCoefficients() {

    intercept1 = 0.0; // if using channels, gain just gives channel
    slope1 = 1.0; // see line above
    intercept2 = (chan2f * chan1i - chan1f * chan2i) / (chan2f - chan1f);
    if (chan1f == 0.0) { // avoid divide by zero errors
      slope2 = (chan2i - intercept2) / chan2f;
    } else {
      slope2 = (chan1i - intercept2) / chan1f;
    }
  }

  private void calculateCountsInsideRange(
      final double[] countsIn, final double[] countsOut, final int index) {
    for (int i = mlo + 1; i <= mhi - 1; i++) {
      countsOut[i] = countsOut[i] + countsIn[index] / (x2hi - x2lo);
    }
    countsOut[mlo] =
        countsOut[mlo] + countsIn[index] * (mlo + BIN_CENTER_OFFSET - x2lo) / (x2hi - x2lo);
    countsOut[mhi] =
        countsOut[mhi] + countsIn[index] * (x2hi - mhi + BIN_CENTER_OFFSET) / (x2hi - x2lo);
  }

  private void calculateIntermediateValues(
      final double interceptIn,
      final double slopeIn,
      final double interceptOut,
      final double slopeOut,
      final int size,
      final int index) {
    // energy at lower edge of spec#1 channel
    final double e1lo = interceptIn + slopeIn * (index - BIN_CENTER_OFFSET);

    // energy at upper edge of spec#1 channel
    final double e1hi = interceptIn + slopeIn * (index + BIN_CENTER_OFFSET);

    // fractional chan#2 corresponding to e1lo
    x2lo = (e1lo - interceptOut) / slopeOut;

    // fractional chan#2 corresponding to e1hi
    x2hi = (e1hi - interceptOut) / slopeOut;

    mlo = (int) (x2lo + BIN_CENTER_OFFSET);
    mhi = (int) (x2hi + BIN_CENTER_OFFSET);
    mlo = Math.max(mlo, 0);
    mhi = Math.max(mhi, 0);
    mlo = Math.min(mlo, size - 1);
    mhi = Math.min(mhi, size - 1);
  }

  /*
   * non-javadoc: Does the work of manipulating histograms
   */
  private void doGainShift() throws DataException {
    /* Get coefficients or channels. */
    if (channels.isSelected()) {
      getChannels();
      calculateCoefficients();
    } else {
      getCoefficients();
    }
    /* Get input histogram. */
    final HistogramType oneDi = HistogramType.ONE_DIM_INT;
    final boolean isOneD = from_histogram.getType() == oneDi;
    final double[] countsIn =
        isOneD
            ? this.numberUtilities.intToDoubleArray(
                ((jam.data.HistInt1D) from_histogram).getCounts())
            : ((jam.data.HistDouble1D) from_histogram).getCounts();
    final double[] errIn = from_histogram.getErrors();
    getOrCreateOutputHistogram();
    to_histogram.setZero();
    final int countLen =
        to_histogram.getType() == oneDi
            ? ((jam.data.HistInt1D) to_histogram).getCounts().length
            : ((jam.data.HistDouble1D) to_histogram).getCounts().length;
    final double[] out = gainShift(countsIn, intercept1, slope1, intercept2, slope2, countLen);
    final double[] errOut =
        errorGainShift(
            errIn, intercept1, slope1, intercept2, slope2, to_histogram.getErrors().length);
    if (to_histogram.getType() == oneDi) {
      to_histogram.setCounts(this.numberUtilities.doubleToIntArray(out));
    } else {
      to_histogram.setCounts(out);
    }
    to_histogram.setErrors(errOut);
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info( // NOPMD
          "Gain shift "
              + from_histogram.getFullName().trim()
              + " with gain: "
              + format(intercept1)
              + " + "
              + format(slope1)
              + " x ch; to "
              + to_histogram.getFullName()
              + " with gain: "
              + format(intercept2)
              + " + "
              + format(slope2)
              + " x ch");
    }
  }

  private void getOrCreateOutputHistogram() {
    final String name = (String) to.getSelectedItem();
    if (isNewHistogram(name)) {
      final String histName = ttextto.getText().trim();
      final String groupName = parseGroupName(name);
      to_histogram =
          (AbstractHist1D) createNewDoubleHistogram(groupName, histName, from_histogram.getSizeX());
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("New Histogram created: '" + groupName + "/" + histName + "'"); // NOPMD
      }
    } else {
      to_histogram = (AbstractHist1D) AbstractHistogram.getHistogram(name);
    }
  }

  /** Loads the list of gates and set co-ordinates as x y if 2d or lower upper if 1d. */
  @Override
  public void doSetup() {
    final String lto = (String) to.getSelectedItem();
    to.removeAllItems();
    loadAllHists(to, true, HistogramType.ONE_D);
    to.setSelectedItem(lto);
    setUseHist((String) to.getSelectedItem());
    from.setSelectedIndex(0);
  }

  /**
   * Error terms gain-shifting subroutine adapted from Fortran code written and used at the Nuclear
   * Physics Laboratory at University of Washington, Seattle.
   *
   * @param countsIn input array of counts
   * @param interceptIn constant calibration coefficient of countsIn
   * @param slopeIn linear calibration coefficient of countsIn
   * @param interceptOut constant calibration coefficient for output array
   * @param slopeOut linear calibration coefficient for output array
   * @param size desired size of output array
   * @return new array of size <code>size</code> re-binned for new gain coefficients
   * @throws DataException if there's a problem
   */
  private double[] errorGainShift(
      final double[] countsIn,
      final double interceptIn,
      final double slopeIn,
      final double interceptOut,
      final double slopeOut,
      final int size)
      throws DataException {
    final double[] countsOut = new double[size];
    for (int n = 0; n < countsIn.length; n++) {
      calculateIntermediateValues(interceptIn, slopeIn, interceptOut, slopeOut, size, n);
      if ((mlo >= 0) && (mhi < size)) {
        calculateErrorContribution(countsIn, countsOut, n);
      }
    }
    return countsOut;
  }

  private void calculateErrorContribution(
      final double[] countsIn, double[] countsOut, final int inChannel) throws DataException {
    if (mhi == mlo) {
      /* spectrum #1 channel fits within one spectrum #2 channel */
      countsOut[mlo] = countsOut[mlo] + countsIn[inChannel];
    } else if (mhi == mlo + 1) {
      /* spectrum #1 channel falls into two spectrum #2 channels */
      countsOut[mlo] =
          Math.sqrt(
              countsOut[mlo] * countsOut[mlo]
                  + Math.pow(
                      countsIn[inChannel] * (mlo + BIN_CENTER_OFFSET - x2lo) / (x2hi - x2lo), 2.0));
      countsOut[mhi] =
          countsOut[mhi] + countsIn[inChannel] * (x2hi - mhi + BIN_CENTER_OFFSET) / (x2hi - x2lo);
    } else if (mhi > mlo + 1) {
      /* spectrum #1 channel covers several spectrum #2 channels */
      calculateCountsInsideRange(countsIn, countsOut, inChannel);
    } else {
      throw new DataException(
          "Something is wrong: n = " + inChannel + ", mlo = " + mlo + ", mhi = " + mhi);
    }
  }

  /*
   * format a number
   */
  private String format(final double value) {
    int integer = (int) log10(Math.abs(value));
    integer = Math.max(integer, 1);
    final int fraction = Math.max(7 - integer, 0);
    var format = NumberFormat.getInstance();
    format.setGroupingUsed(false);
    format.setMinimumFractionDigits(fraction);
    format.setMinimumFractionDigits(fraction);
    format.setMinimumIntegerDigits(integer);
    return format.format(value);
  }

  /**
   * Gain-shifting subroutine adapted from Fortran code written and used at the Nuclear Physics
   * Laboratory at University of Washington, Seattle.
   *
   * @param countsIn input array of counts
   * @param interceptIn constant calibration coefficient of countsIn
   * @param slopeIn linear calibration coefficient of countsIn
   * @param interceptOut constant calibration coefficient for output array
   * @param slopeOut linear calibration coefficient for output array
   * @param size desired size of output array
   * @return new array of size <code>size</code> re-binned for new gain coefficients
   * @throws DataException if there's a problem
   */
  private double[] gainShift(
      final double[] countsIn,
      final double interceptIn,
      final double slopeIn,
      final double interceptOut,
      final double slopeOut,
      final int size)
      throws DataException {
    double[] countsOut = new double[size]; // language specifies elements
    // initialized to
    // zero
    // loop for each channel of original array
    for (int n = 0; n < countsIn.length; n++) {
      calculateIntermediateValues(interceptIn, slopeIn, interceptOut, slopeOut, size, n);
      // treat the 3 cases below
      if ((mlo >= 0) && (mhi < size)) {
        // sp#1 chan fits within one sp#2 chan
        if (mhi == mlo) {
          countsOut[mlo] = countsOut[mlo] + countsIn[n];
          // sp#1 chan falls into two sp#2 channels
        } else if (mhi == (mlo + 1)) {
          countsOut[mlo] =
              countsOut[mlo] + countsIn[n] * (mlo + BIN_CENTER_OFFSET - x2lo) / (x2hi - x2lo);
          countsOut[mhi] =
              countsOut[mhi] + countsIn[n] * (x2hi - mhi + BIN_CENTER_OFFSET) / (x2hi - x2lo);
          // sp#1 chan covers several sp#2 channels
        } else if (mhi > mlo + 1) {
          calculateCountsInsideRange(countsIn, countsOut, n);
        } else {
          throw new DataException(
              "Something is wrong: n = " + n + ", mlo = " + mlo + ", mhi = " + mhi);
        }
      }
    }
    // test debug
    double sum = 0;
    for (double aCountsOut : countsOut) {
      sum = sum + aCountsOut;
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
   * non-javadoc: get the coefficients from the text fields
   */
  private void getCoefficients() throws DataException {
    try {
      intercept1 = Double.parseDouble(text1.getText().trim());
      slope1 = Double.parseDouble(text2.getText().trim());
      intercept2 = Double.parseDouble(text3.getText().trim());
      slope2 = Double.parseDouble(text4.getText().trim());
    } catch (NumberFormatException nfe) {
      throw new DataException("A Coefficient is not a valid number [GainShift]", nfe);
    }
  }

  /** A item state change indicates that a gate has been chosen. */
  public void itemStateChanged(final ItemEvent event) {
    if (event.getSource() == coefficients || event.getSource() == channels) {
      try {
        setUseCoefficients(coefficients.isSelected());
      } catch (DataException de) {
        LOGGER.log(Level.SEVERE, de.getMessage(), de);
      }
    } else if (event.getSource() == to && (to.getSelectedItem() != null)) {
      setUseHist((String) to.getSelectedItem());
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

  private void setUseCoefficients(final boolean state) throws DataException {
    setUILabels(state);
    if (state) {
      getChannels();
      calculateCoefficients();
      text1.setText(format(intercept1));
      text2.setText(format(slope1));
      text3.setText(format(intercept2));
      text4.setText(format(slope2));
    } else {
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
   * Implementation of PropertyChangeListener interface to receive broadcast events. Listen for
   * histograms new, histogram added
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    final BroadcastEvent.Command com = ((BroadcastEvent) evt).getCommand();
    if (com == BroadcastEvent.Command.HISTOGRAM_NEW
        || com == BroadcastEvent.Command.HISTOGRAM_ADD) {
      doSetup();
    }
  }
}
