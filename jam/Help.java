package jam;
import jam.global.JamProperties;
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
class Help implements ActionListener {

	private String browser;
	private String jamHome;
	private String docsPath;

	private Frame frame;
	private JDialog aboutD;
	private JDialog licenseD;

	private final String LOCAL_USERGUIDE_PATH = "docs/jam_manual/index.html";
	private final String INET_USERGUIDE_URL =
		"http://jam-daq.sourceforge.net/jam_manual/index.html";

	private final String LOCAL_API_PATH = "docs/API/index.html";
	private final String INET_API_URL =
		"http://jam-daq.sourceforge.net/API/index.html";

	private final String LOCAL_ACK_PATH = "acknowledgements.html";
	/**
	 * Constructor
	 */
	public Help(Frame frame) {
		this.frame = frame;
		browser = JamProperties.getPropString(JamProperties.BROWSER_PATH);
		jamHome = JamProperties.getPropString(JamProperties.JAM_HOME);
		docsPath = JamProperties.getPropString(JamProperties.DOCS_PATH);

		aboutD = new JDialog(frame, "About Jam", false);
		Container cad = aboutD.getContentPane();
		aboutD.setForeground(Color.black);
		aboutD.setBackground(Color.lightGray);
		aboutD.setResizable(false);
		aboutD.setLocation(20, 50);
		//aboutD.setSize(300, 275);
		cad.setLayout(new BorderLayout());
		JPanel pcenter = new JPanel(new GridLayout(0, 1));
		cad.add(pcenter, BorderLayout.CENTER);
		pcenter.add(
			new JLabel("Jam version " + JamMain.JAM_VERSION, JLabel.CENTER));
		pcenter.add(new JLabel("by", JLabel.CENTER));
		pcenter.add(
			new JLabel(
				"Ken Swartz, Dale Visser, and John Baris",
				JLabel.CENTER));
		pcenter.add(
			new JLabel("http://jam-daq.sourceforge.net/", JLabel.CENTER));
		JPanel pbut = new JPanel(new GridLayout(1, 0));
		cad.add(pbut, BorderLayout.SOUTH);
		JButton bok = new JButton("OK");
		bok.setActionCommand("ok");
		bok.addActionListener(this);
		pbut.add(bok);
		aboutD.pack();
		//Recieves events for closing the dialog box and closes it.
		aboutD.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				aboutD.dispose();
			}
		});

		createLicenseDialog();

	}

	/**
	 * Recieves events from this dialog box.
	 */
	public void actionPerformed(ActionEvent ae) {
		String command = ae.getActionCommand();
		if (command == "ok") {
			aboutD.dispose();
		} else if (command.equals("l_ok")) {
			licenseD.dispose();
		}
	}

	/**
	 * show the about dialog box
	 */
	public void showAbout() {
		aboutD.show();
	}

	/**
	 * Display the jam documents using a browser
	 */
	public void showJamDocs() throws JamException {
		//new Browser("Jam API", this.LOCAL_API_PATH, this.INET_API_URL);
	}

	private void createLicenseDialog() {
		licenseD =
			new JDialog(
				frame,
				"University of Illinois/NCSA Open Source License",
				false);
		Container contents = licenseD.getContentPane();
		licenseD.setForeground(Color.black);
		licenseD.setBackground(Color.lightGray);
		licenseD.setResizable(false);
		licenseD.setLocation(20, 50);
		contents.setLayout(new BorderLayout());
		JPanel center = new JPanel(new GridLayout(0, 1));
		InputStream license_in =
			getClass().getClassLoader().getResourceAsStream("license.txt");
		Reader reader = new InputStreamReader(license_in);
		String text = "";
		int length = 0;
		char[] textarray = new char[2000];
		try {
			length = reader.read(textarray);
		} catch (IOException e) {
			System.err.println(e);
		}
		text = new String(textarray, 0, length);
		System.out.println(text);

		JTextArea textarea = new JTextArea(text);
		center.add(new JScrollPane(textarea));
		contents.add(center, BorderLayout.CENTER);
		JPanel south = new JPanel(new GridLayout(1, 0));
		contents.add(south, BorderLayout.SOUTH);
		JButton bok = new JButton("OK");
		bok.setActionCommand("l_ok");
		bok.addActionListener(this);
		south.add(bok);
		licenseD.pack();
		//Recieves events for closing the dialog box and closes it.
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