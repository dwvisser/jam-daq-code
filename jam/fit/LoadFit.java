package jam.fit;

import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.JamException;
import jam.global.RuntimeSubclassIdentifier;
import jam.plot.PlotDisplay;
import jam.ui.PanelOKApplyCancelButtons;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import com.google.inject.Inject;

/**
 * Load a fit routine.. Draw the fit routines interface window
 * 
 * @version 1.1
 * @author <a href="mailto:dwvisser@users.sourceforge.net">Dale Visser</a>
 */
public class LoadFit {

	private static final Logger LOGGER = Logger.getLogger(LoadFit.class
			.getPackage().getName());

	private transient final Broadcaster broadcaster;

	private transient final JDialog dialog;

	private transient final PlotDisplay display;

	private transient final Frame jamMain;

	private transient final RuntimeSubclassIdentifier rtsi;

	/**
	 * Create the fit routine loading dialog.
	 * 
	 * @param frame
	 *            application frame
	 * @param display
	 *            plot display
	 * @param broadcaster
	 *            broadcasts state changes
	 * @param rtsi
	 *            for finding fit classes
	 */
	@Inject
	public LoadFit(final JFrame frame, final PlotDisplay display,
			final Broadcaster broadcaster, final RuntimeSubclassIdentifier rtsi) {
		super();
		this.broadcaster = broadcaster;
		this.rtsi = rtsi;
		jamMain = frame;
		this.display = display;
		final String dialogName = "Load Fit Routine";
		dialog = new JDialog(jamMain, dialogName, false);
		final Container contents = dialog.getContentPane();
		dialog.setResizable(false);
		final int posx = 20;
		final int posy = 50;
		dialog.setLocation(posx, posy);
		contents.setLayout(new BorderLayout());
		// panel for fit file
		final JPanel pFit = new JPanel(new FlowLayout(FlowLayout.CENTER));
		final Border border = new EmptyBorder(20, 20, 20, 20);
		pFit.setBorder(border);
		final JLabel lFit = new JLabel("Fit class: ", SwingConstants.RIGHT);
		pFit.add(lFit);
		final JComboBox chooseFit = new JComboBox(this.getFitClasses());
		final Dimension dim = chooseFit.getPreferredSize();
		dim.width = 200;
		chooseFit.setPreferredSize(dim);
		pFit.add(chooseFit);
		final PanelOKApplyCancelButtons.Listener callback = new PanelOKApplyCancelButtons.AbstractListener(
				dialog) {
			@SuppressWarnings("unchecked")
			public void apply() {
				final Class<? extends AbstractFit> fit = (Class<? extends AbstractFit>) chooseFit
						.getSelectedItem();
				try {
					makeFit(fit);
				} catch (JamException je) {
					LOGGER.log(Level.SEVERE, je.getMessage(), je);
				}
			}
		};
		final PanelOKApplyCancelButtons buttons = new PanelOKApplyCancelButtons(
				callback);
		contents.add(pFit, BorderLayout.CENTER);
		contents.add(buttons.getComponent(), BorderLayout.SOUTH);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.pack();
	}

	private Object[] getFitClasses() {
		final String package1 = "jam.fit";
		final String package2 = "fit";
		final Set<Class<? extends AbstractFit>> set = rtsi.find(package1,
				AbstractFit.class, false);
		set.addAll(rtsi.find(package2, AbstractFit.class, false));
		return set.toArray();
	}

	private void makeFit(final Class<? extends AbstractFit> fitClass)
			throws JamException {
		final String fitName = fitClass.getName();
		try {

			final AbstractFit fit = fitClass.newInstance();
			final int indexPeriod = fitName.lastIndexOf('.');
			final String fitNameFront = fitName.substring(indexPeriod + 1);
			final FitDialog dfit = fit.createDialog(jamMain, display);
			// Create action for menu
			final Action fitAction = new AbstractAction(fitNameFront) {
				public void actionPerformed(final ActionEvent ae) {
					dfit.show();
				}
			};
			broadcaster.broadcast(BroadcastEvent.Command.FIT_NEW, fitAction);
		} catch (InstantiationException ie) {
			throw new JamException(" Fit Class cannot instantize: " + fitName,
					ie);
		} catch (IllegalAccessException iae) {
			throw new JamException(" Fit Class cannot Access: " + fitName, iae);
		}
	}

	/**
	 * Show the load fit routine dialog box
	 */
	public void showLoad() {
		dialog.setVisible(true);
	}
}
