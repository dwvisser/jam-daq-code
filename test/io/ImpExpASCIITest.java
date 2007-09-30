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

	private transient File temp1, temp2;

	private transient ImpExpASCII impExp;

	private static final String histName = "oneDtest";

	@Before
	public void setUp() {
		Histogram.clearList();
		try {
			temp1 = File.createTempFile(ASCIITEST, ".txt");
			temp2 = File.createTempFile(ASCIITEST, ".txt");
			final FileWriter writer = new FileWriter(temp1);
			final FileWriter writer2 = new FileWriter(temp2);
			writer.write(histName);
			writer.append('\n');
			final StringBuilder data = new StringBuilder();
			data.append("0 8\n").append("1 2\n").append("2 4");
			writer.write(data.toString());
			writer2.write(data.toString());
			writer.close();
			writer2.close();
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
			impExp.openFile(temp1);
			String groupName = FileUtilities.getInstance()
					.removeExtensionFileName(temp1.getName());
			Group importGroup = Group.getGroup(groupName);
			Histogram hist = importGroup.getHistogramList().get(0);
			assertNotNull("Hist got read in.", hist);
			assertEquals("Testing # channels correct.", 3, hist.getSizeX());
			assertEquals("1 dimension test.", 1, hist.getDimensionality());
			assertEquals("Sum test.", 14L, Math.round(hist.getArea()));
			impExp.openFile(temp2);
			groupName = FileUtilities.getInstance().removeExtensionFileName(
					temp2.getName());
			importGroup = Group.getGroup(groupName);
			hist = importGroup.getHistogramList().get(0);
			assertNotNull("Hist got read in.", hist);
			assertEquals("Testing # channels correct.", 3, hist.getSizeX());
			assertEquals("1 dimension test.", 1, hist.getDimensionality());
			assertEquals("Sum test.", 14L, Math.round(hist.getArea()));
		} catch (ImpExpException iee) {
			fail(iee.getMessage());
		}
	}

}
