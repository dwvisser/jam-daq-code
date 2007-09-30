package test.global;

import static org.junit.Assert.assertEquals;
import jam.global.JamProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public final class JamPropertiesTest {// NOPMD

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private static final String fileName = "JamConfig.ini";

	private static final String testProp = "test.prop";

	private static final String testValue = "testing";

	@Before
	public void setUp() {
		final String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
		final File out = new File(tmpDir, fileName);
		out.deleteOnExit();
		System.setProperty("jam.home", tmpDir);
		final Properties properties = new Properties();
		properties.put(testProp, testValue);
		try {
			final FileWriter writer = new FileWriter(out);
			properties.store(writer, "JamProperties Test");
			writer.close();
		} catch (IOException e) {
			System.err.println(e.getLocalizedMessage());// NOPMD
		}
	}

	@Test
	public void test() {
		new JamProperties();
		assertEquals("test property not what was set", testValue, JamProperties
				.getPropString(testProp));
	}
}
