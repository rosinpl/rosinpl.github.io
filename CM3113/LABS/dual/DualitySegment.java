import java.awt.*;
import java.awt.event.*;
import java.util.*;

class DualitySegment extends DualityObject
{
    public static final double TOLERANCE = 0.1;

    boolean isLine;

    double slope = 0;
    double yint = 0;

    DualityVertex p;
    DualityVertex q;
    
    // creates a line
    public DualitySegment(Vertex a, Vertex b, DrawingArea d)
    {
	this(0, 0, d);
	isLine = false; // disable check
	update(a.x, a.y, b.x, b.y);
	isLine = true;
	update(slope, -yint);
	((DualityVertex)dual).update(slope, -yint);
    }

    // creates a line
    public DualitySegment(double slope, double yint, DrawingArea d) 
    {
	this(slope, yint, d, new DualityVertex(slope, -yint, d, null));
	dual.dual = this;
    }

    // creates a line
    public DualitySegment(double slope, double yint, DrawingArea d, DualityObject obj)
    {
	super(d);
	// this is a line that extends the entire bounding window
	isLine = true;
	this.slope = slope;
	this.yint  = yint;

	//p = new DualityVertex(d.minCoord.x, d.minCoord.x*slope+yint, d, null);
	//q = new DualityVertex(d.maxCoord.x, d.maxCoord.x*slope+yint, d, null);

	p = new DualityVertex(0, 0, d, null);
	q = new DualityVertex(0, 0, d, null);

	update(slope, yint);
	dual = obj;
    }

    // creates a segment
    public DualitySegment(double px, double py, double qx, double qy, 
			  DrawingArea d)
    {
	this(px, py, qx, qy, d, new DualityWedge(d));
	dual.dual = this;
	DualityWedge wedge = (DualityWedge) dual;
	wedge.setDualitySegments((DualitySegment)p.dual, 
				 (DualitySegment)q.dual);
    }

    // creates a segment
    public DualitySegment(double px, double py, double qx, double qy, 
			  DrawingArea d, DualityObject obj)
    {
	super(d);
	isLine = false;
	p = new DualityVertex(px, py, d);
	q = new DualityVertex(qx, qy, d);
	update(px, py, qx, qy);
	dual = obj;
    }

    public void draw(Graphics g, Vector secondPass)
    {
	if(secondPass != null) {
	    secondPass.addElement(this);
	    return;
	}
	setColor(g);

	Vertex pp = drawArea.transform(p.p.x, p.p.y);
	Vertex qq = drawArea.transform(q.p.x, q.p.y);

	/*
	  System.out.println("DualitySegment drawLine \n"+
	  Math.round((float)pp.x)+" "+
	  Math.round((float)pp.y)+" "+
	  Math.round((float)qq.x)+" "+
	  Math.round((float)qq.y));
	*/

	g.drawLine(Math.round((float)pp.x), Math.round((float)pp.y), 
		   Math.round((float)qq.x), Math.round((float)qq.y));

	if(!isLine) {
	    p.draw(g, secondPass);
	    q.draw(g, secondPass);
	}
    }

    boolean pMousePressed;
    boolean qMousePressed;

    double slopeAnchor;
    double yIntAnchor;

    public void mousePressed(double x, double y, int modifier) 
    { 
	mousePressed(x, y, modifier, p.onBoundary(x, y), q.onBoundary(x, y));
    }

    public void mousePressed(double x, double y, int modifier,
			     boolean pMP, boolean qMP)
    {
	pMousePressed = pMP;
	qMousePressed = qMP;

	p.mousePressed(x, y, modifier);
	q.mousePressed(x, y, modifier);
	setState(SELECTED);

	slopeAnchor = slope;
	yIntAnchor  = yint;
    }

    public void mouseOver(double x, double y, int modifier)
    {
	setState(HIGHLIGHT);
	p.mouseOver(x, y, modifier);
	q.mouseOver(x, y, modifier);
	if(!isLine) {
	    boolean pTouched = p.onBoundary(x, y);
	    boolean qTouched = q.onBoundary(x, y);
	    if(pTouched)
	        p.setState(HIGHLIGHT2);
	    if(qTouched) // no "else" to allow overlapped vertices
		q.setState(HIGHLIGHT2);
	}
    }

