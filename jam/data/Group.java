package jam.data;

import jam.util.StringUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * A group of histograms, A node in the tree
 *  
 */
public class Group {

    /**
     * Abstraction of Group type.
     * 
     * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
     */
    public static class Type {
    	
        final private static int TYPE_FILE = 1;

        final private static int TYPE_SORT = 2;

        final private static int TYPE_TEMP = 3;        
        final int type;

        private Type(int kind) {
            type = kind;
        }

        /**
         * Group that comes from a file.
         */
        static public final Type FILE = new Type(TYPE_FILE);

        /**
         * Group that comes from a sort routine.
         */
        static public final Type SORT = new Type(TYPE_SORT);
        /**
         * Group that is tempory until save
         */
        
        static public final Type TEMP = new Type(TYPE_TEMP);
    }

    /** Working group name */
    public final static String WORKING_NAME = "Working";
    /** Default group name */
    public final static String DEFAULT_NAME = "Default";
    
    /** List of all groups */
    private final static List LIST = new ArrayList();

    /** Map of all groups using name */
    private final static Map NAME_MAP = new HashMap();
    
    /** The sort group, group with sort histogram */
    private static Group sortGroup;

    /** children histograms of group */
    private final List histList = new ArrayList();

    /** children of group */
    private final Map histogramMap = new HashMap();

    /** children scalers of group */
    private final List scalerList = new ArrayList();
    
    /** Origonal group name */
    private String groupName;
    
    /** Name of file that group belongs to */
    private String fileName;
    
    /** Name of file and group canotated */
    private String fullName;
    /** Type of group, file or sort */
    private Type type;
    
    /**
     * Set a group as the current group, create the group if it does not already
     * exist
     * 
     * @param groupName
     *            name of the group
     * @param type
     *            of group
     * @param fileName name of the file that this group belongs to
     * @return the created <code>Group</code> object
     */
    public synchronized static Group createGroup(String groupName,
            String fileName, Type type) {
        final Group group = new Group(groupName, type, fileName);
        /* Only one sort group */
        if (type.type == Group.Type.TYPE_SORT) {
            sortGroup = group;
        }
        return group;
    }

    /**
     * Set a group as the current group, create the group if it does not already
     * exist
     * 
     * @param groupName
     *            name of the group
     * @param type
     *            of group
     * @return the created <code>Group</code> object
     */
    public synchronized static Group createGroup(String groupName, Type type) {
    	return Group.createGroup(groupName, null, type);
    }
    
    /**
     * Clear a group, removes it
     * @param group
     */
    public static void clearGroup(Group group){
    	NAME_MAP.remove(group);
    	LIST.remove(group);
    }

    /**
     * Returns the group with the given name.
     * 
     * @param name
     *            of group
     * @return the group
     */
    public static Group getGroup(String name) {
        return (Group) (NAME_MAP.get(name));
    }
    /**
     * Returns the group that is the sort group
     * 
     * @return the sort group
     */
    public synchronized static Group getSortGroup() {
        return sortGroup;
    }


    /**
     * Gets a list of all groups.
     * 
     * @return list of all groups
     */
    public static List getGroupList() {
        return Collections.unmodifiableList(LIST);
    }

    /**
     * Gets a map of groups keyed by name.
     * 
     * @return map of groups keyed by name
     */
    public static Map getGroupMap() {
        return Collections.unmodifiableMap(NAME_MAP);
    }
    /** 
     * Clear all groups 
     */
    public static synchronized void clearList() {
        NAME_MAP.clear();
        LIST.clear();
        sortGroup=null;
    }

	/**
	 *  Test if Group is a valid Group
	 * @param group the group to test
	 * @return <code>true</code> if this group remains in the name mapping
	 */
	public static boolean isValid(Group group){
		return NAME_MAP.containsValue(group);
	}
    
    /**
     * Constructor
     * 
     * @param groupName
     *            name of the group
     * @param type
     *            the type of group
     * @param fileName name of file this group is associated with
     */
    private Group(String groupName, Type type, String fileName) {
        final StringUtilities stringUtil = StringUtilities.instance();
        String tempFullName = "GROUP";
        if (fileName != null && groupName != null) {
            tempFullName = fileName + "/" + groupName;
        } else if (fileName != null) {
            tempFullName = fileName;
        } else if (groupName != null) {
            tempFullName = groupName;
        }
        String uniqueName = stringUtil.makeUniqueName(tempFullName, NAME_MAP
                .keySet());
        this.type = type;
        this.groupName = groupName;
        this.fileName = fileName;
        this.fullName = uniqueName;
        LIST.add(this);
        NAME_MAP.put(uniqueName, this);
    }
    
    /**
     * Set the name, used to rename group
     * @param name
     * 			the new name of a group
     */
    public void setName(String name) {
    	NAME_MAP.remove(this.getName());
    	NAME_MAP.put(name, this);
    	this.fullName = name;
    }
    /**
     * @return the name of this group
     */
    public String getName() {
    	return fullName;
    }
    /**
     * @return the group name part of the full name
     */
    public String getGroupName() {
    	return groupName;
    }
    /**
     * @return the file name part of this group
     */
    public String getFileName() {
    	return fileName;
    }
    
    /**
     * @return the type of this group
     */
    public Type getType() {
        return type;
    }    
    /**
     * Add a histogram to the group
     *  
     * @param hist
     */
    public void addHistogram(Histogram hist) {
        histList.add(hist);
        histogramMap.put(hist.getName(), hist);
    }
    /**
     * Remove a histogram from the group
     * 
     * @param hist
     */
    public void removeHistogram(Histogram hist) {
        histList.remove(hist);
        histogramMap.remove(hist.getName());
    }
    /**
     * Retreive a histogram given its name
     * @param name the histogram name
     * @return the histogram
     */
    public Histogram getHistogram(String name) {
    	return (Histogram)histogramMap.get(name);
    }
    /**
     * @return list of histograms in this group
     */
    public List getHistogramList() {
        return Collections.unmodifiableList(histList);
    }
    
    /**
     * Returns whether this group has histograms in it.
     * @return whether this group has histograms in it
     */
    public boolean hasHistograms(){
        return !histList.isEmpty();
    }

    /**
     * @return map of histograms in this group keyed by name
     */
    public Map getHistogramMap() {
        return Collections.unmodifiableMap(histogramMap);
    }

    /**
     * Add a scaler to the stored list of scalers.
     * 
     * @param scaler to add
     */
    public void addScaler(Scaler scaler) {
    	 scalerList.add(scaler);    	    	
    }
    
    /**
     * Returns a view of the list of scalers.
     * @return a view of the list of scalers
     */
    public List getScalerList() {
        return Collections.unmodifiableList(scalerList);
    }
    
    public String toString() {
        return fullName;
    }
}