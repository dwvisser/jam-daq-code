package jam.fit;

import jam.global.MessageHandler;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This uses the Levenberg-Marquadt prescription for finding the local minimum
 * of chi-squared given seed parameter values for the function to fit. The
 * <code>NonLinearFit</code> class which creates and calls this must supply
 * the function evaluation and differentiation.
 * 
 * @author Dale Visser
 * @version 0.5, 8/28/98
 * 
 * @see AbstractNonLinearFit
 * @see GaussianFit
 */
final class LevenbergMarquadt {

	/**
	 * <code>int</code> used when calling to calculate the first iteration
	 */
	public static final int FIRST_ITERATION = 0;

	/**
	 * <code>int</code> used when calling to calculate subsequent iterations
	 */
	public static final int NEXT_ITERATION = 1;

	/**
	 * <code>int</code> used when calling to calculate the last iteration
	 */
	public static final int LAST_ITERATION = 2;

	/**
	 * counts how many iterations have bee called
	 */
	private transient int iterationCount = 0;

	/**
	 * reference to NonLinearFit object
	 */
	private transient final AbstractNonLinearFit nonLinFit;

	/**
	 * proportional to second derivative of chi-squared with respect to
	 * parameters
	 */
	private transient Matrix space;

	/**
	 * proportional to first derivative of chi-squared with respect to
	 * parameters
	 */
	private transient Matrix vec;

	/**
	 * smooth variation parameter between using exact solution, or a small step
	 * in that direction
	 */
	private transient double lambda = -1.0;// NOPMD

	/**
	 * alpha*da=beta is the equation to be solved. alpha and space are similar
	 */
	private transient Matrix alpha;// NOPMD

	/**
	 * beta and vec are similar
	 */
	private transient Matrix beta;// NOPMD

	/**
	 * second holder of alpha, expanded at end
	 */
	private transient Matrix covar;

	/**
	 * contains trial changes to parameter values
	 */
	private transient Matrix deltaA;// NOPMD

	/**
	 * array containing all parameters
	 */
	private transient final Parameter[] parameters;

	/**
	 * contains trial values for calculation
	 */
	private transient Parameter[] tryParameters;// NOPMD

	/**
	 * the low channel limit for fitting
	 */
	private transient int minChannel;

	/**
	 * the high channel limit for fitting
	 */
	private transient int maxChannel;

	/**
	 * number of function parameters
	 */
	private transient final int nPar;

	/**
	 * number of variable function parameters
	 */
	private transient int nVar;

	/**
	 * degrees of freedom numChannels-number variable params
	 */
	private transient int dof;

	/**
	 * full contents of 1-d histogram to fit
	 */
	private transient double[] data;

	/**
	 * Array of channel count estimated errors.
	 */

	private transient double[] errors;

	/**
	 * latest chi-squared value
	 */
	private transient double chiSq;

	/**
	 * previous chi-squared value
	 */
	private transient double oChiSq;// NOPMD

	private transient final MessageHandler messages;

	/**
	 * Class constructor giving handle to parent.
	 * 
	 * @param nlf
	 *            the parent <code>NonLinearFit</code> object creating this
	 */
	public LevenbergMarquadt(AbstractNonLinearFit nlf) {
		super();
		nonLinFit = nlf;
		messages = nlf.getTextInfo();
		List<Parameter> temp2 = new ArrayList<Parameter>();
		for (Parameter param : nonLinFit.getParameters()) {
			if (param.isDouble()) {
				boolean variableParameter = !(param.isOutputOnly() || param
						.isKnown());
				if (variableParameter) {
					temp2.add(param);
				}
			}
		}
		parameters = temp2.toArray(new Parameter[temp2.size()]);
		nPar = parameters.length;
	}

	/**
	 * Sets the histogram values and the boundaries for the fit.
	 * 
	 * @param counts
	 *            the counts in the histogram to fit
	 * @param errors
	 *            error bars on the histogram bin counts
	 * @param minChannel
	 *            the lower limit of the fit
	 * @param maxChannel
	 *            the upper limit of the fit
	 */
	void setup(final double[] counts, final double[] errors,
			final int minChannel, final int maxChannel) {
		this.data = new double[counts.length];
		System.arraycopy(counts, 0, data, 0, counts.length);
		this.errors = new double[errors.length];
		System.arraycopy(errors, 0, this.errors, 0, errors.length);
		this.minChannel = minChannel;
		this.maxChannel = maxChannel;
	}

