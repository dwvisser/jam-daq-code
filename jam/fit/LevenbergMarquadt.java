package jam.fit;
import jam.fit.NonLinearFit;
import java.util.Vector;
import java.util.Hashtable;
import java.text.NumberFormat;

/**
 * This uses the Levenberg-Marquadt prescription for finding the local minimum of chi-squared
 * given seed parameter values for the function to fit.  The <code>NonLinearFit</code> class which
 * creates and calls this must supply the function evaluation and differentiation.
 *
 * @author  Dale Visser
 * @version 0.5, 8/28/98
 *
 * @see	NonLinearFit
 * @see GaussianFit
 */
public class LevenbergMarquadt {

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
	private int iterationCount = 0;

	/**
	 * reference to NonLinearFit object
	 */
	private NonLinearFit nonLinFit;

	/**
	 * proportional to second derivative of chi-squared with respect to parameters
	 */
	Matrix space;

	/**
	 * proportional to first derivative of chi-squared with respect to parameters
	 */
	Matrix vec;

	/**
	 * smooth variation parameter between using exact solution, or a small step in
	 * that direction
	 */
	private double lambda = -1.0;

	/**
	 * alpha*da=beta is the equation to be solved.
	 * alpha and space are similar
	 */
	private Matrix alpha;

	/**
	 * beta and vec are similar
	 */
	private Matrix beta;

	/**
	 * second holder of alpha, expanded at end
	 */
	private Matrix covar;

	/**
	 * da contains trial changes to parameter values
	 */
	private Matrix da;

	/**
	 * array containing all parameters
	 */
	private Parameter[] parameters;

	/**
	 * contains trial values for calculation
	 */
	private Parameter[] tryParameters;

	/**
	 * the low channel limit for fitting
	 */
	private int minChannel;

	/**
	 * the high channel limit for fitting
	 */
	private int maxChannel;

	/**
	 * number of function parameters
	 */
	private int nPar;

	/**
	 * number of variable function parameters
	 */
	private int nVar;

	/**
	 * degrees of freedom numChannels-number variable params
	 */
	private int dof;

	/** 
	 * full contents of 1-d histogram to fit
	 */
	private double[] data;

	/**
	 * Array of channel count estimated errors.
	 */

	private double[] errors;

	/**
	 * latest chi-squared value
	 */
	private double chiSq;

	/**
	 * previous chi-squared value
	 */
	private double oChiSq;

	/**
	 * Class constructor giving handle to parent.
	 *
	 * @param	nlf the parent <code>NonLinearFit</code> object creating this
	 */
	public LevenbergMarquadt(NonLinearFit nlf) {

		int j, k;
		Vector temp;
		int type;
		boolean done;
		this.nonLinFit = nlf;

		temp = nonLinFit.getParameters();
		Parameter[] temp2 = new Parameter[temp.size()];
		j = 0;
		k = 0;
		do { //eliminate non-double parameters
			type = ((Parameter) (temp.elementAt(j))).getType();
			if (type == Parameter.DOUBLE) {
				if (!((Parameter) (temp.elementAt(j))).outputOption) {
					temp2[k] = (Parameter) (temp.elementAt(j));
					k++;
				}
			}
			j++;
			done = (j == temp.size());
		} while (!done);

		parameters = new Parameter[k];
		for (int i = 0; i < parameters.length; i++) {
			parameters[i] = temp2[i];
		}
		nPar = parameters.length;
	}

	/** 
	 * Sets the histogram values and the boundaries for the fit.
	 * 
	 * @param	counts	    the counts in the histogram to fit
	 * @param	minChannel  the lower limit of the fit
	 * @param	maxChannel  the upper limit of the fit
	 */
	public void setup(
		double[] counts,
		double[] errors,
		int minChannel,
		int maxChannel) {
		this.data = counts;
		this.errors = errors;
		this.minChannel = minChannel;
		this.maxChannel = maxChannel;
	}

