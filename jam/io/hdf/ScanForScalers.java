package jam.io.hdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import static jam.io.hdf.JamFileFields.SCALER_SECT;

/**
 * Scans files given it for scaler values and outputs strings.
 * 
 * @author Dale Visser
 * 
 */
public class ScanForScalers {
	private static final Logger LOGGER = Logger.getLogger(ScanForScalers.class
			.getPackage().getName());

	private static final char TAB = '\t';

	private transient final ProgressUpdater updater;

	/**
	 * @param updater
	 *            the progress updater
	 */
	public ScanForScalers(final ProgressUpdater updater) {
		this.updater = updater;
	}

	/**
	 * @param carriage
	 * @param outText
	 * @param firstRun
	 * @param index
	 * @param infile
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws HDFException
	 */
	public void processFile(final char carriage, final StringBuffer outText,
			final int firstRun, final int index, final File infile)
			throws IOException, HDFException {
		updater.updateProgressBar("Processing " + infile.getName(), index);
		final HDFile inHDF = new HDFile(infile, "r");
		inHDF.seek(0);
		inHDF.readFile();
		if (index == firstRun) {
			writeHeaderLine(carriage, outText);
		}
		outText.append(index);
		for (int value : getScalerValues()) {
			outText.append(TAB).append(value);
		}
		outText.append(carriage);
	}

	/**
	 * @param carriage
	 * @param outText
	 */
	private void writeHeaderLine(final char carriage, final StringBuffer outText) {
		outText.append("Run");
		for (String name : getScalerNames()) {
			outText.append(TAB).append(name);
		}
		outText.append(carriage);
	}

	private String[] getScalerNames() {
		String[] sname = null;
		final VDataDescription dataDesc = VDataDescription.ofName(AbstractData
				.ofType(VDataDescription.class), SCALER_SECT);
		// only the "scalers" VH (only one element) in the file
		if (dataDesc == null) {
			LOGGER.warning("No Scalers section in HDF file.");
		} else {
			final VData data = AbstractData.getObject(VData.class, dataDesc
					.getRef());
			final int numScalers = dataDesc.getNumRows();
			sname = new String[numScalers];
			for (int i = 0; i < numScalers; i++) {
				sname[i] = data.getString(i, 1).trim();
				final char space = ' ';
				while (sname[i].indexOf(space) != -1) {
					final int tmp = sname[i].indexOf(space);
					final String temp1 = sname[i].substring(0, tmp);
					final String temp2 = sname[i].substring(tmp + 1);
					sname[i] = temp1 + temp2;
				}
			}
		}
		return sname;
	}

	private int[] getScalerValues() {
		int[] values = null;
		final VDataDescription dataDesc = VDataDescription.ofName(AbstractData
				.ofType(VDataDescription.class), SCALER_SECT);
		// only the "scalers" VH (only one element) in the file
		if (dataDesc == null) {
			LOGGER.warning("No Scalers section in HDF file.");
		} else {
			final VData data = AbstractData.getObject(VData.class, dataDesc
					.getRef());
			// corresponding VS
			final int numScalers = dataDesc.getNumRows();
			values = new int[numScalers];
			for (int i = 0; i < numScalers; i++) {
				values[i] = data.getInteger(i, 2);
			}
		}
		return values;
	}
}
