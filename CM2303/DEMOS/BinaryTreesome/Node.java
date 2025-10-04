// ---------------------------------- Imports ---------------------------------




import java.awt.*;




// ------------------------------- Start of class -----------------------------




/**
* A class representing a node.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class Node
{
    // ----- Constants ----

    /** The radius of the nodes.
    */
    public static final int RADIUS      = 9;

    //The different types of nodes
    /** The root node type.
    */
    public static final String ROOT     = "root";

    /** The normal node type.
    */
    public static final String NORMAL   = "normal";

    /** The shadow node type.
    */
    public static final String SHADOW   = "shadow";

    /** The selected node type.
    */
    public static final String SELECTED = "select";

    /** The loose node type.
    */
    public static final String LOOSE    = "loose";

    /** The default node type.
    */
    public static final String DEFAULT  = "default";


    // The value of all the shadow-nodes that are created

    /** The value of a shadow node.
    */
    public static final String SHADOW_VALUE = "";

    // ----- Variables ----

    /** The lowest level of any node at any time.
    */
    private static int lowestLevel      = 0;

    /** Node's current center.
    */
    private Point center        = null;

    /** Node's destination center.
    */
    private Point destination   = null;

    /** Value of node.
    */
    private String value        = null;

    /** Graphic interface of node.
    */
    private String type         = null;

    /** The level of the node.
    */
    private int level           = 0;

    /** The AVL balance of the node.
    */
    private int AVLBalance      = -1;

    /** The AA level of the node.
    */
    private int AALevel         = 0;

    /** The height of the node's subtree.
    */
    private int height          = 0;

    /** Flag - Node is in front (in case of overlapping).
    */
    private boolean front       = true;

    /** Flag - Can't drag node.
    */
    private boolean locked      = false;

    /** "Pointer" to the node's parent.
    */
    private Node parent         = null;

    /** "Pointer" to the node's left child.
    */
    private Node l_child        = null;

    /** "Pointer" to the node's right child.
    */
    private Node r_child        = null;

    /** "Pointer" to the node's neighbour.
    */
    private Node neighbour      = null;

    /** "Pointer" to the thread that moves this node.
    */
    private MoveNode nodeThread = null;




    // ---------------------------- Constructors ------------------------------




    /** Constructs a node with default values.
    * This constructor calls another constructor with
    * default values for all the parameters.
    */
    public Node(){this(new Point(0,0),SHADOW_VALUE,-1,SHADOW,0,true,false,null,null,null,null);}




    /** Constructs a node with the specified values.
    *
    * @param center the point representing the center of the node
    * @param value the number which is to be the value of the node
    * @param type what type of node it is going to be
    * @param level the level of the tree which the node is in
    * @param front tells if the node is to be painted in front or not
    * @param locked tells if the node should be locked in place or not
    * @param parent pointer to the nodes parent in the tree
    * @param left pointer to the nodes left child in the tree
    * @param right pointer to the nodes right child in the tree
    * @param neighbour pointer to the nodes neighbour in the tree
    * @see Node#center
    * @see Node#value
    * @see Node#type
    * @see Node#level
    * @see Node#front
    * @see Node#locked
    * @see Node#parent
    * @see Node#l_child
    * @see Node#r_child
    * @see Node#neighbour
    */
    public Node(Point center, String value, int height, String type, int level, boolean front, 
                boolean locked, Node parent, Node left, Node right, Node neighbour)
    {
        // Initialize variables
        this.center     = new Point(center);
        this.destination= new Point(center);
        this.value      = new String(value);
        this.type       = new String(type);
        this.level      = level;
        this.height     = height;
        this.front      = front;
        this.locked     = locked;
        this.parent     = parent;
        this.l_child    = left;
        this.r_child    = right;
        this.neighbour  = neighbour;
    }




    // -------------------------- Other functions -----------------------------




    /**  Checks if a point is inside the node.
    *
    * @param p the point that is to be checked
    * @return true if the point was inside the node, false otherwise
    */
    public boolean contains(Point p)
    {
        double dist = (center.x-p.x)*(center.x-p.x) +
                      (center.y-p.y)*(center.y-p.y);
        int d = (int)Math.abs(Math.sqrt(dist));

        // True if mouse is close enough to node, select it
    	return (d <= RADIUS);
    }





    /** Returns the node's AA-balance factor.
    *
    * @see Node#AALevel
    */
    public int getAALevel(){return AALevel;}





    /** Returns the node's AVL-balance factor.
    *
    * @see Node#AVLBalance
    */
    public int getAVLBalance(){return AVLBalance;}




    /** Returns the node's current center.
    *
    * @see Node#center
    */
    public Point getCenter(){return center;}




    /** Returns the node's destination.
    *
    * @see Node#destination
    */
    public Point getDestination(){return destination;}




    /** Returns the node's height.
    *
    * @see Node#height
    */
    public int getHeight(){return height;}




    /** Returns the node's left child.
    *
    * @see Node#l_child
    */
    public Node getLeft(){return l_child;}




    /** Returns the node's level.
    *
    * @see Node#level
    */
    public int getLevel(){return level;}




    /** Returns the lowest level of any node.
    *
    * @see Node#lowestLevel
    */
    public static int getLowestLevel(){return lowestLevel;}




    /** Returns the node's neighbour.
    *
    * @see Node#neighbour
    */
    public Node getNeighbour(){return neighbour;}




    /** Returns the node's parent.
    *
    * @see Node#parent
    */
    public Node getParent(){return parent;}




    /** Returns the node's right child.
    *
    * @see Node#r_child
    */
    public Node getRight(){return r_child;}




    /** Returns the node's thread.
    *
    * @see Node#nodeThread
    */
    public MoveNode getThread(){return nodeThread;}




    /** Returns the node's type.
    *
    * @see Node#type
    */
    public String getType(){return type;}




    /** Returns the node's value.
    *
    * @see Node#value
    */
    public String getValue(){return value;}




    /** Returns the x coordinate of the node's center.
    *
    * @see Node#center
    */
    public int getX(){return center.x;}




    /** Returns the y coordinate of the node's center.
    *
    * @see Node#center
    */
    public int getY(){return center.y;}




    /** Returns true if the node has a thread.
    *
    * @see Node#nodeThread
    */
    public boolean hasThread(){return (nodeThread == null)?false:true;}




    /** Returns true if the node is in front (not behind another node).
    *
    * @see Node#front
    */
    public boolean isFront(){return front;}




    /** Returns true if the node is locked.
    * When a node is locked it cannot be dragged.
    *
    * @see Node#locked
    */
    public boolean isLocked(){return locked;}




    /** Locks the node.
    * When a node is locked it cannot be dragged.
    *
    * @see Node#locked
    */
    public void lock(){locked = true;}




    /** Checks if one node overlaps another.
    * Checks and returns true if this node is close enough to the node that is
    * sent to the function (intuitively, this should be when overlapped).
    *
    * @param n the node that is to be checked against
    */
    public boolean overlaps(Node n)
    {
        // The hypotenus of the triangle between the two centers
        double dist = (center.x-n.getX())*(center.x-n.getX()) +
                      (center.y-n.getY())*(center.y-n.getY());

        // The distance between the two centers
        int d = (int)Math.abs(Math.sqrt(dist));

        // The distance is less than the minimum distance between the nodes
        // (as set in the static variable "nodeDistance" in class "TreeTemplate")
    	return (d <= RADIUS+(TreeTemplate.nodeDistance/2));
    }




    /** Sets the node's AA-balance factor.
    *
    * @see Node#AALevel
    */
    public void setAALevel(int b){AALevel = b;}
    
    
    
    
    /** Sets the node's AVL-balance factor.
    *
    * @see Node#AVLBalance
    */
    public void setAVLBalance(int b){AVLBalance = b;}
    
    
    
    
    /** Sets the node to back.
    * The private variable front is set to false.
    * It is important to remember to put it's neighbour to front.
    *
    * @see Node#front
    */
    public void setBack(){front = false;}




    /** Sets the node's center.
    *
    * @param x the x coordinate of the node's new center
    * @param y the y coordinate of the node's new center
    * @see Node#center
    */
    public void setCenter(int x, int y){center = new Point(x,y);}




    /** Sets the node's center.
    *
    * @param c the point that is to be the node's new center
    * @see Node#center
    */
    public void setCenter(Point c){center = c;}




    /** Sets the node's destination.
    *
    * @param x the x coordinate of the node's new destination
    * @param y the y coordinate of the node's new destination
    * @see Node#destination
    */
    public void setDestination(int x, int y){destination = new Point(x,y);}




    /** Sets the node's destination.
    *
    * @param c the point that is to be the node's new destination
    * @see Node#destination
    */
    public void setDestination(Point c){destination = c;}




    /** Sets the node to front.
    * The private variable front is set to true.
    * It is important to remember to put it's neighbour to back.
    *
    * @see Node#front
    */
    public void setFront(){front = true;}




    /** Sets the height node's subtree.
    *
    * @param n the height
    * @see Node#height
    */
    public void setHeight(int h){height = h;}




    /** Sets the node's left child.
    *
    * @param n the node that is to be this node's left child
    * @see Node#l_child
    */
    public void setLeft(Node n){l_child = n;}




    /** Sets the node's level.
    *
    * @param l the new level of this node
    * @see Node#level
    */
    public void setLevel(int l){level = l;}




    /** Sets the lowest level in the tree.
    * Updates the private variable lowestLevel.
    *
    * @param l the trees new lowest level
    * @see Node#lowestLevel
    */
    public static void setLowestLevel(int l){lowestLevel = l;}




    /** Sets the node's neighbour.
    *
    * @param n the node that is to be this node's neighbour
    * @see Node#neighbour
    */
    public void setNeighbour(Node n){neighbour = n;}




    /** Sets the node's neighbour, and the neighbour's neighbour to this node.
    * Updates this node's private variable neigbour. Sets the neigbour's
    * private variable neighbour by calling it's setNeigbour function.
    *
    * @param n the node that is to be this node's neighbour
    * @see Node#neighbour
    */
    public void setNeighbours(Node n){neighbour = n; n.setNeighbour(this);}




    /** Sets the node's parent.
    *
    * @param n the node that is to be this node's parent
    * @see Node#parent
    */
    public void setParent(Node n){parent = n;}




    /** Sets the node's right child.
    *
    * @param n the node that is to be this node's right child
    * @see Node#r_child
    */
    public void setRight(Node n){r_child = n;}




    /** Sets the node's thread.
    * The variable nodeThread should be set to null if there
    * isn't a thread working on the node (moving it).
    *
    * @param t the thread that is to be working on this node
    * @see Node#nodeThread
    */
    public void setThread(MoveNode t){nodeThread = t;}




    /** Sets the node's type.
    *
    * @param t the type this node is giong to be
    * @see Node#type
    */
    public void setType(String t){type = t;}




    /** Sets the node's value.
    *
    * @param v the value this node is going to have
    * @see Node#value
    */
    public void setValue(String v){value = v;}




    /** Sets the x coordinate of the node's center.
    *
    * @param x this node's new x coordinate
    * @see Node#center
    */
    public void setX(int x){center.x = x;}




    /** Sets the y coordinate of the node's center.
    *
    * @param y this node's new y coordinate
    * @see Node#center
    */
    public void setY(int y){center.y = y;}




    /** Unlocks the node.
    * When a node is locked it cannot be dragged.
    *
    * @see Node#locked
    */
    public void unlock(){locked = false;}
}




// -------------------------------- End of class ------------------------------

