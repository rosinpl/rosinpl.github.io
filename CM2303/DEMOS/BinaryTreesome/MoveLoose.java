// ---------------------------------- Imports ---------------------------------




import java.awt.*;
import java.util.Stack;




// ------------------------------- Start of class -----------------------------




/**
* A thread class that moves a node down through the tree to
* the position where it is to be inserted.
* The thread moves the node pixel by pixel from one node to another.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class MoveLoose extends Thread
{
    // ----- Variables ----

    /** The node to start from.
    */
    private Node root           = null;

    /** The node to be moved.
    */
    private Node node           = null;

    /** The tree where the node should be inserted into.
    */
    private BinaryTreesome main = null;

    /** The panel where the node should be painted.
    */
    private CenterPanel center  = null;

    /** The panel to output messages in.
    */
    private BottomPanel bottom  = null;




    // ---------------------------- Constructors ------------------------------




    /** Constructs a thread to move a node.
    *
    * @param r the node to start from
    * @param n the node that is to be moved
    * @param m the main applet class
    * @param c the panel in which the node is to be painted
    * @param b the panel in which to output messages
    * @see Node
    * @see BinaryTreesome
    * @see CenterPanel
    * @see BottomPanel
    */
    public MoveLoose(Node r, Node n, BinaryTreesome m, CenterPanel c, BottomPanel b)
    {
        root    = r;
        node    = n;

        main    = m;
        center  = c;
        bottom  = b;
    }




    // -------------------- Automatically called functions --------------------




    /** Controls the movement of the node.
    * Decides if the node should move left or right when
    * moving down through the tree. The actual moving is done
    * by calling the move function.
    *
    * @see MoveLoose#move
    */
    public synchronized void run()
    {
        ((center.getTree()).getTop()).readyForInsertionInput(false);

        Node n = root;

        node.setType(Node.SELECTED);
        node.setDestination(n.getCenter());
        move();

        while(!(n.getType()).equals(Node.SHADOW))
        {
            if ((n.getLeft() != null) && (Integer.parseInt(node.getValue()) < Integer.parseInt(n.getValue())))
            {
                n = n.getLeft();
                node.setDestination(n.getCenter());
                move();
            }
         
            else
            {
                n = n.getRight();
                node.setDestination(n.getCenter()); 
                move();
            }
            
            center.repaint();
        }
        
        if (!(n.isFront()))
        {
            n.setFront();
            (n.getNeighbour()).setBack();
        }

        bottom.writeError("");
        ((center.getTree()).getTop()).readyForInsertionInput(true);

        if ((main.getChosenTree()).equals(BinaryTreesome.treeType[2]))
            ((SortedTree)(center.getTree())).insertSelectedNode(n);
        else if ((main.getChosenTree()).equals(BinaryTreesome.treeType[3]))
            ((AVLTree)(center.getTree())).insertSelectedNode(n);
        else if ((main.getChosenTree()).equals(BinaryTreesome.treeType[4]))
            ((AATree)(center.getTree())).insertSelectedNode(n);
        else if ((main.getChosenTree()).equals(BinaryTreesome.treeType[5]))
            ((SplayTree)(center.getTree())).insertSelectedNode(n);
        else if ((main.getChosenTree()).equals(BinaryTreesome.treeType[6]))
            ((RedBlackTree)(center.getTree())).insertSelectedNode(n);

        if ((center.getTree()).hasMoreNumbersToBeInserted())
            ((center.getTree()).getTop()).setChangeButton();
        else
            ((center.getTree()).getTop()).setRandomButton();
    }




    // -------------------------- Other functions -----------------------------




    /** Takes care of the actual moving of the node.
    * The node is moved pixel by pixel from it's current
    * position to it's destination, both defined whithin the node.
    *
    * @see Node#center
    * @see Node#destination
    */
    public synchronized void move()
    {
        // Calculate the maximum distance in either direction
        int width = ((node.getDestination()).x - node.getX());
        int pos_width = width<0?((-1)*(width)):width;
        int height= ((node.getDestination()).y - node.getY());
        int pos_height= height<0?((-1)*(height)):height;
        int maxDistance = (pos_width)>(pos_height)?(pos_width):(pos_height);

        // Calculate steps in either direction (in pixels)
        float fx = (((float)width)/((float)maxDistance));
        float fy = (((float)height)/((float)maxDistance));

        // Get the coordinates of the center when started
        float x = node.getX();
        float y = node.getY();

        for (int i=0; i<maxDistance; i++)
        {
            // Update the coordinates and repaint
            x += fx;
            y += fy;
            node.setCenter((int)x,(int)y);
            center.repaint();
            try
            {
                sleep(5);
            }
            catch(InterruptedException e)
            {
                stop();
            }
        }

        // Set the current center to destination at the end to make sure the
        // noder actually reached the exact destination point
        node.setCenter(node.getDestination());
        center.repaint();
    }
}




// -------------------------------- End of class ------------------------------

