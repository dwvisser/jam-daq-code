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
import java.util.List;

/** 
 * Allows remote access to histograms
 */
public class RemoteAccess extends UnicastRemoteObject implements RemoteData {

	/**
	 * Contructor.
	 * 
	 * @throws RemoteException if there's a problem
	 */
	public RemoteAccess() throws RemoteException {
		super();
	}
	
	/**
	 * @return a test string 
	 * @throws RemoteException if there's a problem
	 */
	public String getTestString() throws RemoteException {
		String message = "Remote from jam";
		return message;
	}
	
	/**
	 * @return the experiment name
	 * @throws RemoteException if there's a problem
	 */
	public String getExperimentName() throws RemoteException {
		return RunInfo.experimentName;
	}

	/**
	 * @return the list of histograms
	 * @throws RemoteException if there's a problem
	 */
	public List getHistogramList() throws RemoteException {
		return Histogram.getHistogramList();
	}
	
	/**
	 * @return the list of histograms names
	 * @throws RemoteException if there's a problem
	 */
	public String[] getHistogramNames() throws RemoteException {
		List hists = Histogram.getHistogramList();
		String[] names = new String[hists.size()];
		for (int i = 0; i < hists.size(); i++) {
			names[i] = ((Histogram) hists.get(i)).getFullName();
		}
		return names;
	}
	
	/**
	 * @param name the name of a histogram
	 * @return the histogram
	 * @throws RemoteException if there's a problem
	 */
	public Histogram getHistogram(String name) throws RemoteException {
		return Histogram.getHistogram(name);
	}
	
	/**
	 * @return the list of all gates
	 * @throws RemoteException if there's a problem
	 */
	public List getGateList() throws RemoteException {
		return Gate.getGateList();
	}
	
	/**
	 * @param histName name of a histogram
	 * @return the list of names of gates in the histogram
	 * @throws RemoteException if there's a problem
	 */
	public String[] getGateNames(String histName) throws RemoteException {
		Histogram hist = Histogram.getHistogram(histName);
		Gate[] gates = (Gate [])hist.getGates().toArray(new Gate[0]);
		String[] names = new String[gates.length];
		for (int i = 0; i < gates.length; i++) {
			names[i] = gates[i].getName();
		}
		return names;
	}
	
	/**
	 * @param name of a gate
	 * @return the gate
	 * @throws RemoteException if there's a problem
	 */
	public Gate getGate(String name) throws RemoteException {
		return Gate.getGate(name);
	}

	/**
	 * @return the list of scalers
	 * @throws RemoteException if there's a problem
	 */
	public List getScalerList() throws RemoteException {
		return Scaler.getScalerList();
	}
	
	/**
	 * @return the list of monitors
	 * @throws RemoteException if there's a problem
	 */
	public List getMonitorList() throws RemoteException {
		return Monitor.getMonitorList();
	}

	/**
	 * @return the values of monitors
	 * @throws RemoteException if there's a problem
	 */
	public double[] getMonitorValues() throws RemoteException {
		List monitors = Monitor.getMonitorList();
		double values[] = new double[monitors.size()];
		for (int i = 0; i < monitors.size(); i++) {
			values[i] = ((Monitor) monitors.get(i)).getValue();
		}
		return values;
	}

	/**
	 * A test routine for this class.
	 * @param args ignored
	 */
	public static void main(String args[]) {
		System.out.println("Test starting up Server");
		try {
			final String name = "jam";
			final RemoteAccess remote = new RemoteAccess();
			Naming.rebind(name, remote);
			System.out.println("Server setup");
		} catch (RemoteException re) {
			System.out.println("Error constructing Server");
			re.printStackTrace();
		} catch (java.net.MalformedURLException mue) {
			System.out.println("Error malformed URL");
			mue.printStackTrace();
		}
	}
}
