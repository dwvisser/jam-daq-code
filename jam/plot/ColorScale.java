package jam.plot;
import java.awt.Color;

/**
 * A <code>ColorScale</scale> maps 2d histogram channel counts
 * to colors.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @see java.awt.Color
 * @see GradientColorScale
 */
interface ColorScale {
	Color getColor(double counts);
}
