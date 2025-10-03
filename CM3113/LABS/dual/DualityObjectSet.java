import java.awt.*;
import java.util.*;

abstract class DualityObjectSet extends DualityObject
{
    protected Vector set;

    public DualityObjectSet(DrawingArea d)
    {
	super(d);
	set = new Vector();
    }

    public boolean onBoundary(double x, double y)
    {
	for(int i = 0; i < set.size(); i++) {
	    if(((DualityObject) set.elementAt(i)).onBoundary(x,y))
		return true;
	}
	return false;
    }

    public void draw(Graphics g, Vector secondPass)
    {
	for(int i = 0; i < set.size(); i++)
	    ((DualityObject) set.elementAt(i)).draw(g, secondPass);
    }

    public void mouseOver(double x, double y, int modifier)
    {
	setState(HIGHLIGHT3);
	for(int i = 0; i < set.size(); i++) {
	    DualityObject obj = (DualityObject) set.elementAt(i);
	    obj.mouseOver(x, y, modifier);
	    obj.setState(HIGHLIGHT3);
	}
    }

    public void setState(int i)
    { 
	state = i;
	for(int c = 0; c < set.size(); c++)
	    ((DualityObject) set.elementAt(c)).setState(i);
	if(dual != null)
	    dual.state = i;
    }

    public String toString(String className)
    {
	String result = className+": ";
	for(int i = 0; i < set.size(); i++) {
	    result += "\n  "+set.elementAt(i).toString();
	}
	return result;
    }
}

