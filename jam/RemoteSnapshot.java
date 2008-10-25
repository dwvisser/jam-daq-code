package jam;

import jam.data.Gate;
import jam.data.Histogram;
import jam.data.RemoteData;
import jam.global.JamException;
import jam.global.JamStatus;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

final class RemoteSnapshot {
	private static final Logger LOGGER = Logger.getLogger(RemoteSnapshot.class
			.getPackage().getName());

	private static final JamStatus STATUS = JamStatus.getSingletonInstance();

	/**
	 * Get a snap shot of data.
	 * 
	 * @param url
	 *            location of service
	 * @exception JamException
	 *                all exceptions given to <code>JamException</code> go to
	 *                the console
	 */
	public void takeSnapshot(final String url) throws JamException {
		final RemoteData remoteData;
		try {
			if (STATUS.canSetup()) {
				remoteData = (RemoteData) Naming.lookup(url);
				LOGGER.info("Remote lookup OK!");
			} else {
				throw new JamException(
						"Can't view remotely, sort mode locked [SetupRemote]");
			}
		} catch (RemoteException re) {
			throw new JamException("Remote lookup up failed URL: " + url, re);
		} catch (java.net.MalformedURLException mue) {
			throw new JamException("Remote look up malformed URL: " + url, mue);
		} catch (NotBoundException nbe) {
			throw new JamException("Remote look up could not find name " + url,
					nbe);
		}
		try {
			Histogram.setHistogramList(remoteData.getHistogramList());
			Gate.setGateList(remoteData.getGateList());
		} catch (RemoteException re) {
			throw new JamException(
					"Remote getting histogram list [SetupRemote]", re);
		}
	}
}
