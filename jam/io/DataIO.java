package jam.io;

import jam.data.Group;

import java.io.File;
import java.util.List;

/**
 * Interface to read in and write out Jam data
 * package
 *
 */
public interface  DataIO {
    
    /**
     * Writes out to a specified file all the currently held spectra, gates,
     * scalers, and parameters.
     * 
     * @param file
     *            to write to
     */
    void writeFile(File file);

    /**
     * Writes out to a specified file all the currently held spectra, gates,
     * scalers, and parameters associated with the given group.
     * 
     * @param file
     *            to write to
     * @param group to writ out
     */
    void writeFile(File file, Group group);
    
    /**
     * Write out a file with the specified histograms and 
     * all gates that go with them, plus scalers and parameters.
     * 
     * @param file to write to
     * @param histList list of histograms to write
     */
    void writeFile(File file, List histList);
                
    /**
     * Read a file in.
     * @param mode open or reload
     * @param file to read in
     * @see FileOpenMode#OPEN
     * @see FileOpenMode#RELOAD
     * @return <code>true</code> if successful
     */ 			        
    boolean readFile(FileOpenMode mode, File file);
    /**
     * Read in an HDF file.
     * 
     * @param infile
     *            file to load
     * @param mode
     *            whether to open or reload
     * @param group
     * 			  group to read in
     * @return <code>true</code> if successful
     */
    public boolean readFile(FileOpenMode mode, File infile, Group group);
    /**
     * Writes out (to a specific file) the currently held spectra, gates, and
     * scalers, subject to the options given. Sets separately which data
     * writeFile should actually output. Not writing histograms when you are
     * saving tape data can significantly save time when you have many 2-d
     * spectra.
     * 
     * @param hist
     *            if true, Histograms will be written
     * @param gate
     *            if true, Gates will be written
     * @param scaler
     *            if true, scaler values will be written
     * @param params
     *            if true, parameter values will be written
     * @param file
     *            to write to
     */
    //FIXME KBS old write remove when new writes tested
    void writeFile(boolean hist, boolean gate, boolean scaler, boolean params,
            File file);
    
    
}    