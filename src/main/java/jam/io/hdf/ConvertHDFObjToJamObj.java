package jam.io.hdf;

import com.google.inject.Inject;
import jam.data.*;
import jam.data.func.AbstractCalibrationFunction;
import jam.data.func.CalibrationFitException;
import jam.data.func.CalibrationFunctionCollection;
import jam.io.FileOpenMode;
import jam.util.StringUtilities;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static jam.io.hdf.JamFileFields.Calibration.TYPE_COEFF;
import static jam.io.hdf.JamFileFields.Calibration.TYPE_POINTS;

/**
 * @author Ken Swartz
 * @version 15 Feb 2005
 */
final class ConvertHDFObjToJamObj {

    private transient final StringUtilities stringUtilities;

    private transient HDFile inHDF;

    @Inject
    ConvertHDFObjToJamObj(final StringUtilities stringUtilities) {
        super();
        this.stringUtilities = stringUtilities;
    }

    private AbstractHistogram addHistogram(final Group group,
            final String name, final Object histData) {
        final AbstractHistogram histogram = group.histograms
                .get(stringUtilities.makeLength(name,
                        AbstractHistogram.NAME_LENGTH));
        if (histogram != null) {
            histogram.addCounts(histData);
        }
        return histogram;
    }

    private HistogramAttributes attributesHistogram(final String groupName,
            final String name, final String title, final int number) {
        return new HistogramAttributes(groupName, name, title, number);
    }

    protected boolean containsGroup(final String groupName,
            final List<Group> groups) {
        boolean rval = false;
        for (Group group : groups) {
            if (groupName.equals(group.getGroupName())) {
                rval = true;
                break;
            }
        }
        return rval;
    }

    private boolean containsHistogramAttribute(final String groupName,
            final String histName, final List<HistogramAttributes> attributes) {
        boolean rval = false;
        for (HistogramAttributes histAttribute : attributes) {
            if (groupName == null) {
                if (histName.equals(histAttribute.getName())) {
                    rval = true;
                    break;
                }
            } else {
                if (groupName.equals(histAttribute.getGroupName())
                        && histName.equals(histAttribute.getName())) {
                    rval = true;
                    break;
                }
            }
        }
        return rval;
    }

    protected AbstractCalibrationFunction convertCalibration(
            final AbstractHistogram hist, final VDataDescription vdd)
            throws HDFException {
        final VData data = AbstractData.getObject(VData.class, vdd.getRef());
        final String funcName = vdd.getName();
        final String dataTypeName = vdd.getDataTypeName();
        final int numbPts = vdd.getNumRows();
        final AbstractCalibrationFunction calFunc = makeCalibration(funcName);
        if (calFunc != null) {
            final int numPts = vdd.getNumRows();
            switch (dataTypeName) {
                case TYPE_POINTS:
                    double[] ptsChannel = new double[numbPts];
                    double[] ptsEnergy = new double[numbPts];
                    for (int i = 0; i < numPts; i++) {
                        ptsChannel[i] = data.getDouble(i, 0);
                        ptsEnergy[i] = data.getDouble(i, 1);
                    }
                    calFunc.setPoints(ptsChannel, ptsEnergy);
                    try {
                        calFunc.fit();
                    } catch (CalibrationFitException de) {
                        throw new HDFException(
                                "Cannot create fit for calibration function "
                                        + funcName, de);
                    }
                    break;
                case TYPE_COEFF:
                    double[] coeff = new double[numbPts];
                    for (int i = 0; i < numPts; i++) {
                        coeff[i] = data.getDouble(i, 0);
                    }
                    calFunc.setCoeff(coeff);
                    break;
                default:
                    throw new HDFException("Unrecognized calibration type");
            }
        }

        ((AbstractHist1D) hist).setCalibration(calFunc);

        return calFunc;
    }

