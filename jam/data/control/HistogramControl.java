/*
 */
package jam.data.control;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import jam.global.*;
import jam.data.*;
import jam.*;
import javax.swing.*;

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
    private JamMain jamMain;
    private Broadcaster broadcaster;
    private MessageHandler msghdlr;

    private Dialog dialogCalib;
    private JDialog dialogZero, dialogNew;
    Histogram currentHistogram;

    //new Histogram
    private JTextField textName,textSize,textTitle;
    private JCheckBox coneInt,coneDbl,ctwoInt,ctwoDbl;

    private String histogramName,histogramTitle;
    private int histogramType,histogramSize;

    /**
     * Constructor
     */
    public HistogramControl(JamMain jamMain, Broadcaster broadcaster, MessageHandler msghdlr){
        super();
        this.frame=(Frame)jamMain;
        this.jamMain=jamMain;
        this.broadcaster=broadcaster;
        this.msghdlr=msghdlr;
        //zero histogram dialog box
        dialogZero=new JDialog(frame,"Zero Histograms",false);
        Container dzc = dialogZero.getContentPane();
        dialogZero.setResizable(false);
        //dialogZero.setSize(350, 100);
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
        dialogNew.setForeground(Color.black);
        dialogNew.setBackground(Color.lightGray);
        dialogNew.setResizable(false);
        dialogNew.setLocation(30,30);
        //dialogNew.setSize(450, 300);
        Container cdialogNew=dialogNew.getContentPane();
        cdialogNew.setLayout(new GridLayout(0, 1, 10,10));
        JPanel pn= new JPanel();
        pn.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
        cdialogNew.add(pn);
        JLabel ln=new JLabel("   ",JLabel.RIGHT);
        ln.setText("Name");
        pn.add(ln);
        textName =new JTextField(" ");
        textName.setColumns(15);
        textName.setForeground(Color.black);
        textName.setBackground(Color.white);
        pn.add(textName);


        JPanel pti= new JPanel();
        pti.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
        cdialogNew.add(pti);

        JLabel lti=new JLabel("   ", JLabel.RIGHT);
        lti.setText("Title");
        pti.add(lti);

        textTitle =new JTextField(" ");
        textTitle.setColumns(30);
        textTitle.setForeground(Color.black);
        textTitle.setBackground(Color.white);
        pti.add(textTitle);

        JPanel pt= new JPanel();
        pt.setLayout(new FlowLayout(FlowLayout.LEFT,3,5));
        cdialogNew.add(pt);

        JLabel lt=new JLabel("   ", JLabel.RIGHT);
        lt.setText("Type");
        pt.add(lt);

        //Panel pradio = new Panel(new FlowLayout(FlowLayout.CENTER));
        ButtonGroup cbg = new ButtonGroup();
        coneInt = new JCheckBox("1-D int",true);
        coneDbl = new JCheckBox("1-D double",false);
        ctwoInt = new JCheckBox("2-D int",false);
        ctwoDbl = new JCheckBox("2-D double",false);
        cbg.add(coneInt);
        cbg.add(coneDbl);
        cbg.add(ctwoInt);
        cbg.add(ctwoDbl);

        pt.add(coneInt);
        pt.add(coneDbl);
        pt.add(ctwoInt);
        pt.add(ctwoDbl);

        JPanel ps= new JPanel();
        ps.setLayout(new FlowLayout(FlowLayout.LEFT,5,5));
        cdialogNew.add(ps);

        JLabel ls=new JLabel("   ", JLabel.LEFT);
        ls.setText("Size");
        ps.add(ls);

        textSize =new JTextField(" ");
        textSize.setColumns(4);
        textSize.setForeground(Color.black);
        textSize.setBackground(Color.white);
        ps.add(textSize);

        //FIXME      textNumber =new TextField(" ");
        //      textNumber.setColumns(3);
        //      textNumber.setForeground(Color.black);
        //      textNumber.setBackground(Color.white);
        //      pt.add(textNumber);



        // panel for buttons
        JPanel pbOuter= new JPanel();
        pbOuter.setLayout(new FlowLayout(FlowLayout.CENTER));
        cdialogNew.add(pbOuter);

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
                throw new DataException("Unregonized command [HistogramControl]");
            }
        } catch (DataException je) {
            msghdlr.errorOutln(je.getMessage());
        } catch (GlobalException ge) {
            msghdlr.errorOutln(getClass().getName()+
            ".actionPerformed(): "+ge);
        }
    }

    /**
     * Zero all the histograms
     * Loops through the histograms zeroing them in turn
     */
    public void zeroAll() throws GlobalException {
        Histogram hist;
        Iterator allHistograms;

        msghdlr.messageOut("Zero All", MessageHandler.NEW);
        allHistograms=Histogram.getHistogramList().iterator();
        while(allHistograms.hasNext()){
            hist = ( (Histogram) allHistograms.next() );
            msghdlr.messageOut(" .", MessageHandler.CONTINUE);
            hist.setZero();
        }
        broadcaster.broadcast(BroadcastEvent.REFRESH);
        msghdlr.messageOut(" done!", MessageHandler.END);
    }

    /**
     * Make a new histogram from the field inputs
     */
    private void makeHistogram() throws DataException, GlobalException {
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
            histogramSize=Integer.parseInt(textSize.getText().trim());
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
