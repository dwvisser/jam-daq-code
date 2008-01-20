package test.sort;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import jam.Script;
import jam.data.HistInt1D;
import jam.sort.stream.YaleInputStream;
import jam.sort.stream.YaleOutputStream;

import org.junit.Test;

import test.sort.mockfrontend.GUI;

public class SortOnlineTest {

	@Test
	public void testSuccessfulOnlineSort() {
		GUI.main(null);
		final Script script = new Script();
		script.setupOnline("help.sortfiles.EvsDE", YaleInputStream.class,
				YaleOutputStream.class);
		final HistInt1D energy = Utility.getOneDHistogramFromSortGroup("E");
		assertNotNull("Expected histogram to exist.", energy);
		script.startAcquisition();
		try {
			Thread.sleep(2000);
		} catch (InterruptedException ie) {
			fail("Interrupted while sleeping." + ie.getMessage());
		}
		script.stopAcquisition();
		assertTrue("Expected counts > 0.", energy.getArea() > 0.0);
	}
}
