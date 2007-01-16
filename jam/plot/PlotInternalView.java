package jam.plot;

/**
 * Defines the pixel coordinates that contain the plot itself. 
 * I.e., doesn't count labels or axes, just internal points to
 * the plot.
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

	void setRight(final int value) {
		right = value;
	}

	int getRight() {
		return right;
	}

	void setBottom(final int value) {
		bottom = value;
	}

	int getBottom() {
		return bottom;
	}
	
	void setWidth(final int value){
		width=value;
	}
	
	int getWidth(){
		return width;
	}
	void setHeight(final int value){
		height=value;
	}
	
	int getHeight(){
		return height;
	}
}