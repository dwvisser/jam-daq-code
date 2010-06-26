package jam;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import injection.GuiceInjector;

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
		Main.setLookAndFeel();
		final int displayTime = 10000; // milliseconds
		new SplashWindow(GuiceInjector.getFrame(), displayTime);
		GuiceInjector.getJamInitialization().showMainWindow();
	}

    /**
     * Set look and feel for the application
     */
    public static void setLookAndFeel() {
        try {
            String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
            // Override system look and feel for gtk
            if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
                    .equals(lookAndFeel)) {
                lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
            }
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Jam--error setting GUI appearance",
            JOptionPane.WARNING_MESSAGE);
        }
    }
}
