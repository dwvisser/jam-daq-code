/*
 */
package jam;

import jam.data.Gate;
import jam.data.Histogram;
import jam.data.Monitor;
import jam.data.RemoteData;
import jam.data.Scaler;
import jam.global.RunInfo;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows remote access to histograms
 */
public class RemoteAccess extends UnicastRemoteObject implements RemoteData {

	/**
	 * Contructor.
	 * 
	 * @throws RemoteException
	 *             if there's a problem
	 */
	public RemoteAccess() throws RemoteException {
		super();
	}

	/**
	 * @return a test string
	 * @throws RemoteException
	 *             if there's a problem
	 */
	public String getTestString() throws RemoteException {
		return "Remote from jam";
	}

	/**
	 * @return the experiment name
	 * @throws RemoteException
	 *             if there's a problem
	 */
	public String getExperimentName() throws RemoteException {
		return RunInfo.getInstance().experimentName;
	}

	/**
	 * @return the list of histograms
	 * @throws RemoteException
	 *             if there's a problem
	 */
	public List<Histogram> getHistogramList() throws RemoteException {
		return Histogram.getHistogramList();
	}

	/**
	 * @return the list of histograms names
	 * @throws RemoteException
	 *             if there's a problem
	 */
	public String[] getHistogramNames() throws RemoteException {
		final List hists = Histogram.getHistogramList();
		final String[] names = new String[hists.size()];
		for (int i = 0; i < hists.size(); i++) {
			names[i] = ((Histogram) hists.get(i)).getFullName();
		}
		return names;
	}

	/**
	 * @param name
	 *            the name of a histogram
	 * @return the histogram
	 * @throws RemoteException
	 *             if there's a problem
	 */
	public Histogram getHistogram(final String name) throws RemoteException {
		return Histogram.getHistogram(name);
	}

	/**
	 * @return the list of all gates
	 * @throws RemoteException
	 *             if there's a problem
	 */
	public List<Gate> getGateList() throws RemoteException {
		return Gate.getGateList();
	}

	/**
	 * @param histName
	 *            name of a histogram
	 * @return the list of names of gates in the histogram
	 * @throws RemoteException
	 *             if there's a problem
	 */
	public List<String> getGateNames(final String histName)
			throws RemoteException {
		final Histogram hist = Histogram.getHistogram(histName);
		final List<Gate> gates = hist.getGates();
		final List<String> names = new ArrayList<String>();
		for (Gate gate : gates) {
			names.add(gate.getName());
		}
		return Collections.unmodifiableList(names);
	}

	/**
	 * @param name
	 *            of a gate
	 * @return the gate
	 * @throws RemoteException
	 *             if there's a problem
	 */
	public Gate getGate(final String name) throws RemoteException {
		return Gate.getGate(name);
	}

	/**
	 * @return the list of scalers
	 * @throws RemoteException
	 *             if there's a problem
	 */
	public List<Scaler> getScalerList() throws RemoteException {
		return Scaler.getScalerList();
	}

	/**
	 * @return the list of monitors
	 * @throws RemoteException
	 *             if there's a problem
	 */
	public List<Monitor> getMonitorList() throws RemoteException {
		return Monitor.getMonitorList();
	}

	/**
	 * @return the values of monitors
	 * @throws RemoteException
	 *             if there's a problem
	 */
	public List<Double> getMonitorValues() throws RemoteException {
		final List<Monitor> monitors = Monitor.getMonitorList();
		final List<Double> values = new ArrayList<Double>();
		for (Monitor monitor : monitors) {
			values.add(monitor.getValue());
		}
		return Collections.unmodifiableList(values);
	}

	private static final Logger LOGGER = Logger.getLogger(RemoteAccess.class
			.getPackage().getName());

	/**
	 * A test routine for this class.
	 * 
	 * @param args
	 *            ignored
	 */
	public static void main(final String args[]) {
		LOGGER.fine("Test starting up Server");
		try {
			final String name = "jam";
			final RemoteAccess remote = new RemoteAccess();
			Naming.rebind(name, remote);
			LOGGER.fine("Server setup");
		} catch (RemoteException re) {
			LOGGER.log(Level.SEVERE, "Error constructing Server", re);
		} catch (java.net.MalformedURLException mue) {
			LOGGER.log(Level.SEVERE, "Error malformed URL", mue);
		}
	}
}
