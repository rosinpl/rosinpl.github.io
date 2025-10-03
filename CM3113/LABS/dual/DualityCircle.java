import java.awt.*;
import java.awt.event.*;
import java.util.*;

// no rotation
class DualityCircle extends DualityObjectSet
{
    public static final double TOLERANCE = 0.15;
    public static final int NUM_POINTS = 32; // should be even

    public double radius;
    public Vertex center;

    public DualityCircle(double x, double y, DrawingArea d)
    {
	super(d);
	dual = new DualityTriangleDual(d); // heh, a hack
	dual.dual = this;
	center = new Vertex(x, y);
	radius = 0.25;
	for(int i = 0; i < NUM_POINTS; i++) {
	    DualityVertex v = new DualityVertex(0, 0, d);
	    set.addElement(v);
	    ((DualityTriangleDual) dual).set.addElement(v.dual);
	}
	update(x, y, radius);
    }

    private void update(double x, double y, double r)
    {
	radius = r;
	center.x = x;
	center.y = y;

	//System.out.println(radius+" "+center);
	double angle = 2*Math.PI/NUM_POINTS;
	for(int i = 0; i < NUM_POINTS/2; i++) {
	    double yy = radius*Math.sin(i*angle) + y;
	    double xx = radius*Math.cos(i*angle) + x;
	    DualityVertex v = (DualityVertex) set.elementAt(i);
	    v.mouseDragged(xx, yy, null, 0);

	    if(i > 0) {
		yy = radius*Math.sin(-i*angle) + y;
		xx = radius*Math.cos(-i*angle) + x;
	    }
	    else {
		yy = y;
		xx = x - radius;		
	    }

	    v = (DualityVertex) set.elementAt(i+NUM_POINTS/2);
	    v.mouseDragged(xx, yy, null, 0);
	}
    }

    public void mouseOver(double x, double y, int modifier)
    {
	DualityObject v1 = null;
	DualityObject v2 = null;

	setState(HIGHLIGHT3);
	for(int i = 0; i < set.size(); i++) {
	    DualityObject obj = (DualityObject) set.elementAt(i);
	    obj.mouseOver(x, y, modifier);
	    if(obj.onBoundary(x, y) && v1 == null) {
		v1 = obj;
		int index = i;
		if((modifier & MouseEvent.SHIFT_MASK) > 0) {
		    if(i == 0 || i == NUM_POINTS/2)
			index = (i+NUM_POINTS/2) % NUM_POINTS;
		    else
			index = NUM_POINTS-i;
		}
		else if((modifier & MouseEvent.CTRL_MASK) > 0) { 
		    // symmetry over y axis
		    if( i <= NUM_POINTS/2 )
			index = NUM_POINTS/2 - i;
		    else
			index = 3*NUM_POINTS/2 - i;
		}
		else { // symmetry over x axis
		    if(i != 0 && i != NUM_POINTS/2)
			index = (i+NUM_POINTS/2) % NUM_POINTS;
		}
		v2 = (DualityObject) set.elementAt(index);
	    }
	}
	if(v1 != null) {
	    v1.setState(HIGHLIGHT2);
	    v2.setState(HIGHLIGHT2);
	}
    }


    private double sign(double angle, boolean isSin)
    {
	if(isSin) {
	    if(angle > Math.PI)
		return -1;
	}
	else {
	    if(angle > 3)
		return 1;
	}
	return 1;
    }

    public boolean onBoundary(double x, double y)
    {
	double dist = rad(center.x, center.y, x, y);
	return Math.abs(dist - radius) <= TOLERANCE || center.inBound(x,y);
    }

    public double rad(double px, double py, double qx, double qy)
    {
	return Math.sqrt(sqr(px - qx) + sqr(py - qy));
    }

    public double sqr(double r)
    {
	return r*r;
    }

    public void draw(Graphics g, Vector secondPass)
    {
	if(secondPass != null) {
	    secondPass.addElement(this);
	    return;
	}

	Vertex pp = drawArea.transform(center.x-radius, center.y+radius);
	Vertex qq = drawArea.transform(center.x+radius, center.y-radius);

	setColor(g);

	// java-ism
	g.drawOval(Math.round((float)pp.x), Math.round((float)pp.y), 
		   Math.round((float)Math.abs(qq.x-pp.x)), Math.round((float)Math.abs(qq.y-pp.y)));

	if(state != NORMAL)
	    center.draw(g, drawArea);

	for(int i = 0; i < set.size(); i++) {
	    DualityObject obj = (DualityObject) set.elementAt(i);
	    if(obj.state == HIGHLIGHT2)
		obj.draw(g, null);
	}
    }

    public void mouseDragged(double x, double y, Vertex anchor, 
			     int modifier) 
    { 
	double newRad = rAnchor;
	double newX   = cAnchor.x;
	double newY   = cAnchor.y;

	if((modifier & MouseEvent.CTRL_MASK) > 0) {
	    newRad = rad(cAnchor.x, cAnchor.y, x, y);
	    newRad = Util.min(newRad, drawArea.maxCoord.x - cAnchor.x);
	    newRad = Util.min(newRad, cAnchor.x - drawArea.minCoord.x);
	    newRad = Util.min(newRad, drawArea.maxCoord.y - cAnchor.y);
	    newRad = Util.min(newRad, cAnchor.y - drawArea.minCoord.y);
	}
	else { 	// translation
	    double dx = x - anchor.x;
	    double dy = y - anchor.y;

	    newX += dx;
	    newY += dy;

	    double minX = drawArea.minCoord.x + radius;
	    double minY = drawArea.minCoord.y + radius;
	    double maxX = drawArea.maxCoord.x - radius;
	    double maxY = drawArea.maxCoord.y - radius;

	    newX = Util.min(newX, maxX);
	    newX = Util.max(newX, minX);
	    newY = Util.min(newY, maxY);
	    newY = Util.max(newY, minY);
	}

	update(newX, newY, newRad);
    }

    Vertex cAnchor = new Vertex(0, 0);
    double rAnchor;
    public void mousePressed(double x, double y, int modifier) 
    { 
	setState(SELECTED);
	for(int i = 0; i < set.size(); i++)
	    ((DualityObject) set.elementAt(i)).mousePressed(x, y, modifier);
	cAnchor.x = center.x;
	cAnchor.y = center.y;
	rAnchor   = radius;
    }

    public void insertBSP(BSPCell root)
    {
	return;
    }

    public String toString()
    {
	return super.toString("DualityCircle");
    }
}

