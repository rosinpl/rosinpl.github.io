// ---------------------------------- Imports ---------------------------------




import java.awt.*;
import java.util.Stack;




// ------------------------------- Start of class -----------------------------




/**
* A thread class that moves a small tracer around in the tree.
* The thread moves the ball pixel by pixel from one node to another.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class Tracer extends Thread
{
    // ----- Constants ----

    /** The radius of the tracer ball.
    */
    public static final int RADIUS      = 5;

    // ----- Variables ----

    /** The node to start the trace from.
    */
    private Node root           = null;

    /** The numbers to be traced.
    */
    private Stack order         = null;

    /** Tracer's current center.
    */
    private Point traceCenter   = null;

    /** Tracer's destination center.
    */
    private Point destination   = null;

    /** Tells if the tree is sorted or not.
    */
    private boolean sorted      = true;

    /** The panel where the tracer should be painted.
    */
    private CenterPanel center  = null;

    /** The panel to output messages in.
    */
    private BottomPanel bottom  = null;




    // ---------------------------- Constructors ------------------------------




    /** Constructs a a thread to move the tracer ball.
    * The thread needs to know about the the center panel because
    * this is where the ball is painted. It also needs to know
    * about the bottom panel to output messages.
    * The stack contains the order in which the nodes in the tree
    * should be selected.
    *
    * @param c the panel in which to paint
    * @param b the panel in which to print messages
    * @param c the stack containing the order of selection
    * @see CenterPanel
    * @see BottomPanel
    * @see TreeTemplate#order
    * @see TreeTemplate#searchOrder
    */
    public Tracer(CenterPanel c, BottomPanel b, Stack s, boolean sorted)
    {
        this.sorted = sorted;
        center = c;
        bottom = b;
        order = s;
    }




    // -------------------- Automatically called functions --------------------




    /** Controls the movement of the tracer ball.
    * The run function uses the Stack order to decide where the tracer
    * ball shall move, and calls the move function to move it.
    *
    * @see Tracer#order
    * @see Tracer#moveTracer
    */
    public synchronized void run()
    {
        Node n = root;

        TreeTemplate t = center.getTree();

        String value = "";
        boolean first = true;
        boolean search = t.searchOperation();

        setCenter((t.getRootPos()).x,RADIUS+5);
        setDestination(n.getCenter());

        moveTracer();

        while(!order.empty())
        {
            value = (String)order.peek();

            while (!value.equals(n.getValue()))
            {
                if ((n.getLeft() != null) && (!((n.getLeft()).getType()).equals(Node.SHADOW)) &&
                    (!((n.getLeft()).getType()).equals(Node.SELECTED)))
                {
                    if ((sorted == true && Integer.parseInt(value) < Integer.parseInt(n.getValue()) || (!sorted)))    
                    {
                        n = n.getLeft();
                        setDestination(n.getCenter());
                        moveTracer();
                        continue;
                    }
                }
                
                if ((n.getRight() != null) && (!((n.getRight()).getType()).equals(Node.SHADOW)) &&
                    (!((n.getRight()).getType()).equals(Node.SELECTED)))
                {
                    if ((sorted == true && Integer.parseInt(value) > Integer.parseInt(n.getValue()) || (!sorted)))    
                    {
                        n = n.getRight();
                        setDestination(n.getCenter());
                        moveTracer();
                        continue;
                    }
                }

                if (n.getParent() != null)
                {
                    n = n.getParent();
                    setDestination(n.getCenter());
                    moveTracer();
                    continue;
                }

                System.out.println("Everything just fucked up majorly!! Exiting...");
                System.exit(0);
            }

            // Number is found. Select the node with the number, pop number from stack
            n.setType(Node.SELECTED);
            order.pop();

            // Write progress message
            if (!search)
            {
                if (!first)
                    bottom.addToProgress(", " + value);
                else
                {
                    bottom.addToProgress(value);
                    first = false;
                }
            }
            center.repaint();
        }

        center.repaint();
        bottom.writeError("");

        if (search)
        {
            if (!(value.equals(t.getSearchValue())))
                 bottom.writeInfo(InfoMessage.searchValueNotFound());
            else
                 bottom.writeInfo(InfoMessage.finishedSearch());

            bottom.writeProgress("");
            (center.getTree()).setSearchValue("");
            ((center.getTree()).getTop()).readyForSearchInput(true);
        }

        else
        {
            bottom.addToProgress(".");
            bottom.writeInfo(InfoMessage.finishedIteration());
        }

        center.repaint();
    }




    // -------------------------- Other functions -----------------------------




    /** Returns the tracer's current center.
    *
    * @see Tracer#traceCenter
    */
    public Point getCenter(){return traceCenter;}




    /** Returns the tracer's destination.
    *
    * @see Tracer#destination
    */
    public Point getDestination(){return destination;}




    /** Returns the tracer's radius.
    *
    * @see Tracer#RADIUS
    */
    public int getRadius(){return RADIUS;}




    /** Moves the tracer from it's current position to it's destination.
    * The tracer moves in a straight line between the points kept in the
    * private variables traceCenter and destination.
    *
    * @see Tracer#traceCenter
    * @see Tracer#destination
    */
    public synchronized void moveTracer()
    {
        // Calculate the maximum distance in either direction
        int width = (destination.x - traceCenter.x);
        int pos_width = width<0?((-1)*(width)):width;
        int height= (destination.y - traceCenter.y);
        int pos_height= height<0?((-1)*(height)):height;
        int maxDistance = (pos_width)>(pos_height)?(pos_width):(pos_height);

        // Calculate steps in either direction (in pixels)
        float fx = (((float)width)/((float)maxDistance));
        float fy = (((float)height)/((float)maxDistance));

        // Get the coordinates of the center when started
        float x = traceCenter.x;
        float y = traceCenter.y;

        for (int i=0; i<maxDistance; i++)
        {
            // Update the coordinates and repaint
            x += fx;
            y += fy;
            setCenter((int)x,(int)y);
            center.repaint();
            try
            {
                sleep(5);
            }
            catch(InterruptedException e)
            {
                traceCenter = destination;
                stop();
            }
        }

        // Set the current center to destination at the end to make sure the
        // tracer actually reached the exact destination point
        traceCenter = destination;
        center.repaint();
    }




    /** Sets the tracer's center.
    *
    * @param x the x coordinate of the tracer's new center
    * @param y the y coordinate of the tracer's new center
    * @see Tracer#center
    */
    public void setCenter(int x, int y){traceCenter = new Point(x,y);}




    /** Sets the tracer's center.
    *
    * @param c the point that is to be the tracer's new center
    * @see Tracer#traceCenter
    */
    public void setCenter(Point c){traceCenter = new Point(c);}




    /** Sets the tracer's destination.
    *
    * @param x the x coordinate of the tracer's new destination
    * @param y the y coordinate of the tracer's new destination
    * @see Tracer#destination
    */
    public void setDestination(int x, int y){destination = new Point(x,y);}




    /** Sets the tracer's destination.
    *
    * @param c the point that is to be the tracer's new destination
    * @see Tracer#destination
    */
    public void setDestination(Point c){destination = new Point(c);}




    /** Sets the node that the tracer should start the trace from.
    * This node will in all cases be the tree's rootNode.
    *
    * @param n the node to start the trace from
    * @see Tracer#root
    */
    public void setRoot(Node n){root = n;}
}



// -------------------------------- End of class ------------------------------

