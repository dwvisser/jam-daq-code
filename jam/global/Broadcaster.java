package jam.global;
import java.util.*;

/**
 * Part of a client server to handle message between packages
 * Broadcast events to all listeners.
 * These listeners must have already registered that they would
 * like to receive events using Observer.addObserver()
 * 
 * @author Ken Swartz
 */
public class Broadcaster extends Observable {

    /** 
     * Broadcast an event to all registered observers. The necessary
     * synchronization is already taken care of by 
     * <code>Observable.notifyObservers(Object)</code>.
     * 
     * @param command an <CODE>int</CODE> from <CODE>BroadcastEvent</CODE>
     * @param param a parameter to be wrapped in the <CODE>BroadcastEvent</CODE>
     * object
     */
    public void broadcast(int command, Object param) 
    throws GlobalException {
        Object broadcastEvent=new BroadcastEvent(command, param);
        setChanged();//necessary for next line to work
        notifyObservers(broadcastEvent);//automatically calls clearChanged()
    }

    /** 
     * Broadcast an event to all registered observers. Calls
     * <code>broadcast(int, null)</code>.
     * 
     * @param command code from <CODE>BroadcastEvent</CODE>, I presume
     */
    public void broadcast(int command) throws GlobalException {
        broadcast(command,null);
    }    
}