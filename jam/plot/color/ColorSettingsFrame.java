package jam.plot.color;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
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

	private JComboBox colorSchemeComboBox;

	private final RainbowPanel elementVizRainbowPanel=new RainbowPanel();

	private JPanel colorPanel, buttonPanel, sliderPanel;

	private JSlider x0RSlider, x0GSlider, x0BSlider, aRSlider, aGSlider,
			aBSlider;

	private JTextField x0RField, x0GField, x0BField, aRField, aGField, aBField;

	static private final ColorSettingsFrame CSF = new ColorSettingsFrame();

	static public ColorSettingsFrame getSingletonInstance() {
		return CSF;
	}

	private ColorSettingsFrame() {
		final int x0R = (int) Math.round(100 * COLOR_PREFS.getDouble(
				ColorPrefs.X0R, .80));
		final int x0G = (int) Math.round(100 * COLOR_PREFS.getDouble(
				ColorPrefs.X0G, .60));
		final int x0B = (int) Math.round(100 * COLOR_PREFS.getDouble(
				ColorPrefs.X0B, .20));
		final int aR = (int) Math.round(100 * COLOR_PREFS.getDouble(
				ColorPrefs.ARED, .50));
		final int aG = (int) Math.round(100 * COLOR_PREFS.getDouble(
				ColorPrefs.AGREEN, .40));
		final int aB = (int) Math.round(100 * COLOR_PREFS.getDouble(
				ColorPrefs.ABLUE, .30));
		setTitle("Color Scale Settings");
		final Container c = getContentPane();
		c.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		final AbstractButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				apply();
				setVisible(false);
			}
		});
		final AbstractButton applyButton = new JButton("Apply");
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				apply();
			}
		});
		final AbstractButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				setVisible(false);
			}
		});
		x0RSlider = new JSlider(JSlider.VERTICAL, 0, 100, x0R);
		x0RSlider.addChangeListener(this);
		x0RSlider.setPreferredSize(new Dimension(20, 130));
		x0GSlider = new JSlider(JSlider.VERTICAL, 0, 100, x0G);
		x0GSlider.addChangeListener(this);
		x0GSlider.setPreferredSize(new Dimension(20, 130));
		x0BSlider = new JSlider(JSlider.VERTICAL, 0, 100, x0B);
		x0BSlider.addChangeListener(this);
		x0BSlider.setPreferredSize(new Dimension(20, 130));
		aRSlider = new JSlider(JSlider.VERTICAL, 0, 100, aR);
		aRSlider.addChangeListener(this);
		aRSlider.setPreferredSize(new Dimension(20, 130));
		aGSlider = new JSlider(JSlider.VERTICAL, 0, 100, aG);
		aGSlider.addChangeListener(this);
		aGSlider.setPreferredSize(new Dimension(20, 130));
		aBSlider = new JSlider(JSlider.VERTICAL, 0, 100, aB);
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
		aRField.setText(String.valueOf(aR / 100.0));
		aRField.setEditable(false);
		aGField = new JTextField(5);
		aGField.setText(String.valueOf(aG / 100.0));
		aGField.setEditable(false);
		aBField = new JTextField(5);
		aBField.setText(String.valueOf(aB / 100.0));
		aBField.setEditable(false);
		final JComponent minAbundLabel = new JLabel("Abundance min : ");
		final JComponent maxAbundLabel = new JLabel("Abundance max : ");
		final JComponent colorSchemeLabel = new JLabel(
				"<html>Choose a<p>color scheme :</html>");
		final String current="Current";
		final String rainbow="Rainbow";
		colorSchemeComboBox = new JComboBox();
		colorSchemeComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				final String selection = (String) colorSchemeComboBox
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
				} else {//current
					final int x0R = (int) Math.round(100 * COLOR_PREFS
							.getDouble(ColorPrefs.X0R, .80));
					final int x0G = (int) Math.round(100 * COLOR_PREFS
							.getDouble(ColorPrefs.X0G, .60));
					final int x0B = (int) Math.round(100 * COLOR_PREFS
							.getDouble(ColorPrefs.X0B, .20));
					final int aR = (int) Math.round(100 * COLOR_PREFS.getDouble(
							ColorPrefs.ARED, .50));
					final int aG = (int) Math.round(100 * COLOR_PREFS.getDouble(
							ColorPrefs.AGREEN, .40));
					final int aB = (int) Math.round(100 * COLOR_PREFS.getDouble(
							ColorPrefs.ABLUE, .30));
					x0RSlider.setValue(x0R);
					x0GSlider.setValue(x0G);
					x0BSlider.setValue(x0B);
					aRSlider.setValue(aR);
					aGSlider.setValue(aG);
					aBSlider.setValue(aB);
				}
			}
		});
		colorSchemeComboBox.addItem(current);
		colorSchemeComboBox.addItem(rainbow);
		colorSchemeComboBox.addItem("Purple Haze");
		colorSchemeComboBox.addItem("Greyscale");
		colorSchemeComboBox.setSelectedItem(rainbow);
		colorSchemeComboBox.setSelectedItem(current);
		final JScrollPane sp = new JScrollPane(elementVizRainbowPanel,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setPreferredSize(new Dimension(50, 100));
		colorPanel = new JPanel(new GridBagLayout());
		buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		sliderPanel = new JPanel(new GridBagLayout());
		gbc.insets = new Insets(5, 5, 5, 5);
		setGrid(gbc,0,0,2);
		sliderPanel.add(redLabel, gbc);
		setGrid(gbc,2,0,2);
		sliderPanel.add(greenLabel, gbc);
		setGrid(gbc,4,0,2);
		sliderPanel.add(blueLabel, gbc);
		setGrid(gbc,0,1,1);
		sliderPanel.add(x0RLabel, gbc);
		setGrid(gbc,0,2,1);
		sliderPanel.add(x0RField, gbc);
		setGrid(gbc,0,3,1);
		sliderPanel.add(x0RSlider, gbc);
		setGrid(gbc,1,1,1);
		sliderPanel.add(aRLabel, gbc);
		setGrid(gbc,1,2,1);
		sliderPanel.add(aRField, gbc);
		setGrid(gbc,1,3,1);
		sliderPanel.add(aRSlider, gbc);
		setGrid(gbc,2,1,1);
		sliderPanel.add(x0GLabel, gbc);
		setGrid(gbc,2,2,1);
		sliderPanel.add(x0GField, gbc);
		setGrid(gbc,2,3,1);
		sliderPanel.add(x0GSlider, gbc);
		setGrid(gbc,3,1,1);
		sliderPanel.add(aGLabel, gbc);
		setGrid(gbc,3,2,1);
		sliderPanel.add(aGField, gbc);
		setGrid(gbc,3,3,1);
		sliderPanel.add(aGSlider, gbc);
		setGrid(gbc,4,1,1);
		sliderPanel.add(x0BLabel, gbc);
		setGrid(gbc,4,2,1);
		sliderPanel.add(x0BField, gbc);
		setGrid(gbc,4,3,1);
		sliderPanel.add(x0BSlider, gbc);
		setGrid(gbc,5,1,1);
		sliderPanel.add(aBLabel, gbc);
		setGrid(gbc,5,2,1);
		sliderPanel.add(aBField, gbc);
		setGrid(gbc,5,3,1);
		sliderPanel.add(aBSlider, gbc);
		gbc.insets = new Insets(5, 5, 20, 5);
		gbc.insets = new Insets(5, 5, 5, 5);
		setGrid(gbc,0,0,1);
		colorPanel.add(colorSchemeLabel, gbc);
		setGrid(gbc,0,1,1);
		colorPanel.add(colorSchemeComboBox, gbc);
		setGrid(gbc,0,2,1);
		colorPanel.add(sp, gbc);
		buttonPanel.add(okButton);
		buttonPanel.add(applyButton);
		buttonPanel.add(cancelButton);
		setGrid(gbc,1,1,1);
		c.add(colorPanel, gbc);
		setGrid(gbc,2,1,1);
		c.add(sliderPanel, gbc);
		setGrid(gbc,0,2,3);
		c.add(buttonPanel, gbc);
		gbc.gridwidth = 1;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
	}
	
	private void setGrid(GridBagConstraints gbc, int x, int y, int width){
		gbc.gridx=x;
		gbc.gridy=y;
		gbc.gridwidth=width;
	}

	public void stateChanged(ChangeEvent ce) {
		x0RField.setText(String.valueOf(x0RSlider.getValue() / 100.0));
		x0GField.setText(String.valueOf(x0GSlider.getValue() / 100.0));
		x0BField.setText(String.valueOf(x0BSlider.getValue() / 100.0));
		aRField.setText(String.valueOf(aRSlider.getValue() / 100.0));
		aGField.setText(String.valueOf(aGSlider.getValue() / 100.0));
		aBField.setText(String.valueOf(aBSlider.getValue() / 100.0));
		elementVizRainbowPanel.x0R = x0RSlider.getValue() / 100.0;
		elementVizRainbowPanel.x0G = x0GSlider.getValue() / 100.0;
		elementVizRainbowPanel.x0B = x0BSlider.getValue() / 100.0;
		elementVizRainbowPanel.sigR = aRSlider.getValue() / 100.0;
		elementVizRainbowPanel.sigG = aGSlider.getValue() / 100.0;
		elementVizRainbowPanel.sigB = aBSlider.getValue() / 100.0;
		elementVizRainbowPanel.repaint();
	}

	public static void main(String[] args) {
		CSF.setVisible(true);
	}
	
	private void apply(){
		final double x0R = x0RSlider.getValue() / 100.0;
		final double x0G = x0GSlider.getValue() / 100.0;
		final double x0B = x0BSlider.getValue() / 100.0;
		final double aR = aRSlider.getValue() / 100.0;
		final double aG = aGSlider.getValue() / 100.0;
		final double aB = aBSlider.getValue() / 100.0;
		COLOR_PREFS.putDouble(ColorPrefs.ABLUE, aB);
		COLOR_PREFS.putDouble(ColorPrefs.AGREEN, aG);
		COLOR_PREFS.putDouble(ColorPrefs.ARED, aR);
		COLOR_PREFS.putDouble(ColorPrefs.X0B, x0B);
		COLOR_PREFS.putDouble(ColorPrefs.X0G, x0G);
		COLOR_PREFS.putDouble(ColorPrefs.X0R, x0R);
	}
}