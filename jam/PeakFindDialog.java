package jam;

import com.google.inject.Inject;
import jam.plot.PlotDisplay;
import jam.ui.PanelOKApplyCancelButtons;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.logging.Logger;

/**
 * Dialog for setting peak finding parameters.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser</a>
 */
public class PeakFindDialog extends JDialog {

	private static final Logger LOGGER = Logger.getLogger(PeakFindDialog.class
			.getPackage().getName());

	private transient final PlotDisplay display;

	/**
	 * Constructs a new peak find dialog.
	 * 
	 * @param display
	 *            plot display
	 * 
	 */
	@Inject
	public PeakFindDialog(final PlotDisplay display) {
		super();
		this.display = display;
		createDialog();
	}

	private transient JTextField width, sensitivity;

	private transient JCheckBox calibrate;

	private void createDialog() {
		this.setTitle("Peak Find Preferences");
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout(10, 10));
		final JPanel fields = new JPanel(new GridLayout(0, 1, 5, 5));
		contents.add(fields, BorderLayout.CENTER);
		fields.setBorder(new EmptyBorder(10, 10, 0, 0));
		fields.add(new JLabel("Width", SwingConstants.RIGHT));
		fields.add(new JLabel("Sensitivity", SwingConstants.RIGHT));
		fields.add(new JLabel("Display", SwingConstants.RIGHT));
		final JPanel center = new JPanel(new GridLayout(0, 1, 5, 5));
		contents.add(center, BorderLayout.EAST);
		center.setBorder(new EmptyBorder(10, 0, 0, 10));
		width = new JTextField("12");
		width.setToolTipText("FWHM to search for.");
		center.add(width);
		sensitivity = new JTextField("3");
		sensitivity
				.setToolTipText("Greater values require better defined peaks.\n"
						+ "A value of 3 gives an appr. 3% chance for a found peak to be false.");
		center.add(sensitivity);
		calibrate = new JCheckBox("Calibrated value", true);
		center.add(calibrate);
		final PanelOKApplyCancelButtons.Listener callback = new PanelOKApplyCancelButtons.AbstractListener(
				this) {
			public void apply() {
				setPeakFindProperties();
			}
		};
		final PanelOKApplyCancelButtons pbutton = new PanelOKApplyCancelButtons(
				callback);
		contents.add(pbutton.getComponent(), BorderLayout.SOUTH);
		setResizable(false);
		pack();
	}

	private void setPeakFindProperties() {
		final double dWidth = Double.parseDouble(width.getText().trim());
		final double dSense = Double.parseDouble(sensitivity.getText().trim());
		final boolean cal = calibrate.isSelected();
		display.setPeakFindProperties(dWidth, dSense, cal);
        String msg = "Peak Find Properties Set: Width=" + dWidth + ", Sensitivity=" + dSense +
                (cal ? ", calibrated value displayed if available, centroid channel if not."
                        : ", centroid channel displayed.");
        LOGGER.info(msg);
	}
}
