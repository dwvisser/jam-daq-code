package jam.io.hdf;

/**
 * Contains field names used in HDF files produced by
 * the <code>jam.io.hdf</code> package.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 */
public interface JamHDFFields {

    
     String FILE_SECTION_NAME="Jam File Section";

     String HIST_SECTION_NAME = "Histograms";
    
     String HIST_TYPE_NAME = "Histogram";
    
     String [] HIST_1D_DIMENSION_LABEL = {"ADC Channel + 1"};

     String [] HIST_2D_DIMENSION_LABEL = {"X Channel + 1","Y Channel + 1"};
        
     String GATE_SECTION_NAME = "Gates";
    
     String GATE_1D_TYPE_NAME = "1-d Gate";
    
     String GATE_2D_TYPE_NAME = "2-d Banana Gate";
    
     String [] GATE_1D_NAMES = {"Lower Limit","Upper Limit"};

     String [] GATE_2D_NAMES = {"X Coordinate","Y Coordinate"};
    
     String SCALER_SECTION_NAME = "Scalers";

     String SCALER_TYPE_NAME = "Scaler Value";
    
     String [] SCALER_COLUMN_NAMES = {"Number","Name","Value"};
    
     String PARAMETER_SECTION_NAME = "Parameters";

     String PARAMETER_TYPE_NAME = "Parameter Value";
    
     String [] PARAMETER_COLUMN_NAMES = {"Name","Value"};
    
     String ERROR_LABEL="Errors";
    
}
