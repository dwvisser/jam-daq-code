package jam.data;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote interface for getting data from a Jam server process.
 *
 * @author Ken Swartz
 * @version 0.9
 * @since JDK 1.1
 */
public interface RemoteData extends Remote {
    /**
     * A test method.
     *
     * @return a dummy string used for testing only
     * @exception RemoteException thrown if there is a problem accessing the remote data
     */
    String getTestString() throws RemoteException;

    /**
     * Returns the server's experiment name.
     *
     * @return then name of the current experiment on the server process
     * @exception RemoteException thrown if there is a problem accessing the remote data
     */
    String getExperimentName() throws RemoteException ;

    /**
     * Returns the list of histograms.
     *
     * @return the list of all histograms
     * @exception RemoteException thrown if there is a problem accessing the remote data
     */
    List<Histogram> getHistogramList() throws RemoteException ;

    /**
     * Returns the list of histogram names.
     *
     * @return the list of all histogram names
     * @exception RemoteException thrown if there is a problem accessing the remote data
     */
    String [] getHistogramNames() throws RemoteException ;

    /**
     * Returns a histogram given its name.
     *
     * @param name the name of the desired histogram
     * @return the histogram with the specified name
     * @exception RemoteException thrown if there is a problem accessing the remote data
     */
    Histogram getHistogram(String name) throws RemoteException;

    /**
     * Returns the list of gates.
     *
     * @return the list of all gates
     * @exception RemoteException thrown if there is a problem accessing the remote data
     */
    List<Gate> getGateList() throws RemoteException;

    /**
     * Gets the list of gate names assiciated with the given histogram name.
     *
     * @param histName name of histogram which the gates are associated with
     * @return list of the gates associated with the specified histogram
     * @exception RemoteException thrown if there is a problem accessing the remote data
     */
    List<String> getGateNames(String histName) throws RemoteException;

    /**
     * Returns a gate given its name.
     *
     * @param nameGate the name of the desired gate
     * @return the gate with the specified name
     * @exception RemoteException thrown if there is a problem accessing the remote data
     */
    Gate getGate(String nameGate) throws RemoteException;

    /**
     * Returns the list of scalers.
     *
     * @return the list of all scalers
     * @exception RemoteException thrown if there is a problem accessing the remote data
     */
    List<Scaler> getScalerList() throws RemoteException ;

    /**
     * Returns the list of monitors.
     *
     * @return the list of all monitors
     * @exception RemoteException thrown if there is a problem accessing the remote data
     */
    List<Monitor> getMonitorList() throws RemoteException ;
    
    /**
     * Returns the values of monitors.
     *
     * @return the values of all monitors
     * @exception RemoteException thrown if there is a problem accessing the remote data
     */
    List<Double> getMonitorValues() throws RemoteException ;
}
