// ---------------------------------- Imports ---------------------------------




import java.awt.*;




// ------------------------------- Start of class -----------------------------




/**
* A class representing a small canvas in which information is printed.
* This canvas is situated in the TopPanel and in it information about
* which operation is in progress is printed.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class MessagePanel extends Canvas
{
    // ----- Variables ----

    /** The string to be printed in the canvas.
    */
    private String operation        = "";

    // Some color-definitions

    /** The color in which to print the welcome message.
    */
    private Color welcomeColor      = Color.black;

    /** The color in which to print operations.
    */
    private Color operationColor    = Color.black;




    // ---------------------------- Constructor -------------------------------




    /** Constructs the canvas.
    * The canvas' size is set to 200x20 pixels.
    */
    public MessagePanel(){this.setSize(200,20);}




    // -------------------- Automatically called functions --------------------




    /** Takes care of painting the graphics in this canvas.
    * Writes the appropriate string depending on which
    * operation is in progress. The string printed is
    * variable operation.
    *
    * @param g the Graphics context in which things will be painted
    * @see MessagePanel#operation
    */
    public void paint(Graphics g)
    {
        Dimension d = getSize();

      	FontMetrics fm = g.getFontMetrics();
        int sh = fm.getMaxDescent()+fm.getMaxAscent();
        int sw = fm.stringWidth(operation);

        g.setColor(operationColor);
        g.drawString(operation,d.width-sw-115,sh+5);
    }




    // -------------------------- Other functions -----------------------------




    /** Adds the string sent to the function to the private variable operation.
    *
    * @param t the string added to the variable operation
    * @see MessagePanel#operation
    */
    public void addToOperation(String t)
    {
        operation += t;
        writeOperation(operation);
    }




    /** Prints out the current operation.
    * The string is set as the variable operation and repaint is called.
    *
    * @param t the string set
    * @see MessagePanel#operation
    */
    public void writeOperation(String t)
    {
        operation = t;
        repaint();
    }
}




// -------------------------------- End of class ------------------------------

