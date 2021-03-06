/*
 * Created on Nov 30, 2004
 */
package jam.data.peaks;

/**
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser</a>
 */
public final class GaussianConstants {
	
	private GaussianConstants(){
		super();
	}
	
	/**
	 * Coefficient for proper normalization of gaussians.
	 */
	public static final double MAGIC_A = 2.0 * Math.sqrt(Math.log(2.0) / Math.PI);

	/**
	 * Factor in exponential terms to properly relate width to sigma.
	 */
	public static final double MAGIC_B = 4.0 * Math.log(2.0);
	
	/**
	 * 2 * A * B, appears in derivatives a lot
	 */
	public static final double MAGIC_2AB=16.0 * Math.sqrt(Math.pow(Math.log(2.0),3.0)/Math.PI);

	/**
	 * Ratio of FWHM to sigma for a gaussian.
	 */
	public static final double SIG_TO_FWHM = 2.0 * Math.sqrt(2.0 * Math.log(2.0));

}
