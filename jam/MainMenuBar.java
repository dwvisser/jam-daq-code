package jam;
import jam.commands.JamCmdManager;
import jam.data.Histogram;
import jam.data.control.DataControl;
import jam.fit.LoadFit;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.CommandNames;
import jam.global.ComponentPrintable;
import jam.global.JamProperties;
import jam.global.JamStatus;
import jam.global.MessageHandler;
import jam.global.SortMode;
import jam.io.ImpExp;
import jam.io.ImpExpASCII;
import jam.io.ImpExpORNL;
import jam.io.ImpExpSPE;
import jam.io.ImpExpXSYS;
import jam.io.ImportBanGates;
import jam.io.hdf.HDFIO;
import jam.plot.Display;
import jam.plot.PlotGraphicsLayout;
import jam.util.ScalerScan;
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
import java.util.Observable;
import java.util.Observer;

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
public class MainMenuBar extends JMenuBar implements Observer {

	private final JamStatus status=JamStatus.instance();

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
			super("Print\u2026");
		}
		
		private boolean firstTime=true;

		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent ae) {
			if (firstTime){
				console.warningOutln("On some systems, it will be necessary to first "+
				"use 'Page Setup\u2026' for your hardcopy to have correct size and margins.");
				firstTime=false;
			}
			PrinterJob pj = PrinterJob.getPrinterJob();
			ComponentPrintable cp = display.getComponentPrintable();
			pj.setPrintable(cp, mPageFormat);
			if (pj.printDialog()) {
				console.messageOut("Preparing to send histogram '" + 
				JamStatus.instance().getCurrentHistogramName()+"' to printer\u2026",
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
	/* Remove KBS
	class SaveHDF extends AbstractAction {	
		SaveHDF(){
			super("Save (hdf)");
		}
		
		public void actionPerformed(ActionEvent ae){
			final File last=hdfio.lastValidFile();
			if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
			MainMenuBar.this,"Replace the existing file?","Save "+last.getName(),
			JOptionPane.YES_NO_OPTION)){
				hdfio.writeFile(last);				
			}			
		}
	}
	*/
	/* Remove KBS
	class SaveAsHDF extends AbstractAction {	
		SaveAsHDF(){
			super("Save as (hdf)...");
		}
		
		public void actionPerformed(ActionEvent ae){
			int write = hdfio.writeFile();
			if (write==JFileChooser.APPROVE_OPTION){
				setSaveEnabled(true);
			}
		}
	}
	*/
/* Remove KBS
	class OpenHDF extends AbstractAction {	
		OpenHDF(){
			super("Open(hdf)...");
		}
		
		public void actionPerformed(ActionEvent ae){
			if (hdfio.readFile(FileOpenMode.OPEN)) { //true if successful
				status.setSortMode(hdfio.getFileOpen());
				DataControl.setupAll();
				jamCommand.dataChanged();
				jamMain.repaint();
				setSaveEnabled(true);
			}
		}
	}
*/
/* Remove KBS
	class ReloadHDF extends AbstractAction {	
		private final ScalerControl scalerControl;
		
		ReloadHDF(){
			super("Reload(hdf)...");
			scalerControl=jamCommand.getScalerControl();
		}
		
		public void actionPerformed(ActionEvent ae){
			if (hdfio.readFile(FileOpenMode.RELOAD)) { //true if successful
				scalerControl.displayScalers();
			}
		}
	}
	*/
	/* Remove KBS
	class AddHDF extends AbstractAction {	
		AddHDF(){
			super("Add counts(hdf)...");
			this.setEnabled(false);
		}
		
		public void actionPerformed(ActionEvent ae){
			hdfio.readFile(FileOpenMode.ADD);
		}
	}
	*/
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
			super("Page Setup\u2026");
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
				 	status.setSortMode(impexp.getLastFile());
					DataControl.setupAll();
					jamCommand.dataChanged();
				}
			} catch (Exception e){
				console.errorOutln(e.getMessage());
			}
		}
	}

	/*class ExportAction extends AbstractAction {
		
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
	}*/
	
	class SaveGatesAction extends AbstractAction {

		SaveGatesAction(){
			super("Save gates, scalers & parameters as HDF\u2026");
		}
		
		public void actionPerformed(ActionEvent ae){
			hdfio.writeFile(false,true,true,true);
		}
	}


	static final String NO_FILL_MENU_TEXT = "Disable Gate Fill";

	final private JMenu fitting;
	final private JMenuItem impHist,
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
	
	final private JMenuItem histogramNew = new JMenuItem("New\u2026");
	final private JMenuItem zeroHistogram = new JMenuItem("Zero\u2026");
	final private JMenu calHist = new JMenu("Calibrate");
	final private JMenuItem projectHistogram = new JMenuItem("Projections\u2026");
	final private JMenuItem manipHistogram = new JMenuItem("Combine\u2026");
	final private JMenuItem gainShift = new JMenuItem("Gain Shift\u2026");

	//Remove KBS final private OpenHDF openhdf;
	//Remove KBS final private ReloadHDF reloadhdf;
	//Remove KBS
	//final private AddHDF addHDF=new AddHDF();
	final private HDFIO hdfio;

	private PageFormat mPageFormat=PrinterJob.getPrinterJob().defaultPage();
	final private JamCmdManager commands;

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
		//KBS commands=jamCommand.getCmdManager();
		commands=JamCmdManager.getInstance();
		hdfio=jamCommand.getHDFIO();
		Broadcaster.getSingletonInstance().addObserver(this);
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
		final JMenuItem newClear = new JMenuItem(commands.getAction(CommandNames.NEW));
		impHist = new JMenu("Import");
		runacq = new JMenuItem("Run\u2026");
		sortacq = new JMenuItem("Sort\u2026");
		paramacq = new JMenuItem("Parameters\u2026");
		statusacq = new JMenuItem("Buffer Count\u2026");
		newClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ctrl_mask));		
		file.add(newClear);
		
	
		final JMenuItem openhdf = new JMenuItem(commands.getAction(CommandNames.OPEN_HDF));
		openhdf.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ctrl_mask));
		file.add(openhdf);
		
		final JMenuItem reloadhdf = new JMenuItem(commands.getAction(CommandNames.RELOAD_HDF));
		reloadhdf.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ctrl_mask | Event.SHIFT_MASK));		
		file.add(reloadhdf);

		final JMenuItem addhdf=new JMenuItem(commands.getAction(
		CommandNames.ADD_HDF));
		file.add(addhdf);
		
		final JMenuItem saveHDF  = new JMenuItem(commands.getAction(CommandNames.SAVE_HDF));
		saveHDF.setEnabled(false);	//KBS should not have to set
		saveHDF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl_mask|KeyEvent.SHIFT_MASK));
		file.add(saveHDF);		

		final JMenuItem saveAsHDF  = new JMenuItem(commands.getAction(CommandNames.SAVE_AS_HDF));
		saveAsHDF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl_mask));
		file.add(saveAsHDF);		

		 
		final JMenuItem special=new JMenu("Special");
		final JMenuItem openSelectdHist =new JMenuItem("Open Selected Histogram\u2026");
		openSelectdHist.setActionCommand("openselectedhist");		
		openSelectdHist.addActionListener(jamCommand);
		special.add(openSelectdHist);
		final JMenuItem saveGates=new JMenuItem(new SaveGatesAction());
		special.add(saveGates);
		file.add(special);
		file.addSeparator();
		final JMenuItem utilities=new JMenu("Utilities");
		file.add(utilities);
		final YaleCAENgetScalers ycgs=new YaleCAENgetScalers(jamMain,console);
		utilities.add(new JMenuItem(ycgs.getAction()));
		final ScalerScan ss=new ScalerScan(jamMain,console);
		utilities.add(new JMenuItem(ss.getAction()));
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
		final JMenuItem saveascii = new JMenuItem(commands.getAction(CommandNames.EXPORT_TEXT));
		expHist.add(saveascii);
		final JMenuItem savespe = new JMenuItem(commands.getAction(CommandNames.EXPORT_SPE));
		expHist.add(savespe);
		final JMenuItem saveornl = new JMenuItem(commands.getAction(CommandNames.EXPORT_DAMM));
		expHist.add(saveornl);
		final JMenuItem batchexport = new JMenuItem("Batch Export\u2026");
		batchexport.setActionCommand("batchexport");
		batchexport.addActionListener(jamCommand);
		expHist.add(batchexport);
		file.addSeparator();
		file.add(new FilePrintAction()).setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_P, ctrl_mask));
		file.add(new FilePageSetupAction()).setAccelerator(
		KeyStroke.getKeyStroke(KeyEvent.VK_P, 
		ctrl_mask | Event.SHIFT_MASK));
		file.addSeparator();
		final JMenuItem exit = new JMenuItem("Exit\u2026");
		/*
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				jm.showExitDialog();
			}
		});
		*/
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,ctrl_mask));
		file.add(exit);
		exit.setActionCommand("exit");
		exit.addActionListener(jamCommand);
		
		final JMenu setup = new JMenu("Setup");
		add(setup);
		final JMenuItem setupOnline = new JMenuItem("Online sorting\u2026");
		setupOnline.setActionCommand("online");
		setupOnline.addActionListener(jamCommand);
		setup.add(setupOnline);
		setup.addSeparator();
		final JMenuItem setupOffline = new JMenuItem("Offline sorting\u2026");
		setupOffline.setActionCommand("offline");
		setupOffline.addActionListener(jamCommand);
		setup.add(setupOffline);
		setup.addSeparator();
		final JMenuItem setupRemote = new JMenuItem("Remote Hookup\u2026");
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
		histogramNew.setActionCommand("shownewhist");
		histogramNew.addActionListener(jamCommand);
		histogram.add(histogramNew);
		zeroHistogram.setActionCommand("zerohist");
		zeroHistogram.addActionListener(jamCommand);
		histogram.add(zeroHistogram);
		histogram.add(calHist);
		final JMenuItem calibFit = new JMenuItem("Fit\u2026");
		calibFit.setActionCommand("calfitlin");
		calibFit.addActionListener(jamCommand);
		calHist.add(calibFit);
		final JMenuItem calibFunc = new JMenuItem("Enter Coefficients\u2026");
		calibFunc.setActionCommand("caldisp");
		calibFunc.addActionListener(jamCommand);
		calHist.add(calibFunc);
		projectHistogram.setActionCommand("project");
		projectHistogram.addActionListener(jamCommand);
		histogram.add(projectHistogram);
		manipHistogram.setActionCommand("manipulate");
		manipHistogram.addActionListener(jamCommand);
		histogram.add(manipHistogram);
		gainShift.setActionCommand("gainshift");
		gainShift.addActionListener(jamCommand);
		histogram.add(gainShift);
		final JMenu gate = new JMenu("Gate");
		add(gate);
		final JMenuItem gateNew = new JMenuItem("New Gate\u2026");
		gateNew.setActionCommand("gatenew");
		gateNew.addActionListener(jamCommand);
		gate.add(gateNew);
		final JMenuItem gateAdd = new JMenuItem("Add Gate\u2026");
		gateAdd.setActionCommand("gateadd");
		gateAdd.addActionListener(jamCommand);
		gate.add(gateAdd);
		final JMenuItem gateSet = new JMenuItem("Set Gate\u2026");
		gateSet.setActionCommand("gateset");
		gateSet.addActionListener(jamCommand);
		gate.add(gateSet);
		final JMenu scalers = new JMenu("Scalers");
		add(scalers);
		final JMenuItem showScalers = new JMenuItem("Display Scalers\u2026");
		showScalers.setActionCommand("displayscalers");
		showScalers.addActionListener(jamCommand);
		scalers.add(showScalers);
		final JMenuItem clearScalers = new JMenuItem("Zero Scalers\u2026");
		scalers.add(clearScalers);
		clearScalers.setActionCommand("showzeroscalers");
		clearScalers.addActionListener(jamCommand);
		scalers.addSeparator();
		final JMenuItem showMonitors = new JMenuItem("Display Monitors\u2026");
		showMonitors.setActionCommand("displaymonitors");
		showMonitors.addActionListener(jamCommand);
		scalers.add(showMonitors);
		final JMenuItem configMonitors = new JMenuItem("Configure Monitors\u2026");
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
			new JMenuItem("Peak Find Properties\u2026");
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
		final JMenuItem loadFit = new JMenuItem("Load Fit\u2026");
		loadFit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				loadfit.showLoad();
			}
		});
		fitting.add(loadFit);
		fitting.addSeparator();
		final JMenu helpMenu = new JMenu("Help");
		add(helpMenu);
		final JMenuItem about = new JMenuItem("About\u2026");
		helpMenu.add(about);
		about.setActionCommand("about");
		about.addActionListener(jamCommand);
		final JMenuItem userG = new JMenuItem("User Guide\u2026");
		helpMenu.add(userG);
		userG.setActionCommand("userguide");
		userG.addActionListener(getUserGuideListener());
		final JMenuItem license = new JMenuItem("License\u2026");
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

	private void sortModeChanged() {
		final SortMode mode=status.getSortMode();
		final boolean online = mode == SortMode.ONLINE_DISK || 
		mode == SortMode.ONLINE_NO_DISK;
		final boolean offline = mode == SortMode.OFFLINE;
		final boolean sorting = online || offline;
		final boolean file = mode==SortMode.FILE || mode==SortMode.NO_SORT;
		cstartacq.setEnabled(online);
		cstopacq.setEnabled(online);
		iflushacq.setEnabled(online);
		runacq.setEnabled(online);
		sortacq.setEnabled(offline);
		paramacq.setEnabled(sorting);
		statusacq.setEnabled(sorting);
		impHist.setEnabled(file);
	}

	void setRunState(RunState rs) {
		final boolean acqmode = rs.isAcquireMode();
		final boolean acqon = rs.isAcqOn();
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
	
	void adjustHistogramItems(Histogram h){
		final boolean oneDops;
		if (h==null){
			oneDops=false;
		} else if (h.getDimensionality()==1){
			oneDops=true;
		} else {
			oneDops=false;
		}
		zeroHistogram.setEnabled(h != null);
		calHist.setEnabled(oneDops);
	}

	public void update(Observable observe, Object obj){
		final BroadcastEvent be=(BroadcastEvent)obj;
		final int command=be.getCommand();
		if (command==BroadcastEvent.SORT_MODE_CHANGED){
			sortModeChanged();
		}
	}
}