package test.sort;

import static org.junit.Assert.assertFalse;
import jam.data.HistInt1D;
import jam.data.Histogram;

import java.util.List;

final class Utility {

	static HistInt1D getOneDHistogramFromSortGroup(final String name) {
		final List<Histogram> oneDimHistograms = Histogram.getHistogramList(1);
		assertFalse("Expected 1D histograms.", oneDimHistograms.isEmpty());
		HistInt1D result = null;
		for (Histogram histogram : oneDimHistograms) {
			if (histogram.getName().contains(name)) {
				result = (HistInt1D) histogram;
				break;
			}
		}
		return result;
	}

}
