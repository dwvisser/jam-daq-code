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
public class PeakFindDialog extends JDialog {

	private transient final Display display;
	private transient final MessageHandler console;

	/**
	 * Constructs a new peak find dialog.
	 *
	 */
	public PeakFindDialog() {
		final JamStatus status=JamStatus.getSingletonInstance();
		display = status.getDisplay();
		console = status.getMessageHandler();
		createDialog();
	}

	private transient JTextField width, sensitivity;
	private transient JCheckBox calibrate;
	
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
		final JPanel pGrid1by3 = new JPanel();
		pbutton.add(pGrid1by3);
		pGrid1by3.setLayout(new GridLayout(1, 3,5,5));

		final JButton bok = new JButton("OK");
		pGrid1by3.add(bok);
		bok.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				setPeakFindProperties();
				setVisible(false);
			}
		});
		final JButton bapply = new JButton("Apply");
		pGrid1by3.add(bapply);
		bapply.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				setPeakFindProperties();
			}
		});
		final JButton bcancel = new JButton(" Cancel ");
		pGrid1by3.add(bcancel);
		bcancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				setVisible(false);
			}
		});		
		setResizable(false);
		pack();
	}

	private void setPeakFindProperties(){
		final double dWidth = Double.parseDouble(width.getText().trim());
		final double dSense = Double.parseDouble(sensitivity.getText().trim());
		boolean cal = calibrate.isSelected();
		display.setPeakFindProperties(dWidth,dSense,cal);
		console.messageOut("Peak Find Properties Set: Width="+dWidth+
		", Sensitivity="+dSense,MessageHandler.NEW);
		if (!cal) {
			console.messageOut(", centroid channel displayed.",MessageHandler.END);
		} else {
			console.messageOut(", calibrated value displayed if available, "+
			"centroid channel if not.",MessageHandler.END);
		}
	}
}
