package jam.fit;

import java.text.NumberFormat;

/**
*  <p>Matrix is a generic  set of linear algebra algorithms.It can accept a 
*  string argument and return a rectangular matrix array of double precision
*  numbers, along with the row and column sizes.
*  It can also generate certain special matrices, and perform basic
*  matrix operations.</p>
* 
*  <p>This software is public domain and can be used, modified and distributed freely.</p>
* 
* <p>Note that "Matrix" represents an arbitrary array class including row and
*  column vectors.</p>
* 
*  Changes:
*  <dl>
* <dt>25-Jun-98</dt><dd>added rowMultuply...Dale William Visser @ Yale University</dd>
*  <dt>27-Feb-97</dt><dd>added Householder-QR factorization method Q()</dd>
*  <dt>25-Feb-97</dt><dd>	added the transpose method transpose()</dd>
*  <dt>27-Feb-97</dt><dd>	added the default norm() method</dd>
*  <dt>3-March-97</dt><dd>	added the permutation method permute()</dd>
*  <dt>5-March-97</dt><dd>	added *crude* LR and QR eigenvalue methods.</dd>
*  <dt>9-March-97</dt><dd>	added the toHess function to transform a matrix to Hessenberg form</dd>
*  <dt>25-April-97</dt><dd>	added quicksort</dd>
*  <dt>26-April-97</dt><dd>	added average and rank</dd>
*  <dt>11-June-97</dt><dd>	added submatrix selection method</dd>
*  <dt>21-July, 97</dt><dd>	modified QR decomposition (minor improvement)</dd>
*  <dt>21-July, 97</dt><dd>	added QR method that returns 'em both in a vector</dd>
*  <dt>21-July, 97</dt><dd>	added backsolve contructor method A\v</dd>
*  <dt>15-August, 97</dt><dd>	added a sum of squares function for sttistical applications</dd>
*  <dt>18-March, 98</dt><dd>	added a method (leig) to *estimate* the largest eigenvalue of 
* 		a matrix that has positive entries </dd>
*  <dt>30-March, 98</dt><dd>	modified the '\' operation to solve a general dense linear system.</dd>
*  <dt>30-March, 98</dt><dd>	added a backsolve method to solve an upper triangular system</dd>
*  <dt>30-March, 98</dt><dd>	added a forwardsolve method to solve a lower triangular system</dd>
*  <dt>30-March, 98</dt><dd>  modified QR for non-square matrices</dd>
*</dl>
* Still to do:
* <ul>
* <li>incorporate complex numbers</li>
* <li>add an 'append' method</li>
* <li>better eigenvalue methods</li>
* </ul>
* 
* @author <a href="blewis@mcs.kent.edu">Bryan Lewis, Kent State</a>
*
*/
final class Matrix {

	/**
	 *  number of rows in matrix
	 */
	public int rows;

	/**
	 *  number of columns in matrix
	 */
	public int columns;

	/**
	 *  elements of matrix
	 */
	public double[][] element; // the array containing the matrix

	/* non-javadoc:
	 * new r by c zero matrix
	 */
	Matrix(int r, int c) {
		// contructor: creates an empty r by c matrix
		rows = r;
		columns = c;
		element = new double[rows][columns];
	}

	/* non-javadoc:
	 * 	new r by c matrix with entries all = z
	 */
	Matrix(int r, int c, double fill) {
		// contructor: creates an  r by c matrix with entries 'fill'
		rows = r;
		columns = c;
		element = new double[rows][columns];
		int i, j;
		for (i = 0; i < rows; i++) {
			for (j = 0; j < columns; j++) {
				element[i][j] = fill;
			}
		}
	}

	/* non-javadoc:
	 * return a copy of matrix M
	 */
	Matrix(Matrix m) {
		// contructor: creates a new replicate of m
		rows = m.rows;
		columns = m.columns;
		element = new double[rows][columns];
		int i, j;
		for (i = 0; i < rows; i++) {
			for (j = 0; j < columns; j++) {
				element[i][j] = m.element[i][j];
			}
		}
	}

	/* non-javadoc:
	 * Returns a permuted matrix according  code c
	* 	where c is in {'c', 'r'} for columns or rows and
	* 	a1, a2 represent the columns/rows to swap
	*/
	Matrix permute(int a1, int a2, char c) {
		Matrix p = new Matrix(this);
		int i;
		if (c == 'r') {
			for (i = 0; i < columns; i++) {
				p.element[a1][i] = this.element[a2][i];
				p.element[a2][i] = this.element[a1][i];
			}
		} else if (c == 'c') {
			for (i = 0; i < rows; i++) {
				p.element[i][a1] = this.element[i][a2];
				p.element[i][a2] = this.element[i][a1];
			}
		}
		return p;
	}

