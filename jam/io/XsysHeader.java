/*
 */
package jam.io;
/**
 * This interface defines the constants need to import
 * a XSYS data file. 
 * The following table were generated from a fortran routine that was part of XSYS.
 * Each histogram has a header which includes a x_dir array.
 * A long word is 4 bytes long.
 *<H2> XSYS (TUNL) header: </H2>
 *<TABLE >
 *<TR><TH>jam name	</TH><TH>longwords	</TH> <TH>contents</TH></TR>
 *<TR><TD>P_HEADER	</TD><TD>	1	</TD><TD>SPEC	</TD></TR>
 *<TR><TD>P_RUN_NUMBER	</TD><TD>	2	</TD><TD>i*4 run number				</TD></TR>
 *<TR><TD>P_HEADER	</TD><TD>	3-22	</TD><TD>20 word x_dir/info array (see below)	</TD></TR>
 *<TR><TD>P_TITLE	</TD><TD>	23-42	</TD><TD>80-character run title			</TD></TR>
 *<TR><TD>P_SCALERS	</TD><TD>	43-66	</TD><TD>24 scalers in i*4			</TD></TR>
 *<TR><TD>P_SCALER_TITLES</TD><TD>	67-114	</TD><TD>24 8-char titles for scalers		</TD></TR>
 *<TR><TD>P_		</TD><TD>	115-122	</TD><TD>8 deadtime correction values (r*4)	</TD></TR>
 *<TR><TD>P_		</TD><TD>	123	</TD><TD>preset i*4				</TD></TR>
 *<TR><TD>P_		</TD><TD>	124-128	</TD><TD>spare					</TD></TR>
 *</table>
 *<BR>
 *<H2> x_dir/info array </H2>
 *<TABLE>
 *<TR><TH>jam name</TH><TH>longwords</TH> <TH>contents</TH></TR>
 *<TR><TD>P_AREA_NUMBER	    </TD> <TD>	1	</TD><TD>x_area	IAREA	original data area number</TD></TR>
 *<TR><TD>P_AREA_TYPE	    </TD><TD>	2	</TD><TD>x_type	ITYPE	data type (1=i*4 1d)</TD></TR>
 *<TR><TD>P_AREA_LENGTH_WORD</TD><TD>	3	</TD><TD>x_lwlen	LWLEN	length in longwords</TD></TR>
 *<TR><TD>P_AREA_LENGTH_PAGE</TD><TD>	4	</TD><TD>x_plen	NPG	length in pages</TD></TR>
 *<TR><TD>P_AREA	    </TD><TD>	5	</TD><TD>x_relp	IRELP	original location in xdata array of data area</TD></TR>
 *<TR><TD>P_AREA_NAME	    </TD><TD>	6,7	</TD><TD>x_anam	ANAM	8-character spectrum name</TD></TR>
 *<TR><TD>P_AREA_SIZE_X	    </TD><TD>	8	</TD><TD>x_xsiz	IXSIZ	upper bound of x-axis</TD></TR>
 *<TR><TD>P_AREA_SIZE_Y	    </TD><TD>	9	</TD><TD>x_ysiz	IYSIZ	2d: upper bound of y-axis</TD></TR>
 *<TR><TD>P_AREA	    </TD><TD>	10,11	</TD><TD>x_low,x_hi IGLOW,IGHI	gates associated with this spect</TD></TR>
 *<TR><TD>P_AREA_CAL_FLAG   </TD><TD>	12	</TD><TD>x_flp	IFLP	dirsp/energy/tofcon flag</TD></TR>
 *<TR><TD>P_AREA	    </TD><TD>	13	</TD><TD>x_no2dg	N2DG	(unused)</TD></TR>
 *<TR><TD>P_AREA	    </TD><TD>	14	</TD><TD>x_mbdcno MBDCNO	mbd channel which took this</TD></TR>
 *<TR><TD>P_AREA	    </TD><TD>	15	</TD><TD>x_bkinx	IBACK	point to x_back for background data</TD></TR>
 *<TR><TD>P_AREA_CAL_COEF   </TD><TD>	16-18	</TD><TD>x_cal1-3 CAL1-3	channel energy coeff's</TD></TR>
 *<TR><TD>P_AREA	    </TD><TD>	19	</TD><TD>x_displim DISPLIM	disp's x-axis limits</TD></TR>
 *<TR><TD>P_AREA	    </TD><TD>	20	</TD><TD>(unused)</TD></TR>
 * </TABLE>
 *<BR>
 * <H2>Type of data areas</H2>
 * <DL>	
 *<DT> XSYS1DI4=1;		    <DD> xsys 1d I*4 data area 
 *<DT> XSYS2DI4=2;		    <DD> xsys 2d I*4 data area
 *<DT> XSYS2DI2=3;		    <DD> xsys 2d I*2 data area
 *<DT> XSYS1DR4=4;		    <DD> xsys 1d R*4 data area
 *<DT> XSYS1DR8=5;		    <DD> xsys 1d R*8 data area		
 *<DT> XSYSEVAL=6;		    <DD> xsys eval routine
 *<DT> XSYS2DGT=7;		    <DD> xsys 2d gate
 *<DT> XSYSR4PT=8;		   <DD>  xsys R*4 protected from clearing
 *</DL>	
 *
 * @author Ken Swartz 
 * @version 0.5
 * @since jdk1.1
 *
 */
