package jam.data;

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
    /** List of all groups */
    private final static List LIST = new ArrayList();

    /** Map of all groups using name */
    private final static Map NAME_MAP = new HashMap();

    /** The current active group for creating histograms */
    private static Group currentGroup;
    
    /** The sort group, group with sort histogram */
    private static Group sortGroup;

    /** children histograms of group */
    private final List histList = new ArrayList();

    /** children of group */
    private final Map histogramMap = new HashMap();

    /** children scalers of group */
    private final List scalerList = new ArrayList();
    
    private String name;

    private Type type;

    /**
     * Set a group as the current group, create the group if it does not already
     * exist
     * 
     * @param groupName
     *            name of the group
     * @param type
     *            of group
     */
    public synchronized static void createGroup(String groupName, Type type) {
        if (NAME_MAP.containsKey(groupName)) {
            setCurrentGroup(groupName);
        } else {
            final Group group = new Group(groupName, type);
            setCurrentGroup(group);
        }
        /* Only one sort group */
        if (type.type==Group.Type.TYPE_SORT) {
        	sortGroup =currentGroup;
        }
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
     * Set a group as the current group, create the group if it does not already
     * exist
     * 
     * @param groupName
     */
    public synchronized static void setCurrentGroup(String groupName) {
        if (NAME_MAP.containsKey(groupName)) {
            currentGroup = (Group) (NAME_MAP.get(groupName));
        }
    }
    /**
     * Sets the "current" group.
     * 
     * @param group
     *            to be "current"
     */
    public synchronized static void setCurrentGroup(Group group) {
        currentGroup = group;
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
     * Get the current group.
     * 
     * @return the current group
     */
    public synchronized static Group getCurrentGroup() {
        return currentGroup;
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
        /* Cast needed because of overloaded method. */
        createGroup(WORKING_NAME,Group.Type.TEMP);
        setCurrentGroup(WORKING_NAME);
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
     * @param name
     *            of the group
     * @param type
     *            the type of group
     */
    public Group(String name, Type type) {
        this.name = name;
        this.type = type;
        LIST.add(this);
        NAME_MAP.put(name, this);
    }
    /**
     * Set the name, used to rename group
     * @param name
     * 			the new name of a group
     */
    public void setName(String name) {
    	NAME_MAP.remove(this.getName());
    	NAME_MAP.put(name, this);
    	this.name = name;
    }
    /**
     * @return the name of this group
     */
    public String getName() {
        return name;
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
        return name;
    }
}