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
    int NEW=0;
    
    /**
     * continue partical message
     */
    int CONTINUE=1;
    
    /**
     * end partial message
     */
    int END=9;

    /**
     * Output a message of many parts and tell which part.
     *
     * @param message the message to be printed
     * @param part one of NEW, CONTINUE, or END
     */
    void messageOut(String message, int part);

    /**
     * Output the middle part of a message of many parts
     * 
     * @param message to print
     */
    void messageOut(String message);

    /**
     * Output a full message.
     * 
     * @param message to print
     */
    void messageOutln(String message);
    
    /**
     * Output carriage return.
     */
    void messageOutln();

    /**
     * Output a warning message.
     * 
     * @param message warning
     */
    void warningOutln(String message);

    /**
     * Output an error message.
     * 
     * @param message to print
     */
    void errorOutln(String message);
}