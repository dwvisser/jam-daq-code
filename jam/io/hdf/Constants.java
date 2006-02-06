package jam.io.hdf;

/**
 * Contains constants for the <code>jam.io.hdf</code> package.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 * @version 1.0
 */
public interface Constants {

	/**
	 * Byte pattern for bytes 0-3 of any standard v4.1r2 HDF file.
	 */
	int HDF_HEADER = 0x0e031301;

	/**
	 * Number of bytes in hdf header word.
	 */
	int HEADER_BYTES = 4;

	/**
	 * Tag for No Data.
	 * 
	 * @see JavaMachineType
	 */
	short DFTAG_NULL = 1;

	/**
	 * Tag for machine type.
	 * 
	 * @see JavaMachineType
	 */
	short DFTAG_MT = 107;

	/**
	 * HDF tag for Data identifier annotation
	 * 
	 * @see DataIDAnnotation
	 */
	short DFTAG_DIA = 105;

	/**
	 * HDF tag for Data identifier label.
	 * 
	 * @see DataIDLabel
	 */
	short DFTAG_DIL = 104;

	/**
	 * HDF tag for File Identifier.
	 * 
	 * @see FileIdentifier
	 */
	short DFTAG_FID = 100;

	/**
	 * HDF tag for File Description.
	 * 
	 * @see FileDescription
	 */
	short DFTAG_FD = 101;

	/**
	 * HDF tag for number type. (106)
	 * 
	 * @see NumberType
	 */
	short DFTAG_NT = 0x006a;

	/**
	 * HDF tag for Library version number
	 * 
	 * @see LibVersion
	 */
	short DFTAG_VER = 30;

	/**
	 * HDF tag for Numerical Data Group
	 * 
	 * @see NumericalDataGroup
	 */
	short DFTAG_NDG = 720;

	/**
	 * HDF tag for Scientific Data
	 * 
	 * @see ScientificData
	 */
	short DFTAG_SD = 702;

	/**
	 * HDF tag for Scientific data dimension records
	 * 
	 * @see ScientificDataDimension
	 */
	short DFTAG_SDD = 701;

	/**
	 * HDF tag for Scientific data labels
	 * 
	 * @see ScientificDataLabel
	 */
	short DFTAG_SDL = 704;

	/**
	 * HDF tag for Scientific data scales
	 */
	short DFTAG_SDS = 703;

	/**
	 * HDF tag for Vgroup
	 * 
	 * @see VirtualGroup
	 */
	short DFTAG_VG = 1965;

	/**
	 * HDF tag for Vdata description
	 * 
	 * @see VDataDescription
	 */
	short DFTAG_VH = 1962;

	/**
	 * HDF tag for Vdata
	 * 
	 * @see VData
	 */
	short DFTAG_VS = 1963;

}