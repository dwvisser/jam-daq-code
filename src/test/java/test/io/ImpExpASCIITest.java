package test.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import injection.GuiceInjector;
import jam.data.AbstractHistogram;
import jam.data.Group;
import jam.io.ImpExpASCII;
import jam.io.ImpExpException;
import jam.util.FileUtilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for importing and exporting ASCII feature.
 * @author Dale Visser
 */
public final class ImpExpASCIITest {// NOPMD

    private static final String ASCIITEST = "ASCIItest";

    private static final String DATA = "0 8\n1 2\n2 4";

    private static final int HIST_DIMENSION = 1;

    private static final int HIST_SIZE = 3;

    private static final long HIST_SUM = 14L;

    private static final String HISTNAME = "oneDtest";

    private transient ImpExpASCII impExp;

    private transient File temp1, temp2;

    private void appendDataAndClose(final FileWriter writer)
            throws IOException {
        writer.write(DATA);
        writer.close();
    }

    private void assertCorrectHistogramProperties(final AbstractHistogram hist) {
        assertNotNull(hist, "Expected to read a histogram.");
        assertEquals(HIST_SIZE, hist.getSizeX(), "Expecting a certain number of channels.");
        assertEquals(HIST_DIMENSION, hist.getDimensionality(), "Expected one dimension.");
        assertEquals(HIST_SUM, Math.round(hist.getArea()), "Expected a certain sum.");
    }

    private void readHistDataAndCheck(final File file) throws ImpExpException {
        impExp.openFile(file);
        final String groupName = GuiceInjector.getObjectInstance(
                FileUtilities.class).removeExtensionFileName(file.getName());
        final Group importGroup = jam.data.Warehouse.getGroupCollection().get(
                groupName);
        final AbstractHistogram hist = importGroup.histograms.getList().get(0);
        this.assertCorrectHistogramProperties(hist);
    }

    /**
     * Setup for tests.
     */
    @BeforeEach
    public void setUp() {
        AbstractHistogram.clearList();
        try {
            temp1 = File.createTempFile(ASCIITEST, ".txt");
            final FileWriter writer = new FileWriter(temp1);
            writer.write(HISTNAME);
            writer.append('\n');
            appendDataAndClose(writer);
            temp2 = File.createTempFile(ASCIITEST, ".txt");
            appendDataAndClose(new FileWriter(temp2));
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        }

        impExp = new ImpExpASCII(GuiceInjector.getObjectInstance(JFrame.class));
    }

    /**
     * Tear down for tests.
     */
    @AfterEach
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
