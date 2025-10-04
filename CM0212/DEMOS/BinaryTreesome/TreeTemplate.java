// ---------------------------------- Imports ---------------------------------




import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Stack;
import java.util.Enumeration;
import java.net.URL;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;




// ------------------------------- Start of class -----------------------------




/**
* The tree superclass.
* Holds functions that is used by all the trees that
* inherits this class.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
* @see            TreeTemplate
*/
public abstract class TreeTemplate
implements  ActionListener, KeyListener, MouseListener, MouseMotionListener
{
    // ----- Constants ----

    /** Index of the lowest level in a tree (root is level #0).
    */
    protected static final int MAXLEVEL         = 5;

    /** Horisontal distance between node-edges.
    */
    protected static final int nodeDistance     = 2*Node.RADIUS-2;

    /** Vertical distance between node-levels.
    */
    protected static final int levelDistance    = 55;

    /** The horizontal distance from a node to ot's shadow.
    */
    protected static final int horDistance3D    = 10;

    /** The vertical distance from a node to ot's shadow.
    */
    protected static final int verDistance3D    = 8;


    /** Color of any shadow that is drawn to create a 3D-effect.
    */
    protected static final Color shadowColor    = Color.gray;


    /** Color of a normal branch.
    */
    protected static final Color branchColor    = Color.black;

    // ----- Variables ----

    /** Position of root-node.
    */
    protected Point rootPos             = new Point(304,30);

    /** Default position of a new node.
    */
    protected Point defaultPos          = new Point(35,30);


    /** Remember info about a tree's root.
    */
    protected Node rootNode             = new Node(rootPos,Node.SHADOW_VALUE,-1,Node.SHADOW,1,true,true,null,null,null,null);

    /** The selected node at any time.
    */
    protected Node selectedNode         = null;

    /** Remember the loose node at any time.
    */
    protected Node looseNode            = null;


    /** In a sorted tree, points to the shadow-node where the next node should be inserted.
    */
    protected Node positionNode         = null;

    // When deleting nodes in the tree, we must remember some nodes and a point
    /** The node that is being deleted.
    */
    protected Node deletedNode          = null;

    /** The node that will try to replace the deleted node.
    */
    protected Node replacementNode      = null;

    /** A pointer to the node that needs to be rotated after deletion/insertion.
    */
    protected Node unbalancedNode       = null;

    /** The center of the last node that might be moved during deletion.
    */
    protected Point reboundPoint        = null;


    /** Hash-table with all the tree's nodes.
    */
    protected Hashtable normalNodes     = new Hashtable();

    /** Vector with all the leave-node's children.
    */
    protected Vector shadowNodes        = new Vector(20,20);

    /** Stack with values of the nodes to come.
    */
    protected Stack toBeInserted        = new Stack();

    /** Stack with all numbers in a certain order (in-,pre-,post-).
    */
    protected Stack order               = new Stack();

    /** Stack with all numbers to be traversed to find a number during search.
    */
    protected Stack searchOrder         = new Stack();


    /** Holds the value the user is searching for.
    */
    protected String searchValue        = "";


    /** Holds the number of nodes in the tree.
    */
    protected int numberOfNodes         = 0;


    /** Operation to be performed (selected from menu).
    */
    protected String operation          = null;

    /** Flag says if a node is in the process of being delted.
    */
    protected boolean busyDeleting      = false;

    /** Flag says if double deletion is to be performed.
    */
    protected boolean doubleDeletion    = false;

    /** Flag says if a node is in the process of being rotated.
    */
    protected boolean busyRotating      = false;


    /** The different types of information that can be output with each node.
    */
    public static String drawType[]     = new String[]{"Value","Height","Level","AVLBalance","FrontBack","AALevel"};

    /** What type of information that should be output with each node.
    */
    public static String whatToDraw     = drawType[0];


    /** Pointer to the applet-class.
    */
    protected BinaryTreesome mainClass  = null;

    /** Panel to output messages in.
    */
    protected BottomPanel bottom        = null;

    /** Panel to output operation-mode in.
    */
    protected TopPanel top              = null;

    /** Panel to paint the tree in.
    */
    protected CenterPanel center        = null;


    /** Takes care of moving a small ball when looking through the tree.
    */
    protected Tracer tracer             = null;

    /** Takes care of moving the looseNode to the right position (if solve).
    */
    protected MoveLoose mover           = null;


    /** Remembers the different images.
    */
    protected static Image ball[]       = null;

    /** Remembers the image for the tracer.
    */
    protected static Image tracerBall   = null;


    /** The window where algorithms will be shown.
    */
    protected Window window             = new Window();

    /** Variable used to read a file.
    */
    protected FileReader file           = null;

    // "Off-screen" buffers for drawing a tree
    /** Image (copy of the applet-window).
    */
    private Image offscreen             = null;

    /** Size of the applet-window.
    */
    private Dimension offscreensize     = null;

    /** Graphics object to update.
    */
    private Graphics offgraphics        = null;




    // ---------------------------- Constructor -------------------------------

    

    /** Constructs an empty tree with a shadow node as root.
    *
    * @param b the panel where messages are output
    * @param t the top panel, used for input and output
    * @param c the panel where the tree is to be painted
    * @param a the main applet class
    * @see BottomPanel
    * @see TopPanel
    * @see CenterPanel
    * @see BinaryTreesome
    */
    public TreeTemplate(BottomPanel b, TopPanel t, CenterPanel c, BinaryTreesome a)
    {
        center    = c;
        bottom    = b;
        top       = t;
        mainClass = a;

        // Let the window where algorithms should be output know where to write messages and print text
        window.setBottom(bottom);

        // Set the lowest level in the tree
        Node.setLowestLevel(0);

        // Insert a shadow-node to the tree (reserves space for root-node)
        shadowNodes.addElement(rootNode);

        // Draw values of nodes as default
        whatToDraw = drawType[0];
    }




    // -------------------- Automatically called functions --------------------

    
    
    
    /** Draws the whole tree.
    * Calls functions that draws all the nodes, shadows and branches.
    *
    * @param g the Graphics context in which things will be painted
    * @see TreeTemplate#paint3DShadows
    * @see TreeTemplate#paintLooseShadows
    * @see TreeTemplate#paintBranch
    * @see TreeTemplate#paintAllNodes
    * @see TreeTemplate#paintTracer
    */
    public synchronized void update(Graphics g)
    {
        // Get the actual size of the panel where the tree should be output
    	Dimension d = center.getSize();

        // True when no off-screen buffer exists, or change of applett-size
    	if ((offscreen == null) || (d.width != offscreensize.width) ||
            (d.height != offscreensize.height))
    	{
            // Create an off-screen buffer
    	    offscreen = center.createImage(d.width, d.height);
    	    offscreensize = d;
    	    offgraphics = offscreen.getGraphics();
    	    offgraphics.setFont(center.getFont());
    	}

        // Set/initialize the off-screen image
    	offgraphics.setColor(center.getBackground());
        offgraphics.fill3DRect(BinaryTreesome.BORDERSIZE, 0, d.width-BinaryTreesome.BORDERSIZE, d.height,true);


        // Paint all shadows to create a 3D-effect
        paint3DShadows(rootNode);
        paintLooseShadows();

        // paint all the branches in an off-screen buffer
        paintBranch(rootNode);

        // paint all the nodes in an off-screen buffer
        paintAllNodes();

        if ((tracer != null) && (tracer.isAlive()))
           paintTracer();

        // Move image from off-screen buffer and back onto screen
    	g.drawImage(offscreen, 0, 0, null);
    }




    // ----------------------- Mouse-related functions ------------------------

    
    
    
    /** Moves the selected node around with the mouse pointer.
    * Calls the selected nodes setCenter function whith the mouse
    * pointer's position as argument and repaints.
    *
    * @param e the mouse event
    * @see Node#setCenter
    */
    public void mouseDragged(MouseEvent e)
    {
        // Ignore mousedrag during solving an iteration or search
        if (((getTracer() != null) && ((getTracer()).isAlive())) ||
            ((getMover() != null) && ((getMover()).isAlive())))
        {
            return;
        }

        // A node is selected, set it's new coordinates
	    if (selectedNode != null)
    	{
	        selectedNode.setCenter(e.getX(),e.getY());

            Dimension d = center.getSize();

            // Don't let the node exceed the applet's vertical edges
            if (selectedNode.getX() < Node.RADIUS+BinaryTreesome.BORDERSIZE+11)
                selectedNode.setX(Node.RADIUS+BinaryTreesome.BORDERSIZE+11);
            else if (selectedNode.getX() > (d.width-Node.RADIUS-BinaryTreesome.BORDERSIZE+2))
                selectedNode.setX(d.width-Node.RADIUS-BinaryTreesome.BORDERSIZE+2);

            // Don't let the node exceed the applet's horizontal edges
            if (selectedNode.getY() < Node.RADIUS+11)
                selectedNode.setY(Node.RADIUS+11);
            else if (selectedNode.getY() > (d.height-Node.RADIUS-2))
                selectedNode.setY(d.height-Node.RADIUS-2);

      	    center.repaint();
    	}
    }




    /** Controls what happens when the mouse button is pressed.
    * If the mouse pointer is inside a draggable node this node
    * is selected. Also calls updateMessages.
    *
    * @param e the mouse event
    * @see TreeTemplate#updateMessages
    */
    public void mousePressed(MouseEvent e)
    {
        // Mouseclick not allowed during solving an iteration or search
        if (((getTracer() != null) && ((getTracer()).isAlive())) ||
            ((getMover() != null) && ((getMover()).isAlive())))
        {
            return;
        }

        Point mp = new Point(e.getX(),e.getY());

        // Check if mouse was pressed over a loose node, and if so - select it
        if ((looseNode != null) && (looseNode.contains(mp)))
        {
            updateMessages();
            selectedNode = looseNode;
            selectedNode.setType(Node.SELECTED);
            center.repaint();
            return;
        }

        // Trying to press (and drag) a node during deletion
        if ((deleteOperation()) && (busyDeleting) && (!normalNodes.isEmpty()))
        {
            updateMessages();

            Node temp = null;

            // Check all normal nodes in hash
            for (Enumeration enum = normalNodes.elements(); enum.hasMoreElements();)
            {
                temp = (Node)enum.nextElement();

                // Node contains point and isn't locked. Create a new node (copy of
                // the one that was pressed) that the user can drag to another place.
                // The node that was clicked on becomes a hole (shadow)
                if ((!temp.isLocked()) && (!((temp.getType()).equals(Node.SHADOW))) && (temp.contains(mp)))
                {
                    selectedNode = new Node(temp.getCenter(),temp.getValue(),temp.getHeight(),
                                            Node.SELECTED,0,true,false,null,null,null,null);
                    replacementNode = temp;
                    replacementNode.setType(Node.SHADOW);
                    center.repaint();
                    return;
                }
            }
        }
    }




    /** Just here to satisfy the compiler.
    */
    public void mouseClicked(MouseEvent e){}
    /** Just here to satisfy the compiler.
    */
    public void mouseEntered(MouseEvent e){}
    /** Just here to satisfy the compiler.
    */
    public void mouseExited(MouseEvent e){}
    /** Just here to satisfy the compiler.
    */
    public void mouseMoved(MouseEvent e){}
    /** Just here to satisfy the compiler.
    */
    public void mouseReleased(MouseEvent e){}




    // ---------------------- Action-related functions ------------------------




    /** Controls what happens when the one of the buttons in the
    * TopPanel top are clicked.
    * Adds or changes the numbers to be inserted or searched for.
    *
    * @param e the action event
    * @see TreeTemplate#updateNumbers
    * @see TreeTemplate#updateSearchValue
    */
    public void actionPerformed(ActionEvent e)
    {
        String chosen = new String(e.getActionCommand());

        // Add-button pressed from inputPanel
        if (chosen.equals(BinaryTreesome.inputType[0]))
        {
            // If user is inserting nodes, add numbers to our stack, otherwise set the searchValue
            if (insertOperation())
                updateNumbers(BinaryTreesome.inputType[0]);
            else if (searchOperation())
                updateSearchValue(BinaryTreesome.inputType[0]);
        }

        // Change-button pressed from inputPanel
        else if (chosen.equals(BinaryTreesome.inputType[1]))
        {
            // If user is inserting nodes and there still are numbers to be inserted (and the text-field is
            // empty), bring the numbers from stack and into text-field for user to change. If user has
            // entered some valid numbers, exchange current stack with new numbers.
            if (insertOperation())
            {
                if (((top.getInput()).length() == 0) && ((!toBeInserted.empty()) || (looseNode != null)))
                    setInput();
                else
                    updateNumbers(BinaryTreesome.inputType[1]);
            }

            // If user is in search-mode, either replace old search value with the new number (if any), or
            // bring the old value up into textfield for change if no numbers exists in the field.
            else if (searchOperation())
            {
                updateSearchValue(BinaryTreesome.inputType[1]);
            }
        }

        // Random-button pressed from inputPanel. Generate random numbers.
        else if (chosen.equals(BinaryTreesome.inputType[2]))
        {
				String numbers = "";
				if (insertOperation())
					numbers = generateRandomNumbers(10);
            else if (searchOperation())
					numbers = generateRandomNumbers(1);
	         top.setInput(numbers);
        }
    }




    // ------------------------ Key-related functions -------------------------




    /** Controls what happens when a key is released.
    * Only responds to the enter button being released.
    * Calls updateNumbers or updateSearchValue depending
    * on the operation.
    *
    * @param e the key event
    * @see TreeTemplate#updateNumbers
    * @see TreeTemplate#updateSearchValue
    */
    public void keyReleased(KeyEvent e)
    {
        // If enter is pressed, interpret it as clicking the add-button
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
            if (insertOperation())
                updateNumbers(BinaryTreesome.inputType[0]);
            else if (searchOperation())
                updateSearchValue(BinaryTreesome.inputType[0]);
        }
    }




    /** Just here to satisfy the compiler.
    */
    public void keyTyped(KeyEvent e){}
    /** Just here to satisfy the compiler.
    */
    public void keyPressed(KeyEvent e){}




    // -------------------------- Other functions -----------------------------




    /** Adds two shadow nodes as children to a node.
    *
    * @param n the parent node of the new children
    * @see Node#setNeighbours
    * @see Node#setLeft
    * @see Node#setRight
    * @see Node#setLowestLevel
    * @see TreeTemplate#shadowNodes
    */
    public void addShadowChildren(Node n)
    {
           // Create the two children.
           Node left  = new Node(n.getCenter(),Node.SHADOW_VALUE,-1,Node.SHADOW,n.getLevel()+1,true,true,n,null,null,null);
           Node right = new Node(n.getCenter(),Node.SHADOW_VALUE,-1,Node.SHADOW,n.getLevel()+1,true,true,n,null,null,null);

           // Set right child to back if it is created in the maximum level
           if (right.getLevel() == MAXLEVEL) right.setBack();

           // The two nodes are neighbours
           left.setNeighbours(right);

           // Set parent's pointers to these children
           n.setLeft(left);
           n.setRight(right);

           shadowNodes.addElement(left);
           shadowNodes.addElement(right);

           // Check if children start filling up a new level. If the children come
           // in the maximum level, don't update "lowestLevel", because this last
           // level is not to be taken into consideration  when calculating the
           // new positions of all the nodes in the tree.
           if ((left.getLevel() < MAXLEVEL ) && (left.getLevel() > n.getLowestLevel()))
               n.setLowestLevel(left.getLevel());

           // Update "pointers" to children in rootNode if necessary
           if (n.getType().equals(Node.ROOT))
              rootNode = n;
    }




    /** Adds shadows as children to nodes that should have them but don't.
    * Recursive function that starts at the node received.
    *
    * @param n the node to be checked
    * @see TreeTemplate#addShadowChildren
    */
    public void addShadows(Node n)
    {
        // The node sent to the function exists
        if (n != null)
        {

            // Add shadow children if needed, otherwise check the nodes children
            if ((n.getRight() == null) && (!(n.getType().equals(Node.SHADOW))) && (n.getLevel() < MAXLEVEL))
            {
                addShadowChildren(n);
            }
            else if (n.getRight() != null)
            {
                addShadows(n.getLeft());
                addShadows(n.getRight());
            }
        }
    }




    /** Calculates the correct positions of all nodes in a subtree.
    * If a node needs to be moved, the function starts a thread that
    * does this.
    *
    * @param n the root node in the subtree to be checked
    * @param dest_x the x coordinate of n's destination
    * @param dest_y the y coordinate of n's destination
    * @see Node#setDestination
    * @see MoveNode#setDestination
    */
    public void calculatePositions(Node n, int dest_x, int dest_y)
    {
        // Update this node's destination position
        n.setDestination(dest_x,dest_y);


        // Start a thread that moves the node (unless a thread for the node allready exists)
        if (n.hasThread())
            (n.getThread()).setDestination(new Point(dest_x,dest_y));
        else
        {
            Thread t = new MoveNode("Children",n,center);
            t.start();
        }

        // As long as the node sent to the function isn't a leaf in the tree, call the function
        // again for each of the node's children.
        if (n.getLeft() != null)
        {
             // If parent is in the level next to the last, the distance between
             // it's children will be different. The children in the lowest level will overlap
             // to make the tree fit in the applet.

             int d = 0;

             // Calculate the distance between the two children depending on the level they're in.
             if (n.getLevel() == MAXLEVEL-1)
                 d = Node.RADIUS;
             else
                 d = (int)(Math.pow(2,(n.getLowestLevel()-n.getLevel())))*
                     (nodeDistance/2 + 1 + Node.RADIUS);

            // The children's coordinates
            int l_child_x = (n.getDestination()).x - (int)d/2;
            int r_child_x = (n.getDestination()).x + (int)d/2;
            int child_y   = (n.getDestination()).y + levelDistance;

            // Call the function again for each of the children
            calculatePositions(n.getLeft(), l_child_x, child_y);
            calculatePositions(n.getRight(),r_child_x, child_y);
        }
    }




    /** Checks if nodes are selected in correct order during
    * iteration operations.
    * The order is given in the private Stack variable order.
    * Checks if the point received is inside a node and the
    * value of that node is equal to the first number in the stack.
    *
    * @param point the point where the mouse was clicked
    * @see TreeTemplate#order
    * @see MoveNode#setDestination
    */
    public void checkOrderClick(Point point)
    {
        // Safety check. There must be numbers in the order-stack (which tells the
        // right order of the numbers depending on which iteration is chosen).
        if (order.empty()) return;

        String number   = (String)order.peek();     // Look at the first number in the stack
        Node temp       = null;

        // Look through all the nodes in the tree (except the shadows)
        if (!normalNodes.isEmpty())
        {
            for (Enumeration enum = normalNodes.elements(); enum.hasMoreElements();)
            {
                temp = (Node)enum.nextElement();

                // The node is in the front, and is clicked on by the user. Do stuff and return.
                if ((temp.isFront()) && (temp.contains(point)))
                {
                    // Clear earlier error messages
                    bottom.writeError("");

                    // User clicked on the right node in the tree.
                    if ((temp.getValue()).equals(number))
                    {
                        // Change color of the ball that was clicked (selected)
                        temp.setType(Node.SELECTED);
                        center.repaint();

                        // Output the users iteration progress and pop the value from our stack
                        bottom.addToProgress(number);
                        order.pop();

                        // If no more numbers in stack, iteration is finised. Give message.
                        if (order.empty())
                        {
                            bottom.addToProgress(".");
                            bottom.writeInfo(InfoMessage.finishedIteration());
                        }
                        else
                            bottom.addToProgress(", ");
                    }

                    // The node the user clicked on wasn't the right one
                    else
                        bottom.writeError(ErrorMessage.wrongNode());

                    return;
                }
            }
        }
    }




    /** Sets selectedNode and looseNode to null.
    * Be careful with the use of this function!
    * The value of the looseNode is pushed on the
    * toBeInserted stack before it is set to null.
    *
    * @see TreeTemplate#looseNode
    * @see TreeTemplate#selectedNode
    * @see TreeTemplate#toBeInserted
    */
    public void clearLooseNodes()
    {
        selectedNode = null; 
        if (looseNode != null)
        {
            toBeInserted.push(looseNode.getValue());
        }
        looseNode = null;
    }


    
    
    /** Clears all variables used temporarily during deletion and rotation.
    * Be careful with the use of this function!
    * See below for the variabels that are cleared
    *
    * @see TreeTemplate#busyDeleting
    * @see TreeTemplate#doubleDeletion
    * @see TreeTemplate#selectedNode
    * @see TreeTemplate#deletedNode
    * @see TreeTemplate#replacementNode
    */
    public void clearTempVariables()
    {
        // restore node-pointers and flag
        busyDeleting    = false;
        doubleDeletion  = false;
        selectedNode    = null;
        deletedNode     = null;
        replacementNode = null;
    }

    
    
    
    /** Called when user clicks on the delete button in the menu.
    * Sets the operation variable to delete, and outputs information
    * to the user if deletion is possible.
    *
    * @see TreeTemplate#operation
    */
    public void delete()
    {
        // Remember the current operation
        operation = BinaryTreesome.menuItem[1];
        bottom.writeProgress("");

        // Deletion is possible. Write some output to help user
        if ((rootNode != null) && (!((rootNode.getValue()).equals(Node.SHADOW_VALUE))))
        {
            top.writeOperation(InfoMessage.deleteOperation());
            bottom.writeInfo(InfoMessage.howToDelete());
        }

        // Don't print operation mode in the topPanel if no normal rootNode exists, but give
        // some info and an error-message
        else
        {
            // No current operation
            operation = null;

            top.writeOperation("");
            bottom.writeInfo(InfoMessage.emptyTree());
            bottom.writeError(ErrorMessage.operationNotPossible());
        }

        center.repaint();
    }




    /** Decides what should happen when the mouse button is clicked
    * during deletion.
    * If clicked inside a node, the node is deleted and restructuring
    * of the tree is prepared.
    *
    * @param point the point where the mouse was clicked
    * @see TreeTemplate#busyDeleting
    * @see TreeTemplate#doubleDeletion
    */
    public void deleteClick(Point point)
    {
        // normal nodes exists, and none are in the process of being deleted
        if ((!normalNodes.isEmpty()) && (!busyDeleting))
        {
            Node temp = null;

            // Look at each element in the stack to see what numbers are there
            for (Enumeration enum = normalNodes.elements(); enum.hasMoreElements();)
            {
                temp = (Node)enum.nextElement();

                // The node is in the front, and is clicked on by the user. Do stuff and break for-loop.
                if ((temp.isFront()) && (temp.contains(point)))
                {
                    mainClass.play(mainClass.getCodeBase(),"Delete.au");

                    // Clear earlier error messages
                    bottom.writeError("");

                    // Trying to delete a node with no children and no shadows.
                    // Just "delete" the node.
                    if (temp.getLeft() == null)
                    {
                        // "Delete" the normal node and add it to the shadow-vector
                        normalNodes.remove(temp.getValue());
                        temp.setValue(Node.SHADOW_VALUE);
                        temp.setHeight(-1);
                        temp.setType(Node.SHADOW);
                        shadowNodes.addElement(temp);

                        // If the node deleted was at the bottom of the tree, set it to
                        // back and it's neighbour to front
                        if (temp.getLeft() == null)
                        {
                            temp.setBack();
                            (temp.getNeighbour()).setFront();
                        }

                        // Temp's parent and grandparents and so on will have new balances. Remember
                        // the temp's parent to check for an imbalanced tree later if necessary
                        unbalancedNode = temp.getParent();

                        // Finished deleted the node. 
                        busyDeleting = false;
                    } // ---end if---


                    // Trying to delete a node with two shadows as children.
                    // Remove shadows and "delete" the node
                    else if ((((temp.getLeft()).getType()).equals(Node.SHADOW)) &&
                            (((temp.getRight()).getType()).equals(Node.SHADOW)))
                    {
                        shadowNodes.removeElement(temp.getLeft());
                        shadowNodes.removeElement(temp.getRight());

                        // "Delete" the normal node and add it to the shadow-vector
                        normalNodes.remove(temp.getValue());
                        temp.setRight(null);
                        temp.setLeft(null);
                        temp.setValue(Node.SHADOW_VALUE);
                        temp.setHeight(-1);
                        temp.setType(Node.SHADOW);
                        shadowNodes.addElement(temp);

                        // Temp's parent and grandparents and so on will have new balances. Remember
                        // the temp's parent to check for an imbalanced tree later if necessary
                        unbalancedNode = temp.getParent();

                        // Finished deleted the node. 
                        busyDeleting = false;
                    }// ---end else if---


                    // More complicated deletion.
                    else
                    {
                        // Remember the node that is being tried deleted
                        deletedNode = temp;

                        // Unlock the whole tree to enable the user to drag any node
                        // to restructure the tree after the deletion
                        lockSubtree(rootNode,false);

                        // Change color of the ball that was clicked to shadow and
                        // set flag to say that deletion is in progress
                        deletedNode.setType(Node.SHADOW);
                        busyDeleting = true;

                        // Set flag to indicate whether a double deletion is neede
                        // is necessary.
                        if ((!(((deletedNode.getLeft()).getType()).equals(Node.SHADOW))) &&
                           (!(((deletedNode.getRight()).getType()).equals(Node.SHADOW))))
                        {
                            doubleDeletion = true;
                        }
                        else
                        {
                            doubleDeletion = false;
                        }

                        // Give info to user
                        bottom.writeInfo(InfoMessage.howToReplace());
                    }// ---end else---

                    break;
                } // ---end if---
            }// ---end for---
            center.repaint();
        }// ---end if---
    }//---end function---




    /** Returns true if current operation is deletion.
    *
    * @see TreeTemplate#operation
    */
    public boolean deleteOperation(){return (operation==null?false:(operation.equals(BinaryTreesome.menuItem[1])?true:false));}

    
    
    
    /** Called when a node is dropped on the available shadow node
    * during restructuring of the tree after a deletion.
    *
    * @see TreeTemplate#findMin
    * @see TreeTemplate#busyDeleting
    * @see TreeTemplate#doubleDeletion
    * @see TreeTemplate#deletedNode
    * @see TreeTemplate#replacementNode
    * @see TreeTemplate#unbalancedNode
    */
    public void deleteReleased()
    {
        // Double deletion was necessary.
        if (doubleDeletion)
        {
            // Find the leftMost node in the deleted node's right subtree
            // (selectedNode is set to point to the leftmost node)
            findMin(deletedNode.getRight());

            // The wrong node was dropped, restore node, give errormessage and return
            if (replacementNode != selectedNode)
            {
                bottom.writeError(ErrorMessage.unableToDrop());

                if (replacementNode.getParent() != null)
                    replacementNode.setType(Node.NORMAL);
                else
                    replacementNode.setType(Node.ROOT);

                selectedNode = null;
                center.repaint();
                return;
            }

            // The correct node was dropped. Both the deleted node and the replacementnode must
            // be removed from the hash because their value will be changed
            normalNodes.remove(deletedNode.getValue());
            normalNodes.remove(replacementNode.getValue());

            // Remember the deleted node`s value
            String value = deletedNode.getValue();

            // The deleted node must now take the replacementNode's value
            // and vice versa.
            deletedNode.setValue(replacementNode.getValue());
            replacementNode.setValue(value);

            // The deleted node must also be restored to the correct type
            if (deletedNode.getParent() != null)
                deletedNode.setType(Node.NORMAL);
            else
                deletedNode.setType(Node.ROOT);

            // deletedNode and replacementNode must be added to the hash
            // again with their new values as the key
            normalNodes.put(deletedNode.getValue(), deletedNode);
            normalNodes.put(replacementNode.getValue(), replacementNode);

            // Now, the new deleted node must be set to point to the empty hole
            // where the replacement node used to be.
            deletedNode = replacementNode;

            // The replacementNode has been put into the right hole, and there is
            // no longer any replacementNode or selectdeNode available
            replacementNode = null;
            selectedNode = null;

            // Set flag to indicate that the double deletion has been done, and only
            // the single deletion is left
            doubleDeletion = false;

            // The flag over is set, because if the node you dragged into place had
            // a child, the child must be moved to the hole where the replacementNode
            // used to be. There's one exception, and that is that the node that
            // was dragged in place didn't have any children. Test this, and if it
            // is true, clean up and finish deletion.
            if  ((deletedNode.getLeft() == null) ||
                ((((deletedNode.getLeft()).getType()).equals(Node.SHADOW)) &&
                (((deletedNode.getRight()).getType()).equals(Node.SHADOW))))
            {
                // "Delete" the node's shadows if any
                if (deletedNode.getLeft() != null)
                {
                    shadowNodes.removeElement(deletedNode.getLeft());
                    shadowNodes.removeElement(deletedNode.getRight());
                }

                // "Delete" the node and add it to the shadow-vector
                normalNodes.remove(deletedNode.getValue());
                deletedNode.setRight(null);
                deletedNode.setLeft(null);
                deletedNode.setValue(Node.SHADOW_VALUE);
                deletedNode.setHeight(-1);
                deletedNode.setType(Node.SHADOW);
                shadowNodes.addElement(deletedNode);

                // DeletedNode's parent and grandparents and so on will have new balances. Remember
                // the deletedNode's parent to check for an imbalanced tree later if necessary
                unbalancedNode = deletedNode.getParent();

                // Finished deleted the node. 
                busyDeleting = false;
            }
        }

        // Single deletion was necessary.
        else if ((deletedNode.getRight() == replacementNode) ||
                 (deletedNode.getLeft() == replacementNode))
        {
            // Remove the node's one shadow-child
            if (deletedNode.getLeft() == replacementNode)
                shadowNodes.removeElement(deletedNode.getRight());
            else
                shadowNodes.removeElement(deletedNode.getLeft());

            // Update replacementNode's position to take the node it overlapped's place
            replacementNode.setCenter(deletedNode.getCenter());

            // Update pointers that used to point to the node that was deleted
            replacementNode.setParent(deletedNode.getParent());

            // Root was not deleted
            if (deletedNode.getParent() != null)
            {
                // Update deleted node's parent's child-pointer
                if (deletedNode == (deletedNode.getParent()).getLeft())
                    (deletedNode.getParent()).setLeft(replacementNode);
                else
                    (deletedNode.getParent()).setRight(replacementNode);

                // Update the neighbour-pointer
                replacementNode.setNeighbours(deletedNode.getNeighbour());
            }

            // Root was deleted - update our private rootNode variable
            else
            {
                replacementNode.setNeighbour(null);
                rootNode = replacementNode;
            }

            // Set the type of the node that was dragged in place
            if (deletedNode.getParent() != null)
                replacementNode.setType(Node.NORMAL);
            else
                replacementNode.setType(Node.ROOT);

            // All pointers are updated. Safe to delete "deletedNode" (i.e.
            // remove it from our hash of nodes)
            normalNodes.remove(deletedNode.getValue());

            // ReplacementNode's parent and grandparents and so on will have new balances. Remember
            // the replacementNode's parent to check for an imbalanced tree later if necessary
            unbalancedNode = replacementNode.getParent();

            // Finished deleted the node.
            busyDeleting = false;
        }

        // The node wasn't dropped in the right place. Restore it to it's
        // original type (it's still in it's old position, because it was actually
        // the selected node you dragged and dropped) and give and errormessage
        else
        {
            bottom.writeError(ErrorMessage.unableToDrop());

            if (replacementNode.getParent() != null)
                replacementNode.setType(Node.NORMAL);
            else
                replacementNode.setType(Node.ROOT);

            selectedNode = null;
        }

        center.repaint();
    }




    /** Finds the left most node in the received node's subtree.
    * SelectedNode is set to point to the left most node. The function
    * assumes that SelectedNode is not in use elsewhere.
    * This function will behave in an unpredictable way if first
    * called for n = null (we don't set selectedNode then).
    *
    * @param n the root node of the subtree
    * @see TreeTemplate#findMin
    * @see TreeTemplate#selectedNode
    */
    public void findMin(Node n)
    {
        // Bottom of tree isn't reached. Call function for left child.
        if (n != null)
        {
            selectedNode = n;
            findMin(n.getLeft());
        }

       // If selectedNode is a shadow and to the left of it's parent, leftmost node will be
       // selectedNode's parent, otherwise it is a leaf and is itself the leftmost node
        else if (((selectedNode.getValue()).equals(Node.SHADOW_VALUE)) &&
                (selectedNode == (selectedNode.getParent()).getLeft()))
        {
                selectedNode = selectedNode.getParent();
        }
    }




    /** Cleans up after a sucsessfully completed deletion.
    * Calls the functions below to restore the tree completely
    * after the deletion of a node.
    *
    * @see TreeTemplate#numberOfNodes
    * @see TreeTemplate#lockSubtree
    * @see TreeTemplate#updateLevel
    * @see TreeTemplate#updateLowestLevel
    * @see TreeTemplate#addShadows
    * @see TreeTemplate#updateFronts
    * @see TreeTemplate#updateHeights
    * @see TreeTemplate#calculatePositions
    */
    public void fixTreeAfterDeletion()
    {
        // A loose node has successfully been deleted. Subtract 1 from the number of nodes.
        numberOfNodes--;

        // lock subtree again
        lockSubtree(rootNode,true);

        // Reset lowest level in tree and recalculate level-information
        Node.setLowestLevel(0);
        updateLevel(rootNode);
        updateLowestLevel(rootNode);

        // Add new shadows if needed (i.e. when a node at the bottom of the tree was moved up)
        addShadows(rootNode);

        // Set all nodes to front, except at the bottom level where one must be in the back
        updateFronts(rootNode);

        // Update all subtree's heights if necessary
        updateHeights(rootNode);

        // Move the nodes to their new positions
        calculatePositions(rootNode, rootPos.x,rootPos.y);

        // Give output to user.
        if (normalNodes.isEmpty())
            bottom.writeInfo(InfoMessage.allNodesDeleted());
        else
            bottom.writeInfo(InfoMessage.howToDelete());
    }


    
    
    /** Cleans up after a sucsessfully completed insertion.
    * Calls the functions below and sets some variables to
    * restore the tree completely after the insertion of a node.
    *
    * @see TreeTemplate#addShadowChildren
    * @see TreeTemplate#calculatePositions
    * @see TreeTemplate#updateHeights
    * @see TreeTemplate#looseNode
    * @see TreeTemplate#selectedNode
    */
    public void fixTreeAfterInsertion()
    {
        // If there is room for nodes in the level below, add two children to
        // the node that was dragged in place, and calculate the new positions
        // to all the nodes in the tree.
        if (selectedNode.getLevel() < MAXLEVEL )
        {
            addShadowChildren(selectedNode);
            calculatePositions(rootNode,rootPos.x,rootPos.y);
        }

        // A node was inserted, and all node's might have a change of level. Update if necessary
        updateHeights(rootNode);
        
        // If the selected node was the loose node (which it must be at the moment,
        // but perhaps not in the future), no loose node is longer available.
        if (selectedNode == looseNode)
           looseNode = null;

        // No selected node anymore
        selectedNode = null;

        center.repaint();
    }




    /** Creates a loose node with the next number to be inserted
    * as it's value.
    * The node is not created if there allready is a loose node or
    * there isn't room for the node in the tree.
    *
    * @see TreeTemplate#printLeftToBeInserted
    * @see TreeTemplate#toBeInserted
    * @see TreeTemplate#looseNode
    * @see TreeTemplate#numberOfNodes
    */
    public void generateLooseNode()
    {
        String skippingInfo  = "We only have room for " + (MAXLEVEL+1) + " levels.";
        String skippingError = "These numbers will be skipped: ";
        boolean errorWritten = false;

        while ((looseNode == null) && (!toBeInserted.isEmpty()))
        {
            String value = (String)toBeInserted.pop();
            looseNode = new Node(defaultPos,value,0,Node.LOOSE,0,true,false,null,null,null,null);

            // If the tree that is chosen isn't a standard tree, the tree shall be sorted.
            // We must check that the number from the stack will fit into the tree (that
            // is limited to a only few levels).
            if (!((mainClass.getChosenTree()).equals(BinaryTreesome.treeType[1])))
            {
                getSortedPosition(rootNode, Integer.parseInt(value));

                // No room for this value in the tree. Give message, skip value and get next.
                if (positionNode == null)
                {
                    if (errorWritten)
                        skippingError += ", " + value;
                    else
                        skippingError += value;

                    // Set a flag to say that an errormessage should be output
                    errorWritten = true;

                    looseNode = null;
                }
            }
            
            // The tree is a standard tree. Check that there's still room for another node. If there isn't -
            // set error-message and break loop
            else if (numberOfNodes > (((int)(Math.pow(2,MAXLEVEL+1)))-1))
            {
                // Set a flag to say that an errormessage should be output
                errorWritten = true;

                skippingError += value;
                looseNode = null;
                while (!toBeInserted.isEmpty())
                {
                    value = (String)toBeInserted.pop();
                    skippingError += ", " + value;
                }
                break;
            }
        }

        // A loose node has successfully been created. Add 1 to number of nodes.
        if (looseNode != null)
            numberOfNodes++;

        // Print the numbers left to be inserted (if any)
        printLeftToBeInserted();

        // Print out an errormessage if flag is set
        if (errorWritten)
        {
            bottom.writeError(skippingError + " !!!");
            bottom.writeInfo(skippingInfo);
        }
    }




    /** Generates a string with random integers between 1 and 99 seperated by
    * a comma.
    *
    * @param num number of random numbers to be generated
    * @return a string with random integers
    */
	public String generateRandomNumbers(int num)
   {
		Hashtable hash = new Hashtable();
      Integer number;

		while (hash.size() < num)
      {
          boolean single  = ((int)(Math.random() + 0.5) == 1)?true:false;
	  		 if (single)
             	number = new Integer(((int)(Math.random()*10))+1);
          else
             	number = new Integer(((int)(Math.random()*100)));

	   	 hash.put(number.toString(),number);
      }

      String numbers = "";

      if (!hash.isEmpty())
          for (Enumeration e = hash.elements(); e.hasMoreElements();)
              numbers += (((Integer)e.nextElement()).toString()) + " ";

      return numbers.trim();
   }




    /** Returns the first number encountered in the string.
    * If no number is found an empty string is returned.
    *
    * @param text the string to be checked
    * @return the first number as a string
    */
    public String getFirstNumber(String text)
    {
        String number   = "";

        for (int i=0; i<text.length(); i++)
        {
            int c = (int)text.charAt(i);

            // If a zero was encountered, only add it if it's not the first character of the number
            if ((c == 48) && (number.length() > 0))
                number += (""+(c-48));

            // character is an integer between 1 and 9
            else if ((c > 48) && (c < 58))
            {
                number += (""+(c-48));
            }

            // Character isn't an integer. The integer we have up until now, if
            // between 1 and 99, is returned
            else if ((number.length() > 0) && (number.length() < 3))
                return number;
            else
                number = "";
        }

        // If text ended with a number, it has not been added yet. Add it now.
        if ((number.length() > 0) && (number.length() < 3))
            return number;

        number = "";
        return number;
    }




    /** Returns the operation in progress.
    * The different operations are defined in BinaryTreesome's menuItem.
    *
    * @see TreeTemplate#operation
    * @see BinaryTreesome#menuItem
    */
    public String getOperation(){return operation;}




    /** Returns the position of the rootNode.
    *
    * @see TreeTemplate#rootPos
    */
    public Point getRootPos(){return rootPos;}




    /** Attempts to get a number to search for.
    * Gets a string from the text box in the top panel, and checks
    * to see if it contains a number. If so the variable searchValue
    * is set to this number. Makes everything ready for the search.
    *
    * @see TopPanel#getInput
    * @see TreeTemplate#getFirstNumber
    * @see TreeTemplate#searchValue
    * @see TreeTemplate#restoreNodes
    * @see TreeTemplate#updateSearchOrder
    * @see TreeTemplate#searchOrder
    */
    public void getSearchInput()
    {
        // Attempt to get a number from the input entered
        searchValue = getFirstNumber(top.getInput());

        if (searchValue.equals(""))
            bottom.writeError(ErrorMessage.notValidNumber());
        else
        {
            // restore all node-types (from possible earlier searches)
            restoreNodes();
            center.repaint();

            // Output some info
            bottom.writeInfo(InfoMessage.howToSearch());
            bottom.writeProgress(InfoMessage.whatToSearch() + searchValue);
            bottom.writeError("");

            // Clear the stack with the values to iterate through to find the number being 
            // searched for, and updates it to contain the new numbers associated with the
            // one the user is looking for now.
            searchOrder.removeAllElements();
            updateSearchOrder(rootNode);

            // We use a stack, and it must be reversed to have the numbers in the correct order
            searchOrder = reverseStack(searchOrder);

            // Clear the textfield for text
            top.clearInput();
        }
    }




    /** Returns the number being searched for.
    *
    * @see TreeTemplate#searchValue
    */
    public String getSearchValue(){return searchValue;}
        
        
        
        
    /** Finds the shadow node where the node with the given value
    * should be inserted.
    * Set the variable positionNode to point to that shadow.
    * This function only makes sense for a sorted tree.
    *
    * @param n the to start from (usually root)
    * @param value the value of the node to be inserted
    * @see TreeTemplate#searchValue
    * @see TreeTemplate#positionNode
    */
    public void getSortedPosition(Node n, int value)
    {
        // A shadow that is meant to keep this value is found. Set positionNode to
        // point to this shadow
        if ((n.getType()).equals(Node.SHADOW))
           positionNode = n;

        // Bottom of tree was reached without finding a suitable position for the value
        // sent to this function. Set positionNode to point to null
        else if (n.getLeft() == null)
             positionNode = null;

        // Continue iteration of the tree (to left or right depending on the value)

        else if (value < Integer.parseInt(n.getValue()))
            getSortedPosition(n.getLeft(),value);

        else if (value > Integer.parseInt(n.getValue()))
            getSortedPosition(n.getRight(),value);
    }



    
    /** Returns the mover.
    *
    * @see TreeTemplate#mover
    */
    public MoveLoose getMover(){return mover;}




    /** Returns the tracer.
    *
    * @see TreeTemplate#tracer
    */
    public Tracer getTracer(){return tracer;}




    /** Returns the topPanel.
    *
    * @see TreeTemplate#top
    * @see TopPanel
    */
    public TopPanel getTop(){return top;}




    /** Returns true if the current operation is inorder iteration.
    *
    * @see TreeTemplate#toBeInserted
    * @see TreeTemplate#looseNode
    */
	 public boolean hasMoreNumbersToBeInserted()
    {
	 	  if ((looseNode != null) || (!(toBeInserted.isEmpty())))
            return true;
        return false;

    }




    /** Returns true if the current operation is inorder iteration.
    *
    * @see TreeTemplate#operation
    * @see BinaryTreesome#getChosenIteration
    */
    public boolean inorderOperation()
    {
        if (operation==null)
            return false;
        else if ((operation.equals(BinaryTreesome.menuItem[3])) && ((mainClass.getChosenIteration()).equals(BinaryTreesome.iteration[0])))
            return true;
        else
            return false;
    }




    /** Makes everything ready for insertion.
    *
    * @see TopPanel#readyForInsertionInput
    * @see TreeTemplate#generateLooseNode
    * @see TreeTemplate#printLeftToBeInserted
    */
    public void insert()
    {
        // During insertion-operation, always let user have possibility to insert numbers
        top.readyForInsertionInput(true);

        // Remember the current operation
        operation = BinaryTreesome.menuItem[0];
        top.writeOperation(InfoMessage.insertOperation());

        // generate a new loose node if possible
        if (!busyRotating)
            generateLooseNode();

        // Print the numbers that are in the stack (if any)
        printLeftToBeInserted();

        // Repaint the screen
        center.repaint();
    }




    /** Returns true if the current operation is insertion.
    *
    * @see TreeTemplate#operation
    */
    public boolean insertOperation(){return (operation==null?false:(operation.equals(BinaryTreesome.menuItem[0])?true:false));}




    /** Returns true if a delete operation is in progress.
    *
    * @see TreeTemplate#busyDeleting
    */
    public boolean isBusyDeleting(){return busyDeleting;}




    /** Returns true if a rotation is in progress.
    *
    * @see TreeTemplate#busyRotating
    */
    public boolean isBusyRotating(){return busyRotating;}




    /** Locks or unlocks all the nodes in a subtree.
    * Recursive function that locks or unlocks the
    * received node and all the nodes in it's subtree.
    *
    * @param n the node to start from
    * @param lock locks if true, unlocks if false
    * @see Node#lock
    * @see Node#unlock
    */
    public void lockSubtree(Node n, boolean lock)
    {
        // Lock / unlock node if possible
        if  ((n != null) && (!((n.getType()).equals(Node.SHADOW))))
        {
            if (lock)
                n.lock();
            else
                n.unlock();

            // Call function for each of the children
            lockSubtree(n.getLeft(),lock);
            lockSubtree(n.getRight(),lock);
        }
    }



    /** Returns true if an operation is in progress.
    *
    * @see TreeTemplate#operation
    */
    public boolean operationInProgress(){return operation == null?false:true;}

    
    
    
    /** Sets the iteration order according to the iteration type.
    * Calls updateInorder, updatePreorder or updatePostorder
    * depending on what type of iteration that is in progress.
    *
    * @see TreeTemplate#updateInorder
    * @see TreeTemplate#updatePreorder
    * @see TreeTemplate#updatePostorder
    */
    public void order()
    {
        // Remember the current operation
        operation = BinaryTreesome.menuItem[3];

        String progress;

        // No nodes in tree, set no current operation and give error-message
        if (normalNodes.isEmpty())
        {
            // No current operation
            operation = null;
            top.writeOperation("");
            bottom.writeProgress("");
            bottom.writeInfo(InfoMessage.emptyTree());
            bottom.writeError(ErrorMessage.operationNotPossible());
            center.repaint();
            return;
        }

        // Clear the stack with values in a specific order
        order.removeAllElements();

        // Write top message and progress message depending on iteration-operation, and fix
        // the stack to have the correct order
        if ((mainClass.getChosenIteration()).equals(BinaryTreesome.iteration[0]))
        {
            top.writeOperation(InfoMessage.inorderOperation());
            bottom.writeProgress("Nodes in inorder: ");
            updateInorder(rootNode);
        }
        else if ((mainClass.getChosenIteration()).equals(BinaryTreesome.iteration[1]))
        {
            top.writeOperation(InfoMessage.preorderOperation());
            bottom.writeProgress("Nodes in preorder: ");
            updatePreorder(rootNode);
        }
        else if ((mainClass.getChosenIteration()).equals(BinaryTreesome.iteration[2]))
        {
            top.writeOperation(InfoMessage.postorderOperation());
            bottom.writeProgress("Nodes in postorder: ");
            updatePostorder(rootNode);
        }

        // We use a stack, and it must be reversed to have the numbers in the correct order
        order = reverseStack(order);

        // Give info to user
        bottom.writeInfo(InfoMessage.howToIterate());

        center.repaint();
    }




    /** Draws shadows for the selected node and the loosenode
    * if they exsists.
    *
    * @see TreeTemplate#selectedNode
    * @see TreeTemplate#looseNode
    */
    public void paintLooseShadows()
    {
        offgraphics.setColor(shadowColor);

        // Always draw the looseNode's shadow if it exists
        if (looseNode != null)
        {
            offgraphics.fillOval(looseNode.getX()-Node.RADIUS+horDistance3D, 
                                 looseNode.getY()-Node.RADIUS+verDistance3D,
                                 2*Node.RADIUS,2*Node.RADIUS);
        }

        // Always draw the selectedNode's shadow if it exists
        if (selectedNode != null)
        {
            offgraphics.fillOval(selectedNode.getX()-Node.RADIUS+horDistance3D, 
                                 selectedNode.getY()-Node.RADIUS+verDistance3D,
                                 2*Node.RADIUS,2*Node.RADIUS);
        }
    }




    /** Draws shadows for the all the nodes and the branches.
    * Recursive function that starts from the node received.
    *
    * @param n the node to start from
    */
    public void paint3DShadows(Node n)
    {
        if (n == null) return;

        // Set the color of the shadow
        offgraphics.setColor(shadowColor);

        // Node isn't root and it doesn't have any children (neither shadows nor normals),
        // draw a line to the parent
    	if ((n.getParent() != null) && (!((n.getValue()).equals(Node.SHADOW_VALUE))))
        {
            offgraphics.drawLine(n.getX()+horDistance3D,n.getY()+verDistance3D,
                                 n.getParent().getX()+horDistance3D,n.getParent().getY()+verDistance3D);
        }

        // Node is root and a tracer is traversing the tree. Paint branch-shadow from
        // top to root-node
        else if ((tracer != null) && (tracer.isAlive()))
        {
            if (tracer.getCenter() != null)
            {
               offgraphics.fillOval(tracer.getCenter().x+horDistance3D-Tracer.RADIUS,
                                 tracer.getCenter().y+verDistance3D-Tracer.RADIUS,
                                 2*Tracer.RADIUS,2*Tracer.RADIUS);
            }
            offgraphics.drawLine(rootPos.x+horDistance3D,0,rootPos.x+horDistance3D,rootPos.y+verDistance3D);
        }

        // If node isn't a shadow-node, draw it's shadow
        if (!((n.getType()).equals(Node.SHADOW)))
            offgraphics.fillOval(n.getX()-Node.RADIUS+horDistance3D, n.getY()-Node.RADIUS+verDistance3D,
                                 2*Node.RADIUS,2*Node.RADIUS);

        // Do the same for the children if they exist
        if (n.getLeft() != null)
        {
            paint3DShadows(n.getLeft());
            paint3DShadows(n.getRight());
        }
    }




    /** Draws all the normal nodes in the tree.
    * The nodes in the hashtable normalNodes are painted,
    * by calling the paintNode function.
    *
    * @see TreeTemplate#normalNodes
    * @see TreeTemplate#paintNode
    */
    public void paintAllNodes()
    {
        // Nodes may overlap. Draw the ones in the back first, then the
        // ones in the front...

        // Draw all normal-nodes that are in the back
        if (!normalNodes.isEmpty())
        {
            for (Enumeration e = normalNodes.elements(); e.hasMoreElements();)
            {
                Node temp = (Node)e.nextElement();
                if (!temp.isFront()) paintNode(temp);
            }
        }

        // Draw all shadow-nodes that are in the back (if in insertion-mode)
        if ((!shadowNodes.isEmpty()) && (insertOperation()))
        {
            for (Enumeration e = shadowNodes.elements(); e.hasMoreElements();)
            {
                Node temp = (Node)e.nextElement();
                if (!temp.isFront()) paintNode(temp);
            }
        }

        // Draw all normal-nodes that are in the front
        if (!normalNodes.isEmpty())
        {
            for (Enumeration e = normalNodes.elements(); e.hasMoreElements();)
            {
                Node temp = (Node)e.nextElement();
                if (temp.isFront()) paintNode(temp);
            }
        }

        // Draw all shadow-nodes that are in the front (if in insertion-mode)
        if ((!shadowNodes.isEmpty()) && (insertOperation()))
        {
            for (Enumeration e = shadowNodes.elements(); e.hasMoreElements();)
            {
                Node temp = (Node)e.nextElement();
                if (temp.isFront()) paintNode(temp);
            }
        }

        // If a loose node exists, draw it (on top of everything)
        if ((looseNode != null) && (insertOperation()))
            paintNode(looseNode);

        // At the moment, only the loose node can be selected, so the line under is really
        // not necessary, but this might change in the future, so we'll keep it anyway.
        // If a node is selected, draw it (on top of everything)
        if (selectedNode != null)
            paintNode(selectedNode);
    }




    /** Draws all the branches in a subtree.
    * Recursive function that starts from the node received, and
    * draws all the branches in it's subtree.
    *
    * @param n the node to start from
    */
    public void paintBranch(Node n)
    {
        if (n == null) return;

        // Unless the node is root, perhaps draw a branch to the parent
    	if (n.getParent() != null)
    	{
            // If operation is to insert nodes, always draw branch to parent
            if (insertOperation())
            {
                // Draw branch with right color (gray if from a shadow)
                if ((n.getType()).equals(Node.SHADOW))
                    offgraphics.setColor(shadowColor);
                else
                    offgraphics.setColor(branchColor);

               offgraphics.drawLine(n.getX(),n.getY(), n.getParent().getX(),n.getParent().getY());
            }

            // Not insertion. Draw all branches, except from leaf-shadows
            else if ((((n.getValue()).equals(Node.SHADOW_VALUE)) && (n.getLeft() != null)) ||
                    (!((n.getValue()).equals(Node.SHADOW_VALUE))))
            {
                offgraphics.setColor(branchColor);
                offgraphics.drawLine(n.getX(),n.getY(), n.getParent().getX(),n.getParent().getY());
            }
        }

        // Node is root and a tracer is traversing the tree. Paint branch from
        // top to root-node
        else if ((tracer != null) && (tracer.isAlive()))
        {
            offgraphics.setColor(branchColor);
            offgraphics.drawLine(rootPos.x,0,rootPos.x, rootPos.y);
        }

        // Call function again for both children
        paintBranch(n.getLeft());
        paintBranch(n.getRight());
    }




    /** Paints a node with it's value in a rectangle above to the left.
    * The function can also write some other type of information in the
    * rectangle.
    *
    * @param n the node to be painted
    */
    public void paintNode(Node n)
    {
    	FontMetrics fm = offgraphics.getFontMetrics();

        // The coordinates of the node's corner
    	int x = n.getX() - Node.RADIUS;
    	int y = n.getY() - Node.RADIUS;

    	int h = fm.getAscent();

        // Draw a rectangle with the node's value if node isn't shadow
        if (!((n.getType()).equals(Node.SHADOW)))
        {
            if (((searchOperation()) && ((n.getType()).equals(Node.SELECTED))) ||
               (!searchOperation()))
            {
            	offgraphics.setColor(Color.white);
                offgraphics.fillRect(x-10,y-10,17,h);
                offgraphics.setColor(Color.black);
                offgraphics.drawRect(x-10,y-10,17,h);

                if (whatToDraw.equals(drawType[0]))
                {
                	int w = fm.stringWidth(n.getValue())+2;
        	        offgraphics.drawString(n.getValue(), x-(w/2), y-11+h);
        	    }
                else if (whatToDraw.equals(drawType[1]))
                {
                	int w = fm.stringWidth(n.getHeight()+"")+2;
        	        offgraphics.drawString(n.getHeight()+"", x-(w/2), y-11+h);
        	    }
                else if (whatToDraw.equals(drawType[2]))
                {
                	int w = fm.stringWidth(n.getLevel()+"")+2;
        	        offgraphics.drawString(n.getLevel()+"", x-(w/2), y-11+h);
        	    }
                else if (whatToDraw.equals(drawType[3]))
                {
                	int w = fm.stringWidth(n.getAVLBalance()+"")+2;
        	        offgraphics.drawString(n.getAVLBalance()+"", x-(w/2), y-11+h);
        	    }
                else if (whatToDraw.equals(drawType[4]))
                {
                   	int w = fm.stringWidth("Fr"+2);
                    if (n.isFront())
            	        offgraphics.drawString("Fr", x-(w/2), y-11+h);
            	    else
            	        offgraphics.drawString("Ba", x-(w/2), y-11+h);
            	}
                else if (whatToDraw.equals(drawType[5]))
                {
                	int w = fm.stringWidth(n.getAALevel()+"")+2;
        	        offgraphics.drawString(n.getAALevel()+"", x-(w/2), y-11+h);
        	    }
            }
        }

        // Draw an image depending on the node-type
        if (n.getType().equals(Node.ROOT))
             	offgraphics.drawImage(ball[0],x,y, center);
        else if (n.getType().equals(Node.NORMAL))
             	offgraphics.drawImage(ball[1],x,y, center);
        else if (n.getType().equals(Node.SHADOW))
             	offgraphics.drawImage(ball[2],x,y, center);
        else if (n.getType().equals(Node.SELECTED))
             	offgraphics.drawImage(ball[3],x,y, center);
        else if (n.getType().equals(Node.LOOSE))
             	offgraphics.drawImage(ball[4],x,y, center);
        else offgraphics.drawImage(ball[4],x,y, center);

        // Paint the unbalanced node
/*      if ((unbalancedNode != null) && ((n.getValue()).equals(unbalancedNode.getValue())))
          offgraphics.drawImage(ball[3],x,y, center);*/
    }




    /** Paints the tracer-ball on the screen.
    *
    * @see TreeTemplate#tracer
    */
    public void paintTracer()
    {
        if (tracer.getCenter() != null)
        {
            offgraphics.drawImage(tracerBall,tracer.getCenter().x - tracer.getRadius(),
                                  tracer.getCenter().y - tracer.getRadius(), center);
        }
    }




    /** Returns a vector with all the numbers between 1 and 99 found
    * in the string received.
    *
    * @param text the string to be parsed
    */
    public Vector parseInput(String text)
    {
        Vector v        = new Vector(20,20);
        String number   = "";

        // Remove white-spaces at each end of string
        text.trim();

        // Push all numbers seperated by any character to a vector (exept the last number)
        for (int i=0; i<text.length(); i++)
        {
            int c = (int)text.charAt(i);

            // If a zero was encountered, only add it if it's not the first character of the number
            if ((c == 48) && (number.length() > 0))
                number += (""+(c-48));

            // character is an integer between 1 and 9
            else if ((c > 48) && (c < 58))
            {
                number += (""+(c-48));
            }

            // character isn't an integer. Remember the integer we have up until now (if between 1 and 99)
            else if ((number.length() > 0) && (number.length() < 3))
            {
                v.addElement(number);
                number = "";
            }
        }

        // If text ended with a number, it has not been added yet. Add it now.
        if ((number.length() > 0) && (number.length() < 3))
            v.addElement(number);

        return v;
    }


    

    /** Returns true if the current operation is postorder iteration.
    *
    * @see TreeTemplate#operation
    * @see BinaryTreesome#getChosenIteration
    */
    public boolean postorderOperation()
    {
        if (operation==null)
            return false;
        else if ((operation.equals(BinaryTreesome.menuItem[3])) && ((mainClass.getChosenIteration()).equals(BinaryTreesome.iteration[2])))
            return true;
        return false;
    }




    /** Returns true if the current operation is preorder iteration.
    *
    * @see TreeTemplate#operation
    * @see BinaryTreesome#getChosenIteration
    */
    public boolean preorderOperation()
    {
        if (operation==null)
            return false;
        else if ((operation.equals(BinaryTreesome.menuItem[3])) && ((mainClass.getChosenIteration()).equals(BinaryTreesome.iteration[1])))
            return true;
        return false;
    }


    
    
    /** Prints the numbers that are left to be inserted (if any).
    * The information is output in the bottom panel by the
    * function writeProgress.
    *
    * @see TreeTemplate#toBeInserted
    * @see BottomPanel#writeProgress
    */
    public void printLeftToBeInserted()
    {
        String progress = "Numbers left to be inserted: ";

        // Still numbers in the stack, write the ones to come
        if (!toBeInserted.isEmpty())
        {
				if (!((getMover() != null) && ((getMover()).isAlive())))
	            top.setChangeButton();

            // If a loose node exists, add it's number
            if (looseNode != null)
                progress += looseNode.getValue() + ", ";

            // Add all the numbers that is to be inserted to a string
            for (int i=toBeInserted.size()-1; i>0; i--)
                progress += ((Vector)toBeInserted).elementAt(i) + ", ";

            progress += ((Vector)toBeInserted).elementAt(0) + ".";

        }

        // No values in stack, but a loose node exists
        else if (looseNode != null)
        {
				if (!((getMover() != null) && ((getMover()).isAlive())))
	            top.setChangeButton();
            progress += looseNode.getValue() + ".";
        }

        // No values anywhere, everything is inserted
        else
        {
				if (!((getMover() != null) && ((getMover()).isAlive())))
	            top.setRandomButton();
	         progress = InfoMessage.allNodesInserted();
        }

        bottom.writeProgress(progress);
        updateMessages();
    }




    /** Restores all the node's original types.
    * The root node is set to type ROOT and the others to NORMAL.
    *
    * @see Node#setType
    * @see Node#NORMAL
    * @see Node#ROOT
    */
    public void restoreNodes()
    {
        if (!normalNodes.isEmpty())
        {
            for (Enumeration e = normalNodes.elements(); e.hasMoreElements();)
            {
                ((Node)e.nextElement()).setType(Node.NORMAL);
            }
        }

        if (!((rootNode.getType()).equals(Node.SHADOW)))
            rootNode.setType(Node.ROOT);
    }




    /** Reverses the stack received.
    * The stack received and a reversed copy is returned.
    *
    * @param s the stack that is to be reversed
    */
    public Stack reverseStack(Stack s)
    {
        Stack other = new Stack();
        while (!s.empty())
            other.push((String)s.pop());
        return other;
    }




    /** Called when user clicks on the search button in the menu.
    * Sets the operation variable to search, and outputs information
    * to the user if searching is possible.
    *
    * @see TreeTemplate#operation
    */
    public void search()
    {
        // Remember the current operation
        operation = BinaryTreesome.menuItem[2];

        bottom.writeProgress("");

        // Don't print operation mode in the topPanel if no normal rootNode exists
        if ((rootNode != null) && (!((rootNode.getValue()).equals(Node.SHADOW_VALUE))))
        {
            top.readyForSearchInput(true);
    	      top.setRandomButton();
            bottom.writeInfo(InfoMessage.inputSearchValue());
            top.writeOperation(InfoMessage.searchOperation());
        }
        else
        {
            // No current operation
            operation = null;

            // Don't take more input from user
            top.readyForSearchInput(false);
            bottom.writeInfo(InfoMessage.emptyTree());
            bottom.writeError(ErrorMessage.operationNotPossible());
            top.writeOperation("");
        }
        center.repaint();
    }




    /** Decides what should happen when the mouse button is clicked
    * when searching.
    * If clicked inside a nodethe function checks if it is the
    * right one, if so it is selected.
    *
    * @param point the point where the mouse was clicked
    * @see TreeTemplate#searchOrder
    */
    public void searchClick(Point point)
    {
       if (searchOrder.empty()) return;

        String number   = (String)searchOrder.peek();
        Node temp       = null;

        // Look at all nodes
        if (!normalNodes.isEmpty())
        {
            for (Enumeration enum = normalNodes.elements(); enum.hasMoreElements();)
            {
                temp = (Node)enum.nextElement();

                // The node is in the front, and is clicked on by the user
                if ((temp.isFront()) && (temp.contains(point)))
                {
                    // Clear earlier error messages
                    bottom.writeError("");

                    // User clicked on the right node in the tree
                    if ((temp.getValue()).equals(number))
                    {
                        // Don't take more input from user
                        top.readyForSearchInput(false);

                        // Change color of the ball that was clicked (selected)
                        temp.setType(Node.SELECTED);
                        center.repaint();

                        // Output the users iteration progress and pop the value from our stack
                        searchOrder.pop();

                        // If no more numbers in stack, iteration is finised. Give message.
                        if (searchOrder.empty())
                        {
                            // Take more input from user
                            top.readyForSearchInput(true);
                            if (!(number.equals(searchValue)))
                                bottom.writeInfo(InfoMessage.searchValueNotFound());
                            else
                                bottom.writeInfo(InfoMessage.finishedSearch());

                            bottom.writeProgress("");
                            searchValue = "";
                        }
                    }

                    // The node the user clicked on wasn't the right one
                    else
                        bottom.writeError(ErrorMessage.wrongNode());

                    return;
                }
            }
        }
    }




    /** Returns true if the current operation is searching.
    *
    * @see TreeTemplate#operation
    */
    public boolean searchOperation(){return (operation==null?false:(operation.equals(BinaryTreesome.menuItem[2])?true:false));}

    
    
    
    /** Sets the images to be drawn for each of the different types of nodes.
    *
    * @param image the array that holds the names of the images
    * @param num the number of images in the array
    * @see TreeTemplate#ball
    * @see TreeTemplate#tracerBall
    */
    public static void setImages(Image image[], int num)
    {
        ball = new Image[num-1];

        for (int i=0; i<num-1; i++)
            ball[i] = image[i];

        tracerBall = image[num-1];
    }




    /** Puts the numbers left to be inserted in the input box.
    * The numbers are sent as a string to TopPanel's setInput
    * funtion. The stack toBeInserted is left empty.
    *
    * @see TreeTemplate#toBeInserted
    * @see TreeTemplate#printLeftToBeInserted
    * @see TopPanel#setInput
    */
    public void setInput()
    {
        String numbers = "";

        // If a loose node exists, add it's number
        if (looseNode != null)
        {
            numbers += looseNode.getValue() + " ";
            looseNode = null;
        }

        // Add all the numbers that is to be inserted to a string
        for (int i=toBeInserted.size()-1; i>=0; i--)
            numbers += ((Vector)toBeInserted).elementAt(i) + " ";

        // Remove all numbers to be inserted
        ((Vector)toBeInserted).removeAllElements();

        // Write the string into the input-area
        top.setInput(numbers);

        // Output the numbers left to be inserted (none)
        printLeftToBeInserted();
        center.repaint();
    }




    /** Sets the operation currently in progress.
    *
    * @see TreeTemplate#operation
    */
    public void setOperation(String o){operation = o;}




    /** Sets the search value.
    * The variable searchValue is set to the string received
    *
    * @param s the string that is to be the new search value
    * @see TreeTemplate#operation
    */
    public void setSearchValue(String s){searchValue = s;}



    
    /** Opens a window  to show the contents of a file.
    * Opens the file and passes it on to class Window's outPutAlgorithm
    * function together with the title of the window.
    *
    * @see Window#outputAlgorithm
    */
    public void showWindow(String title, String fileName)
    {
        // The file allready read isn't the same as the onme to be shown.
        if (!(window.getFilename()).equals(fileName))
        {
            // Remember the filename and read it
            window.setFilename(fileName);
            window.readFile((mainClass.getCodeBase()).toString(),fileName);
        }

        // File allready read. Show the text in the window
        window.outputAlgorithm(title);
    }

    
    
    
    /** Solves the delete operation for the tree.
    * The deletion started by the user is completed and
    * the tree restructured.
    *
    * @see TreeTemplate#busyDeleting
    * @see TreeTemplate#doubleDeletion
    * @see TreeTemplate#deletedNode
    * @see TreeTemplate#replacementNode
    * @see TreeTemplate#findMin
    */
    public void solveDeleteOperation()
    {
        if (deletedNode == null)
           return;

        bottom.writeInfo("Solving deletion of a node...");

        // Double deletion was necessary.
        if (doubleDeletion)
        {
            // Find the leftMost node in the deleted node's right subtree
            // (selectedNode is set to point to the leftmost node)
            findMin(deletedNode.getRight());
            replacementNode = selectedNode;

            // Both the deleted node and the replacementnode must
            // be removed from the hash because their value will be changed
            normalNodes.remove(deletedNode.getValue());
            normalNodes.remove(replacementNode.getValue());

            // Remember the deleted node`s value
            String value = deletedNode.getValue();

            // The deleted node must now take the replacementNode's value
            // and vice versa.
            deletedNode.setValue(replacementNode.getValue());
            replacementNode.setValue(value);

            // The deleted node must also be restored to the correct type
            if (deletedNode.getParent() != null)
                deletedNode.setType(Node.NORMAL);
            else
                deletedNode.setType(Node.ROOT);
            
            deletedNode.setCenter(replacementNode.getCenter());

            // deletedNode and replacementNode must be added to the hash
            // again with their new values as the key
            normalNodes.put(deletedNode.getValue(), deletedNode);
            normalNodes.put(replacementNode.getValue(), replacementNode);

            // Now, the new deleted node must be set to point to the empty hole
            // where the replacement node used to be.
            deletedNode = replacementNode;

            // The replacementNode has been put into the right hole, and there is
            // no longer any replacementNode or selectdeNode available
            replacementNode = null;
            selectedNode = null;

            // Set flag to indicate that the double deletion has been done, and only
            // the single deletion is left
            doubleDeletion = false;

            // The flag over is set, because if the node you dragged into place had
            // a child, the child must be moved to the hole where the replacementNode
            // used to be. There's one exception, and that is that the node that
            // was dragged in place didn't have any children. Test this, and if it
            // is true, clean up and finish deletion.
            if  ((deletedNode.getLeft() == null) ||
                ((((deletedNode.getLeft()).getType()).equals(Node.SHADOW)) &&
                (((deletedNode.getRight()).getType()).equals(Node.SHADOW))))
            {
                // "Delete" the node's shadows if any
                if (deletedNode.getLeft() != null)
                {
                    shadowNodes.removeElement(deletedNode.getLeft());
                    shadowNodes.removeElement(deletedNode.getRight());
                }

                // "Delete" the node and add it to the shadow-vector
                normalNodes.remove(deletedNode.getValue());
                deletedNode.setRight(null);
                deletedNode.setLeft(null);
                deletedNode.setValue(Node.SHADOW_VALUE);
                deletedNode.setHeight(-1);
                deletedNode.setType(Node.SHADOW);
                shadowNodes.addElement(deletedNode);

                // deletedNode's parent and grandparents and so on will have new balances. Remember
                // the deletedNode's parent to check for an imbalanced tree later if necessary
                unbalancedNode = deletedNode.getParent();

                // Clean up
                busyDeleting = false;
                return;
            }
        }

        // Single deletion is necessary.
        if (!(doubleDeletion)) //deletedNode.getRight() == replacementNode) || (deletedNode.getLeft() == replacementNode))
        {
            // Remove the node's one shadow-child
            if (((deletedNode.getLeft()).getType()).equals(Node.SHADOW))
            {
                replacementNode = deletedNode.getRight();
                shadowNodes.removeElement(deletedNode.getLeft());
            }
            else
            {
                replacementNode = deletedNode.getLeft();
                shadowNodes.removeElement(deletedNode.getRight());
            }

            // Update pointers that used to point to the node that was deleted
            replacementNode.setParent(deletedNode.getParent());

            // Root was not deleted
            if (deletedNode.getParent() != null)
            {
                // Update deleted node's parent's child-pointer
                if (deletedNode == (deletedNode.getParent()).getLeft())
                    (deletedNode.getParent()).setLeft(replacementNode);
                else
                    (deletedNode.getParent()).setRight(replacementNode);

                // Update the neighbour-pointer
                replacementNode.setNeighbours(deletedNode.getNeighbour());
            }

            // Root was deleted - update our private rootNode variable
            else
            {
                replacementNode.setNeighbour(null);
                rootNode = replacementNode;
            }

            // Set the type of the node that was dragged in place
            if (deletedNode.getParent() != null)
                replacementNode.setType(Node.NORMAL);
            else
                replacementNode.setType(Node.ROOT);

            // All pointers are updated. Safe to delete "deletedNode" (i.e.
            // remove it from our hash of nodes)
            normalNodes.remove(deletedNode.getValue());

            // ReplacementNode's parent and grandparents and so on will have new balances. Remember
            // the replacementNode's parent to check for an imbalanced tree later if necessary
            unbalancedNode = replacementNode.getParent();

            // Clean up
            busyDeleting = false;
        }
    }




    /** Solves the inorder operation for the tree.
    * The iteration started by the user is completed by letting
    * a small tracer ball move through the tree and select the nodes
    * in the correct order.
    *
    * @see TreeTemplate#tracer
    * @see TreeTemplate#updateInorder
    */
    public void solveInorderIteration()
    {
        // Restore tree and repaint
        restoreNodes();
        center.repaint();

        // Get ready for new iteration
        order.removeAllElements();
        updateInorder(rootNode);
        order = reverseStack(order);

        // Write progress message
        bottom.writeProgress("Nodes in inorder: ");
        bottom.writeInfo("Solving inorder iteration...");

        // StandardTree - not sorted
        if ((mainClass.getChosenTree()).equals(BinaryTreesome.treeType[1]))
            tracer = new Tracer(center,bottom,order,false);
        else
            tracer = new Tracer(center,bottom,order,true);
        tracer.setRoot(rootNode);
        tracer.start();
    }




    /** Solves the insert operation for the tree.
    * The insertion of the current looseNode is finished by
    * moving it through the tree to it's correct position
    * and inserting it there.
    *
    * The actual moving is done by the mover thread.
    *
    * @see TreeTemplate#mover
    */
    public void solveInsertOperation()
    {
        // No looseNode, make one
        if (looseNode == null)
            return;

        selectedNode = looseNode;

        bottom.writeInfo("Solving insertion of a node...");

        if ((mover == null) || (!mover.isAlive()))
        {
            mover = new MoveLoose(rootNode,selectedNode,mainClass,center,bottom);
            mover.start();
        }
    }




    /** Solves the preorder operation for the tree.
    * The iteration started by the user is completed by letting
    * a small tracer ball move through the tree and select the nodes
    * in the correct order.
    *
    * @see TreeTemplate#tracer
    * @see TreeTemplate#updatePreorder
    */
    public void solvePreorderIteration()
    {
        // Restore tree and repaint
        restoreNodes();
        center.repaint();

        // Get ready for new iteration
        order.removeAllElements();
        updatePreorder(rootNode);
        order = reverseStack(order);

        // Write progress message
        bottom.writeProgress("Nodes in preorder: ");
        bottom.writeInfo("Solving preorder iteration...");

        // StandardTree - not sorted
        if ((mainClass.getChosenTree()).equals(BinaryTreesome.treeType[1]))
            tracer = new Tracer(center,bottom,order,false);
        else
            tracer = new Tracer(center,bottom,order,true);
        tracer.setRoot(rootNode);
        tracer.start();
    }




    /** Solves the postorder operation for the tree.
    * The iteration started by the user is completed by letting
    * a small tracer ball move through the tree and select the nodes
    * in the correct order.
    *
    * @see TreeTemplate#tracer
    * @see TreeTemplate#updatePostorder
    */
    public void solvePostorderIteration()
    {
        // Restore tree and repaint
        restoreNodes();
        center.repaint();

        // Get ready for new iteration
        order.removeAllElements();
        updatePostorder(rootNode);
        order = reverseStack(order);

        // Write progress message
        bottom.writeProgress("Nodes in postorder: ");
        bottom.writeInfo("Solving postorder iteration...");

        // StandardTree - not sorted
        if ((mainClass.getChosenTree()).equals(BinaryTreesome.treeType[1]))
            tracer = new Tracer(center,bottom,order,false);
        else
            tracer = new Tracer(center,bottom,order,true);
        tracer.setRoot(rootNode);
        tracer.start();
    }



    
    /** Solves the search operation for the tree.
    * The search started by the user is completed by letting
    * a small tracer ball move down through the tree until it
    * finds the node that was searched for or hits bottom.
    *
    * @see TreeTemplate#tracer
    * @see TreeTemplate#searchOrder
    * @see TreeTemplate#updateSearchOrder
    */
    public void solveSearchOperation()
    {
        restoreNodes();
        
        searchOrder.removeAllElements();
        updateSearchOrder(rootNode);
        searchOrder = reverseStack(searchOrder);

        top.readyForSearchInput(false);
        bottom.writeInfo("Solving search for number " + searchValue + "...");

        // StandardTree - not sorted
        if ((mainClass.getChosenTree()).equals(BinaryTreesome.treeType[1]))
            tracer = new Tracer(center,bottom,searchOrder,false);
        else
            tracer = new Tracer(center,bottom,searchOrder,true);
        tracer.setRoot(rootNode);
        tracer.start();
    }




    /** Sets all nodes in a subtree to front except the ones at the bottom,
    * where one (the right) is set to back.
    * Recursive function that starts at the node received.
    *
    * @param n the root node of the subtree
    */
    public void updateFronts(Node n)
    {
        // The node sent to the function exists and is not at the bottom. Set it to
        // front and call the function again for each of its children
        if (n != null)
        {
             if (n.getLevel() < MAXLEVEL)
                 n.setFront();
             else
             {
                if (n == ((n.getParent()).getLeft()))
                    n.setFront();
                else
                    n.setBack();
             }
             updateFronts(n.getLeft());
             updateFronts(n.getRight());
        }
    }




    /** Updates the height of all the nodes in a subtree.
    * Recursive function that starts at the node received.
    *
    * @param n the root node of the subtree
    */
    public void updateHeights(Node n)
    {
        // Zero all heights
        n.setHeight(0);
        
        if ((n.getLeft() != null) && (!(((n.getLeft()).getType()).equals(Node.SHADOW))))
            updateHeights(n.getLeft());

        if ((n.getRight() != null) && (!(((n.getRight()).getType()).equals(Node.SHADOW))))
            updateHeights(n.getRight());

        Node p = n.getParent();
        int h  = n.getHeight();

        // Update parents height
        if ((p != null) && ((h+1)>p.getHeight()))
            p.setHeight(n.getHeight()+1);
    }


    
    
    /** Updates the stack order with the node's values in inorder.
    * Recursive function that starts at the node received.
    *
    * @param n the node to start from
    * @see TreeTemplate#order
    */
    public void updateInorder(Node n)
    {
        if ((n.getLeft() != null) && (!(((n.getLeft()).getType()).equals(Node.SHADOW))))
            updateInorder(n.getLeft());

        order.push(n.getValue());

        if ((n.getRight() != null) && (!(((n.getRight()).getType()).equals(Node.SHADOW))))
            updateInorder(n.getRight());
    }




    /** Updates the levels of all the nodes in a subtree.
    * The levels are set relative to the node received.
    * Recursive function that starts at the node received.
    *
    * @param n the node to start from
    * @see Node#level
    * @see Node#setLevel
    */
    public void updateLevel(Node n)
    {
        // Set root's level to 0
        if (n.getParent() == null)
           n.setLevel(0);

        // Update all node's levels until bottom is reached
        else
            n.setLevel((n.getParent()).getLevel()+1);

        if (n.getLeft() != null)
        {
            updateLevel(n.getLeft());
            updateLevel(n.getRight());
        }
    }




    /** Finds the lowest level in a subtree.
    * The static varible lowestLevel in class Node is
    * set to the lowest level in the subtree.
    * Recursive function that starts at the node received.
    *
    * @param n the node to start from
    * @see Node#setLowestLevel
    * @see Node#getLevel
    */
    public void updateLowestLevel(Node n)
    {
        if ((n.getLevel() > Node.getLowestLevel()) && (n.getLevel() < MAXLEVEL))
            Node.setLowestLevel(n.getLevel());

        if (n.getLeft() != null)
        {
            updateLowestLevel(n.getLeft());
            updateLowestLevel(n.getRight());
        }
    }




    /** Clears error messages and outputs info on how to
    * perform the current operation.
    *
    * @see BottomPanel#writeError
    * @see BottomPanel#writeInfo
    */
    public void updateMessages()
    {
        bottom.writeError("");

        if (insertOperation())
        {
            if (busyRotating)
                bottom.writeInfo(InfoMessage.notBalanced());
            else
            {
                if ((!toBeInserted.isEmpty()) || (looseNode != null))
                    bottom.writeInfo(InfoMessage.howToInsert());
                else
                    bottom.writeInfo(InfoMessage.inputInsertValues());
            }
        }
        else if (deleteOperation())
        {
            if ((mainClass.getChosenTree()).equals(BinaryTreesome.treeType[1]))
                bottom.writeInfo(InfoMessage.howToDeleteStandard());
            else
            {
                if (!busyDeleting)
                {
                    if (!busyRotating)
                        bottom.writeInfo(InfoMessage.howToDelete());
                    else
                        bottom.writeInfo(InfoMessage.notBalanced());
                }
                else
                    bottom.writeInfo(InfoMessage.howToReplace());
            }
                
        }
        else if (searchOperation())
        {
            if ((mainClass.getChosenTree()).equals(BinaryTreesome.treeType[1]))
                bottom.writeInfo(InfoMessage.howToSearchStandard());
            else
            {
                if (searchValue.equals(""))
                    bottom.writeInfo(InfoMessage.inputSearchValue());
                else                
                    bottom.writeInfo(InfoMessage.howToSearch());
            }
        }
        else if ((inorderOperation()) || (preorderOperation()) || (postorderOperation()))
        {
            if (!order.empty())
                bottom.writeInfo(InfoMessage.howToIterate());
        }
    }




    /** Updates the numbers to be inserted.
    * The numbers in the input box will either replace the numbers allready in the
    * toBeInserted stack or be added to them depending on the type received.
    * The types are defined in BinaryTreesome's static variable inputType.
    *
    * @param type the type of update to be performed
    * @see BinaryTreesome#inputType
    * @see TreeTemplate#toBeInserted
    * @see BottomPanel#writeInfo
    */
    public void updateNumbers(String type)
    {
        // User is dragging a node around. Don't allow new input while this is
        // going on!
        if (selectedNode != null)
           return;

        // if looseNode exists, push it back on our stack, so that the last number entered in
        // the input-area will be the first to be put in place
        if (looseNode != null)
            toBeInserted.push(looseNode.getValue());

        looseNode = null;

        // Reverse the stack first to get values in right order
        toBeInserted = reverseStack(toBeInserted);

        // Numbers in our stack are to be changed, remove old stuff
        if (type.equals(BinaryTreesome.inputType[1]))
            ((Vector)toBeInserted).removeAllElements();

        Vector v = parseInput(top.getInput());

        // Push all the numbers from the vector onto our reversed stack
        for (int i=0; i<v.size(); i++)
            if ((!(normalNodes.containsKey(v.elementAt(i)))) && (!(toBeInserted.contains(v.elementAt(i)))))
                toBeInserted.push(v.elementAt(i));

        // Reverse the stack back to it's right way
        toBeInserted = reverseStack(toBeInserted);

        // Clear the input-area
        top.clearInput();

        // Generate a loose node if possible
        // Generate and activate a new node that can be dragged into place if no rotation needed
        if (!busyRotating)
            generateLooseNode();
        else
        {
            printLeftToBeInserted();
            bottom.writeInfo(InfoMessage.notBalanced());
        }

        center.repaint();
    }




    /** Updates the stack order with the node's values in postorder.
    * Recursive function that starts at the node received.
    *
    * @param n the node to start from
    * @see TreeTemplate#order
    */
    public void updatePostorder(Node n)
    {
        if ((n.getLeft() != null) && (!(((n.getLeft()).getType()).equals(Node.SHADOW))))
            updatePostorder(n.getLeft());

        if ((n.getRight() != null) && (!(((n.getRight()).getType()).equals(Node.SHADOW))))
            updatePostorder(n.getRight());

        order.push(n.getValue());
    }




    /** Updates the stack order with the node's values in preorder.
    * Recursive function that starts at the node received.
    *
    * @param n the node to start from
    * @see TreeTemplate#order
    */
    public void updatePreorder(Node n)
    {
        order.push(n.getValue());

        if ((n.getLeft() != null) && (!(((n.getLeft()).getType()).equals(Node.SHADOW))))
            updatePreorder(n.getLeft());

        if ((n.getRight() != null) && (!(((n.getRight()).getType()).equals(Node.SHADOW))))
            updatePreorder(n.getRight());
    }




    /** Updates the stack searchOrder.
    * The stack is updated with the values of the nodes in
    * the order they should be checked during a search
    * for the number given in searchValue.
    * Recursive function that starts at the node received.
    *
    * @param n the node to start from
    * @see TreeTemplate#searchOrder
    * @see TreeTemplate#searchValue
    */
    public void updateSearchOrder(Node n)
    {
        searchOrder.push(n.getValue());

        if (((n.getLeft() != null) && (!(((n.getLeft()).getType()).equals(Node.SHADOW)))) &&
            (Integer.parseInt(searchValue) < Integer.parseInt(n.getValue())))
            updateSearchOrder(n.getLeft());

        else if (((n.getRight() != null) && (!(((n.getRight()).getType()).equals(Node.SHADOW)))) &&
            (Integer.parseInt(searchValue) > Integer.parseInt(n.getValue())))
            updateSearchOrder(n.getRight());
    }

    
    
    
    /** Updates the value to be searched for.
    * The variable is updated with the number in the input box
    * if it exists. If it doesn't, and the type is change the current
    * search value is output in the input box for a possible change.
    *
    * @param type the type of update to be done
    * @see TreeTemplate#searchValue
    * @see TreeTemplate#getSearchInput
    */
    public void updateSearchValue(String type)
    {
        // Change button pressed and no text exists in the textfield allready.
        // This means that a search value exists. Put the searchvalue into textbox 
        // for possible change and return. Otherwise set the searchValue to the input 
        // (if it is a legal number). (This is done in the function "getSearchInput".
        if ((type.equals(BinaryTreesome.inputType[1])) && 
           (((top.getInput()).length() == 0) && (!(searchValue.equals("")))))
        {
            top.setInput(searchValue);
            searchValue = "";
            bottom.writeProgress(InfoMessage.whatToSearch());
            bottom.writeInfo(InfoMessage.inputSearchValue());
            return;
        }
        
        getSearchInput();
    }




    /** Called when user clicks on the view button in the menu.
    * This function should be overrided by a function in the class that
    * inherits this class. If not, an error message is output from
    * this function.
    */
    public void view()
    {
        bottom.writeError("The \"View\"-button is not yet programmed for the tree you've chosen!!!");
    }
}




// -------------------------------- End of class ------------------------------
