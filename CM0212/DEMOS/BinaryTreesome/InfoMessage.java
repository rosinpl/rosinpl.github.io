// ------------------------------- Start of class -----------------------------




/**
* A class containing functions that return standard information messages.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class InfoMessage
{
    // Window messages


    /** Returns an info message saying that a file is sent to the printer.
    * @return "Graphics is being loaded. Please wait..."
    */
    public static final String fileToPrinter(){return "Window message: File is sent to printer.";}


    // Standard info-messages


    /** Returns an info message telling the user to wait while graphics is being loaded.
    * @return "The operation is not implemented yet."
    */
    public static final String operationNotImplemented(){return "The operation is not implemented yet.";}

    /** Returns an info message telling the user to wait while graphics is being loaded.
    * @return "Graphics is being loaded. Please wait..."
    */
    public static final String pleaseWait(){return "Graphics is being loaded. Please wait...";}

    /** Returns an info message telling the user that the chosen tree is unavailable.
    * @return "The tree you selected is not available. Please select another tree."
    */
    public static final String treeNotAvailable(){return "The tree you selected is not available. Please select another tree.";}

    /** Returns an info message telling the user to click insert in the menu to insert more numbers.
    * @return "Click \"Insert\" from the menu to insert more nodes."
    */
    public static final String clickInsert(){return "Click \"Insert\" from the menu to insert more nodes.";}

    /** Returns an info message saying that the tree is empty.
    * @return "The tree is empty."
    */
    public static final String emptyTree(){return "The tree is empty.";}

    /** Returns an info message telling the user to select a tree from the menu.
    * @return "From the menu, select a tree to practice on."
    */
    public static final String selectTree(){return "From the menu, select a tree to practice on.";}

    /** Returns an info message tellling the user to select an operation from the menu.
    * @return "From the menu, select an operation to practice on."
    */
    public static final String selectMenuItem(){return "From the menu, select an operation to practice on.";}

    /** Returns an info message telling the user that there is no operation to solve.
    * @return "No operation is in progress. Nothing to solve."
    */
    public static final String noOperation(){return "No operation is in progress. Nothing to solve.";}

    /** Returns an info message telling the user that all the nodes are deleted.
    * @return "All the nodes in the tree are deleted."
    */
    public static final String allNodesDeleted(){return "All the nodes in the tree are deleted.";}

    /** Returns an info message telling the user that there is no available numbers to insert.
    * @return "There are no available numbers to be inserted into the tree."
    */
    public static final String allNodesInserted(){return "There are no available numbers to be inserted into the tree.";}

    /** Returns an info message stating that the insert opearation is in progress.
    * @return "Operation in progress: Insertion of nodes."
    */
    public static final String insertOperation(){return "Operation in progress: Insertion of nodes.";}

    /** Returns an info message stating that the search opearation is in progress.
    * @return "Operation in progress: Searching for a node."
    */
    public static final String searchOperation(){return "Operation in progress: Searching for a node.";}

    /** Returns an info message stating that the delete opearation is in progress.
    * @return "Operation in progress: Deletion of nodes."
    */
    public static final String deleteOperation(){return "Operation in progress: Deletion of nodes.";}

    /** Returns an info message stating that inorder iteration is in progress.
    * @return "Operation in progress: Inorder iteration."
    */
    public static final String inorderOperation(){return "Operation in progress: Inorder iteration.";}

    /** Returns an info message stating that preorder iteration is in progress.
    * @return "Operation in progress: Preorder iteration."
    */
    public static final String preorderOperation(){return "Operation in progress: Preorder iteration.";}

    /** Returns an info message stating that postorder iteration is in progress.
    * @return "Operation in progress: Postorder iteration."
    */
    public static final String postorderOperation(){return "Operation in progress: Postorder iteration.";}

    /** Returns an info message telling the user how to insert nodes.
    * @return "Drag the gray node and drop it over one of the empty (white) holes to build the tree."
    */
    public static final String howToInsert(){return "Drag the gray node and drop it over one of the empty (white) holes to build the tree.";}

    /** Returns an info message telling the user to enter a number to search for.
    * @return "Enter the number you want to search for in the textbox above the tree."
    */
    public static final String inputSearchValue(){return "Enter the number you want to search for in the textbox above the tree.";}

    /** Returns an info message telling the user to enter the numbers he/she wants to insert.
    * @return "Enter the numbers you want to insert in the textbox above the tree."
    */
    public static final String inputInsertValues(){return "Enter the numbers you want to insert in the textbox above the tree.";}

    /** Returns an info message telling the user how to search.
    * @return "Search for the number by clicking your way through the tree in the correct order."
    */
    public static final String howToSearch(){return "Search for the number by clicking your way through the tree in the correct order.";}

    /** Returns an info message telling the user that searching in a standard tree doesn't make sense.
    * @return "Searching a standard tree makes no sense!!"
    */
    public static final String howToSearchStandard(){return "Searching a standard tree makes no sense!!";}

    /** Returns an info message telling the user how to delete a node.
    * @return "Click on the node you want to delete."
    */
    public static final String howToDelete(){return "Click on the node you want to delete.";}

    /** Returns an info message telling the user that deleting in a standard tree doesn't make sense.
    * @return "Deletion in a standard tree makes no sense!!"
    */
    public static final String howToDeleteStandard(){return "Deletion in a standard tree makes no sense!!";}

    /** Returns an info message telling the user how to replace a deleted node.
    * @return "Tree must be restructured to stay sorted. Drag the correct node and drop it over the hole."
    */
    public static final String howToReplace(){return "Tree must be restructured to stay sorted. Drag the correct node and drop it over the hole.";}

    /** Returns an info message telling the user how to iterate.
    * @return "Iterate through the tree by clicking on the nodes in the right order."
    */
    public static final String howToIterate(){return "Iterate through the tree by clicking on the nodes in the right order.";}

    /** Returns an info message that congratulates the user after finishing an iteration.
    * @return "Congratulations!! You have finished iterating through the tree."
    */
    public static final String finishedIteration(){return "Congratulations!! You have finished iterating through the tree.";}

    /** Returns an info message that congratulates the user after finishing a search.
    * @return "Congratulations!! You have found the number you searched for."
    */
    public static final String finishedSearch(){return "Congratulations!! You have found the number you searched for.";}

    /** Returns an info message stating what to search for.
    * @return "Number to search for: "
    */
    public static final String whatToSearch(){return "Number to search for: ";}

    /** Returns an info message telling the user that the number search for isn't in the tree.
    * @return "Sorry :(  The number you searched for isn't in the tree."
    */
    public static final String searchValueNotFound(){return "Sorry :(  The number you searched for isn't in the tree.";}

    /** Returns an info message stating who made this program.
    * @return "Programmed by Bjørn E. Gustafson & Jørgen Kjensli - Oslo College"
    */
    public static final String whoAreWe(){return "Programmed by Bjorn E. Gustafson & Jorgen Kjensli - Oslo College ";}

    /** Returns the name of the applet.
    * @return "Binary Treesome!"
    */
    public static final String appletName(){return "Binary Treesome!";}

    /** Returns an info message telling the user to enter numbers for insertion.
    * @return "Enter the numbers you want to insert (1-99):"
    */
    public static final String enterInsertionNumbers(){return "Enter the numbers you want to insert (1-99):";}

    /** Returns an info message telling the user to enter a number to search for.
    * @return "Enter the number you want to search for (1-99):"
    */
    public static final String enterSearchNumber(){return "Enter the number you want to search for (1-99):";}

    /** Returns an info message telling the user that the tree isn't balanced.
    * @return "The tree is not balanced. Click on the node that is unbalanced."
    */
    public static final String notBalanced(){return "The tree is not balanced. Click on the node that is unbalanced.";}

    /** Returns an info message telling the user that there is many soulutions.
    * @return "There are many ways to solve this, and one way is as good as another. Figure it out!"
    */
    public static final String manyWaysToSolve(){return "There are many ways to solve this, and one way is as good as another. Figure it out!";}

    /** Returns an info message telling the user that there is nothing to solve.
    * @return "There's nothing to solve."
    */
    public static final String nothingToSolve(){return "There's nothing to solve.";}

    /** Returns an info message telling the user that there is no node to insert.
    * @return "There's no node to insert. Enter a number before choosing \"Solve\"."
    */
    public static final String noNumberToInsert(){return "There's no node to insert. Enter a number before choosing \"Solve\".";}

    /** Returns an info message telling the user that there is no node to search for.
    * @return "There's no node to search for. Enter a number before choosing \"Solve\"."
    */
    public static final String noNumberToSearch(){return "There's no node to search for. Enter a number before choosing \"Solve\".";}

    /** Returns an info message telling the user that any node can be deleted.
    * @return "Any node can be deleted. Click on the one you want to delete before choosing \"Solve\"."
    */
    public static final String anyNodeToDelete(){return "Any node can be deleted. Click on the one you want to delete before choosing \"Solve\".";}

    // Hints

    /** Returns a hint to the user
    * @return "No hint available for what you are doing right now."
    */
    public static final String noHintAvailable(){return "No hint available for what you are doing right now.";}

    /** Returns a hint to the user
    * @return "The node can be inserted anywhere at the bottom of the tree."
    */
    public static final String insertStandardHint(){return "The node can be inserted anywhere at the bottom of the tree.";}

    /** Returns a hint to the user
    * @return "Starting from root, go left if node to be inserted has lower number, right if higher. Insert at bottom."
    */
    public static final String insertSortedHint(){return "Starting from root, go left if node to be inserted has lower number, right if higher. Insert at bottom.";}

    /** Returns a hint to the user
    * @return "Any node in the tree can be deleted."
    */
    public static final String deleteHint(){return "Any node in the tree can be deleted.";}

    /** Returns a hint to the user
    * @return "The node with the lowest number in the deleted node's right subtree takes it's place."
    */
    public static final String doubleDeletionHint(){return "The node with the lowest number in the deleted node's right subtree takes it's place.";}

    /** Returns a hint to the user
    * @return "The hole's only child should be moved up to take the hole's position."
    */
    public static final String singleDeletionHint(){return "The hole's only child should be moved up to take the hole's position.";}

    /** Returns a hint to the user
    * @return "Starting from root, go left if number is lower than the node's value, right if higher."
    */
    public static final String searchHint(){return "Starting from root, go left if number is lower than the node's value, right if higher.";}

    /** Returns a hint to the user
    * @return "First the node's left child, then the node, and finally the node's right child."
    */
    public static final String inorderHint(){return "First the node's left child, then the node, and finally the node's right child.";}

    /** Returns a hint to the user
    * @return "First the node, then the node's left child, and finally the node's right child."
    */
    public static final String preorderHint(){return "First the node, then the node's left child, and finally the node's right child.";}

    /** Returns a hint to the user
    * @return "First the node's left child, then the node's right child, and finally the node itself."
    */
    public static final String postorderHint(){return "First the node's left child, then the node's right child, and finally the node itself.";}

    /** Returns a hint to the user
    * @return "When the difference in height between a node's two subtrees is greater than 2, the node is unbalanced."
    */
    public static final String AVLrotationHint(){return "When the difference in height between a node's two subtrees is greater than 2, the node is unbalanced.";}

    /** Returns a hint to the user
    * @return "A node is unbalanced when it's left child has the same level, or the node's right child only has a right child."
    */
    public static final String AArotationHint(){return "A node is unbalanced when it's left child has the same level, or the node's right child only has a right child.";}
}




// -------------------------------- End of class ------------------------------
