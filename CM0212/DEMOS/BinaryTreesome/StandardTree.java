// ---------------------------------- Imports ---------------------------------




import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;




// ------------------------------- Start of class -----------------------------




/**
* A class representing a standard binary tree.
* Holds the functions that are needed to be specifically coded
* for the standard tree.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
* @see            TreeTemplate
*/
public class StandardTree extends TreeTemplate
{
    // ---------------------------- Constructor -------------------------------

    
    
    
    /** Calls the superclass constructor with the same parameters.
    *
    * @param b the panel where messages are output
    * @param t the top panel, used for input and output
    * @param c the panel where the tree is to be painted
    * @param b the main applet class
    * @see TreeTemplate#TreeTemplate
    * @see BottomPanel
    * @see TopPanel
    * @see CenterPanel
    * @see BinaryTreesome
    */
    public StandardTree(BottomPanel b, TopPanel t, CenterPanel c, BinaryTreesome a){super(b,t,c,a);}




    // ----------------------- Mouse-related functions ------------------------




    /** Decides what is going to happen when a mouse button is clicked
    * in a standard tree.
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
    }




    /** Decides what is going to happen when a mouse button is released
    * in a standard Tree.
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
                    super.deleteReleased();

                else
                {
                    replacementNode.setType(Node.NORMAL);
                    selectedNode = null;
                    center.repaint();
                }
            }
        }
    }
    
    


    // -------------------------- Other functions -----------------------------




    /** Outputs error and info messages saying that the delete
    * operation isn't possible in a standard tree.
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

        bottom.writeInfo(InfoMessage.howToDeleteStandard());
        bottom.writeProgress("");
        bottom.writeError(ErrorMessage.operationNotPossible());

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

            if ((!toBeInserted.isEmpty()) || (looseNode != null))
                bottom.writeInfo(InfoMessage.insertStandardHint());
            else
                bottom.writeInfo(InfoMessage.inputInsertValues());
        }

        else if (inorderOperation())
            bottom.writeInfo(InfoMessage.inorderHint());
        else if (preorderOperation())
            bottom.writeInfo(InfoMessage.preorderHint());
        else if (postorderOperation())
            bottom.writeInfo(InfoMessage.postorderHint());
        else
            bottom.writeInfo(InfoMessage.noHintAvailable());
    }




    /** Called when a node is dropped during insertion.
    * Tries to insert the selected node into the tree, i.e. take
    * the overlapped node's position.
    *
    * @param overlapped the node which is overlapped by the selected node
    */
    public void insertSelectedNode(Node overlapped)
    {
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




    /** Outputs error and info messages saying that the search
    * operation isn't possible in a standard tree.
    *
    * @see ErrorMessage#operationNotPossible
    * @see BottomPanel#writeError
    * @see BottomPanel#writeInfo
    */
    public void search()
    {
        top.writeOperation("");

        if (rootNode.getType().equals(Node.SHADOW))
            operation = null;
        else
            operation = BinaryTreesome.menuItem[2];

        bottom.writeInfo(InfoMessage.howToSearchStandard());
        bottom.writeProgress("");
        bottom.writeError(ErrorMessage.operationNotPossible());

        center.repaint();
    }




    /** Called when the user clicks the solve button in the menu.
    * Much of the work is done by calling solve functions
    * the superclass TreeTemplate.
    *
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
            top.readyForInsertionInput(true);
            bottom.writeInfo(InfoMessage.manyWaysToSolve());
            bottom.writeError(ErrorMessage.cantSolveNow());
        }
        else if (deleteOperation())
        {
            bottom.writeInfo(InfoMessage.howToDeleteStandard() + " " + InfoMessage.nothingToSolve());
            bottom.writeError(ErrorMessage.cantSolveNow());
        }
        else if (searchOperation())
        {
            bottom.writeInfo(InfoMessage.howToSearchStandard() + " " + InfoMessage.nothingToSolve());
            bottom.writeError(ErrorMessage.cantSolveNow());
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
            super.showWindow("The insert algorithm for a standard tree!","insert.algo");

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
