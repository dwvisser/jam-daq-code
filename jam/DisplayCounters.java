/*
 */
package jam;
import jam.global.BroadcastEvent;
import jam.global.Broadcaster;
import jam.global.GlobalException;
import jam.global.MessageHandler;
import jam.sort.NetDaemon;
import jam.sort.SortDaemon;
import jam.sort.StorageDaemon;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Displays buffer countesr of sort threads.
 * Gives the number of buffers and events received
 * and sorted.
 *
 * @author Ken Swartz
 * @version 05 newest done 9-98
 *
 */
class DisplayCounters implements Observer, ActionListener {

    public static final int ONLINE=1;
    public static final int OFFLINE=2;
    private static int NUMBER_COUNTERS=3;
    private static int INDEX_CNT_EVNT=1;
    private static int INDEX_CNT_BUFF=2;

    //stuff for dialog box
    private JDialog d;
    private JamMain jamMain;

    SortDaemon sortDaemon;
    NetDaemon netDaemon;
    StorageDaemon storageDaemon;
    Broadcaster broadcaster;
    MessageHandler messageHandler;

    private int mode;

    //text fields
    private JTextField textFileRead, textBuffSent, textBuffRecv, textBuffSort, 
    textBuffWrit,textEvntSent, textEvntSort;
    private JPanel pFileRead, pBuffSent, pBuffRecv, pBuffWrit, pBuffSort, 
    pEvntSent,pEvntSort, pButton;


    /**
     * Constructor
     */
    public DisplayCounters(JamMain jamMain , Broadcaster broadcaster, 
    MessageHandler msgHandler ){
        this.jamMain=jamMain;
        this.broadcaster=broadcaster;
        this.messageHandler=msgHandler;
        d =new JDialog (jamMain, "Buffer Counters",false);
        d.setForeground(Color.black);
        d.setBackground(Color.lightGray);
        d.setResizable(false);
        d.setLocation(20,50);
        Container cd=d.getContentPane();
        cd.setLayout(new GridLayout(0,1,10,10));
        pBuffSent= new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
        JLabel lps=new JLabel("Packets sent",JLabel.RIGHT);
        pBuffSent.add(lps);
        textBuffSent =new JTextField("");
        textBuffSent.setColumns(8);
        textBuffSent.setBackground(Color.white);
        textBuffSent.setForeground(Color.black);
        pBuffSent.add(textBuffSent);
        pBuffRecv= new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
        JLabel lpr=new JLabel("Packets received", JLabel.RIGHT);
        pBuffRecv.add(lpr);
        textBuffRecv =new JTextField("");
        textBuffRecv.setColumns(8);
        textBuffRecv.setBackground(Color.white);
        textBuffRecv.setForeground(Color.black);
        pBuffRecv.add(textBuffRecv);
        pBuffSort= new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
        JLabel lsb=new JLabel("Buffers sorted", JLabel.RIGHT);
        pBuffSort.add(lsb);
        textBuffSort =new JTextField("");
        textBuffSort.setColumns(8);
        textBuffSort.setBackground(Color.white);
        textBuffSort.setForeground(Color.black);
        pBuffSort.add(textBuffSort);
        pBuffWrit= new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
        JLabel lw=new JLabel("Buffers written", JLabel.RIGHT);
        pBuffWrit.add(lw);
        textBuffWrit =new JTextField("");
        textBuffWrit.setColumns(8);
        textBuffWrit.setBackground(Color.white);
        textBuffWrit.setForeground(Color.black);
        pBuffWrit.add(textBuffWrit);
        pEvntSent= new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
        JLabel les=new JLabel("Events sent", JLabel.RIGHT);
        pEvntSent.add(les);
        textEvntSent =new JTextField("");
        textEvntSent.setColumns(8);
        textEvntSent.setBackground(Color.white);
        textEvntSent.setForeground(Color.black);
        pEvntSent.add(textEvntSent);
        pEvntSort= new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
        JLabel lst=new JLabel("Events sorted", JLabel.RIGHT);
        pEvntSort.add(lst);
        textEvntSort =new JTextField("");
        textEvntSort.setColumns(8);
        textEvntSort.setBackground(Color.white);
        textEvntSort.setForeground(Color.black);
        pEvntSort.add(textEvntSort);
        pFileRead= new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
        JLabel lf=new JLabel("Files read", JLabel.RIGHT);
        pFileRead.add(lf);
        textFileRead =new JTextField("");
        textFileRead.setColumns(8);
        textFileRead.setBackground(Color.white);
        textFileRead.setForeground(Color.black);
        pFileRead.add(textFileRead);
        // panel for buttons
        pButton= new JPanel(new GridLayout(1,0,5,5));
        JButton bupdate =  new JButton("Update");
        bupdate.setActionCommand("update");
        bupdate.addActionListener(this);
        pButton.add(bupdate);
        JButton bclear = new JButton("Clear");
        bclear.setActionCommand("clear");
        bclear.addActionListener(this);
        pButton.add(bclear);

        //Recieves events for closing the dialog box and closes it.
        d.addWindowListener( new WindowAdapter() {
            public void windowClosing(WindowEvent e){
                d.dispose();
            }
        });
    }

