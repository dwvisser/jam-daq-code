/*
 * Created on Nov 26, 2004
 */
package jam.data;

import jam.data.peaks.PeakFinder;

/**
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public abstract class AbstractHist1D extends Histogram {
	
	/**
	 * Array which contains the errors in the channel counts.
	 */
	protected double[] errors;

	/**
	 * Set to true if errors are set explicitly. Put in place so as not to waste
	 * disk space on saved files. IO routines should only write out error bars
	 * when this is set to true (checked by calling <code>errorsSet()</code>.
	 * Otherwise, Poisson errors should be assumed by other software.
	 * 
	 * @see #errorsSet()
	 */
	protected transient boolean errorsSet;

	AbstractHist1D(String name, Type type, int len, String title, String axisLabelX,
			String axisLabelY){
		super(name,type,len,title,axisLabelX,axisLabelY);
		unsetErrors();
	}
	
	AbstractHist1D(String name, Type type, int len, String title){
		super(name,type,len,title);
		unsetErrors();
	}
	
	protected synchronized final void unsetErrors(){
		errors=null;
		errorsSet=false;
	}
		
	public abstract double getCounts(int channel);
	public abstract void setCounts(int channel, double counts);
	public abstract double[] getErrors();
	
	/**
	 * Attempt to find gaussian peaks.
	 * 
	 * @param sensitivity
	 *            if larger, peaks need to be more significant to be found
	 * @param width
	 *            target FWHM of peak
	 * @param cal
	 *            whether we return calibrated values
	 * @return centroids, in channel units or calibrated units, depending on
	 *         <code>cal</code>
	 * @throws UnsupportedOperationException
	 *             if called on a 2d histogram
	 */
	public synchronized double[][] findPeaks(double sensitivity, double width, boolean cal) {
		double[] histArray;
		if (getType() == Type.ONE_D_DOUBLE) {
			histArray = (double [])getCounts();
		} else if (getType() == Type.ONE_DIM_INT) { //INT type
			int[] temp = (int [])getCounts();
			histArray = new double[temp.length];
			for (int i = 0; i < temp.length; i++) {
				histArray[i] = temp[i];
			}
		} else { //2D
			throw new UnsupportedOperationException(
					"findPeaks() called on 2D hist");
		}
		double[] posn = PeakFinder.getCentroids(histArray, sensitivity, width);
		double[][] rval = new double[3][posn.length];
		if (cal && this.isCalibrated()) {
			for (int i = 0; i < posn.length; i++) {
				rval[0][i] = posn[i];
				rval[1][i] = calibFunc.getValue(posn[i]);
				rval[2][i] = histArray[(int) Math.round(posn[i])];
			}
		} else { //no calibration
			for (int i = 0; i < posn.length; i++) {
				rval[0][i] = posn[i];
				rval[1][i] = posn[i];
				rval[2][i] = histArray[(int) Math.round(posn[i])];
			}
		}
		return rval;
	}
	
	public synchronized final void setErrors(final double [] errs){
		if (!errorsSet()){
			errors=new double[sizeX];
		}
		final int max=Math.min(sizeX,errs.length);
		System.arraycopy(errs,0,errors,0,max);
		errorsSet=true;
	}
	
	/**
	 * Returns whether errors have been explicitly set or not.
	 * 
	 * @return <code>true</code> if errors have been explicitly set,
	 *         <code>false</code> if not
	 */
	public synchronized boolean errorsSet() {
		return errorsSet;
	}
}
