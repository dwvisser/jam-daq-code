package help.sortfiles;
import jam.data.*;
import jam.sort.*;

/**
 *  Online sort routine for YLSA coincidence with Enge Spectrometer. This was
 *  modified from <CODE>sort.coinc.SPplus3LEDA_v3</CODE>, which was used in the
 *  January 2001 test run. Changed 10 Aug 2001 to calculate the scintillator
 *  event the right way; also added gate to cathAnde
 *
 *@author     Dale Visser
 *@created    March 24, 2004
 *@since      26 July 2001
 */
public class SpectrographExample extends SortRoutine {

    private final static String DEAD_TIME = "Dead Time %";
    /*
     *  VME properties
     */
    private final static int[] ADC_BASE = {0x20000000};
    private final static int[] TDC_BASE = {0x30000000, 0x30020000};

    /*
     *  ADC lower threshold in channels
     */
    private final static int THRESHOLDS = 128;
    /*
     *  TDC lower threshold in channels
     */
    private final static int TIME_THRESH = 30;
    /*
     *  in nanoseconds
     */
    private final static int TIME_RANGE = 1200;

    /*
     *  num of channels per ADC
     */
    private final static int ADC_CHANNELS = 4096;

    /*
     *  2D histograms
     */
    private final static int CHAN_2D = 256;
    /*
     *  hi-res 2D histograms
     */
    private final static int TWO_D_HIRES = 2 * CHAN_2D;

    /*
     *  2D bits to shift >>
     */
    private final static int TWO_D_FACTOR = Math.round((float)
            (Math.log(ADC_CHANNELS / CHAN_2D) / Math.log(2.0)));
    /*
     *  2D hi-res bits to shift >>
     */
    private final static int HIRES_FACTOR = Math.round((float)
            (Math.log(ADC_CHANNELS / TWO_D_HIRES) / Math.log(2.0)));

    /*
     *  ungated spectra
     */
    private transient Histogram hCthd, hSntrSum, hFrntPsn;
    /*
     *  Rear Wire Pulse Height
     */
    private transient Histogram hFrntPH;
    /*
     *  position x height y
     */
    private transient Histogram hRearPH;
    private transient Histogram hSntrCthd, hFrntCthd,
            hFrntSntr, hSntrAnde, hFrntPRearP;

    /*
     *  gate by scintillator cathode
     */
    private transient Histogram hFrntSntrGSC, hFrntCthdGSC;
    /*
     *  gate by Front wire Cathode
     */
    private transient Histogram hSntrCthdGFC, hFrntSntrGFC;
    private transient Histogram hSntrCthdGFS, hFrntCthdGFS;
    /*
     *  gate by Front wire Scintillator
     */
    private transient Histogram hFrntGAll;
    /*
     *  1D gates
     */
    private transient Gate gCthd;
    /*
     *  2D gates
     */
    private transient Gate gSntrCthd, gFrntSntr, gFrntCthd, gFrntRear;

    /*
     *  Scalers and monitors
     */
    private transient Scaler sBic, sClck, sEvntRaw, sEvntAccpt, sScint, sCathode,
            sNMR, sFCLR;
    /*
     *  number of FCLR's that went to ADC's
     */
    private transient Monitor mBeam, mClck, mEvntRaw, mEvntAccept, mScint,
            mCathode, mNMR, mFCLR;

    /*
     *  id numbers for the signals;
     */
    private transient int idCthd, idAnde, idScintR, idScintL, idFrntPsn,
            idRearPsn, idFrntHgh, idRearHgh, idYFrnt, idYRear, idFWbias, idRWbias,
            idBCIRange, idFrontLeft, idFrontRight, idRearLeft, idRearRight,
            idFrMidPH, idFrBackPH, idReFrPH, idRearBackPH, idFrLeftTDC,
            idFrRightTDC, idReLeftTDC, idReRightTDC;
    private transient DataParameter pXTheta, pThOffset, pCTheta;


