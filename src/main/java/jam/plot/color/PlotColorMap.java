package jam.plot.color;

import java.awt.*;

/**
 * Color map for display.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 */
public final class PlotColorMap {

	private static final Color DARK_RED = new Color(192, 0, 0);

	static final private PlotColorMap MAP = new PlotColorMap(Mode.B_ON_W);

	private static final Color[] OVERLAY = { Color.RED, Color.GREEN,
			Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.ORANGE,
			DARK_RED };

	/**
	 * Returns the only instance of this class.
	 * 
	 * @return the only instance of this class
	 */
	static public PlotColorMap getInstance() {
		return MAP;
	}

	private transient Color area;

	private transient Color background;

	private transient Color fitBkgd;

	private transient Color fitResidual;

	private transient Color fitSignal;

	private transient Color fitTotal;

	private transient Color foreground;

	private transient Color gateDraw;

	private transient Color gateShow;

	private transient Color hist;

	private transient Color mark;

	private transient Color peakLabel;

	private PlotColorMap(final Mode mode) {
		super();
		setColorMap(mode);
	}

	/**
	 * Returns the area highlight color.
	 * 
	 * @return area highlight color
	 */
	public Color getArea() {
		synchronized (this) {
			return area;
		}
	}

	/**
	 * Get the background color.
	 * 
	 * @return background color
	 */
	public Color getBackground() {
		synchronized (this) {
			return background;
		}
	}

	/**
	 * Returns the color used to draw the fit background function.
	 * 
	 * @return fit background curve color
	 */
	public Color getFitBackground() {
		synchronized (this) {
			return fitBkgd;
		}
	}

	/**
	 * Returns the color used to draw the fit residuals.
	 * 
	 * @return fit residuals color
	 */
	public Color getFitResidual() {
		synchronized (this) {
			return fitResidual;
		}
	}

	/**
	 * Returns the color used to draw the fit signal function.
	 * 
	 * @return fit signal curve color
	 */
	public Color getFitSignal() {
		synchronized (this) {
			return fitSignal;
		}
	}

	/**
	 * Returns the color used to draw the total (signal+background) fit
	 * function.
	 * 
	 * @return fit total curve color
	 */
	public Color getFitTotal() {
		synchronized (this) {
			return fitTotal;
		}
	}

	/**
	 * Get the foreground color.
	 * 
	 * @return foreground color
	 */
	public Color getForeground() {
		synchronized (this) {
			return foreground;
		}
	}

	/**
	 * Returns the gate setting color.
	 * 
	 * @return the gate setting color
	 */
	public Color getGateDraw() {
		synchronized (this) {
			return gateDraw;
		}
	}

	/**
	 * Returns the gate display color.
	 * 
	 * @return the gate display color
	 */
	public Color getGateShow() {
		synchronized (this) {
			return gateShow;
		}
	}

	/**
	 * Returns the color to draw histogram lines.
	 * 
	 * @return histogram curve color
	 */
	public Color getHistogram() {
		synchronized (this) {
			return hist;
		}
	}

	/**
	 * Returns the color for marked channels.
	 * 
	 * @return marked channels color
	 */
	public Color getMark() {
		synchronized (this) {
			return mark;
		}
	}

	/**
	 * Returns the color for the index'th overlay curve.
	 * 
	 * @param index
	 *            which overlay, counting starts at 0
	 * @return overlay curve color
	 */
	public Color getOverlay(final int index) {
		synchronized (this) {
			return OVERLAY[index % OVERLAY.length];
		}
	}

	/**
	 * Returns the color used to label automatically found peaks.
	 * 
	 * @return peak label color
	 */
	public Color getPeakLabel() {
		synchronized (this) {
			return peakLabel;
		}
	}

	/**
	 * Sets the color mappings for the given mode.
	 * 
	 * @param mode
	 *            what type of graphics context
	 */
	public void setColorMap(final Mode mode) {
		synchronized (this) {
			if (mode == Mode.B_ON_W) {
				background = Color.WHITE;
				foreground = Color.DARK_GRAY;
				hist = Color.BLACK;
				gateDraw = Color.GREEN;
				gateShow = Color.RED;
				mark = Color.RED;
				area = Color.GREEN;
				fitTotal = Color.BLUE;
				fitSignal = Color.DARK_GRAY;
				fitBkgd = Color.GREEN;
				fitResidual = Color.RED;
				peakLabel = Color.BLUE;
			} else if (mode == Mode.W_ON_B) {
				background = Color.BLACK;
				foreground = Color.LIGHT_GRAY;
				hist = Color.WHITE;
				gateShow = Color.RED;
				gateDraw = Color.GREEN;
				mark = Color.YELLOW;
				area = Color.GREEN;
				fitTotal = Color.CYAN;
				fitSignal = Color.LIGHT_GRAY;
				fitBkgd = Color.GREEN;
				fitResidual = Color.RED;
				peakLabel = Color.CYAN;
			} else if (mode == Mode.PRINT) {
				background = Color.WHITE;
				foreground = Color.BLACK;
				hist = Color.BLACK;
				gateDraw = new Color(59, 59, 59);
				gateShow = new Color(59, 59, 59);
				mark = new Color(102, 102, 102);
				area = new Color(102, 102, 102);
				fitTotal = Color.BLUE;
				fitSignal = Color.DARK_GRAY;
				fitBkgd = Color.GREEN;
				fitResidual = Color.RED;
				peakLabel = Color.BLUE;
			} else {
				throw new IllegalArgumentException(
						"PlotGraphicsColorMap.setColorMap(" + mode
								+ "): Invalid Color Mode!");
			}
			DiscreteColorScale.setColors(mode);
		}
	}
}
