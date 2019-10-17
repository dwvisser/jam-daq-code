package jam.fit;

final class GaussJordanElimination {

	private transient final Matrix inputMatrix; // n by n
	private transient final Matrix inputVectors; // n by m

	private transient final int[] rowIndex;
	private transient final int[] columnIndex;

	private transient final int rows;
	private transient final int columns;

	GaussJordanElimination(final Matrix inMatrix, final Matrix InputVector) {
		this.inputMatrix = new Matrix(inMatrix);
		this.inputVectors = new Matrix(InputVector);
		this.rows = inMatrix.rows;
		this.columns = inputVectors.columns;
		if (rows != inputVectors.rows) {
			throw new IllegalArgumentException(rows + " not equal to "
					+ inputVectors.rows + "!!!");
		}
		rowIndex = new int[rows];
		columnIndex = new int[rows];
	}

	protected void doIt() throws IllegalStateException {
		final int[] pivot = new int[rows];
		int pivotRow = 0;
		int pivotColumn = 0;
		for (int i = 0; i < rows; i++) {
			double big = 0.0;
			for (int j = 0; j < rows; j++) {
				if (pivot[j] != 1) {
					for (int k = 0; k < rows; k++) {
						if (pivot[k] == 0) {
							if (Math.abs(inputMatrix.element[j][k]) >= big) {
								big = Math.abs(inputMatrix.element[j][k]);
								pivotRow = j;
								pivotColumn = k;
							}
						} else if (pivot[k] > 1) {
							throw new IllegalStateException(
									"GJE: Singular Matrix-1");
						}
					}
				}
			}
			pivot[pivotColumn]++;
			doPivoting(i, pivotRow, pivotColumn);
		}
		/* Now unscramble the permuted columns. */
		unscrambleColumns();
	}

	private void doPivoting(final int index, final int pivotRow,
			final int pivotColumn) throws IllegalStateException {
		if (pivotRow != pivotColumn) { // put pivot element on diagonal
			inputMatrix.permute(pivotRow, pivotColumn, 'r');
			inputVectors.permute(pivotRow, pivotColumn, 'r');
		}
		rowIndex[index] = pivotRow;
		columnIndex[index] = pivotColumn;
		if (inputMatrix.element[pivotColumn][pivotColumn] == 0.0) {
			throw new IllegalStateException("GJE: Singular Matrix-2");
		}
		final double pivotInverse = 1.0 / inputMatrix.element[pivotColumn][pivotColumn];
		inputMatrix.element[pivotColumn][pivotColumn] = 1.0;
		inputMatrix.rowMultiply(pivotColumn, pivotInverse);
		inputVectors.rowMultiply(pivotColumn, pivotInverse);
		busyWork(pivotColumn);
	}

	/* Not sure what this does. */
	private void busyWork(final int pivotColumn) {
		for (int ll = 0; ll < rows; ll++) {
			if (ll != pivotColumn) {
				final double dummy = inputMatrix.element[ll][pivotColumn];
				inputMatrix.element[ll][pivotColumn] = 0.0;
				for (int l = 0; l < rows; l++) {
					inputMatrix.element[ll][l] = inputMatrix.element[ll][l]
							- inputMatrix.element[pivotColumn][l] * dummy;
				}
				for (int l = 0; l < columns; l++) {
					inputVectors.element[ll][l] = inputVectors.element[ll][l]
							- inputVectors.element[pivotColumn][l] * dummy;
				}
			}
		}
	}

	private void unscrambleColumns() {
		for (int l = rows - 1; l >= 0; l--) {
			if (rowIndex[l] != columnIndex[l]) {
				inputMatrix.permute(rowIndex[l], columnIndex[l], 'r');
			}
		}
	}

	protected Matrix getMatrix() {
		return inputMatrix;
	}

	protected Matrix getVectors() {
		return inputVectors;
	}
}