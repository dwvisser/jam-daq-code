package jam.data.control;

import jam.data.DataBase;
import jam.data.DataException;
import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.Nameable;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.util.logging.Level;

import javax.swing.JTextField;

/**
 * * Class create a new group
 * 
 * @author Ken Swartz
 * 
 */
public class GroupRename extends AbstractControl {

	private transient Group currentGroup;

	private transient final JTextField textName;

	/**
	 * Constructs a "new group" dialog command.
	 */
	public GroupRename() {
		super("Rename Group ", false);
		textName = GroupControlInitializer.initializeDialog(this);
		final PanelOKApplyCancelButtons pButtons = new PanelOKApplyCancelButtons(
				new PanelOKApplyCancelButtons.AbstractListener(this) {
					public void apply() {
						renameGroup();
					}
				});
		getContentPane().add(pButtons.getComponent(), BorderLayout.SOUTH);
		pack();
	}

	/**
	 * Does nothing. It is here to match other contollers.
	 */
	@Override
	public void doSetup() {
		// do-nothing implementation of AbstractControl method
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
			LOGGER.log(Level.SEVERE, "Can't rename to existing name: " + name,
					dataError);
		}
	}

	@Override
	public void setVisible(final boolean show) {
		if (show) {
			final Nameable nameable = STATUS.getCurrentGroup();
			if (DataBase.getInstance().isValid(nameable)) {
				currentGroup = (Group) nameable;
				if (currentGroup.getType() == Group.Type.SORT) {
					LOGGER.severe("Cannot rename sort groups, selected group "
							+ currentGroup.getName() + ".");
				} else {
					textName.setText(currentGroup.getName());
					super.setVisible(true);
				}
			} else {
				currentGroup = null; // NOPMD
				LOGGER.severe("Need to select a group.");
			}
		} else {
			super.setVisible(false);
		}
	}
}
