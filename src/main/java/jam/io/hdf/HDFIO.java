package jam.io.hdf;

import com.google.inject.Inject;
import jam.data.*;
import jam.data.func.AbstractCalibrationFunction;
import jam.data.func.NoFunction;
import jam.global.JamStatus;
import jam.io.DataIO;
import jam.io.FileOpenMode;
import jam.util.AbstractSwingWorker;
import jam.util.FileUtilities;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Reads and writes HDF files containing spectra, scalers, gates, and additional
 * useful information.
 * 
 * @version 0.5 November 98, January 2005
 * @author Dale Visser, Ken Swartz
 * @since JDK1.1
 */
public final class HDFIO implements DataIO {
	private static final Logger LOGGER = Logger.getLogger(HDFIO.class
			.getPackage().getName());

	/**
	 * Interface to be called when asynchronized IO is completed.
	 */
	public interface AsyncListener {
		/**
		 * Called when asynchronous IO is completed
		 * 
		 * @param message
		 *            if normal completion
		 * @param errorMessage
		 *            if an error occurs
		 */
		void completedIO(String message, String errorMessage);
	}

	private transient final FileUtilities fileUtilities;

	private static final String HDF_FILE_EXT = "hdf";

	/**
	 * Last file successfully read from or written to for all instances of
	 * HDFIO.
	 * 
	 * see #readFile
	 */
	private static File lastGoodFile;

	private static final String LFILE_KEY = "LastValidFile";

	private static final Object LVF_MONITOR = new Object();

	private static class MonitorSteps {
		private MonitorSteps() {
			super();
		}

		public static final int OVERHEAD_READ = 1;

		public static final int OVERHEAD_WRITE = 3; // 2 for start

		/**
		 * Number of steps in progress, 1 for converting objects, 10 for writing
		 * them out
		 */
		public static final int READ_WRITE = 11; // 1 Count DD's, 10
	}

	// read objects

	private static final Preferences PREFS = Preferences
			.userNodeForPackage(HDFIO.class);

	static {
		lastGoodFile = new File(PREFS.get(LFILE_KEY, System
				.getProperty("user.dir")));
	}

	private static final NameValueCollection<Group> GROUPS = Warehouse
			.getGroupCollection();

	/**
	 * @return last file successfully read from or written to.
	 */
	public static File getLastValidFile() {
		synchronized (LVF_MONITOR) {
			return lastGoodFile;
		}
	}

	private static void setLastValidFile(final File file) {
		synchronized (LVF_MONITOR) {
			lastGoodFile = file;
			PREFS.put(LFILE_KEY, file.getAbsolutePath());
		}
	}

	private transient final AsyncProgressMonitor asyncMonitor;

	private static final AsyncListener DO_NOTHING = (message, errorMessage) -> {
        // do nothing
    };

	private transient AsyncListener asListener = DO_NOTHING;

	private transient Group firstLoadedGroup;

	private transient int gateCount = 0;

	private transient int groupCount = 0;

	private transient final ConvertHDFObjToJamObj hdfToJam;

	private transient int histCount = 0;

	/**
	 * <code>HDFile<code> object to read from.
	 */
	private transient HDFile inHDF;

	private transient final ConvertJamObjToHDFObj jamToHDF;

	private transient int paramCount = 0;

	private transient int scalerCount = 0;

	/* --------------------- Begin DataIO Interface Methods ------------------ */

	private transient String uiErrorMsg;

	private transient String uiMessage;

	private transient final JamStatus status;

	/**
	 * Class constructor handed references to the main class and message
	 * handler.
	 * 
	 * @param parent
	 *            the parent window
	 * @param status
	 *            application status
	 * @param jamToHDF
	 *            converts between objects and hdf
	 * @param hdfToJam
	 *            converts from hdf to objects
	 * @param fileUtilities
	 *            the file utility object
	 */
	@Inject
	public HDFIO(final Frame parent, final JamStatus status,
			final ConvertJamObjToHDFObj jamToHDF,
			final ConvertHDFObjToJamObj hdfToJam,
			final FileUtilities fileUtilities) {
		super();
		asyncMonitor = new AsyncProgressMonitor(parent);
		this.jamToHDF = jamToHDF;
		this.hdfToJam = hdfToJam;
		this.status = status;
		this.fileUtilities = fileUtilities;
	}

