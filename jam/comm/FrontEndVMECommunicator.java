package jam.comm;

import jam.global.JamProperties;
import jam.global.PropertyKeys;
import jam.sort.CamacCommands;
import jam.sort.VME_Channel;
import jam.sort.VME_Map;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import com.google.inject.Inject;

/**
 * Communicates with VME front end.
 * 
 * @author Dale Visser
 * 
 */
public final class FrontEndVMECommunicator implements FrontEndCommunication,
		PreferenceChangeListener {

	private static final Object LOCK = new Object();

	private transient final VMECommunication vme;

	@Inject
	FrontEndVMECommunicator(final VMECommunication vme) {
		this.vme = vme;
	}

	public void close() {
		this.vme.close();
	}

	/**
	 * New version uploads CAMAC CNAF commands with udp pakets, and sets up the
	 * camac crate.
	 * 
	 * @param camacCommands
	 *            object containing CAMAC CNAF commands
	 * @throws IOException
	 *             if there's a problem
	 */
	public void setupCamac(final CamacCommands camacCommands)
			throws IOException {
		final String RUN_INIT = "list init";
		final String CNAF_INIT = "cnaf init";
		final String CNAF_EVENT = "cnaf event";
		final String CNAF_SCALER = "cnaf scaler";
		final String CNAF_CLEAR = "cnaf clear";
		// load CNAF's
		this.vme.sendCNAFList(CNAF_INIT, camacCommands.getInitCommands());
		this.vme.sendCNAFList(CNAF_EVENT, camacCommands.getEventCommands());
		this.vme.sendCNAFList(CNAF_SCALER, camacCommands.getScalerCommands());
		this.vme.sendCNAFList(CNAF_CLEAR, camacCommands.getClearCommands());
		this.vme.sendMessage(RUN_INIT); // initialize camac
		this.vme.log("Loaded CAMAC command lists, and initialized VME.");
	}

	/**
	 * Tells the VME whether to print out debugging statements.
	 * 
	 * @param state
	 *            true if we want debug messages from the VME
	 */
	public void debug(final boolean state) {
		final String DEBUG_ON = "debug on";
		final String DEBUG_OFF = "debug off";
		if (state) {
			this.vme.sendMessage(DEBUG_ON);
		} else {
			this.vme.sendMessage(DEBUG_OFF);
		}
	}

	/**
	 * Tells the front end to stop acquisition and end the run, which flushes
	 * out the data buffer with an appended end-run marker.
	 */
	public void end() {
		final String END = "END";
		this.vme.sendMessage(END);
	}

	/**
	 * Tells the front end to flush out the data buffer, and send the contents.
	 */
	public void flush() {
		final String FLUSH = "FLUSH";
		this.vme.sendMessage(FLUSH);
	}

	/**
	 * Tells the VME whether to send verbose verbose status messages, which Jam
	 * automatically prints on the console.
	 * 
	 * @param state
	 *            true if user wants VME to be verbose
	 */
	public void verbose(final boolean state) {
		final String VERBOSE_ON = "verbose on";
		final String VERBOSE_OFF = "verbose off";
		if (state) {
			this.vme.sendMessage(VERBOSE_ON);
		} else {
			this.vme.sendMessage(VERBOSE_OFF);
		}
	}

	/**
	 * Sets up networking. Two UDP sockets are created: one for receiving and
	 * one for sending. A daemon is also set up for receiving.
	 * 
	 * @throws CommunicationsException
	 *             if something goes wrong
	 */
	public void setupAcquisition() throws CommunicationsException {
		synchronized (LOCK) {
			final String LOCAL_IP = JamProperties
					.getPropString(PropertyKeys.HOST_IP);
			final int portSend = JamProperties
					.getPropInt(PropertyKeys.HOST_PORT_SEND);
			final int portRecv = JamProperties
					.getPropInt(PropertyKeys.HOST_PORT_RECV);
			final String VME_IP = JamProperties
					.getPropString(PropertyKeys.TARGET_IP);
			this.vme.bindSocketsAndSetActive(LOCAL_IP, portSend, portRecv,
					VME_IP);
			if (this.vme.isActive()) {
				final Preferences prefs = CommunicationPreferences.PREFS;
				debug(prefs.getBoolean(CommunicationPreferences.DEBUG, false));
				verbose(prefs.getBoolean(CommunicationPreferences.VERBOSE,
						false));
			}
		}
	}

	/**
	 * Send the message specifying how to use ADC's and TDC's.
	 * 
	 * @param vmeMap
	 *            the map of channels to use and TDC ranges
	 * @throws IllegalStateException
	 *             if there are no parameters in the map
	 */
	public void setupVMEmap(final VME_Map vmeMap) {
		final StringBuffer temp = new StringBuffer();
		final List<VME_Channel> eventParams = vmeMap.getEventParameters();
		final Map<Integer, Byte> hRanges = vmeMap.getV775Ranges();
		if (eventParams.isEmpty()) {
			throw new IllegalStateException("No event parameters in map.");
		}
		final int totalParams = eventParams.size();
		final char endl = '\n';
		final char space = ' ';
		final String hex = "0x";
		temp.append(totalParams).append(endl);
		for (VME_Channel channel : eventParams) {
			temp.append(channel.getSlot()).append(space).append(hex).append(
					Integer.toHexString(channel.getBaseAddress()))
					.append(space).append(channel.getChannel()).append(space)
					.append(channel.getThreshold()).append(endl);
		}
		final int numRanges = hRanges.size();
		temp.append(numRanges).append(endl);
		if (numRanges > 0) {
			for (Map.Entry<Integer, Byte> entry : hRanges.entrySet()) {
				final int base = entry.getKey();
				temp.append(hex).append(Integer.toHexString(base))
						.append(space).append(entry.getValue()).append(endl);
			}
		}
		temp.append('\0');
		this.vme.sendToVME(PacketTypes.VME_ADDRESS, temp.toString());
	}

	/**
	 * @param pce
	 *            change event
	 * @see java.util.prefs.PreferenceChangeListener#preferenceChange(java.util.prefs.PreferenceChangeEvent)
	 */
	public void preferenceChange(final PreferenceChangeEvent pce) {
		final String key = pce.getKey();
		final String newValue = pce.getNewValue();
		final boolean state = Boolean.parseBoolean(newValue);
		if (key.equals(CommunicationPreferences.DEBUG)) {
			debug(state);
		} else if (key.equals(CommunicationPreferences.VERBOSE)) {
			verbose(state);
		}
	}

	/**
	 * Tells the front end to start acquisition.
	 */
	public void startAcquisition() {
		final String START = "START";
		this.vme.sendMessage(START);
	}

	/**
	 * Tells the front end to stop acquisition, which also flushes out a buffer.
	 */
	public void stopAcquisition() {
		final String STOPACQ = "STOP";
		this.vme.sendMessage(STOPACQ);
	}

}
