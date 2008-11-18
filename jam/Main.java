package jam;

import injection.GuiceInjector;
import jam.ui.Utility;

/**
 * Launch point for Jam.
 */
public final class Main {

	private Main() {
		// static class
	}

	/**
	 * @param args
	 *            not used
	 */
	public static void main(final String[] args) {
		Utility.setLookAndFeel();
		final int displayTime = 10000; // milliseconds
		new SplashWindow(GuiceInjector.getFrame(), displayTime);
		GuiceInjector.getJamInitialization().showMainWindow();
	}
}
