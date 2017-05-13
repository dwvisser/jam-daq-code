package jam.fit;

import java.text.NumberFormat;
import java.util.Arrays;

/**
 * For the original more complete class, e-mail the author.
 * 
 * @author <a href="blewis@mcs.kent.edu">Bryan Lewis, Kent State</a>
 * 
 */
final class Matrix {

	/**
	 * number of rows in matrix
	 */
	transient int rows;// NOPMD

	/**
	 * number of columns in matrix
	 */
	transient int columns;// NOPMD

	/**
	 * elements of matrix
	 */
	transient double[][] element; // NOPMD

	/*
	 * non-javadoc: new r by c zero matrix
	 */
	Matrix(final int nRows, final int nCols) {
		rows = nRows;
		columns = nCols;
		element = new double[rows][columns];
	}

	/*
	 * non-javadoc: new r by c matrix with all entries = fill
	 */
	Matrix(final int nRows, final int nCols, final double fill) {
		this(nRows, nCols);
		for (int i = 0; i < rows; i++) {
			Arrays.fill(element[i], fill);
		}
	}

	/*
	 * non-javadoc: return a copy of matrix M
	 */
	Matrix(final Matrix original) {
		this(original.rows, original.columns);
		for (int i = 0; i < rows; i++) {
			System.arraycopy(original.element[i], 0, element[i], 0, columns);
		}
	}

	/*
	 * non-javadoc: Returns a permuted matrix according code c where c is in
	 * {'c', 'r'} for columns or rows and a1, a2 represent the columns/rows to
	 * swap
	 */
	protected Matrix permute(final int arg1, final int arg2, final char which) {
		final Matrix rval = new Matrix(this);
		final boolean row = which == 'r';
		final boolean col = which == 'c';
		if (row) {
			for (int i = 0; i < columns; i++) {
				rval.element[arg1][i] = element[arg2][i];
				rval.element[arg2][i] = element[arg1][i];
			}
		} else if (col) {
			for (int i = 0; i < rows; i++) {
				rval.element[i][arg1] = element[i][arg2];
				rval.element[i][arg2] = element[i][arg1];
			}
		} else {
			throw new IllegalArgumentException("Use 'r' or 'c' for which.");
		}
		return rval;
	}

	/*
	 * non-javadoc: This method returns a string representation of the matrix.
	 * 
	 * @param frac displayed fractional digits
	 */
	protected String toStringUL(final int frac) {
		final StringBuilder outPut = new StringBuilder(); // return value
		if (rows == columns) {
			final NumberFormat formatter = NumberFormat.getInstance();
			formatter.setMaximumFractionDigits(frac);
			formatter.setMinimumFractionDigits(frac);
			for (int i = 0; i < this.rows; i++) {
				for (int j = 0; j <= i; j++) {
					final String num = formatter.format(element[i][j]);
					outPut.append(num).append((char) 9);
				}
				outPut.append('\n');
			}
		} else {
			throw new UnsupportedOperationException(
					"toStringUL() must be called on a square matrix.");
		}
		return outPut.toString();
	}

	/* non-javadoc: Multiplies the given row by the given factor. */
	protected void rowMultiply(final int row, final double factor) {
		for (int i = 0; i < columns; i++) {
			element[row][i] = element[row][i] * factor;
		}
	}
}