	/*
	 * non-javadoc: Read in an HDF file
	 * 
	 * @param infile file to load @param mode whether to open or reload @param
	 * histNames names of histograms to read, null if all @return
	 * <code>true</code> if successful
	 */
	private boolean asyncReadFileGroup(final File infile,
			final FileOpenMode mode, final List<Group> existingGrps,
			final List<HistogramAttributes> histAttrList) {
		synchronized (this) {
			boolean rval = true;
			final StringBuilder message = new StringBuilder(54);
			// reset all counters
			groupCount = 0;
			histCount = 0;
			gateCount = 0;
			scalerCount = 0;
			paramCount = 0;
			try {
				AbstractData.clearAll();
				// Read in objects
				inHDF = new HDFile(infile, "r", asyncMonitor,
						MonitorSteps.READ_WRITE);
				inHDF.setLazyLoadData(true);
				/*
				 * read file into set of AbstractHData's, set their internal
				 * variables
				 */
				inHDF.readFile();
				AbstractData.interpretBytesAll();
				asyncMonitor.increment();
				final String fileName = fileUtilities
						.removeExtensionFileName(infile.getName());
				if (hdfToJam.hasVGroupRootGroup()) {
					convertHDFToJam(mode, fileName, existingGrps, histAttrList);
				} else {
					convertHDFToJamOriginal(mode, fileName, existingGrps,
							histAttrList);
				}
				createOutputMessage(infile, mode, existingGrps, message);
			} catch (FileNotFoundException e) {
				uiErrorMsg = "Opening file: " + infile.getPath()
						+ " Cannot find file or file is locked";
			} catch (HDFException e) {
				uiErrorMsg = "Reading file: '" + infile.getName()
						+ "', Exception " + e.toString();
				rval = false;
			} finally {
				try {
					inHDF.close();
				} catch (IOException except) {
					uiErrorMsg = "Closing file " + infile.getName();
					rval = false;
				}
				/* destroys reference to HDFile (and its AbstractHData's) */
				inHDF = null;// NOPMD
			}
			AbstractData.clearAll();
			setLastValidFile(infile);
			uiMessage = message.toString();
			return rval;
		}
	}

	private void createOutputMessage(final File infile,
			final FileOpenMode mode, final List<Group> existingGrps,
			final StringBuilder message) {
		/* Create output message. */
		if (mode == FileOpenMode.OPEN) {
			message.append("Opened ").append(infile.getName());
		} else if (mode == FileOpenMode.OPEN_MORE) {
			message.append("Opened Additional ").append(infile.getName());
		} else if (mode == FileOpenMode.RELOAD) {
			message.append("Reloaded ").append(infile.getName());
		} else { // ADD
			message.append("Adding counts in ").append(infile.getName());
			// FIXME currently only add one group
			message.append(" to groups ");
			for (int i = 0; i < existingGrps.size(); i++) {
				final String groupName = existingGrps.get(0).getName();
				if (0 < i) {
					message.append(", ");
				}
				message.append(groupName);
			}
		}
		appendDataCounts(message);
	}

	private void appendDataCounts(final StringBuilder message) {
		message.append(" (");
		message.append(groupCount).append(" groups");
		message.append(", ").append(histCount).append(" histograms");
		message.append(", ").append(gateCount).append(" gates");
		message.append(", ").append(scalerCount).append(" scalers");
		message.append(", ").append(paramCount).append(" parameters");
		message.append(')');
	}

