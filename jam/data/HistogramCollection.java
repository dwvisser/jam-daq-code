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
public final class HistogramCollection {

	/** children of group */
	private transient final Map<String, AbstractHistogram> histogramMap = new HashMap<String, AbstractHistogram>();
	/** children histograms of group */
	private transient final List<AbstractHistogram> histList = new ArrayList<AbstractHistogram>();

	/**
	 * Add a histogram to the group
	 * 
	 * @param group
	 *            TODO
	 * @param hist
	 */
	protected void add(final AbstractHistogram hist) {
		this.histList.add(hist);
		this.histogramMap.put(hist.getName(), hist);
	}

	/**
	 * @return list of histograms in this group
	 */
	public List<AbstractHistogram> getList() {
		return Collections.unmodifiableList(this.histList);
	}

	/**
	 * Remove a histogram from the group
	 * 
	 * @param group
	 *            TODO
	 * @param hist
	 */
	public void removeHistogram(final AbstractHistogram hist) {
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
	public AbstractHistogram getHistogram(final String name) {
		return this.histogramMap.get(name);
	}

	protected Set<String> getNameSet() {
		return this.histogramMap.keySet();
	}
}
