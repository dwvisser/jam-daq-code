package jam.fit;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

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
public class Matrix {

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

	/**
	 * new r by c zero matrix
	 */
	public Matrix(int r, int c) {
		// contructor: creates an empty r by c matrix
		rows = r;
		columns = c;
		element = new double[rows][columns];
	}
	/**
	 * new 1x1 matrix with value d (i.e., a scalar)
	 */
	public Matrix(double d) {
		// contructor: creates a 1x1 matrix of double d
		rows = 1;
		columns = 1;
		element = new double[1][1];
		element[0][0] = d;
	}

	/**
	 * perform an operation on A & B
	 * <br>
	*  		operation = '+' -> return a new matrix equal to A + B<br>
	*  		operation = '-' -> return a new matrix equal to A - B<br>
	*  		operation = '*' -> return a new matrix equal to A * B 
	* 			(arbitrary matrix-vector product)<br>
	* 		operation = '\' -> backsolve similar to MATLAB x = A\b<br>
	*/
	public Matrix(Matrix m1, Matrix m2, char code) {
		// constructor method for matrix add, product and so on
		if (((code == '+') || (code == '-'))
			&& (m1.rows == m2.rows)
			&& (m1.columns == m2.columns)) {
			// add
			rows = m1.rows;
			columns = m1.columns;
			element = new double[rows][columns];
			int i, j;
			for (i = 0; i < rows; i++) {
				for (j = 0; j < columns; j++) {
					switch (code) {
						case '+' :
							element[i][j] = m1.element[i][j] + m2.element[i][j];
							break;
						case '-' :
							element[i][j] = m1.element[i][j] - m2.element[i][j];
							break;
						default://do nothing
							break; 
					}
				}
			}
		} else if ((code == '*') && (m1.columns == m2.rows)) {
			// matrix product
			double sum = 0;
			int k = 0;
			rows = m1.rows;
			columns = m2.columns;
			element = new double[rows][columns];
			int i, j;
			for (i = 0; i < rows; i++) {
				for (k = 0; k < m2.columns; k++) {
					for (j = 0; j < m1.columns; j++) {
						sum = sum + m1.element[i][j] * m2.element[j][k];
					}
					element[i][k] = sum;
					sum = 0;
				}
			}
		} else if ((code == '*') && (m1.columns == 1) && (m1.rows == 1)) {
			// scalar-vector product
			rows = m2.rows;
			columns = m2.columns;
			element = new double[rows][columns];
			int i, j;
			for (i = 0; i < rows; i++) {
				for (j = 0; j < columns; j++) {
					element[i][j] = m1.element[0][0] * m2.element[i][j];
				}
			}
		} else if ((code == '/') && (m2.columns == 1) && (m2.rows == 1)) {
			// vector-scalar division
			rows = m1.rows;
			columns = m1.columns;
			element = new double[rows][columns];
			int i, j;
			for (i = 0; i < rows; i++) {
				for (j = 0; j < columns; j++) {
					element[i][j] = m1.element[i][j] / m2.element[0][0];
				}
			}
		} else if (
			(code == '\\')
				&& (m1.columns == m1.rows)
				&& (m2.columns == 1)
				&& (m2.rows == m1.rows)) {
			/* Solve a general, dense, non-singular linear system Ax=b via QR, where
			   A=m1, b=m2, and x is returned.
			*/
			rows = m1.rows;
			columns = 1;
			element = new double[rows][columns];
			int i, j;
			double sum = 0;

			element[rows - 1][0] =
				m2.element[rows - 1][0] / m1.element[rows - 1][rows - 1];

			i = rows - 1;
			while (i >= 0) {
				sum = 0;
				j = rows - 1;
				while (j >= i + 1) {
					sum = sum + m1.element[i][j] * element[j][0];
					j--;
				}
				element[i][0] = (m2.element[i][0] - sum) / m1.element[i][i];
				i--;
			}
		}

	}

	/**
	 * perform an operation on y & A<br>
	* 		operation = '*' -> scalar-matrix product y*A
	*/
	public Matrix(double x, Matrix m, char code) {
		if (code == '*') {
			// scalar-vector product
			rows = m.rows;
			columns = m.columns;
			element = new double[rows][columns];
			int i, j;
			for (i = 0; i < rows; i++) {
				for (j = 0; j < columns; j++) {
					element[i][j] = x * m.element[i][j];
				}
			}
		}

	}

