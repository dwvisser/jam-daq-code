package jam.plot.color;
import java.awt.Color;
/**
 * Color map for display.
 *
 * @author Ken Swartz
 * @author Dale Visser
 */
public class PlotColorMap {

	private static final Color DARK_RED=new Color(192,0,0);
    public final static int BLACK_ON_WHITE=0;
    public final static int WHITE_ON_BLACK=1;
    public final static int PRINT=5;

    private final static int NUMBER_COLORS=9;

    int colorMode;

    private Color background;
    private Color foreground;
    private Color hist;
    private static final Color [] overlay={Color.RED, Color.GREEN, Color.BLUE,
    		Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.ORANGE, DARK_RED};
    private Color gateDraw;
    private Color gateShow;
    private Color fitTotal;
    private Color fitSignal;
    private Color fitBackground;
    private Color fitResidual;
    private Color mark;
    private Color area;
    private Color peakLabel;

    static final private PlotColorMap pcm=new PlotColorMap(BLACK_ON_WHITE);
    
    private PlotColorMap(int mode){
        setColorMap(mode);
    }
    
    static public PlotColorMap getSingletonInstance(){
    	return pcm;
    }

    public synchronized void setColorMap(int mode){
        if (mode==BLACK_ON_WHITE){
            colorMode=mode;
            background=Color.WHITE;
            foreground=Color.DARK_GRAY;
            hist=Color.BLACK;
            gateDraw=Color.GREEN;
            gateShow=Color.RED;
            mark=Color.RED;
            area=Color.GREEN;
            fitTotal=Color.BLUE;
            fitSignal=Color.DARK_GRAY;
            fitBackground=Color.GREEN;
            fitResidual=Color.RED;
            peakLabel=Color.BLUE;
        } else if (mode==WHITE_ON_BLACK){
            colorMode=mode;
            background=Color.BLACK;
            foreground=Color.LIGHT_GRAY;
            hist=Color.WHITE;
            gateShow=Color.RED;
            gateDraw=Color.GREEN;
            mark=Color.YELLOW;
            area=Color.GREEN;
			fitTotal=Color.CYAN;
			fitSignal=Color.LIGHT_GRAY;
			fitBackground=Color.GREEN;
			fitResidual=Color.RED;
			peakLabel=Color.CYAN;
        } else if (mode==PRINT){
            colorMode=mode;
            background=Color.WHITE;
            foreground=Color.BLACK;
            hist=Color.BLACK;
            gateDraw=new Color(59, 59, 59);
            gateShow=new Color(59, 59, 59);
            mark=new Color(102, 102, 102 );
            area=new Color(102, 102, 102 );
			fitTotal=Color.BLUE;
			fitSignal=Color.DARK_GRAY;
			fitBackground=Color.GREEN;
			fitResidual=Color.RED;
			peakLabel=Color.BLUE;
        } else {
            throw new IllegalArgumentException("PlotGraphicsColorMap.setColorMap("+mode+"): Invalid Color Mode!");
        }
        DiscreteColorScale.setColors(mode);
    }

    /**
     * Number of colors
     */
    public static int getNumberColors() {
        return NUMBER_COLORS;
    }
    
    
    synchronized public Color getForeground(){
    	return foreground;
    }
    
    synchronized public Color getBackground(){
    	return background;
    }
    
    synchronized public Color getGateShow(){
    	return gateShow;
    }
    
    synchronized public Color getGateDraw(){
    	return gateDraw;
    }
    
    synchronized public Color getMark(){
    	return mark;
    }
    
    synchronized public Color getArea(){
    	return area;
    }
    
    synchronized public Color getHistogram(){
    	return hist;
    }
    
    synchronized public Color getFitBackground(){
    	return fitBackground;
    }
    
    synchronized public Color getFitResidual(){
    	return fitResidual;
    }
    
    synchronized public Color getFitTotal(){
    	return fitTotal;
    }
    
    synchronized public Color getFitSignal(){
    	return fitSignal;
    }
    
    synchronized public Color getOverlay(int index){
    	return overlay[index % overlay.length];
    }
    
    synchronized public Color getPeakLabel(){
    	return peakLabel;
    }
}
