import java.awt.*;
import java.awt.event.*;
import java.util.*;

// class to represent the dual of a segment
// It is not meant to be a "primitive" class that is inserted directly
// by the GUI.
class DualityWedge extends DualityObject
{

    DualitySegment sp;
    DualitySegment sq;
    
    DualityVertex intersect;

    public DualityWedge(DrawingArea d)
    {
	super(d);
    }
    
    public void setDualitySegments(DualitySegment p, DualitySegment q)
    {
	sp = p;
	sq = q;
	Vertex v  = new Vertex(0, 0);
	sp.intersect(v, sq);
	intersect = new DualityVertex(v.x, v.y, drawArea, null);
    }

    protected void setColor(Graphics g)
    {
	switch(state) {
	case NORMAL:
	    g.setColor(Color.gray.darker().darker()); break;
	case HIGHLIGHT:
	    g.setColor(Color.blue.darker().darker().darker()); break;
	case HIGHLIGHT2:
	    g.setColor(Color.yellow.darker().darker().darker()); break;
	case HIGHLIGHT3:
	    g.setColor(Color.cyan.darker().darker().darker()); break;
	case HIGHLIGHT4:
	    g.setColor(Color.pink.darker().darker().darker()); break;
	case HIGHLIGHT5:
	    g.setColor(Color.green.darker().darker().darker()); break;
	case SELECTED:
	    g.setColor(Color.red); break;
	default:
	    Util.assert(false, "DualityWedge:setColor");
	}
    }    

    int[] xc = new int[4];
    int[] yc = new int[4];

    public void draw(Graphics g, Vector secondPass)
    {
	setColor(g);

	Vertex spp = drawArea.transform(sp.p.p.x, sp.p.p.y);
	Vertex spq = drawArea.transform(sp.q.p.x, sp.q.p.y);
	Vertex sqp = drawArea.transform(sq.p.p.x, sq.p.p.y);
	Vertex sqq = drawArea.transform(sq.q.p.x, sq.q.p.y);

	xc[0] = Math.round((float)spp.x); 
	xc[1] = Math.round((float)spq.x); 
	xc[2] = Math.round((float)sqq.x);
	xc[3] = Math.round((float)sqp.x); 
	yc[0] = Math.round((float)spp.y); 
	yc[1] = Math.round((float)spq.y); 
	yc[2] = Math.round((float)sqq.y);
	yc[3] = Math.round((float)sqp.y);

	/*
	  System.out.println("DualityWedge fillPolygon");
	  
	  for(int i = 0; i < 4; i++) {
	  System.out.println(xc[i]+" ");
	  }
	  
	  System.out.println("\n");
	  
	  for(int i = 0; i < 4; i++) {
	  System.out.println(yc[i]+" ");
	  }
	  
	  g.fillPolygon(xc, yc, 4);
	  
	  System.out.println("DualityWedge");
	*/

	g.fillPolygon(xc, yc, 4);

	// the lines and vertex in the second pass
	
	secondPass.addElement(sp);
	secondPass.addElement(sq);
	
	if(intersect != null && intersect.state != NORMAL)
	    secondPass.addElement(intersect);

	//System.out.println("DualityWedge exits");
    }

    boolean pMousePressed;
    boolean qMousePressed;
    boolean iMousePressed;

    public void mousePressed(double x, double y, int modifier) 
    { 
	iMousePressed = intersect.onBoundary(x, y);
	pMousePressed = sp.onBoundary(x, y);
	qMousePressed = sq.onBoundary(x, y);

	if(iMousePressed) {
	    intersect.mousePressed(x, y, modifier);
	    sp.mousePressed(x, y, modifier);
	    sq.mousePressed(x, y, modifier);
	    setState(SELECTED);
	}
	else if(pMousePressed)
	    sp.mousePressed(x, y, modifier);
	else if(qMousePressed) 
	    sq.mousePressed(x, y, modifier);
    }

    public void mouseOver(double x, double y, int modifier)
    {
	setState(HIGHLIGHT);
	intersect.mouseOver(x, y, modifier);
	sp.mouseOver(x, y, modifier);
	sq.mouseOver(x, y, modifier);
	if(intersect != null && intersect.onBoundary(x, y))
	    intersect.setState(HIGHLIGHT2);
	else if(sp.onBoundary(x, y))
	    sp.setState(HIGHLIGHT2);
	else if(sq.onBoundary(x, y))
	    sq.setState(HIGHLIGHT2);
    }

    public boolean onBoundary(double x, double y)
    {
	return (intersect != null && intersect.onBoundary(x, y)) || 
	    sp.onBoundary(x, y) || sq.onBoundary(x, y);
    }

    public void update()
    {
	sp.intersect(intersect.p, sq);
    }

    // mouseDragged
    public void mouseDragged(double x, double y, Vertex anchor, int modifier)
    {
	if(iMousePressed) {
	    if((modifier & MouseEvent.CTRL_MASK) > 0)
		modifier -= MouseEvent.CTRL_MASK;
	    intersect.mouseDragged(x, y, anchor, modifier);
	    sp.mouseDragged(x, y, anchor, modifier);
	    sq.mouseDragged(x, y, anchor, modifier);
	    update();
	}
	else if(pMousePressed) {
	    // use the intersection as anchor node
	    sp.mouseDragged(x, y, intersect.p, modifier);
	    update();
	}
	else if(qMousePressed) {
	    // use the intersection as anchor node
	    sq.mouseDragged(x, y, intersect.p, modifier);
	    update();
	}
    }

    public String toString()
    {
	String result = "DualityWedge: ";
	result += "  "+sp.toString()+"\n  "+sq.toString()+"\n  "+intersect;
	return result;
    }

    public void insertBSP(BSPCell root)
    {
	BSPCell.insert(root, new Segment(sp.p.p, sp.q.p, sp));
	BSPCell.insert(root, new Segment(sq.p.p, sq.q.p, sq));
    }

    public void exportObjects(Vector primalList, Vector dualList)
    {
	Util.assert(false, "DualityWedge::exportObjects "+
		    "this class should not never be a primitive instance");
    }

    public void setState(int i)
    {
	state = i;

	if(state == NORMAL) {
	    sp.setState(i);
	    sq.setState(i);
	    if(intersect != null)
		intersect.setState(i);
	}
	if(dual != null)
	    dual.state = i;
    }

    private HalfSpace spPQ = new HalfSpace();
    private HalfSpace sqQP = new HalfSpace();

    public boolean contains(Vertex v)
    {
	spPQ.set(sp.p.p.x, sp.p.p.y, sp.q.p.x, sp.q.p.y);
	sqQP.set(sq.q.p.x, sq.q.p.y, sq.p.p.x, sq.p.p.y);

	return
	    ((spPQ.inPosPlane(v) && sqQP.inPosPlane(v)) ||
	     (!spPQ.inPosPlane(v) && !sqQP.inPosPlane(v)));
    }
}
