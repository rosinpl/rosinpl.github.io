// ---------------------------------- Imports ---------------------------------




import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;




// ------------------------------- Start of class -----------------------------




/**
* A class representing a sorted binary tree.
* Holds the functions that are needed to be specifically coded
* for the sorted tree.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
* @see            TreeTemplate
*/
public class SortedTree extends TreeTemplate
{
    // ---------------------------- Constructor -------------------------------




    /** Calls the superclass constructor with the same parameters.
    *
    * @param b the panel where messages are output
    * @param p the top panel, used for input and output
    * @param c the panel where the tree is to be painted
    * @param b the main applet class
    * @see TreeTemplate#TreeTemplate
    * @see BottomPanel
    * @see TopPanel
    * @see CenterPanel
    * @see BinaryTreesome
    */
    public SortedTree(BottomPanel b, TopPanel p, CenterPanel c, BinaryTreesome a){super(b,p,c,a);}




    // ----------------------- Mouse-related functions ------------------------




    /** Decides what is going to happen when a mouse button is clicked
    * in a sorted tree.
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

        // If in iteration-mode, call appropriate function to deal with the click
        if ((inorderOperation()) || (preorderOperation()) || (postorderOperation()))
        {
             super.checkOrderClick(mp);
        }

        // If in deletion-mode, call appropriate function to deal with the click
        else if (deleteOperation())
        {
             initiateDeleteClick(mp);
        }

        // If in search-mode, call appropriate function to deal with the click
        else if (searchOperation())
        {
             super.searchClick(mp);
        }
    }




    /** Decides what is going to happen when a mouse button is released
    * in a sorted Tree.
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

            if (insertOperation())
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

            if ((!toBeInserted.isEmpty()) || (looseNode != null))
                bottom.writeInfo(InfoMessage.insertSortedHint());
            else
                bottom.writeInfo(InfoMessage.inputInsertValues());
        }

        else if (deleteOperation())
        {
            if (!busyDeleting)
                bottom.writeInfo(InfoMessage.deleteHint());
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
    * called to clean up.
    *
    * @param mp the point where the mouse was clicked
    * @see TreeTemplate#deleteClick
    * @see TreeTemplate#fixTreeAfterDeletion
    * @see TreeTemplate#clearTempVariables
    */
    public void initiateDeleteClick(Point mp)
    {
        super.deleteClick(mp);
        if (!busyDeleting)
        {
            super.fixTreeAfterDeletion();
            super.clearTempVariables();
            unbalancedNode = null;
        }
    }




    /** Tries to finish the delete procedure.
    * Calls the superclass TreeTemplate's deleteReleased function, and then
    * checks if the deletion is finished, i.e. if busyDeleting is false.
    * If it is, the superclass TreeTemplate's fixTreeAfterDeletion is
    * called to clean up.
    *
    * @see TreeTemplate#deleteReleased
    * @see TreeTemplate#fixTreeAfterDeletion
    * @see TreeTemplate#clearTempVariables
    */
    public void initiateDeleteReleased()
    {
        super.deleteReleased();
        if (!busyDeleting)
        {
            super.fixTreeAfterDeletion();
            super.clearTempVariables();
            unbalancedNode = null;
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

        // The selected node is now inserted into the tree. Fix the tree
        super.fixTreeAfterInsertion();

        // Generate and activate a new node that can be dragged into place
        super.generateLooseNode();
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
            if (looseNode != null)
                super.solveInsertOperation();
            else
            {
                bottom.writeInfo(InfoMessage.noNumberToInsert());
                bottom.writeError(ErrorMessage.cantSolveNow());
            }

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
                bottom.writeInfo(InfoMessage.anyNodeToDelete());
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




    /** Shows a window on the screen with the algorithm for the current operation.
    *
    * @see TreeTemplate#showWindow
    */
    public void view()
    {
        // Insert
        if ((mainClass.getChosenAlgorithm()).equals(BinaryTreesome.algorithm[0]))
            super.showWindow("The insert algorithm for a sorted tree!","insert_sorted.algo");

        // Delete
        else if ((mainClass.getChosenAlgorithm()).equals(BinaryTreesome.algorithm[1]))
            super.showWindow("Delete algorithm for a sorted tree!","delete_sorted.algo");

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