    protected Gate convertGate(final AbstractHistogram hist,
            final VDataDescription vdd, final String gateName,
            final FileOpenMode mode) {
        final Gate gate;
        final Polygon shape = new Polygon();
        final VData data = AbstractData.getObject(VData.class, vdd.getRef());
        // corresponding VS
        final int numRows = vdd.getNumRows();
        if (mode.isOpenMode()) {
            gate = makeGate(hist, gateName);
        } else { // reload
            final String histName = hist.getFullName();
            final String gateNameMod = stringUtilities.makeLength(gateName,
                    Gate.NAME_LENGTH);
            final String gateFullName = stringUtilities.makeFullName(histName,
                    gateNameMod);
            gate = Gate.getGate(gateFullName);
        }
        if (gate != null) {
            if (gate.getDimensionality() == 1) { // 1-d gate
                gate.setLimits(data.getInteger(0, 0), data.getInteger(0, 1));
            } else { // 2-d gate
                shape.reset();
                for (int i = 0; i < numRows; i++) {
                    shape.addPoint(data.getInteger(i, 0), data
                            .getInteger(i, 1));
                }
                gate.setLimits(shape);
            }
        }
        return gate;
    }

    protected Gate convertGate(final AbstractHistogram hist,
            final VirtualGroup currVG, final FileOpenMode mode)
            throws HDFException {
        // Get VDD member of Virtual group
        final VDataDescription vdd = AbstractData.ofType(currVG.getObjects(),
                VDataDescription.class).get(0);
        final String gname = currVG.getName();
        if (vdd == null) {
            throw new HDFException(
                    "No VdataDescription under gate VirtualGroup");
        }
        return convertGate(hist, vdd, gname, mode);
    }

    /*
     * non-javadoc: Retrieve the gates from the file.
     * 
     * @param mode whether to open or reload @throws HDFException thrown if
     * unrecoverable error occurs
     */
    protected int convertGatesOriginal(final Group currentGroup,
            final FileOpenMode mode) throws HDFException {
        int numGates = 0;
        // Gate gate = null;
        /* get list of all VG's in file */
        final List<VirtualGroup> groups = AbstractData
                .ofType(VirtualGroup.class);
        /* get only the "gates" VG (only one element) */
        final VirtualGroup gates = VirtualGroup.ofName(groups,
                JamFileFields.GATE_SECTION);
        final List<DataIDAnnotation> annotations = AbstractData
                .ofType(DataIDAnnotation.class);
        if (gates != null) {
            for (AbstractData data : gates.getObjects()) {
                final VirtualGroup currVG = (VirtualGroup) data;
                final String hname = DataIDAnnotation.withTagRef(annotations,
                        VirtualGroup.class, currVG.getRef()).getNote();
                final String groupName = currentGroup.getName();
                final String histFullName = groupName
                        + "/"
                        + stringUtilities.makeLength(hname,
                                AbstractHistogram.NAME_LENGTH);
                final AbstractHistogram hist = AbstractHistogram
                        .getHistogram(histFullName);
                final Gate gate = convertGate(hist, currVG, mode);
                if (gate != null) {
                    numGates++;
                }
            }
        }
        return numGates;
    }

    /*
     * non-javadoc: Convert a virtual group to a jam data group
     */
    protected Group convertGroup(final VirtualGroup virtualGroup,
            final String fileName,
            final List<HistogramAttributes> histAttributeList,
            final FileOpenMode mode) {
        Group group = null;
        String groupName;

        final DataIDLabel dataIDLabel = DataIDLabel.withTagRef(
                VirtualGroup.class, virtualGroup.getRef());
        groupName = dataIDLabel.getLabel();
        if (hasHistogramsInList(virtualGroup, groupName, histAttributeList)) {
            /* Don't use file name for group name for open. */
            final String fname = mode == FileOpenMode.OPEN ? null : fileName;
            group = Factory.createGroup(groupName, fname, Group.Type.FILE);
        }
        return group;
    }

