/*
 */
package jam.fit;
/**
 * Exception that is throw if there is a fit exception that can be
 * handled inside fit
 *
 */
public class FitException extends Exception{

    public FitException(String msg){
	super(msg);
    }
    
}