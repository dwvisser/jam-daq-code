/*
 * Created on Nov 26, 2004
 */
package jam.data;


/**
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 */
public abstract class AbstractHist2D extends Histogram {

	AbstractHist2D(String nameIn, Type type, int sizeX, int sizeY, String title) {
		super(nameIn, type, sizeX, sizeY, title);
	}

	AbstractHist2D(String name, Type type, int sizeX, int sizeY, String title,
			String axisLabelX, String axisLabelY) {
		super(name, type, sizeX, sizeY, title, axisLabelX, axisLabelY);
	}

	public abstract double getCounts(int chX, int chY);

	public abstract void setCounts(int chX, int chY, double counts);
}