package jam.ui;


import javax.swing.JOptionPane;
import javax.swing.UIManager;

public final class Utility {
	
	private Utility(){
		super();
	}

	public static void setLookAndFeel() {
		try {
			String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			// Override system look and feel for gtk
			if ("com.sun.java.swing.plaf.gtk.GTKLookAndFeel".equals(lookAndFeel)) {
				lookAndFeel = UIManager.getCrossPlatformLookAndFeelClassName();
			}
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			Utility.warning(e, "Jam--error setting GUI appearance");
		}
	}

	public static void warning(final Exception exception, final String title) {
		JOptionPane.showMessageDialog(null, exception.getMessage(), title,
				JOptionPane.WARNING_MESSAGE);
	}

}
