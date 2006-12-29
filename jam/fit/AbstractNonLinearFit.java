/*
 *
 */
package jam.fit;

import java.util.ArrayList;

/**
 * This abstract class uses LevenbergMarquadt to do non-linear parametric
 * function fitting. An actual class should define additional parameters and add
 * them to <code>parameters</code>, It should implement
 * <code>estimate()</code>,<code>valueAt()</code>, and
 * <code>derivative()<code>.
 *
 * @author  Dale Visser
 * @version 0.5, 8/28/98
 *
 * @see      #valueAt
 * @see      #derivative
 * @see      AbstractFit
 * @see      LevenbergMarquadt
 * @see      GaussianFit
 * @see      AbstractFit#estimate
 */
public abstract class AbstractNonLinearFit extends AbstractFit {

	/**
	 * does the actual matrix algebra to find the best fit
	 */
	protected transient LevenbergMarquadt fitter;

	/**
	 * the low channel limit for the fit
	 */
	protected transient Parameter lowChannel;

	/**
	 * the high channel limit for the fit
	 */
	protected transient Parameter highChannel;

	/**
	 * the calculated reduced chi-squared statistic
	 */
	protected transient Parameter chisq;

	/**
	 * the <code>int</code> value of <code>lo</code>
	 */
	protected transient int minCH;

	/**
	 * the <code>int</code> value of <code>hi</code>
	 */
	protected transient int maxCH;

	/**
	 * the name of <code>lo</code>
	 */
	public static final String FIT_LOW = "Fit Low";

	/**
	 * the name of <code>hi</code>
	 */
	public static final String FIT_HIGH = "Fit High";

	/**
	 * Class constructor. This is still an abstract class. Specific subclass
	 * constructors will call this before executing their own constructors.
	 * 
	 * @param name
	 *            of the fit function
	 */
	public AbstractNonLinearFit(String name) {
		super(name);
		parameters = new ArrayList<Parameter>();
		chisq = new Parameter("ChiSq/dof", Parameter.DOUBLE, Parameter.KNOWN,
				Parameter.OUTPUT);
		addParameter(chisq);
		lowChannel = new Parameter(FIT_LOW, Parameter.INT, Parameter.KNOWN,
				Parameter.MOUSE);
		addParameter(lowChannel);
		highChannel = new Parameter(FIT_HIGH, Parameter.INT, Parameter.KNOWN,
				Parameter.MOUSE);
		addParameter(highChannel);
	}

	/**
	 * Evaluates at x for given parameters.
	 * 
	 * @param xValue
	 *            value at which to evaluate the fit function
	 * @return value of fit function at <code>x</code>
	 */
	public abstract double valueAt(double xValue);

	/**
	 * Evaluates derivative with respect to <code>parameterName</code> at
	 * <code>x</code>.
	 * 
	 * @param parameterName
	 *            the name of the parameter to differentiate with respect to
	 * @param xValue
	 *            value to evalueate at
	 * @return df(<code>x</code>)/d(<code>parameterName</code>) at x
	 */
	public abstract double derivative(double xValue, String parameterName);

	/**
	 * Perform fit calulation and return a status <code>String</code>. Calls
	 * <code>LevenbergMarquadt</code> several times, which determines changes
	 * in parameter values likely to reduce chi-squared. When reductions are no
	 * longer significant, it stops. If there is no covergence, it stops after
	 * 10 iterations.
	 * 
	 * @exception FitException
	 *                thrown if unrecoverable error occurs during fit
	 * @return message with number of iterations and degrees of freedom to fit
	 */
	public String doFit() throws FitException {
		double chiSq, newChiSq;
		int numIter = 1;
		final int MAX_ITERATIONS = 10;
		final int MAX_SMALL = 2; // total non- or marginal improvements
		// before giving up
		boolean close;
		boolean quit;
		int smallCounter = 0;
		String returnVal = "Note: errors not independent.";
		fitter = new LevenbergMarquadt(this);
		// function.setParameters(parameters);
		minCH = getParameter(FIT_LOW).getIntValue();
		lowerLimit = minCH;
		maxCH = getParameter(FIT_HIGH).getIntValue();
		upperLimit = maxCH;
		fitter.setup(counts, errors, minCH, maxCH);

		try {
			fitter.iterate(LevenbergMarquadt.Iteration.FIRST_ITERATION);
		} catch (Exception e) {
			returnVal = e.toString();
		}
		chiSq = fitter.getChiSq();
		do {
			try {
				fitter.iterate(LevenbergMarquadt.Iteration.NEXT_ITERATION);
			} catch (Exception e) {
				returnVal = e.toString();
			}
			newChiSq = fitter.getChiSq();
			numIter++;
			close = (Math.abs(newChiSq - chiSq) < 0.01);// didn't improve or
			// improved marginally
			if (close) {
				smallCounter++;
			}
			quit = ((smallCounter >= MAX_SMALL) || (numIter >= MAX_ITERATIONS));
			chiSq = newChiSq;
		} while (!quit);
		// do last iteration
		try {
			fitter.iterate(LevenbergMarquadt.Iteration.LAST_ITERATION);
			returnVal = (numIter + " iterations, d.o.f. = " + fitter
					.getDegreesOfFreedom());
			textInfo.messageOutln(numIter + " iterations, d.o.f. = "
					+ fitter.getDegreesOfFreedom());
		} catch (Exception e) {
			returnVal = e.toString();
		}
		getParameter("ChiSq/dof").setValue(newChiSq);
		return returnVal;
	}

	/**
	 * Returns <code>double</code> value of parameter indicated by name.
	 * 
	 * @param which
	 *            the name of the parameter
	 * @return the current value of the parameter
	 */
	public double getValue(final String which) {
		return getParameter(which).getDoubleValue();
	}

	/**
	 * Set a parameter designated by name to a new value.
	 * 
	 * @param which
	 *            the name of the parameter
	 * @param value
	 *            the value to assign
	 */
	public void setParameter(final String which, final double value) {
		getParameter(which).setValue(value);
	}

	/**
	 * Calculate function value for specific channel in the histogram.
	 * 
	 * @param channel
	 *            the channel to evaluate the function at
	 * @return the value of the function at <code>channel</code>
	 */
	public double calculate(final int channel) {
		double rval = 0.0;
		if (channel >= minCH && channel <= maxCH) {
			rval = valueAt(channel);
		}
		return rval;
	}
}
