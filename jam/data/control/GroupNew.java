package jam.data.control;

import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *  * Class create a new group
 *
 * @author Ken Swartz
 *
 */
public class GroupNew extends AbstractControl {

	private final JTextField textName;
	
	/**
	 * Constructs a "new group" dialog command.
	 */
	public GroupNew() {
		super("New Group ", false);
		setLocation(30, 30);
		setResizable(false);
		final Container cdialog = getContentPane();
		cdialog.setLayout(new BorderLayout(10, 10));
		JPanel pMiddle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,10));
		cdialog.add(pMiddle, BorderLayout.CENTER);
		final JLabel lName = new JLabel("Name", SwingConstants.RIGHT);
		pMiddle.add(lName);
		final String space = " ";
		textName = new JTextField(space);
		textName.setColumns(15);
		pMiddle.add(textName);
		final PanelOKApplyCancelButtons pButtons = new PanelOKApplyCancelButtons(
		        new PanelOKApplyCancelButtons.AbstractListener(this){
		            public void apply(){
		        		createGroup();
		            }
		        });
		cdialog.add(pButtons.getComponent(), BorderLayout.SOUTH);
		
		pack();
	}
	
	/**
	 * Does nothing. It is here to match other contollers.
	 */
	public void doSetup() {
		// NOOP
	}
	
	/**
	 * Create a new group
	 *
	 */
	private void createGroup() {
		Group.createGroup(textName.getText(), Group.Type.TEMP);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
	}

}