    /**
     *  Description of the Method
     *
     *@exception  Exception  Description of the Exception
     */
    public void initialize() throws Exception {
        vmeMap.setScalerInterval(3);
        for (int i = 0; i < TDC_BASE.length; i++) {
            vmeMap.setV775Range(TDC_BASE[i], TIME_RANGE);
        }

        vmeMap.eventParameter(2, ADC_BASE[0], 0, 0);
        idAnde = vmeMap.eventParameter(2, ADC_BASE[0], 1, THRESHOLDS);
        idScintR = vmeMap.eventParameter(2, ADC_BASE[0], 2, THRESHOLDS);
        idScintL = vmeMap.eventParameter(2, ADC_BASE[0], 3, THRESHOLDS);
        idFrntPsn = vmeMap.eventParameter(2, ADC_BASE[0], 4,
                THRESHOLDS);
        idRearPsn = vmeMap.eventParameter(2, ADC_BASE[0], 5,
                THRESHOLDS);
        idFrntHgh = vmeMap.eventParameter(2, ADC_BASE[0], 6,
                THRESHOLDS);
        idRearHgh = vmeMap.eventParameter(2, ADC_BASE[0], 7,
                THRESHOLDS);
        idYFrnt = vmeMap.eventParameter(2, ADC_BASE[0], 8, THRESHOLDS);
        idYRear = vmeMap.eventParameter(2, ADC_BASE[0], 9, THRESHOLDS);
        idCthd = vmeMap.eventParameter(2, ADC_BASE[0], 10, THRESHOLDS);

        idFWbias = vmeMap.eventParameter(2, ADC_BASE[0], 12,
                THRESHOLDS);
        idRWbias = vmeMap.eventParameter(2, ADC_BASE[0], 13,
                THRESHOLDS);
        idBCIRange = vmeMap.eventParameter(2, ADC_BASE[0], 14, 16);
        idFrontLeft = vmeMap.eventParameter(2, ADC_BASE[0], 16 + 3,
                THRESHOLDS);
        idFrontRight = vmeMap.eventParameter(2, ADC_BASE[0], 16 + 4,
                THRESHOLDS);
        idRearLeft = vmeMap.eventParameter(2, ADC_BASE[0], 16 + 6,
                THRESHOLDS);
        idRearRight = vmeMap.eventParameter(2, ADC_BASE[0], 16 + 7,
                THRESHOLDS);

        /*
         *  new pulse height signals from TAJ
         */
        idRearBackPH = vmeMap.eventParameter(2, ADC_BASE[0], 16 + 10,
                THRESHOLDS);
        idReFrPH = vmeMap.eventParameter(2, ADC_BASE[0], 16 + 11,
                THRESHOLDS);
        idFrMidPH = vmeMap.eventParameter(2, ADC_BASE[0], 16 + 12,
                THRESHOLDS);
        idFrBackPH = vmeMap.eventParameter(2, ADC_BASE[0], 16 + 13,
                THRESHOLDS);

        /*
         *  TDC based position parameters
         */
        idFrLeftTDC = vmeMap.eventParameter(5, TDC_BASE[0], 16 + 0,
                TIME_THRESH);
        idFrRightTDC = vmeMap.eventParameter(5, TDC_BASE[0], 16 + 1,
                TIME_THRESH);
        idReLeftTDC = vmeMap.eventParameter(7, TDC_BASE[1], 0,
                TIME_THRESH);
        idReRightTDC = vmeMap.eventParameter(7, TDC_BASE[1], 1,
                TIME_THRESH);

        hCthd = new Histogram("Cathode     ", HIST_1D_INT,
                ADC_CHANNELS, "Cathode Raw ");
        hSntrSum = new Histogram("ScintSum    ", HIST_1D_INT,
                ADC_CHANNELS, "Scintillator Sum");
        hFrntPsn = new Histogram("FrontPosn    ", HIST_1D_INT,
                ADC_CHANNELS, "Front Wire Position");
        final String FRONT_POS = "Front Position";
        hFrntPH = new Histogram("FrontPvsHeight", HIST_2D_INT,
                CHAN_2D,
                "Pulse Height of FrontFront wire vs Front Position",
                FRONT_POS,
                "Pulse Height");
        hRearPH = new Histogram("RearPvsHeight ", HIST_2D_INT,
                CHAN_2D,
                "Pulse Height of RearMiddle wire vs Rear Position",
                "Rear Position", "Pulse Height");
        final String SCINT = "Scintillator";
        final String CATH = "Cathode";
        hSntrCthd = new Histogram("ScintCathode  ", HIST_2D_INT, CHAN_2D,
                "Cathode vs Scintillator",
                SCINT,
                CATH);
        hSntrAnde = new Histogram("ScintAnode  ", HIST_2D_INT, CHAN_2D,
                "Anode vs Scintillator", SCINT, "Anode");
        hFrntCthd = new Histogram("FrontCathode  ", HIST_2D_INT, CHAN_2D,
                "Cathode vs Front Position", FRONT_POS, CATH);
        hFrntSntr = new Histogram("FrontScint ", HIST_2D_INT, CHAN_2D,
                "Scintillator vs Front Position", FRONT_POS, SCINT);
        hFrntPRearP = new Histogram("FrontRear  ", HIST_2D_INT, TWO_D_HIRES,
                "Rear Position vs Front Position", FRONT_POS, "Rear Position");
        //ScintCathode Gated on other
        hSntrCthdGFC = new Histogram("ScintCathodeGFC", HIST_2D_INT, CHAN_2D,
                "Cathode vs Scintillator - FwCa gate", SCINT, CATH);
        hSntrCthdGFS = new Histogram("ScintCathodeGFS", HIST_2D_INT, CHAN_2D,
                "Cathode vs Scintillator - FwSc gate", SCINT, CATH);
        //FrontCathode Gated on other
        hFrntCthdGSC = new Histogram("FrontCathodeGSC", HIST_2D_INT, CHAN_2D,
                "Cathode vs Front Position - ScCa gate", FRONT_POS, CATH);
        hFrntCthdGFS = new Histogram("FrontCathodeGFS ", HIST_2D_INT, CHAN_2D,
                "Cathode vs Front Position - FwSc gate ", FRONT_POS, CATH);
        //FrontScint Gated on other
        hFrntSntrGSC = new Histogram("FrontScintGSC ", HIST_2D_INT, CHAN_2D,
                "Scintillator vs Front Position - ScCa gate", FRONT_POS,
                SCINT);
        hFrntSntrGFC = new Histogram("FrontScintGFC", HIST_2D_INT, CHAN_2D,
                "Scintillator vs Front Position - FwCa gate", FRONT_POS,
                SCINT);
        //gated on 4 gates
        hFrntGAll = new Histogram("FrontGAll    ", HIST_1D_INT, ADC_CHANNELS,
                "Front Position - ScCa,FwCa,FwSc,FwRw gates");
        // gates 1d
        gCthd = new Gate("Counts", hCthd);

        //gates  2d
        gSntrCthd = new Gate("Ca-Sc", hSntrCthd);
        //gate on Scintillator Cathode
        gFrntSntr = new Gate("Fw-Sc", hFrntSntr);
        //gate on Front Scintillator
        gFrntCthd = new Gate("Fw-Ca", hFrntCthd);
        //gate on Front Cathode

        gFrntRear = new Gate("Fw-Rw", hFrntPRearP);
        hFrntSntrGSC.addGate(gFrntSntr);
        hFrntCthdGSC.addGate(gFrntCthd);
        hSntrCthdGFC.addGate(gSntrCthd);
        hFrntSntrGFC.addGate(gFrntSntr);
        hSntrCthdGFS.addGate(gSntrCthd);
        hFrntCthdGFS.addGate(gFrntCthd);
        //scalers
        sBic = new Scaler("BIC", 0);
        sClck = new Scaler("Clock", 1);
        sEvntRaw = new Scaler("Event Raw", 2);
        sEvntAccpt = new Scaler("Event Accept", 3);
        sScint = new Scaler(SCINT, 4);
        sCathode = new Scaler(CATH, 5);
        sFCLR = new Scaler("FCLR", 6);
        sNMR = new Scaler("NMR", 14);

        //monitors
        mBeam = new Monitor("Beam ", sBic);
        mClck = new Monitor("Clock", sClck);
        mEvntRaw = new Monitor("Raw Events", sEvntRaw);
        mEvntAccept = new Monitor("Accepted Events", sEvntAccpt);
        mScint = new Monitor(SCINT, sScint);
        mCathode = new Monitor(CATH, sCathode);
        mFCLR = new Monitor("FCLR", sFCLR);
        mNMR = new Monitor("NMR", sNMR);
        new Monitor(DEAD_TIME, this);

        pThOffset = new DataParameter("THETA_OFFSET");
        pXTheta = new DataParameter("(X|Theta)    ");
        pCTheta = new DataParameter("CTheta");

    }


