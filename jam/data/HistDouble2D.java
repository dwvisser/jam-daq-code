/*
 * Created on Nov 29, 2004
 */
package jam.data;

import java.util.Arrays;


/**
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
final class HistDouble2D extends AbstractHist2D {
	
	/* array to hold counds for 2d double */
	private transient double[][] counts2dD;

	/**
	 * Create a new 2-d histogram with counts known and automatically give it a
	 * number
	 * 
	 * @param name
	 *            unique name of histogram, should be limited to
	 *            <code>NAME_LENGTH</code> characters, used in both .jhf and
	 *            .hdf files as the unique identifier for reloading the
	 *            histogram
	 * @param title
	 *            lengthier title of histogram, displayed on plot
	 * @param countsIn
	 *            array of counts to initialize with, must be square
	 */
	HistDouble2D(String name, String title, double[][] countsIn) {
		super(name, Type.TWO_D_DOUBLE, countsIn.length, countsIn[0].length,
				title);
		initCounts(countsIn);
	}
	
	HistDouble2D(String name, String title, String axisLabelX, String axisLabelY,
			double[][] countsIn) {
		super(name, Type.TWO_D_DOUBLE, countsIn.length, countsIn[0].length,
				title, axisLabelX, axisLabelY);
		initCounts(countsIn);
	}

	private void initCounts(double [][] countsIn){
		counts2dD=new double[getSizeX()][getSizeY()];
		for (int i = 0; i < countsIn.length; i++) { //copy arrays
			System.arraycopy(countsIn[i], 0, counts2dD[i], 0,
					countsIn[0].length);
		}
	}

	/* (non-Javadoc)
	 * @see jam.data.AbstractHist2D#getCounts(int, int)
	 */
	public double getCounts(int chX, int chY) {
		return counts2dD[chX][chY];
	}

	/* (non-Javadoc)
	 * @see jam.data.AbstractHist2D#setCounts(int, int, double)
	 */
	public void setCounts(int chX, int chY, double counts) {
		counts2dD[chX][chY]=counts;
	}

	/* (non-Javadoc)
	 * @see jam.data.Histogram#clearCounts()
	 */
	void clearCounts() {
		counts2dD=null;
	}

	/* (non-Javadoc)
	 * @see jam.data.Histogram#getCounts()
	 */
	public Object getCounts() {
		return counts2dD;
	}

	/* (non-Javadoc)
	 * @see jam.data.Histogram#setZero()
	 */
	public void setZero() {
		final int size=getSizeX();
		for (int i = 0; i < size; i++) {
			Arrays.fill(counts2dD[i],0);
		}
	}

	/* (non-Javadoc)
	 * @see jam.data.Histogram#setCounts(java.lang.Object)
	 */
	public void setCounts(Object countsIn) {
		if (Type.getArrayType(countsIn) != getType()) {
			throw new IllegalArgumentException("Expected array for type "
					+ getType());
		}
		setCounts((double[][]) countsIn);
	}

	/* (non-Javadoc)
	 * @see jam.data.Histogram#addCounts(java.lang.Object)
	 */
	public void addCounts(Object countsIn) {
		if (Type.getArrayType(countsIn) != getType()) {
			throw new IllegalArgumentException("Expected array for type "
					+ getType());
		}
		addCounts((double[][]) countsIn);
	}

	/* (non-Javadoc)
	 * @see jam.data.Histogram#getArea()
	 */
	public double getArea() {
		final int size=getSizeX();
		double sum=0.0;
		for (int i = 0; i < size; i++) {
			final int sizeY=getSizeY();
			for (int j = 0; j < sizeY; j++) {
				sum += counts2dD[i][j];
			}
		}
		return sum;
	}
	
	private synchronized void setCounts(double[][] countsIn) {
		final int loopLen = Math.min(countsIn.length, counts2dD.length);
		for (int i = 0; i < loopLen; i++) {
			System.arraycopy(countsIn[i], 0, counts2dD[i], 0, Math.min(
					countsIn[i].length, counts2dD[i].length));
		}
	}


}
