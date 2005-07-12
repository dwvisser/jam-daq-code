package jam.plot.color;

import jam.ui.PanelOKApplyCancelButtons;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Dialog for setting the gradient color scale parameters.
 * 
 * @author Eric Lingerfelt
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 * @version 2004-11-08
 */
public class ColorSettingsFrame extends JDialog implements ChangeListener,
		ColorPrefs {

	static private final ColorSettingsFrame CSF = new ColorSettingsFrame();

	/**
	 * Returns the only instance of this class.
	 * 
	 * @return the only insstance of this class
	 */
	static public ColorSettingsFrame getInstance() {
		return CSF;
	}

	private transient final RainbowPanel rainbowPanel = new RainbowPanel();

	private transient final JTextField x0RField, x0GField, x0BField, aRField, aGField,
			aBField;

	private transient final JSlider x0RSlider, x0GSlider, x0BSlider, aRSlider, aGSlider,
			aBSlider;

	private ColorSettingsFrame() {
		final int x0R = (int) Math.round(100 * COLOR_PREFS.getDouble(
				ColorPrefs.X0R, .80));
		final int x0G = (int) Math.round(100 * COLOR_PREFS.getDouble(
				ColorPrefs.X0G, .60));
		final int x0B = (int) Math.round(100 * COLOR_PREFS.getDouble(
				ColorPrefs.X0B, .20));
		final int aRed = (int) Math.round(100 * COLOR_PREFS.getDouble(
				ColorPrefs.ARED, .50));
		final int aGreen = (int) Math.round(100 * COLOR_PREFS.getDouble(
				ColorPrefs.AGREEN, .40));
		final int aBlue = (int) Math.round(100 * COLOR_PREFS.getDouble(
				ColorPrefs.ABLUE, .30));
		setTitle("Color Scale Settings");
		final Container contents = getContentPane();
		contents.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		final PanelOKApplyCancelButtons buttons = new PanelOKApplyCancelButtons(
				new PanelOKApplyCancelButtons.DefaultListener(this) {
					public void apply() {
						final double x0red = x0RSlider.getValue() / 100.0;
						final double x0green = x0GSlider.getValue() / 100.0;
						final double x0blue = x0BSlider.getValue() / 100.0;
						final double aRedD = aRSlider.getValue() / 100.0;
						final double aGreenD = aGSlider.getValue() / 100.0;
						final double aBlueD = aBSlider.getValue() / 100.0;
						COLOR_PREFS.putDouble(ColorPrefs.ABLUE, aBlueD);
						COLOR_PREFS.putDouble(ColorPrefs.AGREEN, aGreenD);
						COLOR_PREFS.putDouble(ColorPrefs.ARED, aRedD);
						COLOR_PREFS.putDouble(ColorPrefs.X0B, x0blue);
						COLOR_PREFS.putDouble(ColorPrefs.X0G, x0green);
						COLOR_PREFS.putDouble(ColorPrefs.X0R, x0red);
					}
				});
		x0RSlider = new JSlider(SwingConstants.VERTICAL, 0, 100, x0R);
		x0RSlider.addChangeListener(this);
		x0RSlider.setPreferredSize(new Dimension(20, 130));
		x0GSlider = new JSlider(SwingConstants.VERTICAL, 0, 100, x0G);
		x0GSlider.addChangeListener(this);
		x0GSlider.setPreferredSize(new Dimension(20, 130));
		x0BSlider = new JSlider(SwingConstants.VERTICAL, 0, 100, x0B);
		x0BSlider.addChangeListener(this);
		x0BSlider.setPreferredSize(new Dimension(20, 130));
		aRSlider = new JSlider(SwingConstants.VERTICAL, 0, 100, aRed);
		aRSlider.addChangeListener(this);
		aRSlider.setPreferredSize(new Dimension(20, 130));
		aGSlider = new JSlider(SwingConstants.VERTICAL, 0, 100, aGreen);
		aGSlider.addChangeListener(this);
		aGSlider.setPreferredSize(new Dimension(20, 130));
		aBSlider = new JSlider(SwingConstants.VERTICAL, 0, 100, aBlue);
		aBSlider.addChangeListener(this);
		aBSlider.setPreferredSize(new Dimension(20, 130));
		final JComponent redLabel = new JLabel("Red");
		final JComponent greenLabel = new JLabel("Green");
		final JComponent blueLabel = new JLabel("Blue");
		final JComponent x0RLabel = new JLabel("Position : ");
		final JComponent x0GLabel = new JLabel("Position : ");
		final JComponent x0BLabel = new JLabel("Position : ");
		final JComponent aRLabel = new JLabel("Amount : ");
		final JComponent aGLabel = new JLabel("Amount : ");
		final JComponent aBLabel = new JLabel("Amount : ");
		x0RField = new JTextField(5);
		x0RField.setText(String.valueOf(x0R / 100.0));
		x0RField.setEditable(false);
		x0GField = new JTextField(5);
		x0GField.setText(String.valueOf(x0G / 100.0));
		x0GField.setEditable(false);
		x0BField = new JTextField(5);
		x0BField.setText(String.valueOf(x0B / 100.0));
		x0BField.setEditable(false);
		aRField = new JTextField(5);
		aRField.setText(String.valueOf(aRed / 100.0));
		aRField.setEditable(false);
		aGField = new JTextField(5);
		aGField.setText(String.valueOf(aGreen / 100.0));
		aGField.setEditable(false);
		aBField = new JTextField(5);
		aBField.setText(String.valueOf(aBlue / 100.0));
		aBField.setEditable(false);
		final JComponent csLabel = new JLabel(
				"<html>Choose a<p>color scheme :</html>");
		final String current = "Current";
		final String rainbow = "Rainbow";
		final JComboBox csChooser = new JComboBox();
		csChooser.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				final String selection = (String) csChooser
						.getSelectedItem();
				if (selection.equals("Greyscale")) {
					x0RSlider.setValue(100);
					x0GSlider.setValue(100);
					x0BSlider.setValue(100);
					aRSlider.setValue(100);
					aGSlider.setValue(100);
					aBSlider.setValue(100);
				} else if (selection.equals(rainbow)) {
					x0RSlider.setValue(80);
					x0GSlider.setValue(60);
					x0BSlider.setValue(20);
					aRSlider.setValue(50);
					aGSlider.setValue(40);
					aBSlider.setValue(30);
				} else if (selection.equals("Purple Haze")) {
					x0RSlider.setValue(100);
					x0GSlider.setValue(0);
					x0BSlider.setValue(100);
					aRSlider.setValue(84);
					aGSlider.setValue(0);
					aBSlider.setValue(84);
				} else {// current
					final int x0red = (int) Math.round(100 * COLOR_PREFS
							.getDouble(ColorPrefs.X0R, .80));
					final int x0green = (int) Math.round(100 * COLOR_PREFS
							.getDouble(ColorPrefs.X0G, .60));
					final int x0blue = (int) Math.round(100 * COLOR_PREFS
							.getDouble(ColorPrefs.X0B, .20));
					final int aRedD = (int) Math.round(100 * COLOR_PREFS
							.getDouble(ColorPrefs.ARED, .50));
					final int aGreenD = (int) Math.round(100 * COLOR_PREFS
							.getDouble(ColorPrefs.AGREEN, .40));
					final int aBlueD = (int) Math.round(100 * COLOR_PREFS
							.getDouble(ColorPrefs.ABLUE, .30));
					x0RSlider.setValue(x0red);
					x0GSlider.setValue(x0green);
					x0BSlider.setValue(x0blue);
					aRSlider.setValue(aRedD);
					aGSlider.setValue(aGreenD);
					aBSlider.setValue(aBlueD);
				}
			}
		});
		csChooser.addItem(current);
		csChooser.addItem(rainbow);
		csChooser.addItem("Purple Haze");
		csChooser.addItem("Greyscale");
		csChooser.setSelectedItem(rainbow);
		csChooser.setSelectedItem(current);
		final JScrollPane scrollpane = new JScrollPane(rainbowPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollpane.setPreferredSize(new Dimension(50, 100));
		final JPanel colorPanel = new JPanel(new GridBagLayout());
		// final JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		final JPanel sliderPanel = new JPanel(new GridBagLayout());
		gbc.insets = new Insets(5, 5, 5, 5);
		setGrid(gbc, 0, 0, 2);
		sliderPanel.add(redLabel, gbc);
		setGrid(gbc, 2, 0, 2);
		sliderPanel.add(greenLabel, gbc);
		setGrid(gbc, 4, 0, 2);
		sliderPanel.add(blueLabel, gbc);
		setGrid(gbc, 0, 1, 1);
		sliderPanel.add(x0RLabel, gbc);
		setGrid(gbc, 0, 2, 1);
		sliderPanel.add(x0RField, gbc);
		setGrid(gbc, 0, 3, 1);
		sliderPanel.add(x0RSlider, gbc);
		setGrid(gbc, 1, 1, 1);
		sliderPanel.add(aRLabel, gbc);
		setGrid(gbc, 1, 2, 1);
		sliderPanel.add(aRField, gbc);
		setGrid(gbc, 1, 3, 1);
		sliderPanel.add(aRSlider, gbc);
		setGrid(gbc, 2, 1, 1);
		sliderPanel.add(x0GLabel, gbc);
		setGrid(gbc, 2, 2, 1);
		sliderPanel.add(x0GField, gbc);
		setGrid(gbc, 2, 3, 1);
		sliderPanel.add(x0GSlider, gbc);
		setGrid(gbc, 3, 1, 1);
		sliderPanel.add(aGLabel, gbc);
		setGrid(gbc, 3, 2, 1);
		sliderPanel.add(aGField, gbc);
		setGrid(gbc, 3, 3, 1);
		sliderPanel.add(aGSlider, gbc);
		setGrid(gbc, 4, 1, 1);
		sliderPanel.add(x0BLabel, gbc);
		setGrid(gbc, 4, 2, 1);
		sliderPanel.add(x0BField, gbc);
		setGrid(gbc, 4, 3, 1);
		sliderPanel.add(x0BSlider, gbc);
		setGrid(gbc, 5, 1, 1);
		sliderPanel.add(aBLabel, gbc);
		setGrid(gbc, 5, 2, 1);
		sliderPanel.add(aBField, gbc);
		setGrid(gbc, 5, 3, 1);
		sliderPanel.add(aBSlider, gbc);
		gbc.insets = new Insets(5, 5, 20, 5);
		gbc.insets = new Insets(5, 5, 5, 5);
		setGrid(gbc, 0, 0, 1);
		colorPanel.add(csLabel, gbc);
		setGrid(gbc, 0, 1, 1);
		colorPanel.add(csChooser, gbc);
		setGrid(gbc, 0, 2, 1);
		colorPanel.add(scrollpane, gbc);
		setGrid(gbc, 1, 1, 1);
		contents.add(colorPanel, gbc);
		setGrid(gbc, 2, 1, 1);
		contents.add(sliderPanel, gbc);
		setGrid(gbc, 0, 2, 3);
		contents.add(buttons.getComponent(), gbc);
		gbc.gridwidth = 1;
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		pack();
	}

	private void setGrid(GridBagConstraints gbc, int gridx, int gridy, int width) {
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridwidth = width;
	}

	public void stateChanged(ChangeEvent changeEvent) {
		x0RField.setText(String.valueOf(x0RSlider.getValue() / 100.0));
		x0GField.setText(String.valueOf(x0GSlider.getValue() / 100.0));
		x0BField.setText(String.valueOf(x0BSlider.getValue() / 100.0));
		aRField.setText(String.valueOf(aRSlider.getValue() / 100.0));
		aGField.setText(String.valueOf(aGSlider.getValue() / 100.0));
		aBField.setText(String.valueOf(aBSlider.getValue() / 100.0));
		rainbowPanel.x0R = x0RSlider.getValue() / 100.0;
		rainbowPanel.x0G = x0GSlider.getValue() / 100.0;
		rainbowPanel.x0B = x0BSlider.getValue() / 100.0;
		rainbowPanel.sigR = aRSlider.getValue() / 100.0;
		rainbowPanel.sigG = aGSlider.getValue() / 100.0;
		rainbowPanel.sigB = aBSlider.getValue() / 100.0;
		rainbowPanel.repaint();
	}

}