package jam.fit;

class GaussJordanElimination {

	private Matrix inputMatrix; // n by n
	private Matrix inputVectors; // n by m

	private int[] rowIndex;
	private int[] columnIndex;
	private int[] pivot;

	private int rows;
	private int columns;

	private int pivotRow;
	private int pivotColumn;

	GaussJordanElimination(Matrix InputMatrix, Matrix InputVector) {
		this.inputMatrix = new Matrix(InputMatrix);
		this.inputVectors = new Matrix(InputVector);
		this.rows = InputMatrix.rows;
		this.columns = inputVectors.columns;
		if (rows != inputVectors.rows) {
			throw new IllegalArgumentException(
				rows + " not equal to " + inputVectors.rows + "!!!");
		}
		rowIndex = new int[rows];
		columnIndex = new int[rows];
		pivot = new int[rows];
	}

	public void go() throws Exception {
		clearPivot();
		findPivots();
	}

	private void clearPivot() {
		int i;

		for (i = 0; i < rows; i++) {
			pivot[i] = 0;
		}
	}

	private void findPivots() throws Exception {
		int k;
		int j;
		int ll;
		int l;
		int i;
		double big;
		double pivotInverse;
		double dummy;

		for (i = 0; i < rows; i++) {
			big = 0.0;
			for (j = 0; j < rows; j++) {
				if (pivot[j] != 1) {
					for (k = 0; k < rows; k++) {
						if (pivot[k] == 0) {
							if (Math.abs(inputMatrix.element[j][k]) >= big) {
								big = Math.abs(inputMatrix.element[j][k]);
								pivotRow = j;
								pivotColumn = k;
							}
						} else if (pivot[k] > 1) {
							throw new IllegalStateException("GJE: Singular Matrix-1");
						}
					}
				}
			}
			pivot[pivotColumn]++;
			if (pivotRow != pivotColumn) { //put pivot element on diagonal
				inputMatrix.permute(pivotRow, pivotColumn, 'r');
				inputVectors.permute(pivotRow, pivotColumn, 'r');
			}
			rowIndex[i] = pivotRow;
			columnIndex[i] = pivotColumn;
			if (inputMatrix.element[pivotColumn][pivotColumn] == 0.0) {
				throw new IllegalStateException("GJE: Singular Matrix-2");
			}
			pivotInverse = 1.0 / inputMatrix.element[pivotColumn][pivotColumn];
			inputMatrix.element[pivotColumn][pivotColumn] = 1.0;
			inputMatrix.rowMultiply(pivotColumn, pivotInverse);
			inputVectors.rowMultiply(pivotColumn, pivotInverse);
			for (ll = 0; ll < rows; ll++) {
				if (ll != pivotColumn) {
					dummy = inputMatrix.element[ll][pivotColumn];
					inputMatrix.element[ll][pivotColumn] = 0.0;
					for (l = 0; l < rows; l++) {
						inputMatrix.element[ll][l] =
							inputMatrix.element[ll][l]
								- inputMatrix.element[pivotColumn][l] * dummy;
					}
					for (l = 0; l < columns; l++) {
						inputVectors.element[ll][l] =
							inputVectors.element[ll][l]
								- inputVectors.element[pivotColumn][l] * dummy;
					}
				}
			}
		}
		//Now unscramble the permuted columns.
		for (l = rows - 1; l >= 0; l--) {
			if (rowIndex[l] != columnIndex[l]) {
				inputMatrix.permute(rowIndex[l], columnIndex[l], 'r');
			}
		}
	}

	public Matrix getMatrix() {
		return inputMatrix;
	}
	public Matrix getVectors() {
		return inputVectors;
	}
}