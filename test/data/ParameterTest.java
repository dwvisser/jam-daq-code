package test.data;

import jam.data.DataBase;
import jam.data.DataParameter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for <code>jam.data.DataParameter</code>.
 * 
 * @author <a href="mailto:dale.visser@gmail.com">Dale Visser</a>
 */
public class ParameterTest {// NOPMD

	/**
	 * Make sure DataParameter constructor throws for too long name.
	 */
	@Test
	public void createParameterFail() {
		try {
			final StringBuilder name = new StringBuilder();
			// loop generates name with one character too many
			for (int i = 0; i <= DataParameter.NAME_LENGTH; i++) {
				name.append('x');
			}

			new DataParameter(name.toString());
			Assert.fail("Expected IllegalArgumentException for name too long.");
		} catch (IllegalArgumentException iae) {// NOPMD
			// pass
		}
	}

	/**
	 * Test name properties of new parameters, when duplicate names are
	 * attempted.
	 */
	@Test
	public void createDuplicateNamedParameters() {
		final String name = "param";
		final String compare = name + "           ";
		final DataParameter parameter1 = new DataParameter(name);
		Assert.assertEquals("Expected unmodified name.", compare, parameter1
				.getName());
		final DataParameter parameter2 = new DataParameter(name);
		final String compare2 = name + "        [1]";
		Assert.assertEquals("Expected modified name.", compare2, parameter2
				.getName());
	}

	@Test
	public void testGetSetValue() {
		final DataParameter testParameter = new DataParameter("testValue");
		Assert.assertEquals("Expected paramter inital value to be 0.", 0.0,
				testParameter.getValue());
		final double value = 3.141592;
		testParameter.setValue(value);
		Assert.assertEquals("Expected the value we set.", value, testParameter
				.getValue());
	}

	/**
	 * Tests retrieval of parameter from internal table by name.
	 */
	@Test
	public void testGetParameter() {
		final String name = "testGet";
		final DataParameter testParameter = new DataParameter(name);
		final String paddedName = name + "         ";
		Assert.assertSame("Expected to get parameter by name.", DataParameter
				.getParameter(paddedName), testParameter);
	}

	/**
	 * Clean up after tests.
	 */
	@After
	public void tearDown() {
		DataBase.getInstance().clearAllLists();
	}
}
