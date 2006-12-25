package jam.io.hdf;

/**
 * Contains constants for the <code>jam.io.hdf</code> package.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 * @version 1.0
 */
public final class Constants {

	private Constants() {
		super();
	}

	/**
	 * Byte pattern for bytes 0-3 of any standard v4.1r2 HDF file.
	 */
	public static final int HDF_HEADER = 0x0e031301;

	/**
	 * Number of bytes in hdf header word.
	 */
	public static final int HEADER_BYTES = 4;

	/**
	 * Tag for No Data.
	 * 
	 * @see JavaMachineType
	 */
	public static final short DFTAG_NULL = 1;

	/**
	 * Tag for machine type.
	 * 
	 * @see JavaMachineType
	 */
	public static final short DFTAG_MT = 107;

	/**
	 * HDF tag for Data identifier annotation
	 * 
	 * @see DataIDAnnotation
	 */
	public static final short DFTAG_DIA = 105;

	/**
	 * HDF tag for Data identifier label.
	 * 
	 * @see DataIDLabel
	 */
	public static final short DFTAG_DIL = 104;

	/**
	 * HDF tag for File Identifier.
	 * 
	 * @see FileIdentifier
	 */
	public static final short DFTAG_FID = 100;

	/**
	 * HDF tag for File Description.
	 * 
	 * @see FileDescription
	 */
	public static final short DFTAG_FD = 101;

	/**
	 * HDF tag for number type. (106)
	 * 
	 * @see NumberType
	 */
	public static final short DFTAG_NT = 0x006a;

	/**
	 * HDF tag for Library version number
	 * 
	 * @see LibVersion
	 */
	public static final short DFTAG_VER = 30;

	/**
	 * HDF tag for Numerical Data Group
	 * 
	 * @see NumericalDataGroup
	 */
	public static final short DFTAG_NDG = 720;

	/**
	 * HDF tag for Scientific Data
	 * 
	 * @see ScientificData
	 */
	public static final short DFTAG_SD = 702;

	/**
	 * HDF tag for Scientific data dimension records
	 * 
	 * @see ScientificDataDimension
	 */
	public static final short DFTAG_SDD = 701;

	/**
	 * HDF tag for Scientific data labels
	 * 
	 * @see ScientificDataLabel
	 */
	public static final short DFTAG_SDL = 704;

	/**
	 * HDF tag for Scientific data scales
	 */
	public static final short DFTAG_SDS = 703;

	/**
	 * HDF tag for Vgroup
	 * 
	 * @see VirtualGroup
	 */
	public static final short DFTAG_VG = 1965;

	/**
	 * HDF tag for Vdata description
	 * 
	 * @see VDataDescription
	 */
	public static final short DFTAG_VH = 1962;

	/**
	 * HDF tag for Vdata
	 * 
	 * @see VData
	 */
	public static final short DFTAG_VS = 1963;

}