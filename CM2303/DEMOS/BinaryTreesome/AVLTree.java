// ---------------------------------- Imports ---------------------------------




import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;




// ------------------------------- Start of class -----------------------------




/**
* A class representing an AVL tree.
* Holds the functions that are needed to be specifically coded
* for the AVL tree.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
* @see            TreeTemplate
*/
public class AVLTree extends TreeTemplate
{
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
    public AVLTree(BottomPanel b, TopPanel p, CenterPanel c, BinaryTreesome a){super(b,p,c,a);}




    // ----------------------- Mouse-related functions ------------------------




    /** Decides what is going to happen when a mouse button is clicked
    * in an AVL Tree.
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

        // If in iteration-mode, call appropriate function to deal with the click
        else if ((inorderOperation()) || (preorderOperation()) || (postorderOperation()))
        {
             super.checkOrderClick(mp);
        }

        // If in deletion-mode, call appropriate function to deal with the click
        else if (deleteOperation())
        {
            if (!busyRotating)
                 initiateDeleteClick(mp);
            else
                 rotateClick(mp);
        }

        // If in search-mode, call appropriate function to deal with the click
        else if (searchOperation())
        {
             super.searchClick(mp);
        }
    }




    /** Decides what is going to happen when a mouse button is released
    * in an AVL Tree.
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
            }


            // Mouse released during deletion-operation.
            if ((deleteOperation()) && (busyDeleting))
            {
                if (selectedNode.overlaps(deletedNode))
                    initiateDeleteReleased();

                else
                {
                    replacementNode.setType(Node.NORMAL);
                    selectedNode = null;
                    center.repaint();
                }
            }
        }
    }




    // ------------------------ Other functions -------------------------




    /** Restores the tree after a rotation.
    * Calls a number of functions in this class and the superclass
    * TreeTemplate to restore the tree after a rotation. These
    * functions are stated below.
    *
    * @see AVLTree#updateAVLBalance
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

        // Something has been shifted down. If something has landed at the bottom,
        // set one to the front, the other to the back
//        updateBottomFronts(rootNode);

        // Add new shadows if needed (i.e. when a node was rotated down)
        super.addShadows(rootNode);

        // Set all nodes to front, except at the bottom level where one must be in the back
        super.updateFronts(rootNode);

        // A node was deleted, and all node's might have a change of level. Update if necessary
        super.updateHeights(rootNode);

        // Update the balance-values of all nodes from the one that was rotated
        updateAVLBalance(unbalancedNode);

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
            else
                bottom.writeInfo(InfoMessage.howToDelete());

            unbalancedNode = null;
        }

        // Another rotation is needed. Give message to user
        else
            bottom.writeInfo(InfoMessage.notBalanced());

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
                bottom.writeInfo(InfoMessage.AVLrotationHint());
            else
            {
                if ((!toBeInserted.isEmpty()) || (looseNode != null))
                    bottom.writeInfo(InfoMessage.insertSortedHint());
                else
                    bottom.writeInfo(InfoMessage.inputInsertValues());
            }
        }

        else if (deleteOperation())
        {
            if (!busyDeleting)
            {
                if (!busyRotating)
                    bottom.writeInfo(InfoMessage.deleteHint());
                else
                    bottom.writeInfo(InfoMessage.AVLrotationHint());
            }
            else
            {
                if (doubleDeletion)
                    bottom.writeInfo(InfoMessage.doubleDeletionHint());
                else
                    bottom.writeInfo(InfoMessage.singleDeletionHint());
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




    /** Starts the delete procedure and checks if it can be finished.
    * Calls the superclass TreeTemplate's deleteClick function, and then
    * checks if the deletion is finished, i.e. if busyDeleting is false.
    * If it is the superclass TreeTemplate's fixTreeAfterDeletion is
    * called to clean up. Updates the AVL balance if needed.
    *
    * @param mp the point where the mouse was clicked
    * @see TreeTemplate#deleteClick
    * @see TreeTemplate#fixTreeAfterDeletion
    * @see AVLTree#updateAVLBalance
    */
    public void initiateDeleteClick(Point mp)
    {
        super.deleteClick(mp);

        if (!busyDeleting)
        {
            super.fixTreeAfterDeletion();

            // Update all node's AVLBalance-values from the first node necessary and up.
            if (unbalancedNode != null)
                updateAVLBalance(unbalancedNode);

            if (busyRotating)
                bottom.writeInfo(InfoMessage.notBalanced());
            else
                unbalancedNode = null;

            clearTempVariables();
        }
    }




