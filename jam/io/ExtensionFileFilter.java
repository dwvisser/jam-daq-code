package jam.io;

import javax.swing.filechooser.*;
import java.io.File;

/**
 * Copied from SimpleFileFilter pages 363-364 in O'Reilly's <it>Java Swing</it>.
 */
public class ExtensionFileFilter extends FileFilter {
    
	String [] extensions;
	String description;
	
	/**
	 * Creates and file filter for a certain extension. The description
	 * is built using the extension.
	 *
	 * @param ext The extension without the period
	 */    
    public ExtensionFileFilter(String ext) {
		this(new String[] {ext}, null);
    }
    
	/**
	 * Creates and file filter for a certain extension and using a specific description. 
	 *
	 * @param ext The extension without the period
	 * @param dexcr A short description of the file type
	 */    
    public ExtensionFileFilter(String ext, String descr) {
		this(new String[] {ext}, descr);
    }
    
	/**
	 * Creates and file filter for a list of extensions and using a specific description. 
	 *
	 * @param ext The extensions without the period
	 * @param dexcr A short description of the file type
	 */    
    public ExtensionFileFilter(String [] exts, String descr){
    	//clone and lowercase the extensions
    	extensions = new String[exts.length];
    	for (int i=exts.length -1; i >= 0;  i--){
    		extensions[i]=exts[i].toLowerCase();
    	}
    	//make sure we have a valid (if simplistic) description
    	description = (descr == null ? "*."+exts[0]+" files" : descr);
    }
    
    public boolean accept(File f){
    	//we always allow directories, regardless of their extension
		if (f.isDirectory()) {return true;}

		// ok, it's a regular file so check the extension
		String name = f.getName().toLowerCase();
		for (int i = extensions.length -1; i >= 0; i--){
			if (name.endsWith(extensions[i])){
				return true;
			}
		}
		return false;
    }
    
    public String getDescription() {
		return description;
    }
    
}