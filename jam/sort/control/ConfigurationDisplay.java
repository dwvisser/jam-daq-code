package jam.sort.control;

import jam.global.JamProperties;
import jam.global.JamStatus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;

/**
 * Dialog to so the configuration
 */
public class ConfigurationDisplay extends JDialog {

	private transient final JTextPane textConfig = new JTextPane();

	/**
	 * Constructor.
	 */
	public ConfigurationDisplay() {
		super(JamStatus.getSingletonInstance().getFrame(), "Configuration");
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout(10, 10));
		setSize(400, 400);
		setLocation(20, 50);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent event) {
				dispose();
			}

			public void windowOpened(final WindowEvent event) {
				setup();
			}
		});

		// Text
		textConfig.setEnabled(false);
		textConfig.setForeground(Color.BLACK);
		textConfig.setDisabledTextColor(Color.BLACK);

		// Panel so text wrap is disable
		JPanel noWrapPanel = new JPanel();
		noWrapPanel.setLayout(new BorderLayout());
		noWrapPanel.add(textConfig);
		// Scroll Panel
		JScrollPane scrollPane = new JScrollPane(noWrapPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		contents.add(scrollPane, BorderLayout.CENTER);

		// Low panel buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		contents.add(buttonPanel, BorderLayout.SOUTH);
		JButton btnClose = new JButton("Close");
		buttonPanel.add(btnClose);
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent actionEvent) {
				close();
			}
		});

	}

	/**
	 * Create a string from JamProperties
	 * 
	 * @return jam properties as a formatted string
	 */
	private String propertyString() {
		final List<String> keys = new ArrayList<String>();
		final StringBuffer strBuffProperties = new StringBuffer();
		final Properties properties = JamProperties.getProperties();
		final Enumeration names = properties.propertyNames();
		while (names.hasMoreElements()) {
			final String key = (String) names.nextElement();
			keys.add(key);
		}
		Collections.sort(keys);
		for (String key : keys) {
			final String value = properties.getProperty(key);
			strBuffProperties.append(key).append(" = ").append(value).append(
					'\n');
		}
		return strBuffProperties.toString();
	}

	/**
	 * Setup loads the properties into the text pane and lays out the dialog
	 * 
	 */
	public void setup() {//NOPMD
		textConfig.setText(propertyString());
		pack();
	}

	/**
	 * override of JDialog.close()
	 *
	 */
	public void close() {
		dispose();
	}
}