package jam.io;

import jam.data.AbstractHistogram;
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
    void writeFile(File file, List<AbstractHistogram> histList);
                
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
    boolean readFile(FileOpenMode mode, File infile, Group group);
    
    
}    