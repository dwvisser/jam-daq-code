/*
 */
package jam.plot;
import java.awt.*;
/**
 * Color map for display
 * The possible colors are
 *
 *@author Ken Swartz and Dale Visser
 *
 */
class PlotColorMap {


    public final static int BLACK_ON_WHITE=0;
    public final static int WHITE_ON_BLACK=1;
    public final static int PRINT=5;

    private final static int NUMBER_COLORS=9;

    static int colorMode;

    static Color colorScale[]=new Color [NUMBER_COLORS];
    static Color background;
    static Color foreground;
    static Color hist;
    static Color overlay;
    static Color gateDraw;
    static Color gateShow;
    static Color fit;
    static Color residual;
    static Color mark;
    static Color area;

    public  PlotColorMap(int mode){
        setColorMap(mode);
    }

    public static void setColorMap(int mode){
        //System.err.println("PlotGraphicsColorMap.setColorMap("+mode+")");
        if (mode==BLACK_ON_WHITE){
            colorMode=mode;
            background=Color.white;
            foreground=Color.darkGray;
            hist=Color.black;
            overlay=Color.magenta;
            gateDraw=Color.blue;
            gateShow=Color.red;
            mark=Color.red;
            area=Color.green;
            fit=Color.red;
            residual=Color.blue;
            colorScale=colorScaleBonW;
        } else if (mode==WHITE_ON_BLACK){
            colorMode=mode;
            background=Color.black;
            foreground=Color.lightGray;
            hist=Color.white;
            overlay=Color.orange;
            gateDraw=Color.cyan;
            gateShow=Color.yellow;
            mark=Color.yellow;
            area=Color.green;
            fit=Color.pink;
            residual=Color.red;
            colorScale=colorScaleWonB;
        } else if (mode==PRINT){
            colorMode=mode;
            background=Color.white;
            foreground=Color.black;
            hist=Color.black;
            overlay=new Color(59, 59, 59);
            gateDraw=new Color(59, 59, 59);
            gateShow=new Color(59, 59, 59);
            mark=new Color(102, 102, 102 );
            area=new Color(102, 102, 102 );
            fit= new Color(59, 59, 59);
            residual=new Color(102, 102, 102 );
            colorScale=colorScaleGray;
        } else {
            System.err.println("PlotGraphicsColorMap.setColorMap("+mode+"): Invalid Color Mode!");
        }
    }

    /**
     * color scale
     */
    public static Color [] getColorScale(){
        return colorScale;
    }
    /**
     * Number of colors
     */
    public static int getNumberColors() {
        return NUMBER_COLORS;
    }
    private static Color [] colorScaleBonW= {
        new Color(0, 0, 127),  //0
        new Color(0, 0, 255),  //1
        new Color(128, 0, 255 ),  //2
        new Color(255, 0,255 ),  //3
        new Color(255,0,128  ),  //4
        new Color(255,0,0 ),  //5
        new Color(255,128,0 ),  //6
        new Color(255,255,0 ),  //7
        //new Color(255, 255, 255 ),  //8
        new Color(0  , 0  , 0 )  //9
    };

    private static Color [] colorScaleWonB= {
        new Color(0, 0, 127),  //0
        new Color(0, 0, 255),  //1
        new Color(128, 0, 255 ),  //2
        new Color(255, 0,255 ),  //3
        new Color(255,0,128  ),  //4
        new Color(255,0,0 ),  //5
        new Color(255,128,0 ),  //6
        new Color(255,255,0 ),  //7
        new Color(255, 255, 255 ),  //8
        //new Color(0  , 0  , 0 )  //9
    };


    //color map for printing
    private static Color [] colorScaleGray= {
        new Color(0, 127, 0),  //0
        new Color(0, 0, 255),  //1
        new Color(128, 0, 255 ),  //2
        new Color(255, 0,255 ),  //3
        new Color(255,0,128  ),  //4
        new Color(255,0,0 ),  //5
        new Color(255,128,0 ),  //6
        new Color(255,255,0 ),  //7
        new Color(255, 255, 255 ),  //8
        //new Color(0  , 0  , 0 )  //9



        /*new Color(234, 234, 234 ),  //0
        new Color(212,212,212 ),  //1
        new Color(190,190,190 ),  //2
        new Color(168,168,168 ),  //3
        new Color(146,146,146 ),  //4
        new Color(124, 124,124 ),  //5
        new Color(102, 102, 102 ),  //6
        new Color(80, 80, 80),  //7
        new Color(59, 59, 59),  //8
        new Color(1, 1,   1)  //9*/
    };

}
