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
public class HDFileFilter extends FileFilter implements HDFconstants, 
java.io.FileFilter {
    
    final boolean option;
    
    /**
     * Constructs a filter for HDF files. It checks the first 4 bytes of the file.
     * 
     * @param showDirectories whether to show directories as well
     * @see HDFconstants#HDF_HEADER
     */
    public HDFileFilter(boolean showDirectories){
    	super();
		option=showDirectories;
    }
    
    public boolean accept(File f){
    	boolean rval=false;//default return value
		if (f.isDirectory()) {
			rval = option;
		} else {
			try {
	    		RandomAccessFile raf=new RandomAccessFile(f,"r");
	    		int temp=raf.readInt();
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