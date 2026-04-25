package test.script;

import static org.junit.jupiter.api.Assertions.assertThrows;

import injection.GuiceInjector;
import jam.script.Session;
import java.io.File;
import org.junit.jupiter.api.Test;

/** Tests security checks in scripting session file handling. */
public class SessionTest {

  @Test
  public void testDefineFileRejectsPathTraversal() {
    final Session session = GuiceInjector.getObjectInstance(Session.class);
    assertThrows(
        IllegalArgumentException.class, () -> session.defineFile("../outside-directory/evil.evn"));
  }

  @Test
  public void testDefineFileAllowsRelativePathInsideBase() {
    final Session session = GuiceInjector.getObjectInstance(Session.class);
    final File file = session.defineFile("sampledata/example.evn");
    assert file.getPath().contains("sampledata")
        : "Expected relative path to resolve within the base directory";
  }
}
