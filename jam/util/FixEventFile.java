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
import java.util.*;

/**
 * This program takes the first data block of a specified event file,
 * removes it, and appends it to the end of a second specified
 * event file.  The first file is typically run number "x" and 
 * the second file is for run number "x-1".
 * 
 * @author <a href="mailto:dale@visser.name">Dale W. Visser</a>
 */
public class FixEventFile {

	File directory;
	String expName;
	File outDir;
	Set runNumberSet; //Collections class for unique,sorted elements

	FixEventFile(File inputFile) {
		/* the input file has the format:
		  		event directory
		  		experiment name
		  		output directory
		  		list of run#'s
		 */
		try {
			FileReader fr = new FileReader(inputFile);
			StreamTokenizer st = new StreamTokenizer(fr);
			st.eolIsSignificant(false);
			st.slashSlashComments(true);
			st.slashStarComments(true);
			st.quoteChar('\"');
			st.nextToken();
			String _dir = st.sval;
			System.out.println("Directory containing input event files: "+_dir);
			directory = getDir(_dir);
			st.nextToken();
			expName = st.sval;
			System.out.println("Experiment name: "+expName);
			st.nextToken();
			_dir = st.sval;
			System.out.println("Directory containing output event files: "+_dir);
			outDir = getDir(_dir);
			runNumberSet = new TreeSet();
			System.out.print("Run numbers to move 1st buffer from: ");
			do {
				st.nextToken();
				if (st.ttype == StreamTokenizer.TT_NUMBER) {
					int temp = (int) st.nval;
					System.out.print(temp+" ");
					runNumberSet.add(new Integer(temp));
				}
			} while (st.ttype != StreamTokenizer.TT_EOF);
			System.out.println();
		} catch (IOException e) {
			System.err.println(e);
		}
		if (directory.equals(outDir)) {
			System.err.println(
				"Can't have input directory the same as output directory!");
		} else {
			processFiles();
		}
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

	private void copyFile(File from, File to) {
		byte[] block = new byte[16 * 1024];
		System.out.println("Copying " + from.getPath() + " to " + to.getPath());
		try {
			BufferedInputStream bis =
				new BufferedInputStream(new FileInputStream(from));
			BufferedOutputStream bos =
				new BufferedOutputStream(new FileOutputStream(to));
			//copy rest of data blocks to mod file
			int numBytesRead = bis.read(block);
			while (numBytesRead != -1) {
				bos.write(block, 0, numBytesRead);
				numBytesRead = bis.read(block);
			}
			bis.close();
			bos.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private void processFiles() {
		String evn = ".evn";
		final int bufferSize = 8192;
		//make array of run numbers running from lowest to highest run number
		int[] runNumberList = new int[runNumberSet.size()];
		int i = 0;
		for (Iterator it = runNumberSet.iterator(); it.hasNext(); i++) {
			runNumberList[i] = ((Integer) it.next()).intValue();
		}
		for (i = 0; i < runNumberList.length; i++) {
			int currentRun = runNumberList[i];
			int priorRun = currentRun - 1;
			File priorSourceFile =
				new File(directory, expName + priorRun + evn);
			File priorDestFile = new File(outDir, expName + priorRun + evn);
			if (!priorDestFile.exists())
				copyFile(priorSourceFile, priorDestFile);
			File currentSourceFile =
				new File(directory, expName + currentRun + evn);
			File currentDestFile = new File(outDir, expName + currentRun + evn);

			System.out.println(
				"Pulling first data block from "
					+ currentSourceFile.getPath()
					+ " and appending to "
					+ priorDestFile.getPath()
					+ ", and file with block removed will be called "
					+ currentDestFile.getPath());
			FileInputStream fromFile;
			FileOutputStream appendFile, modFile;
			try {
				fromFile = new FileInputStream(currentSourceFile);
				appendFile = new FileOutputStream(priorDestFile, true);
				modFile = new FileOutputStream(currentDestFile);

				BufferedInputStream fromStream =
					new BufferedInputStream(fromFile);
				BufferedOutputStream modStream =
					new BufferedOutputStream(modFile);
				BufferedOutputStream appendStream =
					new BufferedOutputStream(appendFile);

				byte[] header = new byte[256];
				byte[] dataBlock = new byte[bufferSize];

				//copy header from input stream to mod file
				fromStream.read(header);
				modStream.write(header);

				//copy first data block to end of append file, close append file
				fromStream.read(dataBlock);
				appendStream.write(dataBlock);
				appendStream.close();

				//copy rest of data blocks to mod file
				int numBytesRead = fromStream.read(dataBlock);
				while (numBytesRead != -1) {
					modStream.write(dataBlock, 0, numBytesRead);
					numBytesRead = fromStream.read(dataBlock);
				}
				fromStream.close();
				modStream.close();
			} catch (IOException e) {
				System.err.println(e);
			}
			System.out.println("Done with run "+currentRun);
		} //for
		System.out.println("Done with everything.");
	}

	public static void main(String[] args) {
		if (args.length==0) {
			System.out.println("Supply an input file argument.");
			System.out.println("The input file format is as follows.");
			System.out.println("Line 1: Directory containing input event files");
			System.out.println("Line 2: Experiment name");
			System.out.println("Line 3: Directory for output event files, must be different than input");
			System.out.println("Line 4-: List of run numbers of files containing end-of-run as first buffer");
		} else {
			File input = new File(args[0]);
			if (input.exists()){
				if (input.isFile()) {
					new FixEventFile(input);
				} else {
					System.err.println("Need to supply a file, not a directory!");
				}
			} else {
				System.err.println("File for given filename does not exist!");
			}
		}
	}
}