	/**
	 * Calculates a single iteration of the minimization routine.
	 *
	 * @param	    iteration		indicates first, middle, or final iteration
	 * @exception   Exception		trying to diagonalize singular matrices	    
	 * @see	    #FIRST_ITERATION
	 * @see	    #NEXT_ITERATION
	 * @see	    #LAST_ITERATION
	 * @see	    NonLinearFit#doFit
	 */
	public void iterate(int iteration) throws Exception {
		int j, l;
		Matrix oneda = new Matrix(nPar, 1);
		boolean firstCall;
		boolean lastCall;
		boolean allDone = false;

		firstCall = (iteration == FIRST_ITERATION);
		lastCall = (iteration == LAST_ITERATION);

		//first iteration, initialize
		if (firstCall) {
			da = new Matrix(nPar, 1, 0.0);
			beta = new Matrix(nPar, 1, 0.0);
			nVar = 0;
			for (j = 0; j < nPar; j++) {
				if (!parameters[j].fix) {
					//increment number of variable function parameters
					nVar++;
				}
			}
			dof = maxChannel - minChannel + 1 - nVar;
			//set working space variables (used in calculate())
			vec = new Matrix(nVar, 1, 0.0);
			space = new Matrix(nVar, nVar, 0.0);

			alpha = new Matrix(nVar, nVar, 0.0);
			oneda = new Matrix(nVar, 1, 0.0);
			//mfit x 1 matrix filled w/ zeroes
			lambda = 0.001;
			//call calculate w/ parameters, retrieve results into alpha,beta,chiSq is updated
			calculate(parameters);
			alpha = new Matrix(space);
			beta = new Matrix(vec);

			oChiSq = chiSq;
			tryParameters = new Parameter[parameters.length];
			System.arraycopy(
				parameters,
				0,
				tryParameters,
				0,
				parameters.length);
		}

		//Entering main iteration, must have alpha and beta from last calculation,and oChiSq

		//Alter linearized fitting matrix, by augmenting diagonal elements
		covar = new Matrix(alpha); //make a copy of alpha into covar
		oneda = new Matrix(beta); //make a copy of beta into oneda
		if (lastCall) {
			lambda = 0.0;
		}
		for (j = 0; j < nVar; j++) {
			covar.element[j][j] = covar.element[j][j] * (1.0 + lambda);
			//aubment diagonal elements
		}

		//Matrix solution using covar and oneda
		GaussJordanElimination gje = new GaussJordanElimination(covar, oneda);
		gje.go();
		covar = gje.getMatrix();
		oneda = gje.getVectors();

		//copy oneda into da, which will be used to alter the parameters for another try
		da = new Matrix(oneda);
		//System.out.println(da.toString(3));
		//expand if last call, otherwise output iterationCount
		if (lastCall) { //final call (converged presumably) evaluate covariance matrix
			expandCovarianceMatrix();
			setParameterErrors();
			allDone = true;
		} else {
			iterationCount++;
		}

		//do only if not on final call	
		if (!allDone) {

			//alter those parameters that are not fixed by adding da 
			// before execution, tryParameters=same as last call
			// after execution, tryParameters changed to new
			j = 0;
			for (l = 0; l < nPar; l++) {
				if (!tryParameters[l].isFix()) {
					//System.out.print(tryParameters[l].getName()+": "+round(tryParameters[l].getDoubleValue(),3));
					tryParameters[l].setValue(
						tryParameters[l].getDoubleValue() + da.element[j][0]);
					j++;
					//System.out.println(" -> "+round(tryParameters[l].getDoubleValue(),3));
				}
			}

			//find alpha, beta matrices, put in covar and da, chiSq is calculated
			calculate(tryParameters);
			covar = new Matrix(space);
			da = new Matrix(vec);
			//System.out.println("chiSq: old = "+oChiSq+"\n        new = "+chiSq+"\n---");
			//Did the trial succeed?
			if (chiSq < oChiSq) { //Success, adopt the new solution.
				lambda = lambda * 0.1;
				oChiSq = chiSq;
				alpha = new Matrix(covar);
				beta = new Matrix(da);
				//tryParameters left the same for next iteration		
			} else { //Failure, increase lambda 
				lambda = lambda * 10;
				chiSq = oChiSq;
				System.arraycopy(parameters, 0, tryParameters, 0, nPar);
				//alpha and beta left the same
				//old parameters copied into tryParameters for next iteration
			}
		}
	}

