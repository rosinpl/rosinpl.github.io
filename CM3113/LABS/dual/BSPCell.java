import java.util.*;
import java.awt.*;

class BSPCell 
{
    public Face f;
    public Segment l;
    public BSPCell parent, leftChild, rightChild;
    private static int idCount = 0;
    public int id;

    public boolean isLeaf()
    {
	return leftChild == null && rightChild == null;
    }

    public BSPCell(BSPCell p, Face f)
    {
	parent = p;
	this.f = f;
	l = null;
	parent = null;
	leftChild = rightChild = null;
	id = idCount++;
    }
    
    public BSPCell(int width, int height)
    {
	this(null, new Face(0, 0, width, height));
    }

    public BSPCell(double x, double y, double width, double height)
    {
	this(null, new Face(x, y, width, height));
    }

    public void draw(Graphics g, Color clr)
    {
	f.draw(g, clr);
    }

    public void draw(Graphics g, Color clr, DrawingArea d)
    {
	f.draw(g, clr, d);
    }

    public static void insert(BSPCell b, Segment s)
    {
	//System.out.println("inserting "+s);
	Segment clippedSeg = b.f.clip(s);
	if(clippedSeg == null)
	    return;
	if(b.isLeaf())
	    b.split(clippedSeg, s.ds);
	else {
	    Segment clone = clippedSeg;
	    if(b.leftChild != null && b.rightChild != null)
		clone = (Segment) clippedSeg.clone();
	    if(b.leftChild != null)
		insert(b.leftChild, clippedSeg);
	    if(b.rightChild != null)
		insert(b.rightChild, clone);
	}
    }

    public void split(Segment s, DualitySegment ds)
    {
	l = s;
	Face[] newFaces = f.split(s.h, ds);

	leftChild  = new BSPCell(this, newFaces[0]);
	rightChild = new BSPCell(this, newFaces[1]);
	
	/*
	  if(s.q.equals(newFaces[1].head.s.p)) {
	  leftChild  = new BSPCell(this, newFaces[0]);
	  rightChild = new BSPCell(this, newFaces[1]);
	  }
	  else {
	  leftChild  = new BSPCell(this, newFaces[1]);
	  rightChild = new BSPCell(this, newFaces[0]);
	  }
	*/
    }

    /* assumes query point is within initial bounding box */
    public static BSPCell Locate(BSPCell c, Vertex p)
    {
	if(c.isLeaf()) 
	    return c;

	if(c.l.h.inPosPlane(p))
	    return Locate(c.leftChild, p);
	else
	    return Locate(c.rightChild, p);
    }

    private static int order;

    public static void resetOrder()
    {
	order = 1;
    }
    
    /* FIXME: The returned Vector is convenient for debugging and 
       is completely not necessary */
    public static Vector Traverse(Graphics g, BSPCell c, Vertex p)
    {
	if(c.isLeaf())
	    return new Vector();

	BSPCell first  = c.rightChild;
	BSPCell second = c.leftChild;

	if(!c.l.h.inPosPlane(p)) {
	    first  = c.leftChild;
	    second = c.rightChild;
	}

	Vector v = Traverse(g, first, p);
	if(g != null) {
	    /* draw this splitting line */
	    c.l.draw(g, Color.black, order);
	    order++;
	}
	Vector w = Traverse(g, second, p);

	v.addElement(c.l);
	for(int i = 0; i < w.size(); i++)
	    v.addElement(w.elementAt(i));

	return v;
    }

    public String toString() 
    {
	return "BSP["+id+","+l+","+f+",\n"+leftChild+",\n"+rightChild+"]";
    }
    
    /* main() to test data structure */
    public static void main(String[] args)
    {
	BSPCell root = new BSPCell(800, 410);
	System.out.println("Dump: "+root);

	BSPCell.insert(root, new Segment(new Vertex(347, 250),
					 new Vertex(534, 321)));

	System.out.println("BSP Dump:\n"+root);
	/*
	  BSPCell.insert(root, new Segment(new Node(90, 20),
	  new Node(30, 140)));
	  
	  System.out.println("BSP Dump:\n"+root);
	  
	  Node q = new Node(40, 50);
	  BSPCell locate = BSPCell.Locate(root, q);
	  System.out.println("Query "+q+" is in cell "+locate.id);
	  
	  Vector render  = BSPCell.Traverse(null, root, q);
	  System.out.println("  Redner:");
	  for(int i = 0; i < render.size(); i++)
	  System.out.println("    "+render.elementAt(i));

	*/

	/*	  
	q = new Node(40, 30);
	locate = BSPCell.Locate(root, q);
	System.out.println("Query "+q+" is in cell "+locate.id);

	q = new Node(80, 30);
	locate = BSPCell.Locate(root, q);
	System.out.println("Query "+q+" is in cell "+locate.id);

	q = new Node(80, 60);
	locate = BSPCell.Locate(root, q);
	System.out.println("Query "+q+" is in cell "+locate.id);

	q = new Node(80, 150);
	locate = BSPCell.Locate(root, q);
	System.out.println("Query "+q+" is in cell "+locate.id);
	*/
    }
}



