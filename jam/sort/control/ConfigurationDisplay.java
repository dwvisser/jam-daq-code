package jam.sort.control;

import jam.global.JamProperties;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.util.Enumeration;
import java.util.Properties;
import java.util.*;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

/**
* Dialog to so the configuration
**/
public class ConfigurationDisplay extends JDialog {

	private JTextPane textConfig;
	
	public ConfigurationDisplay() {
		this.setTitle("Configuration");
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout(10,10));
		setSize(400,400);
		setLocation(20, 50);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}

			public void windowOpened(WindowEvent e) {
				setup();
			}
		});

		//Text
		textConfig = new JTextPane(); 
		textConfig.setEnabled(false);
		textConfig.setForeground(Color.BLACK);
		textConfig.setDisabledTextColor(Color.BLACK);	

		//Panel so text wrap is disable
		JPanel noWrapPanel = new JPanel();
		noWrapPanel.setLayout( new BorderLayout() );
		noWrapPanel.add( textConfig );
		//Scroll Panel
		JScrollPane scrollPane = new JScrollPane(noWrapPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		contents.add(scrollPane, BorderLayout.CENTER);		

		//Low panel buttons
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
	 * @return jam properties as a formatted string
	 */
	private String propertyString() {
		
		List<String> keys= new ArrayList<String>(); 
		StringBuffer strBuffProperties = new StringBuffer();
		
		Properties properties = JamProperties.getProperties(); 
		Enumeration names = properties.propertyNames();
		
		while(names.hasMoreElements()){
			String key=(String)names.nextElement();
			keys.add(key);
		}
		
		Collections.sort(keys);
		
		 for(String key : keys) {
				String value = properties.getProperty(key);
				String entry=key+" = "+value+"\n";			 
				strBuffProperties.append(entry);				
		 }

		return strBuffProperties.toString();
		 
	}
	/**
	 * Setup loads the properties into the text pane and lays out the dialog
	 *
	 */
	public void setup() {
		textConfig.setText(propertyString());
		pack();			
	}
	
	public void close() {
		dispose();
	}
}
