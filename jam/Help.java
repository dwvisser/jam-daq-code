package jam;

import jam.global.JamStatus;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * Help shows the program about
 *
 * @author Ken Swartz
 * @author Dale Visser
 * @version version 0.5 November 98
 */
public class Help extends JDialog {
	
	private static final JamStatus status=JamStatus.instance();
	private final static int posx=20;

	/**
	 * @param frame the parent frame, i.e. the main Jam Window
	 * @param mh for outputting error messages
	 */
	public Help() {
		super(status.getFrame(),
		"University of Illinois/NCSA Open Source License",
		true);
		layoutLicenseDialog();
		final String defaultVal="notseen";
		final String version=Version.getName();
		final String key="license";
		final Preferences helpnode=Preferences.userNodeForPackage(getClass());
		if (status.isShowGUI() && 
		!version.equals(helpnode.get(key,defaultVal))){
			show();
			helpnode.put(key,version);
		}
	}


	private void layoutLicenseDialog() {
		final String hyphen = " - ";
		final Container contents = this.getContentPane();
		this.setResizable(true);
		contents.setLayout(new BorderLayout());
		final JPanel center = new JPanel(new GridLayout(0, 1));
		final InputStream license_in =
			getClass().getClassLoader().getResourceAsStream("license.txt");
		final Reader reader = new InputStreamReader(license_in);
		int length = 0;
		final char [] textarray=new char[2000];
		try {
			length = reader.read(textarray);
		} catch (IOException e) {
			status.getMessageHandler().errorOutln(getClass().getName()+hyphen+
			e.getMessage());
		}
		final String text = new String(textarray, 0, length);
		center.add(new JScrollPane(new JTextArea(text)));
		contents.add(center, BorderLayout.CENTER);
		final JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contents.add(south, BorderLayout.SOUTH);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				dispose();
			}
		});
		south.add(bok);
		this.pack();
		final Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
		this.setSize(this.getWidth(),screen.height/2);
		this.setLocation(posx,screen.height/4);
	}

	
	private static void setLookAndFeel(){
		final String linux = "Linux";
		final String kunststoff =
			"com.incors.plaf.kunststoff.KunststoffLookAndFeel";
		boolean useKunststoff = linux.equals(System.getProperty("os.name"));
		if (useKunststoff) {
			try {
				UIManager.setLookAndFeel(kunststoff);
			} catch (ClassNotFoundException e) {
				useKunststoff = false;
			} catch (Exception e) { //all other exceptions
				final String title = "Jam--error setting GUI appearance";
				JOptionPane.showMessageDialog(
					null,
					e.getMessage(),
					title,
					JOptionPane.WARNING_MESSAGE);
			}
		}
		if (!useKunststoff) {
			try {
				UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				final String title = "Error setting GUI appearance";
				JOptionPane.showMessageDialog(
					null,
					e.getMessage(),
					title,
					JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	public static void main(String [] args){
		final String helpsetName = "help/jam.hs";
		setLookAndFeel();
		try {
			final URL hsURL =
				ClassLoader.getSystemClassLoader().getResource(helpsetName);
			final HelpSet hs = new HelpSet(null, hsURL);
			final ActionListener al=new CSH.DisplayHelpFromSource(hs.createHelpBroker());
			final JButton proxy=new JButton("Proxy");
			proxy.addActionListener(al);
			final JFrame frame=new JFrame("JamHelp");
			final JButton exit=new JButton("Exit");
			frame.getContentPane().add(exit,BorderLayout.CENTER);
			exit.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					System.exit(0);
				}
			});
			frame.pack();
			frame.show();
			proxy.doClick();
		} catch (Exception ee) {
			JOptionPane.showMessageDialog(null,ee.getMessage(),ee.getClass().getName(),
			JOptionPane.ERROR_MESSAGE);
		}
	}
		
}