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
		if (f.isDirectory()) return option;
		try {
	    	RandomAccessFile raf=new RandomAccessFile(f,"r");
	    	int temp=raf.readInt();
	    	raf.close();
	   	 	return (temp == HDF_HEADER);
		} catch (IOException e) {
	    	return false;
		}
    }
    
    public String getDescription() {
		return "Heirarchical Data  Format v4.1r2";
    }
    
}