package jam.commands;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jam.data.Group;
import jam.global.BroadcastEvent;
import jam.global.CommandListenerException;

/**
 * Delete a group
 * 
 * @author Ken Swartz
 *
 */
public class DeleteGroup extends AbstractCommand {

	DeleteGroup(){
		super();
		putValue(NAME,"Delete Group\u2026");
	}
	
	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#execute(java.lang.Object[])
	 */
	protected void execute(Object[] cmdParams) throws CommandException {
		final JFrame frame =status.getFrame();
		Group group = status.getCurrentGroup();
		if (null==group){
			msghdlr.errorOutln("Need to select a group.");
			return;
		}
		final Group.Type type=group.getType();
		String name = group.getName();
		/* Cannot delete sort histograms */
		if (type == Group.Type.SORT) {
			msghdlr.errorOutln("Cannot delete '"+name+"', it is sort group.");
		} else {
			if (JOptionPane.YES_OPTION==JOptionPane.showConfirmDialog(frame,
					"Delete "+name+"?","Delete group",JOptionPane.YES_NO_OPTION)){
				Group.clearGroup(group);
				broadcaster.broadcast(BroadcastEvent.Command.HISTOGRAM_ADD);
			}
		}


	}

	/* (non-Javadoc)
	 * @see jam.commands.AbstractCommand#executeParse(java.lang.String[])
	 */
	protected void executeParse(String[] cmdTokens)
			throws CommandListenerException {
		// TODO Auto-generated method stub

	}

}
