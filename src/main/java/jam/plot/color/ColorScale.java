package jam.plot.color;

import java.awt.*;

/**
 * A <code>ColorScale</code> maps 2d histogram channel counts
 * to colors.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @see java.awt.Color
 * @see GradientColorScale
 */
public interface ColorScale {
	
	/**
	 * Returns the color for the bin given its counts.
	 * 
	 * @param counts the counts in the bin
	 * @return color for the bin
	 */
	Color getColor(double counts);
	
	/**
	 * Sets the counts range to use when calculating the colors.
	 * 
	 * @param min the low end
	 * @param max the high end
	 */
	void setRange(int min, int max);
}
