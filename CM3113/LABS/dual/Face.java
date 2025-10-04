import java.awt.*;
import java.util.*;

// Face contains half edges, not segments!!!!!
class Face 
{
    public SegmentList head, tail;
    private int numSeg;

    public Face()
    {
	head = tail = null;
	numSeg = 0;
    }

    // origin defines upper lefthand corner
    public Face( int x, int y, int width, int height ) {
	this((double)x, (double)y, (double)width, (double)height);
    }

    public Face( double x, double y, double width, double height) {
	add(new Segment(new Vertex(x, y), new Vertex(x+width, y)));
	add(new Segment(new Vertex(x+width, y), new Vertex(x+width, y+height)));
	add(new Segment(new Vertex(x+width, y+height), new Vertex(x, y+height)));
	add(new Segment(new Vertex(x, y+height), new Vertex(x, y)));
    }

    public void draw(Graphics g, Color clr)
    {
	if(head == null)
	    return;
	
	int[] x, y;
	x = new int[numSeg];
	y = new int[numSeg];
	SegmentList e = head;
	int c = 0;
	while(e != null) {
	    x[c] = (int) e.s.p.x;
	    y[c] = (int) e.s.p.y;
	    //	    e.s.draw(g, clr, 0);
	    e = e.next;
	    c++;
	}
	g.setColor(clr);
	g.fillPolygon(x, y, x.length);
    }

    public void draw(Graphics g, Color clr, DrawingArea d)
    {
	if(head == null)
	    return;
	
	int[] x, y;
	x = new int[numSeg];
	y = new int[numSeg];
	SegmentList e = head;
	int c = 0;
	while(e != null) {
	    Vertex v = d.transform(e.s.p.x, e.s.p.y);
	    x[c] = Math.round((float) v.x);
	    y[c] = Math.round((float) v.y);
	    //	    e.s.draw(g, clr, 0);
	    e = e.next;
	    c++;
	}
	g.setColor(clr);
	g.fillPolygon(x, y, x.length);
    }


    public Segment clip(Segment s)
    {
	Segment result = s;
	SegmentList e = head;
	while(e != null && result != null) {
	    //	    System.out.println("clip with "+e.s.h);
	    result = result.clip(e.s.h);
	    e = e.next;
	}
	return result;
    }

    // p, q is layout order
    // ccw is the counter clockwise order
    public Face[] split(HalfSpace h, DualitySegment ds) 
    {
	Face[] result = new Face[2];
	result[0] = new Face();
	result[1] = new Face();
	SegmentList e  = head;
	Vertex prevSplit = null;
	while(e != null) {
	    Vertex n = e.s.intersect(h);
	    if(n == null) { 
		if(prevSplit != null)
		    result[1].add(e.s);
		else 
		    result[0].add(e.s);
	    }
	    else {
		// need to split current e
		Segment left  = new Segment(e.s.p, n, e.s.ds);
		Segment right = new Segment(n, e.s.q, e.s.ds);

		if(prevSplit != null) {
		    // second intersection found
		    Segment newSegLeft  = new Segment(prevSplit, n, ds);
		    Segment newSegRight = new Segment(n, prevSplit, ds); //twin
		    result[0].add(newSegLeft);
		    result[0].add(right);

		    result[1].add(left);
		    result[1].add(newSegRight);
		    prevSplit = null;
		}
		else {
		    result[0].add(left);
		    result[1].add(right);
		    prevSplit = n;
		}
	    }
	    e = e.next;
	}

	//Util.assert(result[1].head != null, "blah "+this);

	/* 
	   check to see if the order should be swapped by looking at the 
	   end vertex of the first edge and see which half of the HalfSpace
	   it belongs to.
	*/
	/* FIXME: result[1].head could be null if intersect() doesn't
	   work properly due to round off errors.  Just ignore this case
	   for now.  */
	if(result[1].head != null && h.inPosPlane(result[1].head.s.q)) {
	    /* swap */
	    Face temp = result[0];
	    result[0] = result[1];
	    result[1] = temp;
	}
      
	return result;
    }

    public void add(Segment e) {
	//	System.out.println(e);
	if(head == null) {
	    Util.assert(tail == null, "Face::add()");
	    head = tail = new SegmentList(e);
	}
	else
	    tail = SegmentList.insert(tail, new SegmentList(e));
	numSeg++;
    }

    public String toString()
    {
	SegmentList e = head;
	String result = "F[";
	while(e != null) {
	    result += e+",";
	    e = e.next;
	}
	result += "null]";
	return result;
    }
}
