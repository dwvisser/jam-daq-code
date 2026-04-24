package test.data.func;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import jam.data.func.CubicFunction;
import org.junit.jupiter.api.Test;

/**
 * Test cubic fits.
 *
 * @author Dale Visser
 */
public final class CubicFunctionTest { // NOPMD

  private static final int CUBIC_TERMS = 4;

  /** Test cubic fits. */
  @Test
  public void testFit() {
    final double[] channels = {0.0, 10.0, 20.0, 30.0, 40.0};
    final double[] energies = {11.0, 21.0, 30.5, 41.0, 49.6};
    final CubicFunction function = new CubicFunction();
    function.setPoints(channels, energies);
    try {
      function.fit();
    } catch (Exception e) {
      fail(e.getMessage());
    }
    assertEquals(
        CUBIC_TERMS, function.getNumberTerms(), "Expected number of terms = " + CUBIC_TERMS);
  }
}
