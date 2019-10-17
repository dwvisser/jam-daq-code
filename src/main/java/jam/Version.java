package jam;

import com.google.inject.Singleton;

/**
 * Constants representing which version of Jam this is.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version 30 December 2003
 */
@Singleton
public final class Version {

	/*
	 * When building, And replaces the version fields with the right values.
	 */

	/**
	 * Increments only for changes which impact backward and/or forward
	 * compatibility in a big way.
	 */
	private static final String MAJOR = "@MAJOR@";

	/**
	 * Incremented whenever work starts on new features to be included in a
	 * future release. Normally, these go into a new development branch, so that
	 * "sub-minor" level fixes, etc. may go into the current release without
	 * committing users to the new, untested stuff yet.
	 */
	private static final String MINOR = "@MINOR@";

	/**
	 * Incremented every time a bug fix or patch is performed for release back
	 * to the users.
	 */
	private static final String RELEASE = "@RELEASE@";

	private static final String PLATFORM = System.getProperty("os.name");

	private static final String JRE_VERSION = System
			.getProperty("java.version");

	private static final StringBuffer NAME = new StringBuffer(); // NOPMD
	static {
		NAME.append(MAJOR).append('.').append(MINOR).append('.')
				.append(RELEASE).append('-').append(PLATFORM);
	}

	/**
	 * @return a string representing the build version of Jam running
	 */
	public String getName() {
		return NAME.toString();
	}

	/**
	 * @return whether we are running on J2SE 6 or greater
	 */
	public boolean isJ2SE6() {
		final String[] parts = JRE_VERSION.split("\\.");
		final int minor = Integer.parseInt(parts[1]);
		return minor >= 6;
	}

}
