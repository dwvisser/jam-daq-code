package jam.io;
import java.io.File;

import javax.swing.JFileChooser;
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
     *	write out to a file, prompted for file
     * 
     * @param hist whether to write histograms
     * @param gate whether to write gates
     * @param scaler whether to write scalers
     * @param params whether to write parameters
	 * @return <code>JFileChooser.APPROVE_OPTION</code> or
	 *         <code>JFileChooser.CANCEL_OPTION</code>
	 * @see JFileChooser#APPROVE_OPTION
	 * @see JFileChooser#CANCEL_OPTION
     */ 			            
    int writeFile(boolean hist, boolean gate, boolean scaler, boolean params);
    
    /**
     *	write out to a given file all options true
     *
     * @param file to write to
     */ 			        
    void writeFile(File file);
    
    /**
     *	write out to a file, prompted for file
     *  name all options true
	 * @return <code>JFileChooser.APPROVE_OPTION</code> or
	 *         <code>JFileChooser.CANCEL_OPTION</code>
	 * @see JFileChooser#APPROVE_OPTION
	 * @see JFileChooser#CANCEL_OPTION
     */ 			            		            
    int writeFile() ;
    
    /**
     * Prompt for a file with a file chooser and read it in.
     *
     * @param mode open or reload
     * @see FileOpenMode#OPEN
     * @see FileOpenMode#RELOAD
     * @return <code>true</code> if successful
     */ 			        
    boolean readFile(FileOpenMode mode);
    
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