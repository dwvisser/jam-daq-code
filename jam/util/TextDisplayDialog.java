/*
 * Created on Apr 2, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package jam.util;

import javax.swing.*;
import java.awt.*;


/**
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version Apr 2, 2004
 */
public class TextDisplayDialog extends JDialog {

	TextDisplayDialog(Frame f, String title, boolean modal, String text){
		super(f,title,modal);
		final Container contents=getContentPane();
		contents.setLayout(new BorderLayout());
		final JTextArea ta=new JTextArea(text);
		final JScrollPane jsp = new JScrollPane(ta);
		ta.setToolTipText("Use select, cut and paste to export the text.");
		contents.add(jsp, BorderLayout.CENTER);
		pack();
		final Dimension screenSize = 
		Toolkit.getDefaultToolkit().getScreenSize();
		final int del=25;
		final int x=f.getX()+del;
		final int y=f.getY()+del;
		final Dimension initSize=getSize();
		final int sizex=Math.min(initSize.width,
		screenSize.width-del-x);	
		final int sizey=Math.min(initSize.height,
		screenSize.height-del-y);	
		setLocation(x,y);
		setSize(sizex,sizey);
		show();
	}
}
