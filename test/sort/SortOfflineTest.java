package test.sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import jam.Script;
import jam.data.DimensionalData;
import jam.data.Gate;
import jam.data.HistInt1D;
import jam.data.HistInt2D;
import jam.data.Histogram;
import jam.sort.stream.YaleCAEN_InputStream;
import jam.sort.stream.YaleInputStream;
import jam.sort.stream.YaleOutputStream;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Test;

public class SortOfflineTest {// NOPMD

	private static final Script script = new Script();

	@After
	public void tearDown() {
		script.resetOfflineSorting();
	}

	@Test
	public void testYaleCAENOfflineSort() {
		final String sortRoutineName = "YaleCAENTestSortRoutine";
		script.setupOffline("help.sortfiles." + sortRoutineName,
				YaleCAEN_InputStream.class, YaleOutputStream.class);
		final HistInt2D eVsPSD = getTwoDHistogramFromSortGroup();
		final List<DimensionalData> psdGates = eVsPSD.getGateCollection()
				.getGates();
		Gate gate = null;
		for (DimensionalData data : psdGates) {
			if (data.getName().contains("PSD Gate a")) {
				gate = (Gate) data;
				break;
			}
		}
		final File gateFile = script
				.defineFile("test/sort/YaleCAENTestGates.hdf");
		script.loadHDF(gateFile);
		script.zeroHistograms();
		assertEquals("Expected histogram to be zeroed.", eVsPSD.getArea(), 0.0);
		assertEquals("Area in gate wasn't the same as expected.", 0.0, gate
				.getArea());
		final File eventFile = script
				.defineFile("test/sort/YaleCAENTestData.evn");
		script.addEventFile(eventFile);
		script.beginSort();
		final double expectedArea = 106.0;
		assertEquals("Area in gate wasn't the same as expected.", expectedArea,
				gate.getArea());
	}

	private HistInt2D getTwoDHistogramFromSortGroup() {
		final List<Histogram> twoDimHistograms = Histogram.getHistogramList(2);
		assertFalse("Expected 2D histograms.", twoDimHistograms.isEmpty());
		final String histName = "E vs PSD";
		HistInt2D eVsPSD = null;
		for (Histogram histogram : twoDimHistograms) {
			if (histogram.getName().contains(histName)) {
				eVsPSD = (HistInt2D) histogram;
				break;
			}
		}
		assertNotNull("Expected non-null histogram.", eVsPSD);
		return eVsPSD;
	}

	@Test
	public void testYaleOfflineSort() {
		final String sortRoutineName = "SpectrographExample";
		script.setupOffline("help.sortfiles." + sortRoutineName,
				YaleInputStream.class, YaleOutputStream.class);
		final HistInt1D cathode = getOneDHistogramFromSortGroup();
		assertEquals("Expected histogram to be zeroed.", cathode.getArea(), 0.0);
		final File eventFile = script.defineFile("sampledata/example.evn");
		script.addEventFile(eventFile);
		script.beginSort();
		final double expectedArea = 789.0;
		assertEquals("Area in histogram wasn't the same as expected.",
				expectedArea, cathode.getArea());
	}

	private HistInt1D getOneDHistogramFromSortGroup() {
		final List<Histogram> oneDimHistograms = Histogram.getHistogramList(1);
		assertFalse("Expected 1D histograms.", oneDimHistograms.isEmpty());
		final String histName = "Cathode";
		HistInt1D cathode = null;
		for (Histogram histogram : oneDimHistograms) {
			if (histogram.getName().contains(histName)) {
				cathode = (HistInt1D) histogram;
				break;
			}
		}
		return cathode;
	}
}
