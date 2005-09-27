/*
 * Created on Dec 6, 2004
 */
package jam.ui;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Contains the panel displayed at the bottom of the Jam window
 * which contains status information.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public final class StatusBar {
	
	private transient final JLabel info=new JLabel();
	private transient final JPanel panel=new JPanel(new FlowLayout(FlowLayout.LEFT));

	private static final StatusBar INSTANCE=new StatusBar();

	private StatusBar(){
		super();
		panel.setBorder(BorderFactory.createLoweredBevelBorder());
		/* Run status */
		panel.add(info);
	}
	
	
	/**
	 * Returns the only instance of this class.
	 * 
	 * @return the only instance of this class
	 */
	static public StatusBar getInstance(){
		return INSTANCE;
	}

	/**
	 * Returns the displayable component.
	 * @return the displayable component
	 */
	public Component getComponent(){
		return panel;
	}
	
	/**
	 * Set the info text to display.
	 * @param text the text to display
	 */
	public void setInfo(final CharSequence text){
		info.setText(text.toString());
	}
	
	
}