    protected HistogramAttributes convertHistogamAttributes(
            final String groupName, final VirtualGroup histGroup,
            final FileOpenMode mode) throws HDFException {
        final DataIDLabel dataLabel = DataIDLabel.withTagRef(
                VirtualGroup.class, histGroup.getRef());
        final String name = dataLabel.getLabel();
        final DataIDAnnotation dataNote = DataIDAnnotation.withTagRef(
                VirtualGroup.class, histGroup.getRef());
        final String title = dataNote.getNote();

        /* only the "histograms" VG (only one element) */
        final List<NumericalDataGroup> tempVec = AbstractData.ofType(histGroup
                .getObjects(), NumericalDataGroup.class);
        final NumericalDataGroup[] dataGroups = tempVec
                .toArray(new NumericalDataGroup[tempVec.size()]);
        NumericalDataGroup ndg;
        if (dataGroups.length == 1) {
            ndg = dataGroups[0]; // only one NDG -- the data
        } else if (dataGroups.length == 2) {
            if (DataIDLabel.withTagRef(NumericalDataGroup.class,
                    dataGroups[0].getRef()).getLabel().equals(
                    JamFileFields.ERROR_LABEL)) {
                ndg = dataGroups[1];
            } else {
                ndg = dataGroups[0];
            }
        } else {
            throw new HDFException("Invalid number of data groups ("
                    + dataGroups.length + ") in VirtualGroup.");
        }
        final DataIDLabel numLabel = DataIDLabel.withTagRef(
                NumericalDataGroup.class, ndg.getRef());
        final int number = Integer.parseInt(numLabel.getLabel());
        /* Given name list check that that the name is in the list. */
        HistogramAttributes histAttributes = null;
        if (mode == FileOpenMode.ATTRIBUTES) {
            histAttributes = attributesHistogram(groupName, name, title,
                    number);
        }
        return histAttributes;
    }

    protected AbstractHistogram convertHistogram(final Group group,
            final VirtualGroup histGroup,
            final List<HistogramAttributes> histAttributes,
            final FileOpenMode mode) throws HDFException {
        AbstractHistogram rval = null;
        final DataIDLabel dataLabel = DataIDLabel.withTagRef(
                VirtualGroup.class, histGroup.getRef());
        final String name = dataLabel.getLabel();
        final DataIDAnnotation dataNote = DataIDAnnotation.withTagRef(
                VirtualGroup.class, histGroup.getRef());
        final String title = dataNote.getNote();
        /* only the "histograms" VG (only one element) */
        final List<NumericalDataGroup> ndgList = AbstractData.ofType(histGroup
                .getObjects(), NumericalDataGroup.class);
        final int len = ndgList.size();
        if (len > 0 && len < 3) {
            final NumericalDataGroup ndg;
            /* check ndgErr==null to determine if error bars exist */
            NumericalDataGroup ndgErr = null;
            if (len == 1) {
                ndg = ndgList.get(0); // only one NDG -- the data
            } else if (len == 2) {
                final NumericalDataGroup element0 = ndgList.get(0);
                if (DataIDLabel.withTagRef(NumericalDataGroup.class,
                        element0.getRef()).getLabel().equals(
                        JamFileFields.ERROR_LABEL)) {
                    ndg = ndgList.get(1);
                    ndgErr = element0;
                } else {
                    ndg = element0;
                    ndgErr = ndgList.get(1);
                }
            } else {
                throw new HDFException(
                        "Encountered numerical data group with # of lists != 1 or 2: "
                                + len);
            }
            rval = extractHistData(group, mode, histAttributes, ndg, ndgErr,
                    name, title);
        } else {
            // Can reload without this histogram
            if (mode == FileOpenMode.RELOAD) {
                rval = group.histograms.get(stringUtilities.makeLength(name,
                        AbstractHistogram.NAME_LENGTH));
            }
        }
        return rval;
    }

