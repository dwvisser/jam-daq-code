package jam.io.hdf;

import jam.data.AbstractHist1D;
import jam.data.DataException;
import jam.data.DataParameter;
import jam.data.Gate;
import jam.data.Group;
import jam.data.Histogram;
import jam.data.Scaler;
import jam.data.func.AbstractCalibrationFunction;
import jam.io.FileOpenMode;
import jam.util.StringUtilities;

import java.awt.Polygon;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * @author Ken Swartz
 * @version 15 Feb 2005
 */
final class ConvertHDFObjToJamObj implements JamFileFields {

	final StringUtilities STRING_UTIL = StringUtilities.instance();
	
    private HDFile inHDF;

    ConvertHDFObjToJamObj() {
        super();
    }

    void setInFile(HDFile infile) {
        inHDF = infile;
    }
    
    boolean hasVGroupRootGroup() throws HDFException {
    	boolean hasRoot =false;
        final VirtualGroup groupsRoot = VirtualGroup.ofName(GRP_SECTION);
        if (groupsRoot!=null) {
        	hasRoot =true;
        }
        return hasRoot;        	
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
    List findGroups(FileOpenMode mode, List existingGroupList) throws HDFException {
    	final List groupList = new ArrayList();
        /* Get VirtualGroup that is root of all groups */
        final VirtualGroup groupsInRoot = VirtualGroup.ofName(GRP_SECTION);
        //Found root node
        if (groupsInRoot != null) {   	
            //Group iterator
            final Iterator groupIter = groupsInRoot.getObjects().iterator();
            // loop begin
            while (groupIter.hasNext()) {
            	final AbstractData hData = (AbstractData)groupIter.next();
            	//Is a virtual group
            	if ( hData.getTag() == AbstractData.DFTAG_VG ) {
            		//Cast to VirtualGroup and add to list
            		final VirtualGroup currentVGrp = (VirtualGroup)hData;
            		String groupName =readVirtualGroupName(currentVGrp);            		
            		if (existingGroupList!=null && containsGroup(groupName, existingGroupList)) {
            			groupList.add(currentVGrp);
            		} else if (existingGroupList==null ){
            			groupList.add(currentVGrp);
            		}
            	}
            }//loop
        }
        return groupList;
    }
    
    boolean containsGroup(String groupName, List groupList){
    	boolean inList=false;
    	
    	Iterator groupIter =groupList.iterator();
    	while(groupIter.hasNext()) {
    		Group group =( Group)groupIter.next();
			if ( groupName.equals(group.getGroupName()) ) {
				inList =true;
				break;
			}
    	}
    	
    	return inList;
    }
    /* non-javadoc:
     * Convert a virtual group to a jam data group
     */
	Group convertGroup(VirtualGroup virtualGroup, String fileName, FileOpenMode mode){
		final DataIDLabel dataIDLabel = DataIDLabel.withTagRef(virtualGroup.getTag(),
				virtualGroup.getRef());
		/* Don't use file name for group name for open. */
		final String fname = mode==FileOpenMode.OPEN ? null : fileName;
		return Group.createGroup(dataIDLabel.getLabel(), fname, Group.Type.FILE);
	} 
	
	
	List findHistograms(VirtualGroup virtualGroupGroup, List histogramNames) throws HDFException {
		return findSubGroups(virtualGroupGroup, HIST_TYPE, histogramNames);
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
    int convertHistogramsOriginal(Group group, FileOpenMode mode, List histAttributes) throws HDFException {
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
            	convertHistogram(group, currHistGrp, histAttributes, mode);
            }
            //after loop 
        }
        return numHists;
    }
    
    
    Histogram  convertHistogram(Group group, VirtualGroup histGroup,  List histAttributes, FileOpenMode mode) throws HDFException {
    	
    	Histogram hist=null;    	
        final NumericalDataGroup ndg;
        NumericalDataGroup ndgErr = null; //check ndgErr==null to determine if error bars exist
        
        final DataIDLabel templabel = DataIDLabel.withTagRef(histGroup.getTag(), histGroup.getRef());
        final String name = templabel.getLabel();            
        final DataIDAnnotation tempnote = DataIDAnnotation.withTagRef(histGroup.getTag(), histGroup.getRef());            
        final String title = tempnote.getNote();
        
        /* only the "histograms" VG (only one element) */
        final List tempVec = AbstractData.ofType(histGroup.getObjects(),
                AbstractData.DFTAG_NDG);
        final NumericalDataGroup[] dataGroups = getNumericalGroups(tempVec);
        if (dataGroups.length == 1) {
            ndg = dataGroups[0]; //only one NDG -- the data
        } else if (dataGroups.length == 2) {
            if (DataIDLabel.withTagRef(AbstractData.DFTAG_NDG,
                    dataGroups[0].getRef()).getLabel().equals(ERROR_LABEL)) {
                ndg = dataGroups[1];
                ndgErr = dataGroups[0];
            } else {
                ndg = dataGroups[0];
                ndgErr = dataGroups[1];
            }
        } else {
        	if (mode == FileOpenMode.RELOAD) {
        		hist = group.getHistogram(STRING_UTIL.makeLength(name,
                    Histogram.NAME_LENGTH));
        	} else {
        		hist=null; //no histogram data
        	}
        	return hist; 
        	//FIXME KBS rem no longer the case
        	//throw new HDFException( "Invalid number of data groups (" + dataGroups.length
            //        + ") in VirtualGroup.");
        }
        final ScientificData sciData = (ScientificData) (AbstractData
                .ofType(ndg.getObjects(), AbstractData.DFTAG_SD).get(0));
        final ScientificDataDimension sdd = (ScientificDataDimension) (AbstractData
                .ofType(ndg.getObjects(), AbstractData.DFTAG_SDD).get(0));
        final byte histNumType = sdd.getType();
        sciData.setNumberType(histNumType);
        final int histDim = sdd.getRank();
        sciData.setRank(histDim);
        final int sizeX = sdd.getSizeX();
        final int sizeY = histDim == 2 ? sdd.getSizeY() : 0;
        
        final DataIDLabel numLabel =DataIDLabel.withTagRef(ndg.getTag(), ndg.getRef());
        final int number = Integer.parseInt(numLabel.getLabel());            
        
        final ScientificData sdErr = produceErrorData(ndgErr, histDim);
                    
        /* Given name list check that that the name is in the list. */
        if (histAttributes == null || containsHistogramAttribute(group.getGroupName(), name, histAttributes)) {
            if (mode.isOpenMode()) {
            	final Object histData = sciData.getData(inHDF, histDim, histNumType, sizeX, sizeY);                	
            	Object histErrData =null;
                if (sdErr != null) {
                	histErrData = sdErr.getData1dD(inHDF, sizeX);
                }
                hist=openHistogram(group, name, title, number, histData, histErrData);
            } else if (mode == FileOpenMode.RELOAD) {
            	final Object histData = sciData.getData(inHDF, histDim, histNumType, sizeX, sizeY);                	
            	hist=reloadHistogram(group, name, histData);
            } else if  (mode == FileOpenMode.ADD) {
            	final Object histData = sciData.getData(inHDF, histDim, histNumType, sizeX, sizeY);                	
            	hist=addHistogram(group, name, histData);
            } else {
            	hist=null;
            }
            	
        }
            return hist;
    }
    private boolean containsHistogramAttribute(String groupName, String histName, List histogramAttributeList){
    	boolean inList=false;
    	
    	Iterator histAttIter =histogramAttributeList.iterator();
    	while(histAttIter.hasNext()) {
    		HistogramAttributes histAttribute =( HistogramAttributes)histAttIter.next();
    		if (groupName!=null) {
    			if ( groupName.equals(histAttribute.getGroupName()) &&
	    	         histName.equals(histAttribute.getName()) ) {
	    			inList =true;
	    			break;
	    		}
    		}else{
    			if ( histName.equals(histAttribute.getName()) ) {
	    			inList =true;
	    			break;
	    		}
    		}
    	}
    	
    	return inList;
    }
    				
