import java.awt.*;
import java.util.*;

// no rotation
class DualityTriangle extends DualityObjectSet
{
    public static final double TOLERANCE = 0.1;

    public DualityTriangle(double x, double y, DrawingArea d)
    {
	super(d);
	dual = new DualityTriangleDual(d);
	dual.dual = this;
	add(x, y);
    }

    public void add(double x, double y)
    {
	DualityVertex v = new DualityVertex(x, y, drawArea);
	set.addElement(v);
	((DualityTriangleDual) dual).set.addElement(v.dual);
    }

    public boolean onBoundary(double x, double y)
    {
	if(set.size() == 1)
	    return ((DualityObject) set.elementAt(0)).onBoundary(x,y);
	else if(set.size() >= 2) {
	    Vertex p = ((DualityVertex) set.elementAt(0)).p;
	    Vertex q = ((DualityVertex) set.elementAt(1)).p;
	    if(set.size() == 2)
		return onSegment(p, q, x, y);
	    else {
		Vertex r = ((DualityVertex) set.elementAt(2)).p;
		return 
		    onSegment(p, q, x, y) ||
		    onSegment(q, r, x, y) ||
		    onSegment(r, p, x, y);
	    }
	}
	return false;
    }

    public boolean onSegment(Vertex p, Vertex q, double x, double y)
    {
	double slope;
	double yint;
	if(Math.abs(p.x - q.x) < 0.00001) {
	    return (Math.abs(x - p.x) < TOLERANCE) &&
		((y <= (p.y + TOLERANCE)) && (y >= (q.y - TOLERANCE)) ||
		 (y <= (q.y + TOLERANCE)) && (y >= (p.y - TOLERANCE)));
	}
	else {
	    slope = (q.y-p.y)/(q.x-p.x);
	    yint  = p.y-p.x*slope;
	}

	return ((Math.abs(x*slope + yint - y) < TOLERANCE) && 
		((x <= (p.x + TOLERANCE)) && (x >= (q.x - TOLERANCE)) ||
		 (x <= (q.x + TOLERANCE)) && (x >= (p.x - TOLERANCE))));
    }


    public void draw(Graphics g, Vector secondPass)
    {
	if(secondPass != null) {
	    secondPass.addElement(this);
	    return;
	}
	Util.assert(set.size() <= 3, "DualityTriangle::draw");

	for(int i = 0; i < set.size(); i++) {
	    DualityVertex p = (DualityVertex) set.elementAt(i);
	    DualityVertex q = (DualityVertex) set.elementAt((i+1)%set.size());

	    Vertex pp = drawArea.transform(p.p.x, p.p.y);
	    Vertex qq = drawArea.transform(q.p.x, q.p.y);

	    setColor(g);

	    g.drawLine(Math.round((float)pp.x), Math.round((float)pp.y), 
		       Math.round((float)qq.x), Math.round((float)qq.y));

	    p.draw(g, secondPass); // might set a different color after this call
	}
    }

    public void mouseOver(double x, double y, int modifier)
    {
	setState(HIGHLIGHT3);
	for(int i = 0; i < set.size(); i++) {
	    DualityObject obj = (DualityObject) set.elementAt(i);
	    obj.mouseOver(x, y, modifier);
	    if(obj.onBoundary(x, y)) 
		obj.setState(HIGHLIGHT2);
	}
    }

    public void mouseDragged(double x, double y, Vertex anchor, 
			     int modifier) 
    { 
	for(int i = 0; i < set.size(); i++) {
	    DualityObject obj = (DualityObject) set.elementAt(i);
	    if(obj.onBoundary(x, y)) {
		obj.mouseDragged(x, y, anchor, modifier);
		return;
	    }
	}

	// translational movement
	if(set.size() < 3)
	    return;

	double dx = x - anchor.x;
	double dy = y - anchor.y;

	DualityVertex a = (DualityVertex) set.elementAt(0);
	DualityVertex b = (DualityVertex) set.elementAt(1);
	DualityVertex c = (DualityVertex) set.elementAt(2);

	dx = Util.min(dx, drawArea.maxCoord.x - a.anchor.x);
	dx = Util.min(dx, drawArea.maxCoord.x - b.anchor.x);
	dx = Util.min(dx, drawArea.maxCoord.x - c.anchor.x);

	dx = Util.max(dx, drawArea.minCoord.x - a.anchor.x);
	dx = Util.max(dx, drawArea.minCoord.x - b.anchor.x);
	dx = Util.max(dx, drawArea.minCoord.x - c.anchor.x);

	dy = Util.min(dy, drawArea.maxCoord.y - a.anchor.y);
	dy = Util.min(dy, drawArea.maxCoord.y - b.anchor.y);
	dy = Util.min(dy, drawArea.maxCoord.y - c.anchor.y);

	dy = Util.max(dy, drawArea.minCoord.y - a.anchor.y);
	dy = Util.max(dy, drawArea.minCoord.y - b.anchor.y);
	dy = Util.max(dy, drawArea.minCoord.y - c.anchor.y);

	a.mouseDragged(a.anchor.x + dx, a.anchor.y + dy, anchor, modifier);
	b.mouseDragged(b.anchor.x + dx, b.anchor.y + dy, anchor, modifier);
	c.mouseDragged(c.anchor.x + dx, c.anchor.y + dy, anchor, modifier);
    }

    public void mousePressed(double x, double y, int modifier) 
    { 
	if(set.size() < 3) 
	    add(x, y);
	setState(SELECTED);
	for(int i = 0; i < set.size(); i++)
	    ((DualityObject) set.elementAt(i)).mousePressed(x, y, modifier);
    }

    public void mouseMoved(double x, double y, int modifier)
    {
	// called when creating the triangle
	((DualityObject)set.elementAt(set.size()-1)).mouseDragged(x, y, null, modifier);
    }

    public void insertBSP(BSPCell root)
    {
	return;
    }

    public String toString()
    {
	return super.toString("DualityTriangle");
    }

    public int numVertices()
    {
	return set.size();
    }
}

