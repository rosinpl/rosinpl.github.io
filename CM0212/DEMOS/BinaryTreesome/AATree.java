// ---------------------------------- Imports ---------------------------------




import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;




// ------------------------------- Start of class -----------------------------




/**
* A class representing an AA tree.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
* @see            TreeTemplate
*/
public class AATree extends TreeTemplate
{
    // ----- Variables ----

    /** Flag that says if a left rotation is in order or not.
    */
    private boolean rotateLeft = false;
    
    // ---------------------------- Constructor -------------------------------




    /** Calls the superclass constructor with the same parameters.
    *
    * @param b the panel where messages are output
    * @param p the top panel, used for input and output
    * @param c the panel where the tree is to be painted
    * @param a the main applet class
    * @see TreeTemplate#TreeTemplate
    * @see BottomPanel
    * @see TopPanel
    * @see CenterPanel
    * @see BinaryTreesome
    */
    public AATree(BottomPanel b, TopPanel p, CenterPanel c, BinaryTreesome a){super(b,p,c,a);}




    // ----------------------- Mouse-related functions ------------------------




    /** Decides what is going to happen when a mouse button is clicked
    * in an AA Tree.
    * Action to be taken depends on which operation is in progress.
    *
    * @param e the mouse event
    */
    public void mouseClicked(MouseEvent e)
    {
        // Mouseclick not allowed during solving an iteration or search
        if (((getTracer() != null) && ((getTracer()).isAlive())) ||
            ((getMover() != null) && ((getMover()).isAlive())))
        {
            return;
        }

        super.updateMessages();

        // If two nodes overlap, one can only bring the node in the back to front if
        // one clicks over the area that isn't overlapped by another node.

        Node temp = null;
        Point mp = new Point(e.getX(),e.getY());

        // Check if user clicked over a shadow node that was in the back. If so
        // - bring it to front, and send it's neighbour to back
        if (!shadowNodes.isEmpty())
        {
            for (Enumeration enum = shadowNodes.elements(); enum.hasMoreElements();)
            {
                temp = (Node)enum.nextElement();

                // Node is in the back, it contains the mouse-point and it's neighbour doesn't
                if ((!temp.isFront()) && (temp.contains(mp)) && (!(temp.getNeighbour()).contains(mp)))
                {
                   temp.setFront();
                   (temp.getNeighbour()).setBack();
                   center.repaint();
                   return;
                }
            }
        }

        // Check if user clicked over a normal node that was in the back. If so
        // - bring it to front, and send it's neighbour to back
        if (!normalNodes.isEmpty())
        {
            for (Enumeration enum = normalNodes.elements(); enum.hasMoreElements();)
            {
                temp = (Node)enum.nextElement();
                if ((!temp.isFront()) && (temp.contains(mp)) && (!(temp.getNeighbour()).contains(mp)))
                {
                   temp.setFront();
                   (temp.getNeighbour()).setBack();
                   center.repaint();
                   return;
                }
            }
        }

        // If in insertion-mode and rotation is needed, call appropriate function to deal with the click
        if ((insertOperation()) && (busyRotating))
        {
             rotateClick(mp);
        }

        // If in deletion-mode, call appropriate function to deal with the click
        else if (deleteOperation())
        {
             initiateDeleteClick(mp);
        }

        // If in iteration-mode, call appropriate function to deal with the click
        else if ((inorderOperation()) || (preorderOperation()) || (postorderOperation()))
        {
             super.checkOrderClick(mp);
        }

        // If in search-mode, call appropriate function to deal with the click
        else if (searchOperation())
        {
             super.searchClick(mp);
        }
    }




