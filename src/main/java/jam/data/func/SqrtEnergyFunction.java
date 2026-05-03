package jam.data.func;

import java.text.NumberFormat;

/** A sqrt function that can be use to calibrate a histogram taken by a magnetic spectrometer. */
public class SqrtEnergyFunction extends AbstractLinearRegressionFunction {

  private static final int NUMBER_TERMS = 2;

  /** Creates a new <code>SqrtEnergyFunction </code> object of the specified type. */
  public SqrtEnergyFunction() {
    super("Linear in Square Root", NUMBER_TERMS);
    title = "\u221aE = a0 + a1\u2219ch";
    labels[0] = "a0";
    labels[1] = "a1";
    this.loadIcon("jam/data/func/sqrt.png");
  }

  /**
   * Get the calibration value at a specified channel.
   *
   * @param channel value at which to get calibration
   * @return calibration value of the channel
   */
  @Override
  public double getValue(final double channel) {
    return (coefficients[0] + coefficients[1] * channel)
        * (coefficients[0] + coefficients[1] * channel);
  }

  @Override
  public double getChannel(final double energy) {
    return (Math.sqrt(energy) - coefficients[0]) / coefficients[1];
  }

  /** do a fit of x y values */
  @Override
  public void fit() throws CalibrationFitException {
    final double[] sqrtE = new double[ptsEnergy.length];
    for (int i = 0; i < ptsEnergy.length; i++) {
      sqrtE[i] = Math.sqrt(ptsEnergy[i]);
    }
    setCoefficients(linearRegression(ptsChannel, sqrtE));
  }

  @Override
  protected String updateFormula(final NumberFormat numFormat) {
    return "\u221aE = "
        + numFormat.format(coefficients[0])
        + " + "
        + numFormat.format(coefficients[1])
        + "\u2219ch";
  }
}
