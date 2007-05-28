package jam.data.control;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

final class GroupControlInitializer {
	static JTextField initializeDialog(final JDialog dialog) {
		dialog.setLocation(30, 30);
		dialog.setResizable(false);
		final Container cdialog = dialog.getContentPane();
		cdialog.setLayout(new BorderLayout(10, 10));
		final JPanel pMiddle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10,
				10));
		cdialog.add(pMiddle, BorderLayout.CENTER);
		final JLabel lName = new JLabel("Name", SwingConstants.RIGHT);
		pMiddle.add(lName);
		final String space = " ";
		final JTextField textName = new JTextField(space);
		textName.setColumns(15);
		pMiddle.add(textName);
		return textName;
	}

	private GroupControlInitializer() {
		// do nothing
	}
}
