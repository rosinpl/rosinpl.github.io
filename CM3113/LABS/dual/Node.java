import java.awt.*;

class Node extends Point 
{
    public Node(int x, int y)
    {
	super(x, y);
    }
    
    public boolean clickedOn(int x, int y, int tolerance)
    {
	Util.assert(false, "Node::clickedOn() is deprecated");
	return 
	    Math.abs(this.x - x) < tolerance &&
	    Math.abs(this.y - y) < tolerance;
    }

    public Object clone()
    {
	return new Node(x, y);
    }

    public void draw(Graphics g, Color clr) 
    {
	g.setColor(clr);
	g.fillOval(x - DrawingArea.nodeSizeDiv2, 
		   y - DrawingArea.nodeSizeDiv2, 
		   DrawingArea.nodeSize, 
		   DrawingArea.nodeSize);
    }
}
