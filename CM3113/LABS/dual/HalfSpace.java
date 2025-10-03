import java.awt.*;

class HalfSpace 
{
    public double a, b, c;
    public Segment s;

    public HalfSpace()
    {
	s = null;
    }

    public HalfSpace(double slope, double yint)
    {
	a = -slope;
	b = 1;
	c = -yint;
    }

    public HalfSpace(double px, double py, double qx, double qy)
    {
	set(px, py, qx, qy);
    }

    public void set(double px, double py, double qx, double qy)
    {
	a = qy - py;
	b = px - qx;
	c = -0.5*(a*(px + qx) + b*(py + qy));
    }

    public HalfSpace(Segment s, double a, double b, double c)
    {
	this.s = s;
	this.a = a;
	this.b = b;
	this.c = c;
    }

    public void draw(Graphics g, Color clr)
    {
	return; // FIXME: figure out how to draw this later
    }

    public boolean inPosPlane(Vertex n)
    {
	boolean result = a*n.x + b*n.y + c > 0;
	//	System.out.println("inPosPlane: "+n+" "+result);
	return result;
    }

    public Vertex intersect(HalfSpace hs)
    {
	double det = a*hs.b - b*hs.a;

	/*
	  Util.assert(Math.abs(det) > 0.0000001, 
	  "HalfSpace::intersect() "+this+" "+hs);
	*/
	double x = (b*hs.c - hs.b*c)/det;
	double y = (c*hs.a - a*hs.c)/det;
	return new Vertex(x, y);
    }

    public String toString()
    {
	return "HS["+a+","+b+","+c+"]";
    }
}
