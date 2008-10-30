package jam.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A collection of histograms, used by Group.
 * 
 * @author Dale Visser
 * 
 */
public final class HistogramCollection implements
		NameValueCollection<AbstractHistogram> {

	/** children of group */
	private transient final Map<String, AbstractHistogram> histogramMap = new HashMap<String, AbstractHistogram>();
	/** children histograms of group */
	private transient final List<AbstractHistogram> histList = new ArrayList<AbstractHistogram>();

	/**
	 * @return list of histograms in this group
	 */
	public List<AbstractHistogram> getList() {
		return Collections.unmodifiableList(this.histList);
	}

	/**
	 * Remove a histogram from the group
	 * 
	 * @param hist
	 */
	public void remove(final AbstractHistogram hist) {
		this.histList.remove(hist);
		this.histogramMap.remove(hist.getName());
	}

	/**
	 * Retrieve a histogram given its name
	 * 
	 * @param name
	 *            the histogram name
	 * 
	 * @return the histogram
	 */
	public AbstractHistogram get(final String name) {
		return this.histogramMap.get(name);
	}

	public Set<String> getNameSet() {
		return this.histogramMap.keySet();
	}

	public void add(final AbstractHistogram nameable, final String uniqueName) {
		this.histList.add(nameable);
		this.histogramMap.put(uniqueName, nameable);
	}

	public void clear() {
		throw new UnsupportedOperationException(
				"HistogramCollection does not support clear().");
	}

	public void remap(final AbstractHistogram nameable, final String oldName,
			final String newName) {
		throw new UnsupportedOperationException(
				"HistogramCollection does not support remap().");
	}
}
