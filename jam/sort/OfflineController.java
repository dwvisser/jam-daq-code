package jam.sort;

public interface OfflineController extends Controller {

	/**
	 * Called by <code>SortDaemon</code> when it needs to start the next
	 * stream.
	 * 
	 * @return <code>true</code> if there was a next file and it's open now
	 */
	boolean openNextFile();
}
