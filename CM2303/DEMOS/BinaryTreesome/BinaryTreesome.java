// -------------------------------- Imports -----------------------------------




import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;




// ------------------------------- Start of class -----------------------------




/**
* Our main applet class.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
* @see            BinaryTreesome#initOperation
*/
public class BinaryTreesome extends Applet
implements ActionListener, ItemListener
{
    // ----- Constants ----

    // Size-related constants

    /** The width of the applet.
    */
	// changed from 720 - PLR
    public static final int APPLET_WIDTH   = 820;

    /** The height of the applet.
    */
	// changed from 490 - PLR
    public static final int APPLET_HIGHT   = 590;

    /**  The width of the border around the centerpanel.
    */
    public static final int BORDERSIZE     = 5;

    // String constants (used in buttons)


    /**  Defines the different types of trees.
    */
    public static final String treeType[]  = new String[]{"None","Standard","Sorted","AVL","AA","Splay","Red Black"};

    /**  Defines the different types of operations.
    */
    public static final String menuItem[]  = new String[]{"Insert","Delete","Search","Iterate","View","Hint","Solve"};

    /**  Defines the different types of algorithms.
    */
    public static final String algorithm[] = new String[]{"Insert","Delete","Search","Inorder","Preorder","Postorder"};

    /**  Defines the different types of iterations.
    */
    public static final String iteration[] = new String[]{"Inorder","Preorder","Postorder"};

    /**  Defines the different types of input.
    */
    public static final String inputType[] = new String[]{"Add","Change","Random"};

    // ----- Variables ----

    // Elements of the applet


    /** One of the panels that make up the applet.
    */
    private TopPanel top                = new TopPanel();

    /** One of the panels that make up the applet.
    */
    private BottomPanel bottom          = new BottomPanel();

    /** One of the panels that make up the applet.
    */
    private CenterPanel center          = new CenterPanel(bottom, top, this);

    /** One of the panels that make up the applet.
    */
    private MenuPanel menu              = new MenuPanel(this);

    // The selected items from the pulldown-menu in the menu


    /** Holds the current selection in the menu's tree pulldown menu.
    */
    private String chosenTree           = new String(treeType[0]);

    /** Holds the current selection in the menu's algorithm pulldown menu.
    */
    private String chosenAlgorithm      = new String(algorithm[0]);

    /** Holds the current selection in the menu's iteration pulldown menu.
    */
    private String chosenIteration      = new String(iteration[0]);




    // -------------------- Automatically called functions --------------------




    /** Initializes the applet's size and layout.
    */
    public void init()
    {
        // Decide size of applet and set layout
        setSize(APPLET_WIDTH, APPLET_HIGHT);
    	setLayout(new BorderLayout());

        // Set some colors
    	center.setBackground(Color.lightGray);
    	menu.setBackground(Color.lightGray);
    	top.setBackground(Color.lightGray);
    	bottom.setBackground(Color.lightGray);

        // Add panels to the applet
       	add("Center", center);
       	add("East", menu);
       	add("North", top);
       	add("South", bottom);

        // Set the images that the tree will use to paint the nodes
        // (root,normal,shadows,selected,loose)
        center.setImages(getCodeBase(),new String[]{"RedBall.gif","YellowBall.gif","Hole.gif","BlueBall.gif","GrayBall.gif","TracerBall.gif"});
        System.out.println("appletContext: " + getAppletContext());
        System.out.println("codeBase     : " + getCodeBase());
    }




    /** Takes care of painting the applet when needed.
    * Calls the center panel's repaint function.
    *
    * @param g The graphich context of the applet
    * @see CenterPanel#paint
    */
    public void paint(Graphics g){center.repaint();}




    // ----------------------- Item-related functions -------------------------




    /** Decides what should be done when an item has been
    * selected from one of the pulldown-menus in the menu.
    *
    * @param e the item event
    */
    public void itemStateChanged(ItemEvent e)
    {
        // Get the selected item
        String c = new String((String)e.getItem());

        // Check whether a new tree or a new algorithm was chosen from the menu.
        for (int i=0; i<treeType.length; i++)
        {
            // A tree was chosen, different from what allready was selected
            if ((c.equals(treeType[i])) && (!c.equals(chosenTree)))
            {
                // Change of tree not allowed during solving an iteration or search
                if ((center.getTree() != null) &&
                   ((((center.getTree()).getTracer() != null) && (((center.getTree()).getTracer()).isAlive())) ||
                   (((center.getTree()).getMover() != null) && (((center.getTree()).getMover()).isAlive()))))
                {
                    menu.setTree(chosenTree);
                    bottom.writeError(ErrorMessage.solveAndItemChanged());
                    return;
                }

                chosenTree = c;                 // Remember what tree was chosen
                center.setOperation(null);      // New tree chosen, "no operation" as default

                // Clear some output-areas
                top.writeOperation("");
                bottom.writeProgress("");
                bottom.writeError("");

                // Output info depending on what was chosen
                if  (i == 0)
                    bottom.writeInfo(InfoMessage.selectTree());

                else if ((i == 5) || (i == 6))
                    bottom.writeInfo(InfoMessage.treeNotAvailable());

                else
                    bottom.writeInfo(InfoMessage.selectMenuItem());

                // Let center-panel know what tree was chosen
                center.setTree(treeType[i]);

                return;
            }
        }

        // The item selected from the menu wasn't a new tree, check the algorithm-items
        for (int i=0; i<algorithm.length; i++)
        {
            // An algorithm was chosen, different from what allready was selected
            if ((c.equals(algorithm[i])) && (!c.equals(chosenAlgorithm)))
            {
                chosenAlgorithm = c;        // Remember the algorithm chosen
            }
        }

        // The item selected from the menu wasn't a new tree nor an algorithm.
        // Check the iteration items
        for (int i=0; i<iteration.length; i++)
        {
            // An iteration was chosen, different from what allready was selected
            if ((c.equals(iteration[i])) && (!c.equals(chosenIteration)))
            {
                chosenIteration = c;        // Remember the iteration chosen
            }
        }
    }




    // ---------------------- Action-related functions ------------------------




    /** Called when a button is pressed in the menu.
    * Calls the appropriate functions for current tree.
    *
    * @param e the action event
    */
    public void actionPerformed(ActionEvent e)
    {
        // Get the action-command
        String chosen = new String(e.getActionCommand());

        // No tree has been selected yet, give error message and return
        if (chosenTree.equals(treeType[0]))
        {
            bottom.writeProgress(" ");
            bottom.writeError(ErrorMessage.noTreeSelected());
            bottom.writeInfo(InfoMessage.selectTree());
            return;
        }

        // Buttonclick not allowed before deletion has been finished
        // (except solve and hint and view)
        if ((center.getTree()).isBusyDeleting())
        {
            if (!(chosen.equals(menuItem[1])))
                bottom.writeError(ErrorMessage.busyDeleting());
            if ((!(chosen.equals(menuItem[4]))) && (!(chosen.equals(menuItem[5]))) && (!(chosen.equals(menuItem[6]))))
                return;
        }

        // Buttonclick not allowed before rotation has been finished
        // (except solve and hint and view)
        if ((center.getTree()).isBusyRotating())
        {
            if ((((center.getTree()).insertOperation()) && (!(chosen.equals(menuItem[0])))) || 
                (((center.getTree()).deleteOperation()) && (!(chosen.equals(menuItem[1])))))
                bottom.writeError(ErrorMessage.busyRotating());
            if ((!(chosen.equals(menuItem[4]))) && (!(chosen.equals(menuItem[5]))) && (!(chosen.equals(menuItem[6]))))
                return;
        }

        // Buttonclick not allowed during solving an iteration or search
        else if (((center.getTree()).getTracer() != null) && (((center.getTree()).getTracer()).isAlive()))
        {
            if (!chosen.equals(menuItem[4]))
            {
               bottom.writeError(ErrorMessage.solveAndButtonClicked());
               return;
            }
            // View-click accepted. Continue
        }

        // Buttonclick not allowed during solving an insertion
        else if (((center.getTree()).getMover() != null) && (((center.getTree()).getMover()).isAlive()))
        {
            if (!chosen.equals(menuItem[4]))
            {
               bottom.writeError(ErrorMessage.solveAndButtonClicked());
               return;
            }
            // View-click accepted. Continue
        }

        // ----- Button-click accepted and is OK. Continue. -----

        // Clear earlier error-messages
        bottom.writeError("");

        // Call the trees appropriate function depending on what button was pressed.
        // Insert
        if (chosen.equals(menuItem[0]))
        {
            initOperation(0);

            // Cast to right tree-type, and call their appropriate function
            if (chosenTree.equals(treeType[1]))
                ((StandardTree)(center.getTree())).insert();
            else if (chosenTree.equals(treeType[2]))
                ((SortedTree)(center.getTree())).insert();
            else if (chosenTree.equals(treeType[3]))
                ((AVLTree)(center.getTree())).insert();
            else if (chosenTree.equals(treeType[4]))
                ((AATree)(center.getTree())).insert();
            else if (chosenTree.equals(treeType[5]))
                ((SplayTree)(center.getTree())).insert();
            else if (chosenTree.equals(treeType[6]))
                ((RedBlackTree)(center.getTree())).insert();
        }

        // Delete
        else if (chosen.equals(menuItem[1]))
        {
            initOperation(1);

            // Cast to right tree-type, and call their appropriate function
            if (chosenTree.equals(treeType[1]))
                ((StandardTree)(center.getTree())).delete();
            else if (chosenTree.equals(treeType[2]))
                ((SortedTree)(center.getTree())).delete();
            else if (chosenTree.equals(treeType[3]))
                ((AVLTree)(center.getTree())).delete();
            else if (chosenTree.equals(treeType[4]))
                ((AATree)(center.getTree())).delete();
            else if (chosenTree.equals(treeType[5]))
                ((SplayTree)(center.getTree())).delete();
            else if (chosenTree.equals(treeType[6]))
                ((RedBlackTree)(center.getTree())).delete();
        }

        // Search
        else if (chosen.equals(menuItem[2]))
        {
            initOperation(2);
            
            // Cast to right tree-type, and call their appropriate function
            if (chosenTree.equals(treeType[1]))
                ((StandardTree)(center.getTree())).search();
            else if (chosenTree.equals(treeType[2]))
                ((SortedTree)(center.getTree())).search();
            else if (chosenTree.equals(treeType[3]))
                ((AVLTree)(center.getTree())).search();
            else if (chosenTree.equals(treeType[4]))
                ((AATree)(center.getTree())).search();
            else if (chosenTree.equals(treeType[5]))
                ((SplayTree)(center.getTree())).search();
            else if (chosenTree.equals(treeType[6]))
                ((RedBlackTree)(center.getTree())).search();
        }

        // Iteration
        else if (chosen.equals(menuItem[3]))
        {
            // Inorder, Preorder, Postorder
            if (chosenIteration.equals(iteration[0]))
                initOperation(3);
            else if (chosenIteration.equals(iteration[1]))
                initOperation(4);
            else if (chosenIteration.equals(iteration[2]))
                initOperation(5);

            (center.getTree()).order();
       }


        // View algorithm
        else if (chosen.equals(menuItem[4]))
        {
            // Cast to right tree-type, and call their appropriate function
            if (chosenTree.equals(treeType[1]))
                ((StandardTree)(center.getTree())).view();
            else if (chosenTree.equals(treeType[2]))
                ((SortedTree)(center.getTree())).view();
            else if (chosenTree.equals(treeType[3]))
                ((AVLTree)(center.getTree())).view();
            else if (chosenTree.equals(treeType[4]))
                ((AATree)(center.getTree())).view();
            else if (chosenTree.equals(treeType[5]))
                ((SplayTree)(center.getTree())).view();
            else if (chosenTree.equals(treeType[6]))
                ((RedBlackTree)(center.getTree())).view();
        }

        // Hint
        else if (chosen.equals(menuItem[5]))
        {
            // Cast to right tree-type, and call their appropriate function
             if (chosenTree.equals(treeType[1]))
                 ((StandardTree)(center.getTree())).hint();
             else if (chosenTree.equals(treeType[2]))
                 ((SortedTree)(center.getTree())).hint();
             else if (chosenTree.equals(treeType[3]))
                 ((AVLTree)(center.getTree())).hint();
            else if (chosenTree.equals(treeType[4]))
                ((AATree)(center.getTree())).hint();
            else if (chosenTree.equals(treeType[5]))
                ((SplayTree)(center.getTree())).hint();
            else if (chosenTree.equals(treeType[6]))
                ((RedBlackTree)(center.getTree())).hint();
        }

        // Solve
        else if (chosen.equals(menuItem[6]))
        {
            // Cast to right tree-type, and call their appropriate function
            if (chosenTree.equals(treeType[1]))
                ((StandardTree)(center.getTree())).solve();
            else if (chosenTree.equals(treeType[2]))
                ((SortedTree)(center.getTree())).solve();
            else if (chosenTree.equals(treeType[3]))
                ((AVLTree)(center.getTree())).solve();
            else if (chosenTree.equals(treeType[4]))
                ((AATree)(center.getTree())).solve();
            else if (chosenTree.equals(treeType[5]))
                ((SplayTree)(center.getTree())).solve();
            else if (chosenTree.equals(treeType[6]))
                ((RedBlackTree)(center.getTree())).solve();
        }
    }




    // --------------------------- Other functions ----------------------------




    /** Returns the algorithm currently chosen in the menu.
    * The different algorithm types are specified in the public
    * static array <A HREF="#algorithm">algorithm</a>
    *
    * @return the algorithm (string) that is currently chosen in the menu.
    * @see BinaryTreesome#algorithm
    * @see BinaryTreesome#chosenAlgorithm
    */
    public String getChosenAlgorithm(){return chosenAlgorithm;}




    /** Returns the iteration currently chosen in the menu.
    * The different iteration types are specified in the public
    * static array <A HREF="#iteration">iteration</a>
    *
    * @return the iteration (string) that is currently chosen in the menu.
    * @see BinaryTreesome#iteration
    * @see BinaryTreesome#chosenIteration
    */
    public String getChosenIteration(){return chosenIteration;}




    /** Returns the tree currently chosen in the menu.
    * The different tree types are specified in the public
    * static array <A HREF="#menuItem">menuItem</a>
    *
    * @return the tree (string) that is currently chosen in the menu.
    * @see BinaryTreesome#menuItem
    * @see BinaryTreesome#chosenTree
    */
    public String getChosenTree(){return chosenTree;}




    /** Called when a new operation has been chosen.
    * Update some things in the menu, and decide whether
    * or not to show the inputbox.
    *
    * @param i the index of the the algorithm chosen in
    * the array <A HREF="#algorithm">algorithm</a>.
    * @see BinaryTreesome#algorithm
    */
    public void initOperation(int i)
    {
        (center.getTree()).clearLooseNodes();
        (center.getTree()).restoreNodes();
        chosenAlgorithm = algorithm[i];
        menu.setAlgorithm(algorithm[i]);
        top.readyForSearchInput(false);
    }
}




// -------------------------------- End of class ------------------------------

