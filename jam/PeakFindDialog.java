package jam;

import javax.swing.*;
import jam.plot.*;
import jam.global.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Dialog for setting peak finding parameters.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W. Visser</a>
 */
public class PeakFindDialog extends JDialog implements ActionListener {

	private JamMain jamMain;
	private MessageHandler console;
	private Display display;

	public PeakFindDialog(JamMain jm, Display d, MessageHandler c) {
		jamMain = jm;
		display = d;
		console = c;
		createDialog();
	}

	private JTextField width, sensitivity;
	private JCheckBox calibrate;
	private void createDialog() {
		this.setTitle("Peak Find Preferences");
		Container contents = getContentPane();
		contents.setLayout(new BorderLayout());
		JPanel center=new JPanel(new BorderLayout());
		JPanel fields=new JPanel(new GridLayout(1,0));
		fields.add(new JLabel("Width"));
		width=new JTextField("12");
		width.setToolTipText("FWHM to search for.");
		fields.add(width);
		fields.add(new JLabel("Sensitivity"));
		sensitivity=new JTextField("3");
		sensitivity.setToolTipText("Greater values require better defined peaks.\n"+
		"A value of 3 gives an appr. 3% chance for a found peak to be false.");
		fields.add(sensitivity);
		calibrate = new JCheckBox("Display calibrated value if available",true);
		center.add(fields,BorderLayout.CENTER);
		center.add(calibrate,BorderLayout.SOUTH);
		contents.add(center,BorderLayout.NORTH);
		// panel for buttons         
		JPanel pb = new JPanel();
		pb.setLayout(new GridLayout(1, 3));
		contents.add(pb, BorderLayout.SOUTH);
		JButton bok = new JButton("OK");
		pb.add(bok);
		bok.setActionCommand("ok");
		bok.addActionListener(this);
		JButton bapply = new JButton("Apply");
		pb.add(bapply);
		bapply.setActionCommand("apply");
		bapply.addActionListener(this);
		JButton bcancel = new JButton(" Cancel ");
		pb.add(bcancel);
		bcancel.setActionCommand("cancel");
		bcancel.addActionListener(this);
		this.setResizable(false);
		//setPeakFindProperties();
		pack();
	}

	public synchronized void actionPerformed(ActionEvent e) {
		String text=e.getActionCommand();
		if (text.equals("ok") || text.equals("apply")){
			setPeakFindProperties();
		    if (text.equals("ok")) this.hide();
		} else if (text.equals("cancel")){
			this.hide();
		}
	}
	
	private void setPeakFindProperties(){
		double w = Double.parseDouble(width.getText().trim());
		double s = Double.parseDouble(sensitivity.getText().trim());
		boolean cal = calibrate.isSelected();
		display.setPeakFindProperties(w,s,cal);
		console.messageOut("Peak Find Properties Set: Width="+w+
		", Sensitivity="+s,MessageHandler.NEW);
		if (!cal) {
			console.messageOut(", centroid channel displayed.",MessageHandler.END);
		} else {
			console.messageOut(", calibrated value displayed if available, "+
			"centroid channel if not.",MessageHandler.END);
		}
	}
}
