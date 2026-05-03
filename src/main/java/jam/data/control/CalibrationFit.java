package jam.data.control;

import com.google.inject.Inject;
import jam.data.AbstractHist1D;
import jam.data.func.AbstractCalibrationFunction;
import jam.data.func.CalibrationFitException;
import jam.data.func.CalibrationFunctionCollection;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.Nameable;
import jam.ui.CalibrationListCellRenderer;
import jam.ui.PanelOKApplyCancelButtons;
import jam.ui.SelectionTree;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.util.logging.Level;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Class to control the histograms Allows one to zero the histograms and create new histograms
 *
 * @author Ken Swartz
 * @version 0.5
 */
public class CalibrationFit extends AbstractControl {

  private static final int NUM_POINTS = 10;

  private static final int MAX_TERMS = 5;

  private static final String BLANK_TITLE = "Histogram not calibrated";

  private static final String BLANK_LABEL = "    --     ";

  private transient AbstractCalibrationFunction calibrationFn =
      CalibrationFunctionCollection.NO_CALIBRATION;

  /* GUI stuff */
  private final transient JTabbedPane tabPane; // Tabbed for fit type

  private final transient JComboBox<Object> funcChooser; // Chooser for
  // function type

  private final transient JLabel equationLabel;

  /* Radio buttons for fit type */
  private final transient JRadioButton rbFitPoints;
  private final transient JRadioButton rbSetCoefficients;

  private transient JPanel pPoint[];

  private transient JTextField[] tEnergy;

  private transient JTextField[] tChannel;

  private transient JCheckBox cUse[];

  private transient JPanel pcoeff[];

  private transient JLabel lcoeff[];

  private transient JTextField tcoeff[];

  private transient int numberTerms;

  private transient boolean isUpdate;

  private final transient NumberFormat coefficientFormat;

  /**
   * Constructs a calibration fitting dialog.
   *
   * @param frame application frame
   * @param broadcaster broadcasts status changes
   */
  @Inject
  public CalibrationFit(final Frame frame, final Broadcaster broadcaster) {
    super(frame, "Calibration Fit", false, broadcaster);
    setResizable(false);
    setLocation(30, 30);
    final Container contents = getContentPane();
    contents.setLayout(new BorderLayout(5, 5));
    /* Selection panel at the top */
    final JPanel pSelection = new JPanel(new GridLayout(0, 1, 5, 0));
    pSelection.setBorder(new EmptyBorder(10, 0, 0, 0));
    contents.add(pSelection, BorderLayout.NORTH);
    /* Equation chooser */
    final JPanel pChoose = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    pChoose.add(new JLabel("Function: "));
    funcChooser = new JComboBox<>(new CalibrationComboBoxModel());
    funcChooser.setRenderer(new CalibrationListCellRenderer());
    funcChooser.addItemListener(event -> selectionChange());
    pChoose.add(funcChooser);
    pSelection.add(pChoose);
    /* Equation */
    equationLabel = new JLabel(BLANK_TITLE, SwingConstants.CENTER);
    pSelection.add(equationLabel);
    /* Fit using points or coefficients */
    final JPanel pFitType = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    final ButtonGroup gFitType = new ButtonGroup();
    rbFitPoints = new JRadioButton("Fit Points", true);
    rbFitPoints.addItemListener(
        itemEvent -> {
          if (rbFitPoints.isSelected()) {
            setFitTypePoints(true);
          }
        });
    gFitType.add(rbFitPoints);
    pFitType.add(rbFitPoints);
    rbSetCoefficients = new JRadioButton("Set Coefficients", false);
    rbSetCoefficients.addItemListener(
        itemEvent -> {
          if (rbSetCoefficients.isSelected()) {
            setFitTypePoints(false);
          }
        });
    gFitType.add(rbSetCoefficients);
    pFitType.add(rbSetCoefficients);
    pSelection.add(pFitType);
    /* Tabbed panel with fit points an coefficients */
    tabPane = new JTabbedPane();
    contents.add(tabPane, BorderLayout.CENTER);
    final JPanel ptsPanel = createAllPointsPanel();
    tabPane.addTab("Points", null, ptsPanel, "Channels and Energies to fit.");
    final JPanel histPanel = createCoeffPanel();
    tabPane.addTab("Coefficients", null, histPanel, "Fit coefficients.");
    /* button panel */
    final PanelOKApplyCancelButtons pButtons =
        new PanelOKApplyCancelButtons(
            new PanelOKApplyCancelButtons.AbstractListener(this) {
              public void apply() {
                if (rbFitPoints.isSelected()) {
                  applyCalibration();
                } else {
                  doSetCoefficients();
                }
              }

              @Override
              public void cancel() {
                cancelCalibration();
              }
            });
    contents.add(pButtons.getComponent(), BorderLayout.SOUTH);
    pack();
    // Initially selections
    isUpdate = false;
    // comboBoxFunction.setSelectedIndex(0);
    rbFitPoints.setSelected(true);
    // numFormat for formatting coeff output
    coefficientFormat = NumberFormat.getInstance();
    coefficientFormat.setGroupingUsed(false);
    coefficientFormat.setMinimumFractionDigits(2);
    coefficientFormat.setMaximumFractionDigits(10);
  }

