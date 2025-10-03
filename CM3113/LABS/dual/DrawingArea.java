import java.awt.*;
import java.awt.event.*;
import java.util.*;

class DrawingArea 
    extends Canvas 
    implements MouseListener, MouseMotionListener {

    // modes
    public static final int INSERT = 0;
    public static final int QUERY  = 1;
    public static final int EDIT   = 2;
    // input method
    public static final int POINT  = 10;
    public static final int SEG    = 11;
    public static final int TRIANG = 12;
    public static final int CIRCLE = 13;

    private int mode;
    private int method;

    public void setMode(int m) 
    { 
	mode = m; 
	if(mode == INSERT) {
	    queryObject = null;
	    locatedCell = null;
	    dualTri     = null;
	    if(dual != null) {
		dual.queryObject = null;
		locatedCell = null;
	    }
	}
	repaintBothPlanes();
    }
    public void setMethod(int m) { method = m; }

    private BSPCell root;

    private Node prevMousePos, selectPos, anchorPoint;
    private Image   img;
    private Graphics gc;

    public static final int nodeSize = 8; // assume this to be even
    public static final int nodeSizeDiv2 = nodeSize/2;

    /* transform stuff */
    public static Vertex minCoord, maxCoord, translate;
    public double scaleX, scaleY;

    DualityObject currentObjectUnderMouse;
    DualityObject currentObjectSelectMouse;
    DualityObject queryObject;

    public DrawingArea(int width, boolean p) 
    {
	super();
	preferredSize = new Dimension(width, width);
	setBackground(Color.gray.darker());
	addMouseListener(this);
	addMouseMotionListener(this);

	translate = new Vertex(width/2.0, width/2.0);
	minCoord = new Vertex(-3, -3);
	maxCoord = new Vertex(3, 3);
	
	scaleX = width/(maxCoord.x-minCoord.x);
	scaleY = -width/(maxCoord.y-minCoord.y);

	isPrimal = p;

	dualityObjects = new Vector();
	//initialize();
    }

    Dimension preferredSize;
    public Dimension getPreferredSize() 
    {
	return preferredSize;
    }

    // Initialize data structure
    public void initialize(DrawingArea d) 
    {
	dual = d;
	setEnabled(true);
	anchorPoint  = new Node(-10, -10);
	prevMousePos = new Node(-10, -10);
	selectPos    = new Node(-10, -10);

	currentObjectUnderMouse = null;
	currentObjectSelectMouse = null;
	queryObject = null;

	root = null;
	locatedCell = null;
	dualityObjects.removeAllElements();
	
	img = createImage(getPreferredSize().width, 
			  getPreferredSize().height);
	gc  = img.getGraphics();

	repaintBothPlanes();
    }

    DualityTriangle dualTri = null;

    public void mousePressed(MouseEvent e) {
	int x        = e.getX();
	int y        = e.getY();
	int modifier = e.getModifiers();

	Util.dbgPrintln("mpressed: "+modifier+" "+MouseEvent.BUTTON3_MASK+
			"at (" +x+","+y+").");

	// SHOULD ALWAYS USE ONE MOUSE BUTTON!!! IT'S TRICKY TO HANDLE
	// MOUSERELEASE EVENT WHEN TWO BUTTONS ARE PUSHED BY ACCIDENT
	if ((modifier & MouseEvent.BUTTON3_MASK) > 0)
	    return;
	
	// checking MouseEvent.BUTTON1_MASK doesn't work for netscape,
	// so check for button3 and reverse the if-condition claues instead
	anchorPoint.setLocation(x, y);
	prevMousePos.setLocation(x, y); 
	selectPos.setLocation(x, y);

	Vertex v = invTransformStatic(x, y);

	DualityObject obj = null;
	if(mode == INSERT) {// && currentObjectUnderMouse == null) {

	    switch(method) {
	    case SEG: 
		obj = new DualitySegment(v.x, v.y, v.x, v.y, this);
		((DualitySegment) obj).mousePressed(v.x, v.y, modifier, false, true);
		break;
	    case POINT:
		obj = new DualityVertex(v.x, v.y, this); 
		obj.mousePressed(v.x, v.y, modifier);
		break;
	    default:
		Util.assert(false, "mousePressed() invalid method "+method);
	    }
      	}
	else if(mode == EDIT) {
	    if(currentObjectUnderMouse != null) {
		currentObjectUnderMouse.mousePressed(v.x, v.y, modifier);
		currentObjectSelectMouse = currentObjectUnderMouse;
	    }
	}
	else {
	    if(currentObjectUnderMouse != null) {
		// select primal segments, find set of primal lines 
		// that stabs all selected segments, display in dual
		if(currentObjectUnderMouse != queryObject &&
		   currentObjectUnderMouse instanceof DualitySegment) {
		    DualitySegment ds = (DualitySegment) currentObjectUnderMouse;
		    if(!ds.isLine) {
			if(queryObject instanceof DualitySegmentSet) 
			    ((DualitySegmentSet) queryObject).addSegment(ds);
			else {
			    queryObject = new DualitySegmentSet(ds, this);
			    dual.queryObject = queryObject.dual;
			}
			currentObjectUnderMouse = queryObject;
			currentObjectUnderMouse.mousePressed(v.x, v.y, modifier);
			currentObjectSelectMouse = currentObjectUnderMouse;
		    }
		}
		else if(method == TRIANG) {
		    if(dualTri != null && dualTri.numVertices() == 3) {
			dualTri.mousePressed(v.x, v.y, modifier);
			dualTri = null; // finished
		    }
		    else
			dualTri.mousePressed(v.x, v.y, modifier);
		}
	    }
	    else {
		// Primal line query --> dual BSP cell --> dual lines -->
		// primal points
		switch(method) {
		case SEG:
		    queryObject = new DualitySegment(new Vertex(0,v.y), v, this);
		    dual.queryObject = queryObject.dual;
		    currentObjectUnderMouse = queryObject;
		    currentObjectUnderMouse.mousePressed(v.x, v.y, modifier);
		    currentObjectSelectMouse = currentObjectUnderMouse;
		    break;
		case POINT:
		    // Primal point query --> Zone --> lines in Primal
		    break;
		case TRIANG:
		    obj = dualTri = new DualityTriangle(v.x, v.y, this);
		    dualTri.mousePressed(v.x, v.y, modifier);
		    break;
		case CIRCLE:
		    obj = new DualityCircle(v.x, v.y, this); 
		    obj.mousePressed(v.x, v.y, modifier);		
		    break;
		default:
		}
	    }
	}
	
	if(obj != null) {
	    obj.exportObjects(dualityObjects, dual.dualityObjects);
	    currentObjectUnderMouse = obj;
	    currentObjectSelectMouse = currentObjectUnderMouse;
	}

	updateQuery();
	repaintBothPlanes();
    }

    public BSPCell locatedCell;

    public void BSPLocate(DualityVertex v) 
    {
	BSPCell cell = BSPCell.Locate(root, v.p);
	Util.assert(cell.isLeaf(), "DrawingArea::BSPLocate()");
	SegmentList segIter = cell.f.head;
	//System.out.println("bsp "+cell);
	findAndResetObject(0, 0); // reset states

	while(segIter != null) {
	    if(segIter.s.ds != null) {
		//System.out.println("bsp found seg: "+segIter.s.ds);
		segIter.s.ds.setState(DualityObject.HIGHLIGHT4);
	    }
	    // FIXME fill cells by making cell global and have paint draw it
	    segIter = segIter.next;
	}
	locatedCell = cell;
    }

    private boolean isVerticalDisplacement(int x, int y)
    {
	Util.assert(selectPos != null, 
		    "DrawingArea::isVerticalDisplacement");
	double slope = (double) (x - selectPos.x)/(y - selectPos.y);
	return Math.abs(slope) >= 1.0;
    }

    public void mouseDragged(MouseEvent e) 
    {
	int x        = e.getX();
	int y        = e.getY();
	int modifier = e.getModifiers();

	/* to restrict displacement to either HORIZONTAL or VERTICAL */
	if ((modifier & MouseEvent.ALT_MASK) > 0) {
	    if(isVerticalDisplacement(x, y))
		y = selectPos.y;
	    else
		x = selectPos.x;
	}

	int xx = Math.max(Math.min(x, getPreferredSize().width-nodeSizeDiv2), nodeSizeDiv2);
	int yy = Math.max(Math.min(y, getPreferredSize().height-nodeSizeDiv2), nodeSizeDiv2);

	Util.assert(prevMousePos != null, "DrawingArea::mouseDragged()");
	Util.dbgPrintln("Dragging: (" + xx + ", " + yy + ")");
	prevMousePos.setLocation(xx, yy);

	//if(currentObjectUnderMouse != null && !insertMode) {
	Vertex v = invTransformStatic(xx, yy);
	Vertex anchor = invTransform(anchorPoint.x, anchorPoint.y);
	    
	if(currentObjectSelectMouse != null) {
	    v.x = (v.x > minCoord.x)? v.x : minCoord.x;
	    v.y = (v.y > minCoord.y)? v.y : minCoord.y;
	    v.x = (v.x < maxCoord.x)? v.x : maxCoord.x;
	    v.y = (v.y < maxCoord.y)? v.y : maxCoord.y;

	    currentObjectSelectMouse.mouseDragged(v.x, v.y, anchor, modifier);
	    updateQuery();
	    repaintBothPlanes();
	}	
    }

    private void updateQuery()
    {
	if(queryObject instanceof DualitySegment) {
	    dual.updateBSPTree();
	    dual.BSPLocate((DualityVertex) dual.queryObject);
	}
	else if(queryObject instanceof DualityVertex) {
	    updateBSPTree();
	    BSPLocate((DualityVertex) queryObject);
	}
	else if(queryObject instanceof DualitySegmentSet) {
	    dual.locatedCell = null;

	    // find the set of dual vertices that is in the intersection of
	    // all dual wedges and highlight them
	    Vector dualVertices =
		((DualityWedgeSet) dual.queryObject).query(dual.dualityObjects);
	    for(int i = 0; i < dualVertices.size(); i++) {
		DualityVertex dv = (DualityVertex) dualVertices.elementAt(i);
		dv.setState(DualityObject.HIGHLIGHT5);
	    }
	}
    }

    public void mouseReleased(MouseEvent e) 
    {
	int x        = e.getX();
	int y        = e.getY();
	int modifier = e.getModifiers();

	Util.dbgPrintln("mouseReleased "+x+" "+y);
	// SHOULD ALWAYS USE ONE MOUSE BUTTON!!! IT'S TRICKY TO HANDLE
	// MOUSERELEASE EVENT WHEN TWO BUTTONS ARE PUSHED BY ACCIDENT
	if ((modifier & MouseEvent.BUTTON3_MASK) > 0)
	    return;

	findAndResetObject(0, 0); // reset states

	anchorPoint.setLocation(-10, -10);
	prevMousePos.setLocation(-10, -10);
	selectPos.setLocation(-10, -10);

	if(dualTri == null)
	    currentObjectSelectMouse = null;
	mouseMoved(e);
    }

    public void mouseClicked(MouseEvent e) { return; }
    public void mouseEntered(MouseEvent e) { return; }
    public void mouseExited(MouseEvent e) 
    { 
	/*
	  currentObjectUnderMouse = null;
	  currentObjectSelectMouse = null;
	  findAndResetObject(0, 0);	
	  repaintBothPlanes();
	*/
    }

    public void mouseMoved(MouseEvent e) 
    { 
	int x        = e.getX();
	int y        = e.getY();
	int modifier = e.getModifiers();

	Util.dbgPrintln("Moving: (" + x + ", " + y + ")");

	Vertex v = invTransformStatic(x, y);

	if(dualTri != null) {
	    // in triangle creation mode
	    dualTri.mouseMoved(v.x, v.y, modifier);
	}
	else {
	    currentObjectUnderMouse = findAndResetObject(v.x, v.y);
	    if(currentObjectUnderMouse != null) {
		//Util.dbgPrintln("found: "+obj);
		currentObjectUnderMouse.mouseOver(v.x, v.y, modifier);
		if(currentObjectUnderMouse == queryObject) {
		    updateQuery();
		    queryObject.setState(DualityObject.HIGHLIGHT4);
		}
	    }
	}
	repaintBothPlanes();
    }
    
    public void update(Graphics g)
    {
	//double buffering to avoid flickering
	if(img != null) {
	    gc = img.getGraphics();
	    paint(gc);
	    g.drawImage(img, 0, 0, this);
	}
    }

    public void paint(Graphics g) 
    {
	super.paint(g);
	g.clearRect(0, 0, 
		    getPreferredSize().width, 
		    getPreferredSize().height);

	drawObjects(g);

	drawGrid(g, 1, (float)0.5, 3, preferredSize.width/2);
	drawGrid(g, 2, 1, 3, preferredSize.width/2);

	if(mode != INSERT) {
	    // dragging
	    g.setColor(Color.yellow);
	    anchorPoint.draw(g, Color.yellow);
	    prevMousePos.draw(g, Color.yellow);
	    g.drawLine(anchorPoint.x, anchorPoint.y,
		       prevMousePos.x, prevMousePos.y);
	}
    }

    public Vertex transform(double x, double y)
    {
	return new Vertex(x*scaleX+translate.x, y*scaleY+translate.y);
    }

    public Vertex invTransform(double x, double y)
    {
	return new Vertex((x-translate.x)/scaleX,(y-translate.y)/scaleY);
    }

    private Vertex transformStatic = new Vertex(0,0);
    public Vertex invTransformStatic(double x, double y)
    {
	 transformStatic.x = (x-translate.x)/scaleX;
	 transformStatic.y = (y-translate.y)/scaleY;
	 return transformStatic;
    }

    /* duality stuff */
    public boolean isPrimal;
    private DrawingArea dual;
    public Vector dualityObjects;

    public DualityObject findAndResetObject(double x, double y)
    {
	DualityObject result = null;
	if(queryObject != null) {
	    if(queryObject.onBoundary(x, y)) {
		result = queryObject;
	    }
	}
	for(int i = 0; i < dualityObjects.size(); i++) {
	    DualityObject obj = (DualityObject) dualityObjects.elementAt(i);
	    // let DualityVertex and DualitySet stuff take precedence
	    if(obj.onBoundary(x, y) && 
	       (result == null || result != queryObject)) {
		if(result == null || obj instanceof DualityVertex)
		    result = obj;
	    }
	    obj.setState(DualityObject.NORMAL);
	}

	if(queryObject != null)
	    queryObject.setState(DualityObject.HIGHLIGHT3);

	return result;
    }

    private Vector secondPass = new Vector();

    public void drawObjects(Graphics g)
    {
	secondPass.removeAllElements();
	for(int i = 0; i < dualityObjects.size(); i++) {
	    DualityObject obj = (DualityObject) dualityObjects.elementAt(i);
	    obj.draw(g, secondPass);
	}
	if(dual != null) {
	    if(dual.currentObjectUnderMouse != null)
		dual.currentObjectUnderMouse.dual().draw(g, secondPass);
	    if(dual.currentObjectSelectMouse != null)
		dual.currentObjectSelectMouse.dual().draw(g, secondPass);
	}
	if(locatedCell != null)
	    locatedCell.draw(g, Color.red.darker().darker(), this);
	if(queryObject != null)
	    queryObject.draw(g, secondPass);
	for(int i = 0; i < secondPass.size(); i++) {
	    DualityObject obj = (DualityObject) secondPass.elementAt(i);
	    obj.draw(g, null); // no more passes...
	}
    }

    public void updateBSPTree()
    {
	root = new BSPCell(-3.0, -3.0, 6.0, 6.0);
	for(int i = 0; i < dualityObjects.size(); i++) {
	    DualityObject obj = (DualityObject) dualityObjects.elementAt(i);
	    if(obj != queryObject)
		obj.insertBSP(root);
	}
    }

    public void repaintBothPlanes()
    {
	repaint();
	if(dual != null)
	    dual.repaint();
    }

    public void drawGrid(Graphics g, int size, float space, 
			  float realWidth, int screenWidth)
    {
	float scale = screenWidth/realWidth;
	int ulTrans = screenWidth-size;

	float i = 0;
	g.setColor(Color.black);
	do {
	    g.drawOval(Math.round(i*scale)+ulTrans, ulTrans, 2*size, 2*size);
	    g.drawOval(Math.round(-i*scale)+ulTrans, ulTrans, 2*size, 2*size);
	    g.drawOval(ulTrans, Math.round(i*scale)+ulTrans, 2*size, 2*size);
	    g.drawOval(ulTrans, Math.round(-i*scale)+ulTrans, 2*size, 2*size);
	    i += space;
	} while(i <= realWidth);	    
    }

}