    /** Decides what is going to happen when a mouse button is released
    * in an AA Tree.
    * Action to be taken depends on which operation is in progress.
    *
    * @param e the mouse event
    */
    public void mouseReleased(MouseEvent e)
    {
        // Ignore mouserelease during solving an iteration or search
        if (((getTracer() != null) && ((getTracer()).isAlive())) ||
            ((getMover() != null) && ((getMover()).isAlive())))
        {
            return;
        }

        Node overlapped = null;

        // A node is selected, check if it overlaps any of the shadownodes
    	if (selectedNode != null)
    	{
            // ------------ Insertion operation. Try to insert node -----------

            if ((insertOperation()) && (!busyRotating))
            {
                 // shadow nodes exists, check all
                 if (!shadowNodes.isEmpty())
                 {
                     for (Enumeration enum = shadowNodes.elements(); enum.hasMoreElements();)
                     {
                         overlapped = (Node)enum.nextElement();

                         // check if selected node overlaps a shadow
              	         if ((overlapped != null) && (overlapped.isFront()) &&
                             (selectedNode.overlaps(overlapped)))
                         {
                             insertSelectedNode(overlapped);
                             return;
                         }
                     }
                 }

                 // Node wasn't dropped over an acceptable hole. Restore selected
                 // node's color.
                 selectedNode.setType(Node.LOOSE);
                 selectedNode = null;
                 center.repaint();
                 return;
            }

            if ((insertOperation()) && (busyRotating))
            {
                bottom.writeError(ErrorMessage.mustRestructure());
	             bottom.writeProgress("The nodes AA level is shown instead of the numbers. Do you see which one needs to be rotated?");
            }
        }
    }




    // ------------------------ Other functions -------------------------




    /** Checks if right or left rotation is needed.
    * Sets the flags busyRotating and rotateLeft.
    *
    * @param a the node to check
    * @see TreeTemplate#busyRotating
    * @see AATree#rotateLeft
    */
    public void checkForRotation(Node n)
    {
        // Left child exists and has the same level as it'sparent
        if (((n.getLeft()) != null) && (((n.getLeft()).getAALevel()) == n.getAALevel()))
        {
            if (!busyRotating)
            {
                busyRotating   = true;
                rotateLeft     = true;
                unbalancedNode = n;
            }
        }

        // Right child exists and has same level. Check right child's right child also has same level
        else if (((n.getRight()) != null) && (((n.getRight()).getAALevel()) == n.getAALevel()))
        {
            Node child = n.getRight();
            if (((child.getRight()) != null) && (((child.getRight()).getAALevel()) == child.getAALevel()))
            {
                if (!busyRotating)
                {
                    busyRotating   = true;
                    rotateLeft     = false;
                    unbalancedNode = n;
                }
            }
        }
    }




    /** Outputs error and info messages saying that the delete
    * operation isn't implemented yet.
    *
    * @see ErrorMessage#operationNotPossible
    * @see BottomPanel#writeError
    * @see BottomPanel#writeInfo
    */
    public void delete()
    {
        top.writeOperation("");

        if (rootNode.getType().equals(Node.SHADOW))
            operation = null;
        else
            operation = BinaryTreesome.menuItem[1];

        bottom.writeProgress("");
        bottom.writeInfo(InfoMessage.operationNotImplemented());
        bottom.writeError(ErrorMessage.operationNotPossible());
        center.repaint();
    }




    /** Restores the tree after a rotation.
    * Calls a number of functions in this class and the superclass
    * TreeTemplate to restore the tree after a rotation. These
    * functions are stated below.
    *
    * @see AATree#updateAABalance
    * @see TreeTemplate#updateLevel
    * @see TreeTemplate#updateLowestLevel
    * @see TreeTemplate#addShadows
    * @see TreeTemplate#updateFronts
    * @see TreeTemplate#updateHeights
    * @see TreeTemplate#calculatePositions
    * @see TreeTemplate#restoreNodes
    * @see TreeTemplate#generateLooseNode
    */
    public void doneRotating()
    {
        // Clear all error messages
        bottom.writeError("");

        busyRotating = false;
        Node.setLowestLevel(0);

        // Update level information in the tree
        super.updateLevel(rootNode);
        super.updateLowestLevel(rootNode);

        // Add new shadows if needed (i.e. when a node was rotated down)
        super.addShadows(rootNode);

        // Set all nodes to front, except at the bottom level where one must be in the back
        super.updateFronts(rootNode);

        // A node was deleted, and all node's might have a change of level. Update if necessary
        super.updateHeights(rootNode);

        // Update the balance-values of all nodes from the one that was rotated
        updateAABalance(unbalancedNode);

        // Move the nodes to their new positions
        super.calculatePositions(rootNode, rootPos.x, rootPos.y);

        // Restore the types of all nodes
        super.restoreNodes();

        // No new rotation needed.
        if (!busyRotating)
        {
            // Rotation is finished. Generate a new loose node if in insertion mode.
            if (insertOperation())
                super.generateLooseNode();

            unbalancedNode = null;
            whatToDraw = drawType[0];
        }

        // Another rotation is needed. Give message to user
        else
        {
            bottom.writeInfo(InfoMessage.notBalanced());
            bottom.writeProgress("The nodes AA level is shown instead of the numbers. Do you see which one needs to be rotated?");
        }

        center.repaint();
    }