    /**
     * Show online sorting dialog Box
     */
    public void show(){
        d.show();
    }
    
    /**
     * Receives events from this dialog box.
     */
    public void actionPerformed(ActionEvent ae){
        String command=ae.getActionCommand();
        try {
            if (command=="update") {
                //offline
                if(mode==ONLINE){
                    broadcaster.broadcast(BroadcastEvent.COUNTERS_READ);
                    textEvntSort.setText(""+sortDaemon.getEventCount());
                    textBuffSort.setText(""+sortDaemon.getBufferCount());
                    textBuffRecv.setText(""+netDaemon.getPacketCount());
                    textBuffWrit.setText(""+storageDaemon.getBufferCount());
                    //offline
                } else {
                    textEvntSort.setText(""+sortDaemon.getEventCount());
                    textBuffSort.setText(""+sortDaemon.getBufferCount());
                    textFileRead.setText(""+storageDaemon.getFileCount());
                }
                //do we need to blank all fields before zeroing and reading
            } else  if (command=="clear"){
                //online
                if(mode==ONLINE){
                    broadcaster.broadcast(BroadcastEvent.COUNTERS_ZERO);
                    textBuffSort.setText(" ");
                    sortDaemon.setBufferCount(0);
                    textBuffSort.setText(""+sortDaemon.getBufferCount());
                    textEvntSent.setText(" ");
                    sortDaemon.setEventCount(0);
                    textEvntSort.setText(""+sortDaemon.getEventCount());
                    textBuffSent.setText(" ");		//value update method
                    textEvntSent.setText(" ");		//value update method
                    textBuffRecv.setText(" ");
                    netDaemon.setPacketCount(0);
                    textBuffRecv.setText(""+netDaemon.getPacketCount());
                    textBuffWrit.setText(" ");
                    storageDaemon.setBufferCount(0);
                    textBuffWrit.setText(""+storageDaemon.getBufferCount());
                    broadcaster.broadcast(BroadcastEvent.COUNTERS_READ);
                    //offline
                } else {
                    textBuffSort.setText(" ");
                    sortDaemon.setBufferCount(0);
                    textBuffSort.setText(""+sortDaemon.getBufferCount());
                    textEvntSent.setText(" ");
                    sortDaemon.setEventCount(0);
                    textEvntSort.setText(""+sortDaemon.getEventCount());
                    textFileRead.setText(" ");
                    storageDaemon.setFileCount(0);
                    textFileRead.setText(""+storageDaemon.getFileCount());
                }
            }
        } catch (GlobalException ge) {
            messageHandler.errorOutln(getClass().getName()+
            ".actionPerformed(): "+ge);
        }
    }

    /**
     * setup for online
     */
    public void setupOn(	NetDaemon netDaemon, SortDaemon sortDaemon,
    StorageDaemon storageDaemon) {
        mode=ONLINE;
        this.netDaemon=netDaemon;
        this.sortDaemon=sortDaemon;
        this.storageDaemon=storageDaemon;
        // make dialog box
        Container cd=d.getContentPane();
        cd.removeAll();
        d.setTitle("Online Buffer Count");
        cd.add(pBuffSent);
        cd.add(pBuffRecv);
        cd.add(pBuffSort);
        cd.add(pBuffWrit);
        cd.add(pEvntSent);
        cd.add(pEvntSort);
        cd.add(pButton);
        d.pack();
    }

    /**
     * setup for offline
     */
    public void setupOff(SortDaemon sortDaemon, StorageDaemon storageDaemon){
        mode=OFFLINE;
        this.sortDaemon=sortDaemon;
        this.storageDaemon=storageDaemon;
        Container cd=d.getContentPane();
        cd.removeAll();
        d.setTitle("Offline Buffer Count");
        cd.add(pFileRead);
        cd.add(pBuffSort);
        cd.add(pEvntSort);
        cd.add(pButton);
        d.pack();
    }

    /**
     * update counters
     *
     * @author Ken Swartz
     */
    public void update(Observable observable, Object o){
        BroadcastEvent be=(BroadcastEvent)o;
        int command=be.getCommand();
        int vmeCounters []=new int [NUMBER_COUNTERS];

        if(command==BroadcastEvent.COUNTERS_UPDATE){

            //online only update remote fields
            if(mode==ONLINE){
                vmeCounters=(int [])be.getContent();
                textBuffSent.setText(""+vmeCounters[INDEX_CNT_BUFF]);
                textEvntSent.setText(""+vmeCounters[INDEX_CNT_EVNT]);

                //off line we have to update all fields
            } else {
                textBuffSort.setText(""+sortDaemon.getBufferCount());
                textEvntSort.setText(""+sortDaemon.getEventCount());
                textFileRead.setText(""+storageDaemon.getFileCount());
            }
        }
    }
}