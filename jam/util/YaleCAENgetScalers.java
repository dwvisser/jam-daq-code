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
import jam.global.JamStatus;
import jam.global.MessageHandler;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class YaleCAENgetScalers {

	private final Frame frame;
	private final MessageHandler console;
	private String strScalerText;
	private String fileName;
	private final StringBuffer strError=new StringBuffer();

	public YaleCAENgetScalers() {
		JamStatus js = JamStatus.instance();
		frame = js.getFrame();
		console = js.getMessageHandler();
	}

	public void display() {
		new TextDisplayDialog(frame, fileName, false, strScalerText);
	}
	
	/**
	 * Takes an event file, searches for scaler blocks in it and 
	 * creates tab-delimited text listing each scaler block on one
	 * row of text.
	 * 
	 * @param events the file to search
	 * @return whether we were successful
	 */
	public boolean processEventFile(File events) {
		boolean rtnState = true;
		final StringBuffer strBuff = new StringBuffer();
		final int SCALER_HEADER = 0x01cccccc;
		DataInputStream dis = null;
		strError.delete(0, strError.length());
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
						strBuff.append(dis.readInt());
						if (i < numScalers) {
							strBuff.append('\t');
						}
					}
					strBuff.append('\n');
				}
			}
			//End of file reached	
		} catch (EOFException eofe) {
			if (dis != null) {
				//Bury close exception
				try {
					dis.close();
					rtnState = true;
				} catch (Exception e) {
					strError.append(e.getMessage());
					rtnState=false;
				};
			}
		} catch (IOException ioe) {
			strError.append("Reading file: " + ioe.getMessage());
			rtnState = false;
		}
		fileName = events.getName();
		strScalerText = strBuff.toString();
		return rtnState;
	}

	public String getErrorTxt() {
		return strError.toString();
	}

}
