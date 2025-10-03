// ---------------------------------- Imports ---------------------------------




import java.awt.*;




// ------------------------------- Start of class -----------------------------




/**
* A thread class that moves a node.
* The thread moves a node pixel by pixel from it's current
* position to it's destination, both defined whithin the node.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class MoveNode extends Thread
{
    // ----- Variables ----

    /** The node that should move.
    */
    private Node node            = null;

    /** The panel to output messages in.
    */
    private Point destination    = null;

    /** The panel on which the node should move (be drawn).
    */
    private CenterPanel panel    = null;




    // ---------------------------- Constructor -------------------------------




    /** Constructs a thread to move a node.
    *
    * @param tName the name of the thread
    * @param n the node that is to be moved
    * @param p the panel in which the node is to be painted
    * @see CenterPanel
    * @see Node
    */
    public MoveNode(String tName, Node n, CenterPanel p)
    {
        super(tName);

        node = n;
        panel = p;
        destination = new Point(n.getDestination());

        n.setThread(this);                   // Let the node know it's being moved by this thread
    }




    // -------------------- Automatically called functions --------------------




    /** Takes care of the actual moving of the node.
    * The node is moved pixel by pixel from it's current
    * position to it's destination, both defined whithin the node.
    *
    * @see Node#center
    * @see Node#destination
    */
    public void run()
    {
        while((destination.x != (node.getCenter()).x) || (destination.y != (node.getCenter()).y))
        {
            int x = (node.getCenter()).x;
            int y = (node.getCenter()).y;

            if (x < destination.x)
                node.setCenter(new Point(++x,y));
            else if (x > destination.x)
                node.setCenter(new Point(--x,y));
            if (y < destination.y)
                node.setCenter(new Point(x,++y));
            else if (y > destination.y)
                node.setCenter(new Point(x,--y));

            panel.repaint();

            try
            {
                sleep(5);
            }
            catch(InterruptedException e)
            {
                node.setThread(null);
            }
        }
        node.setThread(null);
    }




    // -------------------------- Other functions -----------------------------




    /** Sets the destination of the node to be moved.
    *
    * @param p the new destination point
    */
    public void setDestination(Point p){destination = new Point(p);}




    /** Sets the node that is to be moved.
    * If a node that the thread is moving needs to be
    * replaced, this function sets the new node.
    *
    * @param n the new node that is to be moved
    */
    public void setNode(Node n){node = n;}
}




// -------------------------------- End of class ------------------------------

