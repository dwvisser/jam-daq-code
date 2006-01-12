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

import jam.global.LoggerConfig;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This program takes the first data block of a specified event file, removes
 * it, and appends it to the end of a second specified event file. The first
 * file is typically run number "x" and the second file is for run number "x-1".
 * 
 * @author <a href="mailto:dale@visser.name">Dale W. Visser</a>
 */
public class FixEventFile {

	static {
		new LoggerConfig();
	}

	private static final Logger LOGGER = Logger.getLogger("jam.util");

	/**
	 * Launches the task to fix an event file.
	 * 
	 * @param args
	 *            input file name
	 */
	public static void main(final String[] args) {
		if (args.length == 0) {
			LOGGER.info("Supply an input file argument.");
			LOGGER.info("The input file format is as follows.");
			LOGGER.info("Line 1: Directory containing input event files");
			LOGGER.info("Line 2: Experiment name");
			LOGGER
					.info("Line 3: Directory for output event files, must be different than input");
			LOGGER
					.info("Line 4-: List of run numbers of files containing end-of-run as first buffer");
		} else {
			final File input = new File(args[0]);
			if (input.exists()) {
				if (input.isFile()) {
					new FixEventFile(input);
				} else {
					LOGGER.severe("Need to supply a file, not a directory!");
				}
			} else {
				LOGGER.severe("File for given filename does not exist!");
			}
		}
	}

	private transient File directory;

	private transient String expName;

	private transient File outDir;

	private transient Set<Integer> runNumberSet; // Collections class for
													// unique,sorted elements

	FixEventFile(File inputFile) {
		super();
		/*
		 * the input file has the format: event directory experiment name output
		 * directory list of run#'s
		 */
		try {
			FileReader fileReader = new FileReader(inputFile);
			StreamTokenizer tokenizer = new StreamTokenizer(fileReader);
			tokenizer.eolIsSignificant(false);
			tokenizer.slashSlashComments(true);
			tokenizer.slashStarComments(true);
			tokenizer.quoteChar('\"');
			tokenizer.nextToken();
			String dir = tokenizer.sval;
			LOGGER.info("Directory containing input event files: " + dir);
			final FileUtilities fileUtil = FileUtilities.getInstance();
			directory = fileUtil.getDir(dir);
			tokenizer.nextToken();
			expName = tokenizer.sval;
			LOGGER.info("Experiment name: " + expName);
			tokenizer.nextToken();
			dir = tokenizer.sval;
			LOGGER.info("Directory containing output event files: " + dir);
			outDir = fileUtil.getDir(dir);
			runNumberSet = new TreeSet<Integer>();
			LOGGER.info("Run numbers to move 1st buffer from: ");
			do {
				tokenizer.nextToken();
				if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
					int temp = (int) tokenizer.nval;
					LOGGER.info(temp + " ");
					runNumberSet.add(temp);
				}
			} while (tokenizer.ttype != StreamTokenizer.TT_EOF);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		if (directory.equals(outDir)) {
			LOGGER
					.severe("Can't have input directory the same as output directory!");
		} else {
			processFiles();
		}
	}

	private void copyFile(final File src, final File dest) {
		final byte[] block = new byte[16 * 1024];
		LOGGER.info("Copying " + src.getPath() + " to " + dest.getPath());
		try {
			final BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(src));
			final BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(dest));
			// copy rest of data blocks to mod file
			int numBytesRead = bis.read(block);
			while (numBytesRead != -1) {
				bos.write(block, 0, numBytesRead);
				numBytesRead = bis.read(block);
			}
			bis.close();
			bos.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private void processCurrentRun(final int currentRun) {
		final String evn = ".evn";
		final int priorRun = currentRun - 1;
		final File priorSourceFile = new File(directory, expName + priorRun
				+ evn);
		final File priorDestFile = new File(outDir, expName + priorRun + evn);
		if (!priorDestFile.exists()) {
			copyFile(priorSourceFile, priorDestFile);
		}
		final File currentSourceFile = new File(directory, expName + currentRun
				+ evn);
		final File currentDestFile = new File(outDir, expName + currentRun
				+ evn);
		LOGGER.info("Pulling first data block from "
				+ currentSourceFile.getPath() + " and appending to "
				+ priorDestFile.getPath()
				+ ", and file with block removed will be called "
				+ currentDestFile.getPath());
		FileInputStream fromFile;
		FileOutputStream appendFile, modFile;
		try {
			fromFile = new FileInputStream(currentSourceFile);
			appendFile = new FileOutputStream(priorDestFile, true);
			modFile = new FileOutputStream(currentDestFile);

			final BufferedInputStream fromStream = new BufferedInputStream(
					fromFile);
			final BufferedOutputStream modStream = new BufferedOutputStream(
					modFile);
			final BufferedOutputStream appendStream = new BufferedOutputStream(
					appendFile);
			final byte[] header = new byte[256];
			final int bufferSize = 8192;
			final byte[] dataBlock = new byte[bufferSize];
			// copy header from input stream to mod file
			fromStream.read(header);
			modStream.write(header);
			// copy first data block to end of append file, close append
			// file
			fromStream.read(dataBlock);
			appendStream.write(dataBlock);
			appendStream.close();
			// copy rest of data blocks to mod file
			int numBytesRead = fromStream.read(dataBlock);
			while (numBytesRead != -1) {
				modStream.write(dataBlock, 0, numBytesRead);
				numBytesRead = fromStream.read(dataBlock);
			}
			fromStream.close();
			modStream.close();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
		LOGGER.info("Done with run " + currentRun);
	}

	private void processFiles() {
		for (int currentRun : runNumberSet) {
			processCurrentRun(currentRun);
		} // for
		LOGGER.info("Done with everything.");
	}
}
