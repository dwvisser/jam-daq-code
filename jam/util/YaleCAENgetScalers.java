/***************************************************************
 * Nuclear Simulation Java Class Libraries
 * Copyright (C) 2003 Yale University
 * 
 * Original Developer
 *     Dale Visser (dale@visser.name)
 * 
 * OSI Certified Open Source Software
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the University of Illinois/NCSA 
 * Open Source License.
 * 
 * This program is distributed in the hope that it will be 
 * useful, but without any warranty; without even the implied 
 * warranty of merchantability or fitness for a particular 
 * purpose. See the University of Illinois/NCSA Open Source 
 * License for more details.
 * 
 * You should have received a copy of the University of 
 * Illinois/NCSA Open Source License along with this program; if 
 * not, see http://www.opensource.org/
 **************************************************************/
package jam.util;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.io.ExtensionFileFilter;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;

public class YaleCAENgetScalers {

	private final Frame frame;
	private final MessageHandler console;

	public YaleCAENgetScalers() {
		JamStatus js=JamStatus.instance();
		frame = js.getFrame();
		console = js.getMessageHandler();
	}

	/**
	 * Takes an event file, searches for scaler blocks in it and 
	 * returns tab-delimited text listing each scaler block on one
	 * row of text.
	 * 
	 * @param events the file to search
	 * @return text showing the scaler values in the event file
	 */
	public String processEventFile(File events) throws IOException {
		final StringBuffer rval = new StringBuffer();
		final int SCALER_HEADER = 0x01cccccc;
		DataInputStream dis = null;
		try {
			dis =
				new DataInputStream(
					new BufferedInputStream(new FileInputStream(events)));
			dis.skipBytes(256);
			int blockNum = 0;
			while (true) {
				int read_val = dis.readInt();
				if (read_val == SCALER_HEADER) {
					blockNum++;
					int numScalers = dis.readInt();
					for (int i = 1; i <= numScalers; i++) {
						rval.append(dis.readInt());
						if (i < numScalers) {
							rval.append('\t');
						}
					}
					rval.append('\n');
				}
			}
		} catch (EOFException e) {
			if (dis != null) {
				dis.close();
			}
			return rval.toString();
		}
	}

	public class GetScalersAction extends AbstractAction {
		
		public GetScalersAction(){
			super("Display scaler values from a YaleCAEN event file...");
		}
		
		public void actionPerformed(ActionEvent ae) {
			final File file = getFile();
			if (file != null) {
				String name = file.getName();
				boolean display = true;
				String text = null;
				try {
					text = processEventFile(file);
				} catch (IOException e) {
					console.errorOutln(e.getMessage());
					display = false;
				}
				if (display) {
					new TextDisplayDialog(frame,name,false,text);
					/*final JDialog jd = new JDialog(frame, name, false);
					final Container contents=jd.getContentPane();
					contents.setLayout(new BorderLayout());
					final JScrollPane jsp = new JScrollPane(text);
					text.setToolTipText("Use select, cut and paste to export the text.");
					contents.add(jsp, BorderLayout.CENTER);
					jd.pack();
					final Dimension screenSize = 
					Toolkit.getDefaultToolkit().getScreenSize();
					final int del=25;
					final int x=frame.getX()+del;
					final int y=frame.getY()+del;
					final Dimension initSize=jd.getSize();
					final int sizex=Math.min(initSize.width,
					screenSize.width-del-x);	
					final int sizey=Math.min(initSize.height,
					screenSize.height-del-y);	
					jd.setLocation(x,y);
					jd.setSize(sizex,sizey);
					jd.show();*/
				}
			}
		}
	}
	
	public Action getAction(){
		return new GetScalersAction();
	}

	private File lastFile =
		new File(JamProperties.getPropString(JamProperties.EVENT_INPATH));

	/**
	 * Get a *.evn file from a JFileChooser.
	 *
	 * @return	a <code>File</code> chosen by the user, null if dialog cancelled
	 */
	protected File getFile() {
		File file = null;
		int option;
		JFileChooser jfile = new JFileChooser(lastFile);
		jfile.setDialogTitle("Select an Event File");
		jfile.setFileFilter(new ExtensionFileFilter("evn"));
		option = jfile.showOpenDialog(null);
		/* don't do anything if it was cancel */
		if (option == JFileChooser.APPROVE_OPTION) {
			file = jfile.getSelectedFile();
			lastFile = file;
		}
		return file;
	}
}
