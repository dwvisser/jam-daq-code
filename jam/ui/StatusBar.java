/*
 * Created on Dec 6, 2004
 */
package jam.ui;

import jam.RunState;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;

import java.awt.Color;
import java.awt.Component;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Contains the panel displayed at the bottom of the Jam window
 * which contains status information.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public final class StatusBar implements Observer {
	
	private final JLabel info=new JLabel();
	private final JPanel panel=new JPanel();
	private final JLabel lrunState = new JLabel("   Welcome   ",
			SwingConstants.CENTER);

	private static final StatusBar INSTANCE=new StatusBar();

	private StatusBar(){
		Broadcaster.getSingletonInstance().addObserver(this);
		/* Run status */
		final Box pRunState = new Box(BoxLayout.X_AXIS);
		pRunState.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		pRunState.add(new JLabel(" Status: "));
		lrunState.setOpaque(true);
		pRunState.add(lrunState);
		panel.add(pRunState);
		panel.add(info);
	}
	
	private void setRunState(RunState rs) {
		lrunState.setBackground(rs.getColor());
		lrunState.setText(rs.getLabel());
	}
	
	static public StatusBar getInstance(){
		return INSTANCE;
	}

	public Component getComponent(){
		return panel;
	}
	
	public void setInfo(CharSequence text){
		info.setText(text.toString());
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