	/*
	 * non-javadoc: Given separate vectors of the writeable objects, constructs
	 * and writes out an HDF file containing the contents. Null or empty
	 * <code>Vector</code> arguments are skipped.
	 * 
	 * @param file disk file to write to @param histograms list of
	 * <code>Histogram</code> objects to write @param groups list of
	 * <code>Group</code>'s to write
	 * 
	 * @param writeScalers whether to write out histograms scalers @param
	 * writeParams whether to write out gates, calibration and parameters
	 */
	private void asyncWriteFile(final File file, final List<Group> groups,
			final List<AbstractHistogram> histograms, final boolean writeData,
			final boolean writeSettings) {
		synchronized (this) {
			final StringBuilder message = new StringBuilder(60);
			/* reset all counters */
			groupCount = 0;
			histCount = 0;
			gateCount = 0;
			scalerCount = 0;
			paramCount = 0;
			AbstractData.clearAll();
			jamToHDF.addDefaultDataObjects(file.getPath());
			asyncMonitor.setup("Saving HDF file", "Converting Objects",
					MonitorSteps.READ_WRITE + MonitorSteps.OVERHEAD_WRITE);
			final Preferences prefs = HDFPrefs.PREFS;
			final boolean suppressEmpty = prefs.getBoolean(
					HDFPrefs.SUPPRES_EMPTY, true);
			asyncMonitor.increment();
			HDFile out = null;
			try {
				convertJamToHDF(groups, histograms, writeData, writeSettings,
						suppressEmpty);

				out = new HDFile(file, "rw", asyncMonitor,
						MonitorSteps.READ_WRITE);
				asyncMonitor.setNote("Writing Data Objects");
				out.writeFile();
				asyncMonitor.setNote("Closing File");
				message.append("Saved ").append(file.getName());
				this.appendDataCounts(message);
			} catch (FileNotFoundException e) {
				uiErrorMsg = "Opening file: " + file.getName();
			} catch (HDFException e) {
				uiErrorMsg = "Exception writing to file '" + file.getName()
						+ "': " + e.toString();
			} finally {
				try {
					if (out != null) {
						out.close();
					}
				} catch (IOException e) {
					uiErrorMsg = "Closing file " + file.getName();
				}
				asyncMonitor.close();
			}

			AbstractData.clearAll();
			setLastValidFile(file);
			uiMessage = message.toString();
		}
	}

	/* Convert the HDF DataObjects to Jam objects */
	private void convertHDFToJam(final FileOpenMode mode,
			final String fileName, final List<Group> existingGroupList,
			final List<HistogramAttributes> histAttributeList)
			throws HDFException {
		hdfToJam.setInFile(inHDF);
		// Find groups
		final List<VirtualGroup> virtualGroups = hdfToJam
				.findGroups(existingGroupList);
		GROUPLIST: for (VirtualGroup currentVGroup : virtualGroups) {
			final List<VirtualGroup> histList;
			final Group currentGroup = getCurrentGroup(mode, fileName,
					existingGroupList, histAttributeList, currentVGroup);

			// No histograms in group
			if (currentGroup == null) {
				continue GROUPLIST;
			}

			setFirstLoadedGroupIfNull(currentGroup);
			// Find histograms
			final List<String> empty = Collections.emptyList();
			histList = hdfToJam.findHistograms(currentVGroup, empty);

			// Loop over histograms
			for (VirtualGroup histVGroup : histList) {
				loadHistogram(mode, histAttributeList, currentGroup, histVGroup);
			} // Loop Histogram end
			// Load scalers
			final List<VirtualGroup> scalerList = hdfToJam
					.findScalers(currentVGroup);
			if (!scalerList.isEmpty()) {
				scalerCount = hdfToJam.convertScalers(currentGroup, scalerList
						.get(0), mode);
			}
			// Load Parameters
			final List<VirtualGroup> paramList = hdfToJam
					.findParameters(currentVGroup);
			if (!paramList.isEmpty()) {
				paramCount = hdfToJam.convertParameters(paramList.get(0), mode);
			}
		} // Loop group end
	}

	private void loadHistogram(final FileOpenMode mode,
			final List<HistogramAttributes> histAttributeList,
			final Group currentGroup, final VirtualGroup histVGroup)
			throws HDFException {
		final AbstractHistogram hist = hdfToJam.convertHistogram(currentGroup,
				histVGroup, histAttributeList, mode);
		if (hist != null) {
			histCount++;
		}
		// Load gates and calibration if not add
		if (hist != null && mode != FileOpenMode.ADD) {
			loadGatesAndCalibration(mode, histVGroup, hist);
		} // End Load gates and calibration
	}

	private void loadGatesAndCalibration(final FileOpenMode mode,
			final VirtualGroup histVGroup, final AbstractHistogram hist)
			throws HDFException {
		final List<VirtualGroup> gateList = hdfToJam.findGates(histVGroup, hist
				.getType());
		// Loop over gates
		countGates(mode, hist, gateList);
		/* Load calibration. */
		final VDataDescription vddCalibration = hdfToJam
				.findCalibration(histVGroup);
		if (vddCalibration != null) {
			hdfToJam.convertCalibration(hist, vddCalibration);
		}
	}

	private void countGates(final FileOpenMode mode,
			final AbstractHistogram hist, final List<VirtualGroup> gateList)
			throws HDFException {
		for (VirtualGroup gateVGroup : gateList) {
			final Gate gate = hdfToJam.convertGate(hist, gateVGroup, mode);
			if (gate != null) {
				gateCount++;
			}
		}
	}

