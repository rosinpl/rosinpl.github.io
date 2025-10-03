import java.awt.*;
import java.util.*;

class DualityVertex extends DualityObject
{
    public static final double TOLERANCE = 0.1;

    Vertex p;
    Vertex anchor;

    public DualityVertex(double x, double y, DrawingArea d) {
	this(x, y, d, new DualitySegment(x, -y, d, null));
	dual.dual = this;
    }

    public DualityVertex(double x, double y, DrawingArea d, DualityObject obj) {
	super(d);
	p = new Vertex(x, y);
	anchor = new Vertex(x, y);
	dual = obj;
    }

    public void draw(Graphics g, Vector secondPass)
    {
	if(secondPass != null) {
	    secondPass.addElement(this);
	    return;
	}

	setColor(g);
	p.draw(g, drawArea);
    }

    // assumed input is transformed!
    public boolean onBoundary(double x, double y)
    {
	return p.inBound(x, y);
    }

    public void mousePressed(double x, double y, int modifier) 
    {
	super.mousePressed(x, y, modifier);
	anchor.x = p.x;
	anchor.y = p.y;
    }    

    public void mouseDragged(double x, double y, Vertex anchor, int modifier)
    {
	p.x = x; p.y = y;

	if(dual != null) {
	    DualitySegment seg = (DualitySegment) dual;
	    seg.update(x, -y);
	}
    }

    public String toString()
    {
	return "DualityVertex: "+p;
    }

    public void update(double x, double y)
    {
	p.x = x; 
	p.y = y;
    }
}




