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

import javax.swing.ProgressMonitor;

/**
 * Scans YaleCAEN event files for scaler blocks.
 * 
 * @author <a href="mailto:dale@visser.name">Dale W Visser</a>
 */
public class YaleCAENgetScalers {

	private ProgressMonitor pBstatus;
	private final Frame frame;
	private final MessageHandler console;
	private String strScalerText;
	private String fileName;
	private final StringBuffer strError=new StringBuffer();

	/**
	 * Constructs an object that can scan YaleCAEN event files
	 * for scaler blocks.
	 */
	public YaleCAENgetScalers() {
		JamStatus js = JamStatus.getSingletonInstance();
		frame = js.getFrame();
		console = js.getMessageHandler();
	}

	private void display() {
		new TextDisplayDialog(frame, fileName, false, strScalerText);
	}
	
	/**
	 * Scans the given event file for scaler blocks.
	 * 
	 * @param events file to scan
	 */
	public void processEventFile(final File events){
		final Runnable r=new Runnable(){
			public void run(){
				if (doIt(events)) {
					display();				
				} else {  
					console.errorOutln("Reading Yale CAEN Scalers "+getErrorTxt());
				}
			}
		};
		final Thread t=new Thread(r);
		t.start();		
	}
	
	/**
	 * Takes an event file, searches for scaler blocks in it and 
	 * creates tab-delimited text listing each scaler block on one
	 * row of text.
	 * 
	 * @param events the file to search
	 * @return whether we were successful
	 */
	private boolean doIt(File events) {
		final int mega=1024*1024;
		final long fileLength=events.length();
		final int lengthMB=(int)(fileLength/mega);
		pBstatus=new ProgressMonitor(frame, "Scanning " +events.getName()+
		" for scaler blocks", "Initializing", 0, lengthMB);
		boolean rtnState = true;
		final StringBuffer strBuff = new StringBuffer();
		final int SCALER_HEADER = 0x01cccccc;
		DataInputStream dis = null;
		strError.delete(0, strError.length());
		int counter=0;
		int megaCounter=0;
		try {
			dis =
				new DataInputStream(
					new BufferedInputStream(new FileInputStream(events)));
			counter += dis.skipBytes(256);
			int blockNum = 0;
			while (true) {
				int readVal = dis.readInt();
				counter+=4;
				if (readVal == SCALER_HEADER) {
					blockNum++;
					int numScalers = dis.readInt();
					counter+=4;
					for (int i = 1; i <= numScalers; i++) {
						strBuff.append(dis.readInt());
						counter+=4;
						if (i < numScalers) {
							strBuff.append('\t');
						}
					}
					strBuff.append('\n');
				}
				if (counter >= mega){
					counter -= mega;
					megaCounter++;
					updateProgressBar(megaCounter+" of "+lengthMB+" MB read.", 
					megaCounter);
				}
			}
			//End of file reached	
		} catch (EOFException eofe) {
			if (dis != null) {
				//Bury close exception
				try {
					dis.close();
					updateProgressBar("Done.",lengthMB);
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

	private String getErrorTxt() {
		return strError.toString();
	}

	private void updateProgressBar(final String text, final int value){
		pBstatus.setNote(text);
		pBstatus.setProgress(value);
	}
	

}
