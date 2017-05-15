package jam.fit;

import injection.GuiceInjector;
import jam.data.*;
import jam.global.MessageHandler;
import jam.plot.PlotDisplay;
import jam.ui.Canceller;
import jam.ui.SelectionTree;
import jam.ui.WindowCancelAction;
import jam.util.NumberUtilities;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static javax.swing.SwingConstants.RIGHT;

/**
 * This defines the necessary methods that need to be defined in order for a fit
 * routine to work with <code>FitControl</code>. It also contains the dialog box
 * that serves as the user interface.
 * @author Dale Visser
 * @author Ken Swartz
 * @see AbstractNonLinearFit
 * @see GaussianFit
 */
public abstract class AbstractFit implements Fit {

    /**
     * The histogram to be fitted.
     */
    protected transient double[] counts;

    /**
     * The errors associated with <code>counts</code>.
     */
    protected transient double[] errors;

    /**
     * If these are set, they are used for display.
     */
    protected transient int lowerLimit;

    /**
     * Displayed name of <code>Fit</code> routine.
     */
    protected transient String name;

    protected transient final ParameterList parameters = new ParameterList();

    private transient PlotInteraction plotInteraction;

    /**
     * Whether to calculate residuals.
     */
    protected transient boolean residualOption = true;

    /**
     * Histogram name field
     */
    private transient JLabel textHistName;

    /**
     * The text display area for information the fit produces.
     */
    protected transient FitConsole textInfo;

    protected transient int upperLimit;

    private static final NumberUtilities NUMUTIL = GuiceInjector
            .getObjectInstance(NumberUtilities.class);

    /**
     * Class constructor.
     * @param name
     *            name for dialog box and menu item
     */
    public AbstractFit(final String name) {
        super();
        this.name = name;
    }

    /**
     * Creates user interface dialog box.
     * @param parent
     *            controlling frame
     * @param plotDisplay
     *            Jam main class
     * @return the fit dialog
     */
    public final FitDialog createDialog(final Frame parent,
            final PlotDisplay plotDisplay) {
        final FitDialog fitDialog = new FitDialog(parent, name);
        final JDialog dfit = fitDialog.getDialog();
        final Container contents = dfit.getContentPane();
        final JTabbedPane tabs = new JTabbedPane();
        contents.add(tabs, BorderLayout.CENTER);
        final JPanel main = new JPanel(new BorderLayout());
        tabs.addTab("Fit", null, main, "Setup parameters and do fit.");
        textInfo = new FitConsole(35 * parameters.size());
        tabs.addTab("Information", null, textInfo,
                "Additional information output from the fits.");
        /* top panel with histogram name */
        final JPanel pHistName = new JPanel(new BorderLayout());
        pHistName.setBorder(LineBorder.createBlackLineBorder());
        pHistName.add(new JLabel("Fit Histogram: ", RIGHT), BorderLayout.WEST);
        textHistName = new JLabel("     ");
        pHistName.add(textHistName, BorderLayout.CENTER);
        main.add(pHistName, BorderLayout.NORTH);
        /* bottom panel with status and buttons */
        final JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridLayout(0, 1));
        main.add(bottomPanel, BorderLayout.SOUTH);
        /* status panel part of bottom panel */
        final JLabel status = new JLabel(
                "OK                                                          ");
        final JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(new JLabel("Status: ", RIGHT), BorderLayout.WEST);
        statusPanel.add(status, BorderLayout.CENTER);
        bottomPanel.add(statusPanel);
        /* button panel part of bottom panel */
        final JPanel pbut = new JPanel();
        pbut.setLayout(new GridLayout(1, 0));
        bottomPanel.add(pbut);
        final JButton bMouseGet = createGetMouseButton(status);
        pbut.add(bMouseGet);
        final JButton bGo = createDoFitButton(status);
        pbut.add(bGo);
        final JButton bDraw = new JButton("Draw Fit");
        bDraw.addActionListener(event -> {
            try {
                drawFit();
            } catch (FitException fe) {
                status.setText(fe.getMessage());
            }
        });
        pbut.add(bDraw);
        final JButton bReset = new JButton("Reset");
        bReset.addActionListener(event -> plotInteraction.reset());
        pbut.add(bReset);
        final Canceller canceller = () -> {
            plotInteraction.setMouseActive(false);
            dfit.dispose();
        };
        final JButton bCancel = new JButton(new WindowCancelAction(canceller));
        pbut.add(bCancel);

        /* Layout of parameter widgets */
        final JPanel panelParam = new JPanel(new GridLayout(0, 1));
        main.add(panelParam, BorderLayout.CENTER);
        final JPanel west = new JPanel(new GridLayout(0, 1));
        final JPanel center = new JPanel(new GridLayout(0, 1));
        final JPanel east = new JPanel(new GridLayout(0, 1));
        main.add(west, BorderLayout.WEST);
        main.add(east, BorderLayout.EAST);
        main.add(center, BorderLayout.CENTER);
        for (final Parameter<?> parameter : parameters) {
            parameters.addParameterGUI(parameter, west, center, east);
        } // loop for all parameters
        plotInteraction = new PlotInteraction(plotDisplay, parameters);
        fitDialog.setPlotInteraction(this.plotInteraction);
        dfit.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(final WindowEvent event) {
                updateHist();
            }

