// ---------------------------------- Imports ---------------------------------




import java.awt.*;
import java.net.URL;




// ------------------------------- Start of class -----------------------------




/**
* A class representing the main panel in the applet.
* This is the panel where the trees are shown.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class CenterPanel extends Panel
{
    // ----- Variables ----


    /** Flag is true while graphics are being loaded.
    */
    private boolean loadingGraphics     = false;


    /** The tree that is in use and that will be drawn in this panel.
    */
    private TreeTemplate tree           = null;

    /** The main applet-class.
    */
    private BinaryTreesome mainClass    = null;


    /** One of the panels where information is output.
    */
    private TopPanel top                = null;

    /** One of the panels where information is output.
    */
    private BottomPanel bottom          = null;


    /** Text to be output in the panel.
    */
    private String welcomeLabel         = "Welcome to Binary Treesome!";

    /** Text to be output in the panel.
    */
    private String treeLabel            = "";




    // ---------------------------- Constructor -------------------------------




    /** Constructs a panel with a close and print button.
    *
    * @param b the BottomPanel the variable bottom should point to.
    * @param t the TopPanel the variable top should point to.
    * @param t the main applet class which the variable mainClass will point to.
    * @see BottomPanel
    * @see TopPanel
    * @see BinaryTreesome
    */
    public CenterPanel(BottomPanel b, TopPanel t, BinaryTreesome a)
    {
        bottom    = b;
        top       = t;
        mainClass = a;
    }




    // -------------------- Automatically called functions --------------------




    /** Takes care of painting the graphics in this panel.
    * Prints the welcomeLabel if no tree is selected, and
    * the name of the tree selected if no operation is in
    * progress. Calls the trees update function if an operation
    * is in progress.
    *
    * @param g the Graphics context in which things will be painted
    * @see TreeTemplate#update
    * @see CenterPanel#paintBig
    */
    public void paint(Graphics g)
    {
        // No tree selected
        if (tree == null)
        {
            // Paints welcome-message to the user, output some info
            paintBig(g,welcomeLabel);
            top.readyForSearchInput(false);
            bottom.writeInfo(InfoMessage.selectTree());
        }

        // A tree is selected
        else
        {
            // if no operation is in progress, output the name of the tree
            if (!tree.operationInProgress())
                paintBig(g,treeLabel);
            else
                tree.update(g);
        }
    }




    /** Takes care of painting the graphics in this panel.
    * Prints the welcomeLabel if no tree is selected, and
    * the name of the tree selected if no operation is in
    * progress. Calls the trees update function if an operation
    * is in progress.
    *
    * @param g the Graphics context in which things will be painted
    * @see TreeTemplate#update
    * @see CenterPanel#paintBig
    */
    public void update(Graphics g)
    {
        // No tree selected
        if (tree == null)
        {
            // Paints welcome-message to the user, output some info
            paintBig(g,welcomeLabel);
            top.readyForSearchInput(false);
            bottom.writeInfo(InfoMessage.selectTree());
        }

        // A tree is selected
        else
        {
            // if no operation is in progress, output the name of the tree
            if (!tree.operationInProgress())
                paintBig(g,treeLabel);
            else
                tree.update(g);
        }
    }




    // -------------------------- Other functions -----------------------------




    /** Returns the tree that is currently being worked on.
    * The current tree is held in the private variable tree.
    *
    * @return the tree (TreeTemplate) that is currently being worked on.
    * @see CenterPanel#tree
    */
    public TreeTemplate getTree(){return tree;}




    /** Paints the string recieved as a big label in the center of the panel.
    *
    * @param g the Graphics context in which things will be painted.
    * @param texht the string that should be printed.
    */
    public void paintBig(Graphics g, String text)
    {
        Dimension d = getSize();

        // Draw a "panel" that stands out
        g.setColor(getBackground());
        g.fill3DRect(BinaryTreesome.BORDERSIZE, 0, d.width-BinaryTreesome.BORDERSIZE, d.height,true);

        Font f;

        // Graphics aren't being loaded. Draw big label as normal
        if (!loadingGraphics)
        {
            // Create new big font and insert it into the graphic unit
            f = new Font((g.getFont()).getName(),Font.BOLD,d.height/10);
            g.setFont(f);
        }

        // Graphics being loaded. Change text to be output
        else
        {
            f = g.getFont();
            text = InfoMessage.pleaseWait();
        }

        FontMetrics fm = g.getFontMetrics(f);

        // Calculate string width and height
        int sw = fm.stringWidth(text);
        int sh = (fm.getDescent()+fm.getAscent())/2;

        // Calculate start-coordinates of the text to be output
        int x = (d.width - sw)/2;
        int y = (d.height + sh)/2;

        // Draw the string onto the screen
        g.setColor(Color.black);
        g.drawString(text,x,y);
    }




    /** Loads the images to be used to paint the nodes in the tree.
    *
    * @param u the base URL where the images can be found.
    * @param name an array containing the names of the image files.
    */
    public void setImages(URL u, String name[])
    {

        Image image[] = new Image[name.length];

        MediaTracker loader = new MediaTracker(this);

        for (int i=0; i<name.length; i++)
        {
           image[i] = mainClass.getImage(u,name[i]);
           loader.addImage(image[i],0);
        }

        try
        {
            loadingGraphics = true;
            repaint();
            loader.waitForID(0);
        }
        catch (InterruptedException e)
        {
            System.out.println("Error while loading file. Unable to continue!");
            Graphics g = this.getGraphics();
            loadingGraphics = false;
            g.drawString("Error loading graphics. Unable to continue!",20,20);
            return;
        }

        loadingGraphics = false;
        TreeTemplate.setImages(image,name.length);
    }




    /** Sets the tree's operation mode.
    * This is done by calling TreeTemplate's setOperation if
    * an instance of this class exists, i.e. tree isn't null.
    *
    * @param o the operation that should be set.
    * @see CenterPanel#tree
    * @see TreeTemplate#setOperation
    */
    public void setOperation(String o){if (tree != null) tree.setOperation(o);}




    /** Called when a new tree is chosen from the menu.
    * Does the necessary updates of private variabels,
    * and adds the appropriate mouse listeners.
    *
    * @param treeType the type of tree that was chosen.
    */
    public void setTree(String treeType)
    {
        // Whenever a new tree is set, don't enable input from user
        top.readyForSearchInput(false);

        // No ("None") tree was chosen. Repaint and return
        if (treeType.equals(BinaryTreesome.treeType[0]))
        {
           tree = null;
           repaint();
           return;
        }

        // A tree has been chosen. Remove old listeners
        removeMouseListener(tree);
    	removeMouseMotionListener(tree);
        top.removeListener();

        // Set a new tree, and update the label to be output in the panel

        // Standard-tree
        if (treeType.equals(BinaryTreesome.treeType[1]))
        {
           tree = new StandardTree(bottom, top, this, mainClass);
           treeLabel = "Standard Binary Tree";
        }

        // Sorted-tree
        else if (treeType.equals(BinaryTreesome.treeType[2]))
        {
           tree = new SortedTree(bottom, top, this, mainClass);
           treeLabel = "Sorted Binary Tree";
        }

        // AVL-tree
        else if (treeType.equals(BinaryTreesome.treeType[3]))
        {
           tree = new AVLTree(bottom, top, this, mainClass);
           treeLabel = "AVL Tree";
        }

        // AA-tree
        else if (treeType.equals(BinaryTreesome.treeType[4]))
        {
           tree = new AATree(bottom, top, this, mainClass);
           treeLabel = "AA Tree";
        }

        // Splay-tree
        else if (treeType.equals(BinaryTreesome.treeType[5]))
        {
           tree = new SplayTree(bottom, top, this, mainClass);
           treeLabel = "Not Implemented";
        }

        // RedBlack-tree
        else if (treeType.equals(BinaryTreesome.treeType[6]))
        {
           tree = new RedBlackTree(bottom, top, this, mainClass);
           treeLabel = "Not Implemented";
        }

        // Let the tree chosen be the mouselistener for this and the top-panel
        addMouseListener(tree);
    	addMouseMotionListener(tree);
        top.addListener(tree);

        // Output info and paint the big label in the panel
        paintBig(getGraphics(),treeLabel);
    }
}




// -------------------------------- End of class ------------------------------