    /** Tries to finish the delete procedure.
    * Calls the superclass TreeTemplate's deleteReleased function, and then
    * checks if the deletion is finished, i.e. if busyDeleting is false.
    * If it is the superclass TreeTemplate's fixTreeAfterDeletion is
    * called to clean up. Updates the AVL balance if needed.
    *
    * @see TreeTemplate#deleteReleased
    * @see TreeTemplate#fixTreeAfterDeletion
    * @see AVLTree#updateAVLBalance
    */
    public void initiateDeleteReleased()
    {
        super.deleteReleased();

        if (!busyDeleting)
        {
            super.fixTreeAfterDeletion();

            // Update all node's AVLBalance-values from the first node necessary and up.
            if (unbalancedNode != null)
                updateAVLBalance(unbalancedNode);

            if (busyRotating)
                bottom.writeInfo(InfoMessage.notBalanced());
            else
                unbalancedNode = null;
                
            clearTempVariables();
        }
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

        // Update all node's AVLBalance-values.
        updateAVLBalance(temp.getParent());

        // Generate and activate a new node that can be dragged into place if no rotation needed
        if (!busyRotating)
            super.generateLooseNode();
        else
        {
            super.printLeftToBeInserted();
            bottom.writeInfo(InfoMessage.notBalanced());
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
                        mainClass.play(mainClass.getCodeBase(),"Rotation.au");

                        // Single deletion if unbalanced's height is 2, and either it's left child's subree exists and is weighted to
                        // the left, or it's right child's subree exists and is weighted to the right,
                        if (((unbalancedNode.getAVLBalance() < 0 ) && (((unbalancedNode.getLeft()).getAVLBalance()) <= 0)) ||
                            ((unbalancedNode.getAVLBalance() > 0 ) && (((unbalancedNode.getRight()).getAVLBalance()) >= 0)))
                        {
                            bottom.writeInfo("Single rotation in progress....but if you have time to read this, something funny's going on... ");
                            rotateNode(unbalancedNode);
                        }

                        // Double rotation needed. If left-double is needed, rotate between (unbalancedNode.getLeft())
                        // and ((unbalancedNode.getLeft()).getRight()), then beween unbalancedNode & (unbalancedNode.getLeft()).
                        // Symmetrical for right-double.
                        else
                        {
                            bottom.writeInfo("Double rotation in progress....but if you have time to read this, something funny's going on... ");

                            // Left double rotation needed. Rotate unbalanced child and grandchild.
                            if (unbalancedNode.getAVLBalance() < 0 )
                                rotateNode(unbalancedNode.getLeft());
                            // Left double rotation needed. Rotate unbalanced child and grandchild.
                            else
                                rotateNode(unbalancedNode.getRight());

                            // Now rotate the unbalanced node to complete the double rotation
                            rotateNode(unbalancedNode);
                        }

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




    /** Rotates two nodes.
    * The unbalanced node is rotated with the correct node
    * according to the rules of rotation in an AVL Tree.
    *
    * @param a the unbalanced node
    */
    public void rotateNode(Node a)
    {
        // The node that a will rotate with (depending on left or right rotation
        Node b              = null;
        Node oldNeighbour   = null;

        // Unbalanced to the left, switch pointers around
        if (a.getAVLBalance() < 0)
        {
            // Unbalanced node's left child (b) moves up, and b's right child
            // becomes the unbalanced node. The unbalanced node's left child becomes
            // what used to be b's right child. Can I say it more clearly?
            b = a.getLeft();
            a.setLeft(b.getRight());
            (b.getRight()).setParent(a);
            b.setRight(a);
            (b.getLeft()).setNeighbours(a);

            // Remember b's old neighbour and set b's new neighbour to the unbalanced
            // node's old neighbour. Unbalanced new neighbour will be b's left child.
            oldNeighbour = b.getNeighbour();
            b.setNeighbours(a.getNeighbour());
            a.setNeighbours(b.getLeft());

            // Temp's old neighbour is now a's right child. Thus it's neighbour
            // will be unbalanced's left child
            oldNeighbour.setNeighbours(a.getLeft());
        }

        // Unbalanced to the right, switch pointers around
        else
        {
            // Unbalanced node's right child (b) moves up, and b's left child
            // becomes the unbalanced node. The unbalanced node's right child becomes
            // what used to be b's left child. Can I say it more clearly?
            b = a.getRight();
            a.setRight(b.getLeft());
            (b.getLeft()).setParent(a);
            b.setLeft(a);
            (b.getRight()).setNeighbours(a);

            // Remember b's old neighbour and set b's new neighbour to the unbalanced
            // node's old neighbour. Unbalanced new neighbour will be b's left child.
            oldNeighbour = b.getNeighbour();
            b.setNeighbours(a.getNeighbour());
            a.setNeighbours(b.getRight());

            // Temp's old neighbour is now a's right child. Thus it's neighbour
            // will be unbalanced's left child
            oldNeighbour.setNeighbours(a.getRight());
        }

        // Temp is now one level higher in the tree, and the unbalanced node has
        // moved down one step. Update the levels.
        int oldLevel = b.getLevel();
        b.setLevel(a.getLevel());
        a.setLevel(oldLevel);

        // a wasn't root. Update its parent's child-pointer
        if (a.getParent() != null)
        {
            if (a == (a.getParent()).getLeft())
                (a.getParent()).setLeft(b);
            else
                (a.getParent()).setRight(b);
        }

        // a was root. Update rootPointer and root's position
        else
        {
            rootNode = b;
            rootNode.setDestination(rootPos);
        }

        // Update parent-pointers
        b.setParent(a.getParent());
        a.setParent(b);
    }




    /** Called when the user clicks the solve button in the menu.
    * Much of the work is done by calling solve functions
    * the superclass TreeTemplate.
    *
    * @see AVLTree#rotateClick
    * @see AVLTree#updateAVLBalance
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
                rotateClick(unbalancedNode.getCenter());
                
            top.readyForInsertionInput(true);
        }

        else if (deleteOperation())
        {
            if (busyDeleting)
            {
                super.solveDeleteOperation();
                super.fixTreeAfterDeletion();
                super.clearTempVariables();

                // Update all node's AVLBalance-values from the first node necessary and up.
                if (unbalancedNode != null)
                    updateAVLBalance(unbalancedNode);

                if (busyRotating)
                    bottom.writeInfo(InfoMessage.notBalanced());
                else
                    unbalancedNode = null;

                center.repaint();

                return;
            }

            if (busyRotating)
            {
                rotateClick(unbalancedNode.getCenter());
                return;
            }

            else
            {
                bottom.writeInfo(InfoMessage.anyNodeToDelete());
                bottom.writeError(ErrorMessage.cantSolveNow());
            }

            center.repaint();
        }
        else if (searchOperation())
        {
            if (searchValue.equals(""))
            {
                bottom.writeInfo(InfoMessage.noNumberToSearch());
                bottom.writeError(ErrorMessage.cantSolveNow());
            }
            else
                super.solveSearchOperation();
                
            top.readyForSearchInput(true);
        }
        else if (inorderOperation())
            super.solveInorderIteration();
        else if (preorderOperation())
            super.solvePreorderIteration();
        else if (postorderOperation())
            super.solvePostorderIteration();
    }




    /** Sets all the node's AVL balance.
    * Recursive function that updates the variable AVLBalance for all the nodes
    * by calling Node's setAVLBalance. The AVL balance is the difference in
    * height between the node's right and left subtree.
    *
    * @param n the node to check
    * @see Node#setAVLBalance
    */
    public void updateAVLBalance(Node n)
    {
        // Node exists, update its balance and call the function for it's parent
        if (n != null)
        {
            int leftHeight = (n.getLeft()).getHeight();
            int rightHeight = (n.getRight()).getHeight();
            int diff = rightHeight - leftHeight;
            if ((diff < -1) || (diff > 1))
            {
                if (!busyRotating)
                {
                    busyRotating   = true;
                    unbalancedNode = n;
                }
            }

            n.setAVLBalance(diff);
            updateAVLBalance(n.getParent());
        }
    }




    /** Shows a window on the screen with the algorithm for the current operation.
    *
    * @see TreeTemplate#showWindow
    */
    public void view()
    {
        // Insert
        if ((mainClass.getChosenAlgorithm()).equals(BinaryTreesome.algorithm[0]))
            super.showWindow("The insert algorithm for an AVL tre!","insert_AVL.algo");

        // Delete
        else if ((mainClass.getChosenAlgorithm()).equals(BinaryTreesome.algorithm[1]))
            super.showWindow("Delete algorithm for an AVL tree!","delete_AVL.algo");

        // Search
        else if ((mainClass.getChosenAlgorithm()).equals(BinaryTreesome.algorithm[2]))
            super.showWindow("The search algorithm!","search.algo");

        // Inorder
        else if ((mainClass.getChosenAlgorithm()).equals(BinaryTreesome.algorithm[3]))
            super.showWindow("The inorder-iteration algorithm!","inorder.algo");

        // Preorder
        else if ((mainClass.getChosenAlgorithm()).equals(BinaryTreesome.algorithm[4]))
            super.showWindow("The preorder-iteration algorithm!","preorder.algo");

        // Postorder
        else if ((mainClass.getChosenAlgorithm()).equals(BinaryTreesome.algorithm[5]))
            super.showWindow("The postorder-iteration algorithm!","postorder.algo");

        if (insertOperation())
            top.readyForInsertionInput(true);
        else if (searchOperation())
            top.readyForSearchInput(true);
    }
}




// -------------------------------- End of class ------------------------------
