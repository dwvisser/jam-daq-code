/*
 * Created on Mar 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jam.io.hdf;

import java.util.HashMap;
import java.util.Map;


/**
 * Class to hold histogram properties while we decide if we should load
 * them.
 */
public class HistogramAttributes {
	
	static Map mapFullNames = new HashMap();

	public static void clear() {
		mapFullNames.clear();		
	}
	
	public static HistogramAttributes getHistogramAttribute(String fullName){
		return (HistogramAttributes)mapFullNames.get(fullName);
	}
	
    private String groupName;
    
    private String name;

    private String fullName;
    
    private String title;

    private int number;

    private int sizeX;

    private int sizeY;

    private int histDim;

    private byte histNumType;
 	
    HistogramAttributes(String groupName, String name, String title, int number) {        	
        super();

        this.groupName=groupName;
        this.name=name;
        this.title=title;
        this.number=number;
        fullName=createFullName(groupName, name);
        
        mapFullNames.put(fullName, this);        
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
     * @return the full name of the group/histogram
     */
    public String getFullName() {
        return fullName;
    }
    /**
     * Title of histogram
     */
    String getTitle() {
    	return name;        	
    }

    /**
     * Full name is <groupName>/<histName.
     * @return the full name of the histogram
     * 
     */
    public String createFullName(String groupNameIn, String nameIn) {
    	String tempFullName;
    	
    	if (groupName!=null) { 
    		if(!groupName.equals("") )
    			tempFullName=groupNameIn+"/"+nameIn;
    		else
    			tempFullName=nameIn;
    	}else {
    		tempFullName=nameIn;
    	}
    	
        return tempFullName;
    }
    
    
           
}