package jam.io;
import jam.data.Histogram;
import jam.global.MessageHandler;
import java.awt.Frame;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Imports and Exports Spectra (Histograms) using the
 * SPE format, this is used by RADWARE gf2.
 *
 * @version 0.5
 * @author Ken Swartz
 */
public class ImpExpSPE extends ImpExp {

	static final int NAME_LENGTH = 8;
	static final int MAX_SIZE = 8192;
	static final int MAGIC_WORD = 24;

	public ImpExpSPE(Frame frame, MessageHandler msgHandler) {
		super(frame, msgHandler);

	}

	public ImpExpSPE() {
		super();
	}

	public String getFileExtension() {
		return ".spe";
	}

	public String getFormatDescription() {
		return "Radware gf3";
	}

	/**
	 * Prompts for and opens a file.
	 *
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> display on the MessageHandler
	 */
	public boolean openFile() throws ImpExpException {
		return openFile("Import RadWare .spe file ", "spe");
	}

	/**
	 * Prompts for file name and saves.
	 *
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> display on the MessageHandler
	 */
	public void saveFile(Histogram hist) throws ImpExpException {
		if (hist.getDimensionality() == 2) {
			if (msgHandler != null)
				msgHandler.errorOutln(
					"Cannot write out 2 dimensional spe files");
		} else {
			saveFile("Export RadWare .spe file ", "spe", hist);
		}

	}

	/**
	 * Reads in SPE file. We read in size channels but histogram gets defined with
	 * size channels as jam has 0 to size channels.
	 *
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> display on the MessageHandler
	 */
	public void readData(InputStream buffin) throws ImpExpException {
		try {
			final DataInputStream dis = new DataInputStream(buffin);
			final char[] cName = new char[NAME_LENGTH];
			final int magicInt = dis.readInt();
			if (magicInt != MAGIC_WORD) { //magic word, int 24, hex 18, or ^X
				throw new ImpExpException(
					"Not a Spe File, incorrect magic word, word = "
						+ magicInt
						+ " [ImpExpSPE]");
			}
			/* read in name */
			for (int i = 0; i < NAME_LENGTH; i++) {
				cName[i] = (char) dis.readByte();
			}
			final int size = dis.readInt(); //IDIM1
			dis.readInt(); //should read in a 1, IDIM2
			dis.readInt(); //should read in a 1, IRED1
			dis.readInt(); //should read in a 1, IRED2
			dis.readInt(); //should read a hex  0018  dec 24 
			dis.readInt(); //should read a hex  2000  dec 8192	
			final double [] counts = new double[size];
			final float [] countsFloat = new float[size];
			for (int i = 0;
				i < size;
				i++) { //does not read last channel as Jam size
				countsFloat[i] = dis.readFloat();
				counts[i] = (double) countsFloat[i];
			}
			dis.readInt(); //should read a hex  2000  dec 8192	
			/* parameters of histogram */
			final String nameHist = String.valueOf(cName);
			new Histogram(nameHist, nameHist, counts);
			if (msgHandler != null) {
				msgHandler.messageOut(" .");
			}
			dis.close();
		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		}
	}

	/**
	 * Write out SPE file. We throw out the last channel as jam has histograms have 
	 * channel 0 to size which is size channels. We only write out size channels.
	 *
	 * @exception   ImpExpException    all exceptions given to <code>ImpExpException</code> display on the msgHandler
	 */
	public void writeHist(OutputStream outStream, Histogram hist)
		throws ImpExpException {
		try {
			DataOutputStream dos = new DataOutputStream(outStream);
			/* get data from histogram */
			String name = hist.getName() + "        ";
			//FIXME 8 spaces to pad out should use NAME_LENGTH		
			int size = hist.getSizeX();
			int type = hist.getType();
			/* put data into a float array */
			float[] countsFlt = new float[size];
			if (type == Histogram.ONE_DIM_INT) {
				int[] countsInt = (int[]) hist.getCounts();
				for (int i = 0; i < size; i++) {
					countsFlt[i] = (float) countsInt[i];
				}
			} else if (type == Histogram.ONE_DIM_DOUBLE) {
				double[] countsDbl = (double[]) hist.getCounts();
				for (int i = 0; i < size; i++) {
					countsFlt[i] = (float) countsDbl[i];
				}
			} else {
				throw new ImpExpException("Error unrecognized histogram type [ImpExpSPE]");
			}
			if (size > MAX_SIZE) {
				throw new ImpExpException("Writing out SPE file, size too large [ImpExpSPE]");
			}
			dos.writeInt(MAGIC_WORD); //write out key word
			/* write out histogram name */
			for (int i = 0; i < NAME_LENGTH; i++) {
				dos.writeByte((byte) name.charAt(i));
			}
			/* write out histogram size only does 1 d so far */
			dos.writeInt(size); //IDIM1
			dos.writeInt(1); //IDIM2 in gf3, numch=IDIM1*IDIM2
			dos.writeInt(1);//IRED1, purpose?
			dos.writeInt(1);//IRED2, purpose?
			dos.writeInt(MAGIC_WORD); //hex 0018, dec 24   which is a S0 ^X
			dos.writeInt(4*size);//number of bytes in spectrum
			for (int i = 0; i < size; i++) { //write out histogram data
				dos.writeFloat(countsFlt[i]);
			}
			/* next character found experimentally end of record?
			 * seems to be necessary */
			dos.writeInt(4*size); 
			if (msgHandler != null){
				msgHandler.messageOut(" .");
			}
			dos.flush();
		} catch (IOException ioe) {
			throw new ImpExpException(ioe.toString());
		}
	}
	
	public boolean canExport(){
		return true;
	}
	
	boolean batchExportAllowed(){
		return true;
	}
}
