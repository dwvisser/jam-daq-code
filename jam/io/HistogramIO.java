/*
*/
package jam.io;
import java.io.*;
import java.awt.*;
import java.util.Iterator;
import jam.util.*;
import jam.data.*;
import jam.global.*;
import javax.swing.*;

/**
 * Class to read in and write out histograms
 * writes out files in jam histogram format
 * Methods to open files an close files are used
 * by import and export. 
 *
 * @version	0.5 April 98
 * @author 	Dale Visser and Ken Swartz
 * @see         jam.JamMain
 * @since       JDK1.1
 */
public class HistogramIO implements FilenameFilter {

	final static int OPEN = 0;
	final static int SAVE = 1;

	private static final String MAGIC_WORD_VER_01 = "JHF VER   01";
	private static final String MAGIC_WORD_VER_02 = "JHF VER   02";
	private static final int MAGIC_WORD_LENGTH = 12;

	final static int BUFFER_SIZE = 256 * 256 * 4;
	//size of one 2D histogram int is 4 bytes

	final static int HIST_NAME_LENGTH = 15;
	final static int HIST_TITLE_LENGTH = 50;

	public final static int OPEN_MODE = 1;
	public final static int RELOAD_MODE = 2;

	private Frame frame;
	private MessageHandler msgHandler;
	/** 
	 * write options
	 */
	private boolean writeHistograms = true;
	private boolean writeGates = true;
	private boolean writeScalers = true;

	File fileOpen;
	File fileSave;

	private String fileName;
	private String directoryName;
	BufferedInputStream buffin;

	int[] counts;
	int[][] counts2d;

	/**
	 * Class constructor handed references to the main class and message handler.
	 */
	public HistogramIO(Frame frame, MessageHandler msgHandler) {
		this.frame = frame;
		this.msgHandler = msgHandler;
		directoryName = JamProperties.getPropString(JamProperties.HIST_PATH);
	}

	/**
	 * Neccessary to satisfy <code>FilenameFilter</code> requirements.
	 */
	public boolean accept(File file, String string) {
		return true;
	}

	/**
	 * Write out a JHF file 
	 * No file given so promt user for file
	 * 
	 * @exception ImpExpException when the method can't write file
	 */
	public void writeJHFFile() throws ImpExpException {
		JFileChooser jfile = new JFileChooser(fileSave);
		jfile.setFileFilter(new ExtensionFileFilter("jhf"));
		int option = jfile.showSaveDialog(frame);
		// dont do anything if it was cancel
		if (option == JFileChooser.APPROVE_OPTION
			&& jfile.getSelectedFile() != null) {
			fileSave = jfile.getSelectedFile();
			writeJHFFile(fileSave);
		}
	}

	/**
	 * Write out a JHF file.
	 */
	public void writeJHFFile(File fileSave) {
		this.fileSave = fileSave;
		try {
			msgHandler.messageOut(
				"Save Version 01 File:  " + fileSave.getName(),
				MessageHandler.NEW);
			writeJHFData(fileSave);
		} catch (ImpExpException iee) {
			msgHandler.errorOutln(
				"writeJHFFile() ImpExpException: " + iee.getMessage());
		}
	}

	/**
	 * Read a JHF file--no file given so prompt for file.
	 * 
	 * @exception   ImpExpException    writes error messages out to msgHandler
	 */
	public boolean readJHFFile() throws ImpExpException {
		boolean outF = false;
		JFileChooser jfile = new JFileChooser(fileSave);
		jfile.setFileFilter(new ExtensionFileFilter("jhf"));
		int option = jfile.showOpenDialog(frame);
		// dont do anything if it was cancel
		if (option == JFileChooser.APPROVE_OPTION
			&& jfile.getSelectedFile() != null) {
			fileOpen = jfile.getSelectedFile();
			outF = readJHFFile(fileOpen);
		} else { //dialog didn't return a file
			outF = false;
		}
		return outF;
	}

