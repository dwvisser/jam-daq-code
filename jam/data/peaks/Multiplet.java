/*
 * Multiplet.java
 *
 * Created on February 14, 2001, 1:30 PM
 */

package jam.data.peaks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Represents a group of peaks in a spectrum.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
final class Multiplet extends ArrayList<Peak> {

	private Multiplet() {
		super();
	}

	protected static Multiplet createMultiplet() {
		return new Multiplet();
	}

	protected List<Double> getAllCentroids() {
		final List<Double> centroids = new ArrayList<Double>();
		for (Peak peak : this) {
			centroids.add(peak.getPosition());
		}
		return centroids;
	}

	protected static Multiplet combineMultiplets(final Multiplet... mult) {
		return combineMultiplets(Arrays.asList(mult));
	}

	protected static Multiplet combineMultiplets(
			final Collection<Multiplet> collection) {
		final Multiplet rval = new Multiplet();
		for (Multiplet multiplet : collection) {
			rval.addAll(multiplet);
		}
		return rval;
	}
}