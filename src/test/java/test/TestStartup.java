package test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import injection.GuiceInjector;
import jam.Main;
import jam.ui.ConsoleLog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Dale Visser
 */
public class TestStartup {

  /**
   * @throws Exception if an error occurs
   */
  @BeforeEach
  public void setUp() throws Exception {
    Main.main(null);
  }

  /**
   * @throws Exception if an error occurs
   */
  @AfterEach
  public void tearDown() throws Exception {
    // how do I do this?
    // until there is a nice way besides System.exit(0) to shut down the Jam
    // application, this test can only run in isolation from other tests.
  }

  /** Make sure log has welcome string in it. */
  @Test
  public void testToString() {
    final String welcome = "Welcome to Jam";
    final String logString = GuiceInjector.getObjectInstance(ConsoleLog.class).toString();
    assertTrue(logString.contains(welcome), "Expected log to start with \"" + welcome + "\".");
  }
}