	/**
	 * Read a JHF file, file given.
	 * 
	 * @param	    fileOpen	    file to read from
	 * @exception   ImpExpException    writes error messages out to msgHandler
	 */
	public boolean readJHFFile(File fileOpen) throws ImpExpException {
		this.fileOpen = fileOpen;
		boolean success;
		try {
			DataBase.getInstance().clearAllLists();
			msgHandler.messageOut(
				"Open " + fileOpen.getName(),
				MessageHandler.NEW);
			readJHFData(fileOpen, OPEN_MODE);
			success = true;
			/*} catch(FileNotFoundException exc){
				msgHandler.errorOutln("File not Found");
				success = false;*/
		} catch (IOException ioe) {
			msgHandler.errorOutln(
				"Reading JHF file: IOException: " + ioe.getMessage());
			success = false;
		}
		return success;
	}

	/**
	 * Reload a file. This means only read in 
	 * histograms, gates and scalers defined
	 */
	public boolean reloadJHFFile() throws ImpExpException {
		/*fileOpen=null;	
		boolean success;
		int state=FileDialog.LOAD;	
		fileOpen=getFile("Reload JHF file", "*.jhf", state);
		//dont go on if cancel
		if (fileOpen!=null){	   
			success=reloadJHFFile(fileOpen);
		} else {
			success=false;
		}	    
		return success;*/
		boolean outF = false;
		JFileChooser jfile = new JFileChooser(fileSave);
		jfile.setFileFilter(new ExtensionFileFilter("jhf"));
		int option = jfile.showOpenDialog(frame);
		// dont do anything if it was cancel
		if (option == JFileChooser.APPROVE_OPTION
			&& jfile.getSelectedFile() != null) {
			fileOpen = jfile.getSelectedFile();
			outF = reloadJHFFile(fileOpen);
		} else { //dialog didn't return a file
			outF = false;
		}
		return outF;
	}

	/**
	 * Reload a file. This means only read in 
	 * histograms, gates and scalers defined
	 *
	 * no file given so prompt for file 
	 */
	public boolean reloadJHFFile(File fileOpen) {

		this.fileOpen = fileOpen;
		boolean success;

		try {
			msgHandler.messageOut(
				"Reload " + fileOpen.getName(),
				MessageHandler.NEW);
			readJHFData(fileOpen, RELOAD_MODE);
			success = true;

		} catch (FileNotFoundException exc) {
			msgHandler.errorOutln("File not Found");
			success = false;
		} catch (ImpExpException iee) {
			msgHandler.errorOutln(
				"Reloading JHF file ImpExpException: " + iee.getMessage());
			success = false;
		}
		return success;
	}

	/**
	 * Get a file, to open from
	 */
	public File getFileOpen(String msg, String extension)
		throws ImpExpException {
		return getFile(msg, extension, FileDialog.LOAD);
	}

	/**
	 * Get a file, to save to
	 */
	public File getFileSave(String msg, String extension)
		throws ImpExpException {
		return getFile(msg, extension, FileDialog.SAVE);
	}

	/**
	 *  Get a file, 
	 *	 See books AWT reference page 245 and
	 *   Nutshell Java examples page 162
	 */
	public File getFile(String msg, String extension, int state)
		throws ImpExpException {
		File fileIn = null;//default return value
		try {
			FileDialog fd = new FileDialog(frame, msg, state);
			/* use previous file and directory as default */
			if ((fileName) != null) {
				fd.setFile(fileName);
			} else {
				fd.setFile(extension);
			}
			if (directoryName != null) {
				fd.setDirectory(directoryName);
			}
			fd.setFilenameFilter(this);
			/* show file dialog box to get file */   	    
			fd.show();
			directoryName = fd.getDirectory(); //save current directory
			fileName = fd.getFile();
			fd.dispose();

			if (fileName != null) {
				fileName =
					FileUtilities.setExtension(
						fileName,
						extension,
						FileUtilities.FORCE);
				fileIn = new File(directoryName, fileName);
			} 
			return fileIn;
		} catch (UtilException ue) {
			throw new ImpExpException(
				"Problem calling setExtension(): " + ue.getMessage());
		}
	}