    /** Outputs the correct hint when the hint button is clicked.
    * Which hint is output depends on which operation is in progress.
    * Calls BottomPanel's writeInfo function with messages from
    * class InfoMessage as parameter.
    *
    * @see InfoMessage
    * @see BottomPanel#writeInfo
    */
    public void hint()
    {
        if (insertOperation())
        {
            top.readyForInsertionInput(true);

            if (busyRotating)
                bottom.writeInfo(InfoMessage.AArotationHint());
            else
            {
                if ((!toBeInserted.isEmpty()) || (looseNode != null))
                    bottom.writeInfo(InfoMessage.insertSortedHint());
                else
                    bottom.writeInfo(InfoMessage.inputInsertValues());
            }
        }

        else if (searchOperation())
        {
            if (searchValue.equals(""))
            {
                top.readyForInsertionInput(true);
                bottom.writeInfo(InfoMessage.inputSearchValue());
            }
            else
                bottom.writeInfo(InfoMessage.searchHint());
        }
        else if (inorderOperation())
        {
            if (!order.isEmpty())
                bottom.writeInfo(InfoMessage.inorderHint());
        }
        else if (preorderOperation())
        {
            if (!order.isEmpty())
                bottom.writeInfo(InfoMessage.preorderHint());
        }
        else if (postorderOperation())
        {
            if (!order.isEmpty())
                bottom.writeInfo(InfoMessage.postorderHint());
        }
        else
            bottom.writeInfo(InfoMessage.noHintAvailable());
    }




	 /** This function has not been implemented yet, but is here to
    * prevent the call of treeTemplate's delete-click
    *
    */
    public void initiateDeleteClick(Point mp)
    {
        bottom.writeProgress("");
        bottom.writeInfo(InfoMessage.operationNotImplemented());
        bottom.writeError("");
    }




