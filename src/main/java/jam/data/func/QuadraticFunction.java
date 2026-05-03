package jam.data.func;

import java.text.NumberFormat;

/** A quadratic histogram calibration function, that is, E = a0 + a1 * channel+a2*channel^2 */
public class QuadraticFunction extends AbstractGaussJordanFunction {

  private static final int NUMBER_TERMS = 3;

  /** Creates a new <code>QuadraticFunction</code> object of the specified type. */
  public QuadraticFunction() {
    super("Quadratic", NUMBER_TERMS);
    title = "E = a0 + a1\u2219ch a2\u2219ch^2";
    labels[0] = "a0";
    labels[1] = "a1";
    labels[2] = "a2";
    this.loadIcon("jam/data/func/quad.png");
  }

  /**
   * Get the calibration value at a specified channel.
   *
   * @param channel value at which to get calibration
   * @return calibration value of the channel
   */
  @Override
  public double getValue(final double channel) {
    return coefficients[0] + coefficients[1] * channel + coefficients[2] * channel * channel;
  }

  /** do a fit of x y values */
  @Override
  public void fit() throws CalibrationFitException {
    final double[] coeffQuad = polynomialFit(ptsEnergy, ptsChannel, 2);
    System.arraycopy(coeffQuad, 0, coefficients, 0, coeffQuad.length);
  }

  @Override
  public String updateFormula(final NumberFormat numFormat) {
    return "E = "
        + numFormat.format(coefficients[0])
        + " + "
        + numFormat.format(coefficients[1])
        + "\u2219ch + "
        + numFormat.format(coefficients[2])
        + "\u2219ch^2";
  }
}
