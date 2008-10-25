package jam;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

final class LicenseReader {
	private static final Logger LOGGER = Logger.getLogger(Help.class
			.getPackage().getName());

	LicenseReader() {
		// nothing to do.
	}

	protected String getLicenseText() {
		final InputStream license_in = Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("license.txt");
		final Reader reader = new InputStreamReader(license_in);
		int length = 0;
		final char[] textarray = new char[2000];
		try {
			length = reader.read(textarray);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
		}
		return new String(textarray, 0, length);
	}

}
