/*
 */
package jam.global;
/**
 * Defines an interface for a class which
 * has the status methods so that JamStatus 
 * can make them available to all.
 *
 * @author Ken Swartz
 */

public interface AcquisitionStatus {
    /**
     * Are we Online
     */
    public boolean isOnLine();
    /**
     * Are we currently taking data
     */
    public boolean isAcqOn();
    
}