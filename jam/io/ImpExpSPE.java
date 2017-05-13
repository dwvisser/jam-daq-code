package jam.io;

import jam.data.AbstractHistogram;
import jam.data.Factory;
import jam.data.HistDouble1D;
import jam.data.HistInt1D;
import jam.data.HistogramType;
import jam.ui.ExtensionFileFilter;

import java.awt.Frame;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.filechooser.FileFilter;

import com.google.inject.Inject;

/**
 * Imports and Exports Spectra (Histograms) using the SPE format, this is used
 * by RADWARE gf2.
 * 
 * @version 0.5
 * @author Ken Swartz
 */
public final class ImpExpSPE extends AbstractImpExp {// NOPMD

	private static final int NAME_LENGTH = 8;

	private static final int MAX_SIZE = 8192;

	private static final int MAGIC_WORD = 24;

	private static final ExtensionFileFilter FILTER = new ExtensionFileFilter(
			"spe", "Radware gf3");

	/**
	 * @param frame
	 *            application frame
	 */
	@Inject
	public ImpExpSPE(final Frame frame) {
		super(frame);
	}

	@Override
	protected FileFilter getFileFilter() {
		return FILTER;
	}

	@Override
	protected String getDefaultExtension() {
		return FILTER.getExtension(0);
	}

	@Override
	public String getFormatDescription() {
		return FILTER.getDescription();
	}

	/**
	 * Prompts for and opens a file.
	 * 
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code>
	 *                display on the MessageHandler
	 */
	@Override
	public boolean openFile(final File file) throws ImpExpException {
		return openFile(file, "Import RadWare .spe file ");
	}

	/**
	 * Prompts for file name and saves.
	 * 
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code>
	 *                display on the MessageHandler
	 */
	@Override
	public void saveFile(final AbstractHistogram hist) throws ImpExpException {
		if (hist.getDimensionality() == 2) {
			if (!silent) {
				LOGGER.severe("Cannot write out 2 dimensional spe files");
			}
		} else {
			saveFile("Export RadWare .spe file ", hist);
		}
	}

	/**
	 * Reads in SPE file. We read in size channels but histogram gets defined
	 * with size channels as jam has 0 to size channels.
	 * 
	 * @exception ImpExpException
	 *                all exceptions given to <code>ImpExpException</code>
	 *                display on the MessageHandler
	 */
	@Override
	public void readData(final InputStream buffin) throws ImpExpException {
		try {
			final DataInputStream dis = new DataInputStream(buffin);
			final char[] cName = new char[NAME_LENGTH];
			final int magicInt = dis.readInt();
			if (magicInt != MAGIC_WORD) { // magic word, int 24, hex 18, or ^X
				throw new ImpExpException(
						"Not a Spe File, incorrect magic word, word = "
								+ magicInt + " [ImpExpSPE]");
			}
			/* read in name */
			for (int i = 0; i < NAME_LENGTH; i++) {
				cName[i] = (char) dis.readByte();
			}
			final int size = dis.readInt(); // IDIM1
			dis.readInt(); // should read in a 1, IDIM2
			dis.readInt(); // should read in a 1, IRED1
			dis.readInt(); // should read in a 1, IRED2
			dis.readInt(); // should read a hex 0018 dec 24
			dis.readInt(); // should read a hex 2000 dec 8192
			final double[] counts = new double[size];
			final float[] countsFloat = new float[size];
			for (int i = 0; i < size; i++) { // does not read last channel as
				// Jam size
				countsFloat[i] = dis.readFloat();
				counts[i] = countsFloat[i];
			}
			dis.readInt(); // should read a hex 2000 dec 8192
			/* parameters of histogram */
			final String nameHist = String.valueOf(cName);
			Factory.createHistogram(importGroup, counts, nameHist);
			dis.close();
		} catch (IOException ioe) {
			throw new ImpExpException("Problem reading spectrum file.", ioe);
		}
	}

	@Override
	public void writeHist(final OutputStream outStream,
			final AbstractHistogram hist) throws ImpExpException {
		try {
			final DataOutputStream dos = new DataOutputStream(outStream);
			/* get data from histogram */
			final StringBuilder name = new StringBuilder(hist.getFullName());
			while (name.length() < NAME_LENGTH) {
				name.append(' ');
			}
			final int size = hist.getSizeX();
			final HistogramType type = hist.getType();
			/* put data into a float array */
			final float[] countsFlt;
			if (type == HistogramType.ONE_DIM_INT) {
				final int[] countsInt = ((HistInt1D) hist).getCounts();
				countsFlt = copyIntToFloat(countsInt, size);
			} else if (type == HistogramType.ONE_D_DOUBLE) {
				final double[] countsDbl = ((HistDouble1D) hist).getCounts();
				countsFlt = new float[size];
				for (int i = 0; i < size; i++) {
					countsFlt[i] = (float) countsDbl[i];
				}
			} else {
				throw new ImpExpException(
						"Error unrecognized histogram type [ImpExpSPE]");
			}
			if (size > MAX_SIZE) {
				throw new ImpExpException(
						"Writing out SPE file, size too large [ImpExpSPE]");
			}
			dos.writeInt(MAGIC_WORD); // write out key word
			/* write out histogram name */
			for (int i = 0; i < NAME_LENGTH; i++) {
				dos.writeByte((byte) name.charAt(i));
			}
			/* write out histogram size only does 1 d so far */
			dos.writeInt(size); // IDIM1
			dos.writeInt(1); // IDIM2 in gf3, numch=IDIM1*IDIM2
			dos.writeInt(1);// IRED1, purpose?
			dos.writeInt(1);// IRED2, purpose?
			dos.writeInt(MAGIC_WORD); // hex 0018, dec 24 which is a S0 ^X
			dos.writeInt(4 * size);// number of bytes in spectrum
			for (int i = 0; i < size; i++) { // write out histogram data
				dos.writeFloat(countsFlt[i]);
			}
			/*
			 * next character found experimentally end of record? seems to be
			 * necessary
			 */
			dos.writeInt(4 * size);
			dos.flush();
		} catch (IOException ioe) {
			throw new ImpExpException(ioe);
		}
	}

	private float[] copyIntToFloat(final int[] countsInt, final int size) {
		final float[] countsFlt = new float[size];
		for (int i = 0; i < size; i++) {// NOPMD
			countsFlt[i] = countsInt[i];
		}
		return countsFlt;
	}

	@Override
	public boolean canExport() {
		return true;
	}

	@Override
	protected boolean batchExportAllowed() {
		return true;
	}
}
