package test.io.hdf;

import injection.GuiceInjector;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.script.Session;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests reading HDF files.
 *
 * @author Dale Visser
 */
public class HDFIOTest {

  private static final String SAMPLE_HDF = "sampledata/exampleGates1.hdf";

  /** Tests opening an existing file. */
  @Test
  public void testReadFileFileOpenModeFile() {
    final Session session = GuiceInjector.getObjectInstance(Session.class);
    try {
      final File file = session.defineFile(SAMPLE_HDF);
      final HDFIO hdfio = GuiceInjector.getObjectInstance(HDFIO.class);
      Assertions.assertTrue(
          hdfio.readFile(FileOpenMode.OPEN, file), "Expected readFile success = true.");
    } catch (RuntimeException e) {
      Assertions.fail(e.getMessage());
    }
  }
}
