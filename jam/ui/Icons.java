/*
 * Created on Nov 30, 2004
 */
package jam.ui;

import com.google.inject.Singleton;

import javax.swing.*;
import java.net.URL;

/**
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale W Visser </a>
 */
@Singleton
public final class Icons {
	/**
	 * represents stopping
	 */
	public final transient ImageIcon STOP;

	/**
	 * represents going
	 */
	public final transient ImageIcon GO_GREEN;

	/**
	 * represents pausing
	 */
	public final transient ImageIcon CAUTION;

	/**
	 * transparent
	 */
	public final transient ImageIcon CLEAR;

	/**
	 * for sort groups
	 */
	public final transient ImageIcon GROUP_SORT;

	/**
	 * for file groups
	 */
	public final transient ImageIcon GROUP_FILE;

	/**
	 * for temporary groups?
	 */
	public final transient ImageIcon GROUP_TEMP;

	/**
	 * for 1-d histograms
	 */
	public final transient ImageIcon HIST1D;

	/**
	 * for 2-d histograms
	 */
	public final transient ImageIcon HIST2D;

	/**
	 * for 1-d gates
	 */
	public final transient ImageIcon GATE1D;

	/**
	 * for 2-d gates
	 */
	public final transient ImageIcon GATE2D;

	/**
	 * for defined 1-d gates
	 */
	public final transient ImageIcon GATE_DEF1D;

	/**
	 * for defined 2-d gates
	 */
	public final transient ImageIcon GATE_DEF2D;

	/**
	 * for begin button
	 */
	public final transient ImageIcon BEGIN;

	/**
	 * for end button
	 */
	public final transient ImageIcon END;

	/**
	 * Default constructor, public for testing purposes.
	 */
	public Icons() {
		super();
		final ClassLoader LOADER = ClassLoader.getSystemClassLoader();
		final URL urlStop = LOADER.getResource("jam/ui/stop.png");
		final URL urlGo = LOADER.getResource("jam/ui/go.png");
		final URL urlClear = LOADER.getResource("jam/ui/clear.png");
		final URL urlCaution = LOADER.getResource("jam/ui/caution.png");
		final URL urlSort = LOADER.getResource("jam/ui/groupsort.png");
		final URL urlFile = LOADER.getResource("jam/ui/groupfile.png");
		final URL urlTemp = LOADER.getResource("jam/ui/grouptemp.png");
		final URL urlHist1D = LOADER.getResource("jam/ui/hist1D.png");
		final URL urlGate1D = LOADER.getResource("jam/ui/gate1D.png");
		final URL urlHist2D = LOADER.getResource("jam/ui/hist2D.png");
		final URL urlGate2D = LOADER.getResource("jam/ui/gate2D.png");
		final URL urlGateDef1D = LOADER.getResource("jam/ui/gateDefined1D.png");
		final URL urlGateDef2D = LOADER.getResource("jam/ui/gateDefined2D.png");
		final URL urlBegin = LOADER.getResource("jam/ui/begin.png");
		final URL urlEnd = LOADER.getResource("jam/ui/end.png");
		if (areAnyNull(urlStop, urlGo, urlClear, urlCaution, urlHist1D,
				urlGate1D, urlHist2D, urlGate2D, urlGateDef1D, urlGateDef2D,
				urlBegin, urlEnd)) {
			JOptionPane.showMessageDialog(null,
					"Can't load resource: jam/ui/*.png");
			STOP = GO_GREEN = CLEAR = CAUTION = GROUP_SORT = GROUP_FILE = GROUP_TEMP = HIST1D = HIST2D = GATE1D = GATE2D = GATE_DEF1D = GATE_DEF2D = BEGIN = END = null;
		} else {
			STOP = new ImageIcon(urlStop);
			GO_GREEN = new ImageIcon(urlGo);
			CAUTION = new ImageIcon(urlCaution);
			CLEAR = new ImageIcon(urlClear);
			GROUP_SORT = new ImageIcon(urlSort);
			GROUP_FILE = new ImageIcon(urlFile);
			GROUP_TEMP = new ImageIcon(urlTemp);
			HIST1D = new ImageIcon(urlHist1D);
			HIST2D = new ImageIcon(urlHist2D);
			GATE1D = new ImageIcon(urlGate1D);
			GATE2D = new ImageIcon(urlGate2D);
			GATE_DEF1D = new ImageIcon(urlGateDef1D);
			GATE_DEF2D = new ImageIcon(urlGateDef2D);
			BEGIN = new ImageIcon(urlBegin);
			END = new ImageIcon(urlEnd);
		}
	}

	protected boolean areAnyNull(final URL... urls) {
		boolean rval = false;
		for (int i = urls.length - 1; i >= 0; i--) {
			if (urls[i] == null) {
				rval = true;
				break;
			}
		}
		return rval;
	}
}