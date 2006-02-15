package jam.io;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Copied from SimpleFileFilter pages 363-364 in O'Reilly's <it>Java Swing</it>.
 */
public class ExtensionFileFilter extends FileFilter {
    
	private transient final String description;
	private transient final String [] extensions;
	
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
	 * @param descr A short description of the file type
	 */    
    public ExtensionFileFilter(String ext, String descr) {
		this(new String[] {ext}, descr);
    }
    
    /**
	 * Creates and file filter for a list of extensions and using a specific description. 
	 *
	 * @param exts The extensions without the period
	 * @param descr A short description of the file type
	 */    
    public ExtensionFileFilter(final String [] exts, String descr){
    	super();
    	//clone and lowercase the extensions
    	final int len=exts.length;
    	extensions = new String[len];
    	for (int i=len-1; i >= 0;  i--){
    		extensions[i]=exts[i].toLowerCase();
    	}
    	final StringBuffer buffer=(descr==null) ? new StringBuffer() :
    	new StringBuffer(descr);
    	buffer.append(" (");
		for (int i=0; i < len;  i++){
			buffer.append("*.").append(extensions[i]);
			if (i == (len-1)){
				buffer.append(')');
			} else {
				buffer.append(", ");
			}
		}
		description=buffer.toString();
    }
    
	public boolean accept(final File file){
    	boolean rval=false; //default return value
    	/* we always allow directories, regardless 
    	 * of their extension */
		if (file.isDirectory()) {
			rval = true;
		}
		/* ok, it's a regular file so check the extension */
		final String name = file.getName().toLowerCase();
		for (int i = extensions.length -1; i >= 0; i--){
			if (name.endsWith(extensions[i])){
				rval = true;
				break;
			}
		}
		return rval;
    }
    
    public String getDescription() {
		return description;
    }
    
    /**
     * Get the i'th extension.
     * @param index index of extension
     * @return three character file extension
     */
    public String getExtension(final int index){
    	return extensions[index];
    }
    
}