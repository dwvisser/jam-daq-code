package jam.io.hdf;

/**
 * Contains constants for the <code>jam.io.hdf</code> package.
 * 
 * @author <a href=mailto:dale@visser.name>Dale Visser</a>
 * @version 1.0
 */
public interface HDFconstants{

    /**
     * Byte pattern for bytes 0-3 of any standard v4.1r2 HDF file.
     */
    int HDF_HEADER = 0x0e031301; 
    int HDF_HEADER_NUMBYTES=4;
}