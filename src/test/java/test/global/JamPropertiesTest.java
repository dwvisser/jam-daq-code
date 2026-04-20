package test.global;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jam.global.JamProperties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for property file parsing.
 *
 * @author Dale Visser
 */
public final class JamPropertiesTest {// NOPMD

    private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    private static final String FILENAME = "JamConfig.ini";

    private static final String TEST_PROPERTY = "test.prop";

    private static final String TEST_VALUE = "testing";

    /**
     * Setup for tests.
     */
    @BeforeEach
    public void setUp() {
        final String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
        final File out = new File(tmpDir, FILENAME);
        out.deleteOnExit();
        System.setProperty("jam.home", tmpDir);
        final Properties properties = new Properties();
        properties.put(TEST_PROPERTY, TEST_VALUE);
        try (FileOutputStream output = new FileOutputStream(out)) {
            properties.store(output, "JamProperties Test");
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());// NOPMD
        }
    }

    /**
     * The actual test.
     */
    @Test
    public void test() {
        new JamProperties();
        assertEquals(TEST_VALUE, JamProperties.getPropString(TEST_PROPERTY),
                "test property not what was set");
    }
}