            @Override
            public void windowClosing(final WindowEvent event) {
                plotInteraction.setMouseActive(false);
                dfit.dispose();
            }
        });
        dfit.pack();
        fitDialog.show();
        return fitDialog;
    }

    private JButton createDoFitButton(final JLabel status) {
        final JButton bGo = new JButton("Do Fit");
        bGo.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                try {
                    plotInteraction.setMouseActive(false);
                    plotInteraction.updateParametersFromDialog();
                    this.getCounts();
                    status.setText("Estimating...");
                    AbstractFit.this.estimate();
                    status.setText("Doing Fit...");
                    status.setText(AbstractFit.this.doFit());
                    AbstractFit.this.updateDisplay();
                    AbstractFit.this.drawFit();
                } catch (FitException fe) {
                    status.setText(fe.getMessage());
                }
            }

            /**
             * Gets counts from currently displayed <code>Histogram</code>
             */
            private void getCounts() {
                final AbstractHist1D hist1d = (AbstractHist1D) SelectionTree
                        .getCurrentHistogram();
                if (hist1d.getType() == HistogramType.ONE_DIM_INT) {
                    final int[] temp = ((HistInt1D) hist1d).getCounts();
                    AbstractFit.this.counts = NUMUTIL.intToDoubleArray(temp);
                } else {
                    AbstractFit.this.counts = ((HistDouble1D) hist1d)
                            .getCounts();
                }
                AbstractFit.this.errors = hist1d.getErrors();
            }
        });
        return bGo;
    }

    private JButton createGetMouseButton(final JLabel status) {
        final JButton bMouseGet = new JButton("Get Mouse");
        bMouseGet.addActionListener(event -> {
            AbstractFit.this.initializeMouse(status);
            AbstractFit.this.plotInteraction.setMouseActive(true);
        });
        return bMouseGet;
    }

    /*
     * non-javadoc: Draw the fit with the values in the fields.
     */
    private void drawFit() throws FitException {
        double[][] signals = null;
        double[] residuals = null;
        double[] background = null;
        this.plotInteraction.updateParametersFromDialog();
        final int numChannel = this.upperLimit - this.lowerLimit + 1;
        if (this.getNumberOfSignals() > 0) {
            signals = new double[this.getNumberOfSignals()][numChannel];
            for (int sig = 0; sig < signals.length; sig++) {
                for (int i = 0; i < numChannel; i++) {
                    signals[sig][i] = this.calculateSignal(sig, i
                            + this.lowerLimit);
                }
            }
        }
        if (this.residualOption) {
            residuals = new double[numChannel];
            for (int i = 0; i < numChannel; i++) {
                final int chan = i + this.lowerLimit;
                residuals[i] = this.counts[chan] - this.calculate(chan);
            }
        }
        if (hasBackground()) {
            background = new double[numChannel];
            for (int i = 0; i < numChannel; i++) {
                background[i] = this.calculateBackground(i + this.lowerLimit);
            }
        }
        this.plotInteraction.displayFit(signals, background, residuals,
                this.lowerLimit);
    }

    /**
     * Accesses a parameter in <code>parameterTable</code> by name.
     * @param which
     *            contains the name of the parameter (same name displayed in
     *            dialog box)
     * @return the <code>Parameter</code> object going by that name
     */
    public final Parameter<?> getParameter(final String which) {
        return this.parameters.get(which);
    }

    /**
     * Gets the contents of <code>parameters</code>.
     * @return the contents of <code>parameters</code>
     * @see #parameters
     */
    public final ParameterList getParameters() {
        return parameters;
    }

    public final MessageHandler getTextInfo() {
        return textInfo;
    }

    /**
     * Set the state to enter values using mouse
     */
    private void initializeMouse(final JLabel status) {
        this.parameters.highlightFields(status);
        this.plotInteraction.resetIterator();
    }

    /*
     * non-javadoc: Update all fields in the dialog after performing a fit.
     */
    private void updateDisplay() {
        this.updateHist();
        this.parameters.updateGUI();
    }

    /**
     * Update the name of the displayed histogram in the dialog box.
     */
    private void updateHist() {
        final AbstractHistogram histogram = (AbstractHistogram) SelectionTree
                .getCurrentHistogram();
        if (histogram != null && histogram.getDimensionality() == 1) {
            if (histogram.getType() == HistogramType.ONE_DIM_INT) {
                final int[] temp = ((HistInt1D) histogram).getCounts();
                counts = NUMUTIL.intToDoubleArray(temp);
            } else if (histogram.getType() == HistogramType.ONE_D_DOUBLE) {
                counts = ((HistDouble1D) histogram).getCounts();
            }
            textHistName.setText(histogram.getFullName());
        } else { // 2d
            textHistName.setText("Need 1D Hist!");
        }
    }

}