package jam;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A way for FileDialogs to select only directories.
 * 
 * @author <a href="mailto:dale@visser.name>Dale Visser</a>
 */
public class DirectoryFileFilter extends FileFilter {
    
    public boolean accept(File f){
		if (f.isDirectory()) return true;
	   	return false;
	}
    
    public String getDescription() {
		return "Directories";
    }
    
}