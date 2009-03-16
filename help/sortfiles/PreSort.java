package help.sortfiles;

import injection.GuiceInjector;
import jam.script.Session;
import jam.sort.stream.AbstractEventInputStream;
import jam.sort.stream.AbstractEventOutputStream;
import jam.sort.stream.YaleCAEN_InputStream;
import jam.sort.stream.YaleOutputStream;

import java.io.File;

/**
 * Script for performing pre-sorts on specified subsets of the data.
 * 
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 * @version 8 Apr 2004
 */
public final class PreSort {

	private PreSort(final String target, final int angle) {
		super();
		final Session scripter = GuiceInjector.getSession();
		final File cpath = scripter.defineFile("");
		final Class<? extends AbstractEventInputStream> inStream = YaleCAEN_InputStream.class;
		final Class<? extends AbstractEventOutputStream> outStream = YaleOutputStream.class;
		final String sorter = "sort.newVME.SplitPoleTAJ";
		scripter.setupOffline(cpath, sorter, inStream, outStream);
		final File outPath = scripter.defineFile("results");
		final String filename = target + angle + "_presort";
		final File evnOut = new File(outPath, filename + ".evn");
		final File hdfOut = new File(outPath, filename + ".hdf");
		evnOut.delete();
		scripter.setEventOutput(evnOut);
		scripter.loadHDF(new File(outPath, "gates.hdf"));
		scripter.zeroHistograms();
		scripter.showJam();
		scripter.beginSort();
		hdfOut.delete();
		scripter.saveHDF(hdfOut);
	}

	/**
	 * <p>
	 * Launch to do a pre-sort.
	 * </p>
	 * <p>
	 * Usage: <code>command <i>target angleDeg</i>
	 * </p>
	 * <p>
	 * <i>target</i> is one of Mg, Si, C12, or C13, and <i>angleDeg</i> is one
	 * of 10, 15, or 20.
	 * 
	 * @param args
	 *            <i>target</i> and <i>angleDeg</i>
	 */
	public static void main(final String[] args) {
		new PreSort(args[0], Integer.parseInt(args[1]));
	}
}
