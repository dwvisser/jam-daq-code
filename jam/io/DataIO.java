package jam.io;
import java.io.*;
/**
 * Interface to read in and write out Jam data
 * package
 *
 */
public interface  DataIO {
    /**
     * Used when opening files.
     * 
     * @see #readFile
     */
    public final static int OPEN = 1;
    /**
     * Used when reloading files.
     * 
     * @see #readFile
     */
    public final static int RELOAD = 2;

    /**
     *	write out to a given file all options true
     */ 			        
    public void writeFile(boolean hist, boolean gate, boolean scaler, File file);
    /**
     *	write out to a file, prompted for file
     *  name all options true
     */ 			            
    public int writeFile(boolean hist, boolean gate, boolean scaler) ;    
    /**
     *	write out to a given file all options true
     */ 			        
    public void writeFile(File file);
    
    /**
     *	write out to a file, prompted for file
     *  name all options true
     */ 			            
    public int writeFile() ;
    
    /**
     *	write out to a given file
     *
     */ 			        
    public boolean readFile(int mode);
    
    /**
     *	write out to a given file
     * @param mode Do you 
     */ 			        
    public boolean readFile(int mode, File file);
    
}    