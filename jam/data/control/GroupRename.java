package jam.data.control;

import jam.data.DataException;
import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.util.logging.Level;

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
public class GroupRename extends AbstractControl {

	private transient final JTextField textName;
	private transient Group currentGroup; 

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
		final JLabel lName = new JLabel("Name", SwingConstants.RIGHT);
		pMiddle.add(lName);
		final String space = " ";
		textName = new JTextField(space);
		textName.setColumns(15);
		pMiddle.add(textName);
		final PanelOKApplyCancelButtons pButtons = new PanelOKApplyCancelButtons(
		        new PanelOKApplyCancelButtons.AbstractListener(this){
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
		//do-nothing implementation of AbstractControl method
	}
	
	public void setVisible(final boolean show) {
		if (show) {
			currentGroup = (Group)STATUS.getCurrentGroup();
			if (Group.isValid(currentGroup)) {
				if (currentGroup.getType() == Group.Type.SORT) {
					LOGGER.severe("Cannot rename sort groups, selected group "
									+ currentGroup.getName() + ".");
				} else {
					textName.setText(currentGroup.getName());
					super.setVisible(true);
				}
			} else {
				LOGGER.severe("Need to select a group.");
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
            LOGGER.log(Level.SEVERE,"Can't rename to existing name: " + name, dataError);
        }
    }
}
