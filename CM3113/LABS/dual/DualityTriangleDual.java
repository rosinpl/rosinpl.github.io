import java.awt.*;
import java.util.*;

class DualityTriangleDual extends DualityObjectSet
{

    public DualityTriangleDual(DrawingArea d)
    {
	super(d);
    }

    public void mouseDragged(double x, double y, Vertex anchor, int modifier) 
    { 
	return; // disable edit in the dual plane
    }

    public void insertBSP(BSPCell root)
    {
	return; // forget about this... not sure if it's required
    }

    public String toString()
    {
	return super.toString("DualityTriangleDual");
    }
}
