package jam.global;
import java.util.Observable;
import java.util.logging.Logger;

/**
 * Part of a client server to handle message between packages
 * Broadcast events to all listeners.
 * These listeners must have already registered that they would
 * like to receive events using Broadcaster.addObserver()
 * 
 * @author Ken Swartz
 */
public final class Broadcaster extends Observable {
	
	static private final Broadcaster INSTANCE=new Broadcaster();
	private static final Logger LOGGER = Logger.getLogger(JamProperties.class
			.getPackage().getName());
	private static final JamStatus STATUS = JamStatus.getSingletonInstance();
	
	/**
	 * Return the unique instance of this class.
	 * 
	 * @return singleton instance of this class
	 */
	static public Broadcaster getSingletonInstance(){
		return INSTANCE;
	}
	
	private Broadcaster(){
		super();
	}

    /** 
     * Broadcast an event to all registered observers. The necessary
     * synchronization is already taken care of by 
     * <code>Observable.notifyObservers(Object)</code>.
     * 
     * @param command an <CODE>int</CODE> from <CODE>BroadcastEvent</CODE>
     * @param param a parameter to be wrapped in the <CODE>BroadcastEvent</CODE>
     * object
     */
    public void broadcast(final BroadcastEvent.Command command, final Object param) {
        final Object bEvent=new BroadcastEvent(command, param);
        setChanged();//necessary for next line to work
        notifyObservers(bEvent);//automatically calls clearChanged()
    }

    /** 
     * Broadcast an event to all registered observers. Calls
     * <code>broadcast(int, null)</code>.
     * 
     * @param command code from <CODE>BroadcastEvent</CODE>, I presume
     */
    public void broadcast(final BroadcastEvent.Command command) {
        broadcast(command,null);
    }  
    

	/**
	 * Does the scaler zeroing.
	 */
	public static void zeroScalers() {
		if (STATUS.isOnline()) {
			INSTANCE.broadcast(BroadcastEvent.Command.SCALERS_CLEAR);
			INSTANCE.broadcast(BroadcastEvent.Command.SCALERS_READ);

		} else {
			LOGGER.severe("Can only Zero Scalers when in Online mode.");
		}
	}
}