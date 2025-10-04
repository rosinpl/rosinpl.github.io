import java.awt.*;
import java.util.*;

abstract class DualityObject
{
    protected DualityObject dual;
    protected int state;
    protected DrawingArea drawArea;

    public static final int NORMAL    = 0;
    public static final int HIGHLIGHT = 1;
    public static final int HIGHLIGHT2= 2;
    public static final int HIGHLIGHT3= 3;
    public static final int HIGHLIGHT4= 4;
    public static final int HIGHLIGHT5= 5;
    public static final int SELECTED  = 6;

    abstract public boolean onBoundary(double x, double y);
    abstract public void draw(Graphics g, Vector secondPass);

    public void mousePressed(double x, double y, int modifier) 
    { 
	setState(SELECTED);
    }

    public void mouseDragged(double x, double y, Vertex anchor, int modifier) { return; }

    public DualityObject(DrawingArea d)
    {
	drawArea = d;
    }

    public void mouseOver(double x, double y, int modifier)
    {
	setState(HIGHLIGHT);
    }

    /* change states */
    public void setState(int i)
    { 
	state = i;
	if(dual != null)
	    dual.state = i;
    }

    public DualityObject dual()
    {
	return dual;
    }
    
    /* BSP */
    public void insertBSP(BSPCell root)
    {
	return;
    }

    abstract public String toString();


    protected void setColor(Graphics g)
    {
	switch(state) {
	case NORMAL:
	    g.setColor(Color.white); break;
	case HIGHLIGHT:
	    g.setColor(Color.blue); break;
	case HIGHLIGHT2:
	    g.setColor(Color.yellow); break;
	case HIGHLIGHT3:
	    g.setColor(Color.cyan); break;
	case HIGHLIGHT4:
	    g.setColor(Color.pink); break;
	case HIGHLIGHT5:
	    g.setColor(Color.green); break;
	case SELECTED:
	    g.setColor(Color.red); break;
	default:
	    Util.assert(false, "DualityObject::setColor");
	}
    }    
    
    public void exportObjects(Vector primalList, Vector dualList)
    {
	primalList.addElement(this);
	dualList.addElement(dual);
    }
}
