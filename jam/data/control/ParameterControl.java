package jam.data.control;
import jam.data.DataParameter;
import jam.global.MessageHandler;
import jam.io.ExtensionFileFilter;
import jam.util.FileUtilities;
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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
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

	private final String FILE_EXTENSION ="par";
	
	//widgets for each parameter
	private JScrollPane scrollPane; 	
	private JPanel pCenter;
	private JPanel[] pParam;
	private JLabel[] labelParam;
	private JTextField[] textParam;
	private final int borderHeight=5;
	private String invalidNames;
	private File lastFile; //last file referred to in a JFileChooser	

	final StringUtilities stringUtil=StringUtilities.instance();
	
	private final MessageHandler messageHandler;
	
    private final FileUtilities FILE_UTIL = FileUtilities.getInstance();	
	
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
		
		//Scroll Panel
		scrollPane = new JScrollPane(pCenter);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);		
		cddisp.add(scrollPane, BorderLayout.CENTER);

		//Buttons for display dialog
		JPanel pLower = new JPanel(new GridLayout(0, 1, 0, 0));
		pLower.setBorder(new EmptyBorder(5,0,0,0));
		cddisp.add(pLower, BorderLayout.SOUTH);
		
		JPanel pButtonsTop = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pLower.add(pButtonsTop);		
		JPanel pLoadSave =  new JPanel(new GridLayout(1, 0, 5, 5));
		pButtonsTop.add(pLoadSave);		
		Border etchBorder = new EtchedBorder();
		Border titledBorder =  new TitledBorder(etchBorder, "File");
		//pLoadSave.setBorder(titledBorder);
		
		JButton bload = new JButton("Load\u2026");
		bload.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				load();
			}
		});
		pLoadSave.add(bload);
		
		JButton bsave = new JButton("Save\u2026");
		bsave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				save();
			}
		});
		pLoadSave.add(bsave);
				
		JPanel pButtonsBottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pLower.add(pButtonsBottom);
		JPanel pOKApplyCancel = new JPanel(new GridLayout(1, 0, 5, 5));
		pButtonsBottom.add(pOKApplyCancel);
		
		final JButton brecall = new JButton("Recall");
		brecall.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				read();
			}
		});
		pOKApplyCancel.add(brecall);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				set();				
				dispose();
			}
		});
		pOKApplyCancel.add(bok);
		final JButton bapply = new JButton("Apply");
		bapply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				set();			}
		});
		pOKApplyCancel.add(bapply);
		final JButton bcancel = new JButton("Cancel");
		bcancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();				
			}
		});
		pOKApplyCancel.add(bcancel);
		
		
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
		Iterator parameterIter;
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
			parameterIter = DataParameter.getParameterList().iterator();
			count = 0;
			while (parameterIter.hasNext()) {
				currentParameter = (DataParameter) parameterIter.next();
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
			Dimension dialogDim=calculateScrollDialogSize(this, pParam[0], borderHeight, numberParameters);
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
		int count;
		String textValue;

		if (checkTextAreNumbers()) {		
			Iterator parameterIter = DataParameter.getParameterList().iterator();
			
			try {			
				count = 0;
				while (parameterIter.hasNext()) {
					currentParameter = (DataParameter) parameterIter.next();
					textValue = textParam[count].getText().trim();
					currentParameter.setValue(Double.parseDouble(textValue));
					count++;
				}
				read();
			} catch (NumberFormatException nfe) {
				if (currentParameter != null) {
					messageHandler.errorOutln(
						"Not a valid number for parameter "+ currentParameter.getName());
				} else {
					messageHandler.errorOutln("Not a valid number, null parameter");
				}
			}
		} else {
			
			if (!invalidNames.equals("")) {
				messageHandler.errorOutln("Parameters not set, not a number for parameter(s) "+invalidNames+".");
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
		fd.setFileFilter(new ExtensionFileFilter(new String[] { FILE_EXTENSION },
				"List Files (*."+FILE_EXTENSION+")"));
		int option = fd.showSaveDialog(frame);
		// save current values 
		if (option == JFileChooser.APPROVE_OPTION
				&& fd.getSelectedFile() != null) {

			File selectFile = fd.getSelectedFile();			
			File outputFile =FILE_UTIL.changeExtension(selectFile, FILE_EXTENSION, FileUtilities.APPEND_ONLY );
			if (FILE_UTIL.overWriteExistsConfirm(outputFile)) {
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
					messageHandler.messageOutln("Saved Parameters to file "+outputFile.getName());				
				} catch (FileNotFoundException fnfe) {
					messageHandler.errorOutln("Saving Parameters. Cannot create file "+outputFile.getName());
				} catch (IOException ioe) {				
					messageHandler.errorOutln("Saving Parameters. Cannot write to file "+outputFile.getName());				
				}
			}
		}
	}

	private void load() {
		JFrame frame =null;		
		String name=null;
		String listNotLoaded="";
		
		JFileChooser fd = new JFileChooser(lastFile);
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setFileFilter(new ExtensionFileFilter(new String[] {FILE_EXTENSION },
				"List Files (*."+FILE_EXTENSION+")"));
		int option = fd.showOpenDialog(frame);
		// save current values 
		if (option == JFileChooser.APPROVE_OPTION
				&& fd.getSelectedFile() != null) {
			File inputFile =fd.getSelectedFile(); 
			try {			
				//Load properties from file
				Properties saveProperties = new Properties();
				FileInputStream fos = new FileInputStream( inputFile);
				saveProperties.load(fos);					
				//copy from properties to parameters
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
						if (listNotLoaded.equals("")) {
							listNotLoaded=name;							
						} else {
							listNotLoaded+=", "+name;							
						}
					}
				}
				read();
				messageHandler.messageOutln("Load Parameters from file "+inputFile.getName());
				if (!listNotLoaded.equals("")){
					messageHandler.warningOutln("Did not load parameter(s) "+listNotLoaded+".");
				}
			} catch (IOException ioe) {				
				messageHandler.errorOutln("Loading Parameters. Cannot write to file "+inputFile.getName());				
			} catch (NumberFormatException nfe) {
				messageHandler.errorOutln("Loading Parameters. Cannot convert value for Parameter: "+name);
			}					
			
		}
	}
	/**
	 * Check all text fields are numbers
	 * @return
	 */
	private boolean checkTextAreNumbers() {
		String textValue;
		boolean allValid =true;
		//Check all fields are numbers
		invalidNames ="";
		for (int i=0;i<textParam.length;i++) {
			try {
				textValue = textParam[i].getText().trim();
				if (textValue.equals("")) {
					textValue ="0.0";
					textParam[i].setText(textValue);	
				}
				double value= Double.parseDouble(textValue);					
			} catch (NumberFormatException nfe) {
				allValid=false;
				if (!invalidNames.equals("")) {												
					invalidNames = invalidNames+", "+labelParam[i].getText();; 	
				}else {
					invalidNames = labelParam[i].getText();
				}
			}			
		}
		
		return allValid;
	}
}