    private AbstractHistogram extractHistData(final Group group,
            final FileOpenMode mode,
            final List<HistogramAttributes> histAttributes,
            final NumericalDataGroup ndg, final NumericalDataGroup ndgErr,
            final String name, final String title) throws HDFException {
        AbstractHistogram rval = null;
        final DataIDLabel numLabel = DataIDLabel.withTagRef(
                NumericalDataGroup.class, ndg.getRef());
        final int number = Integer.parseInt(numLabel.getLabel());
        final ScientificDataDimension sdd = AbstractData.ofType(
                ndg.getObjects(), ScientificDataDimension.class).get(0);
        final byte histNumType = sdd.getType();
        final int histDim = sdd.getRank();
        final int sizeX = sdd.getSizeX();
        final int sizeY = histDim == 2 ? sdd.getSizeY() : 0;
        final ScientificData sciData = AbstractData.ofType(ndg.getObjects(),
                ScientificData.class).get(0);
        sciData.setNumberType(histNumType);
        sciData.setRank(histDim);
        final ScientificData sdErr = produceErrorData(ndgErr, histDim);
        /* Given name list check that that the name is in the list. */
        if (histAttributes == null
                || containsHistogramAttribute(group.getGroupName(), name,
                        histAttributes)) {
            final HistogramAttributes attr = new HistogramAttributes(group
                    .getName(), name, title, number);
            rval = generateHistogram(attr, mode, sciData, sdErr, histDim,
                    histNumType, sizeX, sizeY);
        }
        return rval;
    }

    private AbstractHistogram generateHistogram(
            final HistogramAttributes attr, final FileOpenMode mode,
            final ScientificData sciData, final ScientificData sdErr,
            final int histDim, final byte histNumType, final int sizeX,
            final int sizeY) throws HDFException {
        final AbstractHistogram rval;
        final Group group = jam.data.Warehouse.getGroupCollection().get(
                attr.getGroupName());
        final String name = attr.getName();
        final String title = attr.getTitle();
        final int number = attr.getNumber();
        if (mode.isOpenMode()) {
            final Object histData = sciData.getData(inHDF, histDim,
                    histNumType, sizeX, sizeY);
            Object histErrData = null;
            if (sdErr != null) {
                histErrData = sdErr.getData1dD(inHDF, sizeX);
            }
            rval = openHistogram(group, name, title, number, histData,
                    histErrData);
        } else if (mode == FileOpenMode.RELOAD) {
            final Object histData = sciData.getData(inHDF, histDim,
                    histNumType, sizeX, sizeY);
            rval = reloadHistogram(group, name, histData);
        } else if (mode == FileOpenMode.ADD) {
            final Object histData = sciData.getData(inHDF, histDim,
                    histNumType, sizeX, sizeY);
            rval = addHistogram(group, name, histData);
        } else {
            rval = null;
        }
        return rval;
    }

    /*
     * non-javadoc: looks for the special Histogram section and reads the data
     * into memory.
     * 
     * @param mode whether to open or reload @param sb summary message under
     * construction @param histNames names of histograms to read, null if all
     * 
     * @exception HDFException thrown if unrecoverable error occurs @throws
     * IllegalStateException if any histogram apparently has more than 2
     * dimensions @return number of histograms
     */
    protected int convertHistogramsOriginal(final Group group,
            final FileOpenMode mode,
            final List<HistogramAttributes> histAttributes)
            throws HDFException {
        int numHists = 0;
        final VirtualGroup hists = VirtualGroup
                .ofName(JamFileFields.HIST_SECTION);
        /* only the "histograms" VG (only one element) */
        if (hists != null) {
            for (AbstractData data : hists.getObjects()) {
                final VirtualGroup currHistGrp = (VirtualGroup) data;
                final AbstractHistogram hist = convertHistogram(group,
                        currHistGrp, histAttributes, mode);
                if (hist != null) {
                    numHists++;
                }
            }
            // after loop
        }
        return numHists;
    }

