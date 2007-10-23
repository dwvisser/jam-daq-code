/*
 * Created on Oct 11, 2004
 */
package jam.sort;

/**
 * Interface for sort routines to implement if the user wants
 * a block of code to be executed before a run begins.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public interface Beginner {
    
    /**
     * Called before online run begins.
     */
	void begin();
}
