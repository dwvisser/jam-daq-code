package jam.io.hdf;

/**
 * @author Dale Visser
 * 
 */
public interface ProgressUpdater {
	/**
	 * @param message
	 *            message to show
	 * @param progress
	 *            from 0 to 100
	 */
	void updateProgressBar(String message, int progress);
}
