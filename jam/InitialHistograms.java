package jam;
import jam.data.DataException;
import jam.data.Gate;
import jam.data.Histogram;

/**
 * This class to make initial histogram to display
 * that is nice to look at.
 *
 * @version  0.5 April 98
 * @author   Ken Swartz
 * @since       JDK1.1
 */

public class InitialHistograms{

    /** starting histogram */
    public Histogram histStart;
    private int sizeX;
    private int sizeY;
    private int counts[];
    private int counts2d[][];
    private int units[];

    public InitialHistograms() throws DataException{
        makehists();
    }

    private void makehists() throws DataException{
        //histogram with Jam name 2d
        Histogram histJam2d=new Histogram("Histogram2D", "Jam Name 2D",histNameJam2d());    //fake histogram
        histStart=histJam2d;

        //histogram with Jam name
        Histogram histJam1d=new Histogram("Histogram1D", "Jam Name 1D", histNameJam1d());

        //histogram with triangles 
        new Histogram("Triangle", "Triangle ",histTriangle());

        new Gate("Letter A ",histJam1d);    //gate
        new Gate("Letter B ",histJam1d);    //gate
        new Gate("Letter C ",histJam1d);    //gate
        new Gate(" Area A ",histJam2d);      //gate 2d
        new Gate(" Area B ",histJam2d);      //gate 2d
        new Gate(" Area C ",histJam2d);      //gate 2d
    }

    /**
     * Make a 1 d histogram that says JAM.
     */
    private int [] histNameJam1d(){
        sizeX=1024;
        int startCh;
        counts = new int [sizeX];
        units= new int [sizeX];

        //Make a J
        for (int i =100;i< 150;i++) {
            counts[i]=15;
        }
        for (int i =150;i< 225;i++) {
            counts[i]=10;
        }
        for (int i =225;i< 300;i++) {
            counts[i]=100;
        }
        //Make a A
        startCh=400;
        for (int i =startCh;i< startCh+50;i++) {
            counts[i]=2*(i-startCh);
        }
        startCh=450;
        for (int i =startCh;i< startCh+50;i++) {
            counts[i]=100;
        }
        startCh=500;
        for (int i =startCh;i< startCh+50;i++) {
            counts[i]=100-2*(i-startCh);
        }
        //Make a M
        startCh=650;
        for (int i =startCh;i< startCh+50;i++) {
            counts[i]=100;
        }
        startCh=700;
        for (int i =startCh;i< startCh+25;i++) {
            counts[i]=100-2*(i-startCh);
        }
        startCh=725;
        for (int i =startCh;i< startCh+25;i++) {
            counts[i]=50+2*(i-startCh);
        }
        startCh=750;
        for (int i =startCh;i< startCh+50;i++) {
            counts[i]=100;
        }
        return counts;
    }
    
    private int [][] histNameJam2d(){
        sizeX=256;
        sizeY=256;
        int startCh;      //start channel for a loop
        int endCh;      //end channel for a loop
        int ch;
        counts2d = new int [sizeX][sizeY];
        //Make a J
        // increment x then y
        for (int i =20;i< 40;i++) {
            for (int j=30;j<60;j++){
                counts2d[i][j]=(int)Math.exp(1.0+j/20.0);
            }
        }
        for (int i =40;i< 70;i++) {
            for (int j=30;j<50;j++){
                counts2d[i][j]=(int)Math.exp(1.0+j/20.0);
            }
        }
        for (int i =50;i<70;i++) {
            for (int j=50;j<150;j++){
                counts2d[i][j]=(int)Math.exp(1.0+j/20.0);
            }
        }
        //Make a A
        startCh=30;
        //increment y then x
        for (int j=startCh;j<150;j++){
            ch=(j-startCh)/10;
            for (int i =90+ch;i<110+ch;i++) {
                counts2d[i][j]=(int)Math.exp(1.0+j/20.0);
            }
        }
        startCh=30;
        for (int j=startCh;j<150;j++){
            ch=(j-startCh)/10;
            for (int i =130-ch;i<150-ch;i++) {
                counts2d[i][j]=(int)Math.exp(1.0+j/20.0);
            }
        }
        for (int i =100;i<140;i++) {
            for (int j=80;j<100;j++){
                counts2d[i][j]=(int)Math.exp(1.0+j/20.0);
            }
        }
        //Make a M
        for (int i =170;i<190;i++) {
            for (int j=30;j<150;j++){
                counts2d[i][j]=(int)Math.exp(1.0+j/20.0);
            }
        }
        //x then y
        startCh=75;
        endCh=150;
        for (int j=startCh;j<150;j++){
            ch=(endCh-j)/5;
            for (int i =180+ch;i<200+ch;i++) {
                counts2d[i][j]=(int)Math.exp(1.0+j/20.0);
            }
        }
        startCh=75;
        endCh=150;
        for (int j=startCh;j<150;j++){
            ch=(endCh-j)/5;
            for (int i =210-ch;i<230-ch;i++) {
                counts2d[i][j]=(int)Math.exp(1.0+j/20.0);
            }
        }
        for (int i =220;i<240;i++) {
            for (int j=30;j<150;j++){
                counts2d[i][j]=(int)Math.exp(1.0+j/20.0);
            }
        }
        return counts2d;
    }

    /**
     * Make a numer of triangle to display in a 1 D Plot
     *
     */
    private int [] histTriangle(){
        sizeX=1024;
        int range;
        int position;
        counts = new int [sizeX];
        units= new int [sizeX];
        // make a small triangle
        position=0;
        range=200;
        for (int i=position; i<=position+range;i++){

            if (i<=(position+range/2)) {
                counts[i]=i-position;
            } else  {
                counts[i]=position+range-i;
            }
        }
        position=200;
        range=600;
        for (int i=position; i<=position+range;i++){

            if ((i<=position+range/2)) {
                counts[i]=i-position;
            } else  {
                counts[i]=position+range-i;;
            }
        }
        position=800;
        range=200;
        for (int i=position; i<=position+range;i++){
            if (i<=(position+range/2)) {
                counts[i]=i-position;
            } else  {
                counts[i]=position+range-i;
            }
        }
        return counts;
    }
}
