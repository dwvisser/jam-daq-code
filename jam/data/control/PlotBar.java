/*
 */
package jam.data.control;
import jam.data.Monitor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.font.FontRenderContext;

import javax.swing.JPanel;

/**
 * Class that is a bar graph used by monitors
 *
 * @version 0.5
 * @author Ken Swartz
 */
public final class PlotBar extends JPanel implements PlotBarLayout {

	protected Dimension pageSize;
	private Monitor monitor;
	static final private Dimension MIN_SIZE=new Dimension(BAR_LENGTH,BAR_WIDTH);
	
	/**
	 * Constructor
	 */
	public PlotBar(Monitor m) {
		setMonitor(m);
		setBackground(SystemColor.control);
		setForeground(SystemColor.controlHighlight);
		setMinimumSize(MIN_SIZE);
		setPreferredSize(MIN_SIZE);
	}

	/**
	 * Sets the monitor object which is observed by this PlotBar.
	 *
	 * @param monitor to be observed
	 */
	public synchronized void setMonitor(Monitor m) {
		monitor = m;
	}

	/**
	 * get the monitor that is ploted
	 */
	public synchronized Monitor getMonitor() {
		return monitor;
	}

	/**
	 * paint method that is called to redraw widget
	 */
	public synchronized void paintComponent(Graphics g) {
		int plotLength;
		final int thresholdLine;

		super.paintComponent(g);
		final Graphics2D g2=(Graphics2D)g;		
		final double value = monitor.getValue();
		final double threshold = monitor.getThreshold();
		final double maximum = monitor.getMaximum();
		final Dimension dim = getSize();
		/* orientation of plot and size */
		final int length = dim.width - 2 * BORDER_END;
		final int height = dim.height - 2 * BORDER_SIDE;
		/* make sure input is OK */
		if (maximum > 0) {
			plotLength = (int) (length * value / maximum);
			thresholdLine = (int) (length * threshold / maximum);
		} else {
			plotLength = 0;
			thresholdLine = 0;
		}

		if (plotLength >= length) {
			plotLength = length - 1;
		}
		g2.setColor(SystemColor.control);
		g2.fillRect(BORDER_END, BORDER_SIDE, length, height - 1);
		g2.setColor(SystemColor.textText);
		g2.drawRect(BORDER_END, BORDER_SIDE, length, height - 1);
		/* Draw bar color depending on threshold and maximum */
		final Color barColor = (value < threshold) || (value > maximum) ?
		Color.RED : Color.GREEN;
		g2.setColor(barColor);
		g2.fillRect(BORDER_END + 1, BORDER_SIDE + 1, plotLength, height - 2);
		/* draw threshold */
		g2.setColor(SystemColor.textText);
		g2.drawLine(
			BORDER_END + thresholdLine,
			BORDER_SIDE + (height / 2),
			BORDER_END + thresholdLine,
			BORDER_SIDE + height - 1);
		final int midx=(BORDER_END+length)/2;
		final int midy=(BORDER_SIDE+height)/2;
		final String sValue=String.valueOf(value);
		final FontRenderContext frc=g2.getFontRenderContext();
		final Rectangle bounds=g2.getFont().getStringBounds(
		sValue,frc).getBounds();
		g2.drawString(sValue,midx-bounds.width/2,midy+bounds.height/2);
	}
}
