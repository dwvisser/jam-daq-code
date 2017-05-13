package jam.data.func;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;

//TODO Make this dynamically load from classpath.
//TODO Make this load a class only when needed, i.e., make it a factory.
/**
 * Class for obtaining specific instances of calibration functions.
 * 
 * @author Dale Visser
 */
public final class CalibrationFunctionCollection {

	private static final Map<String, Class<? extends AbstractCalibrationFunction>> FUNCTIONS = new TreeMap<>();

	private static final Map<String, ImageIcon> ICONS = new HashMap<>();

	/**
	 * Calibration function representing no calibration at all.
	 */
	public static final AbstractCalibrationFunction NO_CALIBRATION = new NoFunction();

	static {
		clearAll();
		FUNCTIONS.put(NO_CALIBRATION.getName(), NO_CALIBRATION.getClass());
		final AbstractCalibrationFunction linearFunc = new LinearFunction();
		FUNCTIONS.put(linearFunc.getName(), linearFunc.getClass());
		final AbstractCalibrationFunction quadFunc = new QuadraticFunction();
		FUNCTIONS.put(quadFunc.getName(), quadFunc.getClass());
		final AbstractCalibrationFunction cubicFunc = new CubicFunction();
		FUNCTIONS.put(cubicFunc.getName(), cubicFunc.getClass());
		final AbstractCalibrationFunction sqrtEFunc = new SqrtEnergyFunction();
		FUNCTIONS.put(sqrtEFunc.getName(), sqrtEFunc.getClass());
	}

	private CalibrationFunctionCollection() {
		// Static-only class.
	}

	/**
	 * Clear the collections.
	 */
	private static void clearAll() {
		FUNCTIONS.clear();
		ICONS.clear();
	}

	// TODO Make the icons the responsibility of the calibration function.
	/**
	 * @param name
	 *            the name of the calibration function
	 * @return the icon for the calibration function
	 */
	public static ImageIcon getIcon(final String name) {
		return ICONS.get(name);
	}

	/**
	 * Returns the list of function names.
	 * 
	 * @return the list of function names
	 */
	public static List<String> getListNames() {
		return Collections.unmodifiableList(new ArrayList<>(FUNCTIONS.keySet()));
	}

	/**
	 * Returns the map of function names to functions.
	 * 
	 * @return the map of function names to functions
	 */
	public static Map<String, Class<? extends AbstractCalibrationFunction>> getMapFunctions() {
		return Collections.unmodifiableMap(FUNCTIONS);
	}

	/**
	 * Sets an icon for the given function name.
	 * 
	 * @param name
	 *            of the function
	 * @param icon
	 *            for the function
	 */
	public static void setIcon(final String name, final ImageIcon icon) {
		ICONS.put(name, icon);
	}

}
