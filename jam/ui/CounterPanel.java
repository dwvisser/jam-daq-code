package jam.ui;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public final class CounterPanel extends JPanel {
	
	private transient final JTextField textField=new JTextField();

	public CounterPanel(final String description) {
		super();
		final String emptyString = "";
		final int cols = 8;
		final int flowgaph = 10;
		final int flowgapv = 0;
		setLayout(new FlowLayout(FlowLayout.RIGHT, flowgaph, flowgapv));
		final JLabel label = new JLabel(description, SwingConstants.RIGHT);
		textField.setText(emptyString);
		textField.setColumns(cols);
		textField.setEditable(false);
		add(label);
		add(textField);
	}
	
	public void setText(final String text){
		textField.setText(text);
	}
}
