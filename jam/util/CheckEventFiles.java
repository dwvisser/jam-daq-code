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
import java.io.*;

/**
 * @author dwvisser
 * @version 6 August 2002
 *
 * This program will check event files of the type generated by jam.sort.stream.YaleCAEN_InputStream 
 * for the presence of the buggy behavior whereby the last buffer from the previous run gets
 * put as the first buffer as the next run.  Give it a directory and it will print which event
 * files have a first buffer with and end-of-run character at the end of it.
 */
public class CheckEventFiles {

	private CheckEventFiles(File dir, File outDir) {

		File[] eventFiles = getEventFiles(dir);
		if (eventFiles.length > 0) {
			System.out.println("Found event files, starting to examine...");
			checkFirstBuffer(eventFiles);
			makeScalerSummaries(eventFiles, outDir);
		} else {
			System.out.println("Didn't find event files in " + dir);
		}
		System.out.println("Done.");
	}

	private void checkFirstBuffer(File[] eventFiles) {
		for (int i = 0; i < eventFiles.length; i++) {
			int bytesToSkip = 256 + 8192 - 4;
			System.out.print("Checking File " + eventFiles[i]);
			try {
				DataInputStream instream =
					new DataInputStream(
						new BufferedInputStream(
							new FileInputStream(eventFiles[i])));
				/* skip header and all but last word of first data buffer */
				boolean skipSuccess =
					(((long) bytesToSkip) == instream.skip(bytesToSkip));
				if (skipSuccess) {
					int word = instream.readInt();
					String s_word = "0x" + Integer.toHexString(word);
					if (word == 0x01EEEEEE) { //end-of-run word
						System.out.println(
							"...[" + s_word + "]...needs fixing");
					} else {
						System.out.println("...[" + s_word + "]...OK");
					}
				} else {
					System.out.println(
						"...file not long enough for one data buffer");
				}
				instream.close();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	private void makeScalerSummaries(File[] infiles, File outPath) {
		final int SCALER_HEADER = 0x01cccccc;
		DataInputStream fromStream = null;
		FileWriter csvStream = null;
		FileInputStream fromFile;
		File csvFile;
		for (int j = 0; j < infiles.length; j++) {
			try {
				fromFile = new FileInputStream(infiles[j]);
				csvFile =
					new File(
						outPath,
						infiles[j].getName().substring(
							0,
							infiles[j].getName().lastIndexOf(".evn"))
							+ "_scalers.csv");
				System.out.println("Reading file: " + infiles[j].getPath());
				System.out.println("Scaler summary in: " + csvFile.getPath());
				fromStream =
					new DataInputStream(new BufferedInputStream(fromFile));
				csvStream = new FileWriter(csvFile);
				//skip header from input stream 
				fromStream.skipBytes(256);
				int blockNum = 0;
				int [] lastVal=new int[16];
				int [] val=new int[16];
				while (true) {
					int read_val = fromStream.readInt();
					if (read_val == SCALER_HEADER) {
						blockNum++;
						int numScalers = fromStream.readInt();
						if (blockNum==1) {
							lastVal = new int[numScalers];
							val = new int[numScalers];
							for (int i=0; i<numScalers; i++) lastVal[i]=-1;
						}
						for (int i = 0; i < numScalers; i++) {
							val[i]=fromStream.readInt();
							csvStream.write(
								Integer.toString(val[i]));
							if (i < numScalers)
								csvStream.write(",");
							if (val[i]<lastVal[i]) {
									System.out.println("Scaler "+i+" out of sequence, block "+blockNum);
							}
							lastVal[i]=val[i];
						}
						csvStream.write("\n");
					}
				}
				//System.out.println("End of event file reached. Closing file.");				
			} catch (EOFException e) {
				System.err.println("EOFException: End of event file reached. Closing file.");
			} catch (IOException e) {
				System.err.println(e);
			}
			try {
				fromStream.close();
				csvStream.flush();
				csvStream.close();
			} catch (IOException e) {
				System.err.println(e);
			}

		}
		System.out.println("Done.");
	}

	private File[] getEventFiles(File path) {
		return path.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith("evn");
			}
		});
	}

	static private File getDir(String dir) {
		File rval = new File(dir);
		if (rval.exists()) {
			if (!rval.isDirectory())
				rval = rval.getParentFile();
			return rval;
		}
		return null;
	}

	/**
	 * @param args one argument--directory where event files are to be checked
	 */
	public static void main(String[] args) {
		boolean printHelp = false;
		if (args.length >= 2) {
			File f1 = getDir(args[0]);
			File f2 = getDir(args[1]);
			if (f1 == null || f2 == null) {
				printHelp = true;
			} else {
				new CheckEventFiles(f1, f2);
			}
		} else {
			printHelp = true;
		}
		if (printHelp) {
			System.out.println("CheckEventFiles needs 2 arguments:");
			System.out.println("\t1st arg: directory containing event files");
			System.out.println("\t2nd arg: directory for output files");
		}
	}
}