  // Create panel with the points to fit
  private JPanel createAllPointsPanel() {
    final JPanel pAllPoints = new JPanel(new GridLayout(0, 1, 10, 2));
    pPoint = new JPanel[NUM_POINTS];
    tEnergy = new JTextField[NUM_POINTS];
    tChannel = new JTextField[NUM_POINTS];
    cUse = new JCheckBox[NUM_POINTS];
    for (int i = 0; i < NUM_POINTS; i++) {
      generatePointPanel(i);
      pAllPoints.add(pPoint[i]);
    }
    return pAllPoints;
  }

  private void generatePointPanel(final int index) {
    pPoint[index] = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    // pAllPoints.add(pPoint[i]);
    pPoint[index].add(new JLabel("Energy"));
    tEnergy[index] = new JTextField("");
    tEnergy[index].setColumns(6);
    pPoint[index].add(tEnergy[index]);
    pPoint[index].add(new JLabel("Channel"));
    tChannel[index] = new JTextField("");
    tChannel[index].setColumns(6);
    pPoint[index].add(tChannel[index]);
    cUse[index] = new JCheckBox("use");
    cUse[index].setSelected(true);
    pPoint[index].add(cUse[index]);
    cUse[index].addItemListener(itemEvent -> setPointFieldActive(index, cUse[index].isSelected()));
  }

  // Create panel with the coefficients
  private JPanel createCoeffPanel() {
    final JPanel pCoeff = new JPanel(new GridLayout(0, 1, 10, 2));
    pcoeff = new JPanel[MAX_TERMS];
    lcoeff = new JLabel[MAX_TERMS];
    tcoeff = new JTextField[MAX_TERMS];
    for (int i = 0; i < MAX_TERMS; i++) {
      generateCoeffPanel(i);
      pCoeff.add(pcoeff[i]);
    }
    for (int i = 0; i < NUM_POINTS - MAX_TERMS - 1; i++) {
      pCoeff.add(Box.createVerticalGlue());
    }
    return pCoeff;
  }

  private void generateCoeffPanel(final int index) {
    pcoeff[index] = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    lcoeff[index] = new JLabel(BLANK_LABEL, SwingConstants.RIGHT);
    pcoeff[index].add(lcoeff[index]);
    tcoeff[index] = new JTextField("");
    tcoeff[index].setColumns(10);
    pcoeff[index].add(tcoeff[index]);
  }

  private void selectionChange() {
    final String funcName = (String) funcChooser.getSelectedItem();
    if (!isUpdate) {
      selectFunction(funcName);
    }
  }

  /*
   * Function selected
   */
  private void selectFunction(final String funcName) {
    try {
      calibrationFn = getCurrentCalibrationFunction();
      final Class<? extends AbstractCalibrationFunction> calClass =
          CalibrationFunctionCollection.getMapFunctions().get(funcName);
      final boolean change = calClass.isInstance(calibrationFn);
      final AbstractHist1D currentHistogram = getCurrentHistogram();
      if (calibrationFn == null || !change) {
        calibrationFn = calClass.getDeclaredConstructor().newInstance();
        calibrationFn.setSizeHistogram(currentHistogram.getSizeX());
      }
      updateFields(calibrationFn, rbFitPoints.isSelected());
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      LOGGER.log(
          Level.SEVERE, "Creating fit function " + getClass().getName() + " " + e.toString(), e);
    }
  }

  /*
   * Set the fit type to be points instead of setting coefficients.
   */
  private void setFitTypePoints(final boolean state) {
    if (state) {
      tabPane.setSelectedIndex(0);
    } else {
      tabPane.setSelectedIndex(1);
    }
    updateFields(calibrationFn, state);
  }

  private void updateSelection() {
    boolean isCalPts;
    calibrationFn = getCurrentCalibrationFunction();
    /* Select name. */
    String name = calibrationFn.getName();
    /* Set fit type. */
    if (calibrationFn.isFitPoints()) {
      isCalPts = true;
      rbFitPoints.setSelected(true);
    } else {
      isCalPts = false;
      rbSetCoefficients.setSelected(true);
    }
    // Change isUpdate state so we don't loop on item Change event
    isUpdate = true;
    funcChooser.setSelectedItem(name);
    isUpdate = false;
    updateFields(calibrationFn, isCalPts);
  }

