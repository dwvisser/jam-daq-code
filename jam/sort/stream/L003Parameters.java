package jam.sort.stream;
import java.io.*;
import java.util.*;
/**
 * Parameters for L003  ORNL formatted event stream
 *
 * @version	0.5 April 98
 * @author 	Dale Visser
 * @since       JDK1.1
 */
public interface L003Parameters {

    //stream markers
    final short EVENT_END_MARKER=(short)0xFFFF;    
    final short BUFFER_END_MARKER=(short)0xFFF0;
    final short RUN_END_MARKER=(short)0xFF03;    
    final short EVENT_PARAMETER_MARKER=(short)0x8000;    
    final short EVENT_PARAMETER_MASK  =(short)0x07FF;

    
    //header stuff
    static public final int HEADER_SIZE=256;	
    static public final int TITLE_SIZE=80;    
    public final String HEADER_START="HHIRF   L003    LIST DATA       ";    
    
    //event data word parameter limits		    	    	    
    final short PARAMETER_MIN=(short)1;
    final short PARAMETER_MAX=(short)512;
    
    int EVENT_RECORD_SIZE=8192;
    
    int SCALER_RECORD_SIZE=32000;
    int SCALER_BUFFER_SIZE=256;
    
    //tape stuff
//    static public final int DATA_RECORD_LENGTH=0x10000; //64 kbytes	 //must be an even number
//    static public final int PARAM_ERROR=20;    
//    static public final int IMAGE_RECORD_LENGTH=1600; //20 lines of 8-bit chars
    
    
        
}
    