package jam.global;
/**
 * This interface handles messages that are to be output to the screen.
 * This usually on the jam.JamConsole, but could be
 * MessageAdaptor if the full Jam is not started up.
 *
 * @author Ken Swartz
 */
public interface MessageHandler {

    final static int NEW=0;			//new partial message
    final static int CONTINUE=1;		//continue partical message
    final static int END=9;			//end partial message

    /**
     * Output a message of many parts and tell which part.
     *
     * @param message the message to be printed
     * @param part one of NEW, CONTINUE, or END
     */
    public void messageOut(String message, int part);

    /**
     * Output the middle part of a message of many parts
     */
    public void messageOut(String message);

    /**
     * Output a full message
     */
    public void messageOutln(String message);

    /**
     * Output a waring message
     */
    public void warningOutln(String message);

    /**
     * Output a error message
     */
    public void errorOutln(String message);
}