package jam.global;
/**
 * This interface handles messages that are to be output to the screen.
 * This usually on the jam.JamConsole, but could be
 * MessageAdaptor if the full Jam is not started up.
 *
 * @author Ken Swartz
 */
public interface MessageHandler {

    /**
     * new partial message
     */
    final static int NEW=0;
    
    /**
     * continue partical message
     */
    final static int CONTINUE=1;
    
    /**
     * end partial message
     */
    final static int END=9;

    /**
     * Output a message of many parts and tell which part.
     *
     * @param message the message to be printed
     * @param part one of NEW, CONTINUE, or END
     */
    public void messageOut(String message, int part);

    /**
     * Output the middle part of a message of many parts
     * 
     * @param message to print
     */
    public void messageOut(String message);

    /**
     * Output a full message.
     * 
     * @param message to print
     */
    public void messageOutln(String message);
    
    /**
     * Output carriage return.
     */
    public void messageOutln();

    /**
     * Output a warning message.
     * 
     * @param message warning
     */
    public void warningOutln(String message);

    /**
     * Output an error message.
     * 
     * @param message to print
     */
    public void errorOutln(String message);
}