package jam.io;
import java.io.File;

/**
 * Interface to read in and write out Jam data
 * package
 *
 */
public interface  DataIO {
    /**
     *	write out to a given file
     *
     * @param hist whether to write histograms
     * @param gate whether to write gates
     * @param scaler whether to write scalers
     * @param params whether to write parameters
     * @param file to write to
     */ 			        
    void writeFile(boolean hist, boolean gate, boolean scaler, boolean params, File file);
        
    /**
     *	write out to a given file all options true
     *
     * @param file to write to
     */ 			        
    void writeFile(File file);
        
    /**
     * Read a file in.
     * @param mode open or reload
     * @param file to read in
     * @see FileOpenMode#OPEN
     * @see FileOpenMode#RELOAD
     * @return <code>true</code> if successful
     */ 			        
    boolean readFile(FileOpenMode mode, File file);
}    