package jam.data.control;

import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;

import javax.swing.JTextField;

/**
 * * Class create a new group
 * 
 * @author Ken Swartz
 * 
 */
public class GroupNew extends AbstractControl {

	private transient final JTextField textName;

	/**
	 * Constructs a "new group" dialog command.
	 */
	public GroupNew() {
		super("New Group", false);
		textName = GroupControlInitializer.initializeDialog(this);
		final PanelOKApplyCancelButtons pButtons = new PanelOKApplyCancelButtons(
				new PanelOKApplyCancelButtons.AbstractListener(this) {
					public void apply() {
						createGroup();
					}
				});
		getContentPane().add(pButtons.getComponent(), BorderLayout.SOUTH);
		pack();
	}

	/**
	 * Create a new group
	 * 
	 */
	private void createGroup() {
		Group.createGroup(textName.getText(), Group.Type.TEMP);
		BROADCASTER.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
	}

	/**
	 * Does nothing. It is here to match other contollers.
	 */
	public void doSetup() {
		// NOOP
	}

}
