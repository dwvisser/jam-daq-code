package jam.ui;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

/**
 * User interface utility methods.
 * 
 * @author Dale Visser
 * 
 */
public final class Utility {

	private Utility() {
		super();
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
			Utility.warning(e, "Jam--error setting GUI appearance");
		}
	}

	/**
	 * @param exception
	 *            to show warning dialog for
	 * @param title
	 *            title to show on dialog
	 */
	public static void warning(final Exception exception, final String title) {
		JOptionPane.showMessageDialog(null, exception.getMessage(), title,
				JOptionPane.WARNING_MESSAGE);
	}

}
