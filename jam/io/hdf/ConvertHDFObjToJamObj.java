package jam.io.hdf;

import jam.data.AbstractHist1D;
import jam.data.DataParameter;
import jam.data.Gate;
import jam.data.Group;
import jam.data.Histogram;
import jam.data.Scaler;
import jam.io.FileOpenMode;
import jam.util.StringUtilities;

import java.awt.Polygon;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Ken Swartz
 * @version 15 Feb 2005
 */
final class ConvertHDFObjToJamObj implements JamHDFFields {

	final StringUtilities STRING_UTIL = StringUtilities.instance();
	
    private HDFile inHDF;

    ConvertHDFObjToJamObj() {
        super();
    }

    void setInFile(HDFile infile) {
        inHDF = infile;
    }

    /** 
     * looks for the special Histogram section and reads the data into
     * memory.
     * 
     * @param mode
     *            whether to open or reload
     * @param sb
     *            summary message under construction
     * @param histNames
     *            names of histograms to read, null if all
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     * @throws IllegalStateException
     *             if any histogram apparently has more than 2 dimensions
     * @return number of histograms
     */
    List findGroups(FileOpenMode mode, List groupNames) throws HDFException {
    	final List groupList = new ArrayList();
        /* Get VirtualGroup that is root of all groups */
        final VirtualGroup groupsInRoot = VirtualGroup.ofName(GROUP_SECT);
        //Found root node
        if (groupsInRoot != null) {   	
            //Group iterator
            final Iterator groupIter = groupsInRoot.getObjects().iterator();
            // loop begin
            while (groupIter.hasNext()) {
            	final AbstractHData hData = (AbstractHData)groupIter.next();
            	//Is a virtual group
            	if ( hData.getTag() == AbstractHData.DFTAG_VG ) {
            		//Cast to VirtualGroup and add to list
            		final VirtualGroup currentVGrp = (VirtualGroup)hData;
            		if (groupNames==null) {
            			groupList.add(currentVGrp);
            		} else {
            			groupList.add(currentVGrp);
            		}
            	}
            }//loop
        }
        return groupList;
    }
    
    /* non-javadoc:
     * Convert a virtual group to a jam data group
     */
	Group convertGroup(VirtualGroup virtualGroup){
		final DataIDLabel dataIDLabel = DataIDLabel.withTagRef(virtualGroup.getTag(),
				virtualGroup.getRef());
		return new Group(dataIDLabel.getLabel(), Group.Type.FILE);
	}
	
    List findHistograms(VirtualGroup virtualGroupGroup, List histNames) {
        final List histList = new ArrayList();
        final Iterator histIter = virtualGroupGroup.getObjects().iterator();
        while (histIter.hasNext()) {
            final AbstractHData hData = (AbstractHData) histIter.next();
            //Is a virtual group
            if (hData.getTag() == AbstractHData.DFTAG_VG) {
                //add to list if is a histogram goup
                final VirtualGroup currentVGrp = (VirtualGroup) hData;
                if (currentVGrp.getName().equals(HIST_TYPE)) {
                    histList.add(currentVGrp);
                }
            }
        }
        return histList;
    }
    
