// ---------------------------------- Imports ---------------------------------




import java.awt.*;




// ------------------------------- Start of class -----------------------------




/**
* A class representing a small canvas used as a text label.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class SectionLabel extends Canvas
{
    // ----- Variables ----


    /** The foreground-color of the label.
    */
    private Color fore      = Color.blue;


    /** The label to be output.
    */
    private String label    = null;




    // ---------------------------- Constructor -------------------------------




    /** Constructs a canvas with text.
    * The size of the canvas is set to 100x15 pixels.
    * The private variable label is set to the string received.
    *
    * @param l the string that will be the text on this label
    */
    public SectionLabel(String l)
    {
        // Set the label and the size of the canvas
        label = new String(l);
        this.setSize(100,15);
    }




    // -------------------- Automatically called functions --------------------

    
    
    
    /** Takes care of painting the graphics in this canvas.
    * Writes the private string variable label on the canvas.
    *
    * @param g the Graphics context in which things will be painted
    */
    public void paint(Graphics g)
    {
        // Get the fontmetrics and calculate the size of the strings
      	FontMetrics fm = g.getFontMetrics();
        int textWidth  = fm.stringWidth(label);
        int textHeight = fm.getMaxDescent()+fm.getMaxAscent();

        // Set the size of the label and draw it in the canvas
        setSize(textWidth,textHeight);
        g.setColor(fore);
        g.drawString(label,0,textHeight-fm.getMaxDescent());
    }
}




// -------------------------------- End of class ------------------------------
