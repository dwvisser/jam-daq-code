/*
 * Created on Oct 4, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jam.plot;


import java.awt.Point;

/**
 * @author Dale
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
final class Cursor {
	/**
	 * For 1d hists, y-coordinate is arbitrary.
	 */
	private final Point channel=new Point();
	private final Display display;
	
	Cursor(Display disp, Point p){
		display=disp;
		setChannel(p);
	}
	
	synchronized void setChannel(Point p){
		channel.setLocation(p);
	}
	
	synchronized double getCounts(){
		return display.getPlot().getCount(channel);		
	}
	
	synchronized Point getPoint(){
		return new Point(channel);
	}
	
	synchronized String getCoordString() {
		final StringBuffer rval = new StringBuffer().append(channel.x);
		if (display.getPlot() instanceof Plot2d) {
			rval.append(',').append(channel.y);
		}
		return rval.toString();
	}
	
}