    HistogramAttributes convertHistogamAttributes(String groupName, VirtualGroup histGroup,  List histNames, FileOpenMode mode) throws HDFException {
    	
    	Group group;

    	HistogramAttributes histAttributes=null;    	
        NumericalDataGroup ndg=null;
        NumericalDataGroup ndgErr = null; //check ndgErr==null to determine if error bars exist
        
        final DataIDLabel templabel = DataIDLabel.withTagRef(histGroup.getTag(), histGroup.getRef());
        final String name = templabel.getLabel();            
        final DataIDAnnotation tempnote = DataIDAnnotation.withTagRef(histGroup.getTag(), histGroup.getRef());            
        final String title = tempnote.getNote();
        
        /* only the "histograms" VG (only one element) */
        final List tempVec = AbstractData.ofType(histGroup.getObjects(),
                AbstractData.DFTAG_NDG);
        final NumericalDataGroup[] dataGroups = getNumericalGroups(tempVec);
        if (dataGroups.length == 1) {
            ndg = dataGroups[0]; //only one NDG -- the data
        } else if (dataGroups.length == 2) {
            if (DataIDLabel.withTagRef(AbstractData.DFTAG_NDG,
                    dataGroups[0].getRef()).getLabel().equals(ERROR_LABEL)) {
                ndg = dataGroups[1];
                ndgErr = dataGroups[0];
            } else {
                ndg = dataGroups[0];
                ndgErr = dataGroups[1];
            }
        } else {
        	histAttributes=null;
        	//FIXME KBS rem no longer the case
        	//throw new HDFException( "Invalid number of data groups (" + dataGroups.length
            //        + ") in VirtualGroup.");
        }
        final ScientificData sciData = (ScientificData) (AbstractData
                .ofType(ndg.getObjects(), AbstractData.DFTAG_SD).get(0));
        final ScientificDataDimension sdd = (ScientificDataDimension) (AbstractData
                .ofType(ndg.getObjects(), AbstractData.DFTAG_SDD).get(0));
        final byte histNumType = sdd.getType();
        sciData.setNumberType(histNumType);
        final int histDim = sdd.getRank();
        sciData.setRank(histDim);
        final int sizeX = sdd.getSizeX();
        final int sizeY = histDim == 2 ? sdd.getSizeY() : 0;
        
        final DataIDLabel numLabel =DataIDLabel.withTagRef(ndg.getTag(), ndg.getRef());
        final int number = Integer.parseInt(numLabel.getLabel());            
        
        final ScientificData sdErr = produceErrorData(ndgErr, histDim);
                    
        /* Given name list check that that the name is in the list. */
        if (histNames == null || histNames.contains(name)) {            	
            if (mode==FileOpenMode.ATTRIBUTES) {
            	histAttributes=attributesHistogram(groupName, name, title, number);
            } else {
            	histAttributes=null;
            }
            	
        }
        
        return histAttributes;
    }
    
