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

	/**
	 * Constructs a bin with coordinates identical to those of the given
	 * <code>Point</code>.
	 * 
	 * @param point
	 *            coordinates
	 * @return bin at the given coordinates
	 */
	public static Bin create(final Point point) {
		return new Bin(point);
	}

	/**
	 * Constructs a bin at the coordinate (x,y)
	 * 
	 * @param coords
	 *            any missing default to 0
	 * @return bin at (x,y)
	 */
	public static Bin create(final int... coords) {
		final int xCoord = coords.length > 0 ? coords[0] : 0;
		final int yCoord = coords.length > 1 ? coords[1] : 0;
		return create(new Point(xCoord, yCoord));
	}

	private Bin(final Point point) {
		super();
		setChannel(point);
	}

	@Override
	public Object clone() {
		return create(channel);
	}

	protected void setChannel(final Point point) {
		synchronized (this) {
			channel.setLocation(point);
		}
	}

	protected void setChannel(final Bin bin) {
		synchronized (this) {
			channel.setLocation(bin.getPoint());
		}
	}

	protected void setChannel(final int xChan, final int yChan) {
		synchronized (this) {
			channel.setLocation(xChan, yChan);
		}
	}

	protected Point getPoint() {
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

	@Override
	public boolean equals(final Object object) {
		boolean rval = object instanceof Bin;
		if (rval) {
			final Bin that = (Bin) object;
			rval &= channel.equals(that.channel);
		}
		return rval;
	}

	@Override
	public int hashCode() {
		return channel.hashCode();
	}
}
