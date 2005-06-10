package jam.data.control;
import jam.data.DataParameter;
import jam.global.MessageHandler;
import jam.io.ExtensionFileFilter;
import jam.util.StringUtilities;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
/**
 * Sets and displays the Parameters (data.Parameters.class)
 * used for sorting
 *
 * @version	0.5 October 98
 * @author 	Ken Swartz
 *
 */
public final class ParameterControl
	extends AbstractControl {

	private final int MAX_INITIAL_DISPLAY=15;
	
	//widgets for each parameter
	JScrollPane scrollPane; 	
	private JPanel pCenter;
	private JPanel[] pParam;
	private JLabel[] labelParam;
	private JTextField[] textParam;
	private final int borderHeight=5;

	private File lastFile; //last file referred to in a JFileChooser	

	final StringUtilities stringUtil=StringUtilities.instance();
	
	private final MessageHandler messageHandler;
	
	/**
	 * Constructs a new parameter dialog.
	 * @param messageHandler where to print messages
	 */
	public ParameterControl(
		MessageHandler messageHandler) {
		super("Sort Parameters", true);
		this.messageHandler = messageHandler;

		// dialog box to display Parameters
		setResizable(true);		
		setLocation(20, 50);
		final Container cddisp = getContentPane();
		cddisp.setLayout(new BorderLayout());

		//Central Panel
		pCenter =new JPanel(new GridLayout(0,1,borderHeight,5));
		pCenter.setBorder(new EmptyBorder(borderHeight,10,borderHeight,10));
		
		scrollPane = new JScrollPane(pCenter);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);		
		cddisp.add(scrollPane, BorderLayout.CENTER);


		//Buttons for display dialog
		JPanel pButtons = new JPanel(new GridLayout(0, 1, 0, 0));
		cddisp.add(pButtons, BorderLayout.SOUTH);
		
		JPanel pLoad = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pButtons.add(pLoad);
		JButton bsave = new JButton("Save");
		bsave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				save();
			}
		});
		pLoad.add(bsave);
		
		JButton bload = new JButton("Load");
		bload.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				load();
			}
		});
		pLoad.add(bload);
		
		
		JPanel pOKButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pButtons.add(pOKButton);
		JPanel pbut = new JPanel(new GridLayout(1, 0, 5, 5));
		pOKButton.add(pbut);
		JButton bread = new JButton("Read");
		bread.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				read();
			}
		});
		pbut.add(bread);
		JButton bset = new JButton("Set");
		bset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				set();
			}
		});
		pbut.add(bset);
		
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
		doSetup();
	}
	public void setVisible(boolean state) {
		if (state) {
			read();
		}
		super.setVisible(state);
	}
	/**
	 * Setup the display dialog box.
	 *
	 */
	public void doSetup() {
		DataParameter currentParameter;
		Iterator enumParameter;
		int numberParameters;
		int count;

		numberParameters = DataParameter.getParameterList().size();
		//Container cddisp = ddisp.getContentPane();
		//cddisp.removeAll();
		pCenter.removeAll();
		// we have some elements in the parameter list
		if (numberParameters != 0) {
			//widgets for each parameter
			pParam = new JPanel[numberParameters];
			labelParam = new JLabel[numberParameters];
			textParam = new JTextField[numberParameters];
			enumParameter = DataParameter.getParameterList().iterator();
			count = 0;
			while (enumParameter.hasNext()) {
				currentParameter = (DataParameter) enumParameter.next();
				pParam[count] = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
				pCenter.add(pParam[count]);
				labelParam[count] =new JLabel(currentParameter.getName().trim(), JLabel.RIGHT);
				pParam[count].add(labelParam[count]);
				textParam[count] = new JTextField("");
				textParam[count].setColumns(10);
				textParam[count].setEditable(true);
				pParam[count].add(textParam[count]);
				count++;
			}
		}
		pack();
		if (numberParameters>0) {
			Dimension dialogDim=calculateScrollDialogSize(this, pParam[0], borderHeight, numberParameters, MAX_INITIAL_DISPLAY);
			setSize(dialogDim);
		}
		
	}
	/**
	 * Set the parameter values using the values
	 * in the text fields
	 *
	 */
	public void set() {
		DataParameter currentParameter = null;
		Iterator enumParameter;
		int count;
		String textValue;

		enumParameter = DataParameter.getParameterList().iterator();

		try {
			count = 0;
			while (enumParameter.hasNext()) {
				currentParameter = (DataParameter) enumParameter.next();
				textValue = textParam[count].getText().trim();
				if (textValue.equals("")) {
					currentParameter.setValue(0.0);
				} else {
					currentParameter.setValue(
						(new Double(textValue).doubleValue()));
				}
				count++;
			}
			read();
		} catch (NumberFormatException nfe) {
			if (currentParameter != null) {
				messageHandler.errorOutln(
					"Not a valid number, parameter "
						+ currentParameter.getName()
						+ " [ParameterControl]");
			} else {
				messageHandler.errorOutln(
					"Not a valid number, null parameter [ParameterControl]");
			}
		}
	}

	/**
	 * Read the values from the Parameter Objects
	 * and display them.
	 */
	public void read() {
		if (DataParameter.getParameterList().size() != 0) {
			DataParameter.getParameterList().size();//number of parameters
			final Iterator enumParameter =
			DataParameter.getParameterList().iterator();
			int count = 0;
			while (enumParameter.hasNext()) {
				final DataParameter currentParameter = (DataParameter) enumParameter.next();
				textParam[count].setText(
					String.valueOf(currentParameter.getValue()));
				count++;
			}
		}
	}
	/**
	 * Save the parameters to a file
	 *
	 */
	private void save() {
		
		JFrame frame =null;
		
		set();
		
		JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(new ExtensionFileFilter(new String[] { "lst" },
				"List Files (*.lst)"));
		int option = fd.showSaveDialog(frame);
		// save current values 
		if (option == JFileChooser.APPROVE_OPTION
				&& fd.getSelectedFile() != null) {
			File outputFile = fd.getSelectedFile();
			Properties saveProperties = new Properties();			
			final Iterator iterParameter =
				DataParameter.getParameterList().iterator();
			while (iterParameter.hasNext()) {
				DataParameter parameter= (DataParameter)iterParameter.next();
				String valueString = (new Double(parameter.getValue())).toString();
				saveProperties.put(parameter.getName().trim(),valueString );
			}
			try {
				FileOutputStream fos = new FileOutputStream(outputFile);
				saveProperties.store(fos, "Jam Sort Parameters");
			} catch (FileNotFoundException fnfe) {
				messageHandler.errorOutln("Saving Parameters. Cannot create file "+outputFile.getName());
			} catch (IOException ioe) {				
				messageHandler.errorOutln("Saving Parameters. Cannot write to file "+outputFile.getName());				
			}
			messageHandler.messageOutln("Saved Parameters to file "+outputFile.getName());
		}
	}

	private void load() {
		JFrame frame =null;
		
		String name=null;
		
		JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(new ExtensionFileFilter(new String[] { "lst" },
				"List Files (*.lst)"));
		int option = fd.showOpenDialog(frame);
		// save current values 
		if (option == JFileChooser.APPROVE_OPTION
				&& fd.getSelectedFile() != null) {
			File inputFile =fd.getSelectedFile(); 
			try {			
				Properties saveProperties = new Properties();
				FileInputStream fos = new FileInputStream( inputFile);
				saveProperties.load(fos);					
			
				final Iterator iterParameter =
					DataParameter.getParameterList().iterator();
				while (iterParameter.hasNext()) {
					DataParameter parameter= (DataParameter)iterParameter.next();
					name = parameter.getName().trim();
					if (saveProperties.containsKey(name)) {
						String valueString = (String) saveProperties.get(name);						
						double valueDouble = Double.parseDouble(valueString);
						parameter.setValue(valueDouble);						
					} else {
						//add to list of not loaded
					}
				}
			} catch (IOException ioe) {				
				messageHandler.errorOutln("Loading Parameters. Cannot write to file "+inputFile.getName());				
			} catch (NumberFormatException nfe) {
				messageHandler.errorOutln("Loading Parameters. Cannot convert value for Parameter: "+name);
			}					
			read();
			messageHandler.messageOutln("Load Parameters from file "+inputFile.getName());
			
		}
	}
	
}
