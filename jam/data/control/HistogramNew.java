package jam.data.control;
import jam.data.DataException;
import jam.data.Histogram;
import jam.global.BroadcastEvent;
import jam.global.JamStatus;
import jam.global.MessageHandler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
/**
 * Class to control the histograms
 * Allows one to zero the histograms
 * and create new histograms
 *
 * @author Ken Swartz
 * @version 0.5
 */
public class HistogramNew extends DataControl implements ActionListener {

    private final Frame frame;
    private final MessageHandler msghdlr;

    Histogram currentHistogram;

    private final JTextField textName;
    private final JTextField textTitle;
    private final JComboBox comboSize;
    private final JCheckBox coneInt,coneDbl,ctwoInt,ctwoDbl;

    private String histogramName,histogramTitle;
    private int histogramType,histogramSize;
	private final static String[] DEFAULT_SIZES= {
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
    public HistogramNew(MessageHandler msghdlr){
		super("New Histogram ",false);
        frame=status.getFrame();        
        this.msghdlr=msghdlr;

        //dialog box New Histogram
        setResizable(false);
        setLocation(30,30);
        Container cdialogNew=getContentPane();
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

        pack();
        
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                dispose();
            }
        });
    }

    /**
     * Does nothing. It is here to match other contollers.
     */
    public void setup(){
    }

    /**
     * Receive actions from Dialog Boxes
     *
     */
    public void actionPerformed(ActionEvent ae){
        String command=ae.getActionCommand();
        currentHistogram=Histogram.getHistogram(JamStatus.instance().getCurrentHistogramName());
        try {
            /* commands for new histogram */
            if (command=="oknew"||command=="applynew"){
                makeHistogram();
                if (command=="oknew"){
                  dispose();
                }
            } else if (command=="cancelnew"){
                dispose();
            } else {
                /* just so at least a exception is thrown for now */
                throw new UnsupportedOperationException("Unregonized command: "+command);
            }
        } catch (DataException je) {
            msghdlr.errorOutln(je.getMessage());
        } 
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
