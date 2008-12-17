package test;

import static org.junit.Assert.assertTrue;
import injection.GuiceInjector;
import jam.Main;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Dale Visser
 * 
 */
public class TestStartup {

	/**
	 * @throws Exception
	 *             if an error occurs
	 */
	@Before
	public void setUp() throws Exception {
		Main.main(null);
	}

	/**
	 * @throws Exception
	 *             if an error occurs
	 */
	@After
	public void tearDown() throws Exception {
		// how do I do this?
		// until there is a nice way besides System.exit(0) to shut down the Jam
		// application, this test can only run in isolation from other tests.
	}

	/**
	 * Make sure log has welcome string in it.
	 */
	@Test
	public void testToString() {
		final String welcome = "Welcome to Jam";
		final String logString = GuiceInjector.getConsoleLog().toString();
		assertTrue("Expected log to start with \"" + welcome + "\".", logString
				.contains(welcome));
	}
}