	private Group getCurrentGroup(final FileOpenMode mode,
			final String fileName, final List<Group> existingGroupList,
			final List<HistogramAttributes> histAttributeList,
			final VirtualGroup currentVGroup) {
		// Get the current group for the rest of the operation
		Group currentGroup = null;
		if (mode == FileOpenMode.OPEN || mode == FileOpenMode.OPEN_MORE) {
			currentGroup = hdfToJam.convertGroup(currentVGroup, fileName,
					histAttributeList, mode);
			if (currentGroup != null) {
				groupCount++;
			}
		} else {
			final String groupName = hdfToJam
					.readVirtualGroupName(currentVGroup);
			if (hdfToJam.containsGroup(groupName, existingGroupList)) {
				currentGroup = GROUPS.get(groupName);
			}
		}
		return currentGroup;
	}

	private static final SortGroupGetter SORT_GROUP_GETTER = Warehouse
			.getSortGroupGetter();

	/* --------------------- End DataIO Interface Methods ------------------ */

	/**
	 * Convert a the HDF DataObjects to Jam objects for old format files
	 * 
	 * @param mode
	 *            how to read the data into memory
	 * @param groups
	 *            list of existing groups
	 * @param histAttrs
	 *            list of histogram attributes?
	 * @param fileName
	 *            ???
	 * @throws HDFException if a problem occurs
	 */
	private void convertHDFToJamOriginal(final FileOpenMode mode,
			final String fileName, final List<Group> groups,
			final List<HistogramAttributes> histAttrs) throws HDFException {
		hdfToJam.setInFile(inHDF);
		Group currentGroup = null;
		// Set group
		if ((mode == FileOpenMode.OPEN)) {
			currentGroup = Factory.createGroup(Group.DEFAULT_NAME, null,
					Group.Type.FILE);
		} else if (mode == FileOpenMode.OPEN_MORE) {
			currentGroup = Factory.createGroup(Group.DEFAULT_NAME, fileName,
					Group.Type.FILE);
		} else if (mode == FileOpenMode.ADD) {
			currentGroup = groups.get(0);
			// so use current group
		} else if (mode == FileOpenMode.RELOAD) {
			final Group sortGroup = SORT_GROUP_GETTER.getSortGroup();
			status.setCurrentGroup(sortGroup);
			currentGroup = (Group) status.getCurrentGroup();
		}
		setFirstLoadedGroupIfNull(currentGroup);
		groupCount = 0;

		histCount = hdfToJam.convertHistogramsOriginal(currentGroup, mode,
				histAttrs);
		final VDataDescription vddScalers = hdfToJam.findScalersOriginal();
		if (vddScalers != null) {
			scalerCount = hdfToJam.convertScalers(currentGroup, vddScalers,
					mode);
		}
		if (mode != FileOpenMode.ADD) {
			gateCount = hdfToJam.convertGatesOriginal(currentGroup, mode);
			/* clear if opening and there are histograms in file */
			final VDataDescription vddParam = hdfToJam.findParametersOriginal();
			if (vddParam != null) {
				paramCount = hdfToJam.convertParameters(vddParam, mode);
			}
		}
	}

	private void setFirstLoadedGroupIfNull(final Group currentGroup) {
		/* Keep track of first loaded group */
		if (firstLoadedGroup == null) {
			firstLoadedGroup = currentGroup;
		}
	}

