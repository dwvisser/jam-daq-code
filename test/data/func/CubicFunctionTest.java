package test.data.func;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import jam.data.func.CubicFunction;

import org.junit.Test;

public final class CubicFunctionTest {// NOPMD

	private static final int EXPECTED_CUBIC_TERMS = 4;

	@Test
	public void testFit() {
		final double[] channels = { 0.0, 10.0, 20.0, 30.0, 40.0 };
		final double[] energies = { 11.0, 21.0, 30.5, 41.0, 49.6 };
		final CubicFunction function = new CubicFunction();
		function.setPoints(channels, energies);
		try {
			function.fit();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertEquals("Expected number of terms = " + EXPECTED_CUBIC_TERMS,
				EXPECTED_CUBIC_TERMS, function.getNumberTerms());
	}

}
