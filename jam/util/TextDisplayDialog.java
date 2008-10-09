/*
 * Created on Apr 2, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Shows a dialog with the given text.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Apr 2, 2004
 */
public class TextDisplayDialog extends JDialog {

	/**
	 * @param frame
	 *            parent frame
	 * @param title
	 *            title for dialog
	 * @param modal
	 *            whether the dialog is modal
	 * @param text
	 *            text to display in dialog
	 */
	public TextDisplayDialog(Frame frame, String title, boolean modal,
			String text) {
		super(frame, title, modal);
		final Container contents = getContentPane();
		contents.setLayout(new BorderLayout());
		final JTextArea textArea = new JTextArea(text);
		final JScrollPane jsp = new JScrollPane(textArea);
		textArea
				.setToolTipText("Use select, cut and paste to export the text.");
		contents.add(jsp, BorderLayout.CENTER);
		pack();
		final Dimension screenSize = Toolkit.getDefaultToolkit()
				.getScreenSize();
		final int del = 25;
		final int xcoord = frame.getX() + del;
		final int ycoord = frame.getY() + del;
		final Dimension initSize = getSize();
		final int sizex = Math.min(initSize.width, screenSize.width - del
				- xcoord);
		final int sizey = Math.min(initSize.height, screenSize.height - del
				- ycoord);
		setLocation(xcoord, ycoord);
		setSize(sizex, sizey);
		setVisible(true);
	}
}