    /** Called when a node is dropped during insertion.
    * Tries to insert the selected node into the tree, i.e. take
    * the overlapped node's position if it is the correct node.
    *
    * @param overlapped the node which is overlapped by the selected node
    */
    public void insertSelectedNode(Node overlapped)
    {
        // Print what's left to be inserted
        super.printLeftToBeInserted();

        if (!((rootNode.getType()).equals(Node.SHADOW)))
        {
            // Sets positionNode to point to the correct shadownode that the
            // value of the selected node should be dropped over
            super.getSortedPosition(rootNode, Integer.parseInt(selectedNode.getValue()));

            // If the shadow that the selected node should be dropped over isn't
            // equal to the shadow that the selected node actually is overlapping,
            // don't insert the selected node
            if (positionNode != overlapped)
            {
                // No selected node anymore
                selectedNode = null;
                positionNode = null;

                // Restore looseNode's type and reapint the screen
                looseNode.setType(Node.LOOSE);
                center.repaint();

                // Output some info to user
                super.printLeftToBeInserted();
                bottom.writeError(ErrorMessage.unableToInsert());

                return;
            }
        }

        // take shadownode's position and lock node
        selectedNode.setCenter(overlapped.getCenter());
        selectedNode.lock();

        // Update the inserted node, depending on shadow's position
        if (overlapped == rootNode)
        {
            // Shadow is root, set type to root
            selectedNode.setType(Node.ROOT);
            // Update private variable rootNode
            rootNode = selectedNode;
        }
        else
        {
            // Update inserted node's parent pointer, type and level.
            Node parent = overlapped.getParent();
            selectedNode.setParent(parent);
            selectedNode.setType(Node.NORMAL);
            selectedNode.setFront();
            selectedNode.setNeighbours(overlapped.getNeighbour());
            selectedNode.setLevel(overlapped.getLevel());

            // Update parent's child pointer
            if(overlapped == parent.getLeft())
                parent.setLeft(selectedNode);
            else
                parent.setRight(selectedNode);
        }

        // The overlapped shadow-node is being controlled by a thread.
        // The shadow will be deleted, but first give the shadow's thread
        // control over the new node that will take the shadow's place in the tree.
        // (and that node is the "selected" one)
        if (overlapped.hasThread())
        {
            MoveNode t = overlapped.getThread();
            selectedNode.setThread(t);
            if (overlapped.hasThread())
                t.setNode(selectedNode);

            // Pointers are updated, safe to delete the shadow node. The thread shall
            // now continue to move the node that took the shadow-node's position in the tree.
        }

        // Remove the shadownode that was overlapped
        shadowNodes.removeElement(overlapped);

        // Insert the node into our hash with all the normal nodes in the tree
        normalNodes.put(selectedNode.getValue(),selectedNode);

        // Remember the node that was inserted (selectedNode is set to null in afterInsertion)
        Node temp = selectedNode;

        // The selected node is now inserted into the tree. Fix the tree
        super.fixTreeAfterInsertion();

        // Update all node's AABalance-values.
        updateAABalance(temp);

        center.repaint();

        // Generate and activate a new node that can be dragged into place if no rotation needed
        if (!busyRotating)
        {
            whatToDraw = drawType[0];
            super.generateLooseNode();
        }
        else
        {
            whatToDraw = drawType[5];
            super.printLeftToBeInserted();
            bottom.writeInfo(InfoMessage.notBalanced());
            bottom.writeProgress("The nodes AA level is shown instead of the numbers. Do you see which one needs to be rotated?");
        }
    }




