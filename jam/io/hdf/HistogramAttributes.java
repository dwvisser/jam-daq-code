/*
 * Created on Mar 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jam.io.hdf;


/**
 * Class to hold histogram properties while we decide if we should load
 * them.
 */
public class HistogramAttributes {

    HistogramAttributes(String groupName, String name, String title, int number) {        	
        super();
        this.groupName=groupName;
        this.name=name;
        this.title=title;
        this.number=number;
        
    }
	
    HistogramAttributes() {
        super();
    }

    /**
     * @return the group name of the histogram
     */
    public String getGroupName() {
        return groupName;
    }
    
    /**
     * @return the name of the histogram
     */
    public String getName() {
        return name;
    }

    /**
     * Full name is <groupName>/<histName.
     * @return the full name of the histogram
     * 
     */
    public String getFullName() {
    	String fullName;
    	
    	if ( !groupName.equals("") )
			fullName=groupName+"/"+name;
    	else
    		fullName=name;
    	
        return fullName;
    }
    
    String getTitle() {
    	return name;        	
    }
    
    String groupName;
    
    String name;

    String title;

    int number;

    int sizeX;

    int sizeY;

    int histDim;

    byte histNumType;

    Object dataArray; //generic data array

    Object errorArray;        
            
}