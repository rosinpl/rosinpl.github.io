import java.awt.*;

class Segment
{
    public Vertex p, q;
    public HalfSpace h;
    public DualitySegment ds;

    public Segment(Vertex p, Vertex q) 
    {
	init(p, q);
    }

    public Segment(Vertex p, Vertex q, DualitySegment ds)
    {
	this(p, q);
	this.ds = ds;
    }

    protected void init(Vertex p, Vertex q)
    {

	this.p = p;
	this.q = q;
	/*
	double a = 0, c = 0, b = 1;
	if(q.x != p.x) {
	    a = (q.y - p.y)/(q.x - p.x);
	    c = p.y - p.x*a;
	}
	else {
	    a = -1;
	    c = p.x;
	    b = 0;
	}	
	h = new HalfSpace( this, -a, b, -c );
	*/
	h = new HalfSpace( p.x, p.y, q.x, q.y );
	h.s = this;
    }

    public void draw(Graphics g, Color clr, int order) 
    {
	g.setColor(clr);
	/*
	g.drawLine(p.x, p.y, q.x, q.y);
	if(order > 0) {
	    double xm = (p.x + q.x)/2;
	    double ym = (p.y + q.y)/2;
	    int x, y;
	    if(p.x == q.x) {
		x = (int) Math.round(xm+5);
		y = (int) Math.round(ym);
	    }
	    else if(p.y == q.y) {
		x = (int) Math.round(xm);
		y = (int) Math.round(ym-5);
	    }
	    else {
		double m = (q.y - p.y)/(q.x - p.x);
		m = -1/m;
		double xp = 8.0/Math.sqrt(1+m*m) + xm;
		x = (int) Math.round(xp);
		y = (int) Math.round(m*(xp - xm)+ym);
	    }
	    //	    System.out.println(x+" "+y);
	    g.drawString(Integer.toString(order), x, y);			 
	}

	p.draw(g, clr);
	q.draw(g, clr);
	*/
    }

    public Segment clip(HalfSpace hs)
    {
	boolean pinh = hs.s.rightOf(p);
	boolean qinh = hs.s.rightOf(q);

	if(!pinh && !qinh)
	    return null; // segment clips out
	else if(pinh && qinh) 
	    return this; // segment totally in halfspace
	else {
	    //	    System.out.println("pinh "+pinh+" qinh " +qinh);
	    Vertex i = h.intersect(hs);
	    // segment must intersect half plane... 
	    if(pinh)
		q = i;
	    else
		p = i;
	    return this;
	}
	    
    }

    public Object clone()
    {
	return new Segment(p, q, ds);
    }

    public Vertex intersect(HalfSpace hs)
    {
	boolean pinh = hs.s.rightOf(p);
	boolean qinh = hs.s.rightOf(q);
	
	if((!pinh && qinh) || (pinh && !qinh))
	    return h.intersect(hs);
	else
	    return null;
    }

    /* returns true if this vector is clockwise to vector p --> n. */
    /* that is, if n, p, q makes a left turn */
    public boolean rightOf(Vertex n) 
    {
 	double det = (q.x-p.x)*(n.y-p.y)-(q.y-p.y)*(n.x-p.x);
	return det >= 0;
    }

    public String toString()
    {
	return "S["+p+","+q+","+h+"]";
    }
}