    /** Called when the mouse is clicked and the tree is to be rotated.
    *
    * @param point the point where the mouse was clicked
    */
    public void rotateClick(Point point)
    {
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
                    if (temp == unbalancedNode)
                    {
				            bottom.writeProgress("The nodes AA level is shown instead of the numbers. Do you see which one needs to be rotated?");
                        mainClass.play(mainClass.getCodeBase(),"Rotation.au");

                        // ---------- Check for skew or split -------------
                        if (rotateLeft)
                            rotateWithLeftChild(unbalancedNode);
                        else
                            rotateWithRightChild(unbalancedNode);

                        // Clean up after the rotation
                        doneRotating();
                    }


                    // The node the user clicked on wasn't the right one
                    else
                        bottom.writeError(ErrorMessage.wrongNode());

                    return;
                }
            }
        }
    }




    /** Rotates a node with it's right child.
    *
    * @param a the unbalanced node
    */
    public void rotateWithRightChild(Node n)
    {
        // The node that n will rotate with (depending on left or right rotation
        Node b              = n.getRight();

        // Unbalanced node's right child (b) moves up, and b's left child
        // becomes the unbalanced node. The unbalanced node's right child becomes
        // what used to be b's left child. Can I say it more clearly?
        n.setRight(b.getLeft());
        (b.getLeft()).setParent(n);
        b.setLeft(n);
        (b.getRight()).setNeighbours(n);

        // b has moved up and has now two children. It's AALevel increases with one.
        b.setAALevel(b.getAALevel()+1);

        // Remember b's old neighbour and set b's new neighbour to the unbalanced
        // node's old neighbour. Unbalanced new neighbour will be b's left child.
        Node oldNeighbour = b.getNeighbour();
        b.setNeighbours(n.getNeighbour());
        n.setNeighbours(b.getRight());

        // b's old neighbour is now a's right child. Thus it's neighbour
        // will be unbalanced's left child
        oldNeighbour.setNeighbours(n.getRight());

        // b is now one level higher in the tree, and the unbalanced node has
        // moved down one step. Update the levels.
        int oldLevel = b.getLevel();
        b.setLevel(n.getLevel());
        n.setLevel(oldLevel);

        // n wasn't root. Update its parent's child-pointer
        if (n.getParent() != null)
        {
            if (n == (n.getParent()).getLeft())
                (n.getParent()).setLeft(b);
            else
                (n.getParent()).setRight(b);
        }

        // a was root. Update rootPointer and root's position
        else
        {
            rootNode = b;
            rootNode.setDestination(rootPos);
        }

        // Update parent-pointers
        b.setParent(n.getParent());
        n.setParent(b);
        unbalancedNode = n.getParent();
    }




    /** Rotates a node with it's left child.
    *
    * @param a the unbalanced node
    */
    public void rotateWithLeftChild(Node n)
    {
        // The node that n will rotate with (depending on left or right rotation
        Node b              = n.getLeft();

        // Unbalanced node's left child (b) moves up, and b's right child
        // becomes the unbalanced node. The unbalanced node's left child becomes
        // what used to be b's right child. Can I say it more clearly?

        // Trying to perform a left rotation when the node's left child is at the bottom
        // of the tree
       if ((n.getLevel()) == MAXLEVEL-1)
        {
            Node left  = new Node(b.getCenter(),Node.SHADOW_VALUE,-1,Node.SHADOW,b.getLevel(),true,true,b,null,null,null);
            shadowNodes.addElement(left);
            left.setBack();
            n.setFront();

            b.setRight(n);
            b.setLeft(left);
            
            if (((n.getRight()).getType()).equals(Node.SHADOW))
            {
                shadowNodes.removeElement(n.getRight());
                n.setLeft(null);
                n.setRight(null);
            }

            else
            {
                Node anotherLeft  = new Node(n.getCenter(),Node.SHADOW_VALUE,-1,Node.SHADOW,n.getLevel()+1,true,true,n,null,null,null);
                shadowNodes.addElement(anotherLeft);
                (n.getRight()).setFront();
                anotherLeft.setBack();
                n.setLeft(anotherLeft);
                (n.getRight()).setNeighbours(anotherLeft);
                
            }
            b.setNeighbours(n.getNeighbour());
            n.setNeighbours(left);
        }

        // Normal left rotation
        else
        {
            n.setLeft(b.getRight());
            (b.getRight()).setParent(n);
            (b.getLeft()).setNeighbours(n);
            b.setRight(n);

            // Remember b's old neighbour and set b's new neighbour to the unbalanced
            // node's old neighbour. Unbalanced new neighbour will be b's left child.
            Node oldNeighbour = b.getNeighbour();
            b.setNeighbours(n.getNeighbour());
            n.setNeighbours(b.getLeft());

            // b's old neighbour is now a's right child. Thus it's neighbour
            // will be unbalanced's left child
            oldNeighbour.setNeighbours(n.getLeft());
        }


        // b is now one level higher in the tree, and the unbalanced node has
        // moved down one step. Update the levels.
        int oldLevel = b.getLevel();
        b.setLevel(n.getLevel());
        n.setLevel(oldLevel);

        // n wasn't root. Update its parent's child-pointer
        if (n.getParent() != null)
        {
            if (n == (n.getParent()).getLeft())
                (n.getParent()).setLeft(b);
            else
                (n.getParent()).setRight(b);
        }

        // a was root. Update rootPointer and root's position
        else
        {
            rootNode = b;
            rootNode.setDestination(rootPos);
        }

        // Update parent-pointers
        b.setParent(n.getParent());
        n.setParent(b);
        unbalancedNode = n.getParent();
    }




    /** Performs a rotation with the received node's left child.
    *
    * @param a the unbalanced node
    * @see AATree#rotateWithLeftChild
    */
    public void skew(Node n)
    {
        // Left child exists and has the same level as it'sparent
        if (((n.getLeft()) != null) && (((n.getLeft()).getAALevel()) == n.getAALevel()))
            rotateWithLeftChild(n);
    }




    /** Performs a rotation with the received node's right child.
    *
    * @param a the unbalanced node
    * @see AATree#rotateWithRightChild
    */
    public void split(Node n)
    {
        // Right child exists. Check if right child's right child has same level
        if (n.getRight() != null)
        {
            Node child = n.getRight();
            if (((child.getRight()) != null) && (((child.getRight()).getAALevel()) == n.getAALevel()))
                rotateWithRightChild(n);
        }
    }



    
    /** Called when the user clicks the solve button in the menu.
    * Much of the work is done by calling solve functions
    * the superclass TreeTemplate.
    *
    * @see TreeTemplate#solveInsertOperation
    * @see TreeTemplate#solveDeleteOperation
    * @see TreeTemplate#fixTreeAfterDeletion
    * @see TreeTemplate#clearTempVariables
    * @see TreeTemplate#solveSearchOperation
    * @see TreeTemplate#solveInorderIteration
    * @see TreeTemplate#solvePreorderIteration
    * @see TreeTemplate#solvePostorderIteration
    */
    public void solve()
    {
        // No operation in progress. Give error-message and return
        if (operation == null)
        {
            bottom.writeInfo(InfoMessage.noOperation());
            bottom.writeError(ErrorMessage.cantSolveNow());
            return;
        }

        if (insertOperation())
        {
            if (!busyRotating)
            {
                if (looseNode != null)
                    super.solveInsertOperation();
                else
                {
                    bottom.writeInfo(InfoMessage.noNumberToInsert());
                    bottom.writeError(ErrorMessage.cantSolveNow());

                }
            }
            else
                bottom.writeError("Unable to solve the rotation");

            top.readyForInsertionInput(true);
        }

        else if (deleteOperation())
        {
            if (busyDeleting)
            {
                super.solveDeleteOperation();
                super.fixTreeAfterDeletion();
                super.clearTempVariables();
                unbalancedNode = null;
            }

            else
            {
                bottom.writeInfo(InfoMessage.operationNotImplemented());
                bottom.writeError(ErrorMessage.cantSolveNow());
            }
        }
        else if (searchOperation())
        {
            if (searchValue.equals(""))
            {
                top.readyForInsertionInput(true);
                bottom.writeInfo(InfoMessage.noNumberToSearch());
                bottom.writeError(ErrorMessage.cantSolveNow());
            }
            else
                super.solveSearchOperation();
        }
        else if (inorderOperation())
            super.solveInorderIteration();
        else if (preorderOperation())
            super.solvePreorderIteration();
        else if (postorderOperation())
            super.solvePostorderIteration();
    }




    // Recursive function that sets all node's AABalance-values.
    // (The difference in the right's and left's subtrees heights. Rotate if greater than 1)
    /** Sets all the node's AA balance values.
    * Recursive function that updates the variable AALevel for all the nodes
    * by calling Node's setAALevel.
    *
    * @param n the node to check
    * @see Node#setAALevel
    */
    public void updateAABalance(Node n)
    {
        // Node exists, update its balance and call the function for it's parent
        if (n != null)
        {
            // Shadows have no level
            if ((n.getValue()).equals(Node.SHADOW_VALUE))
                n.setAALevel(0);

            // Level is set to number of links to a shadow (or null)
            else if ((n.getLevel() == MAXLEVEL) || (((n.getLeft()).getType()).equals(Node.SHADOW)))
                n.setAALevel(1);

            checkForRotation(n);

            updateAABalance(n.getParent());
        }
    }




    /** Called when user click on the view-button from the menu.
    * Not implemented.
    */
    public void view()
    {
        bottom.writeInfo(InfoMessage.treeNotAvailable());
        bottom.writeError(ErrorMessage.operationNotPossible());
    }
}




// -------------------------------- End of class ------------------------------
