package test.io.hdf;

import injection.GuiceInjector;
import jam.io.FileOpenMode;
import jam.io.hdf.HDFIO;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * Tests reading HDF files.
 * 
 * @author Dale Visser
 */
public class HDFIOTest extends TestCase {

	private static final String SAMPLE_HDF = "sampledata/exampleGates.hdf";

	/**
	 * Default public constructor needed in JUnit suites.
	 */
	public HDFIOTest() {// NOPMD
		super();
	}

	/**
	 * Tests opening an existing file.
	 */
	@Test
	public void testReadFileFileOpenModeFile() {
		final ClassLoader loader = Thread.currentThread()
				.getContextClassLoader();
		final URL url = loader.getResource(SAMPLE_HDF);
		URI uri = null;
		try {
			uri = url.toURI();
			final File file = new File(uri);
			final HDFIO hdfio = GuiceInjector.getHDFIO();
			hdfio.readFile(FileOpenMode.OPEN, file);
		} catch (URISyntaxException e) {
			fail(e.getMessage());
		}
	}

}