	/* Convert Jam objects to HDF DataObjects */
	private void convertJamToHDF(final List<Group> groups,
			final List<AbstractHistogram> histList, final boolean writeData,
			final boolean wrtSettings, final boolean suppressEmpty) {
		final VirtualGroup globalGroups = jamToHDF.addGroupSection();
		final VirtualGroup globalHists = jamToHDF.addHistogramSection();
		final VirtualGroup globalGates = jamToHDF.addGateSection();
		final VirtualGroup globalScaler = jamToHDF.addScalerSection();
		final VirtualGroup globalParams = jamToHDF.addParameterSection();
		/* Loop for all groups */
		for (Group group : groups) {
			final VirtualGroup vgGroup = jamToHDF.convertGroup(group);
			globalGroups.add(vgGroup);
			/* Loop for all histograms */
			for (AbstractHistogram hist : group.histograms.getList()) {
				// Histogram is in histogram list
				if (histList.contains(hist)) {
					final VirtualGroup histVGroup = jamToHDF
							.addHistogramGroup(hist);
					vgGroup.add(histVGroup);
					// backward compatible
					globalHists.add(histVGroup);
					final boolean histDefined = hist.getArea() > 0
							|| !suppressEmpty;
					convertHistogram(writeData, hist, histVGroup, histDefined);
					// Add calibrations
					addCalibration(writeData, wrtSettings, hist, histVGroup);
					addGates(wrtSettings, globalGates, hist, histVGroup);
				}
			} // end loop histograms
			/* Convert all scalers */
			if (writeData) {
				final List<Scaler> scalerList = group.getScalerList();
				addScalers(globalScaler, group, vgGroup, scalerList);
				scalerCount = scalerList.size();
			}
			/* Convert all parameters */
			if (wrtSettings) {
				final List<DataParameter> paramList = DataParameter
						.getParameterList();
				if (!paramList.isEmpty()) {
					final VirtualGroup vgParams = jamToHDF
							.addParameterSection();
					vgGroup.add(vgParams);
					if (group == SORT_GROUP_GETTER.getSortGroup()) {
						final VDataDescription vddParams = jamToHDF
								.convertParameters(paramList);
						vgParams.add(vddParams);
						/* Backwards compatible */
						globalParams.add(vddParams);
						paramCount = paramList.size();
					}
				}
			}
			groupCount++;
		}
	}

	private void addScalers(final VirtualGroup globalScaler, final Group group,
			final VirtualGroup vgGroup, final List<Scaler> scalerList) {
		if (!scalerList.isEmpty()) {
			final VirtualGroup vgScalers = jamToHDF.addScalerSection();
			vgGroup.add(vgScalers);
			final VDataDescription vddScalers = jamToHDF
					.convertScalers(scalerList);
			vgScalers.add(vddScalers);
			addScalers(globalScaler, group, vddScalers);
		}
	}

	private void addScalers(final VirtualGroup globalScaler, final Group group,
			final VDataDescription vddScalers) {
		if (group == SORT_GROUP_GETTER.getSortGroup()) {
			/* here for backwards compatibility */
			globalScaler.add(vddScalers);
		}
	}

	private void addGates(final boolean wrtSettings,
			final VirtualGroup globalGates, final AbstractHistogram hist,
			final VirtualGroup histVGroup) {
		/* Loop for all gates */
		for (DataElement gate : hist.getGateCollection().getGates()) {
			addGates(wrtSettings, globalGates, histVGroup, (Gate) gate);
		} // end loop gates
	}

	private void addCalibration(final boolean writeData,
			final boolean wrtSettings, final AbstractHistogram hist,
			final VirtualGroup histVGroup) {
		if ((writeData || wrtSettings) && hist.getDimensionality() == 1) {
			final AbstractCalibrationFunction calFunc = ((AbstractHist1D) hist)
					.getCalibration();
			addCalibration(histVGroup, calFunc);
		}
	}

	private void addGates(final boolean wrtSettings,
			final VirtualGroup globalGates, final VirtualGroup histVGroup,
			final Gate gate) {
		if (wrtSettings && gate.isDefined()) {
			final VirtualGroup gateVGroup = jamToHDF.convertGate(gate);
			histVGroup.add(gateVGroup);
			// backward compatiable
			globalGates.add(gateVGroup);
			gateCount++;
		}
	}

	private void addCalibration(final VirtualGroup histVGroup,
			final AbstractCalibrationFunction calFunc) {
		if (!(calFunc instanceof NoFunction)) {
			final VDataDescription calibDD = jamToHDF
					.convertCalibration(calFunc);
			histVGroup.add(calibDD);
		}
	}

	private void convertHistogram(final boolean writeData,
			final AbstractHistogram hist, final VirtualGroup histVGroup,
			final boolean histDefined) {
		if (writeData && histDefined) {
			jamToHDF.convertHistogram(histVGroup, hist);
			histCount++;
		}
	}