interface XsysHeader {

	/** key word indicating it is a xsys spectrum file */
	String XSYSHEADER="SPEC";

	/** number of bytes in an integer */
	int L_INT=4;
	
	/** buffer size in ints */
	int XSYS_BUFFER_SIZE=128;
	
	/** buffer size bytes */
	int L_BUFFER=XSYS_BUFFER_SIZE*L_INT;
	
	/** flag used in xsys file to indicate cabration coefficients follow */
	int CALIB_ENERGY=999;

	/* pointers to data are ints so we convert them to bytes */
	
	/** position of marker */
	int P_HEADER=0*L_INT;
	
	/** position of run number */
	int P_RUN_NUMBER=1*L_INT;
	
	/** position of title */
	int P_TITLE=22*L_INT;
	
	/** length of title */
	int L_TITLE=80;
	
	/** number of scalers */
	int NUMBER_SCALERS=24;			
	
	/** position of scalers */
	int P_SCALERS=42*L_INT;		    //
	
	/** position of scalers field */
	int P_SCALER_TITLES=66*L_INT;	    //
	
	/** length of scaler titles */
	int L_SCALER_TITLES=8;		    //
		
	
	/** position of data area number */
	int P_AREA_NUMBER=2*L_INT;		    //	
	
	/** positon of data type */
	int P_AREA_DATA_TYPE=3*L_INT;	    //
	
	/** positon of length in words*/
	int P_AREA_LENGTH_WORD=4*L_INT;	    //
	
	/** ??? */
	int P_AREA_LENGTH_PAGE=5*L_INT;
	
	/** position of Area Name */
	int P_AREA_NAME=7*L_INT;
	
	/** Length area Name */
	int L_AREA_NAME=8;
	
	/** area X size */
	int P_AREA_SIZE_X=9*L_INT;
	
	/** area Y size */
	int P_AREA_SIZE_Y=10*L_INT;
		
	/** position calibration flag */
	int P_AREA_CALIB_FLAG=13*L_INT;
	
	/** position of mbd channel number */
	int P_AREA_MBD_CHAN=15*L_INT;	
	
	/** position calibration coeff. */
	int P_AREA_CALIB_COEF=17*L_INT;
	
	/** length calibration coeff. */
	int L_AREA_CALIB_COEF=3;	
	
	/* types of data areas */
	
	/** 1d I*4 data area */
	int XSYS1DI4=1;
	
	/** 2d I*4 data area */
	int XSYS2DI4=2;
	
	/** 2d I*2 data area */
	int XSYS2DI2=3;
	
	/** 1d R*4 data area */
	int XSYS1DR4=4;
	
	/** 1d R*8 data area */
	int XSYS1DR8=5;		
	
	/** eval routine */
	int XSYSEVAL=6;
	
	/** 2d gate */
	int XSYS2DGT=7;
	
	/** R*4 protected from clearing */
	int XSYSR4PT=8;
}
