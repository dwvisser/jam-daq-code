package test.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import jam.data.DataBase;
import jam.data.DataParameter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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
		final StringBuilder name = new StringBuilder();
		// loop generates name with one character too many
		for (int i = 0; i <= DataParameter.NAME_LENGTH; i++) {
			name.append('x');
		}

		assertThrows(IllegalArgumentException.class, () -> new DataParameter(name.toString()));
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
		assertEquals(compare, parameter1.getName(), "Expected unmodified name.");
		final DataParameter parameter2 = new DataParameter(name);
		final String compare2 = name + "        [1]";
		assertEquals(compare2, parameter2.getName(), "Expected modified name.");
	}

	/**
	 * Test the getting and setting of values to parameters.
	 */
	@Test
	public void testGetSetValue() {
		final DataParameter testParameter = new DataParameter("testValue");
		assertEquals(0.0, testParameter.getValue(), 0.001,
			"Expected paramter inital value to be 0.");
		final double value = 3.141592;
		testParameter.setValue(value);
		assertEquals(value, testParameter.getValue(), 0.001,
			"Expected the value we set.");
	}

	/**
	 * Tests retrieval of parameter from internal table by name.
	 */
	@Test
	public void testGetParameter() {
		final String name = "testGet";
		final DataParameter testParameter = new DataParameter(name);
		final String paddedName = name + "         ";
		assertSame(DataParameter.getParameter(paddedName), testParameter,
			"Expected to get parameter by name.");
		DataBase.getInstance().clearAllLists();
	}
}
