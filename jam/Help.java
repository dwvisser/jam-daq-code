package jam;

import jam.global.MessageHandler;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.help.HelpSet;
import javax.help.CSH;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Help shows the program about
 *
 * @author Ken Swartz
 * @author Dale Visser
 * @version version 0.5 November 98
 */
class Help {

	private final Frame frame;
	private final JDialog aboutD, licenseD;
	private final MessageHandler messageHandler;
	private final static int posx=20;
	private final static int posy=50;

	/**
	 * @param f the parent frame, i.e. the main Jam Window
	 * @param mh for outputting error messages
	 */
	Help(Frame f, MessageHandler mh) {
		final String url="http://jam-daq.sourceforge.net/";
		this.frame = f;
		messageHandler=mh;

		aboutD = new JDialog(frame, "About Jam", false);
		final Container cad = aboutD.getContentPane();
		aboutD.setResizable(false);
		aboutD.setLocation(posx, posy);
		cad.setLayout(new BorderLayout());
		final JPanel pcenter = new JPanel(new GridLayout(0, 1));
		Border border = new EmptyBorder(20,20,20,20);
		pcenter.setBorder(border);
		cad.add(pcenter, BorderLayout.CENTER);
		pcenter.add(
			new JLabel("Jam v" + Version.getName(), JLabel.CENTER));
		pcenter.add(new JLabel("by", JLabel.CENTER));
		pcenter.add(
			new JLabel(
				"Ken Swartz, Dale Visser, and John Baris",
				JLabel.CENTER));
		pcenter.add(
			new JLabel(url, JLabel.CENTER));
		final JPanel pbut = new JPanel(new FlowLayout(FlowLayout.CENTER));
		cad.add(pbut, BorderLayout.SOUTH);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				aboutD.dispose();
			}
		});
		pbut.add(bok);
		aboutD.pack();
		/* Receives events for closing the dialog box and closes it. */
		aboutD.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				aboutD.dispose();
			}
		});
		synchronized (this) {
			licenseD =
			new JDialog(
				frame,
				"University of Illinois/NCSA Open Source License",
				true);
		}
		layoutLicenseDialog();
		final String defaultVal="notseen";
		final String version=Version.getName();
		final String key="license";
		final Preferences helpnode=Preferences.userNodeForPackage(getClass());
		if (!version.equals(helpnode.get(key,defaultVal))){
			showLicense();
			helpnode.put(key,version);
		}
	}

	/**
	 * Show the "About Jam" dialog box.
	 */
	public void showAbout() {
		aboutD.show();
	}

	private void layoutLicenseDialog() {
		final String hyphen = " - ";
		final Container contents = licenseD.getContentPane();
		licenseD.setResizable(true);
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
			messageHandler.errorOutln(getClass().getName()+hyphen+e.getMessage());
		}
		final String text = new String(textarray, 0, length);
		center.add(new JScrollPane(new JTextArea(text)));
		contents.add(center, BorderLayout.CENTER);
		final JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contents.add(south, BorderLayout.SOUTH);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				licenseD.dispose();
			}
		});
		south.add(bok);
		licenseD.pack();
		final Dimension screen=Toolkit.getDefaultToolkit().getScreenSize();
		licenseD.setSize(licenseD.getWidth(),screen.height/2);
		licenseD.setLocation(posx,screen.height/4);
		/* Recieves events for closing the dialog box and closes it. */
		aboutD.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				aboutD.dispose();
			}
		});
	}

	/**
	 * Show Jam's open source license text.
	 */
	public final void showLicense() {
		licenseD.show();
	}
	
	public static void main(String [] args){
		final String helpsetName = "help/jam.hs";
		try {
			final URL hsURL =
				ClassLoader.getSystemClassLoader().getResource(helpsetName);
			final HelpSet hs = new HelpSet(null, hsURL);
			final ActionListener al=new CSH.DisplayHelpFromSource(hs.createHelpBroker());
			final JButton proxy=new JButton("Proxy");
			proxy.addActionListener(al);
			proxy.doClick();
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
		} catch (Exception ee) {
			JOptionPane.showMessageDialog(null,ee.getMessage(),ee.getClass().getName(),
			JOptionPane.ERROR_MESSAGE);
		}
	}
		
}