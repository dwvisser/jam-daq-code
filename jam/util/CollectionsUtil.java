/*
 * Created on Mar 7, 2005
 */
package jam.util;

import java.util.Collection;
import java.util.Iterator;

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
    public interface Condition{
        
        /**
         * Gives whether to accept an object or not.
         * @param object to decide about
         * @return <code>true</code> if acceptable
         */
        boolean accept(Object object);
    }
    
    /**
     * Adds all acceptable objects in one collection to the other collection.
     * 
     * @param source of objects to add
     * @param destination destination to add to
     * @param condition makes decision whether to add an item
     */
    public void addConditional(Collection source, Collection destination,
            Condition condition) {
        final Iterator iterator=source.iterator();
        while (iterator.hasNext()){
            final Object object = iterator.next();
            if (condition.accept(object)){
                destination.add(object);
            }
        }
    }

}