    /** 
     * looks for the special Histogram section and reads the data into
     * memory.
     * 
     * @param mode
     *            whether to open or reload
     * @param sb
     *            summary message under construction
     * @param histNames
     *            names of histograms to read, null if all
     * @exception HDFException
     *                thrown if unrecoverable error occurs
     * @throws IllegalStateException
     *             if any histogram apparently has more than 2 dimensions
     * @return number of histograms
     */
    int convertHistograms(Group group, FileOpenMode mode, List histNames) throws HDFException {
        int numHists = 0;
        final VirtualGroup hists = VirtualGroup.ofName(HIST_SECTION);
        /* only the "histograms" VG (only one element) */
        if (hists != null) {
            numHists = hists.getObjects().size();
            /* Histogram iterator */
            final Iterator temp = hists.getObjects().iterator();
            // loop begin
            while (temp.hasNext()) {
            	final VirtualGroup currHistGrp = (VirtualGroup) (temp.next());
            	convertHist(group, currHistGrp, histNames, mode);
            }
            //after loop
        }
        return numHists;
    }
    
    
    Histogram  convertHist(Group group, VirtualGroup histGroup,  List histNames, FileOpenMode mode) throws HDFException {
    	Histogram hist =null;
    	
            final NumericalDataGroup ndg;
            /* I check ndgErr==null to determine if error bars exist */
            NumericalDataGroup ndgErr = null;
            /* only the "histograms" VG (only one element) */
            final List tempVec = AbstractHData.ofType(histGroup.getObjects(),
                    AbstractHData.DFTAG_NDG);
            final NumericalDataGroup[] dataGroups = getNumericalGroups(tempVec);
            if (dataGroups.length == 1) {
                ndg = dataGroups[0]; //only one NDG -- the data
            } else if (dataGroups.length == 2) {
                if (DataIDLabel.withTagRef(AbstractHData.DFTAG_NDG,
                        dataGroups[0].getRef()).getLabel().equals(ERROR_LABEL)) {
                    ndg = dataGroups[1];
                    ndgErr = dataGroups[0];
                } else {
                    ndg = dataGroups[0];
                    ndgErr = dataGroups[1];
                }
            } else {
            	throw new HDFException( "Invalid number of data groups (" + dataGroups.length
                        + ") in VirtualGroup.");
            }
            final ScientificData sciData = (ScientificData) (AbstractHData
                    .ofType(ndg.getObjects(), AbstractHData.DFTAG_SD).get(0));
            final ScientificDataDimension sdd = (ScientificDataDimension) (AbstractHData
                    .ofType(ndg.getObjects(), AbstractHData.DFTAG_SDD).get(0));
            final byte histNumType = sdd.getType();
            sciData.setNumberType(histNumType);
            final int histDim = sdd.getRank();
            sciData.setRank(histDim);
            final int sizeX = sdd.getSizeX();
            final int sizeY = histDim == 2 ? sdd.getSizeY() : 0;

            final DataIDLabel numLabel =DataIDLabel.withTagRef(ndg.getTag(), ndg.getRef());
            final int number = Integer.parseInt(numLabel.getLabel());            
            final DataIDLabel templabel = DataIDLabel.withTagRef(histGroup.getTag(), histGroup.getRef());
            final String name = templabel.getLabel();            
            final DataIDAnnotation tempnote = DataIDAnnotation.withTagRef(histGroup.getTag(), histGroup.getRef());            
            final String title = tempnote.getNote();
            
            final ScientificData sdErr = produceErrorData(ndgErr, histDim);
            
    		//FIXME KBS hack for now
            Group.setCurrentGroup(group);
            /* Given name list check that that the name is in the list. */
            if (histNames == null || histNames.contains(name)) {
            	final Object histData = sciData.getData(inHDF, histDim, histNumType, sizeX, sizeY);
                if (mode.isOpenMode()) {
                	Object histErrData =null;
                    if (sdErr != null) {
                    	histErrData = sdErr.getData1dD(inHDF, sizeX);
                    }
                	hist=openHistogram(group, name, title, number, histData, histErrData);
                } else if (mode == FileOpenMode.RELOAD) {
                	hist=reloadHistogram(group, name, histData);
                } else if  (mode == FileOpenMode.RELOAD) {                	
                	hist=addHistogram(group, name, histData);
                }
            }
            return hist;
    }
    
    private ScientificData produceErrorData(NumericalDataGroup ndgErr, int histDim) {
        final boolean exists = ndgErr != null;
        final ScientificData rval = exists ? (ScientificData) (AbstractHData.ofType(ndgErr
                .getObjects(), AbstractHData.DFTAG_SD).get(0)) : null;
        if (exists) {
            rval.setRank(histDim);
            rval.setNumberType(NumberType.DOUBLE);
        }
        return rval;
    }

    private NumericalDataGroup[] getNumericalGroups(List list) {
        final NumericalDataGroup[] rval = new NumericalDataGroup[list.size()];
        list.toArray(rval);
        return rval;
    }

    /*
     * non-javadoc: Retrieve the gates from the file.
     * 
     * @param mode whether to open or reload @throws HDFException thrown if
     * unrecoverable error occurs
     */
    int convertGates(FileOpenMode mode) throws HDFException {
        int numGates = 0;
        final StringUtilities util = StringUtilities.instance();
        //Gate gate = null;
        /* get list of all VG's in file */
        final List groups = AbstractHData.ofType(AbstractHData.DFTAG_VG);
        /* get only the "gates" VG (only one element) */
        final VirtualGroup gates = VirtualGroup.ofName(groups, GATE_SECTION);
        final List annotations = AbstractHData.ofType(AbstractHData.DFTAG_DIA);
        if (gates != null) {
            numGates = gates.getObjects().size();
            final Iterator temp = gates.getObjects().iterator();
            boolean errorOccured=false;
            final Polygon shape = new Polygon();
            gateLoop: while (temp.hasNext()) {
                final VirtualGroup currVG = (VirtualGroup) (temp.next());
                final VdataDescription vdd = (VdataDescription) (AbstractHData
                        .ofType(currVG.getObjects(), AbstractHData.DFTAG_VH)
                        .get(0));
                if (vdd == null) {
                    errorOccured=true;
                    break gateLoop;
                }
                final Vdata data = (Vdata) (AbstractHData.getObject(
                        AbstractHData.DFTAG_VS, vdd.getRef()));
                //corresponding VS
                final int numRows = vdd.getNumRows();
                final String gname = currVG.getName();
                final String hname = DataIDAnnotation.withTagRef(annotations,
                        currVG.getTag(), currVG.getRef()).getNote();
                final Gate gate;
                if (mode.isOpenMode()) {
                    final String groupName = Group.getCurrentGroup().getName();
                    final String histFullName = groupName + "/"
                            + util.makeLength(hname, Histogram.NAME_LENGTH);
                    final Histogram hist = Histogram.getHistogram(histFullName);
                    gate = makeGate(hist, gname);
                } else { //reload
                    gate = Gate.getGate(util
                            .makeLength(gname, Gate.NAME_LENGTH));
                }
                if (gate != null) {
                    if (gate.getDimensionality() == 1) { //1-d gate
                        gate.setLimits(data.getInteger(0, 0).intValue(), data
                                .getInteger(0, 1).intValue());
                    } else { //2-d gate
                        shape.reset();
                        for (int i = 0; i < numRows; i++) {
                            shape.addPoint(data.getInteger(i, 0).intValue(),
                                    data.getInteger(i, 1).intValue());
                        }
                        gate.setLimits(shape);
                    }
                }
            }
            if (errorOccured){
                throw new IllegalStateException("Problem processing a VH for a gate.");
            }
        }
        return numGates;
    }
    
