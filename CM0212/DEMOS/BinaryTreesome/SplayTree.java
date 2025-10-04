// ---------------------------------- Imports ---------------------------------




import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;




// ------------------------------- Start of class -----------------------------




/**
* A class representing a splay tree.
* Not implemented.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
* @see            TreeTemplate
*/
public class SplayTree extends TreeTemplate
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
    public SplayTree(BottomPanel b, TopPanel p, CenterPanel c, BinaryTreesome a){super(b,p,c,a);}




    // ----------------------- Mouse-related functions ------------------------




    /** Not implemented.
    */
    public void mouseClicked(MouseEvent e){}
    /** Not implemented.
    */
    public void mouseReleased(MouseEvent e){}




    // ------------------------ Other functions -------------------------




    /** Called when user clicks on the delete-button from the menu.
    * Remove this function when implementing the tree, and super's
    * delete-function will be used.
    */
    public void delete()
    {
        bottom.writeInfo(InfoMessage.treeNotAvailable());
        bottom.writeError(ErrorMessage.operationNotPossible());
    }
    
    
    
    
    /** Called when user clicks on the hint-button from the menu.
    * Not implemented.
    */
    public void hint()
    {
        bottom.writeInfo(InfoMessage.treeNotAvailable());
        bottom.writeError(ErrorMessage.operationNotPossible());
    }




    /** Called when user clicks on the insert button from the menu.
    * Remove this function when implementing the tree, and super's
    * insert-function will be used.
    */
    public void insert()
    {
        bottom.writeInfo(InfoMessage.treeNotAvailable());
        bottom.writeError(ErrorMessage.operationNotPossible());
    }




    /** Tries to insert the selected node into the tree.
    * Not implemented.
    */
    public void insertSelectedNode(Node overlapped){}




    /** Called when user clicks on the iterate button from the menu.
    * Remove this function when implementing the tree, and super's
    * order-function will be used.
    */
    public void order()
    {
        bottom.writeInfo(InfoMessage.treeNotAvailable());
        bottom.writeError(ErrorMessage.operationNotPossible());
    }
    
    
    
    
    /** Called when user clicks on the search button from the menu.
    * Remove this function when implementing the tree, and super's
    * search-function will be used.
    */
    public void search()
    {
        bottom.writeInfo(InfoMessage.treeNotAvailable());
        bottom.writeError(ErrorMessage.operationNotPossible());
    }
    
    
    
    
    /** Called when user click on the solve button from the menu.
    * Not implemented.
    */
    public void solve()
    {
        bottom.writeInfo(InfoMessage.treeNotAvailable());
        bottom.writeError(ErrorMessage.operationNotPossible());
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