	/* non-javadoc:
	 * This method returns a string representation of 
	 * the matrix.
	 * 
	 * @param d displayed fractional digits 
	 */
	String toStringUL(int d) {
		final StringBuffer outPut = new StringBuffer(); //return value
		if (this.rows != this.columns) {
			outPut.append("Error: toStringUR() not square!");
		} else {
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(d);
			nf.setMinimumFractionDigits(d);
			for (int i = 0; i < this.rows; i++) {
				for (int j = 0; j <= i; j++) {
					Double x = new Double(this.element[i][j]);
					final String num = nf.format(x);
					outPut.append(num).append((char) 9);
				}
				outPut.append('\n');
			}
		}
		return outPut.toString();
	}

	Matrix sort() {
		/*	Sorts this vector. Returns sorted vector in the 1st column of a
			two column matrix and a permutation vector in the 2nd column.
			(here, 'vector' indicates a 1-d Matrix, not the Java class Vector).
		*/
		int lngth = 0;
		int i = 0;
		double[] a = new double[1];
		int[] indx = new int[1];

		if (this.rows == 1) {
			lngth = this.columns;
			a = new double[lngth]; // contains the data for sorting
			indx = new int[lngth]; // index array
			for (i = 0; i < lngth; i++) {
				a[i] = this.element[0][i];
				indx[i] = i + 1;
			}
		} else if (this.columns == 1) {
			lngth = this.rows;
			a = new double[lngth]; // contains the data for sorting
			indx = new int[lngth]; // index array
			for (i = 0; i < lngth; i++) {
				a[i] = this.element[i][0];
				indx[i] = i + 1;
			}
		}
		qsort(a, indx, 0, lngth - 1);
		Matrix sortR = new Matrix(lngth, 2);
		for (i = 0; i < lngth; i++) {
			sortR.element[i][0] = a[i];
			sortR.element[i][1] = indx[i];
		}
		return sortR;
	}

	private void qsort(double a[], int index[], int lo0, int hi0) {
		/* Quick Sort algorithm: 
		 *   returns permutaion in index[] vector */
		int lo = lo0;
		int hi = hi0;
		if (hi0 > lo0) {
			double mid = a[(lo0 + hi0) / 2];
			while (lo <= hi) {
				while ((lo < hi0) && (a[lo] < mid)) {
					++lo;
				}
				while ((hi > lo0) && (a[hi] > mid)) {
					--hi;
				}
				if (lo <= hi) {
					swap(a, lo, hi);
					swap(index, lo, hi);
					++lo;
					--hi;
				}
			}
			if (lo0 < hi)
				qsort(a, index, lo0, hi);
			if (lo < hi0)
				qsort(a, index, lo, hi0);
		}
	}

	private void swap(double vector[], int slot1, int slot2) {
		final double temp = vector[slot1];
		vector[slot1] = vector[slot2];
		vector[slot2] = temp;
	}

	private void swap(int vector[], int slot1, int slot2) {
		final int temp = vector[slot1];
		vector[slot1] = vector[slot2];
		vector[slot2] = temp;
	}

	void rowMultiply(int row, double factor) {
		for (int i = 0; i < columns; i++) {
			element[row][i] = element[row][i] * factor;
		}
	}
	
	//following stuff still needs to be incorporated into Javadoc properly
	/* Methods (assume A is of class Matrix, e.g., Matrix A = new Matrix()):
	* A.norm() 	(double) returns the Frobenius norm (Matrix), or Euclidean norm (vector)
	* 	   	This is the default norm for a Matrix object. Use the Norm
	* 	   	class for different norms.
	* A.transpose()	(Matrix) Returns the transpose of A.
	* A.sum()		(double) Returns the sum of the elements in A
	* A.max()		(double) Returns the largest (most positive) element of A
	* A.average()	(double) Returns the average of the elements in A
	* A.sumSquares()	(double) Returns the sum of the squares of the elements in A
	* A.Q()		(Matrix) Returns the QR decomposition of A (can be non-square)
	* A.R()		(Matrix) Returns the QR decomposition of A (can be non-square)
	* A.qr()		(Vector) Returns a java.util.Vector object with
	* 			(Matrix)Q in the first position and (Matrix)R in the 2nd.
	* A.genp()	(Vector) Returns a java.util.Vector object with
	* 		(Matrix) P a permutation matrix in the first position
	* 		(Matrix) L in the 2nd position, in which PA = LU
	* 		(Matrix) U in the 3rd position, where PA = LU
	* 		genp usues NO PIVOTING (for example purposes)
	* A.gepp()	(Vector) Returns a java.util.Vector object with
	* 		(Matrix) P a permutation matrix in the first position
	* 		(Matrix) L in the 2nd position, in which PA = LU
	* 		(Matrix) U in the 3rd position, where PA = LU
	* 		The usual Gaussian elimination
	* A.toHess()	(Vector) Returns a java.util.Vector object with the following entries:
	* 		(Matrix) Q in the 1st position
	* 		(Matrix) H in the 2nd position
	* 		where Q'AQ = H, H = upper Hessenberg, Q'Q = I
	* A.sort()	(Matrix) Assumes A is a column vector. Returns a sorted vector
	* 		in column one of the matrix and a permutation vector in column 2
	* 		(uses Quicksort)
	*/

}
