package test.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import jam.data.AbstractHistogram;
import jam.data.Group;
import jam.io.ImpExpASCII;
import jam.io.ImpExpException;
import jam.util.FileUtilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for importing and exporting ASCII feature.
 * 
 * @author Dale Visser
 * 
 */
public final class ImpExpASCIITest {// NOPMD

	private static final String ASCIITEST = "ASCIItest";

	private static final String DATA = "0 8\n1 2\n2 4";

	private static final int HIST_DIMENSION = 1;

	private static final int HIST_SIZE = 3;

	private static final long HIST_SUM = 14L;

	private static final String histName = "oneDtest";

	private transient ImpExpASCII impExp;

	private transient File temp1, temp2;

	/**
	 * @param writer
	 * @throws IOException
	 */
	private void appendDataAndClose(final FileWriter writer) throws IOException {
		writer.write(DATA);
		writer.close();
	}

	/**
	 * @param hist
	 */
	private void assertCorrectHistogramProperties(final AbstractHistogram hist) {
		assertNotNull("Expected to read a histogram.", hist);
		assertEquals("Expecting a certain number of channels.", HIST_SIZE, hist
				.getSizeX());
		assertEquals("Expected one dimension.", HIST_DIMENSION, hist
				.getDimensionality());
		assertEquals("Expected a certain sum.", HIST_SUM, Math.round(hist
				.getArea()));
	}

	private void readHistDataAndCheck(final File file) throws ImpExpException {
		impExp.openFile(file);
		final String groupName = FileUtilities.getInstance()
				.removeExtensionFileName(file.getName());
		final Group importGroup = jam.data.Warehouse.getGroupCollection().get(
				groupName);
		final AbstractHistogram hist = importGroup.histograms.getList().get(0);
		this.assertCorrectHistogramProperties(hist);
	}

	/**
	 * Setup for tests.
	 */
	@Before
	public void setUp() {
		AbstractHistogram.clearList();
		try {
			temp1 = File.createTempFile(ASCIITEST, ".txt");
			final FileWriter writer = new FileWriter(temp1);
			writer.write(histName);
			writer.append('\n');
			appendDataAndClose(writer);
			temp2 = File.createTempFile(ASCIITEST, ".txt");
			appendDataAndClose(new FileWriter(temp2));
		} catch (IOException ioe) {
			fail(ioe.getMessage());
		}
		impExp = new ImpExpASCII();
	}

	/**
	 * Tear down for tests.
	 */
	@After
	public void tearDown() {
		AbstractHistogram.clearList();
	}

	/**
	 * Test reading data from files.
	 */
	@Test
	public void testReadData() {
		try {
			readHistDataAndCheck(temp1);
			readHistDataAndCheck(temp2);
		} catch (ImpExpException iee) {
			fail(iee.getMessage());
		}
	}

}
