package test.io.hdf;

import injection.GuiceInjector;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;
import jam.script.Session;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests reading HDF files.
 * @author Dale Visser
 */
public class HDFIOTest {

    private static final String SAMPLE_HDF = "sampledata/exampleGates1.hdf";

    /**
     * Tests opening an existing file.
     */
    @Test
    public void testReadFileFileOpenModeFile() {
        final Session session = GuiceInjector.getObjectInstance(Session.class);
        try {
            final File file = session.defineFile(SAMPLE_HDF);
            final HDFIO hdfio = GuiceInjector.getObjectInstance(HDFIO.class);
            Assert.assertTrue("Expected readFile success = true.",
                    hdfio.readFile(FileOpenMode.OPEN, file));
        } catch (RuntimeException e) {
            Assert.fail(e.getMessage());
        }
    }

}
