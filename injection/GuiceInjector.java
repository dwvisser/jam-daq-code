package injection;

import jam.JamInitialization;
import jam.global.AcquisitionStatus;
import jam.global.BroadcastUtilities;
import jam.global.JamStatus;
import jam.plot.PlotDisplay;
import jam.script.Session;
import jam.sort.control.RunControl;
import jam.sort.control.SetupSortOff;
import jam.sort.control.SetupSortOn;
import jam.sort.control.SortControl;

import javax.swing.JFrame;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Injects objects.
 * 
 * @author Dale Visser
 * 
 */
public final class GuiceInjector {
	private static final Injector injector = Guice.createInjector(new Module());

	private GuiceInjector() {
		// static class
	}

	/**
	 * @return a Session object
	 */
	public static Session getSession() {
		return injector.getInstance(Session.class);
	}

	/**
	 * @return the Jam Frame
	 */
	public static JamInitialization getJamInitialization() {
		return injector.getInstance(JamInitialization.class);
	}

	/**
	 * @return the online setup dialog
	 */
	public static SetupSortOn getSetupSortOn() {
		return injector.getInstance(SetupSortOn.class);
	}

	/**
	 * @return application status
	 */
	public static JamStatus getJamStatus() {
		return injector.getInstance(JamStatus.class);
	}

	/**
	 * @return the application frame
	 */
	public static JFrame getFrame() {
		return injector.getInstance(JFrame.class);
	}

	/**
	 * @return for use in broadcasting
	 */
	public static BroadcastUtilities getBroadcastUtilitities() {
		return injector.getInstance(BroadcastUtilities.class);
	}

	/**
	 * @return the acquisition status
	 */
	public static AcquisitionStatus getAcquisitionStatus() {
		return injector.getInstance(AcquisitionStatus.class);
	}

	/**
	 * @return online run control dialog
	 */
	public static RunControl getRunControl() {
		return injector.getInstance(RunControl.class);
	}

	/**
	 * @return offline sort control dialog
	 */
	public static SortControl getSortControl() {
		return injector.getInstance(SortControl.class);
	}

	/**
	 * @return offline setup dialog
	 */
	public static SetupSortOff getSetupSortOff() {
		return injector.getInstance(SetupSortOff.class);
	}

	/**
	 * @return the plot display
	 */
	public static PlotDisplay getPlotDisplay() {
		return injector.getInstance(PlotDisplay.class);
	}
}
