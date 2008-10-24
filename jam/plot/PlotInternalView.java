package jam.plot;

/**
 * Defines the pixel coordinates that contain the plot itself. I.e., doesn't
 * count labels or axes, just internal points to the plot.
 * 
 * @author Dale Visser
 */
final class PlotInternalView {

	PlotInternalView() {
		super();
	}

	private transient int right;

	private transient int bottom;

	private transient int width;

	private transient int height;

	protected void setRight(final int value) {
		right = value;
	}

	protected int getRight() {
		return right;
	}

	protected void setBottom(final int value) {
		bottom = value;
	}

	protected int getBottom() {
		return bottom;
	}

	protected void setWidth(final int value) {
		width = value;
	}

	protected int getWidth() {
		return width;
	}

	protected void setHeight(final int value) {
		height = value;
	}

	protected int getHeight() {
		return height;
	}
}