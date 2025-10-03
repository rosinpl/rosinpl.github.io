// ---------------------------------- Imports ---------------------------------




import java.awt.*;
import java.util.Stack;
import java.util.Enumeration;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.awt.event.*;




// ------------------------------- Start of class -----------------------------




/**
* A class representing a panel containing a text area.
* Used to show the algorithms in the window.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class FilePanel extends Panel
//implements KeyListener
{
    // ----- Variables ----


    /** Panel to output error messages in.
    */
    private BottomPanel bottom              = null;

    /** The text area in which files will be output.
    */
    private TextArea area                   = new TextArea("",0,0,TextArea.SCROLLBARS_BOTH);




    // ---------------------------- Constructor -------------------------------







    /** Constructs the panel.
    */
    public FilePanel()
    {
        area.setEditable(false);
        add(area);
    }




    // -------------------- Automatically called functions --------------------




    /** Takes care of painting the graphics in this panel.
    *
    * @param g the Graphics context in which things will be painted
    */
    public void paint(Graphics g)
    {
        Rectangle r = getBounds();
        area.setBounds(0,0,r.width,r.height);
        area.setVisible(true);
    }




    // -------------------------- Other functions -----------------------------




    /** Prints the contents of the text area.
    *
    * @param g the Graphics context in which things will be painted
    */
    public void print(Graphics g)
    {
/*        Dimension d = this.getSize();
        Image image = this.createImage(d.width, d.height);
        Graphics page = image.getGraphics();
        page.drawString("Hei på deg du.",20,35);
        page.drawString("ljah sdlkjha sdlkj aj,ndsfklsj dflkjasdklja sdlkja sdlk",40,45);
        page.drawLine(1,1,100,100);
    	g.drawImage(image,20,20,null);*/
    }




    /** Reads a file and write it in a TextArea component.
    *
    * @param location the base url of the file
    * @param file the name of the file
    */
    public void readFile(String location, String file)
    {
        URL url;                // The url to be opened
        InputStream stream;     // The stream to be read
        area.setText("Trying to read file....\n\n");

        // Try to open the URL-address
        try
        {
            url = new URL(location + file);
//            url = new URL("http://chaos.iu.hioslo.no/~kjenslj/java/applets/latest/readme.txt");
        }
        catch (MalformedURLException e)
        {
            bottom.writeError("Malformed URL. Cannot open file " + file + " at " + location + ".");
            return;
        }


        // Try to open the stream
        try
        {
            stream = url.openStream();
        }
        catch (IOException e)
        {
            bottom.writeError("Couldn't open the stream to read the file " + file + " at " + location + ".");
            area.append("Couldn't open the stream to read the file " + file + " at " + location + ".");
            return;
        }

        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader in = new BufferedReader(reader);

        String text    = "";               // All lines read
        String line    = "";               // line read

        // try to read the file
        try
        {
            while (true)
            {
                // Read a line from the file
                line = in.readLine();

                if (line != null)
                    text += line + "\n";
                else
                    break;
            }

            area.setText(text);
        }

        catch(IOException e)
        {
            bottom.writeError("An I/O-error ocurred while reading the file with the algorithm!");
            return;
        }
    }




    /** Sets the BottomPanel variable bottom.
    *
    * @param b the panel bottom should point to
    */
    public void setBottom(BottomPanel b){bottom = b;}




    /** Returns the text area.
    *
    * @return the TextArea variable area
    */
    public TextArea getTextArea(){return area;}
}




// -------------------------------- End of class ------------------------------