    private ScientificData produceErrorData(NumericalDataGroup ndgErr, int histDim) {
        final boolean exists = ndgErr != null;
        final ScientificData rval = exists ? (ScientificData) (AbstractData.ofType(ndgErr
                .getObjects(), AbstractData.DFTAG_SD).get(0)) : null;
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

	List findGates(VirtualGroup virtualGroupHistogram, Histogram.Type histType) throws HDFException {
		String gateType;
		if (histType.getDimensionality()==Histogram.Type.ONE_D) {
			gateType=GATE_1D_TYPE;
		} else if (histType.getDimensionality()==Histogram.Type.TWO_D) {
			gateType=GATE_2D_TYPE;
		} else {
			throw new HDFException("Unkown Histogram type in finding gates");
		}
		return findSubGroups(virtualGroupHistogram, gateType, null);				
    }

	VDataDescription findCalibration(VirtualGroup virtualGroup){
        VDataDescription vddCalibration=null; 
        VDataDescription vddpts = findVData(virtualGroup, CALIBRATION_TYPE_POINTS);
        if (vddpts!=null) {
        	vddCalibration =vddpts;
        } 
        VDataDescription vddcoef = findVData(virtualGroup, CALIBRATION_TYPE_COEFF);        	
        if (vddcoef!=null) {
        	vddCalibration =vddcoef;
        }
        return vddCalibration;        		
	}
    AbstractCalibrationFunction convertCalibration(Histogram hist, VDataDescription vdd) throws HDFException {
        final AbstractCalibrationFunction calibrationFunction;
        final VData data = (VData) (AbstractData.getObject(
                AbstractData.DFTAG_VS, vdd.getRef()));
        
        final String funcName = vdd.getName();
        final String dataTypeName =vdd.getDataTypeName();
        final int numbPts = vdd.getNumRows();
        
		
        calibrationFunction = makeCalibration(hist, funcName);
		if (calibrationFunction!=null) {
	        final int numPts = vdd.getNumRows();
	
	        if (dataTypeName.equals(CALIBRATION_TYPE_POINTS)) {
	        	
				double [] ptsChannel = new double [numbPts];
				double [] ptsEnergy = new double [numbPts];
				
				for (int i=0;i<numPts; i++) {
					ptsChannel[i] =data.getDouble(i, 0).doubleValue();
					ptsEnergy[i] =data.getDouble(i, 1).doubleValue();
				}
				calibrationFunction.setPoints(ptsChannel, ptsEnergy);
				try {
					calibrationFunction.fit();
				} catch (DataException de) {
					throw new HDFException("Cannot create fit for calibration function "+funcName);
				}
	        } else if ( dataTypeName.equals(CALIBRATION_TYPE_COEFF) ) {
	        	double [] coeff = new double [numbPts];
				for (int i=0;i<numPts; i++) {
					coeff[i] =data.getDouble(i, 0).doubleValue();
				}
				calibrationFunction.setCoeff(coeff);				
	        } else {
	        	throw new HDFException("Unrecognized calibration type");
	        }
		}
		
		((AbstractHist1D)hist).setCalibration(calibrationFunction);
		
    	return calibrationFunction;
    }
	
    private AbstractCalibrationFunction makeCalibration(Histogram hist, String funcName ) throws HDFException {
    	Class calClass;
    	final Map calMap= AbstractCalibrationFunction.getMapFunctions();
    	AbstractCalibrationFunction calibrationFunction=null;    	
		try {
	    	if (calMap.containsKey(funcName)) {
	    		calClass = (Class)calMap.get(funcName);
				calibrationFunction = (AbstractCalibrationFunction)calClass.newInstance();	    	 
	    	}	    		
		} catch (InstantiationException e) {		
			throw new HDFException("Cannot create calibration  "+funcName);			
		} catch (IllegalAccessException e){	
			throw new HDFException("Cannot create calibration  "+funcName);						
		}
		return calibrationFunction;
    }
    /*
     * non-javadoc: Retrieve the gates from the file.
     * 
     * @param mode whether to open or reload @throws HDFException thrown if
     * unrecoverable error occurs
     */
    int convertGatesOriginal(Group currentGroup, FileOpenMode mode) throws HDFException {
        int numGates = 0;
        final StringUtilities util = StringUtilities.instance();
        //Gate gate = null;
        /* get list of all VG's in file */
        final List groups = AbstractData.ofType(AbstractData.DFTAG_VG);
        /* get only the "gates" VG (only one element) */
        final VirtualGroup gates = VirtualGroup.ofName(groups, GATE_SECTION);
        final List annotations = AbstractData.ofType(AbstractData.DFTAG_DIA);
        if (gates != null) {
            numGates = gates.getObjects().size();
            final Iterator temp = gates.getObjects().iterator();
            boolean errorOccured=false;

            gateLoop: while (temp.hasNext()) {
                final VirtualGroup currVG = (VirtualGroup) (temp.next());

                final String hname = DataIDAnnotation.withTagRef(annotations,
                        currVG.getTag(), currVG.getRef()).getNote();
                final String groupName = currentGroup.getName();
                final String histFullName = groupName + "/"
                        + STRING_UTIL.makeLength(hname, Histogram.NAME_LENGTH);
                final Histogram hist = Histogram.getHistogram(histFullName);
                
                convertGate(hist, currVG, mode);

            }
            if (errorOccured){
                throw new IllegalStateException("Problem processing a VH for a gate.");
            }
        }
        return numGates;
    }
    
    Gate convertGate(Histogram hist, VirtualGroup currVG, FileOpenMode mode) throws HDFException {
    	
    	//Get VDD member of Virtual group
        final VDataDescription vdd = (VDataDescription) (AbstractData
                .ofType(currVG.getObjects(), AbstractData.DFTAG_VH)
                .get(0));
        final String gname = currVG.getName();     
        if (vdd == null) {
        	throw new HDFException("No VdataDescription under gate VirtualGroup");
        }
        return convertGate(hist, vdd, gname, mode);
    	
    }
    Gate convertGate(Histogram hist, VDataDescription vdd, String gateName, FileOpenMode mode) throws HDFException {
        final Gate gate;
        final Polygon shape = new Polygon();
        final VData data = (VData) (AbstractData.getObject(
                AbstractData.DFTAG_VS, vdd.getRef()));
        //corresponding VS
        final int numRows = vdd.getNumRows();

        if (mode.isOpenMode()) {
            
            gate = makeGate(hist, gateName);
        } else { //reload
            gate = Gate.getGate(STRING_UTIL
                    .makeLength(gateName, Gate.NAME_LENGTH));
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
    	
    	return gate;
    }
    
    private Gate makeGate(Histogram hist, String name){
        return hist == null ? null : new Gate(name, hist);
    }

	List findScalers(VirtualGroup virtualGroupGroup) throws HDFException {
		return findSubGroupsName(virtualGroupGroup, SCALER_SECT);		
    }

	VDataDescription findScalersOriginal() throws HDFException {
        final VDataDescription vdd = VDataDescription.ofName(AbstractData
                .ofType(AbstractData.DFTAG_VH), SCALER_SECT);
		return vdd;
    }
	
    /*
     * non-javadoc: Retrieve the scalers from the file.
     * 
     * @param mode whether to open, reload or add @throws HDFException if there
     * is a problem retrieving scalers
     */

    int convertScalers(Group group, VirtualGroup currVG, FileOpenMode mode) throws HDFException {
    	 final VDataDescription vdd = (VDataDescription) (AbstractData
                .ofType(currVG.getObjects(), AbstractData.DFTAG_VH)
                .get(0));
    	 return convertScalers(group, vdd, mode); 
    }
    
    /*
     * non-javadoc: Retrieve the scalers from the file.
     * 
     * @param mode whether to open, reload or add @throws HDFException if there
     * is a problem retrieving scalers
     */
    int convertScalers(Group group, VDataDescription vdd, FileOpenMode mode) throws HDFException {
        int numScalers = 0;
      
        /* get the VS corresponding to the given VH */
        final VData data = (VData) (AbstractData.getObject(AbstractData.DFTAG_VS, vdd.getRef()));
        numScalers = vdd.getNumRows();
        for (int i = 0; i < numScalers; i++) {
            final String sname = data.getString(i, 1);
            final int sNumber = data.getInteger(i, 0).intValue();
            final Scaler scaler = produceScaler(group, mode, sNumber, sname);
            if (scaler != null) {
                final int fileValue = data.getInteger(i, 2).intValue();
                if (mode == FileOpenMode.ADD) {
                    scaler.setValue(scaler.getValue() + fileValue);
                } else {
                    scaler.setValue(fileValue);
                }
            }
        }
        return numScalers;
    }
    
    private Scaler produceScaler(Group group, FileOpenMode mode, int number, String name){
        return mode.isOpenMode() ? new Scaler(group, name,
                number) : Scaler
                .getScaler(name);        
    }

	List findParameters(VirtualGroup virtualGroupGroup) throws HDFException {
		return findSubGroupsName(virtualGroupGroup, PARAMETERS);		
    }
	
	VDataDescription findParametersOriginal() throws HDFException {
		final VDataDescription vdd = VDataDescription.ofName(AbstractData
                .ofType(AbstractData.DFTAG_VH), PARAMETERS);	
		return vdd;
    }
	
    /*
     * non-javadoc: Retrieve the scalers from the file.
     * 
     * @param mode whether to open, reload or add @throws HDFException if there
     * is a problem retrieving scalers
     */

    int convertParameters(Group group, VirtualGroup currVG, FileOpenMode mode) throws HDFException {
    	int numParameters = 0;    	
    	List list =AbstractData.ofType(currVG.getObjects(), AbstractData.DFTAG_VH);
    	if (list.size()>0) {
    		final VDataDescription vdd = (VDataDescription) list.get(0);    	 
    		/* only the "parameters" VH (only one element) in the file */
    		if (vdd != null) {    	 
    			numParameters =convertParameters(group, vdd, mode);
    		}
    	}
         return numParameters;
    }
	
    /*
     * non-javadoc: retrieve the parameters from the file
     * 
     * @param mode whether to open or reload @throws HDFException if an error
     * occurs reading the parameters
     */
    int convertParameters(Group group, VDataDescription vdd, FileOpenMode mode) throws HDFException {
        int numParams = 0;
        /* Get corresponding VS for this VH */
        final VData data = (VData) (AbstractData.getObject(
                AbstractData.DFTAG_VS, vdd.getRef()));
        numParams = vdd.getNumRows();
        for (int i = 0; i < numParams; i++) {
            final String pname = data.getString(i, 0);
            /* make if OPEN, retrieve if RELOAD */
            final DataParameter param = produceParameter(mode, pname);
            if (param != null) {
                param.setValue(data.getFloat(i, 1).floatValue());
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

	VDataDescription findVData(VirtualGroup virtualGroupGroup, String dataType){
		VDataDescription vdd=null;
    	final Iterator iter = virtualGroupGroup.getObjects().iterator();
   	 	while (iter.hasNext()) {
   	 		AbstractData hData = (AbstractData)iter.next();
   	 		//Is a virtual data descriptor
   	 		if ( hData.getTag() == AbstractData.DFTAG_VH ) {        		
   	 			//add to list if is a scaler goup
   	 			final VDataDescription currentVDD = (VDataDescription) hData;
   	 			if ( currentVDD.getDataTypeName().equals(dataType) ) {
   	 				vdd=currentVDD;
   	 				break;
   	 			} 
   	 		}
       	}
   	    return vdd;   	 		
   	 }
    
	String readVirtualGroupName(VirtualGroup virtualGroup) {
		final DataIDLabel dataIDLabel = DataIDLabel.withTagRef(virtualGroup.getTag(),
				virtualGroup.getRef());
		return dataIDLabel.getLabel();
	}
	
	private List findSubGroups(VirtualGroup virtualGroupGroup, String groupType, List groupNameList){
    	final List groupSubList = new ArrayList();    	
    	final Iterator iter = virtualGroupGroup.getObjects().iterator();
   	 	while (iter.hasNext()) {
   	 		AbstractData hData = (AbstractData)iter.next();
   	 		//Is a virtual group
   	 		if ( hData.getTag() == AbstractData.DFTAG_VG ) {        		
   	 			//add to list if is a scaler goup
   	 			final VirtualGroup currentVGroup = (VirtualGroup) hData;
   	 			String groupName = readVirtualGroupName(currentVGroup); 
   	 			if ( currentVGroup.getType().equals(groupType) ){
   	 				if (groupNameList!=null && groupNameList.contains(groupName)) {
   	   	 				groupSubList.add(currentVGroup);
   	 				} else if (groupNameList==null) {
   	 					groupSubList.add(currentVGroup);
   	 				}
   	 			} 
   	 		}
       	}
   	    return groupSubList;   	 		
   	 }

	private List findSubGroupsName(VirtualGroup virtualGroupGroup, String groupName){
    	final List groupSubList = new ArrayList();    	
    	final Iterator iter = virtualGroupGroup.getObjects().iterator();
   	 	while (iter.hasNext()) {
   	 		AbstractData hData = (AbstractData)iter.next();
   	 		//Is a virtual group
   	 		if ( hData.getTag() == AbstractData.DFTAG_VG ) {        		
   	 			//add to list if is a scaler goup
   	 			final VirtualGroup currentVGroup = (VirtualGroup) hData;
   	 			if ( currentVGroup.getName().equals(groupName) ) {
   	 				groupSubList.add(currentVGroup);
   	 			} 
   	 		}
       	}
   	    return groupSubList;   	 		
   	 }
	
    Histogram openHistogram(Group group, String name, String title, int number, 
            Object histData, Object histErrorData) throws HDFException {
        final Histogram histogram = Histogram.createHistogram(group, histData, name, title);
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
    private HistogramAttributes attributesHistogram(String groupName, String name, String title, int number) {
    	return new HistogramAttributes(groupName, name, title, number);
    }
}

