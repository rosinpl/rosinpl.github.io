// ---------------------------------- Imports ---------------------------------




import java.awt.*;




// ------------------------------- Start of class -----------------------------




/**
* A class representing the message area at the bottom of the applet.
* The BottomPanel is a canvas where different messages to the user
* are written.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class BottomPanel extends Canvas
{
    // ----- Constants ----


    /** Holds the maximum length of a line in characters.
    */
    private static final int lineLength = 130;

    // ----- Variables ----

    // The messages that should be output, and their corresponding colors

    /** Message to be output in the top part of the bottom panel.
    */
    private String progress             = null;

    /** Message to be output in the middle part of the bottom panel.
    */
    private String info                 = null;

    /** Message to be output in the bottom part of the bottom panel.
    */
    private String error                = null;

    /** Color in which progress messages should be printed.
    */
    private Color progressColor         = Color.black;

    /** Color in which information messages should be printed.
    */
    private Color infoColor             = Color.blue;

    /** Color in which error messages should be printed.
    */
    private Color errorColor            = Color.red;




    // ---------------------------- Constructor -------------------------------




    /** Constructs a blank BottomPanel canvas.
    * The size of the canvas is set to 700x70 inside the class.
    */
    public BottomPanel()
    {
        // Set the size of the canvas and clear it
        this.setSize(700,70);
        writeProgress("");
        writeInfo("");
        writeError("");
    }




    // -------------------- Automatically called functions --------------------




    /** Prints information in the BottomPanel.
    * Prints the variables progress, info and error
    * in the correct places in the correct colors.
    *
    * @param g the Graphics context of the canvas where things will be painted
    * @see BottomPanel#progress
    * @see BottomPanel#info
    * @see BottomPanel#error
    * @see BottomPanel#progressColor
    * @see BottomPanel#infoColor
    * @see BottomPanel#errorColor
    */
    public void paint(Graphics g)
    {
      	FontMetrics fm = g.getFontMetrics();
        int textHeight = fm.getMaxDescent()+fm.getMaxAscent();

        // Print out the different variables on the right place in the right colors
        g.setColor(progressColor);
        g.drawString(progress,BinaryTreesome.BORDERSIZE,textHeight);
        g.setColor(infoColor);
        g.drawString(info,BinaryTreesome.BORDERSIZE,2*textHeight);
        g.setColor(errorColor);
        g.drawString(error,BinaryTreesome.BORDERSIZE,3*textHeight);
    }




    // -------------------------- Other functions -----------------------------




    /** Returns a string that will (most likely) fit on one line.
    * Receives a string and returns a substring that will fit on
    * one line if it is too long, i.e. longer than lineLength.
    *
    * @param s the string which length will be checked.
    * @return the substring that will fit on one line.
    * @see BottomPanel#lineLength
    */
    public String longestString(String s)
    {
        if (s.length() > lineLength)
        {
            String x = s.substring(0,lineLength-1);
            x += "....";
            return x;
        }

        return s;
    }




    /** Updates the progress variable and repaints.
    *
    * @param t the string that the progress variable should be set to.
    * @see BottomPanel#progress
    * @see BottomPanel#paint
    */
    public void writeProgress(String t)
    {
        progress = longestString(t);
        repaint();
    }




    /** Adds a string to the progress variable and repaints.
    *
    * @param t the string that should be added to the progress variable.
    * @see BottomPanel#progress
    * @see BottomPanel#paint
    */
    public void addToProgress(String t)
    {
        progress += t;
        writeProgress(progress);
    }




    /** Updates the info variable and repaints.
    *
    * @param t the string that the info variable should be set to.
    * @see BottomPanel#info
    * @see BottomPanel#paint
    */
    public void writeInfo(String t)
    {
        info = longestString(t);
        repaint();
    }




    /** Adds a string to the info variable and repaints.
    *
    * @param t the string that should be added to the info variable.
    * @see BottomPanel#info
    * @see BottomPanel#paint
    */
    public void addToInfo(String t)
    {
        info += t;
        writeInfo(info);
    }




    /** Updates the error variable and repaints.
    *
    * @param t the string that the error variable should be set to.
    * @see BottomPanel#error
    * @see BottomPanel#paint
    */
    public void writeError(String t)
    {
        error = longestString(t);
        repaint();
    }




    /** Adds a string to the error variable and repaints.
    *
    * @param t the string that should be added to the error variable.
    * @see BottomPanel#error
    * @see BottomPanel#paint
    */
    public void addToError(String t)
    {
        error += t;
        writeError(error);
    }
}




// -------------------------------- End of class ------------------------------

