/*
 * Created on Mar 7, 2005
 */
package jam.util;

import java.util.Collection;

/**
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public final class CollectionsUtil {
    
    private static final CollectionsUtil INSTANCE=new CollectionsUtil();
    
    private CollectionsUtil(){
        super();
    }
    
    /**
     * Returns the singleton instance of this class.
     * @return the singleton instance of this class
     */
    public static CollectionsUtil instance(){
        return INSTANCE;
    }
    
    /**
     * Used for conditional add of elements in a collection.
     * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
     */
    public interface Condition<T> {
        
        /**
         * Gives whether to accept an object or not.
         * @param object to decide about
         * @return <code>true</code> if acceptable
         */
        <T> boolean accept(T object);
    }
    
    /**
     * Adds all acceptable objects in one collection to the other collection.
     * 
     * @param source of objects to add
     * @param destination destination to add to
     * @param condition makes decision whether to add an item
     */
    public <T> void addConditional(Collection<T> source, Collection<T> destination,
            Condition<T> condition) {
    	for (T item : source) {
            if (condition.accept(item)){
                destination.add(item);
            }
        }
    }

}
