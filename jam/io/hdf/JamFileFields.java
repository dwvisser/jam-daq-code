package jam.io.hdf;

/**
 * Contains field names used in HDF files produced by the
 * <code>jam.io.hdf</code> package.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser </a>
 */
public final class JamFileFields {

	private JamFileFields() {
		super();
	}

	/**
	 * The section name for the whole file.
	 */
	public static final String FILE_SECTION = "Jam File Section";

	/**
	 * The section name for the histograms.
	 */
	public static final String GRP_SECTION = "Groups";

	/**
	 * The type name for the groups?
	 */
	public static final String GROUP_TYPE = "GROUP";

	/**
	 * The section name for the histograms.
	 */
	public static final String HIST_SECTION = "Histograms";

	/**
	 * The type name for the histograms?
	 */
	public static final String HIST_TYPE = "Histogram";

	/**
	 * labels to do with calibrations
	 * @author Dale Visser
	 *
	 */
	public static final class Calibration {
		
		private Calibration(){
			super();
		}

		/**
		 * The type name for the calibration with points
		 */
		public static final String TYPE_POINTS = "CalibrationPoints";

		/**
		 * The type name for the calibration with coefficients
		 */
		public static final String TYPE_COEFF = "CalibrationCoefficients";

		/**
		 * Column names for calibration with points
		 */
		public static final String[] COLUMNS_POINTS = { "Channel", "Energy" };

		/**
		 * Column names for calibration with coefficients
		 */
		public static final String[] COLUMNS_COEFF = { "Coefficients" };
	}

	/**
	 * The section name for the gates.
	 */
	public static final String GATE_SECTION = "Gates";

	/**
	 * The type name for the 1d gates?
	 */
	public static final String GATE_1D_TYPE = "1-d Gate";

	/**
	 * The type name for the 2d gates?
	 */
	public static final String GATE_2D_TYPE = "2-d Banana Gate";

	/**
	 * Names for lower and upper limits of 1d gates.
	 */
	public static final String[] GATE_1D = { "Lower Limit", "Upper Limit" };

	/**
	 * Names for x- and y- coordinates lists for 2d gates.
	 */
	public static final String[] GATE_2D = { "X Coordinate", "Y Coordinate" };

	/**
	 * The section name for the scaler values.
	 */
	public static final String SCALER_SECT = "Scalers";

	/**
	 * The type name for the scaler values?
	 */
	public static final String SCALER_TYPE = "Scaler Value";

	/** Column names for scaler table. */
	public static final String[] SCALER_COLS = { "Number", "Name", "Value" };

	/** The section names for the parameter values. */
	public static final String PARAMETERS = "Parameters";

	/** The type name for the parameter values? */
	public static final String PAR_TYPE = "Parameter Value";

	/** The column names for the parameter table. */
	public static final String[] PARAM_COLS = { "Name", "Value" };

	/** The label for the error bar numerical data groups. */
	public static final String ERROR_LABEL = "Errors";

}