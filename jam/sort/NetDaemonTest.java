package jam.sort;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jam.sort.control.RunControl;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit test case for testing NetDaemon behavior.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 * @version 2004-10-27
 */
public final class NetDaemonTest{//NOPMD

	private transient NetDaemon netDaemon;

	@Before 
	public void setUp() {//NOPMD
		final RunControl runControl = RunControl.getSingletonInstance();
		try{
			netDaemon = new NetDaemon(null, null, "localhost", 8080);
			runControl.setupOn("Test", null, null, null, netDaemon, null);
		} catch (SortException se) {//NOPMD
			fail(se.getMessage());
		}
	}

	@Test
	@Ignore
	public void testSetEmptyBefore() {
		assertNotNull("Wasn't able to initialize netDaemon.", netDaemon);
		netDaemon.setEmptyBefore(true);
		assertTrue("isAssertTrue() should have returned true.", netDaemon
				.isEmptyBefore());
	}
	
	@After
	public void tearDown(){
		if (netDaemon != null) {
			netDaemon.closeNet();
		}
	}
}
