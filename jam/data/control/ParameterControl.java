package jam.data.control;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import jam.global.*;
import jam.data.*;
import javax.swing.*;

/**
 * Sets and displays the Parameters (data.Parameters.class) 
 * used for sorting
 *
 * @version	0.5 October 98
 * @author 	Ken Swartz
 *
 */
public final class ParameterControl
	extends DataControl
	implements ActionListener, ItemListener {

	private Frame frame;
	private Broadcaster broadcaster;
	private MessageHandler messageHandler;

	//dialog box	              
	private JDialog ddisp;

	//widgets for each parameter
	private JPanel[] pParam;
	private JLabel[] labelParam;
	private JTextField[] textParam;

	private JPanel pbut;

	public ParameterControl(
		Frame frame,
		Broadcaster broadcaster,
		MessageHandler messageHandler) {
		super();
		this.frame = frame;
		this.broadcaster = broadcaster;
		this.messageHandler = messageHandler;
		// dialog box to display Parameters
		ddisp = new JDialog(frame, "Sort Parameters", false);
		ddisp.setResizable(false);
		ddisp.setLocation(20, 50);
		Container cddisp = ddisp.getContentPane();
		cddisp.setLayout(new GridLayout(0, 1, 5, 5));
		// buttons for display dialog				
		pbut = new JPanel(new GridLayout(1, 0, 5, 5));
		JButton bread = new JButton("Read");
		bread.setActionCommand("read");
		bread.addActionListener(this);
		pbut.add(bread);
		JButton bset = new JButton("Set");
		bset.setActionCommand("set");
		bset.addActionListener(this);
		pbut.add(bset);
		ddisp.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ddisp.dispose();
			}
		});
		setup();
	}

	/** 
	 * Action set parameters
	 *
	 */
	public void actionPerformed(ActionEvent ae) {

		String command = ae.getActionCommand();

		try {
			if (command == "set") {
				set();

			} else if (command == "read") {
				read();

			} else {
				System.err.println(
					"Error Unregonized command [ParameterControl]");
				throw new DataException("Logic Error");
			}
		} catch (DataException je) {
			messageHandler.errorOutln(je.getMessage());
		}
	}
	/**
	 * Does nothing for right now
	 *
	 */
	public void itemStateChanged(ItemEvent ie) {
		/* not used so far 
		if (ie.getItemSelectable()==checkDisabled) {
		}
		*/
	}
	/**
	 * Show the Display dialog box
	 *
	 */
	public void show() {
		ddisp.show();
	}
	/**
	 * Setup the display dialog box.
	 *
	 */
	public void setup() {
		DataParameter currentParameter;
		Iterator enumParameter;
		int numberParameters;
		int count;

		numberParameters = DataParameter.getParameterList().size();
		Container cddisp = ddisp.getContentPane();
		cddisp.removeAll();
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
				pParam[count] =
					new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
				cddisp.add(pParam[count]);
				labelParam[count] =
					new JLabel(currentParameter.getName(), JLabel.RIGHT);
				pParam[count].add(labelParam[count]);
				textParam[count] = new JTextField(" ");
				textParam[count].setColumns(10);
				textParam[count].setEditable(true);
				textParam[count].setBackground(Color.white);
				textParam[count].setForeground(Color.black);
				pParam[count].add(textParam[count]);
				count++;
			}
		}
		cddisp.add(pbut);
		ddisp.pack();
	}

	/** 
	 * Set the Parameter values called back by
	 *
	 */
	public void setParameters(int[] inParamValue) {

		for (int i = 0; i < inParamValue.length; i++) {
			textParam[i].setText(String.valueOf(inParamValue[i]));
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
	 * and display them
	 *	
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

}
