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
    String FILE_SECTION = "Jam File Section";

    /**
     * The section name for the histograms.
     */
    String GROUP_SECTION = "Groups";

    /**
     * The type name for the histograms?
     */
    String GROUP_TYPE = "GROUP";
    
    /**
     * The section name for the histograms.
     */
    String HIST_SECTION = "Histograms";

    /**
     * The type name for the histograms?
     */
    String HIST_TYPE = "Histogram";

    /**
     * The section name for the gates.
     */
    String GATE_SECTION = "Gates";

    /**
     * The type name for the 1d gates?
     */
    String GATE_1D_TYPE = "1-d Gate";

    /**
     * The type name for the 2d gates?
     */
    String GATE_2D_TYPE = "2-d Banana Gate";

    /**
     * Names for lower and upper limits of 1d gates.
     */
    String[] GATE_1D = { "Lower Limit", "Upper Limit" };

    /**
     * Names for x- and y- coordinates lists for 2d gates.
     */
    String[] GATE_2D = { "X Coordinate", "Y Coordinate" };

    /**
     * The section name for the scaler values.
     */
    String SCALER_SECT = "Scalers";

    /**
     * The type name for the scaler values?
     */
    String SCALER_TYPE = "Scaler Value";

    /** Column names for scaler table. */
    String[] SCALER_COLS = { "Number", "Name", "Value" };

    /** The section names for the parameter values. */
    String PARAMETERS = "Parameters";

    /** The type name for the parameter values? */
    String PAR_TYPE = "Parameter Value";

    /** The column names for the parameter table. */
    String[] PARAM_COLS = { "Name", "Value" };

    /** The label for the error bar numerical data groups. */
    String ERROR_LABEL = "Errors";

}