	/**
	 * Calculates a single iteration of the minimization routine.
	 * 
	 * @param iteration
	 *            indicates first, middle, or final iteration
	 * @exception Exception
	 *                trying to diagonalize singular matrices
	 * @see #FIRST_ITERATION
	 * @see #NEXT_ITERATION
	 * @see #LAST_ITERATION
	 * @see AbstractNonLinearFit#doFit
	 */
	public void iterate(int iteration) throws Exception {// NOPMD
		int index;
		Matrix oneda = new Matrix(nPar, 1);
		boolean firstCall;
		boolean lastCall;
		boolean allDone = false;

		firstCall = (iteration == FIRST_ITERATION);
		lastCall = (iteration == LAST_ITERATION);

		// first iteration, initialize
		if (firstCall) {
			deltaA = new Matrix(nPar, 1, 0.0);
			beta = new Matrix(nPar, 1, 0.0);
			nVar = 0;
			for (index = 0; index < nPar; index++) {
				if (!parameters[index].isFixed()) {
					// increment number of variable function parameters
					nVar++;
				}
			}
			dof = maxChannel - minChannel + 1 - nVar;
			// set working space variables (used in calculate())
			vec = new Matrix(nVar, 1, 0.0);
			space = new Matrix(nVar, nVar, 0.0);

			alpha = new Matrix(nVar, nVar, 0.0);
			oneda = new Matrix(nVar, 1, 0.0);
			// mfit x 1 matrix filled w/ zeroes
			lambda = 0.001;
			// call calculate w/ parameters, retrieve results into
			// alpha,beta,chiSq is updated
			calculate(parameters);
			alpha = new Matrix(space);
			beta = new Matrix(vec);

			oChiSq = chiSq;
			tryParameters = new Parameter[parameters.length];
			System
					.arraycopy(parameters, 0, tryParameters, 0,
							parameters.length);
		}

		// Entering main iteration, must have alpha and beta from last
		// calculation,and oChiSq

		// Alter linearized fitting matrix, by augmenting diagonal elements
		covar = new Matrix(alpha); // make a copy of alpha into covar
		oneda = new Matrix(beta); // make a copy of beta into oneda
		if (lastCall) {
			lambda = 0.0;
		}
		for (index = 0; index < nVar; index++) {
			covar.element[index][index] = covar.element[index][index]
					* (1.0 + lambda);
			// aubment diagonal elements
		}

		// Matrix solution using covar and oneda
		final GaussJordanElimination gje = new GaussJordanElimination(covar, oneda);
		gje.doIt();
		covar = gje.getMatrix();
		oneda = gje.getVectors();

		// copy oneda into da, which will be used to alter the parameters for
		// another try
		deltaA = new Matrix(oneda);
		// expand if last call, otherwise output iterationCount
		if (lastCall) { // final call (converged presumably) evaluate covariance
						// matrix
			expandCovarianceMatrix();
			setParameterErrors();
			allDone = true;
		} else {
			iterationCount++;
		}

		// do only if not on final call
		if (!allDone) {

			// alter those parameters that are not fixed by adding da
			// before execution, tryParameters=same as last call
			// after execution, tryParameters changed to new
			index = 0;
			for (int l = 0; l < nPar; l++) {
				if (!tryParameters[l].isFixed()) {
					tryParameters[l].setValue(tryParameters[l].getDoubleValue()
							+ deltaA.element[index][0]);
					index++;
				}
			}

			// find alpha, beta matrices, put in covar and da, chiSq is
			// calculated
			calculate(tryParameters);
			covar = new Matrix(space);
			deltaA = new Matrix(vec);
			// Did the trial succeed?
			if (chiSq < oChiSq) { // Success, adopt the new solution.
				lambda = lambda * 0.1;
				oChiSq = chiSq;
				alpha = new Matrix(covar);
				beta = new Matrix(deltaA);
				// tryParameters left the same for next iteration
			} else { // Failure, increase lambda
				lambda = lambda * 10;
				chiSq = oChiSq;
				System.arraycopy(parameters, 0, tryParameters, 0, nPar);
				// alpha and beta left the same
				// old parameters copied into tryParameters for next iteration
			}
		}
	}

