/*
 * Created on Nov 8, 2004
 */
package jam.plot.color;

/**
 * @author Eric Lingerfelt
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 */
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

final class RainbowPanel extends JPanel {
	
	transient double x0R = 0.80;
	transient double x0G = 0.60;
	transient double x0B = 0.20;
	transient double sigR = 0.50;
	transient double sigG = 0.40;
	transient double sigB = 0.30;

	RainbowPanel() {
		setSize(100, 50);
	}

	final Color getRGB(final double count) {
		double level=count;
		if (level >= 1.0) {
			level = 1.0;
		}
		if (level <= 0.0) {
			level = 0.0;
		}
		final int red = (int) (255 * Math.exp(-(level - x0R) * (level - x0R) / sigR / sigR));
		final int green = (int) (255 * Math.exp(-(level - x0G) * (level - x0G) / sigG / sigG));
		final int blue = (int) (255 * Math.exp(-(level - x0B) * (level - x0B) / sigB / sigB));
		return new Color(red, green, blue);
	}

	public void paintComponent(Graphics graphics) {
		final Graphics2D graph2d = (Graphics2D) graphics;
		super.paintComponent(graph2d);
		RenderingHints hints = new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graph2d.setRenderingHints(hints);
		for (int i = 0; i < 100; i++) {
			graph2d.setColor(getRGB((double) i / 100.0));
			graph2d.drawLine(0, 100 - i, 50, 100 - i);
		}
		graph2d.setColor(Color.black);
		graph2d.drawString("Max", 9, 18);
		graph2d.setColor(Color.white);
		graph2d.drawString("Min", 10, 92);
	}

}