	/**
	 * 	new r by c matrix with entries all = z
	 */
	public Matrix(int r, int c, double fill) {
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

	/**
	 * return a copy of matrix M
	 */
	public Matrix(Matrix m) {
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

	/**
	 * New r by c special matrix:
	 * <dl>
	*  		<dt>code = 'I' or 'i'</dt><dd>identity matrix</dd>
	*  		<dt>code = 'h' or 'H'</dt><dd>Hilbert matrix</dd>
	* 		<dt>code = 'r' or 'R'</dt><dd>Uniform random entries in [0,1]</dd>
	* </dl>
	*/
	public Matrix(int r, int c, char code) {
		// contructor: creates an  r by c special matrix
		rows = r;
		columns = c;
		element = new double[rows][columns];
		int i, j;
		if ((code == 'i') || (code == 'I')) {
			// make an identity matrix
			for (i = 0; i < r; i++) {
				if (i < c) {
					element[i][i] = 1;
				}
			}
		} else if ((code == 'h') || (code == 'H')) {
			// make a Hilbert matrix
			for (i = 0; i < r; i++) {
				for (j = 0; j < c; j++) {
					element[i][j] = 1 / ((double) i + (double) j + 1);
				}
			}
		} else if ((code == 'r') || (code == 'R')) {
			// make a random matrix with entries uniform in [0, 1]
			for (i = 0; i < r; i++) {
				for (j = 0; j < c; j++) {
					element[i][j] = Math.random();
				}
			}
		}
	}

	/**
	 * New matrix with entries parsed from string s.
	 * The string format can be like Matlab or typical delimited ascii data.
	 */
	public Matrix(String s) {
		// constructor: creates a matrix and parses the string into the element array
		Vector row = new Vector(); // data will be assembled into these vectors
		Vector col = new Vector();
		// and then transferred into the array element[][].

		s = s + " ;";
		int i = s.length();
		int j;
		int rowCounter = 0;
		int colCounter = 0;
		StringBuffer sData = new StringBuffer(); // will hold each element during parsing
		char sChar;
		for (j = 0; j < i; j++) {
			/*	Delimiter syntax:
				columns separated by tabs, commas, or sapces
				rows separated by newline or semicolon
				short rows filled with zeros
			*/
			sChar = s.charAt(j);			
			if ((sChar == ' ')
				|| (sChar == ',')
				|| ((int) sChar == 9)
				|| (sChar == ';')
				|| ((int) sChar == 13)
				|| ((int) sChar == 10)) {// check for a delimiter...
				// See if the string in sData represents a number...
				try {
					// a hack to accomodate JVM 1.1 and higher...
					boolean testSpace = true;
					int ii;
					for (ii = 0; ii < sData.length(); ii++) {
						testSpace = testSpace && (sData.charAt(ii) == ' ');
					}
					if (!testSpace) {
						col.addElement(sData.toString());
					} // append column element as string
					sData = new StringBuffer(); // wipe out contents of string
				} catch (Exception e) {
					// non-numeric stuff...
					sData = new StringBuffer(); // wipe out contents of string
				}
				if (((sChar == ';')
					|| ((int) sChar == 13)
					|| ((int) sChar == 10))
					&& !col.isEmpty()) {
					row.addElement(col);
					// append row (i.e., vector of column elements)
					rowCounter = rowCounter + 1;
					sData = new StringBuffer(); // wipe out contents of string
					colCounter = col.size();
					col = new Vector(); // wipe out the column vector
					/* an interesting Java note: use new Vector() method to
					   force the contents of this vector to be explicitly copied
					   into the row vector. The removeAllElements method will not
					   work in this situation (try it!).
					*/
				}
			} else {// build up data...
				if ((Character.isDigit(sChar))
					|| (sChar == '.')
					|| (sChar == '-')) {
					// allow only digit and decimal point characters
					sData.append(sChar); // append to string
				}
			}

		}
		rows = rowCounter;
		columns = colCounter;
		element = new double[rows][columns];
		col = new Vector();
		Double d = new Double(0);
		for (j = 0; j < rows; j++) {
			col = (Vector) row.elementAt(j);
			for (i = 0; i < col.size(); i++) {
				d = new Double((String) col.elementAt(i));
				element[j][i] = d.doubleValue();
			}
		}
	}

	public Matrix transpose() {
		// returns the transpose of this matrix object
		Matrix t = new Matrix(columns, rows);
		int i, j;
		for (i = 0; i < rows; i++) {
			for (j = 0; j < columns; j++) {
				t.element[j][i] = this.element[i][j];
			}
		}
		return t;
	}

	/**
	 * returns the submatrix specified by the row and column range arguments
	* 	requires r2>=r1, c2>=c1
	*/
	public Matrix sub(int r1, int r2, int c1, int c2) {
		/*
		 * returns the submatrix specified by the row and column range arguments
		 * requires r2>=r1, c2>=c1
		 */
		Matrix rval = new Matrix(r2 - r1 + 1, c2 - c1 + 1);
		int i, j;
		for (i = r1; i <= r2; i++) {
			for (j = c1; j <= c2; j++) {
				rval.element[i - r1][j - c1] = this.element[i][j];
			}
		}
		return rval;
	}

	/**
	 * Returns a permuted matrix according  code c
	* 	where c is in {'c', 'r'} for columns or rows and
	* 	a1, a2 represent the columns/rows to swap
	*/
	public Matrix permute(int a1, int a2, char c) {
		/*	Returns a permuted matrix according  code c
			where c is in {'c', 'r'} for columns or rows and
			a1, a2 represent the columns/rows to swap
		*/
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

	public double norm() {
		/* returns the Frobenius norm (Matrix), or Euclidean norm (Vector)
		   This is the default norm for a Matrix object. Use the Norm
		   class for different norms.
		*/
		double l = 0;
		int i, j;
		for (i = 0; i < rows; i++) {
			for (j = 0; j < columns; j++) {
				l = l + this.element[i][j] * this.element[i][j];
			}
		}
		l = Math.pow(l, 0.5);
		return l;
	}

	public double max() {
		/* returns the most positive element of the matrix or vector */
		double m = this.element[0][0];
		int i, j;
		for (i = 0; i < rows; i++) {
			for (j = 0; j < columns; j++) {
				if (this.element[i][j] > m) {
					m = this.element[i][j];
				}
			}
		}
		return m;
	}

	public double sum() {
		/* returns the sum of all the elements in the matrix or vector
		*/
		double s = 0;
		int i, j;
		for (i = 0; i < rows; i++) {
			for (j = 0; j < columns; j++) {
				s = s + this.element[i][j];
			}
		}
		return s;
	}

	public double average() {
		// returns the average of all the elements in the matrix or vector
		double s = 0;
		int i, j;
		for (i = 0; i < rows; i++) {
			for (j = 0; j < columns; j++) {
				s = s + this.element[i][j];
			}
		}
		return s / (columns * rows);
	}

	public double sumSquares() {
		// returns the sum the squares of all the elements in the matrix or vector
		double s = 0;
		int i, j;
		for (i = 0; i < rows; i++) {
			for (j = 0; j < columns; j++) {
				s = s + Math.pow(this.element[i][j], 2);
			}
		}
		return s;
	}

	/* matrices used during qr decomposition */
	private Matrix pMatrix, aMatrix;
	boolean calculated=false;
	private void qrDecompose() {
		/*	returns the QR-decomposition of this matrix object
			using Householder rotations, without column pivoting
		*/
		pMatrix = new Matrix(rows, rows, 'I');
		aMatrix = new Matrix(this);
		for (int j = 0; j < columns; j++) {
			Matrix v = aMatrix.sub(0, aMatrix.rows - 1, j, j);
			if (j > 0) {
				for (int i = 0; i < j; i++) {
					v.element[i][0] = 0;
				}
			}
			v.element[j][0] =
				v.element[j][0] + v.norm() * sign(v.element[j][0]);
			double r = (double) - 2 / (v.norm() * v.norm());
			final Matrix aaMatrix = new Matrix(aMatrix);
			aMatrix = new Matrix(v.transpose(), aMatrix, '*');
			aMatrix = new Matrix(v, aMatrix, '*');
			aMatrix = new Matrix(r, aMatrix, '*');
			aMatrix = new Matrix(aaMatrix, aMatrix, '+');
			final Matrix ppMatrix = new Matrix(pMatrix);
			pMatrix = new Matrix(v.transpose(), pMatrix, '*');
			pMatrix = new Matrix(v, pMatrix, '*');
			pMatrix = new Matrix(r, pMatrix, '*');
			pMatrix = new Matrix(ppMatrix, pMatrix, '+');
		}
		calculated=true;
	}
	
	public synchronized Matrix qrGetQ(){
		if (!calculated){
			qrDecompose();
		}
		return pMatrix.transpose();		
	}

	public synchronized Matrix qrGetR() {
		if (!calculated){
			qrDecompose();
		}
		return aMatrix;
	}

	public synchronized List qrGetList() {
		final List result=new ArrayList();
		result.add(qrGetR()); // R (at element 0)
		result.add(qrGetQ()); // Q (at element 1)
		return result;

	}

	public List toHess() {
		/*	makes the matrix upper Hessenberg via Householder rotations
			returns  P, H, s.t. P' * this * P = H and H is upper Hessenberg
			and P' * P = I.
		*/
		Vector result = new Vector(); // the result
		Matrix hessP = new Matrix(rows, columns, 'I');
		Matrix hessI = new Matrix(rows, columns, 'I');
		Matrix hessA = new Matrix(this);
		int i, j;
		Matrix v;

		for (j = 0; j < columns - 2; j++) {
			v = new Matrix(rows, 1);
			v.element[j][0] = 1;
			v = new Matrix(hessA, v, '*'); // get the j-th column
			for (i = 0; i < (j + 1); i++) {
				v.element[i][0] = 0;

			}
			v.element[j + 1][0] =
				v.element[j + 1][0] + v.norm() * sign(v.element[j + 1][0]);

			double r = (double) - 2 / (v.norm() * v.norm());
			v = new Matrix(v, v.transpose(), '*');
			v = new Matrix(r, v, '*');
			v = new Matrix(hessI, v, '+');
			hessP = new Matrix(hessP, v, '*');
			hessA = new Matrix(hessP.transpose(), this, '*');
			hessA = new Matrix(hessA, hessP, '*');

		}

		result.addElement(hessP); // the orthogonal operator
		result.addElement(hessA); // the upper-Hessenberg form
		return result;

	}

	public List genp() {
		/*	returns the LU decomposition of a matrix using the Gauss
			transform. This algorithm returns 3 matrices as follows:
			[P, L, U] such that LU = PA. This algorithm performs no
			pivoting other than to assure that the matrix is non-singular.
			As such, it is practical only from a pedagogical standpoint.
			Written 3-March, 1997.
		*/
		Vector v = new Vector(); // the result
		Matrix genpP = new Matrix(rows, columns, 'I');
		// P will track the permutations
		Matrix lMatrix = new Matrix(rows, columns, 'I'); // the lower triangle
		Matrix uMatrix = this; // this matrix to be transformed to upper triangular
		Matrix gMatrix = new Matrix(rows, columns, 'I');
		// temporary Gauss transform matrix
		int i, j, k;

		for (j = 0; j < columns - 1; j++) {
			for (i = (j + 1); i < rows; i++) {
				if (uMatrix.element[j][j] != 0) {
					gMatrix.element[i][j] = -uMatrix.element[i][j] / uMatrix.element[j][j];
				}
			}
			uMatrix = new Matrix(gMatrix, uMatrix, '*');
			for (k = (j + 1); k < rows; k++) {
				lMatrix.element[k][j] = -gMatrix.element[k][j];
				gMatrix.element[k][j] = 0;
			}
		}
		v.addElement(genpP);
		v.addElement(lMatrix);
		v.addElement(uMatrix);
		return v;

	}

	public List gepp() {
		/*	returns the LU decomposition of a matrix using the Gauss
			transform. This algorithm returns 3 matrices as follows:
			[P, L, U] such that LU = PA. This algorithm performs partial
			pivoting.
			Written 3-March, 1997 by Bryan Lewis
		*/
		List rval = new ArrayList(); // the result
		Matrix geppP = new Matrix(rows, columns, 'I');
		// P will track the permutations
		Matrix lMatrix = new Matrix(rows, columns, 'I'); // the lower triangle
		Matrix uMatrix = this; // this matrix to be transformed to upper triangular
		Matrix gMatrix = new Matrix(rows, columns, 'I');
		// temporary Gauss transform matrix
		int i, j, k, p;
		double d;

		for (j = 0; j < columns - 1; j++) {
			// start of parital pivot code:
			d = Math.abs(uMatrix.element[j][j]);
			p = j;
			for (i = j + 1; i < rows; i++) {
				if (Math.abs(uMatrix.element[i][j]) > d) {
					d = Math.abs(uMatrix.element[i][j]);
					p = i;
				}
			}
			if (p > j) {
				uMatrix = uMatrix.permute(j, p, 'r');
				geppP = geppP.permute(j, p, 'r'); // don't forget to track permutations
			}
			// end of partial pivot code.

			for (i = j + 1; i < rows; i++) {
				if (uMatrix.element[j][j] != 0) {
					gMatrix.element[i][j] = -uMatrix.element[i][j] / uMatrix.element[j][j];
				}
			}
			uMatrix = new Matrix(gMatrix, uMatrix, '*');
			lMatrix = lMatrix.permute(j, p, 'r');
			for (k = 0; k < j; k++) {
				lMatrix.element[k][j] = 0;
			}
			lMatrix.element[j][j] = 1;
			for (k = j + 1; k < rows; k++) {
				lMatrix.element[k][j] = -gMatrix.element[k][j];
				gMatrix.element[k][j] = 0;
			}

		}
		for (k = 0; k < rows; k++) {
			lMatrix.element[k][columns - 1] = 0;
		}
		lMatrix.element[rows - 1][columns - 1] = 1;

		rval.add(geppP);
		rval.add(lMatrix);
		rval.add(uMatrix);
		return rval;

	}

	public Matrix lr(int iter) {
		// Very basic LR eigenvalue method (no pivot) for illustration only
		Matrix lrL = new Matrix(rows, columns);
		Matrix lrU = new Matrix(rows, columns);
		Matrix lrA = new Matrix(this); // initialized
		List v = new Vector();
		int i;
		for (i = 0; i < iter; i++) {
			v = lrA.genp(); // get LU factorization
			lrL = (Matrix) v.get(1);
			lrU = (Matrix) v.get(2);
			lrA = new Matrix(lrU, lrL, '*');
		}
		return lrA;
	}

	/**
	 * Returns a nicely formatted string version of the
	* 			matrix A with n displayed digits
	*/
	public String toString(int digits) {
		/*
		 * this method returns a string representation of the matrix d displayed
		 * fractional digits
		 */
		final NumberFormat formatter = NumberFormat.getInstance();
		formatter.setMaximumFractionDigits(digits);
		formatter.setMinimumFractionDigits(digits);
		final StringBuffer outPut = new StringBuffer();
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.columns; j++) {
				final Double cell = new Double(this.element[i][j]);
				final String num = formatter.format(cell);
				outPut.append(num).append((char) 9);
			}
			outPut.append('\n');
		}
		return outPut.toString();
	}

	/**
	 * This method returns a string representation of 
	 * the matrix.
	 * 
	 * @param d displayed fractional digits 
	 */
	public String toStringUL(int d) {
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

	public Matrix sort() {
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

	public Matrix order() {
		/*
		 * Sorts this vector, returning a ranked vector, (here, 'vector'
		 * indicates a 1-d Matrix, not the Java class Vector). 'order' is used
		 * so as to not confuse with the usual definition of rank
		 */
		Matrix orderS = this.sort();
		Matrix y = new Matrix(orderS.rows, 1);
		double[] v = new double[orderS.rows];
		// the only trick here is to handle ties!
		int i = 0, k, j, l;
		while (i < orderS.rows) {
			j = 0;
			l = 0;
			for (k = i; k < orderS.rows; k++) {
				if (orderS.element[k][0] == orderS.element[i][0]) {
					j = j + 1;
					l = l + k + 1;
				}
			}
			for (k = 0; k < j; k++) {
				v[i + k] = (double) (((double) (l)) / (double) j);
			}
			i = i + j;
		}
		// now unsort v and return it...
		for (i = 0; i < orderS.rows; i++) {
			y.element[(int) orderS.element[i][1] - 1][0] = v[i];
		}
		return y;
	}

	// The following methods are used internally by the Matrix class:

	double sign(double d) {
		// returns the sign of the supplied double-precision argument
		double s = 1;
		if (d < 0) {
			s = -1;
		}
		return s;
	}

	void qsort(double a[], int index[], int lo0, int hi0) {
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

	private void swap(double a[], int i, int j) {
		final double temp = a[i];
		a[i] = a[j];
		a[j] = temp;
	}

	private void swap(int a[], int i, int j) {
		final int temp = a[i];
		a[i] = a[j];
		a[j] = temp;
	}

	public void rowMultiply(int row, double factor) {
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
