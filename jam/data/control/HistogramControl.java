/*
 */
package jam.data.control;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import jam.global.*;
import jam.data.*;
import javax.swing.*;
import javax.swing.border.*;
/**
 * Class to control the histograms
 * Allows one to zero the histograms
 * and create new histograms
 *
 * @author Ken Swartz
 * @version 0.5
 */
public class HistogramControl extends DataControl implements ActionListener {

    private Frame frame;
    private Broadcaster broadcaster;
    private MessageHandler msghdlr;

    private Dialog dialogCalib;
    private JDialog dialogZero, dialogNew;
    Histogram currentHistogram;

    //new Histogram
    private JTextField textName;
    private JTextField textTitle;
    private JComboBox comboSize;
    private JCheckBox coneInt,coneDbl,ctwoInt,ctwoDbl;

    private String histogramName,histogramTitle;
    private int histogramType,histogramSize;
	private final String[] DEFAULT_SIZES= {
										"64",
							        	"128",
		         						"256",
		         						"512",
		         						"1024",
		         						"2048",
		         						"4096",
		         						"8192",
									};

    /**
     * Constructor
     */
    public HistogramControl(Frame frame, Broadcaster broadcaster, MessageHandler msghdlr){
        super();
        this.frame=frame;
        
        this.broadcaster=broadcaster;
        this.msghdlr=msghdlr;
        //zero histogram dialog box
        dialogZero=new JDialog(frame,"Zero Histograms",false);
        Container dzc = dialogZero.getContentPane();
        dialogZero.setResizable(false);
        dzc.setLayout(new FlowLayout(FlowLayout.CENTER));
        JPanel pButton = new JPanel(new GridLayout(1,0,5,5));
        dialogZero.setLocation(20,50);
        JButton one =new JButton("Displayed");
        one.setActionCommand("onezero");
        one.addActionListener(this);
        pButton.add(one);
        JButton all =new JButton("   All   ");
        all.setActionCommand("allzero");
        all.addActionListener(this);
        pButton.add(all);
        JButton cancel=new JButton(" Cancel ");
        cancel.setActionCommand("cancelzero");
        cancel.addActionListener(this);
        pButton.add(cancel);
        dzc.add(pButton);
        dialogZero.pack();

        //dialog box New Histogram
        dialogNew =new JDialog (frame,"New Histogram ",false);
        //dialogNew.setForeground(Color.black);
        //dialogNew.setBackground(Color.lightGray);
        dialogNew.setResizable(false);
        dialogNew.setLocation(30,30);
        Container cdialogNew=dialogNew.getContentPane();
        cdialogNew.setLayout(new BorderLayout(10,10));

		//Labels on the left
        JPanel pLabels= new JPanel(new GridLayout(0,1,5,5));
        pLabels.setBorder(new EmptyBorder(10,10,0,0));
        cdialogNew.add(pLabels, BorderLayout.WEST);
        JLabel ln=new JLabel("Name",JLabel.RIGHT);
        pLabels.add(ln);
        JLabel lti=new JLabel("Title", JLabel.RIGHT);
        pLabels.add(lti);
        JLabel lt=new JLabel("Type", JLabel.RIGHT);
        pLabels.add(lt);
        JLabel ls=new JLabel("Size", JLabel.RIGHT);
        pLabels.add(ls);

		//Entires panel
        final JPanel pEntires= new JPanel(new GridLayout(0,1,5,5));
        pEntires.setBorder(new EmptyBorder(10,0,0,10));
        cdialogNew.add(pEntires, BorderLayout.CENTER);

		final JPanel pName = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		pEntires.add(pName);
        textName =new JTextField(" ");
        textName.setColumns(15);
        textName.setForeground(Color.black);
        textName.setBackground(Color.white);
        pName.add(textName);

		final JPanel pTitle = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		pEntires.add(pTitle);
        textTitle =new JTextField(" ");
        textTitle.setColumns(30);
        textTitle.setForeground(Color.black);
        textTitle.setBackground(Color.white);
        pTitle.add(textTitle);

        Panel pradio = new Panel(new FlowLayout(FlowLayout.LEFT,0,0));
        ButtonGroup cbg = new ButtonGroup();
        coneInt = new JCheckBox("1-D int",true);
        coneDbl = new JCheckBox("1-D double",false);
        ctwoInt = new JCheckBox("2-D int",false);
        ctwoDbl = new JCheckBox("2-D double",false);
        cbg.add(coneInt);
        cbg.add(coneDbl);
        cbg.add(ctwoInt);
        cbg.add(ctwoDbl);
        pradio.add(coneInt);
        pradio.add(coneDbl);
        pradio.add(ctwoInt);
        pradio.add(ctwoDbl);
        pEntires.add(pradio);

		final JPanel pSize = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		pEntires.add(pSize);
		comboSize = new JComboBox(DEFAULT_SIZES);
		comboSize.setEditable(true);
		pSize.add(comboSize);

        // panel for buttons
        JPanel pbOuter= new JPanel();
        pbOuter.setLayout(new FlowLayout(FlowLayout.CENTER));
        cdialogNew.add(pbOuter, BorderLayout.SOUTH);

        JPanel pb = new JPanel(new GridLayout(1,0,5,5));
        pbOuter.add(pb);

        JButton bok  =   new JButton("OK");
        bok.setActionCommand("oknew");
        bok.addActionListener(this);
        pb.add(bok);

        JButton bapply = new JButton("Apply");
        bapply.setActionCommand("applynew");
        bapply.addActionListener(this);
        pb.add(bapply);

        JButton bcancel =new JButton("Cancel");
        bcancel.setActionCommand("cancelnew");
        bcancel.addActionListener(this);
        pb.add(bcancel);

        dialogNew.pack();
        dialogZero.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                dialogZero.dispose();
            }
        });
        dialogNew.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                dialogNew.dispose();
            }
        });
    }

    /**
     * Does nothing. It is here to match other contollers.
     */
    public void setup(){
    }

    /**
     * Show zero dialog box
     */
    public void showZero(){
        dialogZero.show();
    }

    /**
     * Show new histogram dialog box
     */
    public void showNew(){
        dialogNew.show();
    }

    /**
     * Show histogram calibration dialog box
     */
    public void showCalib(){
        dialogCalib.show();
    }
    /**
     * Receive actions from Dialog Boxes
     *
     */
    public void actionPerformed(ActionEvent ae){
        String command=ae.getActionCommand();
        currentHistogram=Histogram.getHistogram(JamStatus.instance().getCurrentHistogramName());
        try {
            /* commands for zero histogram */
            if (command=="onezero") {
                currentHistogram.setZero();
                broadcaster.broadcast(BroadcastEvent.REFRESH);
                msghdlr.messageOutln("Zero Histogram: "+currentHistogram.getTitle());
                dialogZero.dispose();
            } else if (command=="allzero") {
                zeroAll();
                dialogZero.dispose();
                /* commands for new histogram */
            }else if (command=="oknew"||command=="applynew"){
                makeHistogram();
                if (command=="oknew"){
                    dialogNew.dispose();
                }
            } else if (command=="cancelzero") {
                dialogZero.dispose();
            } else if (command=="cancelnew"){
                dialogNew.dispose();
            } else {
                /* just so at least a exception is thrown for now */
                throw new UnsupportedOperationException("Unregonized command: "+command);
            }
        } catch (DataException je) {
            msghdlr.errorOutln(je.getMessage());
        } 
    }

    /**
     * Zero all the histograms
     * Loops through the histograms zeroing them in turn
     */
    public void zeroAll() {
        msghdlr.messageOut("Zero All", MessageHandler.NEW);
        final Iterator allHistograms=Histogram.getHistogramList().iterator();
        while(allHistograms.hasNext()){
            final Histogram hist = ( (Histogram) allHistograms.next() );
            msghdlr.messageOut(" .", MessageHandler.CONTINUE);
            hist.setZero();
        }
        broadcaster.broadcast(BroadcastEvent.REFRESH);
        msghdlr.messageOut(" done!", MessageHandler.END);
    }

    /**
     * Make a new histogram from the field inputs
     */
    private void makeHistogram() throws DataException {
        try{
            histogramName=textName.getText().trim();
            histogramTitle=textTitle.getText().trim();
            if (coneInt.isSelected()){
                histogramType=Histogram.ONE_DIM_INT;
            } else  if (coneDbl.isSelected()){
                histogramType=Histogram.ONE_DIM_DOUBLE;
            } else  if (ctwoInt.isSelected()){
                histogramType=Histogram.TWO_DIM_INT;
            } else  if (ctwoDbl.isSelected()){
                histogramType=Histogram.TWO_DIM_DOUBLE;
            }
            histogramSize=Integer.parseInt(((String)comboSize.getSelectedItem()).trim());
            //histogramSize=Integer.parseInt(textSize.getText().trim());
            new Histogram (histogramName, histogramType, histogramSize, histogramTitle);
            broadcaster.broadcast(BroadcastEvent.HISTOGRAM_ADD);
            if (coneInt.isSelected()){
                msghdlr.messageOutln("New Histogram created "+histogramName+" type: 1-D int");
            } else  if (coneDbl.isSelected()){
                msghdlr.messageOutln("New Histogram created "+histogramName+" type: 1-D double");
            } else  if (ctwoInt.isSelected()){
                msghdlr.messageOutln("New Histogram created "+histogramName+" type: 2-D int" );
            } else  if (ctwoDbl.isSelected()){
                msghdlr.messageOutln("New Histogram created "+histogramName+" type: 1-D double");
            }
        } catch(NumberFormatException nfe){
            throw new DataException("Histogram Size not an integer [HistogramControl]");
        }
    }
}