    /*
     * non-javadoc: retrieve the parameters from the file
     * 
     * @param mode whether to open or reload @throws HDFException if an error
     * occurs reading the parameters
     */
    protected int convertParameters(final VDataDescription vdd,
            final FileOpenMode mode) {
        /* Get corresponding VS for this VH */
        final VData data = AbstractData.getObject(VData.class, vdd.getRef());
        int numParams = vdd.getNumRows();
        for (int i = 0; i < numParams; i++) {
            final String pname = data.getString(i, 0);
            /* make if OPEN, retrieve if RELOAD */
            final DataParameter param = produceParameter(mode, pname);
            if (param != null) {
                param.setValue(data.getFloat(i, 1));
            }
        }
        return numParams;
    }

    protected int convertParameters(final VirtualGroup currVG,
            final FileOpenMode mode) {
        int numParameters = 0;
        final List<VDataDescription> list = AbstractData.ofType(currVG
                .getObjects(), VDataDescription.class);
        if (!list.isEmpty()) {
            final VDataDescription vdd = list.get(0);
            /* only the "parameters" VH (only one element) in the file */
            if (vdd != null) {
                numParameters = convertParameters(vdd, mode);
            }
        }
        return numParameters;
    }

    /*
     * non-javadoc: Retrieve the scalers from the file.
     * 
     * @param mode whether to open, reload or add @throws HDFException if there
     * is a problem retrieving scalers
     */
    protected int convertScalers(final Group group,
            final VDataDescription vdd, final FileOpenMode mode) {
        /* get the VS corresponding to the given VH */
        final VData data = AbstractData.getObject(VData.class, vdd.getRef());
        int numScalers = vdd.getNumRows();
        for (int i = 0; i < numScalers; i++) {
            final String sname = data.getString(i, 1);
            final int sNumber = data.getInteger(i, 0);
            final Scaler scaler = produceScaler(group, mode, sNumber, sname);
            if (scaler != null) {
                final int fileValue = data.getInteger(i, 2);
                if (mode == FileOpenMode.ADD) {
                    scaler.setValue(scaler.getValue() + fileValue);
                } else {
                    scaler.setValue(fileValue);
                }
            }
        }
        return numScalers;
    }

    protected int convertScalers(final Group group, final VirtualGroup currVG,
            final FileOpenMode mode) {
        final VDataDescription vdd = AbstractData.ofType(currVG.getObjects(),
                VDataDescription.class).get(0);
        return convertScalers(group, vdd, mode);
    }

    protected VDataDescription findCalibration(final VirtualGroup virtualGroup) {
        VDataDescription vddCalibration = null;
        final VDataDescription vddpts = findVData(virtualGroup, TYPE_POINTS);
        if (vddpts != null) {
            vddCalibration = vddpts;
        }
        final VDataDescription vddcoef = findVData(virtualGroup, TYPE_COEFF);
        if (vddcoef != null) {
            vddCalibration = vddcoef;
        }
        return vddCalibration;
    }

    protected List<VirtualGroup> findGates(final VirtualGroup vgHists,
            final HistogramType type) throws HDFException {
        String gateType;
        if (type.getDimensionality() == HistogramType.ONE_D) {
            gateType = JamFileFields.GATE_1D_TYPE;
        } else if (type.getDimensionality() == HistogramType.TWO_D) {
            gateType = JamFileFields.GATE_2D_TYPE;
        } else {
            throw new HDFException("Unkown Histogram type in finding gates");
        }
        final List<String> empty = Collections.emptyList();
        return findSubGroups(vgHists, gateType, empty);
    }

    /*
     * non-javadoc: looks for the special Histogram section and reads the data
     * into memory.
     * 
     * @param mode whether to open or reload @param sb summary message under
     * construction @param histNames names of histograms to read, null if all
     * 
     * @exception HDFException thrown if unrecoverable error occurs @throws
     * IllegalStateException if any histogram apparently has more than 2
     * dimensions @return number of histograms
     */
    protected List<VirtualGroup> findGroups(final List<Group> existingGroupList) {
        final List<VirtualGroup> groupList = new ArrayList<>();
        /* Get VirtualGroup that is root of all groups */
        final VirtualGroup groupsInRoot = VirtualGroup
                .ofName(JamFileFields.GRP_SECTION);
        // Found root node
        if (groupsInRoot != null) {
            for (AbstractData hData : groupsInRoot.getObjects()) {
                // Is a virtual group
                if (hData instanceof VirtualGroup) {
                    // Cast to VirtualGroup and add to list
                    final VirtualGroup currentVGrp = (VirtualGroup) hData;
                    final String groupName = readVirtualGroupName(currentVGrp);
                    if (existingGroupList != null
                            && containsGroup(groupName, existingGroupList)) {
                        groupList.add(currentVGrp);
                    } else if (existingGroupList == null) {
                        groupList.add(currentVGrp);
                    }
                }
            }// loop
        }
        return groupList;
    }

