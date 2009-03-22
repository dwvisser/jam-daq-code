package test.sort.mockfrontend;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Mock front end for testing Jam online acquisition.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version Feb 15, 2004
 */
public class TestFrontEnd extends JFrame {

	private transient MessageReceiver receiver;// NOPMD

	/**
	 * Creates a new test front end app instance.
	 */
	public TestFrontEnd() {
		super("Test Front End for Jam");
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout());
		final JPanel center = new JPanel(new GridLayout(4, 2));
		contents.add(center, BorderLayout.CENTER);
		final Status status = new Status(Status.Value.BOOTED);
		center.add(status);
		final Counter eventsMade = new Counter("Events Generated", 0);
		center.add(eventsMade);
		final Counter buffersSent = new Counter("Buffers Sent", 0);
		center.add(buffersSent);
		final Counter eventsSent = new Counter("Events Sent", 0);
		center.add(eventsSent);
		final NamedTextPanel fAddress = new NamedTextPanel("Front End Address",
				"");
		center.add(fAddress);
		final NamedTextPanel jAddress = new NamedTextPanel(
				"Jam Message Send Address", "");
		center.add(jAddress);
		final NamedTextPanel jdAddress = new NamedTextPanel(
				"Jam Data Receive Address", "");
		center.add(jdAddress);
		final NamedTextPanel jmrAddress = new NamedTextPanel(
				"Jam Message Receive Address", "");
		center.add(jmrAddress);
		final Console console = new Console();
		contents.add(console, BorderLayout.SOUTH);
		try {
			final String localhost = "localhost";
			final InetSocketAddress frontEnd = new InetSocketAddress(localhost,
					5003);
			fAddress.setText(frontEnd.toString());
			final DatagramSocket frontEndSocket = new DatagramSocket(frontEnd);
			final InetSocketAddress jamSend = new InetSocketAddress(localhost,
					5002);
			jAddress.setText(jamSend.toString());
			final InetSocketAddress jamData = new InetSocketAddress(localhost,
					10205);
			jdAddress.setText(jamData.toString());
			final InetSocketAddress jamMessageReceive = new InetSocketAddress(
					localhost, 5006);
			jmrAddress.setText(jamMessageReceive.toString());
			final MessageSender sender = new MessageSender(eventsSent,
					buffersSent, console, frontEndSocket, jamData,
					jamMessageReceive);
			receiver = new MessageReceiver(this, console, frontEndSocket,
					sender);
		} catch (final SocketException se) {
			Console.LOGGER.throwing(TestFrontEnd.class.getName(), "ctor", se);
		}

		receiver.start();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final Runnable showWindow = new Runnable() {
			public void run() {
				pack();
				setVisible(true);
			}
		};
		SwingUtilities.invokeLater(showWindow);
	}

	/**
	 * Launches the GUI.
	 * 
	 * @param args
	 *            ignored
	 */
	public static void main(final String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			final String title = "Test Front End for Jam--error setting GUI appearance";
			JOptionPane.showMessageDialog(null, e.getMessage(), title,
					JOptionPane.WARNING_MESSAGE);
		}
		new TestFrontEnd();
	}

	/**
	 * 
	 * @return the receiver instance
	 */
	public MessageReceiver getMessageReceiver() {
		return this.receiver;
	}
}
