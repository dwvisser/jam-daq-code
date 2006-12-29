package jam.sort;

/**
 * Constants affecting thread priorities. A guide, taken from 
 * "Concurrent Programming in Java, 2nd Ed.", lists conventional
 * priorities as in the table below. It also notes, however that
 * particular JVM's may use the priorities differently, and may
 * choose to ignore <code>Thread.setPriority()</code> altogether.<p>
 * <table>
 * <tr><td>Range</td><td>Use</td></tr>
 * <tr><td>10</td><td>Crisis management</td></tr>
 * <tr><td>7-9</td><td>Interactive, event-driven</td></tr>
 * <tr><td>4-6</td><td>IO-bound</td></tr>
 * <tr><td>2-3</td><td>Background computation</td></tr>
 * <tr><td>1</td><td>Run only if nothing else can</td></tr>
 * </table>
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Apr 12, 2004
 */
public final class ThreadPriorities {
	
	private ThreadPriorities(){
		super();
	}

	/**
	 * Priority value for <code>jam.sort.NetDaemon</code>. Note that
	 * while this may seem like I/O, the arrival of a data packet
	 * is an important <em>event</em> that must be handled ASAP.
	 */
	public static final int NET = 9;

	/**
	 * Priority value for <code>jam.VMECommunication</code>. Messages
	 * from the VME are also events, but of a lower priority than
	 * data packets.
	 */
	public static final int MESSAGING = 7;

	/**
	 * Priority value for <code>jam.sort.StorageDaemon</code>, a 
	 * relatively high priority thread for writing event data to 
	 * disk.
	 */
	public static final int STORAGE = 6;
	
	/**
	 * Priority value for <code>jam.sort.SortDaemon</code>, which
	 * qualifies as background computation.
	 */
	public static final int SORT = 3;
	
}