    protected List<VirtualGroup> findHistograms(
            final VirtualGroup virtualGroupGroup,
            final List<String> histogramNames) {
        return findSubGroups(virtualGroupGroup, JamFileFields.HIST_TYPE,
                histogramNames);
    }

    protected List<VirtualGroup> findParameters(
            final VirtualGroup virtualGroupGroup) {
        return findSubGroupsName(virtualGroupGroup, JamFileFields.PARAMETERS);
    }

    protected VDataDescription findParametersOriginal() {
        final VDataDescription vdd = VDataDescription.ofName(AbstractData
                .ofType(VDataDescription.class), JamFileFields.PARAMETERS);
        return vdd;
    }

    protected List<VirtualGroup> findScalers(
            final VirtualGroup virtualGroupGroup) {
        return findSubGroupsName(virtualGroupGroup, JamFileFields.SCALER_SECT);
    }

    /*
     * non-javadoc: Retrieve the scalers from the file.
     * 
     * @param mode whether to open, reload or add @throws HDFException if there
     * is a problem retrieving scalers
     */

    protected VDataDescription findScalersOriginal() {
        final VDataDescription vdd = VDataDescription.ofName(AbstractData
                .ofType(VDataDescription.class), JamFileFields.SCALER_SECT);
        return vdd;
    }

    private List<VirtualGroup> findSubGroups(
            final VirtualGroup virtualGroupGroup, final String groupType,
            final List<String> groupNameList) {
        final List<VirtualGroup> groupSubList = new ArrayList<>();
        for (AbstractData hData : virtualGroupGroup.getObjects()) {
            // Is a virtual group
            if (hData instanceof VirtualGroup) {
                // add to list if is a scaler goup
                final VirtualGroup currentVGroup = (VirtualGroup) hData;
                final String groupName = readVirtualGroupName(currentVGroup);
                if (currentVGroup.getType().equals(groupType)
                        && (groupNameList.isEmpty() || groupNameList
                                .contains(groupName))) {
                    groupSubList.add(currentVGroup);
                }
            }
        }
        return groupSubList;
    }

    private List<VirtualGroup> findSubGroupsName(
            final VirtualGroup virtualGroupGroup, final String groupName) {
        final List<VirtualGroup> groupSubList = new ArrayList<>();
        for (AbstractData hData : virtualGroupGroup.getObjects()) {
            // Is a virtual group
            if (hData instanceof VirtualGroup) {
                // add to list if is a scaler goup
                final VirtualGroup currentVGroup = (VirtualGroup) hData;
                if (currentVGroup.getName().equals(groupName)) {
                    groupSubList.add(currentVGroup);
                }
            }
        }
        return groupSubList;
    }

    protected VDataDescription findVData(final VirtualGroup virtualGroupGroup,
            final String dataType) {
        VDataDescription vdd = null;
        for (AbstractData hData : virtualGroupGroup.getObjects()) {
            // Is a virtual data descriptor
            if (hData instanceof VDataDescription) {
                // add to list if is a scaler goup
                final VDataDescription currentVDD = (VDataDescription) hData;
                if (currentVDD.getDataTypeName().equals(dataType)) {
                    vdd = currentVDD;
                    break;
                }
            }
        }
        return vdd;
    }

