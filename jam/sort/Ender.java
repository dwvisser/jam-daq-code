package jam.sort;

/**
 * Interface for sort routines to implement if the user wants
 * a block of code to be executed after a run ends.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 * @version 11 October 2004
 */
public interface Ender {
    /**
     * Code to run after an online acquisition run ends.
     *
     */
	void end();
}
