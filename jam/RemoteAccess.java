/*
 */
package jam;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import jam.global.*;
import jam.data.*;
/** 
 * Allows remote access of histograms
 *
 *
 */
public class RemoteAccess extends UnicastRemoteObject implements RemoteData {

	/**
	 * Contructor
	 */
	public RemoteAccess() throws RemoteException {
		super();
	}
	
	/**
	 * A test string 
	 */
	public String getTestString() throws RemoteException {
		String message = "Remote from jam";
		return message;
	}
	/**
	 * Experiment name
	 */
	public String getExperimentName() throws RemoteException {
		return RunInfo.experimentName;
	}

	/**
	 *Get the list of histograms
	 */
	public List getHistogramList() throws RemoteException {
		return Histogram.getHistogramList();
	}
	
	/**
	 *Get the list of histograms names
	 */
	public String[] getHistogramNames() throws RemoteException {

		Vector hists = Histogram.getHistogramList();
		String[] names = new String[hists.size()];

		for (int i = 0; i < hists.size(); i++) {
			names[i] = ((Histogram) hists.elementAt(i)).getName();
		}
		return names;
	}
	
	/**
	 * Get a histogram given its name
	 */
	public Histogram getHistogram(String name) throws RemoteException {

		return Histogram.getHistogram(name);

	}
	
	/**
	 *Get the list of gates
	 */
	public List getGateList() throws RemoteException {
		return Gate.getGateList();
	}
	
	/**
	 *Get the list of gate names given a histogram
	 */
	public String[] getGateNames(String histogramName) throws RemoteException {

		Histogram hist = Histogram.getHistogram(histogramName);
		Gate[] gates = hist.getGates();
		String[] names = new String[gates.length];

		for (int i = 0; i < gates.length; i++) {
			names[i] = gates[i].getName();
		}
		return names;
	}
	/**
	 * Get a gate given its name
	 */
	public Gate getGate(String name) throws RemoteException {
		return Gate.getGate(name);
	}

	/**
	 *Get the list of scalers
	 */
	public List getScalerList() throws RemoteException {
		return Scaler.getScalerList();
	}
	
	/**
	 *Get the list of monitors
	 */
	public List getMonitorList() throws RemoteException {
		return Monitor.getMonitorList();
	}

	/**
	 *Get the values of monitors
	 */
	public double[] getMonitorValues() throws RemoteException {
		Vector monitors = Monitor.getMonitorList();
		double values[] = new double[monitors.size()];
		for (int i = 0; i < monitors.size(); i++) {
			values[i] = ((Monitor) monitors.elementAt(i)).getValue();
		}
		return values;
	}

	/**
	 * A test routine for this class
	 */
	public static void main(String args[]) {
		String name;
		
		System.out.println("Test starting up Server");
		try {
			name = "jam";
			RemoteAccess ra = new RemoteAccess();
			Naming.rebind(name, ra);
			System.out.println("Server setup");
		} catch (RemoteException re) {
			System.out.println("Error constructing Server");
		} catch (java.net.MalformedURLException mue) {
			System.out.println("Error malformed URL");
		}
	}
}
