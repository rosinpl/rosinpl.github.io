// ------------------------------- Start of class -----------------------------




/**
* A class containing functions that return standard error messages.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class ErrorMessage
{
    // Window messages

    /** Returns an error message for when a file couldn't be opened.
    *
    * @param f the name of the file
    * @return "Window message: Couldn't open file " + f);
    */
    public static final String fileNotFound(String f){return ("Window message: Couldn't open file " + f);}

    // Standard error-messages

    /** Returns an error message for when no tree is selected.
    * @return "You must select a tree before you can choose an operation!"
    */
    public static final String noTreeSelected(){return "You must select a tree before you can choose an operation!";}

    /** Returns an error message for when the operation chosen isn't possible.
    * @return "It is not possible to perform the operation you chose!"
    */
    public static final String operationNotPossible(){return "It is not possible to perform the operation you chose!";}

    /** Returns an error message for when a node is tried inserted in the wrong place.
    * @return "The node can't be inserted here!"
    */
    public static final String unableToInsert(){return "The node can't be inserted here!";}

    /** Returns an error message for when the user tries to drag a locked node.
    * @return "You can't drag this node!"
    */
    public static final String unableToDrag(){return "You can't drag this node!";}

    /** Returns an error message for when a node is dropped in the wrong place in a sorted tree.
    * @return "You can't drop the node here! It doesn't comply with the rules for a sorted tree!"
    */
    public static final String unableToDrop(){return "You can't drop the node here! It doesn't comply with the rules for a sorted tree!";}

    /** Returns an error message for when the user tries to change operation during the deletion of a node.
    * @return "You must finish the deletion of the node before you can change operation!"
    */
    public static final String busyDeleting(){return "You must finish the deletion of the node before you can change operation!";}

    /** Returns an error message for when the user tries to change operation during a rotation.
    * @return "You must finish the balancing of the tree before you can change operation!"
    */
    public static final String busyRotating(){return "You must finish the balancing of the tree before you can change operation!";}

    /** Returns an error message for when an unvalid number is entered.
    * @return "You didn't enter a valid number."
    */
    public static final String notValidNumber(){return "You didn't enter a valid number!";}

    /** Returns an error message for when the user clicks a button while something is beeing solved.
    * @return "Please wait for the applet to finish solving before choosing an operation!"
    */
    public static final String solveAndButtonClicked(){return "Please wait for the applet to finish solving before choosing an operation!";}

    /** Returns an error message for when the user chooses something in a pulldown menu while something is beeing solved.
    * @return "Please wait for the applet to finish solving before choosing another tree!"
    */
    public static final String solveAndItemChanged(){return "Please wait for the applet to finish solving before choosing another tree!";}

    /** Returns an error message for when the user tries to insert a node when the tree is unbalanced.
    * @return "You can't insert more nodes until the tree is balanced!!"
    */
    public static final String mustRestructure(){return "You can't insert more nodes until the tree is balanced!!";}

    /** Returns an error message for when something was done to the wrong node.
    * @return "Wrong node!"
    */
    public static final String wrongNode(){return "Wrong node!";}

    /** Returns an error message for when the user clicks solve and there is nothing to be solved.
    * @return "You can't choose solve now!!"
    */
    public static final String cantSolveNow(){return "You can't choose solve now!!";}
}




// -------------------------------- End of class ------------------------------

