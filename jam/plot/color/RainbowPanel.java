/*
 * Created on Nov 8, 2004
 */
package jam.plot.color;

/**
 * @author Eric Lingerfelt
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class RainbowPanel extends JPanel {

	Font titleFont = new Font("SanSerif", Font.BOLD, 12);

	FontMetrics titleFontMetrics = getFontMetrics(titleFont);

	Font buttonFont = new Font("SanSerif", Font.BOLD, 15);

	FontMetrics buttonFontMetrics = getFontMetrics(buttonFont);

	Font textFont = new Font("SanSerif", Font.PLAIN, 15);

	FontMetrics textFontMetrics = getFontMetrics(textFont);

	double x0R = 0.80;

	double x0G = 0.60;

	double x0B = 0.20;

	double aR = 0.50;

	double aG = 0.40;

	double aB = 0.30;

	public RainbowPanel() {

		setSize(100, 50);

	}

	public Color getRGB(double x) {

		if (x >= 1.0) {

			x = 1.0;

		}

		if (x <= 0.0) {

			x = 0.0;

		}

		int red = (int) (255 * Math.exp(-(x - x0R) * (x - x0R) / aR / aR));
		int green = (int) (255 * Math.exp(-(x - x0G) * (x - x0G) / aG / aG));
		int blue = (int) (255 * Math.exp(-(x - x0B) * (x - x0B) / aB / aB));

		return new Color(red, green, blue);
	}

	public void paintComponent(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;

		super.paintComponent(g2);

		RenderingHints hints = new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g2.setRenderingHints(hints);

		for (int i = 0; i < 100; i++) {

			g2.setColor(getRGB((double) i / 100.0));
			g2.drawLine(0, 100 - i, 50, 100 - i);

		}

		g2.setFont(buttonFont);
		g2.setColor(Color.black);
		g2.drawString("Max", 9, 18);

		g2.setColor(Color.white);
		g2.drawString("Min", 10, 92);

	}

}