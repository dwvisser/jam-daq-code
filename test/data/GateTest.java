package test.data;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import jam.data.DataBase;
import jam.data.Gate;
import jam.data.Group;
import jam.data.Histogram;

import java.awt.Polygon;

import org.junit.After;
import org.junit.Test;

/**
 * JUnit tests for <code>jam.data.Gate</code>.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser </a>
 * @see Gate
 */
public class GateTest {// NOPMD

	private transient final Group group = Group.createGroup("TestGateGroup",
			Group.Type.FILE);

	private static final int LOWER_LIMIT = 10;

	private static final int UPPER_LIMIT = 50;

	private static final int LOWER_LIMIT_MINUS_1 = LOWER_LIMIT - 1;

	private static final int UPPER_LIMIT_PLUS_1 = UPPER_LIMIT + 1;

	private static final int IN_GATE = (LOWER_LIMIT + UPPER_LIMIT) / 2;

	/**
	 * Test for boolean inGate(int).
	 * 
	 * @see Gate#inGate(int)
	 */
	@Test
	public void inGateI() {
		final Histogram hist1 = group.createHistogram(new int[100], "h1");
		final Gate gate1 = new Gate("g1", hist1);
		gate1.setLimits(LOWER_LIMIT, UPPER_LIMIT);
		final String expected = "Expected channel in gate1: ";
		assertTrue(expected + LOWER_LIMIT, gate1.inGate(LOWER_LIMIT));
		assertTrue(expected + UPPER_LIMIT, gate1.inGate(UPPER_LIMIT));
		final String notExpected = "Did not expect channel in gate1: ";
		assertFalse(notExpected + LOWER_LIMIT_MINUS_1, gate1
				.inGate(LOWER_LIMIT_MINUS_1));
		assertFalse(notExpected + UPPER_LIMIT_PLUS_1, gate1
				.inGate(UPPER_LIMIT_PLUS_1));
	}

	private String getAssertMessage(final int xchan, final int ychan,
			final boolean expected) {
		return "Expected inGate(" + xchan + "," + ychan + ") to return "
				+ expected;
	}

	/**
	 * Test for boolean inGate(int, int).
	 * 
	 * @see Gate#inGate(int,int)
	 */
	@Test
	public void inGateII() {
		final Histogram hist2 = group.createHistogram(new int[100][100], "h2");
		final Gate gate2 = new Gate("g2", hist2);
		final int[] xpoints = { LOWER_LIMIT, GateTest.UPPER_LIMIT_PLUS_1,
				GateTest.UPPER_LIMIT_PLUS_1, LOWER_LIMIT };
		final int[] ypoints = { LOWER_LIMIT, LOWER_LIMIT,
				GateTest.UPPER_LIMIT_PLUS_1, GateTest.UPPER_LIMIT_PLUS_1 };
		final Polygon box = new Polygon(xpoints, ypoints, 4);
		gate2.setLimits(box);
		assertTrue(getAssertMessage(GateTest.LOWER_LIMIT, GateTest.IN_GATE,
				true), gate2.inGate(GateTest.LOWER_LIMIT, GateTest.IN_GATE));
		assertFalse(this.getAssertMessage(GateTest.LOWER_LIMIT_MINUS_1,
				GateTest.IN_GATE, false), gate2.inGate(
				GateTest.LOWER_LIMIT_MINUS_1, GateTest.IN_GATE));
		assertTrue(this.getAssertMessage(GateTest.UPPER_LIMIT,
				GateTest.IN_GATE, true), gate2.inGate(GateTest.UPPER_LIMIT,
				GateTest.IN_GATE));
		assertFalse(this.getAssertMessage(GateTest.UPPER_LIMIT_PLUS_1,
				GateTest.IN_GATE, false), gate2.inGate(
				GateTest.UPPER_LIMIT_PLUS_1, GateTest.IN_GATE));
		assertTrue(this.getAssertMessage(GateTest.IN_GATE,
				GateTest.LOWER_LIMIT, true), gate2.inGate(GateTest.IN_GATE,
				GateTest.LOWER_LIMIT));
		assertFalse(this.getAssertMessage(GateTest.IN_GATE,
				GateTest.LOWER_LIMIT_MINUS_1, false), gate2.inGate(
				GateTest.IN_GATE, GateTest.LOWER_LIMIT_MINUS_1));
		assertTrue(this.getAssertMessage(GateTest.IN_GATE,
				GateTest.UPPER_LIMIT, true), gate2.inGate(GateTest.IN_GATE,
				GateTest.UPPER_LIMIT));
		assertFalse(this.getAssertMessage(GateTest.IN_GATE,
				GateTest.UPPER_LIMIT_PLUS_1, false), gate2.inGate(
				GateTest.IN_GATE, GateTest.UPPER_LIMIT_PLUS_1));

	}

	@After
	public void tearDown() {
		DataBase.getInstance().clearAllLists();
	}
}
