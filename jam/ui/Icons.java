/*
 * Created on Nov 30, 2004
 */
package jam.ui;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * @author <a href="mailto:dale@visser.name">Dale W Visser </a>
 */
class Icons {

	static final ImageIcon STOP, GO, CAUTION, CLEAR, HIST1D, HIST2D, GATE1D,
			GATE2D, GATE_DEF1D, GATE_DEF2D;

	static {
		final ClassLoader LOADER = ClassLoader.getSystemClassLoader();
		URL urlStop = LOADER.getResource("jam/ui/stop.png");
		URL urlGo = LOADER.getResource("jam/ui/go.png");
		URL urlClear = LOADER.getResource("jam/ui/clear.png");
		URL urlCaution = LOADER.getResource("jam/ui/caution.png");
		URL urlHist1D = LOADER.getResource("jam/ui/hist1D.png");
		URL urlGate1D = LOADER.getResource("jam/ui/gate1D.png");
		URL urlHist2D = LOADER.getResource("jam/ui/hist2D.png");
		URL urlGate2D = LOADER.getResource("jam/ui/gate2D.png");
		URL urlGateDef1D = LOADER.getResource("jam/ui/gateDefined1D.png");
		URL urlGateDef2D = LOADER.getResource("jam/ui/gateDefined2D.png");
		if (urlStop == null || urlGo == null || urlClear == null
				|| urlCaution == null || urlHist1D == null || urlGate1D == null
				|| urlHist2D == null || urlGate2D == null
				|| urlGateDef1D == null || urlGateDef2D == null) {
			JOptionPane.showMessageDialog(null,
					"Can't load resource: jam/ui/*.png");
			STOP = GO = CLEAR = CAUTION = HIST1D = HIST2D = GATE1D = GATE2D = GATE_DEF1D = GATE_DEF2D = null;
		} else {
			STOP = new ImageIcon(urlStop);
			GO = new ImageIcon(urlGo);
			CAUTION = new ImageIcon(urlCaution);
			CLEAR = new ImageIcon(urlClear);
			HIST1D = new ImageIcon(urlHist1D);
			HIST2D = new ImageIcon(urlHist2D);
			GATE1D = new ImageIcon(urlGate1D);
			GATE2D = new ImageIcon(urlGate2D);
			GATE_DEF1D = new ImageIcon(urlGateDef1D);
			GATE_DEF2D = new ImageIcon(urlGateDef2D);
		}
	}
}