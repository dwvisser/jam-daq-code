package jam.sort.stream;
/**
 * Exception that is thrown if we have a unrecoverable error in handling an event stream.
 *
 * @author Ken Swartz
 * @version 0.5
 * @since JDK 1.1
 */
public class EventException extends Exception{

    /**
     * Constructor with an output message.
     *
     * @param msg the message to be output to the user
     */
    public EventException(String msg){
	super(msg);
    }
}