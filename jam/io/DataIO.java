package jam.io;
import java.io.File;
/**
 * Interface to read in and write out Jam data
 * package
 *
 */
public interface  DataIO {
    /**
     *	write out to a given file all options true
     */ 			        
    public void writeFile(boolean hist, boolean gate, boolean scaler, File file);
    
    /**
     *	write out to a file, prompted for file
     *  name all options true
     */ 			            
    public int writeFile(boolean hist, boolean gate, boolean scaler);
    
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
    public boolean readFile(FileOpenMode mode);
    
    /**
     *	write out to a given file
     * @param mode Do you 
     */ 			        
    public boolean readFile(FileOpenMode mode, File file);
}    