    // assumed input is transformed!
    public boolean onBoundary(double x, double y)
    {
	return onSegment(x, y) || p.onBoundary(x, y) || q.onBoundary(x, y);
    }

    public boolean onSegment(double x, double y)
    {
	return ((Math.abs(x*slope + yint - y) < TOLERANCE) && 
		((x <= (p.p.x + TOLERANCE)) && (x >= (q.p.x - TOLERANCE)) ||
		 (x <= (q.p.x + TOLERANCE)) && (x >= (p.p.x - TOLERANCE))));
    }
    
    public void update(double slope, double yint)
    {
	Util.assert(isLine, "DualitySegment::update()");
	this.slope = slope;
	this.yint  = yint;

	//Util.assert(p.p.x == drawArea.minCoord.x, "DualitySegment::update()2");
	//Util.assert(q.p.x == drawArea.maxCoord.x, "DualitySegment::update()3");

	double px, py, qx, qy;

	px = -30;
	py = -30*slope+yint;

	qx = 30;
	qy = 30*slope+yint;

	p.update(px, py);
	q.update(qx, qy);

    }

    public void update(double px, double py, double qx, double qy)
    {
	Util.assert(!isLine, "DualitySegment::update(2)");
	//Util.assert(px != qx, "DualitySegment::update(2) divide by zero");
	if(px == qx) {
	    this.slope = Double.MAX_VALUE;
	    this.yint  = 0;
	}
	else {
	    this.slope = (qy-py)/(qx-px);
	    this.yint  = py-px*slope;
	}

	p.update(px, py);
	q.update(qx, qy);
    }

    public void insertBSP(BSPCell root)
    {
	BSPCell.insert(root, new Segment(p.p, q.p, this));
    }

    public String toString()
    {
	String result = "";
	if(isLine)
	    result += "DualityLine: ";
	else
	    result += "DualitySegment: ";
	result += "  slope="+slope+" yint="+yint+"\n  "+
	    p.toString()+"\n  "+q.toString();
	return result;
    }

