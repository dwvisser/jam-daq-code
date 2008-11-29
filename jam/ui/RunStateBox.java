package jam.ui;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.RunState;

import java.awt.Color;
import java.awt.Component;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Holds component which displays the current run state.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 * @version 2004-12-09
 * @see jam.global.RunState
 */
@Singleton
final class RunStateBox implements Observer {

	private transient final JLabel lrunState = new JLabel("   Welcome   ",
			SwingConstants.CENTER);

	private transient final JPanel pRunState = new JPanel();

	@Inject
	protected RunStateBox(final Broadcaster broadcaster) {
		super();
		broadcaster.addObserver(this);
		pRunState.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		pRunState.add(new JLabel(" Status: "));
		lrunState.setOpaque(true);
		pRunState.add(lrunState);
	}

	/**
	 * Returns the displayable component.
	 * 
	 * @return the displayable component
	 */
	protected Component getComponent() {
		return pRunState;
	}

	private void setRunState(final RunState runState) {
		lrunState.setBackground(runState.getColor());
		lrunState.setText(runState.getLabel());
	}

	/**
	 * Implementation of Observable interface.
	 * 
	 * @param observable
	 *            the sender
	 * @param object
	 *            the message
	 */
	public void update(final Observable observable, final Object object) {
		final BroadcastEvent event = (BroadcastEvent) object;
		final BroadcastEvent.Command command = event.getCommand();
		if (command == BroadcastEvent.Command.RUN_STATE_CHANGED) {
			setRunState((RunState) event.getContent());
		}
	}

}