	private void displayMessage() {
		final Runnable runner = () -> {
            if (uiErrorMsg.length() == 0) {
                LOGGER.info(uiMessage);
            } else {
                LOGGER.severe(uiErrorMsg);
            }
            uiMessage = "";
            uiErrorMsg = "";
        };

		try {
			SwingUtilities.invokeAndWait(runner);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	/**
	 * 
	 * @return the first loaded group?
	 */
	public Group getFirstLoadGroup() {
		return firstLoadedGroup;
	}

	/*
	 * non-javadoc: Reads in the histogram and hold them in a tempory array
	 * 
	 * @exception HDFException thrown if unrecoverable error occurs
	 */
	private List<HistogramAttributes> loadHistogramAttributesGroup()
			throws HDFException {
		final ArrayList<HistogramAttributes> lstHistAtt = new ArrayList<>();
		final FileOpenMode mode = FileOpenMode.ATTRIBUTES;
		hdfToJam.setInFile(inHDF);
		// Find groups
		final List<VirtualGroup> groupVG = hdfToJam.findGroups(null);
		groupCount = groupVG.size();
		// Loop over groups
		for (VirtualGroup currentVGroup : groupVG) {
			final String groupName = hdfToJam
					.readVirtualGroupName(currentVGroup);
			// Find histograms
			final List<String> empty = Collections.emptyList();
			final List<VirtualGroup> histList = hdfToJam.findHistograms(
					currentVGroup, empty);
			histCount = histList.size();
			// Loop over histograms
			// final Iterator histIter = histList.iterator();
			for (VirtualGroup histVGroup : histList) {
				// final VirtualGroup histVGroup = (VirtualGroup)
				// histIter.next();
				final HistogramAttributes histAttributes = hdfToJam
						.convertHistogamAttributes(groupName, histVGroup, mode);
				lstHistAtt.add(histAttributes);
			}
		}
		return lstHistAtt;
	}

	/*
	 * non-javadoc: Reads in the histogram and hold them in a tempory array
	 * 
	 * @exception HDFException thrown if unrecoverable error occurs
	 */
	private List<HistogramAttributes> loadHistogramAttributesOriginal()
			throws HDFException {
		final List<HistogramAttributes> lstHistAtt = new ArrayList<>();
		hdfToJam.setInFile(inHDF);
		final VirtualGroup hists = VirtualGroup
				.ofName(JamFileFields.HIST_SECTION);
		/* only the "histograms" VG (only one element) */
		if (hists != null) {
			for (AbstractData data : hists.getObjects()) {
				final VirtualGroup currHistGrp = (VirtualGroup) data;
				final HistogramAttributes histAttributes = hdfToJam
						.convertHistogamAttributes(Group.DEFAULT_NAME,
								currHistGrp, FileOpenMode.ATTRIBUTES);
				lstHistAtt.add(histAttributes);
			}
			// after loop
		}
		return lstHistAtt;
	}

	/**
	 * Read in an HDF file.
	 * 
	 * @param mode
	 *            whether to open or reload
	 * @param infile
	 *            file to load
	 */
	public boolean readFile(final FileOpenMode mode, final File infile) {
		return readFile(mode, Collections.singletonList(infile), null, null);
	}

	/**
	 * Read in an HDF file.
	 * 
	 * @param infile
	 *            file to load
	 * @param mode
	 *            whether to open or reload
	 * @param group
	 *            group to read in
	 * @return <code>true</code> if successful
	 */
	public boolean readFile(final FileOpenMode mode, final File infile,
			final Group group) {
		return readFile(mode, Collections.singletonList(infile), Collections
				.singletonList(group), null);
	}

	/**
	 * Read in an HDF file.
	 * 
	 * @param infile
	 *            file to load
	 * @param mode
	 *            whether to open or reload
	 * @param histAttributeList
	 *            attributes of <code>Histogram</code>'s to read in
	 * @return <code>true</code> if successful
	 */
	public boolean readFile(final FileOpenMode mode, final File infile,
			final List<HistogramAttributes> histAttributeList) {
		return readFile(mode, Collections.singletonList(infile), null,
				histAttributeList);
	}

	/**
	 * Read in an HDF file.
	 * 
	 * @param inFiles
	 *            files to load
	 * @param mode
	 *            whether to open or reload
	 * @param histAttributeList
	 *            list of attributes of histograms to read in
	 * @param groupList
	 *            list of names of groups to read in
	 * @return <code>true</code> if successful
	 */
	public boolean readFile(final FileOpenMode mode, final List<File> inFiles,
			final List<Group> groupList,
			final List<HistogramAttributes> histAttributeList) {
		boolean rval = true;
		fileLoop:
		for (final File infile : inFiles) {
			if (!infile.isFile()) {
				LOGGER.severe("Cannot find file " + infile + ".");
				rval = false;
				break fileLoop;
			}
			if (!HDFile.isHDFFile(infile)) {
				LOGGER.severe("File " + infile + " is not a valid HDF file.");
				rval = false;
				break fileLoop;
			}
		}
		if (rval) {
			spawnAsyncReadFile(mode, inFiles, groupList, histAttributeList);
		}
		return rval;
	}

	/**
	 * Read the histograms in.
	 * 
	 * @param infile
	 *            to read from
	 * @return list of attributes
	 * @throws HDFException
	 *             if something goes wrong
	 */
	public List<HistogramAttributes> readHistogramAttributes(final File infile)
			throws HDFException {
		final List<HistogramAttributes> rval = new ArrayList<>();
		if (!HDFile.isHDFFile(infile)) {
			rval.clear();
			throw new HDFException("File:" + infile.getPath()
					+ " is not an HDF file.");
		}
		try {
			AbstractData.clearAll();
			/* Read in histogram names */
			inHDF = new HDFile(infile, "r");
			inHDF.setLazyLoadData(true);
			inHDF.readFile();
			AbstractData.interpretBytesAll();
			HistogramAttributes.clear();
			if (hdfToJam.hasVGroupRootGroup()) {
				rval.addAll(loadHistogramAttributesGroup());
			} else {
				rval.addAll(loadHistogramAttributesOriginal());
			}
		} catch (FileNotFoundException e) {
			throw new HDFException("Opening file: " + infile.getPath()
					+ " Cannot find file or file is locked", e);
		} catch (HDFException e) {
			throw new HDFException("Reading file: '" + infile.getName()
					+ "', Exception " + e.toString(), e);
		} finally {
			try {
				inHDF.close();
			} catch (IOException except) {
				LOGGER.warning(except.getMessage());
			}
			inHDF = null;// NOPMD
		}
		AbstractData.clearAll();
		return rval;
	}

	/**
	 * Unset the listener.
	 * 
	 */
	public void removeListener() {
		synchronized (asListener) {
			asListener = DO_NOTHING;
		}
	}

	/**
	 * Set the listener.
	 * 
	 * @param listener
	 *            the new listener
	 */
	public void setListener(final AsyncListener listener) {
		synchronized (asListener) {
			if (listener == null) {
				removeListener();
			} else {
				asListener = listener;
			}
		}
	}

	/*
	 * non-javadoc: Asyncronized read
	 */
	private void spawnAsyncReadFile(final FileOpenMode mode,
			final List<File> inFiles, final List<Group> groupList,
			final List<HistogramAttributes> histAttributeList) {
		uiMessage = "";
		uiErrorMsg = "";
		final AbstractSwingWorker worker = new AbstractSwingWorker() {
			@Override
			public Object construct() {
				// FIXME KBS Test change thread priority to make monitor pop up
				// sooner
				Thread.yield();
				Thread thisTread = Thread.currentThread();
				thisTread.setPriority(thisTread.getPriority() - 1);
				// End test
				int numberFiles = inFiles.size();
				asyncMonitor.setup("Reading HDF file", "Reading Objects",
						(MonitorSteps.READ_WRITE + MonitorSteps.OVERHEAD_READ)
								* numberFiles);
				firstLoadedGroup = null; // NOPMD
				try {
					// Loop for all files
					for (int i = 0; i < inFiles.size(); i++) {
						File infile = inFiles.get(i);
						if (mode == FileOpenMode.ADD_OPEN_ONE) {
							if (i == 0) {
								asyncReadFileGroup(infile, FileOpenMode.OPEN,
										groupList, histAttributeList);
							} else {
								final List<Group> groupListOpen = GROUPS
										.getList();
								asyncReadFileGroup(infile, FileOpenMode.ADD,
										groupListOpen, histAttributeList);
							}
						} else {
							asyncReadFileGroup(infile, mode, groupList,
									histAttributeList);
						}
						displayMessage();
					}
				} catch (Exception e) {
					uiErrorMsg = "Unknown Error reading file: " + e;
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
				} finally {
					asyncMonitor.close();
				}
				return null;
			}

			/* Runs on the event-dispatching thread. */
			@Override
			public void finished() {
				if (uiErrorMsg.length() > 0) {
					LOGGER.severe(uiErrorMsg);
				}
				synchronized (asListener) {
					asListener.completedIO(uiMessage, uiErrorMsg);
				}
			}
		};
		worker.start();
	}

	/*
	 * non-javadoc: Asynchronized write
	 */
	private void spawnAsyncWriteFile(final File file, final List<Group> groups,
			final List<AbstractHistogram> histograms, final boolean writeData,
			final boolean wrtSettings) {
		this.uiMessage = "";
		this.uiErrorMsg = "";
		final AbstractSwingWorker worker = new AbstractSwingWorker() {
			@Override
			public Object construct() {
				HDFIO.this.asyncWriteFile(file, groups, histograms, writeData,
						wrtSettings);
				return null;
			}

			@Override
			public void finished() {
				if (HDFIO.this.uiErrorMsg.length() == 0) {
					LOGGER.info(uiMessage);
				} else {
					LOGGER.severe(uiErrorMsg);
				}
			}
		};
		worker.start();
	}

	private static final List<AbstractHistogram> EMPTY_HIST_LIST = Collections
			.unmodifiableList(new ArrayList<AbstractHistogram>());

	private static final List<Group> EMPTY_GROUP_LIST = Collections
			.unmodifiableList(new ArrayList<Group>());

	public void writeFile(final File file) {
		writeFile(file, EMPTY_GROUP_LIST, EMPTY_HIST_LIST, true, true);
	}

	/**
	 * Write out an HDF file, specifying whether scalers and parameters should
	 * be included.
	 * 
	 * @param file
	 *            to write to
	 * @param writeData
	 *            whether to write histograms and scalers
	 * @param wrtSettings
	 *            whether to write gates and parameters
	 */
	public void writeFile(final File file, final boolean writeData,
			final boolean wrtSettings) {
		writeFile(file, EMPTY_GROUP_LIST, EMPTY_HIST_LIST, writeData,
				wrtSettings);
	}

	public void writeFile(final File file, final Group group) {
		final List<Group> groupList = Collections.singletonList(group);
		writeFile(file, groupList, EMPTY_HIST_LIST, true, true);
	}

	public void writeFile(final File file,
			final List<AbstractHistogram> histograms) {
		writeFile(file, EMPTY_GROUP_LIST, histograms, true, true);
	}

	/**
	 * Create list of groups and histograms to write out. Use selected groups to
	 * create list of histograms or use selected histograms to create list of
	 * gates
	 * 
	 * @param file
	 *            to write to
	 * @param groups
	 *            if given, groups to write
	 * @param histograms
	 *            to write if groups not given
	 * @param writeData
	 *            whether to write histograms and scalers
	 * @param wrtSettings
	 *            whether to write gates and parameters
	 */
	private void writeFile(final File file, final List<Group> groups,
			final List<AbstractHistogram> histograms, final boolean writeData,
			final boolean wrtSettings) {
		/* Groups specified determines histograms */
		final List<Group> groupsToUse;
		final List<AbstractHistogram> histsToUse;
		final boolean haveGroups = !groups.isEmpty();
		final boolean haveHists = !histograms.isEmpty();
		if (haveGroups) {
			groupsToUse = groups;
			histsToUse = new ArrayList<>();
			for (Group currGroup : groups) {
				histsToUse.addAll(currGroup.histograms.getList());
			}
		} else if (haveHists) {
			/* Histograms specified determines groups. */
			groupsToUse = new ArrayList<>();
			histsToUse = histograms;
			for (AbstractHistogram hist : histsToUse) {
				final Group group = DataUtility.getGroup(hist);
				if (!groupsToUse.contains(group)) {
					groupsToUse.add(group);
				}
			}
		} else {
			/* Neither groups nor histograms specified */
			groupsToUse = GROUPS.getList();
			histsToUse = new ArrayList<>();
			for (Group currGroup : groupsToUse) {
				histsToUse.addAll(currGroup.histograms.getList());
			}
		}
		// Append .hdf to file name
		final String path = file.getParent();
		final String fileName = fileUtilities.changeExtension(file.getName(),
				HDF_FILE_EXT, FileUtilities.APPEND_ONLY);
		final String fileFullName = path + File.separator + fileName;
		final File appendFile = new File(fileFullName);
		if (fileUtilities.overWriteExistsConfirm(appendFile)) {
			spawnAsyncWriteFile(appendFile, groupsToUse, histsToUse, writeData,
					wrtSettings);
		}
	}
}
