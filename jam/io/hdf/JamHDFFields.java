package jam.io.hdf;

/**
 * Contains field names used in HDF files produced by
 * the <code>jam.io.hdf</code> package.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 */
public interface JamHDFFields {

    final static String FILE_SECTION_NAME="Jam File Section";

    final static String HIST_SECTION_NAME = "Histograms";
    
    final static String HIST_TYPE_NAME = "Histogram";
    
    final static String [] HIST_1D_DIMENSION_LABEL = {"ADC Channel + 1"};

    final static String [] HIST_2D_DIMENSION_LABEL = {"X Channel + 1","Y Channel + 1"};
        
    final static String GATE_SECTION_NAME = "Gates";
    
    final static String GATE_1D_TYPE_NAME = "1-d Gate";
    
    final static String GATE_2D_TYPE_NAME = "2-d Banana Gate";
    
    final static String [] GATE_1D_NAMES = {"Lower Limit","Upper Limit"};

    final static String [] GATE_2D_NAMES = {"X Coordinate","Y Coordinate"};
    
    final static String SCALER_SECTION_NAME = "Scalers";

    final static String SCALER_TYPE_NAME = "Scaler Value";
    
    final static String [] SCALER_COLUMN_NAMES = {"Number","Name","Value"};
    
    final static String PARAMETER_SECTION_NAME = "Parameters";

    final static String PARAMETER_TYPE_NAME = "Parameter Value";
    
    final static String [] PARAMETER_COLUMN_NAMES = {"Name","Value"};
    
    final static String ERROR_LABEL="Errors";
    
}
