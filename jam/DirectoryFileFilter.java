package jam;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A way for FileDialogs to select only directories.
 * 
 * @author <a href="mailto:dale@visser.name>Dale Visser</a>
 * @version 1.3
 */
public class DirectoryFileFilter extends FileFilter {
	
    /**
     * @return true if the given file is a directory
     * @param f file to check
     */
     public boolean accept(File file){
		return file.isDirectory();
	}
    
    /**
     * @return description of file type, displayed in file chooser
     */
     public String getDescription() {
    	final String desc="Directories";
		return desc;
    }
    
}