    private Gate makeGate(Histogram hist, String name){
        return hist == null ? null : new Gate(name, hist);
    }

    /*
     * non-javadoc: Retrieve the scalers from the file.
     * 
     * @param mode whether to open, reload or add @throws HDFException if there
     * is a problem retrieving scalers
     */
    int convertScalers(FileOpenMode mode) throws HDFException {
        int numScalers = 0;
        final VdataDescription vdd = VdataDescription.ofName(AbstractHData
                .ofType(AbstractHData.DFTAG_VH), SCALER_SECT);
        /* only the "scalers" VH (only one element) in the file */
        if (vdd != null) {
            /* get the VS corresponding to the given VH */
            final Vdata data = (Vdata) (AbstractHData.getObject(
                    AbstractHData.DFTAG_VS, vdd.getRef()));
            numScalers = vdd.getNumRows();
            for (int i = 0; i < numScalers; i++) {
                final String sname = data.getString(i, 1);
                final int sNumber = data.getInteger(i, 0).intValue();
                final Scaler scaler = produceScaler(mode, sNumber, sname);
                if (scaler != null) {
                    final int fileValue = data.getInteger(i, 2).intValue();
                    if (mode == FileOpenMode.ADD) {
                        scaler.setValue(scaler.getValue() + fileValue);
                    } else {
                        scaler.setValue(fileValue);
                    }
                }
            }
        }
        return numScalers;
    }
    
    private Scaler produceScaler(FileOpenMode mode, int number, String name){
        return mode.isOpenMode() ? new Scaler(name,
                number) : Scaler
                .getScaler(name);        
    }

    /*
     * non-javadoc: retrieve the parameters from the file
     * 
     * @param mode whether to open or reload @throws HDFException if an error
     * occurs reading the parameters
     */
    int convertParameters(FileOpenMode mode) throws HDFException {
        int numParams = 0;
        final VdataDescription vdd = VdataDescription.ofName(AbstractHData
                .ofType(AbstractHData.DFTAG_VH), PARAMETERS);
        /* only the "parameters" VH (only one element) in the file */
        if (vdd != null) {
            /* Get corresponding VS for this VH */
            final Vdata data = (Vdata) (AbstractHData.getObject(
                    AbstractHData.DFTAG_VS, vdd.getRef()));
            numParams = vdd.getNumRows();
            for (int i = 0; i < numParams; i++) {
                final String pname = data.getString(i, 0);
                /* make if OPEN, retrieve if RELOAD */
                final DataParameter param = produceParameter(mode, pname);
                if (param != null) {
                    param.setValue(data.getFloat(i, 1).floatValue());
                }
            }
        }
        return numParams;
    }
    
    private DataParameter produceParameter(FileOpenMode mode, String name){
        final DataParameter param = mode.isOpenMode() ? new DataParameter(
                name)
                : DataParameter.getParameter(name);
        return param;
    }

    
    Histogram openHistogram(Group group, String name, String title, int number, 
            Object histData, Object histErrorData) {
        final Histogram histogram = Histogram.createHistogram(histData, name, title);
        histogram.setNumber(number);
        if (histErrorData != null) {
            ((AbstractHist1D) histogram).setErrors((double [])histErrorData);
        }
        return histogram;
    }
    
    private Histogram addHistogram(Group group, String name, Object histData) {
        final Histogram histogram = group.getHistogram(STRING_UTIL.makeLength(name,
                Histogram.NAME_LENGTH));
        if (histogram != null) {
            histogram.addCounts(histData);            
        }
        return histogram;
    }
    
    private Histogram reloadHistogram(Group group, String name, Object histData) {
        
        final Histogram histogram = group.getHistogram(STRING_UTIL.makeLength(name,
                Histogram.NAME_LENGTH));
        if (histogram != null) {
            histogram.setCounts(histData);
        }
        return histogram;
    }        
}