    /**
     *  Description of the Method
     *
     *@param  dataEvent      Description of the Parameter
     *@exception  Exception  Description of the Exception
     */
    public void sort(int[] dataEvent) throws Exception {
        /*
         *  unpack data into convenient names
         */
        int eCthd = dataEvent[idCthd];
        final int SCINTR = dataEvent[idScintR];
        final int SCINTL = dataEvent[idScintL];
        final int FPOS = dataEvent[idFrntPsn];
        final int RPOS = dataEvent[idRearPsn];
        final int FHEIGHT = dataEvent[idFrntHgh];
        final int RHEIGHT = dataEvent[idRearHgh];

        /*
         *  proper way to add for 2 phototubes at the ends of
         *  scintillating rod see Knoll
         */
        final int SCINT = (int) Math.round(Math.sqrt(SCINTR * SCINTL));
        final int FPOS_COMPR = FPOS >> TWO_D_FACTOR;
        final int RPOS_COMPR = RPOS >> TWO_D_FACTOR;
        final int FHEIGHT_COMP = FHEIGHT >> TWO_D_FACTOR;
        final int RHEIGHT_COMP = RHEIGHT >> TWO_D_FACTOR;
        final int SCINT_COMPR = SCINT >> TWO_D_FACTOR;

        /*
         *  use this to correct the focus for different particle groups with
         *  different kinematics
         */
        final double THETA_OFFSET = pThOffset.getValue();
        // center channel of Theta distribution
        final double THETA_CENTER = pCTheta.getValue();
        final int THETA_CHAN;
        final double DELTA_CATH;
        final double THETA_VAL;
        if (FPOS > 0 && RPOS > 0) {
            THETA_CHAN = RPOS - FPOS;
            THETA_VAL = THETA_CHAN - THETA_OFFSET;
            DELTA_CATH = (THETA_CENTER * (THETA_VAL));
        } else {
            THETA_CHAN = 0;
            DELTA_CATH = 0;
            THETA_VAL = 0;
        }
        eCthd += (int) DELTA_CATH;
        final int ECCTHD = eCthd >> TWO_D_FACTOR;

        // singles spectra
        hCthd.inc(eCthd);
        hSntrSum.inc(SCINT);
        hFrntPsn.inc(FPOS);

        final int FPOS_COMP_HI = FPOS >> HIRES_FACTOR;
        final int RPOS_COMP_HI = RPOS >> HIRES_FACTOR;
        hFrntPH.inc(FPOS_COMPR, FHEIGHT_COMP);
        hRearPH.inc(RPOS_COMPR, RHEIGHT_COMP);
        hSntrCthd.inc(SCINT_COMPR, ECCTHD);
        hFrntCthd.inc(FPOS_COMPR, ECCTHD);
        hFrntSntr.inc(FPOS_COMPR, SCINT_COMPR);
        hFrntPRearP.inc(FPOS_COMP_HI, RPOS_COMP_HI);

        final boolean SC_INGATE = gSntrCthd.inGate(SCINT_COMPR, ECCTHD);
        final boolean FC_INGATE = gFrntCthd.inGate(FPOS_COMPR, ECCTHD);
        final boolean FS_INGATE = gFrntSntr.inGate(FPOS_COMPR, SCINT_COMPR);
        final boolean IN_PID_GATES = SC_INGATE && FC_INGATE && FS_INGATE;
        final boolean FR_RE_INGATE = gFrntRear.inGate(FPOS_COMP_HI, RPOS_COMP_HI);
        final boolean GOOD_DIREC = FR_RE_INGATE;
        final boolean GOOD = GOOD_DIREC && IN_PID_GATES;

        if (SC_INGATE) {
            // gate on Scintillator vs Cathode
            hFrntSntrGSC.inc(FPOS_COMPR, SCINT_COMPR);
            hFrntCthdGSC.inc(FPOS_COMPR, ECCTHD);
        }
        if (FC_INGATE) {
            // gate on Front Wire Position vs Cathode
            hSntrCthdGFC.inc(SCINT_COMPR, ECCTHD);
            hFrntSntrGFC.inc(FPOS_COMPR, SCINT_COMPR);
        }
        if (FS_INGATE) {
            // gate on Front Wire Position vs Scintillator
            hSntrCthdGFS.inc(SCINT_COMPR, ECCTHD);
            hFrntCthdGFS.inc(FPOS_COMPR, ECCTHD);
        }
        if (IN_PID_GATES) {
            // gated on all 3 gate above
            //writeEvent(dataEvent);
            if (GOOD) {
                writeEvent(dataEvent);
                hFrntGAll.inc(FPOS);
            }
        }
    }


    /**
     *  Called so the dead time can be calculated.
     *
     *@param  name  name of monitor to calculate
     *@return       floating point value of monitor
     */
    public double monitor(String name) {
        double rval = 0.0;
        if (name.equals(DEAD_TIME)) {
            final double ACCEPTRATE = mEvntAccept.getValue();
            final double RAWRATE = mEvntRaw.getValue();
            if (ACCEPTRATE > 0.0 && ACCEPTRATE <= RAWRATE) {
                rval = 100.0 * (1.0 - ACCEPTRATE / RAWRATE);
            }
        }
        return rval;
    }
}

