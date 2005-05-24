package jam.data.control;

import jam.data.DataException;
import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.MessageHandler;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *  * Class create a new group
 *
 * @author Ken Swartz
 *
 */
public class GroupRename extends AbstractControl {

	private final JTextField textName;
	private Group currentGroup; 

	MessageHandler msgHandler=STATUS.getMessageHandler();	
	/**
	 * Constructs a "new group" dialog command.
	 */
	public GroupRename() {
		super("Rename Group ", false);
		setLocation(30, 30);
		setResizable(false);
		final Container cdialog = getContentPane();
		cdialog.setLayout(new BorderLayout(10, 10));
		JPanel pMiddle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,10));
		cdialog.add(pMiddle, BorderLayout.CENTER);
		final JLabel lName = new JLabel("Name", JLabel.RIGHT);
		pMiddle.add(lName);
		final String space = " ";
		textName = new JTextField(space);
		textName.setColumns(15);
		pMiddle.add(textName);
		final PanelOKApplyCancelButtons pButtons = new PanelOKApplyCancelButtons(
		        new PanelOKApplyCancelButtons.DefaultListener(this){
		            public void apply(){
		        		renameGroup();
		            }
		        });
		cdialog.add(pButtons.getComponent(), BorderLayout.SOUTH);
		
		pack();
	}
	
	/**
	 * Does nothing. It is here to match other contollers.
	 */
	public void doSetup() {

	}
	
	public void setVisible(final boolean show) {
		if (show) {
			currentGroup = STATUS.getCurrentGroup();
			if (currentGroup == null) {
				msgHandler.errorOutln("Need to select a group.");
			} else {
				if (currentGroup.getType() == Group.Type.SORT) {
					msgHandler
							.errorOutln("Cannot rename sort groups, selected group "
									+ currentGroup.getName() + ".");
				} else {
					textName.setText(currentGroup.getName());
					super.setVisible(true);
				}
			}
		} else {
			super.setVisible(false);
		}
	}
	
	/**
	 * Create a new group
	 *  
	 */
	private void renameGroup() {
        final String name = textName.getText();
        try {
            currentGroup.setName(name);
            BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
        } catch (DataException dataError) {
            msgHandler.errorOutln("Can't rename to existing name: " + name);
        }
    }
}
