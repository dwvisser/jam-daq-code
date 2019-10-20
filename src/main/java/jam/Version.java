package jam;

import java.io.IOException;
import java.util.Properties;

import com.google.inject.Singleton;

/**
 * Constants representing which version of Jam this is.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version 30 December 2003
 */
@Singleton
public final class Version {

	private static final String VERSION;

	static {
		Properties properties = new Properties();
		try {
			properties.load(Version.class.getClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		VERSION = properties.getProperty("version");
	}

	private static final String PLATFORM = System.getProperty("os.name");

	private static final StringBuffer NAME = new StringBuffer(); // NOPMD
	static {
		NAME.append(VERSION).append('-').append(PLATFORM);
	}

	/**
	 * @return a string representing the build version of Jam running
	 */
	public String getName() {
		return NAME.toString();
	}
}
