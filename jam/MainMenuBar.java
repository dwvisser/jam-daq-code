package jam;
import jam.data.Histogram;
import jam.data.control.DataControl;
import jam.fit.LoadFit;
import jam.global.ComponentPrintable;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.io.ImpExp;
import jam.io.ImpExpASCII;
import jam.io.ImpExpORNL;
import jam.io.ImpExpSPE;
import jam.io.ImpExpXSYS;
import jam.io.ImportBanGates;
import jam.plot.Display;
import jam.plot.PlotGraphicsLayout;
import jam.util.YaleCAENgetScalers;

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

/**
 *
 * Jam's menu bar. Separated from JamMain to reduce its 
 * size and separate responsibilities.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 * @version 1.4
 * @since 30 Dec 2003
 */
public class MainMenuBar extends JMenuBar {

	/**
	 * Action for the File|Print menu item. 
	 * 
	 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
	 * @version Jan 23, 2004
	 */
	public class FilePrintAction extends AbstractAction {
		
		/**
		 * Constructor which gives the name to display.
		 */
		public FilePrintAction() {
			super("Print...");
		}
		
		private boolean firstTime=true;

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent ae) {
			if (firstTime){
				console.warningOutln("On some systems, it will be necessary to first "+
				"use 'Page Setup...' for your hardcopy to have correct size and margins.");
				firstTime=false;
			}
			PrinterJob pj = PrinterJob.getPrinterJob();
			ComponentPrintable cp = display.getComponentPrintable();
			pj.setPrintable(cp, mPageFormat);
			if (pj.printDialog()) {
				console.messageOut("Preparing to send histogram '" + 
				JamStatus.instance().getCurrentHistogramName()+"' to printer...",
				MessageHandler.NEW);
				try {
					display.setRenderForPrinting(true, mPageFormat);
					pj.print();
					console.messageOut("sent.", MessageHandler.END);
					display.setRenderForPrinting(false, null);
				} catch (PrinterException e) {
					final StringBuffer mess=new StringBuffer(getClass().getName());
					final String colon=": ";
					mess.append(colon);
					mess.append(e.getMessage());
					console.errorOutln(mess.toString());
				}
			}
		}
	}
	
	/**
	 * Action for the File|Page Setup menu item.
	 * 
	 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
	 * @version Jan 23, 2004
	 */
	public class FilePageSetupAction extends AbstractAction {
		
		/**
		 * Constructor which gives the name to display.
		 */
		public FilePageSetupAction(){
			super("Page Setup...");
		}
		
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent ae){
			PrinterJob pj=PrinterJob.getPrinterJob();
			mPageFormat = pj.pageDialog(mPageFormat);
		}
	}
	
	class ImportAction extends AbstractAction {
		
		private final ImpExp impexp;
		
		ImportAction(ImpExp ie){
			super(ie.getFormatDescription());
			impexp=ie;
		}
		
		public void actionPerformed(ActionEvent ae){
			try {
				if (impexp.openFile()) {
					jamMain.setSortModeFile(impexp.getLastFileName());
					DataControl.setupAll();
					jamCommand.dataChanged();
				}
			} catch (Exception e){
				console.errorOutln(e.getMessage());
			}
		}
	}

	class ExportAction extends AbstractAction {
		
		private final ImpExp impexp;
		
		ExportAction(ImpExp ie){
			super(ie.getFormatDescription());
			impexp=ie;
		}
		
		public void actionPerformed(ActionEvent ae){
			try {
				impexp.saveFile(Histogram.getHistogram(
				JamStatus.instance().getCurrentHistogramName()));
			} catch (Exception e){
				console.errorOutln(e.getMessage());
			}
		}
	}


	static final String NO_FILL_MENU_TEXT = "Disable Gate Fill";

	final private JMenu fitting;
	final private JMenuItem newClear,
		openhdf,
		reloadhdf,
		saveHDF,
		impHist,
		open,
		reload,
		save,
		runacq,
		sortacq,
		paramacq,
		statusacq,
		iflushacq;
	final private JCheckBoxMenuItem cstartacq, cstopacq;
	final private LoadFit loadfit;
	final private Display display;
	final private JamMain jamMain;
	final private MessageHandler console;
	final private JamCommand jamCommand;

	private PageFormat mPageFormat=PrinterJob.getPrinterJob().defaultPage();

	/**
	 * Define and display menu bar.
	 * The menu bar has the following menus: 
	 * <ul>
	 * <li>File</li>
	 * <li>Setup</li>
	 * <li>Control</li>
	 * <li>Histogram</li>
	 * <li>Gate</li>
	 * <li>Scalers</li>
	 * <li>Preferencs</li>
	 * <li>Fitting </li>
	 * <li>Help</li>
	 * </ul>
	 * 
	 * @author Ken Swartz
	 * @return the menu bar
	 */
	MainMenuBar(
		final JamMain jm,
		JamCommand jamCommand,
		final Display d,
		MessageHandler c) {
		super();
		this.jamCommand=jamCommand;
		final int ctrl_mask;
		final boolean macosx=JamProperties.isMacOSX();
		if (macosx){
			ctrl_mask=Event.META_MASK;
		} else {
			ctrl_mask=Event.CTRL_MASK;
		}
		final double inchesToPica=72.0;
		final double top=PlotGraphicsLayout.MARGIN_TOP*inchesToPica;
		final double bottom=mPageFormat.getHeight()-
		PlotGraphicsLayout.MARGIN_BOTTOM*inchesToPica;
		final double height=bottom-top;
		final double left=PlotGraphicsLayout.MARGIN_LEFT*inchesToPica;
		final double right=mPageFormat.getWidth()-
		PlotGraphicsLayout.MARGIN_RIGHT*inchesToPica;
		final double width=right-left;
		final Paper paper=mPageFormat.getPaper();
		paper.setImageableArea(top,left,width,height);
		mPageFormat.setPaper(paper);
		mPageFormat.setOrientation(PageFormat.LANDSCAPE);
		console=c;
		display = d;
		jamMain=jm;
		/* load fitting routine */
		loadfit = new LoadFit(jm, display, console, this);
		final JMenu file = new JMenu("File");
		add(file);
		synchronized (this) {
			newClear = new JMenuItem("New");
			openhdf = new JMenuItem("Open(hdf)...");
			reloadhdf = new JMenuItem("Reload(hdf)...");
			saveHDF = new JMenuItem("Save(hdf)");
			impHist = new JMenu("Import");
			open = new JMenuItem("Open(jhf)...");
			reload = new JMenuItem("Reload(jhf)...");
			save = new JMenuItem("Save(jhf)");
			runacq = new JMenuItem("Run...");
			sortacq = new JMenuItem("Sort...");
			paramacq = new JMenuItem("Parameters...");
			statusacq = new JMenuItem("Buffer Count...");
		}
		newClear.setActionCommand("newclear");
		newClear.addActionListener(jamCommand);
		file.add(newClear);
		openhdf.setActionCommand("openhdf");
		openhdf.addActionListener(jamCommand);
		file.add(openhdf);
		reloadhdf.setActionCommand("reloadhdf");
		reloadhdf.addActionListener(jamCommand);
		reloadhdf.setEnabled(false);
		file.add(reloadhdf);
		saveHDF.setActionCommand("savehdf");
		saveHDF.setEnabled(false);
		saveHDF.addActionListener(jamCommand);
		saveHDF.setEnabled(false);
		file.add(saveHDF);
		final JMenuItem saveAsHDF = new JMenuItem("Save as (hdf)...");
		saveAsHDF.setActionCommand("saveAsHDF");
		saveAsHDF.addActionListener(jamCommand);
		file.add(saveAsHDF);
		file.addSeparator();
		JMenuItem utilities=new JMenu("Utilities");
		file.add(utilities);
		final YaleCAENgetScalers ycgs=new YaleCAENgetScalers(jamMain,console);
		utilities.add(new JMenuItem(ycgs.getAction()));
		file.addSeparator();
		file.add(impHist);
		final ImpExp ieASCII=new ImpExpASCII(jamMain,console);
		final JMenuItem openascii = new JMenuItem(new ImportAction(ieASCII));
		impHist.add(openascii);
		final ImpExp ieSpe=new ImpExpSPE(jamMain,console);
		final JMenuItem openspe = new JMenuItem(new ImportAction(ieSpe));
		impHist.add(openspe);
		final ImpExp ieHis=new ImpExpORNL(jamMain,console);
		final JMenuItem openornl = new JMenuItem(new ImportAction(ieHis));
		impHist.add(openornl);
		final ImpExp ieXsys = new ImpExpXSYS(jamMain,console);
		final JMenuItem openxsys = new JMenuItem(new ImportAction(ieXsys));
		impHist.add(openxsys);
		final JMenuItem importBan = new JMenuItem(new ImportAction(
		new ImportBanGates(jamMain,console)));
		impHist.add(importBan);
		final JMenu expHist = new JMenu("Export");
		file.add(expHist);
		final JMenuItem saveascii = new JMenuItem(new ExportAction(ieASCII));
		expHist.add(saveascii);
		final JMenuItem savespe = new JMenuItem(new ExportAction(ieSpe));
		expHist.add(savespe);
		final JMenuItem saveornl = new JMenuItem(new ExportAction(ieHis));
		expHist.add(saveornl);
		final JMenuItem batchexport = new JMenuItem("Batch Export...");
		batchexport.setActionCommand("batchexport");
		batchexport.addActionListener(jamCommand);
		expHist.add(batchexport);
		file.addSeparator();
		final JMenu oldJHF = new JMenu("JHF Format");
		file.add(oldJHF);
		open.setActionCommand("open");
		open.addActionListener(jamCommand);
		oldJHF.add(open);
		reload.setActionCommand("reload");
		reload.addActionListener(jamCommand);
		reload.setEnabled(false);
		oldJHF.add(reload);
		save.setActionCommand("save");
		save.addActionListener(jamCommand);
		save.setEnabled(false);
		oldJHF.add(save);
		final JMenuItem saveas = new JMenuItem("Save as(jhf)...");
		saveas.setActionCommand("saveas");
		saveas.addActionListener(jamCommand);
		oldJHF.add(saveas);
		file.addSeparator();
		file.add(new FilePrintAction()).setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_P, ctrl_mask));
		file.add(new FilePageSetupAction()).setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_P, 
		ctrl_mask | Event.SHIFT_MASK));
		file.addSeparator();
		final JMenuItem exit = new JMenuItem("Exit...");
		exit.setActionCommand("exitShow");
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				jm.showExitDialog();
			}
		});
		if (macosx){
			exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
			ctrl_mask));
		}
		file.add(exit);
		final JMenu setup = new JMenu("Setup");
		add(setup);
		final JMenuItem setupOnline = new JMenuItem("Online sorting...");
		setupOnline.setActionCommand("online");
		setupOnline.addActionListener(jamCommand);
		setup.add(setupOnline);
		setup.addSeparator();
		final JMenuItem setupOffline = new JMenuItem("Offline sorting...");
		setupOffline.setActionCommand("offline");
		setupOffline.addActionListener(jamCommand);
		setup.add(setupOffline);
		setup.addSeparator();
		final JMenuItem setupRemote = new JMenuItem("Remote Hookup...");
		setupRemote.setActionCommand("remote");
		setupRemote.addActionListener(jamCommand);
		setupRemote.setEnabled(false);
		setup.add(setupRemote);
		final JMenu mcontrol = new JMenu("Control");
		add(mcontrol);
		cstartacq = new JCheckBoxMenuItem("start", false);
		cstartacq.setEnabled(false);
		cstartacq.addActionListener(jamCommand);
		mcontrol.add(cstartacq);
		cstopacq = new JCheckBoxMenuItem("stop", true);
		cstopacq.setEnabled(false);
		cstopacq.addActionListener(jamCommand);
		mcontrol.add(cstopacq);
		iflushacq = new JMenuItem("flush");
		iflushacq.setEnabled(false);
		iflushacq.addActionListener(jamCommand);
		mcontrol.add(iflushacq);
		mcontrol.addSeparator();
		runacq.setEnabled(false);
		runacq.setActionCommand("run");
		runacq.addActionListener(jamCommand);
		mcontrol.add(runacq);
		sortacq.setEnabled(false);
		sortacq.setActionCommand("sort");
		sortacq.addActionListener(jamCommand);
		mcontrol.add(sortacq);
		paramacq.setEnabled(false);
		paramacq.setActionCommand("parameters");
		paramacq.addActionListener(jamCommand);
		mcontrol.add(paramacq);
		statusacq.setEnabled(false);
		statusacq.setActionCommand("status");
		statusacq.addActionListener(jamCommand);
		mcontrol.add(statusacq);
		final JMenu histogram = new JMenu("Histogram");
		add(histogram);
		final JMenuItem histogramNew = new JMenuItem("New...");
		histogramNew.setActionCommand("newhist");
		histogramNew.addActionListener(jamCommand);
		histogram.add(histogramNew);
		final JMenuItem zeroHistogram = new JMenuItem("Zero...");
		zeroHistogram.setActionCommand("zerohist");
		zeroHistogram.addActionListener(jamCommand);
		histogram.add(zeroHistogram);
		final JMenu calHist = new JMenu("Calibrate");
		histogram.add(calHist);
		final JMenuItem calibFit = new JMenuItem("Fit...");
		calibFit.setActionCommand("calfitlin");
		calibFit.addActionListener(jamCommand);
		calHist.add(calibFit);
		final JMenuItem calibFunc = new JMenuItem("Enter Coefficients...");
		calibFunc.setActionCommand("caldisp");
		calibFunc.addActionListener(jamCommand);
		calHist.add(calibFunc);
		final JMenuItem projectHistogram = new JMenuItem("Projections...");
		projectHistogram.setActionCommand("project");
		projectHistogram.addActionListener(jamCommand);
		histogram.add(projectHistogram);
		final JMenuItem manipHistogram = new JMenuItem("Combine...");
		manipHistogram.setActionCommand("manipulate");
		manipHistogram.addActionListener(jamCommand);
		histogram.add(manipHistogram);
		final JMenuItem gainShift = new JMenuItem("Gain Shift...");
		gainShift.setActionCommand("gainshift");
		gainShift.addActionListener(jamCommand);
		histogram.add(gainShift);
		final JMenu gate = new JMenu("Gate");
		add(gate);
		final JMenuItem gateNew = new JMenuItem("New Gate...");
		gateNew.setActionCommand("gatenew");
		gateNew.addActionListener(jamCommand);
		gate.add(gateNew);
		final JMenuItem gateAdd = new JMenuItem("Add Gate...");
		gateAdd.setActionCommand("gateadd");
		gateAdd.addActionListener(jamCommand);
		gate.add(gateAdd);
		final JMenuItem gateSet = new JMenuItem("Set Gate...");
		gateSet.setActionCommand("gateset");
		gateSet.addActionListener(jamCommand);
		gate.add(gateSet);
		final JMenu scalers = new JMenu("Scalers");
		add(scalers);
		final JMenuItem showScalers = new JMenuItem("Display Scalers...");
		showScalers.setActionCommand("displayscalers");
		showScalers.addActionListener(jamCommand);
		scalers.add(showScalers);
		final JMenuItem clearScalers = new JMenuItem("Zero Scalers...");
		scalers.add(clearScalers);
		clearScalers.setActionCommand("zeroscalers");
		clearScalers.addActionListener(jamCommand);
		scalers.addSeparator();
		final JMenuItem showMonitors = new JMenuItem("Display Monitors...");
		showMonitors.setActionCommand("displaymonitors");
		showMonitors.addActionListener(jamCommand);
		scalers.add(showMonitors);
		final JMenuItem configMonitors = new JMenuItem("Configure Monitors...");
		configMonitors.setActionCommand("configmonitors");
		configMonitors.addActionListener(jamCommand);
		scalers.add(configMonitors);
		final JMenu mPrefer = new JMenu("Preferences");
		add(mPrefer);
		final JCheckBoxMenuItem ignoreZero =
			new JCheckBoxMenuItem("Ignore zero channel on autoscale", true);
		ignoreZero.setEnabled(true);
		ignoreZero.addItemListener(jamCommand);
		mPrefer.add(ignoreZero);
		final JCheckBoxMenuItem ignoreFull =
			new JCheckBoxMenuItem("Ignore max channel on autoscale", true);
		ignoreFull.setEnabled(true);
		ignoreFull.addItemListener(jamCommand);
		mPrefer.add(ignoreFull);
		final JCheckBoxMenuItem autoOnExpand =
			new JCheckBoxMenuItem("Autoscale on Expand/Zoom", true);
		autoOnExpand.setEnabled(true);
		autoOnExpand.addItemListener(jamCommand);
		mPrefer.add(autoOnExpand);
		mPrefer.addSeparator();
		final JCheckBoxMenuItem noFill2d =
			new JCheckBoxMenuItem(
				NO_FILL_MENU_TEXT,
				JamProperties.getBooleanProperty(JamProperties.NO_FILL_GATE));
		noFill2d.setEnabled(true);
		noFill2d.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				JamProperties.setProperty(
					JamProperties.NO_FILL_GATE,
					ie.getStateChange() == ItemEvent.SELECTED);
					display.repaint();
			}
		});
		mPrefer.add(noFill2d);
		final JCheckBoxMenuItem gradientColorScale =
			new JCheckBoxMenuItem(
				"Use gradient color scale",
				JamProperties.getBooleanProperty(JamProperties.GRADIENT_SCALE));
		gradientColorScale.setToolTipText(
			"Check to use a continuous gradient color scale on 2d histogram plots.");
		gradientColorScale.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				final boolean state = ie.getStateChange() == ItemEvent.SELECTED;
				JamProperties.setProperty(JamProperties.GRADIENT_SCALE, state);
				display.setPreference(
					Display.Preferences.CONTINUOUS_2D_LOG,
					state);
			}
		});
		mPrefer.add(gradientColorScale);
		mPrefer.addSeparator();
		final JCheckBoxMenuItem autoPeakFind =
			new JCheckBoxMenuItem("Automatic peak find", true);
		autoPeakFind.setEnabled(true);
		autoPeakFind.addItemListener(jamCommand);
		mPrefer.add(autoPeakFind);
		final JMenuItem peakFindPrefs =
			new JMenuItem("Peak Find Properties...");
		peakFindPrefs.setActionCommand("peakfind");
		peakFindPrefs.addActionListener(jamCommand);
		mPrefer.add(peakFindPrefs);
		mPrefer.addSeparator();
		final ButtonGroup colorScheme = new ButtonGroup();
		final JRadioButtonMenuItem whiteOnBlack =
			new JRadioButtonMenuItem("Black Background", false);
		whiteOnBlack.setEnabled(true);
		whiteOnBlack.addActionListener(jamCommand);
		colorScheme.add(whiteOnBlack);
		mPrefer.add(whiteOnBlack);
		final JRadioButtonMenuItem blackOnWhite =
			new JRadioButtonMenuItem("White Background", true);
		blackOnWhite.setEnabled(true);
		blackOnWhite.addActionListener(jamCommand);
		colorScheme.add(blackOnWhite);
		mPrefer.add(blackOnWhite);
		mPrefer.addSeparator();
		final JCheckBoxMenuItem verboseVMEReply =
			new JCheckBoxMenuItem("Verbose front end", false);
		verboseVMEReply.setEnabled(true);
		verboseVMEReply.setToolTipText(
			"If selected, the front end will send verbose messages.");
		JamProperties.setProperty(
			JamProperties.FRONTEND_VERBOSE,
			verboseVMEReply.isSelected());
		verboseVMEReply.addItemListener(jamCommand);
		mPrefer.add(verboseVMEReply);
		final JCheckBoxMenuItem debugVME =
			new JCheckBoxMenuItem("Debug front end", false);
		debugVME.setToolTipText(
			"If selected, the front end will send debugging messages.");
		debugVME.setEnabled(true);
		JamProperties.setProperty(
			JamProperties.FRONTEND_DEBUG,
			debugVME.isSelected());
		debugVME.addItemListener(jamCommand);
		mPrefer.add(debugVME);
		fitting = new JMenu("Fitting");
		add(fitting);
		final JMenuItem loadFit = new JMenuItem("Load Fit...");
		loadFit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				loadfit.showLoad();
			}
		});
		fitting.add(loadFit);
		fitting.addSeparator();
		final JMenu helpMenu = new JMenu("Help");
		add(helpMenu);
		final JMenuItem about = new JMenuItem("About...");
		helpMenu.add(about);
		about.setActionCommand("about");
		about.addActionListener(jamCommand);
		final JMenuItem userG = new JMenuItem("User Guide...");
		helpMenu.add(userG);
		userG.setActionCommand("userguide");
		userG.addActionListener(getUserGuideListener());
		final JMenuItem license = new JMenuItem("License...");
		helpMenu.add(license);
		license.setActionCommand("license");
		license.addActionListener(jamCommand);
	}

	/**
	 * Add a fitting routine to the fitting JMenu
	 * give the name you want to add
	 *
	 * @param action representing the fit routine added
	 */
	public void addFit(Action action) {
		fitting.add(new JMenuItem(action));
	}

	void setSortMode(int mode) {
		if (mode == JamMain.ONLINE_DISK || mode == JamMain.ONLINE_NODISK) {
			cstartacq.setEnabled(true); //enable control JMenu items
			cstopacq.setEnabled(true);
			iflushacq.setEnabled(true);
			runacq.setEnabled(true);
			sortacq.setEnabled(false);
			paramacq.setEnabled(true);
			statusacq.setEnabled(true);
			newClear.setEnabled(false);
			open.setEnabled(false);
			save.setEnabled(false);
			reload.setEnabled(true);
			openhdf.setEnabled(false);
			saveHDF.setEnabled(false);
			reloadhdf.setEnabled(true);
		}
		if (mode == JamMain.OFFLINE_DISK) {
			cstartacq.setEnabled(false);
			cstopacq.setEnabled(false);
			iflushacq.setEnabled(false);
			runacq.setEnabled(false);
			sortacq.setEnabled(true);
			paramacq.setEnabled(true);
			statusacq.setEnabled(true);
			open.setEnabled(false);
			save.setEnabled(false);
			reload.setEnabled(true);
			openhdf.setEnabled(false);
			saveHDF.setEnabled(false);
			reloadhdf.setEnabled(true);
			newClear.setEnabled(false);
		}
		if (mode == JamMain.REMOTE) { //remote display
			cstartacq.setEnabled(false);
			cstopacq.setEnabled(false);
			iflushacq.setEnabled(false);
			runacq.setEnabled(false);
			sortacq.setEnabled(false);
			paramacq.setEnabled(false);
			statusacq.setEnabled(false);
			newClear.setEnabled(false);
			open.setEnabled(false);
			reload.setEnabled(false);
			openhdf.setEnabled(false);
			reloadhdf.setEnabled(false);
			impHist.setEnabled(false);
		}
		if (mode == JamMain.FILE) {
			cstartacq.setEnabled(false);
			cstopacq.setEnabled(false);
			iflushacq.setEnabled(false);
			runacq.setEnabled(false);
			sortacq.setEnabled(false);
			paramacq.setEnabled(false);
			statusacq.setEnabled(false);
			newClear.setEnabled(true);
			open.setEnabled(true);
			save.setEnabled(true);
			reload.setEnabled(false);
			openhdf.setEnabled(true);
			saveHDF.setEnabled(true);
			reloadhdf.setEnabled(false);
			impHist.setEnabled(true);
		}
		if (mode == JamMain.NO_SORT) {
			cstartacq.setEnabled(false);
			cstopacq.setEnabled(false);
			iflushacq.setEnabled(false);
			runacq.setEnabled(false);
			sortacq.setEnabled(false);
			paramacq.setEnabled(false);
			statusacq.setEnabled(false);
			newClear.setEnabled(true);
			open.setEnabled(true);
			reload.setEnabled(false);
			openhdf.setEnabled(true);
			reloadhdf.setEnabled(false);
			impHist.setEnabled(true);
		}
	}

	void setRunState(RunState rs) {
		final boolean acqmode = rs.isAcquireMode();
		final boolean acqon = rs.isAcqOn();
		cstartacq.setEnabled(acqmode);
		cstopacq.setEnabled(acqmode);
		iflushacq.setEnabled(acqon);
		cstartacq.setSelected(acqon);
		cstopacq.setSelected(acqmode && (!acqon));
	}

	/**
	 * Return an ActionListener cabable of displaying the User
	 * Guide.
	 * 
	 * @return an ActionListener cabable of displaying the User
	 * Guide
	 */
	private ActionListener getUserGuideListener() {
		final HelpSet hs;
		final String helpsetName = "help/jam.hs";
		try {
			final URL hsURL =
				getClass().getClassLoader().getResource(helpsetName);
			hs = new HelpSet(null, hsURL);
		} catch (Exception ee) {
			final String message = "HelpSet " + helpsetName + " not found";
			showErrorMessage(message, ee);
			return null;
		}
		return new CSH.DisplayHelpFromSource(hs.createHelpBroker());
	}

	private void showErrorMessage(String title, Exception e) {
		JOptionPane.showMessageDialog(
			this,
			e.getMessage(),
			title,
			JOptionPane.ERROR_MESSAGE);
	}

}