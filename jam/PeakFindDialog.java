package jam;

import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.plot.Display;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for setting peak finding parameters.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public class PeakFindDialog extends JDialog implements ActionListener {

	private final Display display;
	private final MessageHandler console;

	public PeakFindDialog() {
		final JamStatus status=JamStatus.instance();
		display = status.getDisplay();
		console = status.getMessageHandler();
		createDialog();
	}

	private JTextField width, sensitivity;
	private JCheckBox calibrate;
	private void createDialog() {
		this.setTitle("Peak Find Preferences");
		Container contents = getContentPane();
		contents.setLayout(new BorderLayout(10,10));
		
		JPanel fields=new JPanel(new GridLayout(0,1,5,5));
		contents.add(fields,BorderLayout.CENTER);		
		fields.setBorder(new EmptyBorder(10,10,0,0));
		fields.add(new JLabel("Width", JLabel.RIGHT));
		fields.add(new JLabel("Sensitivity", JLabel.RIGHT));
		fields.add(new JLabel("Display", JLabel.RIGHT));

						
		JPanel center=new JPanel(new GridLayout(0,1,5,5));
		contents.add(center,BorderLayout.EAST);
		center.setBorder(new EmptyBorder(10,0,0,10));		
				
		width=new JTextField("12");
		width.setToolTipText("FWHM to search for.");
		center.add(width);

		sensitivity=new JTextField("3");
		sensitivity.setToolTipText("Greater values require better defined peaks.\n"+
		"A value of 3 gives an appr. 3% chance for a found peak to be false.");
		center.add(sensitivity);

		calibrate = new JCheckBox("Calibrated value",true);
		center.add(calibrate);

		
		/* panel for buttons */         
		JPanel pbutton = new JPanel(new FlowLayout(FlowLayout.CENTER));
		contents.add(pbutton, BorderLayout.SOUTH);		
		JPanel pb = new JPanel();
		pbutton.add(pb);
		pb.setLayout(new GridLayout(1, 3,5,5));

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
		
		setResizable(false);
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
