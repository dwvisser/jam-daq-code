/*
 * Created on Nov 8, 2004
 */
package jam.plot.color;

/**
 * @author Eric Lingerfelt
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 */
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
import java.text.DecimalFormat;
import java.text.FieldPosition;

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

public class ColorSettingsFrame extends JDialog implements ItemListener,
		ChangeListener,ColorPrefs {

	JComboBox colorSchemeComboBox;

	RainbowPanel elementVizRainbowPanel;

	JPanel colorPanel, buttonPanel, sliderPanel;

	JSlider x0RSlider, x0GSlider, x0BSlider, aRSlider, aGSlider, aBSlider;

	JTextField x0RField, x0GField, x0BField, aRField, aGField, aBField;

	private static final double log10 = 1.0 / Math.log(10);//0.434294482;

	static private final ColorSettingsFrame csf = new ColorSettingsFrame();
	
	static public ColorSettingsFrame getSingletonInstance(){
		return csf;
	}

	private ColorSettingsFrame() {
		final int x0R = colorPrefs.getInt(ColorPrefs.X0R,80);
		final int x0G = colorPrefs.getInt(ColorPrefs.X0G,60);
		final int x0B = colorPrefs.getInt(ColorPrefs.X0B,20);
		final int aR = colorPrefs.getInt(ColorPrefs.ARED,50);
		final int aG = colorPrefs.getInt(ColorPrefs.AGREEN,40);
		final int aB = colorPrefs.getInt(ColorPrefs.ABLUE,30);
		setTitle("Color Scale Settings");
		setSize(825, 370);
		final Container c = getContentPane();
		c.setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		AbstractButton defaultButton = new JButton("Default Settings");
		defaultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				x0RSlider.setValue(80);
				x0GSlider.setValue(60);
				x0BSlider.setValue(20);
				aRSlider.setValue(50);
				aGSlider.setValue(40);
				aBSlider.setValue(30);
				colorSchemeComboBox.removeItemListener(ColorSettingsFrame.this);
				colorSchemeComboBox.setSelectedItem("Rainbow");
				colorSchemeComboBox.addItemListener(ColorSettingsFrame.this);
			}
		});
		AbstractButton applyButton = new JButton("Apply Settings");
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 final double x0R =x0RSlider .getValue() / 100.0;
				 final double x0G = x0GSlider .getValue() / 100.0;
				 final double x0B =x0BSlider .getValue() / 100.0;
				 final double aR =aRSlider .getValue() / 100.0;
				 final double aG =aGSlider .getValue() / 100.0;
				 final double aB = aBSlider .getValue() / 100.0;
				 colorPrefs.putDouble(ColorPrefs.ABLUE,aB);
				 colorPrefs.putDouble(ColorPrefs.AGREEN,aG);
				 colorPrefs.putDouble(ColorPrefs.ARED,aR);
				 colorPrefs.putDouble(ColorPrefs.X0B,x0B);
				 colorPrefs.putDouble(ColorPrefs.X0G,x0G);
				 colorPrefs.putDouble(ColorPrefs.X0R,x0R);
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
		colorSchemeComboBox = new JComboBox();
		colorSchemeComboBox.addItemListener(this);
		colorSchemeComboBox.addItem("Rainbow");
		colorSchemeComboBox.addItem("Purple Haze");
		colorSchemeComboBox.addItem("Greyscale");
		elementVizRainbowPanel = new RainbowPanel();
		final JScrollPane sp = new JScrollPane(elementVizRainbowPanel,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setPreferredSize(new Dimension(50, 100));
		colorPanel = new JPanel(new GridBagLayout());
		buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		sliderPanel = new JPanel(new GridBagLayout());
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		sliderPanel.add(redLabel, gbc);
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		sliderPanel.add(greenLabel, gbc);
		gbc.gridx = 4;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		sliderPanel.add(blueLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		sliderPanel.add(x0RLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		sliderPanel.add(x0RField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		sliderPanel.add(x0RSlider, gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		sliderPanel.add(aRLabel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		sliderPanel.add(aRField, gbc);
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		sliderPanel.add(aRSlider, gbc);
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		sliderPanel.add(x0GLabel, gbc);
		gbc.gridx = 2;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		sliderPanel.add(x0GField, gbc);
		gbc.gridx = 2;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		sliderPanel.add(x0GSlider, gbc);
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		sliderPanel.add(aGLabel, gbc);
		gbc.gridx = 3;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		sliderPanel.add(aGField, gbc);
		gbc.gridx = 3;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		sliderPanel.add(aGSlider, gbc);
		gbc.gridx = 4;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		sliderPanel.add(x0BLabel, gbc);
		gbc.gridx = 4;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		sliderPanel.add(x0BField, gbc);
		gbc.gridx = 4;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		sliderPanel.add(x0BSlider, gbc);
		gbc.gridx = 5;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		sliderPanel.add(aBLabel, gbc);
		gbc.gridx = 5;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		sliderPanel.add(aBField, gbc);
		gbc.gridx = 5;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		sliderPanel.add(aBSlider, gbc);
		gbc.insets = new Insets(5, 5, 20, 5);
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		colorPanel.add(colorSchemeLabel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		colorPanel.add(colorSchemeComboBox, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		colorPanel.add(sp, gbc);
		buttonPanel.add(defaultButton);
		buttonPanel.add(applyButton);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		c.add(colorPanel, gbc);
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		c.add(sliderPanel, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		c.add(buttonPanel, gbc);
		gbc.gridwidth = 1;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		validate();
	}

	NumberRange range = new NumberRange() {
		public double getMinimum() {
			return 1;
		}

		public double getMaximum() {
			return 100;
		}
	};

	public int getIntegerAbundValue(double abundance) {
		double normValue = setLogScale(abundance, range.getMaximum(), range
				.getMinimum(), getLogConstant());
		return (int) Math.round(normValue * 1000);
	}

	public double setLogScale(double abundance, double max, double min, double C) {
		if (abundance != 0.0) {
			double normValue = C * log10 * Math.log(abundance) - C * log10
					* Math.log(min);
			return normValue;
		} else {
			double normValue = 0.0;
			return normValue;
		}
	}

	public double getLogConstant() {
		return 1.0 / (log10 * Math.log(range.getMaximum() / range.getMinimum()));
	}

	public double getDoubleAbundValue(int sliderValue) {
		final double normValue = ((double) sliderValue) / 1000.0;
		final double constant = 1.0 / getLogConstant();
		final double exponent = constant * normValue + log10
				* Math.log(range.getMinimum());
		return Math.pow(10, exponent);
	}

	public String getFormattedAbundance(double number) {
		final DecimalFormat decimalFormat = new DecimalFormat("0.00000E00");
		final FieldPosition fp = new FieldPosition(0);
		String string = decimalFormat.format(number, new StringBuffer(), fp)
				.toString();
		if (!string.substring(8, 9).equals("-")) {
			String[] tempArray = new String[2];
			tempArray = string.split("E");
			string = tempArray[0] + "E" + "+" + tempArray[1];
		}
		return string;
	}

	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == colorSchemeComboBox) {
			if (((String) colorSchemeComboBox.getSelectedItem())
					.equals("Greyscale")) {
				x0RSlider.setValue(100);
				x0GSlider.setValue(100);
				x0BSlider.setValue(100);
				aRSlider.setValue(100);
				aGSlider.setValue(100);
				aBSlider.setValue(100);
			} else if (((String) colorSchemeComboBox.getSelectedItem())
					.equals("Rainbow")) {
				x0RSlider.setValue(80);
				x0GSlider.setValue(60);
				x0BSlider.setValue(20);
				aRSlider.setValue(50);
				aGSlider.setValue(40);
				aBSlider.setValue(30);
			} else if (((String) colorSchemeComboBox.getSelectedItem())
					.equals("Purple Haze")) {
				x0RSlider.setValue(100);
				x0GSlider.setValue(0);
				x0BSlider.setValue(100);
				aRSlider.setValue(84);
				aGSlider.setValue(0);
				aBSlider.setValue(84);
			}
		}
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
		elementVizRainbowPanel.aR = aRSlider.getValue() / 100.0;
		elementVizRainbowPanel.aG = aGSlider.getValue() / 100.0;
		elementVizRainbowPanel.aB = aBSlider.getValue() / 100.0;
		elementVizRainbowPanel.repaint();
	}

	public static void main(String[] args) {
		csf.setVisible(true);
	}
}