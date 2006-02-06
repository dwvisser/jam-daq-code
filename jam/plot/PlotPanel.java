/**
 * 
 */
package jam.plot;

import jam.data.Histogram;
import jam.plot.color.Mode;
import jam.plot.color.PlotColorMap;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

final class PlotPanel extends JPanel {

	/**
	 * currently have an area already marked?
	 */
	private transient boolean areaMarked = false;

	private transient Mode colorMode;

	/**
	 * currently displaying a fit?
	 */
	private transient boolean displayingFit = false;

	/**
	 * currently displaying a gate?
	 */
	private transient boolean displayingGate = false;

	/**
	 * currently displaying an overlay?
	 */
	private transient boolean displayingOverlay = false;

	/**
	 * currently have individual channels already marked?
	 */
	private transient boolean markingChannels = false;

	/**
	 * Anonymous implementation to handle mouse input.
	 */
	private transient final MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() {
		/**
		 * Undo last temporary line drawn.
		 * 
		 * @param mouseEvent
		 *            created when mouse exits the plot
		 */
		public void mouseExited(final MouseEvent mouseEvent) {
			setMouseMoved(false);
			repaint();
		}

		public void mouseMoved(final MouseEvent mouseEvent) {
			plot.mouseMoved(mouseEvent);
		}
	};

	private transient boolean mouseMove = false;

	private transient PageFormat pageformat = null;

	private transient final AbstractPlot plot;

	/**
	 * currently selecting an area?
	 */
	private transient boolean selectingArea = false;

	/**
	 * Currently setting a gate.
	 */
	private transient boolean settingGate = false;

	PlotPanel(AbstractPlot plot) {
		super(false);
		this.plot = plot;
	}

	/**
	 * @return the container class instance
	 */
	public AbstractPlot getPlot() {
		return plot;
	}

	boolean isAreaMarked() {
		synchronized (this) {
			return areaMarked;
		}
	}

	boolean isDisplayingOverlay() {
		synchronized (this) {
			return displayingOverlay;
		}
	}

	boolean isMarkingChannels() {
		synchronized (this) {
			return markingChannels;
		}
	}

	boolean isSelectingArea() {
		synchronized (this) {
			return selectingArea;
		}
	}

	boolean isSettingGate() {
		synchronized (this) {
			return settingGate;
		}
	}

	private void paintAdditional(final Graphics graphics) {
		if (displayingGate) { // are we to display a gate
			plot.paintGate(graphics);
		}
		if (displayingOverlay) {
			plot.paintOverlay(graphics);
		}
		if (displayingFit) {
			plot.paintFit(graphics);
		}
		if (areaMarked) {
			plot.paintMarkArea(graphics);
		}
		if (settingGate) {
			plot.paintSetGatePoints(graphics);
		}
		if (markingChannels) {
			plot.paintMarkedChannels(graphics);
		}
		if (mouseMove) {
			/* we handle selecting area or setting gate here */
			plot.paintMouseMoved(graphics);
		}
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	protected void paintComponent(final Graphics graphics) {
		super.paintComponent(graphics);
		final PlotColorMap pcm = PlotColorMap.getInstance();
		if (plot.printing) { // output to printer
			// FIXME KBS font not set
			// graph.setFont(printFont);
			pcm.setColorMap(Mode.PRINT);
			plot.graph.setView(pageformat);
		} else { // output to screen
			// graph.setFont(screenFont);
			pcm.setColorMap(colorMode);
			plot.graph.setView(null);
		}
		final Color foreground = pcm.getForeground();
		graphics.setColor(foreground); // color foreground
		this.setForeground(foreground);
		this.setBackground(pcm.getBackground());
		plot.viewSize = getSize();
		plot.graph.update(graphics, plot.viewSize, plot.plotLimits);
		/*
		 * give graph all pertinent info, draw outline, tickmarks, labels, and
		 * title
		 */
		final Histogram plotHist = plot.getHistogram();
		if (plotHist != null) {
			plot.paintHeader(graphics);
			if (plot.binWidth > plotHist.getSizeX()) {
				plot.binWidth = 1.0;
				plot
						.warning("Bin width > hist size, so setting bin width back to 1.");
			}
			plot.paintHistogram(graphics);
			paintAdditional(graphics);
		}
	}

	void setAreaMarked(final boolean state) {
		synchronized (this) {
			areaMarked = state;
		}
	}

	void setColorMode(final boolean color) {
		synchronized (this) {
			colorMode = color ? Mode.W_ON_B : Mode.B_ON_W;
		}
		setBackground(PlotColorMap.getInstance().getBackground());
	}

	void setDisplayingFit(final boolean state) {
		synchronized (this) {
			displayingFit = state;
		}
	}

	void setDisplayingGate(final boolean state) {
		synchronized (this) {
			displayingGate = state;
		}
	}

	void setDisplayingOverlay(final boolean state) {
		synchronized (this) {
			displayingOverlay = state;
		}
	}

	void setListenToMouse(final boolean listen) {
		if (listen) {
			addMouseListener(mouseInputAdapter);
		} else {
			removeMouseListener(mouseInputAdapter);
		}
	}

	void setListenToMouseMotion(final boolean listen) {
		if (listen) {
			addMouseMotionListener(mouseInputAdapter);
		} else {
			removeMouseMotionListener(mouseInputAdapter);
		}
	}

	void setMarkingChannels(final boolean state) {
		synchronized (this) {
			markingChannels = state;
		}
	}

	/**
	 * Sets whether the mouse is moving.
	 * 
	 * @param moved
	 *            <code>true</code> if the mouse is moving
	 */
	protected void setMouseMoved(final boolean moved) {
		synchronized (this) {
			mouseMove = moved;
		}
	}

	void setPageFormat(final PageFormat format) {
		synchronized (this) {
			pageformat = format;
		}
	}

	void setSelectingArea(final boolean selecting) {
		synchronized (this) {
			selectingArea = selecting;
			setListenToMouseMotion(selecting);
			if (!selectingArea) {
				repaint();
			}
		}
	}

	void setSettingGate(final boolean state) {
		synchronized (this) {
			settingGate = state;
		}
	}
}