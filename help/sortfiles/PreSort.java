package help.sortfiles;
import jam.Script;
import java.io.File;
import jam.sort.stream.*;

/**
 * Script for performing pre-sorts on specified subsets of the data. 
 *
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 8 Apr 2004
 */
public class PreSort {
    
    private PreSort(String target, int angle){
        final Script scripter=new Script();
		final File cpath=scripter.defineFile("");
		final Class inStream=YaleCAEN_InputStream.class;
		final Class outStream=YaleOutputStream.class;
		final String sorter="sort.newVME.SplitPoleTAJ";
		scripter.setupOffline(cpath,sorter,inStream,outStream);
        final File outPath=scripter.defineFile("results");
        final String filename=target+angle+"_presort";
		final File evnOut=new File(outPath,filename+".evn");
        final File hdfOut=new File(outPath,filename+".hdf");
		evnOut.delete();
		scripter.setEventOutput(evnOut);
		scripter.loadHDF(new File(outPath,"gates.hdf"));
		scripter.zeroHistograms();
		scripter.showJam();
		scripter.beginSort();
		hdfOut.delete();
		scripter.saveHDF(hdfOut);
    }

    /**
     * <p>Launch to do a pre-sort.</p>
     * <p>Usage: <code>command <i>target angleDeg</i></p>
     * <p><i>target</i> is one of Mg, Si, C12, or C13, and 
     * <i>angleDeg</i> is one of 10, 15, or 20.
     *
     * @param args <i>target</i> and <i>angleDeg</i>
     */
	public static void main(String[] args) {
        new PreSort(args[0], Integer.parseInt(args[1]));
	}
}
