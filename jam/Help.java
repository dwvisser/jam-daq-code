package jam;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.help.HelpSetException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.google.inject.Inject;

/**
 * Deals with JavaHelp-based User Guide and an "About" dialog.
 * 
 * @author Ken Swartz
 * @author Dale Visser
 * @version version 0.5 November 98
 */
public class Help extends JDialog {
	private final static int POS_X = 20;

	/**
	 * Launches the User Guide, with an Exit button in an auxiliary frame.
	 * 
	 * @param args
	 *            ignored
	 */
	public static void main(final String[] args) {
		final String helpsetName = "help/HelpSet.xml";
		setLookAndFeel();
		try {
			final URL hsURL = ClassLoader.getSystemClassLoader().getResource(
					helpsetName);
			final HelpSet helpset = new HelpSet(null, hsURL);
			final ActionListener listener = new CSH.DisplayHelpFromSource(
					helpset.createHelpBroker());
			final JButton proxy = new JButton("Proxy");
			proxy.addActionListener(listener);
			final JFrame frame = new JFrame("Jam User Guide");
			final JButton exit = new JButton("Exit");
			frame.getContentPane().add(exit, BorderLayout.CENTER);
			exit.addActionListener(new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					System.exit(0);
				}
			});
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					frame.pack();
					frame.setVisible(true);
				}
			});
			proxy.doClick();
		} catch (HelpSetException helpSetException) {
			showErrorDialog(helpSetException);
		} catch (InvocationTargetException itException) {
			showErrorDialog(itException);
		} catch (InterruptedException iException) {
			showErrorDialog(iException);
		}
	}

	private static void showErrorDialog(final Throwable throwable) {
		JOptionPane.showMessageDialog(null, throwable.getMessage(), throwable
				.getClass().getName(), JOptionPane.ERROR_MESSAGE);
	}

	private static void setLookAndFeel() {
		final String linux = "Linux";
		final String kunststoff = "com.incors.plaf.kunststoff.KunststoffLookAndFeel";
		boolean bKunststoff = linux.equals(System.getProperty("os.name"));// NOPMD
		if (bKunststoff) {
			try {
				UIManager.setLookAndFeel(kunststoff);
			} catch (ClassNotFoundException e) {
				bKunststoff = false;
			} catch (Exception e) { // all other exceptions
				final String title = "Jam--error setting GUI appearance";
				JOptionPane.showMessageDialog(null, e.getMessage(), title,
						JOptionPane.WARNING_MESSAGE);
			}
		}
		if (!bKunststoff) {
			try {
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				final String title = "Error setting GUI appearance";
				JOptionPane.showMessageDialog(null, e.getMessage(), title,
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param frame
	 *            parent
	 * 
	 * @param licenseReader
	 *            responsible for getting the license text
	 */
	@Inject
	public Help(final JFrame frame, final LicenseReader licenseReader) {
		super(frame, "University of Illinois/NCSA Open Source License", true);
		layoutLicenseDialog(licenseReader);
		final String defaultVal = "notseen";
		final String version = Version.getInstance().getName();
		final String key = "license";
		final Preferences helpnode = Preferences.userNodeForPackage(getClass());
		if (frame.isVisible() && !version.equals(helpnode.get(key, defaultVal))) {
			setVisible(true);
			helpnode.put(key, version);
		}
	}

	private void layoutLicenseDialog(final LicenseReader licenseReader) {
		final Container contents = this.getContentPane();
		this.setResizable(true);
		contents.setLayout(new BorderLayout());
		final JPanel center = new JPanel(new GridLayout(0, 1));
		final String text = licenseReader.getLicenseText();
		center.add(new JScrollPane(new JTextArea(text)));
		contents.add(center, BorderLayout.CENTER);
		final JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contents.add(south, BorderLayout.SOUTH);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				dispose();
			}
		});
		south.add(bok);
		this.pack();
		final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(this.getWidth(), screen.height / 2);
		this.setLocation(POS_X, screen.height / 4);
	}
}