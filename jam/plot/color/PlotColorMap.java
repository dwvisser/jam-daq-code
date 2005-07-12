package jam.plot.color;

import java.awt.Color;

/**
 * Color map for display.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 */
public class PlotColorMap implements GraphicsModes{

	private static final Color DARK_RED = new Color(192, 0, 0);

	private transient Color background;

	private transient Color foreground;

	private transient Color hist;

	private static final Color[] OVERLAY = { Color.RED, Color.GREEN,
			Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.ORANGE,
			DARK_RED };

	private transient Color gateDraw;

	private transient Color gateShow;

	private transient Color fitTotal;

	private transient Color fitSignal;

	private transient Color fitBkgd;

	private transient Color fitResidual;

	private transient Color mark;

	private transient Color area;

	private transient Color peakLabel;

	static final private PlotColorMap MAP = new PlotColorMap(modes.B_ON_W);

	private PlotColorMap(modes mode) {
		super();
		setColorMap(mode);
	}

	/**
	 * Returns the only instance of this class.
	 * 
	 * @return the only instance of this class
	 */
	static public PlotColorMap getInstance() {
		return MAP;
	}

	/**
	 * Sets the color mappings for the given mode.
	 * 
	 * @param mode
	 *            what type of graphics context
	 */
	public void setColorMap(final modes mode) {
		synchronized (this) {
			if (mode == modes.B_ON_W) {
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
			} else if (mode == modes.W_ON_B) {
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
			} else if (mode == modes.PRINT) {
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
	 * Get the background color.
	 * 
	 * @return background color
	 */
	public Color getBackground() {
		synchronized (this){
			return background;
		}
	}

	/**
	 * Returns the gate display color.
	 * 
	 * @return the gate display color
	 */
	synchronized public Color getGateShow() {
		return gateShow;
	}

	/**
	 * Returns the gate setting color.
	 * 
	 * @return the gate setting color
	 */
	synchronized public Color getGateDraw() {
		return gateDraw;
	}

	/**
	 * Returns the color for marked channels.
	 * 
	 * @return marked channels color
	 */
	synchronized public Color getMark() {
		return mark;
	}

	/**
	 * Returns the area highlight color.
	 * 
	 * @return area highlight color
	 */
	synchronized public Color getArea() {
		return area;
	}

	/**
	 * Returns the color to draw histogram lines.
	 * 
	 * @return histogram curve color
	 */
	synchronized public Color getHistogram() {
		return hist;
	}

	/**
	 * Returns the color used to draw the fit background function.
	 * 
	 * @return fit background curve color
	 */
	synchronized public Color getFitBackground() {
		return fitBkgd;
	}

	/**
	 * Returns the color used to draw the fit residuals.
	 * 
	 * @return fit residuals color
	 */
	synchronized public Color getFitResidual() {
		return fitResidual;
	}

	/**
	 * Returns the color used to draw the total (signal+background) fit
	 * function.
	 * 
	 * @return fit total curve color
	 */
	synchronized public Color getFitTotal() {
		return fitTotal;
	}

	/**
	 * Returns the color used to draw the fit signal function.
	 * 
	 * @return fit signal curve color
	 */
	synchronized public Color getFitSignal() {
		return fitSignal;
	}

	/**
	 * Returns the color for the index'th overlay curve.
	 * 
	 * @param index
	 *            which overlay, counting starts at 0
	 * @return overlay curve color
	 */
	synchronized public Color getOverlay(final int index) {
		return OVERLAY[index % OVERLAY.length];
	}

	/**
	 * Returns the color used to label automatically found peaks.
	 * 
	 * @return peak label color
	 */
	synchronized public Color getPeakLabel() {
		return peakLabel;
	}
}
