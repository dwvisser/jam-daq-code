package jam.plot;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import jam.global.*;
import jam.data.*;

/**
 * This class is a display routine for plots.
 * It is implemented by <code>Display</code>.
 * <p>
 *
 * @version  0.5 April 98
 * @author   Ken Swartz
 * @see         java.awt.Graphics
 * @since       JDK1.1
 */
public class Display extends JPanel implements Displayer, CommandListener,
Observer {

    /** for preference ignore channel zero */
    //public static final int AUTO_IGNORE_ZERO=0;
    /** for preference ignore full scale channel  */
    //public static final int AUTO_IGNORE_FULL=1;
    /** for preference white background  */
    //public static final int WHITE_BACKGROUND=5;
    /** for preference black background  */
    //public static final int BLACK_BACKGROUND=6;
    
    //public static final int AUTO_PEAK_FIND=11;
    
    static public class Preferences{
    	private int type;
    	
    	static public Preferences AUTO_IGNORE_ZERO= new Preferences(0);
		static public Preferences AUTO_IGNORE_FULL= new Preferences(1);
		static public Preferences WHITE_BACKGROUND= new Preferences(2);
		static public Preferences BLACK_BACKGROUND= new Preferences(3);
		static public Preferences AUTO_PEAK_FIND= new Preferences(4);
    	static public Preferences CONTINUOUS_2D_LOG= new Preferences(5);
    	private Preferences(int type){
    		this.type=type;
    	}
    }

    private MessageHandler msgHandler;          //output for messages
    private Broadcaster broadcaster;          //broadcaster if needed
    private Action action;      //handles display events

    private Displayable currentData;
    private Displayable overlayData;
    private Histogram currentHist;
    private Histogram overlayHist;

    private Plot currentPlot;
    boolean overlayState=false;

//    private CalibrationFunction calibrationFunction;

    // plot panels
    public JPanel plotswap;
    public CardLayout plotswapLayout;
    private Plot1d plot1d;
    private Plot2d plot2d;

    /**
     * Constructor for Applet, no boadcaster
     *
     * @param   msgHandler  the class to call if the plot button is pushed.
     */
    public Display(Broadcaster broadcaster, MessageHandler msgHandler){
        this(msgHandler);
        this.broadcaster=broadcaster;
        //add broadcaster to action
        action.setBroadcaster(broadcaster);
    }

    /**
     * Constructor called by all constructors
     *
     * @param   msgHandler  the class to call to print out messages
     */
    public Display(MessageHandler msgHandler){
        try{
            this.msgHandler=msgHandler;        //where to send output messages
            //display event handler
            action=new Action(this, msgHandler);
            setSize(500,500);
            this.setLayout(new BorderLayout());
            //setup up middle panel containing plots panel to holds 1d and 2d plots and swaps them
            plotswap=new JPanel();
            plotswapLayout=new CardLayout();
            plotswap.setLayout(plotswapLayout);
            //  panel 1d plot and its scroll bars
            plot1d=new Plot1d();
            plot1d.addPlotMouseListener(action);
            Scroller scroller1d=new Scroller(plot1d);
            plotswap.add("OneD",scroller1d);
            //  panel 2d plot and its scroll bars
            plot2d=new Plot2d();
            plot2d.addPlotMouseListener(action);
            Scroller scroller2d=new Scroller(plot2d);
            plotswap.add("TwoD",scroller2d);
            //default setup
            currentPlot=plot1d;
            action.setPlot(currentPlot);
            //default preferences
            plot1d.setIgnoreChZero(true);
            plot1d.setIgnoreChFull(true);
            plot2d.setIgnoreChZero(true);
            plot2d.setIgnoreChFull(true);
            this.add(plotswap,BorderLayout.CENTER);
        } catch (RuntimeException re) {
            re.printStackTrace();
        }
    }

    /**
     * Set the histogram to display
     */
    public void displayHistogram(Histogram hist){
        currentHist=hist;
        Limits lim=Limits.getLimits(hist);
        if (hist!=null) {
            if(lim==null){//create a new Limits object for this histogram
                newHistogram();
            }
            overlayState=false;
            showPlot(currentHist);//changes local currentPlot            
        } else {//we have a null histogram, but display anyway
            showPlot(currentHist);
        }
        bgoto.setEnabled(currentHist.getDimensionality()==1);
    }

    /**
     * Overlay a histogram
     * only works for 1 d
     */
    public void overlayHistogram(Histogram hist){
        int type;  //type of histogram 1 or 2 d

        if (hist!=null) {
            if(Limits.getLimits(hist)==null){
                newHistogram();
            }
            type=hist.getType();
            // test to make sure none of the histograms are 2d
            if (currentPlot instanceof Plot1d){
                if (hist.getDimensionality()==1){
                    overlayHist=hist;
                    overlayState=true;
                    plot1d.overlayHistogram(overlayHist);
                } else {
                    msgHandler.errorOutln("Cannot overlay a 2D histogram.");
                }
            } else {
                msgHandler.errorOutln("Cannot overlay on top of a 2D histogram.");
            }
        } else {
            msgHandler.errorOutln("Cannot overlay null histogram");
        }
    }

    /**
     * Display a <code>Displayable</code> object.
     */
    public void displayData(Displayable data){
        currentData=data;
        if (data!=null) {
            if(Limits.getLimits(data)==null){
                newHistogram();
            }
            overlayState=false;
        } 
    }

    /**
     * Overlay a displayble data
     * only works for 1 d
     */
    public void overlayData(Displayable data){
        if (data!=null) {
            if(Limits.getLimits(data)==null){
                newHistogram();
            }
            // test to make sure none of the histograms are 2d
            if (currentPlot instanceof Plot1d){
                if (data.getType()==Displayable.ONE_DIMENSION){
                    overlayData=data;
                    overlayState=true;
                } else {
                    msgHandler.errorOutln("Cannot overlay a 2D histogram.");
                }
            } else {
                msgHandler.errorOutln("Cannot overlay on top of a 2D histogram.");
            }
        } else {
            msgHandler.errorOutln("Error tried to overlay null.");
        }
    }

    /**
     * Get the displayed Histogram.
     */
    public Histogram getHistogram(){
        return currentHist;
    }

    /**
     * Get the displayed data.
     */
    public Displayable getData(){
        return currentData;
    }

    /**
     * A new histogram not previously displayed is being displayed, so
     * create limits for histogram and initialize
     * the limits.
     */
    private void newHistogram(){
        Limits plotLimits;
        //System.err.println("newHistogram(): hist="+currentHist.getName());
        if (currentHist!=null){
            try {
                if ((currentHist.getType()==Histogram.ONE_DIM_INT)||
                (currentHist.getType()==Histogram.ONE_DIM_DOUBLE)){
                    currentPlot=plot1d;
                } else {
                    currentPlot=plot2d;
                }
                new Limits(currentHist, currentPlot.getIgnoreChZero(),currentPlot.getIgnoreChFull() );
                plotLimits=Limits.getLimits(currentHist);
            } catch ( IndexOutOfBoundsException e ) {
                //FIXME
                System.err.println(" Error: creating limits for new histogram [plot.Plot]" +currentHist.getName() );
            }
        }
    }

    /**
     * Display a gate.
     * 
     * @param gate The gate to display.
     */
    public void displayGate(Gate gate){
        try {
            currentPlot.displayGate(gate);
        } catch (DataException de) {
            msgHandler.errorOutln("Display.displayGate() DataException: "+de.getMessage());
        }
    }

    /**
     * Prints a histogram.
     *
     * @param gpage
     */
    public void printHistogram( Graphics gpage,
    Dimension pageSize,
    int pagedpi) {
        int runNumber=RunInfo.runNumber;
        if (gpage!=null) {
            currentPlot.print(gpage, pageSize, pagedpi, runNumber,
            JamStatus.instance().getDate());
        } else {
            msgHandler.errorOutln("Can't print page, null graphics [Display]");
        }
    }

    /**
     * Do a command sent in as a message.
     */
    public void commandPerform(String commandIn, int [] parameters){
        action.commandPerform(commandIn, parameters);
    }

    /**
     * Implementation of Observable interface to
     * receive broadcast events.
     */
    public void update(Observable observable, Object o){
        BroadcastEvent be=(BroadcastEvent)o;
        int command=be.getCommand();
        if(command==BroadcastEvent.REFRESH){
            displayHistogram(currentHist);
        } else if (command==BroadcastEvent.GATE_SET_ON){
            currentPlot.displaySetGate(Plot.GATE_NEW, null, null);
            Action.settingGate=true;
        } else if (command==BroadcastEvent.GATE_SET_OFF){
            currentPlot.displaySetGate(Plot.GATE_CANCEL, null, null);
            Action.settingGate=false;
        } else if (command==BroadcastEvent.GATE_SET_SAVE){
            currentPlot.displaySetGate(Plot.GATE_SAVE, null, null);
            Action.settingGate=false;
        } else if (command==BroadcastEvent.GATE_SET_ADD){
            currentPlot.displaySetGate(Plot.GATE_CONTINUE, (Point)be.getContent(), null);
        } else if (command==BroadcastEvent.GATE_SET_REMOVE){
            currentPlot.displaySetGate(Plot.GATE_REMOVE, null, null);
        }
    }

    /**
     *Set the display preference,
     *
     * <ul>
     * <li>AUTO_IGNORE_0  ignore channel zero on auto scale
     * <li>AUTO_IGNORE_MAX  ignore max channel on auto scale
     * </ul>
     * @param preference The preference to set see
     * @param state   The state of the preference if applicable
     */
    public void setPreference(Preferences preference, boolean state) {
        if (preference==Preferences.AUTO_IGNORE_ZERO){
            plot1d.setIgnoreChZero(state);
            plot2d.setIgnoreChZero(state);
        } else if (preference==Preferences.AUTO_IGNORE_FULL){
            plot1d.setIgnoreChFull(state);
            plot2d.setIgnoreChFull(state);
        } else if (preference==Preferences.BLACK_BACKGROUND) {
            plot1d.setColorMode(PlotColorMap.WHITE_ON_BLACK);
            plot2d.setColorMode(PlotColorMap.WHITE_ON_BLACK);
            displayHistogram(currentHist);
        } else if (preference==Preferences.WHITE_BACKGROUND){
            plot1d.setColorMode(PlotColorMap.BLACK_ON_WHITE);
            plot2d.setColorMode(PlotColorMap.BLACK_ON_WHITE);
            displayHistogram(currentHist);
        } else if (preference==Preferences.AUTO_PEAK_FIND){
        	plot1d.setPeakFind(state);
        	displayHistogram(currentHist);
        } else if (preference==Preferences.CONTINUOUS_2D_LOG){
        	displayHistogram(currentHist);
        }
    }
    
    public void setPeakFindProperties(double width, double sensitivity, boolean cal){
    	plot1d.setWidth(width);
    	plot1d.setSensitivity(sensitivity);
    	plot1d.setPeakFindDisplayCal(cal);
    	displayHistogram(currentHist);
    }

    /**
     * Draw a fit overlayed on the current histogram Plot.
     */
    /*public void displayFit(double [] counts, int lowerLimit, int upperLimit){
        plot1d.displayFit(counts, lowerLimit, upperLimit);
    }

    public void displayFit(double [] counts, double [] residual, int lowerLimit, int upperLimit){
        plot1d.displayFit(counts, residual, lowerLimit, upperLimit);
    }*/
    
    public void displayFit(
	double[][] signals,
	double[] background,
	double[] residuals,
	int ll){
    	plot1d.displayFit(signals, background,residuals, ll);
    }

    /**
     * The current Plot either Plot1d or Plot2d
     *
     * @return  <code>void</code>
     * @since Version 0.5
     */
    public Plot getPlot(){
        return currentPlot;
    }

    /**
     * Adds a plot mouse listner, plot mouse is a mouse which is calibrated
     * to the current display.
     *
     * @param   listener the class to notify when the mouse in pressed in the plot
     * @see #removePlotMouseListener
     */
    public void addPlotMouseListener(PlotMouseListener listener){
        currentPlot.addPlotMouseListener(listener);
    }
    
    /**
     * Removes a plot mouse.
     *
     * @param   listener the class to notify when the mouse in pressed in the plot
     * @see #addPlotMouseListener
     */
    public void removePlotMouseListener(PlotMouseListener listener){
        currentPlot.removePlotMouseListener(listener);
    }
    
    /**
     * Shows (display) a histogram.
     *
     * @param   hist  the histogram to display
     */
    private void showPlot(Histogram hist){
        boolean doRepaint;
        currentHist=hist;
        doRepaint=false;
        //cancel all previous stuff
        if (currentPlot!=null) {
            currentPlot.displaySetGate(Plot.GATE_CANCEL, null, null);
            Action.settingGate=false;
        }
        if (hist!=null){
            if ((currentHist.getType()==Histogram.ONE_DIM_INT)||
            (currentHist.getType()==Histogram.ONE_DIM_DOUBLE)){
                //show plot repaint if last plot was also 1d
                plotswapLayout.show(plotswap, "OneD");
                if (currentPlot==plot1d){
                    doRepaint=true;
                }
                currentPlot=plot1d;
            } else if ((currentHist.getType()==Histogram.TWO_DIM_INT)||
            	(currentHist.getType()==Histogram.TWO_DIM_DOUBLE)) {
                //show plot repaint if last plot was also 2d
                plotswapLayout.show(plotswap, "TwoD");
                if (currentPlot==plot2d){
                    doRepaint=true;
                }
                currentPlot=plot2d;
            }
        } else {//null histogram lets be in plot1d
            plotswapLayout.show(plotswap, "OneD");
            currentPlot=plot1d;
            doRepaint=true;
        }
        //plot current histogram
        currentPlot.displayHistogram(currentHist);
        action.setPlot(currentPlot);
        //only repaint if we did not do a card swap
        if(doRepaint){
            currentPlot.repaint();
        }
    }

	JButton bgoto;
    /**
     * Adds the tool bar the left hand side of the plot.
     *
     * @since Version 0.5
     */
    public void addToolbarAction() {
    	ClassLoader cl=this.getClass().getClassLoader();
    	
        JPanel ptoolbar=new JPanel();
        this.add(ptoolbar,BorderLayout.WEST);
        ptoolbar.setForeground(Color.black);
        GridBagLayout gb =new GridBagLayout();
        GridBagConstraints gbc =new GridBagConstraints();
        gbc.ipady=5;
        ptoolbar.setLayout(gb);
        Insets insetsToolbar=new Insets(5,5,5,5);
        gbc.insets=insetsToolbar;
        try{
            JLabel ltoolbar=new JLabel("View",SwingConstants.CENTER);
            addComponent(ptoolbar, ltoolbar, 0, GridBagConstraints.RELATIVE, 1, 1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH);
            JButton bupdate=new JButton("Update");
            bupdate.setToolTipText("Click to update display with most current data.");
            bupdate.setActionCommand("update");        //refresh because update is a component method
            bupdate.addActionListener(action);
            addComponent(ptoolbar, bupdate, 0, GridBagConstraints.RELATIVE, 1, 1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH);
            JPanel zoomPanel=new JPanel(new GridLayout(2,2));
            Icon i_expand = new ImageIcon(cl.getResource("toolbarButtonGraphics/general/Zoom24.gif"));
            JButton bexpand=new JButton(i_expand);
            bexpand.setToolTipText("EXpand. Set new display limits.");
            bexpand.setActionCommand("expand");
            bexpand.addActionListener(action);
            zoomPanel.add(bexpand);
            JButton bfull=new JButton("FUll");
            bfull.setActionCommand("full");
            bfull.setToolTipText("Click here to set display limits to full histogram extents.");
            bfull.addActionListener(action);
            zoomPanel.add(bfull);
            Icon i_zoomin=new ImageIcon(cl.getResource("toolbarButtonGraphics/general/ZoomIn24.gif"));
            JButton bzoomin=new JButton(i_zoomin);
            bzoomin.setToolTipText("ZoomIn the display limits.");
            bzoomin.setActionCommand("zoomin");
            bzoomin.addActionListener(action);
            zoomPanel.add(bzoomin);
            Icon i_zoomout=new ImageIcon(cl.getResource("toolbarButtonGraphics/general/ZoomOut24.gif"));
            JButton bzoomout=new JButton(i_zoomout);
            bzoomout.setToolTipText("ZoomOut the display limits.");
            bzoomout.setActionCommand("zoomout");
            bzoomout.addActionListener(action);
            zoomPanel.add(bzoomout);
            addComponent(ptoolbar, zoomPanel, 0, GridBagConstraints.RELATIVE, 1, 1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH);
            bgoto = new JButton("Goto");
	        bgoto.setActionCommand("goto");
	        bgoto.setToolTipText("Click here to zoom in on desired region");
	        bgoto.addActionListener(action);
	        addComponent(ptoolbar, bgoto, 0, GridBagConstraints.RELATIVE ,1,1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH);
            ltoolbar=new JLabel ("Scale",SwingConstants.CENTER);
            addComponent(ptoolbar, ltoolbar, 0, GridBagConstraints.RELATIVE, 1, 1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH);
            JButton blinear=new JButton("LInear ");
            blinear.setToolTipText("Click here to set counts scale to linear.");
            blinear.setActionCommand("linear");
            blinear.addActionListener(action);
            addComponent(ptoolbar, blinear, 0, GridBagConstraints.RELATIVE, 1, 1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH);
            JButton blog=new JButton("LOg ");
            blog.setToolTipText("Click here to set counts scale to log.");
            blog.setActionCommand("log");
            blog.addActionListener(action);
            addComponent(ptoolbar, blog, 0, GridBagConstraints.RELATIVE, 1, 1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH);
            JButton brange=new JButton("Range");
            brange.setToolTipText("Click here then on display to set limits of counts scale.");
            brange.setActionCommand("range");
            brange.addActionListener(action);
            addComponent(ptoolbar, brange, 0, GridBagConstraints.RELATIVE ,1,1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH);
            JButton bauto=new JButton("Auto");
            bauto.setToolTipText("Click here to automatically set the counts scale.");
            bauto.setActionCommand("auto");
            bauto.addActionListener(action);
            addComponent(ptoolbar, bauto, 0, GridBagConstraints.RELATIVE ,1,1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH);
            ltoolbar=new JLabel ("Inquire",SwingConstants.CENTER);
            addComponent(ptoolbar, ltoolbar, 0, GridBagConstraints.RELATIVE, 1, 1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH);
            JButton barea=new JButton("ARea");
            barea.setToolTipText("Click here then on display to get area and summary stats of a region.");
            barea.setActionCommand("area");
            barea.addActionListener(action);
            addComponent(ptoolbar, barea, 0, GridBagConstraints.RELATIVE ,1,1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH);
            JButton bnetarea=new JButton("NETarea");
            bnetarea.setToolTipText("Click here then on display to get net area and summary stats of a region.");
            bnetarea.setActionCommand("netarea");
            bnetarea.addActionListener(action);
            addComponent(ptoolbar, bnetarea, 0, GridBagConstraints.RELATIVE ,1,1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.NORTH);
            JButton bcancel=new JButton("Cancel");
            bcancel.setActionCommand("cancel");
            bcancel.setToolTipText("Click here to cancel a toolbar action in progress.");
            bcancel.addActionListener(action);
            addComponent(ptoolbar, bcancel, 0, GridBagConstraints.RELATIVE ,1,1,
            GridBagConstraints.HORIZONTAL,GridBagConstraints.SOUTH);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Helper method for GridBagLayout and GridBagConstaints
     *
     * @return  <code>void</code>
     * @since Version 0.5
     */
    private static void addComponent(Container container, Component component,
    int gridx, int gridy, int gridwidth, int gridheight, int fill,
    int anchor) throws AWTException {
        LayoutManager lm = container.getLayout();
        if (!(lm instanceof GridBagLayout)) {
            throw new AWTException("Invaid layout"+lm);
        } else {
            GridBagConstraints gbc=new GridBagConstraints ();
            gbc.ipady=5;
            gbc.gridx=gridx;
            gbc.gridy=gridy;
            gbc.gridwidth=gridwidth;
            gbc.gridheight=gridheight;
            gbc.fill=fill;
            gbc.anchor=anchor;
            ((GridBagLayout)lm).setConstraints(component, gbc);
            container.add (component);
        }
    }
}
