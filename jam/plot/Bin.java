/*
 * Created on Oct 4, 2004
 *
 */
package jam.plot;


import java.awt.Point;

/**
 * Abstraction of a histogram channel on the display.
 * 
 * @author <a href="mailto:dale@visser.name">Dale Visser</a>
 */
public final class Bin {
	/**
	 * For 1d hists, y-coordinate is arbitrary.
	 */
	private final Point channel=new Point();
	private static Display display=null;
	
	public static class Factory{
		static void init(Display disp){
			display=disp;
		}
		
		public static Bin create(Point p){
			if (display==null){
				throw new IllegalStateException("Bin not initialized.");
			}
			return new Bin(p);
		}
		
		public static Bin create(int x, int y){
			return create(new Point(x,y));
		}
		
		public static Bin create(int x){
			return create(x,0);
		}
		
		public static Bin create(){
			return create(0,0);
		}
	}
	
	private Bin(Point p){
		setChannel(p);
	}
	
	
	public static Bin copy(Bin c){
		return Factory.create(c.getPoint());
	}
	
	synchronized void setChannel(Point p){
		channel.setLocation(p);
	}
	
	synchronized void setChannel(Bin c){
		channel.setLocation(c.getPoint());
	}
	
	synchronized void setChannel(int x, int y){
		channel.setLocation(x,y);
	}
	
	synchronized double getCounts(){
		return display.getPlot().getCount(this);		
	}
	
	synchronized Point getPoint(){
		return new Point(channel);
	}
	
	public synchronized int getX(){
		return channel.x;
	}
	
	public synchronized int getY(){
		return channel.y;
	}
	
	synchronized String getCoordString() {
		final StringBuffer rval = new StringBuffer().append(channel.x);
		if (display.getPlot().getType()==Plot.TYPE_1D) {
			rval.append(',').append(channel.y);
		}
		return rval.toString();
	}	
	
	public boolean equals(Object o){
		boolean rval= o instanceof Bin;
		if (rval){
			final Bin that=(Bin)o;
			rval &= channel.equals(that.channel);
		}
		return rval;
	}
	
	public synchronized Bin closestInsideBin() {
		int x=channel.x;
		int y=channel.y;
		final Plot currentPlot = display.getPlot();
		if (x < 0) {
			x = 0;
		} else if (x >= currentPlot.getSizeX()) {
			x = currentPlot.getSizeX() - 1;
		}
		if (y < 0) {
			y = 0;
		} else if (y >= currentPlot.getSizeY()) {
			y = currentPlot.getSizeY() - 1;
		}
		return Factory.create(x,y);
	}
	
	public synchronized void shiftInsidePlot(){
		setChannel(closestInsideBin());
	}
}
