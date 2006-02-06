package jam.plot;

import java.awt.Point;

/**
 * Abstraction of a histogram channel on the display.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 2004-10-04
 */
public final class Bin implements Cloneable {
	/**
	 * For 1d hists, y-coordinate is arbitrary.
	 */
	private transient final Point channel = new Point();

	private static PlotDisplay display = null;

	static void init(final PlotDisplay disp) {
		Bin.display = disp;
	}

	/**
	 * Constructs a bin with coordinates identical to those of the given
	 * <code>Point</code>.
	 * 
	 * @param point
	 *            coordinates
	 * @return bin at the given coordinates
	 */
	public static Bin create(final Point point) {
		if (display == null) {
			throw new IllegalStateException("Bin not initialized.");
		}
		return new Bin(point);
	}

	/**
	 * Constructs a bin at the coordinate (x,y)
	 * 
	 * @param coords any missing default to 0
	 * @return bin at (x,y)
	 */
	public static Bin create(final int... coords) {
		final int xCoord = coords.length > 0 ? coords[0] : 0;
		final int yCoord = coords.length > 1 ? coords[1] : 0;
		return create(new Point(xCoord, yCoord));
	}

	private Bin(Point point) {
		super();
		setChannel(point);
	}

	public Object clone() {
		return create(channel);
	}

	void setChannel(final Point point) {
		synchronized (this) {
			channel.setLocation(point);
		}
	}

	void setChannel(final Bin bin) {
		synchronized (this) {
			channel.setLocation(bin.getPoint());
		}
	}

	void setChannel(final int xChan, final int yChan) {
		synchronized (this) {
			channel.setLocation(xChan, yChan);
		}
	}

	double getCounts() {
		synchronized (this) {
			return display.getPlotContainer().getCount(this);
		}
	}

	Point getPoint() {
		synchronized (this) {
			return new Point(channel);
		}
	}

	/**
	 * Gets the x-coordinate of this bin.
	 * 
	 * @return the x-channel
	 */
	public int getX() {
		synchronized (this) {
			return channel.x;
		}
	}

	/**
	 * Gets the y-coordinate of this bin.
	 * 
	 * @return the y-channel
	 */
	public int getY() {
		synchronized (this) {
			return channel.y;
		}
	}

	String getCoordString() {
		synchronized (this) {
			final StringBuffer rval = new StringBuffer().append(channel.x);
			if (display.getPlotContainer().getDimensionality() == 2) {
				rval.append(',').append(channel.y);
			}
			return rval.toString();
		}
	}

	public boolean equals(final Object object) {
		boolean rval = object instanceof Bin;
		if (rval) {
			final Bin that = (Bin) object;
			rval &= channel.equals(that.channel);
		}
		return rval;
	}

	public int hashCode() {
		return channel.hashCode();
	}

	Bin closestInsideBin() {
		synchronized (this) {
			int xChan = channel.x;
			int yChan = channel.y;
			final PlotContainer currentPlot = display.getPlotContainer();
			if (xChan < 0) {
				xChan = 0;
			} else if (xChan >= currentPlot.getSizeX()) {
				xChan = currentPlot.getSizeX() - 1;
			}
			if (yChan < 0) {
				yChan = 0;
			} else if (yChan >= currentPlot.getSizeY()) {
				yChan = currentPlot.getSizeY() - 1;
			}
			return create(xChan, yChan);
		}
	}
}
