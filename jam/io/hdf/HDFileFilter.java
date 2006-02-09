package jam.io.hdf;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.swing.filechooser.FileFilter;

/**
 * Filters only HDF files for file dialogs.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 */
public class HDFileFilter extends FileFilter implements Constants, 
java.io.FileFilter {
    
    private final transient boolean option;
    
    /**
     * Constructs a filter for HDF files. It checks the first 4 bytes of the file.
     * 
     * @param showDir whether to show directories as well
     * @see Constants#HDF_HEADER
     */
    public HDFileFilter(boolean showDir){
    	super();
		option=showDir;
    }
    
    public boolean accept(final File file){
    	boolean rval=false;//default return value
		if (file.isDirectory()) {
			rval = option;
		} else {
			try {
	    		final RandomAccessFile raf=new RandomAccessFile(file,"r");
	    		final int temp=raf.readInt();
	    		raf.close();
	   	 		rval = (temp == HDF_HEADER);
			} catch (IOException e) {
	    		rval = false;
			}
		}
		return rval;
    }
    
    public String getDescription() {
		return "Hierarchical Data  Format v4.1r2";
    }
    
}