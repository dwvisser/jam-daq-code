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

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * JUnit test.
	 * 
	 */
	public void testSetEmptyBefore() {
		netDaemon.setEmptyBefore(true);
	}
}