	/** 
	 * Sets separately which data writeJHFData should actually output.  Not writing histograms
	 * when you are saving tape data can significantly save time when you have many 2-d spectra.
	 *
	 * @param	his	if true, Histograms will be written
	 * @param	gate	if true, Gates will be written
	 * @param	scalers	if trye, scaler values will be written
	 */
	public void setWriteOptions(boolean his, boolean gate, boolean scalers) {
		writeHistograms = his;
		writeGates = gate;
		writeScalers = scalers;
	}

	/**
	 * Version 01-decided to write at beginning of corresponding sections 
	 * the number of histograms,number of gates, etc.
	 * commented out for runs until opportunity to debug fully
	 * DWV 5/20/98
	 * uncommented for futher work
	 * DWV 6/1/98
	 * histogram name only HIST_NAME_LENGTH =15 char long 
	 * histogram title only HIST_TITLE_LENGTH=50 char long     
	 */
	private void writeJHFData(File fileSave) throws ImpExpException {
		Histogram hist;
		Iterator allHistograms, allGates, allScalers;
		int numHist, numGates, numScalers;
		int[] gate1d;
		final StringUtilities su=StringUtilities.instance();
		try {
			FileOutputStream fileStream = new FileOutputStream(fileSave);
			BufferedOutputStream bos =
				new BufferedOutputStream(fileStream, BUFFER_SIZE);
			DataOutputStream dos = new DataOutputStream(bos);

			allHistograms = Histogram.getHistogramList().iterator();
			numHist = Histogram.getHistogramList().size();
			dos.writeChars(MAGIC_WORD_VER_02);
			if (!writeHistograms) {
				numHist = 0;
			}
			dos.writeInt(numHist);

			//write out histograms
			if (writeHistograms) {
				msgHandler.messageOut(" histograms");
				while (allHistograms.hasNext()) {
					hist = ((Histogram) allHistograms.next());
					String name =
						su.makeLength(
							hist.getName(),
							HIST_NAME_LENGTH);
					for (int i = 0; i < HIST_NAME_LENGTH; i++) {
						dos.writeChar(name.charAt(i));
					}
					dos.writeInt(hist.getNumber());
					String title =
						su.makeLength(
							hist.getTitle(),
							HIST_TITLE_LENGTH);
					for (int i = 0; i < HIST_TITLE_LENGTH; i++) {
						dos.writeChar(title.charAt(i));
					}
					int type = hist.getType();
					int sizeX = hist.getSizeX();
					int sizeY = hist.getSizeY();
					dos.writeInt(type);
					dos.writeInt(sizeX);
					dos.writeInt(sizeY);
					//two dimensional histogram
					if (type == Histogram.TWO_DIM_INT) {
						int counts2d[][] = (int[][]) hist.getCounts();
						for (int i = 0; i < sizeX; i++) {
							for (int j = 0; j < sizeY; j++) {
								dos.writeInt(counts2d[i][j]);
							}
						}
						dos.flush();
						//one dimensional histogram
					} else {
						int counts[] = (int[]) hist.getCounts();
						for (int i = 0; i < sizeX; i++) {
							dos.writeInt(counts[i]);
						}
						dos.flush();
					}
					msgHandler.messageOut(" .");
				}
			}

			//write out gates
			allGates = Gate.getGateList().iterator();
			numGates = Gate.getGateList().size(); //FIXME

			if (!writeGates) {
				numGates = 0;
			}
			dos.writeInt(numGates);

			if (numGates > 0) {
				msgHandler.messageOut(" gates");
				while (allGates.hasNext()) {
					Gate gate = (Gate) (allGates.next());
					int gateNameLength = gate.getName().length();
					dos.writeInt(gateNameLength);
					dos.writeChars(gate.getName());
					dos.writeInt(gate.getNumber());
					int gateType = gate.getType();
					dos.writeInt(gateType);
					dos.writeBoolean(gate.isDefined());
					if (gate.isDefined()) {
						if (gateType == Gate.ONE_DIMENSION) {
							gate1d = gate.getLimits1d();
							for (int i = 0; i < 2; i++) {
								dos.writeInt(gate1d[i]);
							}
						} else { //other choice - 2D
							Polygon bananaGate = gate.getBananaGate();
							dos.writeInt(bananaGate.npoints);
							for (int i = 0; i < bananaGate.npoints; i++) {
								dos.writeInt(bananaGate.xpoints[i]);
								dos.writeInt(bananaGate.ypoints[i]);
							}
						}

					}
					msgHandler.messageOut(" .");
				}
			}
			/* write out scalers */	  
			final java.util.List scalerList = Scaler.getScalerList();     
			allScalers = scalerList.iterator();
			numScalers = scalerList.size();
			if (writeScalers) {
				dos.writeInt(numScalers);
				msgHandler.messageOut(" scalers");
				if (numScalers > 0) {
					while (allScalers.hasNext()) {
						Scaler scaler = ((Scaler) allScalers.next());
						String scalerName = scaler.getName();
						dos.writeInt(scalerName.length()); //length of name
						dos.writeChars(scalerName); //name
						dos.writeInt(scaler.getNumber()); //ID#
						dos.writeInt(scaler.getValue()); //scaler value
						msgHandler.messageOut(" .");
					}
				}
			} else {
				numScalers = 0;
				dos.writeInt(numScalers);
			}
			dos.flush();
			fileStream.close();
			//reset the default options
			setWriteOptions(true, true, true);
			msgHandler.messageOut(" done!", MessageHandler.END);
		} catch (IOException ioe) {
			throw new ImpExpException(
				"Problem writing JHF file: " + ioe.getMessage());
		} catch (DataException de) {
			throw new ImpExpException(
				"Problem accessing data object in memory: " + de.getMessage());
		}
	}

