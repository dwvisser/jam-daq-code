package test.data.func;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import jam.data.func.CubicFunction;

import org.junit.Before;
import org.junit.Test;

public final class CubicFunctionTest {//NOPMD

	private transient CubicFunction function;

	private transient double[] channels;

	private transient double[] energies;

	@Before
	public void setUp() {
		function = new CubicFunction();
		channels = new double[5];
		for (int i = 0; i <= 4; i++) {
			channels[i] = i * 10.0;
		}
		energies = new double[5];
		energies[0] = 11.0;
		energies[1] = 21.0;
		energies[2] = 30.5;
		energies[3] = 41.0;
		energies[4] = 49.6;
	}

	@Test
	public void testFit() {
		function.setPoints(channels, energies);
		try {
			function.fit();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertEquals("Cubic should have 4 terms.",4,function.getNumberTerms());
	}

}
