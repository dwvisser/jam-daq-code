package jam.data.func;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class AbstractCalibrationFunctionCollection {

	private static final Map<String, Class<? extends AbstractCalibrationFunction>> FUNCTIONS = new TreeMap<String, Class<? extends AbstractCalibrationFunction>>();

	private static final Map<String, ImageIcon> ICONS = new HashMap<String, ImageIcon>();

	private static final AbstractCalibrationFunction noFunc = new NoFunction();

	static {
		clearAll();
		FUNCTIONS.put(noFunc.getName(), noFunc.getClass());
		final AbstractCalibrationFunction linearFunc = new LinearFunction();
		FUNCTIONS.put(linearFunc.getName(), linearFunc.getClass());
		final AbstractCalibrationFunction quadFunc = new QuadraticFunction();
		FUNCTIONS.put(quadFunc.getName(), quadFunc.getClass());
		final AbstractCalibrationFunction cubicFunc = new CubicFunction();
		FUNCTIONS.put(cubicFunc.getName(), cubicFunc.getClass());
		final AbstractCalibrationFunction sqrtEFunc = new SqrtEnergyFunction();
		FUNCTIONS.put(sqrtEFunc.getName(), sqrtEFunc.getClass());
	}

	private AbstractCalibrationFunctionCollection()
	{
		// Static-only class.
	}
	
	/**
	 * Clear the collections.
	 */
	private static void clearAll() {
		FUNCTIONS.clear();
		ICONS.clear();
	}

	public static ImageIcon getIcon(final String name) {
		return ICONS.get(name);
	}

	/**
	 * Returns the list of function names.
	 * 
	 * @return the list of function names
	 */
	public static List<String> getListNames() {
		return Collections.unmodifiableList(new ArrayList<String>(FUNCTIONS
				.keySet()));
	}

	/**
	 * Returns the map of function names to functions.
	 * 
	 * @return the map of function names to functions
	 */
	public static Map<String, Class<? extends AbstractCalibrationFunction>> getMapFunctions() {
		return Collections.unmodifiableMap(FUNCTIONS);
	}

	static public AbstractCalibrationFunction getNoCalibration() {
		return noFunc;
	}

	static void loadIcon(final AbstractCalibrationFunction calFunc,
			final String iconFile) {
		final ClassLoader loader = ClassLoader.getSystemClassLoader();

		final URL urlIcon = loader.getResource(iconFile);
		if (urlIcon == null) {
			JOptionPane.showMessageDialog(null,
					"Can't load resource for calibration function icon "
							+ iconFile);
		} else {
			setIcon(calFunc.getName(), new ImageIcon(urlIcon));
		}

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
