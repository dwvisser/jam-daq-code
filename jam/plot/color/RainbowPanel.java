/*
 * Created on Nov 8, 2004
 */
package jam.plot.color;

import javax.swing.*;
import java.awt.*;

/**
 * @author Eric Lingerfelt
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser </a>
 */
final class RainbowPanel extends JPanel {

	private transient double sigB = 0.30;

	private transient double sigG = 0.40;

	private transient double sigR = 0.50;

	private transient double x0B = 0.20;

	private transient double x0G = 0.60;

	private transient double x0R = 0.80;

	RainbowPanel() {
		super();
		setSize(100, 50);
	}

	@Override
	public void paintComponent(final Graphics graphics) {
		final Graphics2D graph2d = (Graphics2D) graphics;
		super.paintComponent(graph2d);
		final RenderingHints hints = new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		graph2d.setRenderingHints(hints);
		for (int i = 0; i < 100; i++) {
			graph2d.setColor(GradientColorScale.getRGB(i / 100.0, x0R, sigR,
					x0G, sigG, x0B, sigB));
			graph2d.drawLine(0, 100 - i, 50, 100 - i);
		}
		graph2d.setColor(Color.black);
		graph2d.drawString("Max", 9, 18);
		graph2d.setColor(Color.white);
		graph2d.drawString("Min", 10, 92);
	}

	protected void setSpecs(final double xor, final double xog,
			final double xob, final double sigr, final double sigg,
			final double sigb) {
		synchronized (this) {
			x0R = xor;
			x0G = xog;
			x0B = xob;
			sigR = sigr;
			sigG = sigg;
			sigB = sigb;
		}
	}

}