  @Override
  public void doSetup() {
    calibrationFn = getCurrentCalibrationFunction();
    final boolean isCalPts = calibrationFn.isFitPoints();
    updateFields(calibrationFn, isCalPts);
  }

  private void applyCalibration() {
    final AbstractHist1D currentHistogram = getCurrentHistogram();
    if (currentHistogram == null) {
      LOGGER.severe("Need a 1 Dimension histogram");
    } else {
      if (calibrationFn != null && calibrationFn.isCalibrated()) {
        doFitCalibration();
        final boolean isCalPts = calibrationFn.isFitPoints();
        updateFields(calibrationFn, isCalPts);
      } else {
        currentHistogram.setCalibration(calibrationFn);
        if (LOGGER.isLoggable(Level.INFO)) {
          LOGGER.info("Uncalibrated histogram " + currentHistogram.getFullName()); // NOPMD
        }
      }
    }
  }

  /** cancel the histogram calibration */
  private void cancelCalibration() {
    this.dispose();
    broadcaster.broadcast(BroadcastEvent.Command.REFRESH);
  }

  /*
   * non-javadoc: sets fields to be active
   */
  private void setPointFieldActive(final int number, final boolean state) {
    tChannel[number].setEnabled(state);
    tEnergy[number].setEnabled(state);
  }

  /**
   * Call the fitting method of the chosen function to calibrate a histogram Already checked have a
   * 1d histogram and a fit function
   */
  private void doFitCalibration() {
    double energy[] = new double[NUM_POINTS];
    double channel[] = new double[NUM_POINTS];
    int numberPoints = 0;
    double xval[];
    double yval[];
    String fitText;
    final AbstractHist1D currentHist = getCurrentHistogram();
    try {
      for (int i = 0; i < NUM_POINTS; i++) {
        // is entry marked
        if (cUse[i].isSelected() && tEnergy[i].getText().trim().length() != 0) {
          energy[numberPoints] = Double.parseDouble(tEnergy[i].getText());
          channel[numberPoints] = Double.parseDouble(tChannel[i].getText());
          numberPoints++;
        }
      }
      if (numberPoints >= 2) {
        // setFunctionType();//ensures a fresh CalibrationFit instance
        // is created
        xval = new double[numberPoints];
        yval = new double[numberPoints];
        // put valid energy and channel into arrays
        for (int i = 0; i < numberPoints; i++) {
          yval[i] = energy[i];
          xval[i] = channel[i];
        }
        calibrationFn.setPoints(xval, yval);
        calibrationFn.fit();
        fitText = calibrationFn.getFormula(coefficientFormat);
        currentHist.setCalibration(calibrationFn);
        broadcaster.broadcast(BroadcastEvent.Command.REFRESH);
        if (LOGGER.isLoggable(Level.INFO)) {
          LOGGER.info( // NOPMD
              "Calibrated histogram " + currentHist.getFullName().trim() + " with " + fitText);
        }
      } else {
        LOGGER.severe("Need at least 2 points");
      }
    } catch (NumberFormatException nfe) {
      currentHist.setCalibration(CalibrationFunctionCollection.NO_CALIBRATION);
      LOGGER.severe("Invalid input, not a number");
    } catch (CalibrationFitException de) {
      currentHist.setCalibration(CalibrationFunctionCollection.NO_CALIBRATION);
      if (LOGGER.isLoggable(Level.SEVERE)) {
        LOGGER.severe(de.getMessage()); // NOPMD
      }
    }
  }

  /** set the calibration coefficients for a histogram */
  private void doSetCoefficients() {
    double coeff[] = new double[numberTerms];
    final AbstractHist1D currentHist = getCurrentHistogram();
    /* silently ignore if histogram null */
    if (calibrationFn == null) {
      LOGGER.severe("Calibration function not defined.");
    } else {
      int index = 0;
      try {
        for (index = 0; index < numberTerms; index++) {
          coeff[index] = Double.parseDouble(tcoeff[index].getText());
        }
        calibrationFn.setCoefficients(coeff);
        currentHist.setCalibration(calibrationFn);
        broadcaster.broadcast(BroadcastEvent.Command.REFRESH);
        if (LOGGER.isLoggable(Level.INFO)) {
          LOGGER.info( // NOPMD
              "Calibrated histogram "
                  + currentHist.getFullName().trim()
                  + " with "
                  + calibrationFn.getFormula(coefficientFormat));
        }
      } catch (NumberFormatException nfe) {
        if (LOGGER.isLoggable(Level.SEVERE)) {
          LOGGER.severe( // NOPMD
              "Invalid input, coefficient " + calibrationFn.getLabels()[index]);
        }
      }
    }
  }

