package jam.plot;

/**
 * Layout constants for plotgraphics, has default size for some things.
 * 
 * @version 0.5
 * @author Ken Swartz
 */
final class PlotGraphicsLayout {

	//LayoutType full plot
	static final int LAYOUT_TYPE_LABELS = 0;

	//LayoutType tiled plots
	static final int LAYOUT_TYPE_NO_LABELS = 1;

	//border outside of plot
	final int BORDER_TOP;

	final int BORDER_LEFT;

	final int BORDER_BOTTOM;

	final int BORDER_RIGHT;

	//tickmark stuff
	final int TICK_SIZE;

	final int TICK_MINOR;

	final int TICK_MAJOR;

	//title stuff
	final int TITLE_OFFSET;

	final int TITLE_OFFSET_TOP;

	final int TITLE_OFFSET_BOTTOM;

	final int TITLE_OFFSET_LEFT;

	final int TITLE_OFFSET_DATE;

	//tickmarks
	final int LABEL_OFFSET;

	final int LABEL_OFFSET_TOP;

	final int LABEL_OFFSET_BOTTOM;

	final int LABEL_OFFSET_LEFT;

	//axis labels
	final int AXIS_LABEL_OFFSET_TOP;

	final int AXIS_LABEL_OFFSET_BOTTOM;

	final int AXIS_LABEL_OFFSET_LEFT;

	final int AXIS_LABEL_OFFSET_RIGHT;

	//stuff for channel marker
	final int MARK_MIN_LENGTH;

	final int MARK_OFFSET;

	//fonts
	final float SCREEN_FONT_SIZE;

	final float TITLE_SCREEN_SIZE;

	//stuff for two d color scale
	final int COLOR_SCALE_OFFSET; // distance from right side of plot

	final int COLOR_SCALE_LABEL_OFFSET;

	final int COLOR_SCALE_SIZE; // size of a color swatch

	final int PRINT_FONT_SIZE;

	final int TILE_PRINT_SIZE;

	final String FONT_CLASS = "Serif";

	private PlotGraphicsLayout(int type) {
		if (type == LAYOUT_TYPE_LABELS) {
			//border outside of plot
			BORDER_TOP = 40;
			BORDER_LEFT = 60;
			BORDER_BOTTOM = 40;
			BORDER_RIGHT = 60;

			//tickmark stuff
			TICK_SIZE = 10;
			TICK_MINOR = 5;
			TICK_MAJOR = 10;

			//title stuff
			TITLE_OFFSET = 10;
			TITLE_OFFSET_TOP = 10;
			TITLE_OFFSET_BOTTOM = 25;
			TITLE_OFFSET_LEFT = 0;
			TITLE_OFFSET_DATE = 25;

			//tickmarks
			LABEL_OFFSET = 5;
			LABEL_OFFSET_TOP = 5;
			LABEL_OFFSET_BOTTOM = 3;
			LABEL_OFFSET_LEFT = 3;

			//axis labels
			AXIS_LABEL_OFFSET_TOP = 20;
			AXIS_LABEL_OFFSET_BOTTOM = 20;
			AXIS_LABEL_OFFSET_LEFT = 35;
			AXIS_LABEL_OFFSET_RIGHT = 20;

			//stuff for channel marker
			MARK_MIN_LENGTH = 20;
			MARK_OFFSET = 3;
			//fonts
			SCREEN_FONT_SIZE = 12;
			TITLE_SCREEN_SIZE = SCREEN_FONT_SIZE + 2;

			//stuff for two d color scale
			COLOR_SCALE_OFFSET = 10; // distance from right side of plot
			COLOR_SCALE_LABEL_OFFSET = 5;
			COLOR_SCALE_SIZE = 15; // size of a color swatch

			PRINT_FONT_SIZE = 12;
			TILE_PRINT_SIZE = PRINT_FONT_SIZE + 2;
		} else {

			//border outside of plot
			BORDER_TOP = 0;
			BORDER_LEFT = 0;
			BORDER_BOTTOM = 0;
			BORDER_RIGHT = 0;

			//tickmark stuff
			TICK_SIZE = 10;
			TICK_MINOR = 5;
			TICK_MAJOR = 10;

			//title stuff
			TITLE_OFFSET = 10;
			TITLE_OFFSET_TOP = -13;
			TITLE_OFFSET_BOTTOM = 25;
			TITLE_OFFSET_LEFT = 20;
			TITLE_OFFSET_DATE = 25;

			//tickmarks
			LABEL_OFFSET = 5;
			LABEL_OFFSET_TOP = 5;
			LABEL_OFFSET_BOTTOM = 3;
			LABEL_OFFSET_LEFT = 3;

			//axis labels
			AXIS_LABEL_OFFSET_TOP = 20;
			AXIS_LABEL_OFFSET_BOTTOM = 20;
			AXIS_LABEL_OFFSET_LEFT = 35;
			AXIS_LABEL_OFFSET_RIGHT = 20;

			//stuff for channel marker
			MARK_MIN_LENGTH = 20;
			MARK_OFFSET = 3;
			//fonts
			SCREEN_FONT_SIZE = 12;
			TITLE_SCREEN_SIZE = SCREEN_FONT_SIZE + 2;

			//stuff for two d color scale
			COLOR_SCALE_OFFSET = 10; // distance from right side of plot
			COLOR_SCALE_LABEL_OFFSET = 5;
			COLOR_SCALE_SIZE = 15; // size of a color swatch

			PRINT_FONT_SIZE = 12;
			TILE_PRINT_SIZE = PRINT_FONT_SIZE + 2;
		}
	}
	
	static PlotGraphicsLayout getLayout(int type){
		return type==LAYOUT_TYPE_LABELS ? LABELS : NO_LABELS;
	}

	public static final PlotGraphicsLayout LABELS = new PlotGraphicsLayout(
			LAYOUT_TYPE_LABELS);

	public static final PlotGraphicsLayout NO_LABELS = new PlotGraphicsLayout(
			LAYOUT_TYPE_NO_LABELS);
}