package jam;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * The About Dialog
 * 
 * @author Ken Swartz
 */
public class AboutDialog extends JDialog {
	
	final String HOME_URL="http://jam-daq.sourceforge.net/";
	private final static int POS_X=20;
	private final static int POS_Y=50;
		
	
	public AboutDialog(Frame frame) {
		super(frame, "About Jam", false);
		
		
				
					
		final Container cad = this.getContentPane();
		this.setResizable(false);
		this.setLocation(POS_X, POS_Y);
		cad.setLayout(new BorderLayout());
		final JPanel pcenter = new JPanel(new GridLayout(0, 1));
		Border border = new EmptyBorder(20,20,20,20);
		pcenter.setBorder(border);
		cad.add(pcenter, BorderLayout.CENTER);
		pcenter.add(
			new JLabel("Jam v" + Version.getName(), JLabel.CENTER));
		pcenter.add(new JLabel("by", JLabel.CENTER));
		pcenter.add(
			new JLabel(
				"Ken Swartz, Dale Visser, and John Baris",
				JLabel.CENTER));
		pcenter.add(
			new JLabel(HOME_URL, JLabel.CENTER));
		final JPanel pbut = new JPanel(new FlowLayout(FlowLayout.CENTER));
		cad.add(pbut, BorderLayout.SOUTH);
		final JButton bok = new JButton("OK");
		bok.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				dispose();
			}
		});
		pbut.add(bok);
		this.pack();
		/* Receives events for closing the dialog box and closes it. */
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});

	}
	

}
