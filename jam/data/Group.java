
package jam.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A group of histograms,
 * A node in the tree
 *
 */
public class Group {

	/** List of all groups */
	private final static List LIST = new ArrayList();
	/** Map of all groups using name*/	
	private final static Map NAME_MAP = new TreeMap();
	/** The current active group for creating histograms*/
	private static Group currentGroup;
	/** children of group */
	private final List children = new ArrayList();	
	private String name; 

	/** Create a new group */
	/*
	public static void createGroup(String name){
		Group group=new Group(name);
		currentGroup=group;
	}
	*/
	/** Set a group as the current group, create the
	 * group if it does not already exist
	 * @param groupName
	 */
	public static void setCurrentGroup(String groupName){
		if ( NAME_MAP.containsKey(groupName) ) {
			currentGroup=(Group)( NAME_MAP.get(groupName) );
		} else {
			Group group=new Group(groupName);
			currentGroup=group;
		}
	}	
	
	public static Group getGroup(String name){
		return (Group)( NAME_MAP.get(name) );
	}
	public static void setCurrentGroup(Group group){
		currentGroup=group;
	}
	public static Group getCurrentGroup(){
		return currentGroup;
	}
	public static List getGroupList(){
		return LIST;
	}
	/** Clear all groups */
	public static void clear(){
		NAME_MAP.clear();
		LIST.clear();
		currentGroup=null;
	}
	/**
	 * Constructor
	 * @param name
	 */
	public Group(String name){
		this.name=name;
		
		LIST.add(this);
		NAME_MAP.put(name, this);
	}
 

	public String getName(){
		return name;
	}
	
	public List getChildren(){
		return children;
	}

	public void add(Object object){
		children.add(object);
	}
	public String toString(){
		return name;
	}
}