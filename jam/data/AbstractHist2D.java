/*
 * Created on Nov 26, 2004
 */
package jam.data;


/**
 * The superclass of all 2-dimensional histograms.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 */
public abstract class AbstractHist2D extends Histogram {

    /**
     * Called by implementing subclasses.
     * 
     * @param name unique
     * @param type of counts
     * @param sizeX channels along x-axis
     * @param sizeY chnnels along y-axis
     * @param title verbose
     */
	protected AbstractHist2D(Group group, String name, Type type, int sizeX, int sizeY, String title) {
		super(group, name, type, sizeX, sizeY, title);
	}

    /**
     * Called by implementing subclasses.
     * 
     * @param name unique
     * @param type of counts
     * @param sizeX channels along x-axis
     * @param sizeY chnnels along y-axis
     * @param title verbose
     * @param axisLabelX x-axis label
     * @param axisLabelY y-axis label
     */
	protected AbstractHist2D(Group group, String name, Type type, int sizeX, int sizeY, String title,
			String axisLabelX, String axisLabelY) {
		super(group, name, type, sizeX, sizeY, title, axisLabelX, axisLabelY);
	}

	/**
	 * Returns the number of counts in the given channel.
	 * 
	 * @param chX x-coordinate of bin
	 * @param chY y-coordinate of bin
	 * @return number of counts
	 */
	public abstract double getCounts(int chX, int chY);

	/**
	 * Sets the counts in the given channel to the specified number of counts.
	 * 
	 * @param chX x-coordinate of bin
	 * @param chY y-coordinate of bin
	 * @param counts to be in the channel, rounded to <code>int</code>, if
	 * necessary
	 */
	public abstract void setCounts(int chX, int chY, double counts);
}