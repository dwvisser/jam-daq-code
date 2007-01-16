package jam.plot;

/**
 * Layout constants for plotgraphics, has default size for some things.
 * 
 * @version 0.5
 * @author Ken Swartz
 */
final class GraphicsLayout {

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
		final transient int top;// NOPMD

		final transient int left;// NOPMD

		final transient int bottom;// NOPMD

		final transient int right;// NOPMD

		Border(int top, int left, int bottom, int right) {
			super();
			this.top = top;
			this.left = left;
			this.bottom = bottom;
			this.right = right;
		}
	}

	// border outside of plot
	final transient Border border;// NOPMD

	// tickmark stuff
	class Tick {
		final transient int minor;// NOPMD

		final transient int major;// NOPMD

		Tick(int minor, int major) {
			super();
			this.minor = minor;
			this.major = major;
		}
	}

	final Tick tick = new Tick(5, 10);// NOPMD

	// title stuff
	class TitleOffsets {

		final transient int top;// NOPMD

		final transient int left;// NOPMD

		final transient int date;// NOPMD

		TitleOffsets(int top, int left, int date) {
			super();
			this.top = top;
			this.left = left;
			this.date = date;
		}

	}

	final transient TitleOffsets titleOffsets;// NOPMD

	// tickmarks
	class LabelOffsets {
		final transient int bottom;// NOPMD

		final transient int left;// NOPMD

		LabelOffsets(int bottom, int left) {
			super();
			this.bottom = bottom;
			this.left = left;
		}
	}

	final LabelOffsets labelOffsets = new LabelOffsets(3, 3);// NOPMD

	// axis labels
	class AxisLabelOffsets {
		final transient int bottom;// NOPMD

		final transient int left;// NOPMD

		AxisLabelOffsets(int bottom, int left) {
			super();
			this.bottom = bottom;
			this.left = left;
		}
	}

	final AxisLabelOffsets axisLabelOffsets = new AxisLabelOffsets(20, 35);// NOPMD

	final static transient float SCREEN_FONT_SIZE = 12;// NOPMD

	class ColorScale {
		// stuff for two d color scale

		/**
		 * distance from right side of plot
		 */
		final transient int offset; // NOPMD

		final transient int labelOffset;// NOPMD

		/**
		 * size of a color swatch
		 */
		final transient int size; // NOPMD

		ColorScale(int offset, int labelOffset, int size) {
			super();
			this.offset = offset;
			this.labelOffset = labelOffset;
			this.size = size;
		}
	}

	final ColorScale colorScale = new ColorScale(10, 5, 15);//NOPMD

	private GraphicsLayout(Type type) {
		super();
		if (type == Type.WITH_LABELS) {
			// border outside of plot
			border = new Border(40, 60, 40, 60);
			// title stuff
			titleOffsets = new TitleOffsets(10, 0, 25);
		} else {
			// border outside of plot
			border = new Border(0, 0, 0, 0);
			// title stuff
			titleOffsets = new TitleOffsets(-13, 20, 25);
		}
	}

	static GraphicsLayout getLayout(final Type type) {
		return type == Type.WITH_LABELS ? LABELS : NO_LABELS;
	}

	/**
	 * Layout with axis labels.
	 */
	static final GraphicsLayout LABELS = new GraphicsLayout(Type.WITH_LABELS);//NOPMD

	/**
	 * Layout without axis labels.
	 */
	private static final GraphicsLayout NO_LABELS = new GraphicsLayout(
			Type.WO_LABELS);
}