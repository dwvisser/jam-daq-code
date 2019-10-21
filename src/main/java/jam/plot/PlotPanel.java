package jam.plot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import jam.data.AbstractHistogram;
import jam.plot.color.Mode;
import jam.plot.color.PlotColorMap;

@SuppressWarnings("serial")
final class PlotPanel extends JPanel implements CountsContainer {

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
		@Override
		public void mouseExited(final MouseEvent mouseEvent) {
			setMouseMoved(false);
			repaint();
		}

		@Override
		public void mouseMoved(final MouseEvent mouseEvent) {
			plot.mouseMoved(mouseEvent);
		}
	};

	private transient boolean mouseMove = false;

	private transient PageFormat pageformat = null;

	private transient final Plot plot;

	/**
	 * currently selecting an area?
	 */
	private transient boolean selectingArea = false;

	/**
	 * Currently setting a gate.
	 */
	private transient boolean settingGate = false;

	PlotPanel(final Plot plot) {
		super(false);
		this.plot = plot;
	}

	public Object getCounts() {
		return plot.getCounts();
	}

	protected boolean isAreaMarked() {
		synchronized (this) {
			return areaMarked;
		}
	}

	protected boolean isDisplayingOverlay() {
		synchronized (this) {
			return displayingOverlay;
		}
	}

	protected boolean isMarkingChannels() {
		synchronized (this) {
			return markingChannels;
		}
	}

	protected boolean isSelectingArea() {
		synchronized (this) {
			return selectingArea;
		}
	}

	protected boolean isSettingGate() {
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
	@Override
	protected void paintComponent(final Graphics graphics) {
		super.paintComponent(graphics);
		final PlotColorMap pcm = PlotColorMap.getInstance();
		if (plot.isPrinting()) { // output to printer
			// FIXME KBS font not set
			// graph.setFont(printFont);
			pcm.setColorMap(Mode.PRINT);
			plot.setView(pageformat);
		} else { // output to screen
			// graph.setFont(screenFont);
			pcm.setColorMap(colorMode);
			plot.setView(null);
		}
		final Color foreground = pcm.getForeground();
		graphics.setColor(foreground);
		this.setForeground(foreground);
		this.setBackground(pcm.getBackground());
		plot.setViewSize(getSize());
		plot.update(graphics);
		/*
		 * give graph all pertinent info, draw outline, tickmarks, labels, and
		 * title
		 */
		final AbstractHistogram plotHist = plot.getHistogram();
		if (plotHist != null) {
			plot.paintHeader(graphics);
			plot.paintHistogram(graphics);
			paintAdditional(graphics);
		}
	}

	protected void setAreaMarked(final boolean state) {
		synchronized (this) {
			areaMarked = state;
		}
	}

	protected void setColorMode(final boolean color) {
		synchronized (this) {
			colorMode = color ? Mode.W_ON_B : Mode.B_ON_W;
		}
		setBackground(PlotColorMap.getInstance().getBackground());
	}

	protected void setDisplayingFit(final boolean state) {
		synchronized (this) {
			displayingFit = state;
		}
	}

	protected void setDisplayingGate(final boolean state) {
		synchronized (this) {
			displayingGate = state;
		}
	}

	protected void setDisplayingOverlay(final boolean state) {
		synchronized (this) {
			displayingOverlay = state;
		}
	}

	protected void setListenToMouse(final boolean listen) {
		if (listen) {
			addMouseListener(mouseInputAdapter);
		} else {
			removeMouseListener(mouseInputAdapter);
		}
	}

	protected void setListenToMouseMotion(final boolean listen) {
		if (listen) {
			addMouseMotionListener(mouseInputAdapter);
		} else {
			removeMouseMotionListener(mouseInputAdapter);
		}
	}

	protected void setMarkingChannels(final boolean state) {
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

	protected void setPageFormat(final PageFormat format) {
		synchronized (this) {
			pageformat = format;
		}
	}

	protected void setSelectingArea(final boolean selecting) {
		synchronized (this) {
			selectingArea = selecting;
			setListenToMouseMotion(selecting);
			if (!selectingArea) {
				repaint();
			}
		}
	}

	protected void setSettingGate(final boolean state) {
		synchronized (this) {
			settingGate = state;
		}
	}
}