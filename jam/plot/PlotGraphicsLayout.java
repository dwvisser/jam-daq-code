package jam.plot;

/**
 * Layout constants for plotgraphics, has default size for some things.
 * 
 * @version 0.5
 * @author Ken Swartz
 */
class PlotGraphicsLayout {

	//LayoutType full plot
	static final int LAYOUT_TYPE_LABELS = 0;
	//LayoutType tiled plots
	static final int LAYOUT_TYPE_NO_LABELS = 1;

	int LAYOUT_TYPE;
	
	//border outside of plot
	int BORDER_TOP;
	int BORDER_LEFT;
	int BORDER_BOTTOM;
	int BORDER_RIGHT;

	//tickmark stuff
	int TICK_SIZE;
	int TICK_MINOR;
	int TICK_MAJOR;

	//title stuff
	int TITLE_OFFSET;
	int TITLE_OFFSET_TOP;
	int TITLE_OFFSET_BOTTOM;
	int TITLE_OFFSET_LEFT;
	int TITLE_OFFSET_DATE;

	//tickmarks
	int LABEL_OFFSET;
	int LABEL_OFFSET_TOP;
	int LABEL_OFFSET_BOTTOM;
	int LABEL_OFFSET_LEFT;

	//axis labels
	int AXIS_LABEL_OFFSET_TOP;
	int AXIS_LABEL_OFFSET_BOTTOM;
	int AXIS_LABEL_OFFSET_LEFT;
	int AXIS_LABEL_OFFSET_RIGHT;

	//stuff for channel marker
	int MARK_MIN_LENGTH;
	int MARK_OFFSET;

	//fonts
	float SCREEN_FONT_SIZE;
	float TITLE_SCREEN_SIZE;

	//stuff for two d color scale
	int COLOR_SCALE_OFFSET; // distance from right side of plot
	int COLOR_SCALE_LABEL_OFFSET;
	int COLOR_SCALE_SIZE; // size of a color swatch

	int PRINT_FONT_SIZE;
	int TILE_PRINT_SIZE;
	
	String FONT_CLASS="Serif";

	void setLayoutType(int type) {
		if (type == LAYOUT_TYPE_LABELS) {
			setLayoutTypeLabels();
			LAYOUT_TYPE=LAYOUT_TYPE_LABELS;
		} else if (type == LAYOUT_TYPE_NO_LABELS) {
			setLayoutTypeNoLabels();
			LAYOUT_TYPE=LAYOUT_TYPE_NO_LABELS;
		}
	}
	/**
	 * Full plot with margins 
	 *
	 */
	private void setLayoutTypeLabels() {
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
		TITLE_OFFSET_LEFT=0;
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
	/**
	 * Tiled plot with no margins  
	 *
	 */
	private void setLayoutTypeNoLabels() {

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
		TITLE_OFFSET_LEFT=20;
		TITLE_OFFSET_DATE = 25;

		//tickmarks
		int LABEL_OFFSET = 5;
		int LABEL_OFFSET_TOP = 5;
		int LABEL_OFFSET_BOTTOM = 3;
		int LABEL_OFFSET_LEFT = 3;

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