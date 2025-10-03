// ---------------------------------- Imports ---------------------------------




import java.awt.*;




// ------------------------------- Start of class -----------------------------




/**
* A class representing a small canvas in the top panel of the applet.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class InputLabel extends Canvas
{
    // ----- Variables ----

    /** The label to be drawn.
    */
    private String infoLabel    = "";

    // Some labels to be output in this panel, and their color

    /** The color of the label to be drawn.
    */
    private Color labelColor    = Color.black;

    /** Whether or not top write the standard applet name.
    */
    private boolean welcome     = true;



    // ---------------------------- Constructor -------------------------------




    /** Constructs a canvas.
    * The size of the canvas is set to 720x16 pixels,
    * and the function writeWelcome is called.
    *
    * @see InputLabel#writeWelcome
    */
    public InputLabel(){this.setSize(720,16); writeWelcome();}




    // -------------------- Automatically called functions --------------------




    /** Takes care of painting the graphics in this canvas.
    * Writes the appropriate string depending on whether the
    * flag welcome is set or not.
    *
    * @param g the Graphics context in which things will be painted
    * @see InputLabel#welcome
    */
    public void paint(Graphics g)
    {
        // Remember old font
        Font old= new Font((g.getFont()).getName(),(g.getFont()).getStyle(),(g.getFont()).getSize());

      	FontMetrics fm;
        Dimension d = getSize();
        g.setColor(Color.black);

        // If flag is set, write welcomeLabel on top
        if (welcome)
        {
            Font w  = new Font((g.getFont()).getName(),Font.BOLD,12);
            g.setFont(w);
            fm = g.getFontMetrics();
     	    g.drawString(InfoMessage.appletName(),BinaryTreesome.BORDERSIZE,fm.getMaxAscent());
     	}

        // Flag isn't set, draw the input-info label
        else
        {
            fm = g.getFontMetrics();
     	    g.drawString(infoLabel,BinaryTreesome.BORDERSIZE,fm.getMaxAscent());
     	}

        // Always output who wrote this program to the top right in small letters
        Font us = new Font((g.getFont()).getName(),Font.PLAIN,9);
        g.setFont(us);
        fm = g.getFontMetrics();
        int x = d.width - fm.stringWidth(InfoMessage.whoAreWe());
        g.drawString(InfoMessage.whoAreWe(),x,fm.getMaxAscent());

        // Restore old font
        g.setFont(old);
    }




    // -------------------------- Other functions -----------------------------




    /** Outputs the string received.
    * The variable infoLabel is set to the string s, and the flag
    * welcome to false. Calls repaint.
    *
    * @param s the string that should be printed
    * @see InputLabel#welcome
    * @see InputLabel#infoLabel
    * @see InputLabel#paint
    */
    public void writeInfo(String s){infoLabel = s; welcome = false; repaint();}




    /** Prints the welcome message.
    * The flag welcome is set to true, and repaint is called.
    *
    * @param s the string that should be printed
    * @see InputLabel#welcome
    * @see InputLabel#paint
    */
    public void writeWelcome(){welcome = true; repaint();}

}




// -------------------------------- End of class ------------------------------

