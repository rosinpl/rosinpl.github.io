import java.awt.*;

public class Vertex
{
    public static final double TOLERANCE = DualityVertex.TOLERANCE;

    public double x;
    public double y;
    
    public Vertex(double x, double y) 
    {
	this.x = x; this.y = y;
    }

    public boolean inBound(double x, double y)
    {
	return
	    Math.abs(this.x - x) < TOLERANCE &&
	    Math.abs(this.y - y) < TOLERANCE;
    }

    public void draw(Graphics g, DrawingArea drawArea) 
    {
	Vertex v = drawArea.transform(x, y);
	
	g.fillOval(Math.round((float)(v.x - DrawingArea.nodeSizeDiv2)), 
		   Math.round((float)(v.y - DrawingArea.nodeSizeDiv2)), 
		   DrawingArea.nodeSize, 
		   DrawingArea.nodeSize);
    }

    public String toString()
    {
	return "("+x+","+y+")";
    }

    public double distSqr(Vertex v)
    {
	return (x - v.x)*(x - v.x) + (y - v.y)*(y - v.y);
    }

    public static double dotProduct(double px, double py, double qx, double qy)
    {
	return px*qx + py*qy;
    }
}
