package jam.data.control;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JTextField;

import com.google.inject.Inject;

import jam.data.Factory;
import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.ui.PanelOKApplyCancelButtons;

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
	 * 
	 * @param frame
	 *            application frame
	 * @param broadcaster
	 *            broadcasts state changes
	 */
	@Inject
	public GroupNew(final Frame frame, final Broadcaster broadcaster) {
		super(frame, "New Group", false, broadcaster);
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
		Factory.createGroup(textName.getText(), Group.Type.TEMP);
		broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
	}

	/**
	 * Does nothing. It is here to match other contollers.
	 */
	@Override
	public void doSetup() {
		// NOOP
	}

}
