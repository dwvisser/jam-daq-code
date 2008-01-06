package test.sort;

import jam.Script;
import jam.sort.control.SetupSortOn;

import org.junit.Test;

public class SortOnlineTest {

	@Test
	public void testSuccessfulOnlineSort() {
		final Script s = new Script();
		final SetupSortOn sso = SetupSortOn.getInstance();
		s.zeroHistograms();
	}
}
