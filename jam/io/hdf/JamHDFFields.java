package jam.io.hdf;

/**
 * Contains field names used in HDF files produced by the
 * <code>jam.io.hdf</code> package.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser </a>
 */
public interface JamHDFFields {
    
    /**
     * The section name for the whole file.
     */
    String FILE_SECTION_NAME = "Jam File Section";

    /**
     * The section name for the histograms.
     */
    String HIST_SECTION_NAME = "Histograms";

    /**
     * The type name for the histograms?
     */
    String HIST_TYPE_NAME = "Histogram";

    /**
     * The section name for the gates.
     */
    String GATE_SECTION_NAME = "Gates";

    /**
     * The type name for the 1d gates?
     */
    String GATE_1D_TYPE_NAME = "1-d Gate";

    /**
     * The type name for the 2d gates?
     */
    String GATE_2D_TYPE_NAME = "2-d Banana Gate";

    /**
     * Names for lower and upper limits of 1d gates.
     */
    String[] GATE_1D_NAMES = { "Lower Limit", "Upper Limit" };

    /**
     * Names for x- and y- coordinates lists for 2d gates.
     */
    String[] GATE_2D_NAMES = { "X Coordinate", "Y Coordinate" };

    /**
     * The section name for the scaler values.
     */
    String SCALER_SECTION_NAME = "Scalers";

    /**
     * The type name for the scaler values?
     */
    String SCALER_TYPE_NAME = "Scaler Value";

    /** Column names for scaler table. */
    String[] SCALER_COLUMN_NAMES = { "Number", "Name", "Value" };

    /** The section names for the parameter values. */
    String PARAMETER_SECTION_NAME = "Parameters";

    /** The type name for the parameter values? */
    String PARAMETER_TYPE_NAME = "Parameter Value";

    /** The column names for the parameter table. */
    String[] PARAMETER_COLUMN_NAMES = { "Name", "Value" };

    /** The label for the error bar numerical data groups. */
    String ERROR_LABEL = "Errors";

}