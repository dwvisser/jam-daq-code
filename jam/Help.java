package jam;
import jam.global.JamProperties;
import jam.global.MessageHandler;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import java.io.*;

/**
 * Help shows the program about
 *
 * @author Ken Swartz
 * @version version 0.5 November 98
 */
class Help {

	private String jamHome;
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
		jamHome = JamProperties.getPropString(JamProperties.JAM_HOME);

		aboutD = new JDialog(frame, "About Jam", false);
		final Container cad = aboutD.getContentPane();
		aboutD.setResizable(false);
		aboutD.setLocation(posx, posy);
		cad.setLayout(new BorderLayout());
		final JPanel pcenter = new JPanel(new GridLayout(0, 1));
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
		final JPanel pbut = new JPanel(new GridLayout(1, 0));
		cad.add(pbut, BorderLayout.SOUTH);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				aboutD.dispose();
			}
		});
		pbut.add(bok);
		aboutD.pack();
		/* Recieves events for closing the dialog box and closes it. */
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
				false);
		}
		layoutLicenseDialog();
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
		licenseD.setForeground(Color.black);
		licenseD.setBackground(Color.lightGray);
		licenseD.setResizable(false);
		licenseD.setLocation(posx, posy);
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
		System.out.println(text);

		center.add(new JScrollPane(new JTextArea(text)));
		contents.add(center, BorderLayout.CENTER);
		final JPanel south = new JPanel(new GridLayout(1, 0));
		contents.add(south, BorderLayout.SOUTH);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				licenseD.dispose();
			}
		});
		south.add(bok);
		licenseD.pack();
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
	public void showLicense() {
		licenseD.show();
	}
}