	/**
	 * Read in a hdf file 
	 */
	private void readJHFData(File fileOpen, int mode)
		throws FileNotFoundException, ImpExpException {
		boolean eof;
		try {
			FileInputStream fileStream = new FileInputStream(fileOpen);
			BufferedInputStream bis =
				new BufferedInputStream(fileStream, BUFFER_SIZE);
			DataInputStream dis = new DataInputStream(bis);

			eof = !(dis.available() > 0);
			String name = new String();
			if (!eof) {
				for (int i = 0; i < MAGIC_WORD_LENGTH; i++) {
					name = name + dis.readChar();
				}
			}
			dis.close();
			if (name.equals(MAGIC_WORD_VER_01)) {
				msgHandler.messageOut(" version 1");
				readJHFDataV01(mode);
			} else if (name.equals(MAGIC_WORD_VER_02)) {
				msgHandler.messageOut(" version 2");
				readJHFDataV02(mode);
			} else { //VER_00
				msgHandler.messageOut(" version 0");
				readJHFDataV00(mode);
			}

		} catch (IOException ioe) {
			throw new ImpExpException(
				"Problem reading JHF file: " + ioe.getMessage());
		}
	}
	/**
	 * read in a jhf file
	 * size of historam is sizeX, sizeY
	 *
	 */
	private void readJHFDataV02(int mode)
		throws FileNotFoundException, ImpExpException {
		Histogram hist;
		boolean eof;
		int numHist = 0;
		Gate gate;
		Scaler scaler;
		try {
			FileInputStream fis = new FileInputStream(fileOpen);
			BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE);
			DataInputStream dis = new DataInputStream(bis);

			eof = !(dis.available() > 0);
			String name = new String();
			if (!eof) {
				//read in key again
				for (int i = 0; i < MAGIC_WORD_LENGTH; i++) {
					dis.readChar();
				}
				numHist = dis.readInt();
				msgHandler.messageOut(" histograms " + numHist);
				for (int i = 0; i < numHist; i++) {
					name = new String();
					//read in magic word
					for (int j = 0; j < MAGIC_WORD_LENGTH; j++) {
						name = name + dis.readChar();
					}
					int number = dis.readInt();
					String title = new String();
					for (int j = 0; j < HIST_TITLE_LENGTH; j++) {
						title = title + dis.readChar();
					}
					int type = dis.readInt();
					int sizeX = dis.readInt();
					int sizeY = dis.readInt();
					if (type == Histogram.TWO_DIM_INT) {
						int[][] counts2D = new int[sizeX][sizeY];
						for (int j = 0; j < sizeX; j++) {
							for (int k = 0; k < sizeY; k++) {
								counts2D[j][k] = dis.readInt();
							}
						}
						if (mode == OPEN_MODE) {
							hist = new Histogram(name, title, counts2D);
							hist.setNumber(number);
							msgHandler.messageOut(" .");
						} else { //RELOAD_MODE
							hist = Histogram.getHistogram(name);
							if (hist != null) {
								hist.setCounts(counts2D);
								msgHandler.messageOut(" .");
							} else {
								msgHandler.messageOut("X");
							}

						}
					} else {
						int[] counts = new int[sizeX];
						for (int j = 0; j < sizeX; j++) {
							counts[j] = dis.readInt();
						}
						if (mode == OPEN_MODE) {
							//try {
							hist = new Histogram(name, title, counts);
							hist.setNumber(number);
							msgHandler.messageOut(" .");
							//} catch (DataException de) {
							//msgHandler.messageOut("Err: '"+name+"'");
							//}
						} else { //RELOAD_MODE
							hist = Histogram.getHistogram(name);
							if (hist != null) {
								hist.setCounts(counts);
								msgHandler.messageOut(" .");
							} else {
								msgHandler.messageOut("X");
							}
						}
					}
				}
				//read in gates
				int numGates = dis.readInt();
				if (numGates > 0) {
					//read past gates, but don't actually do anything with them
					msgHandler.messageOut(" gates");
					for (int i = 0; i < numGates; i++) {
						String gateName = new String();
						int gateNameLength = dis.readInt();
						for (int j = 0; j < gateNameLength; j++) {
							gateName = gateName + dis.readChar();
						}
						dis.readInt(); //skip gate number
						int gateType = dis.readInt();
						boolean isDefined = dis.readBoolean();

						if (isDefined) {
							if (gateType == Gate.ONE_DIMENSION) {
								int lowerLim = dis.readInt();
								int upperLim = dis.readInt();
								if (mode == RELOAD_MODE) {
									gate = Gate.getGate(gateName);
									if (gate != null) {
										hist = gate.getHistogram();
										gate.setLimits(lowerLim, upperLim);
										msgHandler.messageOut(" .");
									} else {
										msgHandler.messageOut("X");
									}
								}
							} else { //2D
								Polygon bananaGate = new Polygon();
								int npoints = dis.readInt();
								for (int j = 0; j < npoints; j++) {
									int xpoint = dis.readInt();
									int ypoint = dis.readInt();
									bananaGate.addPoint(xpoint, ypoint);
								}
								if (mode == RELOAD_MODE) {
									gate = Gate.getGate(gateName);
									if (gate != null) {
										hist = gate.getHistogram();
										gate.setLimits(bananaGate);
										msgHandler.messageOut(" .");
									} else {
										msgHandler.messageOut("X");
									}

								}
							}
						}

					}
				}
				//read in scalers
				int numScalers = dis.readInt();
				if (numScalers > 0) {
					msgHandler.messageOut(" scalers");
					for (int j = 0; j < numScalers; j++) {
						int nameLength = dis.readInt();
						String scalerName = new String();
						for (int k = 0; k < nameLength; k++) {
							scalerName = scalerName + dis.readChar();
						}
						if (mode == OPEN_MODE) {
							scaler = new Scaler((scalerName), j);
						} else { //RELOAD_MODE
							scaler = Scaler.getScaler(scalerName);
						}
						dis.readInt(); //skip scaler number
						int scalerValue = dis.readInt();
						scaler.setValue(scalerValue);
						msgHandler.messageOut(" .");
					}
				}
				dis.close();
				msgHandler.messageOut(" done!", MessageHandler.END);
			}
		} catch (IOException ioe) {
			throw new ImpExpException(
				"Problem reading JHF file: " + ioe.getMessage());
		} catch (DataException de) {
			throw new ImpExpException(
				"Problem creating data object: " + de.getMessage());
		}
	}

	/**
	 * read in a jhf file
	 * size of histogram is sizeX+1 sizeY+1
	 *
	 */
	private void readJHFDataV01(int mode) throws ImpExpException {
		Histogram hist;
		boolean eof;
		int numHist = 0;
		Gate gate;
		Scaler scaler;
		try {
			FileInputStream fis = new FileInputStream(fileOpen);
			BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE);
			DataInputStream dis = new DataInputStream(bis);

			eof = !(dis.available() > 0);
			String name = new String();
			if (!eof) {
				for (int i = 0; i < HIST_NAME_LENGTH; i++) {
					dis.readChar();
				}
				numHist = dis.readInt();
				msgHandler.messageOut(" histograms");
				for (int i = 0; i < numHist; i++) {
					name = new String();
					//read in magic word
					for (int j = 0; j < MAGIC_WORD_LENGTH; j++) {
						name = name + dis.readChar();
					}
					int number = dis.readInt();
					String title = new String();
					for (int j = 0; j < HIST_TITLE_LENGTH; j++) {
						title = title + dis.readChar();
					}
					int type = dis.readInt();
					int sizeX = dis.readInt() + 1;
					int sizeY = dis.readInt() + 1;
					if (type == Histogram.TWO_DIM_INT) {
						int[][] counts2D = new int[sizeX][sizeY];
						for (int j = 0; j < sizeX; j++) {
							for (int k = 0; k < sizeY; k++) {
								counts2D[j][k] = dis.readInt();
							}
						}
						if (mode == OPEN_MODE) {
							hist = new Histogram(name, title, counts2D);
							hist.setNumber(number);
							msgHandler.messageOut(" .");
						} else { //RELOAD_MODE
							hist = Histogram.getHistogram(name);
							hist.setCounts(counts2D);
						}
					} else {
						int[] counts = new int[sizeX];
						for (int j = 0; j < sizeX; j++) {
							counts[j] = dis.readInt();
						}
						if (mode == OPEN_MODE) {
							hist = new Histogram(name, title, counts);
							hist.setNumber(number);
							msgHandler.messageOut(" .");
						} else { //RELOAD_MODE
							hist = Histogram.getHistogram(name);
							hist.setCounts(counts);
						}
					}
				}
				int numGates = dis.readInt();
				if (numGates > 0) {
					//read past gates, but don't actually do anything with them
					msgHandler.messageOut(" gates");
					for (int i = 0; i < numGates; i++) {
						String gateName = new String();
						int gateNameLength = dis.readInt();
						for (int j = 0; j < gateNameLength; j++) {
							gateName = gateName + dis.readChar();
						}
						dis.readInt(); //skip gate Number
						int gateType = dis.readInt();

						boolean isDefined = dis.readBoolean();

						if (isDefined) {
							if (gateType == Gate.ONE_DIMENSION) {
								int lowerLim = dis.readInt();
								int upperLim = dis.readInt();
								if (mode == RELOAD_MODE) {
									gate = Gate.getGate(gateName);
									hist = gate.getHistogram();
									gate.setLimits(lowerLim, upperLim);
								}
							} else { //2D
								Polygon bananaGate = new Polygon();
								int npoints = dis.readInt();
								for (int j = 0; j < npoints; j++) {
									int xpoint = dis.readInt();
									int ypoint = dis.readInt();
									bananaGate.addPoint(xpoint, ypoint);
								}
								if (mode == RELOAD_MODE) {
									gate = Gate.getGate(gateName);
									hist = gate.getHistogram();
									gate.setLimits(bananaGate);
								}
							}
						}
						msgHandler.messageOut(" .");
					}
				}
				int numScalers = dis.readInt();
				if (numScalers > 0) {
					msgHandler.messageOut(" scalers");
					for (int j = 0; j < numScalers; j++) {
						int nameLength = dis.readInt();
						String scalerName = new String();
						for (int k = 0; k < nameLength; k++) {
							scalerName = scalerName + dis.readChar();
						}
						if (mode == OPEN_MODE) {
							scaler = new Scaler((scalerName), j);
						} else { //RELOAD_MODE
							scaler = Scaler.getScaler(scalerName);
						}
						dis.readInt(); //skip scaler number
						int scalerValue = dis.readInt();
						scaler.setValue(scalerValue);
						msgHandler.messageOut(" .");
					}
				}
				dis.close();
				msgHandler.messageOut(" done!", MessageHandler.END);
			}
		} catch (IOException ioe) {
			throw new ImpExpException(
				"Problem reading JHF file: " + ioe.getMessage());
		} catch (DataException de) {
			throw new ImpExpException(
				"Problem creating data object: " + de.getMessage());
		}
	}

	private void readJHFDataV00(int mode) throws ImpExpException {

		Histogram hist;
		boolean eof;
		int numHist = 0;
		int[] counts;
		int[][] counts2D;
		final StringUtilities su=StringUtilities.instance();
		try {
			FileInputStream fis = new FileInputStream(fileOpen);
			BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE);
			DataInputStream dis = new DataInputStream(bis);

			eof = !(dis.available() > 0);
			String name = new String();
			while (!eof) {
				for (int i = 0; i < MAGIC_WORD_LENGTH; i++) {
					name = name + dis.readChar();
				}
				name = su.makeLength(name, HIST_NAME_LENGTH);
				int number = dis.readInt();
				String title = new String();
				for (int i = 0; i < 50; i++) {
					title = title + dis.readChar();
				}
				int type = dis.readInt();
				int sizeX = dis.readInt();
				int sizeY = dis.readInt();
				if (type == Histogram.TWO_DIM_INT) {
					counts2D = new int[sizeX][sizeY];
					for (int i = 0; i < sizeX; i++) {
						for (int j = 0; j < sizeY; j++) {
							counts2D[i][j] = dis.readInt();
						}
					}
					if (mode == OPEN_MODE) {
						hist = new Histogram(name, title, counts2D);
						hist.setNumber(number);
						msgHandler.messageOut(" .");
					} else { //RELOAD_MODE
						hist = Histogram.getHistogram(name);
						hist.setCounts(counts2D);
						msgHandler.messageOut(" .");
					}
				} else {
					counts = new int[sizeX];
					for (int i = 0; i < sizeX; i++) {
						counts[i] = dis.readInt();
					}
					if (mode == OPEN_MODE) {
						hist = new Histogram(name, title, counts);
						hist.setNumber(number);
						msgHandler.messageOut(" .");
					} else { //RELOAD_MODE
						hist = Histogram.getHistogram(name);
						hist.setCounts(counts);
						msgHandler.messageOut(" .");
					}
				}
				numHist++;
				eof = !(dis.available() > 0);
			}
			msgHandler.messageOut(
				", " + numHist + " histograms.",
				MessageHandler.END);
			dis.close();
		} catch (IOException ioe) {
			throw new ImpExpException(
				"Problem reading JHF file: " + ioe.getMessage());
		}

	}

	/**
	 * The name of the file opened
	 */
	public String getFileNameOpen() {
		return fileOpen.getName();
	}

	/**
	 * The name of the file saved
	 */
	public String getFileNameSave() {
		return fileOpen.getName();
	}
}