	/**
	 * Calculates space, vec, and chiSq from passed parameters.
	 * To do this, assigns parameter values in nonLinFit
	 *
	 * @param	params	array of parameters to calculate from
	 */
	private void calculate(Parameter[] params) throws FitException {

		int i, j, k, l, m;
		double sig2i; //1 over sigma squared
		double weight;

		double y, dy;
		double[] dyda = new double[nPar];

		//set parameter values in funk
		for (i = 0; i < params.length; i++) {
			nonLinFit.setParameter(
				params[i].getName(),
				params[i].getDoubleValue());
		}

		if (iterationCount == 0) {
			System.out.print("Iteration ChiSq/dof ");
			for (i = 0; i < params.length; i++) {
				System.out.print(params[i].getName() + " ");
			}
			System.out.println();
		}

		//initialize space and vec (matrix and vector in Numerical Recipes in C 15.5)
		for (j = 0; j < nVar; j++) {
			for (k = 0; k <= j; k++) {
				space.element[j][k] = 0.0;
			}
			vec.element[j][0] = 0.0;
		}

		//initialize chiSq
		chiSq = 0.0;

		//Summation loop over all data channels
		for (i = minChannel; i <= maxChannel; i++) {
			y = nonLinFit.valueAt((double) i);
			for (j = 0; j < nPar; j++) {
				dyda[j] = nonLinFit.derivative((double) i, params[j].getName());
			}
			sig2i = 1.0 / (errors[i] * errors[i]);
			dy = data[i] - y;

			//and find chi squared
			chiSq = chiSq + dy * dy * sig2i;
			//setup workspace for MRQMIN magic
			j = -1;
			for (l = 0; l < nPar; l++) {
				if (!params[l].isFix()) {
					weight = dyda[l] * sig2i;
					j++;
					k = -1;
					for (m = 0; m <= l; m++) {
						if (!params[m].isFix()) {
							k++;
							space.element[j][k] =
								space.element[j][k] + weight * dyda[m];
						}
					}
					vec.element[j][0] = vec.element[j][0] + dy * weight;
				}
			}
		}
		//debug message
		System.out.print(iterationCount + " ");
		System.out.print(round(chiSq / dof, 3) + " ");
		for (i = 0; i < params.length; i++) {
			System.out.print(
				round(
					nonLinFit
						.getParameter(params[i].getName())
						.getDoubleValue(),
					3)
					+ " ");
		}
		System.out.println();

		//fill in the symmetric side
		for (j = 1; j < nVar; j++) {
			for (k = 0; k < j; k++) {
				space.element[k][j] = space.element[j][k];
			}
		}
		//System.out.println("space:\n"+space.toString(3));
	}

	/**
	 * Expands the covariance matrix to include the fixed parameters.
	 */
	private void expandCovarianceMatrix() {
		int i, j, k;
		Matrix temp;
		double temp2;

		temp = new Matrix(nPar, nPar, 0.0);
		for (i = 0; i < nVar; i++) {
			for (j = 0; j < nVar; j++) {
				temp.element[i][j] = covar.element[i][j];
			}
		}
		for (i = nVar; i < nPar; i++) {
			for (j = 0; j <= i; j++) {
				temp.element[i][j] = 0.0;
				temp.element[j][i] = 0.0;
			}
		}
		k = nVar - 1;
		for (j = nPar - 1; j >= 0; j--) {
			if (!parameters[j].isFix()) {
				for (i = 0; i < nPar; i++) {
					temp2 = temp.element[i][k];
					temp.element[i][k] = temp.element[i][j];
					temp.element[i][j] = temp2;
				}
				for (i = 0; i < nPar; i++) {
					temp2 = temp.element[k][i];
					temp.element[k][i] = temp.element[j][i];
					temp.element[j][i] = temp2;
				}
				k--;
			}
		}
		covar = new Matrix(temp);
		System.out.println("\nCovariance Matrix: \n" + covar.toStringUL(3));
	}

	/**
	 * Gives the chi-sqared value of the latest iteration.
	 *	
	 * @return	<code>chiSq/dof</code>
	 */
	public double getChiSq() {
		return chiSq / dof;
	}

	/**
	 * Assigns the error values in the parameters, based on the expanded covariance matrix.
	 */
	private void setParameterErrors() {
		for (int i = 0; i < nPar; i++) {
			parameters[i].setError(Math.sqrt(covar.element[i][i]));
		}
	}

	/**
	 * Rounds a <code>String</code> representation of a <code>double</code> value.
	 *
	 * @param	in	    value to round
	 * @param	fraction    number of digits to show in fractional part
	 * @return		    a <code>String</code> with the specified appearance of the number
	 */
	private String round(double in, int fraction) {
		String out;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMinimumFractionDigits(fraction);
		nf.setMaximumFractionDigits(fraction);
		out = nf.format(in);
		return out;
	}

	public int getDegreesOfFreedom() {
		return dof;
	}

}