    public void mouseDragged(double x, double y, Vertex anchor, int modifier)
    {
	double px = anchor.x;
	double py = anchor.y;
	double qx = x;
	double qy = y;

	double newSlope = slopeAnchor;
	double newYInt  = yIntAnchor;

	// if CNTL, adjust slope from anchor of line
	if ((modifier & MouseEvent.CTRL_MASK) > 0) {
	    //System.out.println(qx+" "+qy+" "+px+" "+py);
	    if(Math.abs(qx-px) > 0.00001) {
		newSlope = (qy-py)/(qx-px);
		newYInt  = py-px*newSlope;
	    }
	}
	else { // else translate
	    newYInt = qy-slopeAnchor*qx;
	}

	// find out if the end points are selected
	DualityVertex target = null;
	Vertex pivot = null;
	if(pMousePressed) {
	    target = p;
	    pivot = q.anchor;
	}
	if(qMousePressed) {
	    target = q;
	    pivot = p.anchor;
	}

	if((!isLine && target != null) || ((modifier & MouseEvent.CTRL_MASK) > 0)) {
	    if(Math.abs(newSlope) >= 3.0 || 
	       Math.abs(newYInt) >= 3.0) {
		newYInt  = py-px*3.0*sign(newSlope);
		newSlope = sign(newSlope)*3.0;
		if(Math.abs(newYInt) >= 3.0) {
		    newSlope = (py-3.0*sign(newYInt))/px;
		    newYInt  = sign(newYInt)*3.0;
		}
	    }
	}
	else {
	    newSlope = Util.max(newSlope, drawArea.minCoord.x);
	    newYInt  = Util.min(newYInt, -drawArea.minCoord.y);
			   
	    newSlope = Util.min(newSlope, drawArea.maxCoord.x);
	    newYInt  = Util.max(newYInt, -drawArea.maxCoord.y);
	}

	if(isLine) {	    
	    update(newSlope, newYInt);
	    DualityVertex vert = (DualityVertex) dual;
	    vert.update(slope, -yint);
	}
	else {
	    if(target != null) {
		if(qx != pivot.x) {
		    newSlope = (qy-pivot.y)/(qx - pivot.x);
		    newYInt  = pivot.y-pivot.x*newSlope;

		    // this is a hack... too lazy to make work for
		    // general drawing area.
		    // assume square  drawing area of half width 3
		    if(Math.abs(newSlope) >= 3.0 || 
		       Math.abs(newYInt) >= 3.0) {
			newYInt  = pivot.y-pivot.x*3.0*sign(newSlope);
			newSlope = sign(newSlope)*3.0;
			if(Math.abs(newYInt) >= 3.0) {
			    newSlope = (pivot.y-3.0*sign(newYInt))/pivot.x;
			    newYInt  = sign(newYInt)*3.0;
			}
		    }
		    //System.out.println(newSlope+" "+newYInt);
		    qy = newSlope*qx+newYInt;
		}
		target.mouseDragged(qx, qy, anchor, modifier);
		update(p.p.x, p.p.y, q.p.x, q.p.y);
		((DualityWedge) dual).update();
		return;
	    }


	    if ((modifier & MouseEvent.CTRL_MASK) > 0) {
		double pRadiusSqr = anchor.distSqr(p.anchor);
		double qRadiusSqr = anchor.distSqr(q.anchor);
		double newSlopeSqr = newSlope*newSlope;

		double pX = Math.sqrt(pRadiusSqr/(1+newSlopeSqr));
		double pY = Math.sqrt(newSlopeSqr*pRadiusSqr/(1+newSlopeSqr));
		double qX = Math.sqrt(qRadiusSqr/(1+newSlopeSqr));
		double qY = Math.sqrt(newSlopeSqr*qRadiusSqr/(1+newSlopeSqr));
		
		double signdx = sign(qx - px);
		double signdy = sign(qy - py);
		
		pX = signdx*pX;
		pY = signdy*pY;
		qX = -1*signdx*qX;
		qY = -1*signdy*qY;
		update(px + pX, py + pY, px + qX, py + qY);
	    }
	    else {
		//always intersect anchor-- qx, qy with newSlope and newInt
		HalfSpace anchorNewPoint = new HalfSpace(px, py, qx, qy);
		HalfSpace newLine = new HalfSpace(newSlope, newYInt);

		Vertex newPoint = anchorNewPoint.intersect(newLine);
		
		double dx = newPoint.x - px;
		double dy = newPoint.y - py;
		
		update(p.anchor.x + dx, p.anchor.y + dy,
		       q.anchor.x + dx, q.anchor.y + dy);
	    }
	    p.mouseDragged(p.p.x, p.p.y, p.anchor, modifier);
	    q.mouseDragged(q.p.x, q.p.y, p.anchor, modifier);
	    ((DualityWedge) dual).update();
	}

    }

    public void exportObjects(Vector primalList, Vector dualList)
    {
	primalList.addElement(this);

	/*
	if(dual instanceof DualityWedge)  // let the wedge be drawn first!
	    dualList.insertElementAt(dual, 0);
	else
	*/
	dualList.addElement(dual);
    }

    public void intersect(Vertex v, DualitySegment s)
    {
	if(s.slope == slope)
	    return;

	double x = (s.yint - yint)/(slope - s.slope);
	double y = (s.yint*slope - yint*s.slope)/(slope - s.slope);

	v.x = x;
	v.y = y;
    }

    private double sign(double a)
    {
	if(a >= 0)
	    return 1.0;
	else
	    return -1.0;
    }

    public void setState(int i)
    {
	state = i;

	if(state == NORMAL) {
	    p.setState(i);
	    q.setState(i);
	}
	if(dual != null)
	    dual.state = i;
    }
}

