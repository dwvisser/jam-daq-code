package jam.ui;

import jam.RunState;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;

import java.awt.Color;
import java.awt.Component;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Holds component which displays the current run state.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 * @version 2004-12-09
 * @see jam.RunState
 */
public final class RunStateBox implements Observer {
	private final JLabel lrunState = new JLabel("   Welcome   ",
			SwingConstants.CENTER);
	final JPanel pRunState = new JPanel();
	
	private static final RunStateBox INSTANCE=new RunStateBox();
	
	private RunStateBox(){
		Broadcaster.getSingletonInstance().addObserver(this);
		pRunState.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		pRunState.add(new JLabel(" Status: "));
		lrunState.setOpaque(true);
		pRunState.add(lrunState);	    	
	}
	
	/**
	 * @return the only instance of this class.
	 */
	static public RunStateBox getInstance(){
	    return INSTANCE;
	}

	/**
	 * Returns the displayable component.
	 * @return the displayable component
	 */
	public Component getComponent(){
		return pRunState;
	}
	
	private void setRunState(RunState rs) {
		lrunState.setBackground(rs.getColor());
		lrunState.setText(rs.getLabel());
	}
	
	/**
	 * Implementation of Observable interface.
	 * 
	 * @param observable
	 *            the sender
	 * @param o
	 *            the message
	 */
	public void update(Observable observable, Object o) {
		final BroadcastEvent be = (BroadcastEvent) o;
		final BroadcastEvent.Command command = be.getCommand();
		if (command == BroadcastEvent.Command.RUN_STATE_CHANGED) {
			setRunState((RunState) be.getContent());
		} 
	}

}
