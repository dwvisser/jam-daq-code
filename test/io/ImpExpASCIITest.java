package test.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import jam.data.Group;
import jam.data.Histogram;
import jam.io.ImpExpASCII;
import jam.io.ImpExpException;
import jam.util.FileUtilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
	private void assertCorrectHistogramProperties(final Histogram hist) {
		assertNotNull("Expected to read a histogram.", hist);
		assertEquals("Expecting a certain number of channels.", HIST_SIZE, hist.getSizeX());
		assertEquals("Expected one dimension.", HIST_DIMENSION, hist.getDimensionality());
		assertEquals("Expected a certain sum.", HIST_SUM, Math.round(hist.getArea()));
	}

	private void readHistDataAndCheck(final File file) throws ImpExpException{
		impExp.openFile(file);
		final String groupName = FileUtilities.getInstance()
				.removeExtensionFileName(file.getName());
		final Group importGroup = Group.getGroup(groupName);
		final Histogram hist = importGroup.getHistogramList().get(0);
		this.assertCorrectHistogramProperties(hist);
	}

	@Before
	public void setUp() {
		Histogram.clearList();
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
	
	@After
	public void tearDown() {
		Histogram.clearList();
	}

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
