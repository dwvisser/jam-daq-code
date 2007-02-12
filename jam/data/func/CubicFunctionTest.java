package jam.data.func;

import junit.framework.TestCase;

public class CubicFunctionTest extends TestCase {//NOPMD

	private transient CubicFunction function;

	private transient double[] channels;

	private transient double[] energies;

	protected void setUp() throws Exception {
		super.setUp();
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

	protected void tearDown() throws Exception {
		super.tearDown();
		function = null;//NOPMD
		channels = null;//NOPMD
		energies = null;//NOPMD
	}

	public final void testFit() {
		function.setPoints(channels, energies);
		try {
			function.fit();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertEquals("Cubic should have 4 terms.",4,function.getNumberTerms());
	}

}
