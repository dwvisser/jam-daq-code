package jam.io.hdf;

import javax.swing.filechooser.FileFilter;
import java.io.*;

/**
 * Filters only HDF files for file dialogs.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 */
public class HDFileFilter extends FileFilter implements HDFconstants {
    
    boolean option=true;
    
    public HDFileFilter(boolean showDirectories){
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
		return "Heirarchical Data  Format v4.1r2";
    }
    
}