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
    public final static int B_ON_W=0;
    public final static int W_ON_B=1;
    public final static int PRINT=5;

    private final static int NUM_COLORS=9;

    private transient Color background;
    private transient Color foreground;
    private transient Color hist;
    private static final Color [] OVERLAY={Color.RED, Color.GREEN, Color.BLUE,
    		Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.ORANGE, DARK_RED};
    private transient Color gateDraw;
    private transient Color gateShow;
    private transient Color fitTotal;
    private transient Color fitSignal;
    private transient Color fitBkgd;
    private transient Color fitResidual;
    private transient Color mark;
    private transient Color area;
    private transient Color peakLabel;

    static final private PlotColorMap MAP=new PlotColorMap(B_ON_W);
    
    private PlotColorMap(int mode){
        setColorMap(mode);
    }
    
    static public PlotColorMap getSingletonInstance(){
    	return MAP;
    }

    public synchronized void setColorMap(int mode){
        if (mode==B_ON_W){
            background=Color.WHITE;
            foreground=Color.DARK_GRAY;
            hist=Color.BLACK;
            gateDraw=Color.GREEN;
            gateShow=Color.RED;
            mark=Color.RED;
            area=Color.GREEN;
            fitTotal=Color.BLUE;
            fitSignal=Color.DARK_GRAY;
            fitBkgd=Color.GREEN;
            fitResidual=Color.RED;
            peakLabel=Color.BLUE;
        } else if (mode==W_ON_B){
            background=Color.BLACK;
            foreground=Color.LIGHT_GRAY;
            hist=Color.WHITE;
            gateShow=Color.RED;
            gateDraw=Color.GREEN;
            mark=Color.YELLOW;
            area=Color.GREEN;
			fitTotal=Color.CYAN;
			fitSignal=Color.LIGHT_GRAY;
			fitBkgd=Color.GREEN;
			fitResidual=Color.RED;
			peakLabel=Color.CYAN;
        } else if (mode==PRINT){
            background=Color.WHITE;
            foreground=Color.BLACK;
            hist=Color.BLACK;
            gateDraw=new Color(59, 59, 59);
            gateShow=new Color(59, 59, 59);
            mark=new Color(102, 102, 102 );
            area=new Color(102, 102, 102 );
			fitTotal=Color.BLUE;
			fitSignal=Color.DARK_GRAY;
			fitBkgd=Color.GREEN;
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
        return NUM_COLORS;
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
    	return fitBkgd;
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
    	return OVERLAY[index % OVERLAY.length];
    }
    
    synchronized public Color getPeakLabel(){
    	return peakLabel;
    }
}