    protected boolean hasHistogramsInList(final VirtualGroup virtualGroup,
            final String groupName,
            final List<HistogramAttributes> histAttributeList) {
        boolean rtnVal = true;
        final List<String> nameList = new ArrayList<>();
        // Check group has histograms
        if (histAttributeList != null) {
            for (HistogramAttributes histAttribute : histAttributeList) {
                final String histName = histAttribute.getName();
                final String histGroupName = histAttribute.getGroupName();
                if (groupName.equals(histGroupName)) {
                    nameList.add(histName);
                }
            }
            final List<VirtualGroup> histList = findHistograms(virtualGroup,
                    nameList);
            final int size = histList.size();
            rtnVal = (size > 0);
        }
        return rtnVal;
    }

    /*
     * non-javadoc: Retrieve the scalers from the file.
     * 
     * @param mode whether to open, reload or add @throws HDFException if there
     * is a problem retrieving scalers
     */

    protected boolean hasVGroupRootGroup() {
        boolean hasRoot = false;
        final VirtualGroup groupsRoot = VirtualGroup
                .ofName(JamFileFields.GRP_SECTION);
        if (groupsRoot != null) {
            hasRoot = true;
        }
        return hasRoot;
    }

    private AbstractCalibrationFunction makeCalibration(final String funcName)
            throws HDFException {
        final Map<String, Class<? extends AbstractCalibrationFunction>> calMap = CalibrationFunctionCollection
                .getMapFunctions();
        AbstractCalibrationFunction calFunc = null;
        try {
            if (calMap.containsKey(funcName)) {
                calFunc = calMap.get(funcName).getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new HDFException("Cannot create calibration  " + funcName, e);
        }
        return calFunc;
    }

    private Gate makeGate(final AbstractHistogram hist, final String name) {
        return hist == null ? null : new Gate(name, hist);
    }

    protected AbstractHistogram openHistogram(final Group group,
            final String name, final String title, final int number,
            final Object histData, final Object histErrorData) {
        final AbstractHistogram histogram = Factory.createHistogram(group,
                histData, name, title);
        histogram.setNumber(number);
        if (histErrorData != null) {
            ((AbstractHist1D) histogram).setErrors((double[]) histErrorData);
        }
        return histogram;
    }

    private ScientificData produceErrorData(final NumericalDataGroup ndgErr,
            final int histDim) {
        ScientificData rval = null;
        if (ndgErr != null) {
            rval = AbstractData.ofType(ndgErr.getObjects(),
                    ScientificData.class).get(0);
            rval.setRank(histDim);
            rval.setNumberType(NumberType.DOUBLE);
        }
        return rval;
    }

    private DataParameter produceParameter(final FileOpenMode mode,
            final String name) {
        final DataParameter param = mode.isOpenMode() ? new DataParameter(name)
                : DataParameter.getParameter(name);
        return param;
    }

    private Scaler produceScaler(final Group group, final FileOpenMode mode,
            final int number, final String name) {
        // FIXME check works for unique name and
        // create unique name should be a utility
        final String uniqueName = group.getName() + "/" + name;
        return mode.isOpenMode() ? Factory.createScaler(group, name, number)
                : Scaler.getScaler(uniqueName);
    }

    protected String readVirtualGroupName(final VirtualGroup group) {
        final DataIDLabel dataIDLabel = DataIDLabel.withTagRef(
                VirtualGroup.class, group.getRef());
        final String rval;
        if (dataIDLabel == null) {// somehow label doesn't exist
            rval = group.getName();
        } else {
            rval = dataIDLabel.getLabel();
        }
        return rval;
    }

    private AbstractHistogram reloadHistogram(final Group group,
            final String name, final Object histData) {
        final AbstractHistogram histogram = group.histograms
                .get(stringUtilities.makeLength(name,
                        AbstractHistogram.NAME_LENGTH));
        if (histogram != null) {
            histogram.setCounts(histData);
        }
        return histogram;
    }

    protected void setInFile(final HDFile infile) {
        inHDF = infile;
    }
}