	/**
	 * Calculates space, vec, and chiSq from passed parameters. To do this,
	 * assigns parameter values in nonLinFit
	 * 
	 * @param params
	 *            array of parameters to calculate from
	 */
	private void calculate(final Parameter[] params) {
		double sig2i; // 1 over sigma squared
		double weight;
		double yFit, deltaY;
		double[] dyda = new double[nPar];
		setParameters(params);
		outputParameters(params);
		/*
		 * initialize space and vec (matrix and vector in Numerical Recipes in C
		 * 15.5)
		 */
		for (int j = 0; j < nVar; j++) {
			Arrays.fill(space.element[j], 0.0);
			vec.element[j][0] = 0.0;
		}
		/* initialize chiSq */
		chiSq = 0.0;
		/* Summation loop over all data channels */
		for (int i = minChannel; i <= maxChannel; i++) {
			yFit = nonLinFit.valueAt(i);
			for (int j = 0; j < nPar; j++) {
				dyda[j] = nonLinFit.derivative(i, params[j].getName());
			}
			sig2i = 1.0 / (errors[i] * errors[i]);
			deltaY = data[i] - yFit;
			/* and find chi squared */
			chiSq = chiSq + deltaY * deltaY * sig2i;
			/* setup workspace for MRQMIN magic */
			int row = -1;
			for (int l = 0; l < nPar; l++) {
				if (!params[l].isFixed()) {
					weight = dyda[l] * sig2i;
					row++;
					int column = -1;
					for (int m = 0; m <= l; m++) {
						if (!params[m].isFixed()) {
							column++;
							space.element[row][column] = space.element[row][column] + weight
									* dyda[m];
						}
					}
					vec.element[row][0] = vec.element[row][0] + deltaY * weight;
				}
			}
		}
		outputParamsPostFit(params);
	}

	/**
	 * @param params
	 */
	private void outputParamsPostFit(final Parameter[] params) {
		/* debug message */
		messages.messageOut(iterationCount + " ", MessageHandler.NEW);
		messages.messageOut(round(chiSq / dof, 3) + " ");
		for (int i = 0; i < params.length; i++) {
			messages.messageOut(round(nonLinFit.getParameter(
					params[i].getName()).getDoubleValue(), 3)
					+ " ");
		}
		messages.messageOut("", MessageHandler.END);
		/* fill in the symmetric side */
		for (int j = 1; j < nVar; j++) {
			for (int k = 0; k < j; k++) {
				space.element[k][j] = space.element[j][k];
			}
		}
	}

	/**
	 * @param params
	 */
	private void outputParameters(final Parameter[] params) {
		if (iterationCount == 0) {
			messages.messageOut("Iteration ChiSq/dof ", MessageHandler.NEW);
			for (int i = 0; i < params.length; i++) {
				messages.messageOut(params[i].getName() + " ");
			}
			messages.messageOut("", MessageHandler.END);
		}
	}

	/**
	 * @param params
	 */
	private void setParameters(final Parameter[] params) {
		/* set parameter values in funk */
		for (int i = 0; i < params.length; i++) {
			nonLinFit.setParameter(params[i].getName(), params[i]
					.getDoubleValue());
		}
	}

	/**
	 * Expands the covariance matrix to include the fixed parameters.
	 */
	private void expandCovarianceMatrix() {
		Matrix temp;
		double temp2;

		temp = new Matrix(nPar, nPar, 0.0);
		for (int i = 0; i < nVar; i++) {
			for (int j = 0; j < nVar; j++) {
				temp.element[i][j] = covar.element[i][j];
			}
		}
		for (int i = nVar; i < nPar; i++) {
			for (int j = 0; j <= i; j++) {
				temp.element[i][j] = 0.0;
				temp.element[j][i] = 0.0;
			}
		}
		int refIndex = nVar - 1;
		for (int j = nPar - 1; j >= 0; j--) {
			if (!parameters[j].isFixed()) {
				for (int i = 0; i < nPar; i++) {
					temp2 = temp.element[i][refIndex];
					temp.element[i][refIndex] = temp.element[i][j];
					temp.element[i][j] = temp2;
				}
				for (int i = 0; i < nPar; i++) {
					temp2 = temp.element[refIndex][i];
					temp.element[refIndex][i] = temp.element[j][i];
					temp.element[j][i] = temp2;
				}
				refIndex--;
			}
		}
		covar = new Matrix(temp);
		messages.messageOutln("\nCovariance Matrix: \n" + covar.toStringUL(3));
	}

	/**
	 * Gives the chi-sqared value of the latest iteration.
	 * 
	 * @return <code>chiSq/dof</code>
	 */
	public double getChiSq() {
		return chiSq / dof;
	}

	/**
	 * Assigns the error values in the parameters, based on the expanded
	 * covariance matrix.
	 */
	private void setParameterErrors() {
		for (int i = 0; i < nPar; i++) {
			parameters[i].setError(Math.sqrt(covar.element[i][i]));
		}
	}

	/**
	 * Rounds a <code>String</code> representation of a <code>double</code>
	 * value.
	 * 
	 * @param value
	 *            value to round
	 * @param fraction
	 *            number of digits to show in fractional part
	 * @return a <code>String</code> with the specified appearance of the
	 *         number
	 */
	private String round(final double value, final int fraction) {
		String out;
		final NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMinimumFractionDigits(fraction);
		formatter.setMaximumFractionDigits(fraction);
		out = formatter.format(value);
		return out;
	}

	int getDegreesOfFreedom() {
		return dof;
	}

}