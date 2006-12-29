package jam.sort;

import jam.sort.control.RunControl;
import junit.framework.TestCase;

/**
 * JUnit test case for testing NetDaemon behavior.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 * @version 2004-10-27
 */
public class NetDaemonTest extends TestCase {

	/**
	 * Default constructor.
	 */
	public NetDaemonTest() {
		super(NetDaemon.class.getName() + " tests");
	}

	private transient NetDaemon netDaemon;

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		final RunControl runControl = RunControl.getSingletonInstance();
		netDaemon = new NetDaemon(null, null, "localhost", 8080);
		runControl.setupOn("Test", null, null, null, netDaemon, null);
	}

	/**
	 * JUnit test.
	 * 
	 */
	public void testSetEmptyBefore() {
		netDaemon.setEmptyBefore(true);
		assertTrue("isAssertTrue() should have returned true.", netDaemon
				.isEmptyBefore());
	}
}
