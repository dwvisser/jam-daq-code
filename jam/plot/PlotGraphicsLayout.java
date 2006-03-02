package jam.plot;

/**
 * Layout constants for plotgraphics, has default size for some things.
 * 
 * @version 0.5
 * @author Ken Swartz
 */
final class PlotGraphicsLayout {

	enum Type {
		/**
		 * plot with labels
		 */
		WITH_LABELS,

		/**
		 * plot w/o labels
		 */
		WO_LABELS
	};

	class Border {
		final transient int top;

		final transient int left;

		final transient int bottom;

		final transient int right;

		Border(int top, int left, int bottom, int right) {
			super();
			this.top = top;
			this.left = left;
			this.bottom = bottom;
			this.right = right;
		}
	}

	// border outside of plot
	final transient Border border;

	// tickmark stuff
	class Tick {
		final transient int size;

		final transient int minor;

		final transient int major;

		Tick(int size, int minor, int major) {
			super();
			this.size = size;
			this.minor = minor;
			this.major = major;
		}
	}
	
	final Tick tick = new Tick(10,5,10);

	// title stuff
	class TitleOffsets {

		final transient int main;

		final transient int top;

		final transient int bottom;

		final transient int left;

		final transient int date;

		TitleOffsets(int main, int top, int bottom, int left, int date) {
			super();
			this.main = main;
			this.top = top;
			this.bottom = bottom;
			this.left = left;
			this.date = date;
		}

	}

	final transient TitleOffsets titleOffsets;

	// tickmarks
	class LabelOffsets {
		final transient int main;

		final transient int top;

		final transient int botttom;

		final transient int left;

		LabelOffsets(int main, int top, int bottom, int left) {
			super();
			this.main = main;
			this.top = top;
			this.botttom = bottom;
			this.left = left;
		}
	}

	final LabelOffsets labelOffsets = new LabelOffsets(5, 5, 3, 3);

	// axis labels
	class AxisLabelOffsets {
		final transient int top;

		final transient int bottom;

		final transient int left;

		final transient int right;

		AxisLabelOffsets(int top, int bottom, int left, int right) {
			super();
			this.top = top;
			this.bottom = bottom;
			this.left = left;
			this.right = right;
		}
	}

	final AxisLabelOffsets axisLabelOffsets = new AxisLabelOffsets(20, 20, 35,
			20);

	// stuff for channel marker
	final static transient int MARK_MIN_LENGTH=20;

	final static transient int MARK_OFFSET=3;

	// fonts
	final static transient float SCREEN_FONT_SIZE=12;

	final static float TITLE_SCREEN_SIZE=SCREEN_FONT_SIZE + 2;

	class ColorScale {
		// stuff for two d color scale
		final transient int offset; // distance from right side of plot

		final transient int labelOffset;

		final transient int size; // size of a color swatch

		ColorScale(int offset, int labelOffset, int size) {
			super();
			this.offset = offset;
			this.labelOffset = labelOffset;
			this.size = size;
		}
	}

	final ColorScale colorScale = new ColorScale(
			10, 5, 15);

	final static transient int PRINT_FONT_SIZE=12;

	final transient int TILE_PRINT_SIZE=PRINT_FONT_SIZE + 2;

	final static String FONT_CLASS = "Serif";

	private PlotGraphicsLayout(Type type) {
		super();
		if (type == Type.WITH_LABELS) {
			// border outside of plot
			border = new Border(40, 60, 40, 60);
			// title stuff
			titleOffsets = new TitleOffsets(10, 10, 25, 0, 25);
		} else {
			// border outside of plot
			border = new Border(0, 0, 0, 0);
			// title stuff
			titleOffsets = new TitleOffsets(10, -13, 35, 20, 25);
		}
	}

	static PlotGraphicsLayout getLayout(final Type type) {
		return type == Type.WITH_LABELS ? LABELS : NO_LABELS;
	}

	/**
	 * Layout with axis labels.
	 */
	static final PlotGraphicsLayout LABELS = new PlotGraphicsLayout(
			Type.WITH_LABELS);

	/**
	 * Layout without axis labels.
	 */
	private static final PlotGraphicsLayout NO_LABELS = new PlotGraphicsLayout(
			Type.WO_LABELS);
}