  /**
   * Sets inputs fields, enables and disables them as appropriate depending of if there is a
   * calibration or what type it is.
   *
   * @param hcf calibration, if any
   * @param isCalPts if <code>true</code>, calibration was fit to points
   */
  private void updateFields(final AbstractCalibrationFunction hcf, final boolean isCalPts) {
    final boolean calNotNull = hcf.isCalibrated();
    final String title = hcf.getTitle();
    equationLabel.setText(title);

    /* Points fields */
    if (calNotNull) {
      final double[] ptsChannel = hcf.getPtsChannel();
      final double[] ptsEnergy = hcf.getPtsEnergy();
      numberTerms = hcf.getNumberTerms();
      final String[] labels = hcf.getLabels();
      final double[] coeff = hcf.getCoefficients();
      /* Calibrated with points */
      if (isCalPts) {
        setCalibratedWithPoints(ptsChannel, ptsEnergy, labels, coeff);
        // Coefficients set for fit
      } else {
        for (int i = 0; i < NUM_POINTS; i++) {
          tChannel[i].setText("");
          tChannel[i].setEnabled(true);
          tChannel[i].setEditable(false);
          tEnergy[i].setText("");
          tEnergy[i].setEnabled(true);
          tEnergy[i].setEditable(false);
        }
        for (int i = 0; i < MAX_TERMS; i++) {
          if (i < numberTerms) {
            lcoeff[i].setText(labels[i]);
            tcoeff[i].setText(coefficientFormat.format(coeff[i]));
            tcoeff[i].setEnabled(true);
            tcoeff[i].setEditable(true);
          } else {
            lcoeff[i].setText(BLANK_LABEL);
            tcoeff[i].setText("");
            tcoeff[i].setEditable(false);
            tcoeff[i].setEnabled(false);
          }
        }
      }

      // Not calibrated
    } else {
      for (int i = 0; i < NUM_POINTS; i++) {
        tChannel[i].setText("");
        tChannel[i].setEditable(false);
        tChannel[i].setEnabled(false);
        tEnergy[i].setText("");
        tEnergy[i].setEditable(false);
        tEnergy[i].setEnabled(false);
      }
      for (int i = 0; i < MAX_TERMS; i++) {
        lcoeff[i].setText(BLANK_LABEL);
        tcoeff[i].setText("");
        tcoeff[i].setEnabled(false);
        tcoeff[i].setEditable(false);
      }
    }
  }

  private void setCalibratedWithPoints(
      final double[] ptsChannel,
      final double[] ptsEnergy,
      final String[] labels,
      final double[] coeff) {
    for (int i = 0; i < NUM_POINTS; i++) {
      if (i < ptsChannel.length) {
        tChannel[i].setText(String.valueOf(ptsChannel[i]));
        tEnergy[i].setText(String.valueOf(ptsEnergy[i]));
      } else {
        tChannel[i].setText("");
        tEnergy[i].setText("");
      }
      tChannel[i].setEditable(true);
      tChannel[i].setEnabled(true);
      tEnergy[i].setEditable(true);
      tEnergy[i].setEnabled(true);
    }
    for (int i = 0; i < MAX_TERMS; i++) {
      if (i < numberTerms) {
        lcoeff[i].setText(labels[i]);
        tcoeff[i].setText(coefficientFormat.format(coeff[i]));
        tcoeff[i].setEnabled(false);
        tcoeff[i].setEditable(true);
      } else {
        lcoeff[i].setText(BLANK_LABEL);
        tcoeff[i].setText("");
        tcoeff[i].setEnabled(false);
        tcoeff[i].setEditable(false);
      }
    }
  }

  private AbstractHist1D getCurrentHistogram() {
    final Nameable hist = SelectionTree.getCurrentHistogram();
    AbstractHist1D rval = null;
    if (hist instanceof AbstractHist1D) {
      rval = (AbstractHist1D) hist;
    }
    return rval;
  }

  private AbstractCalibrationFunction getCurrentCalibrationFunction() {
    final AbstractHist1D currentHist = getCurrentHistogram();
    return currentHist == null
        ? CalibrationFunctionCollection.NO_CALIBRATION
        : currentHist.getCalibration();
  }

  /** Update selection then show dialog */
  @Override
  public void setVisible(final boolean show) {
    if (show) {
      updateSelection();
    }
    super.setVisible(show);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    final BroadcastEvent event = (BroadcastEvent) evt;
    final BroadcastEvent.Command com = event.getCommand();
    if (com == BroadcastEvent.Command.HISTOGRAM_SELECT
        || com == BroadcastEvent.Command.HISTOGRAM_ADD) {
      updateSelection();
    }
  }
}
