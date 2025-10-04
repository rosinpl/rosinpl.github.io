// ------------------------------- Start of class -----------------------------




import java.util.Properties;
import java.awt.*;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.awt.event.*;




// ------------------------------- Start of class -----------------------------




/**
* A class representing a window on the screen.
* We use this window to show the algorithms in.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class Window extends Frame
implements WindowListener, ActionListener
{
    // ----- Variables ----

    /** File to read from.
    */
    private FilePanel filePanel     = new FilePanel();

    /** Panel with buttons.
    */
    private ButtonPanel buttonPanel = new ButtonPanel(this);

    /** Panel to output messages in.
    */
    private BottomPanel bottom      = null;


    // Window coordinates

    /** The x coordinate of the window's upper left hand corner.
    */
    private int winx            = 100;

    /** The y coordinate of the window's upper left hand corner.
    */
    private int winy            = 100;

    /** The width of the window.
    */
    private int winwidth        = 600;

    /** The height of the window.
    */
    private int winheight       = 500;

    /** The windows last title. If it changes, buffers are reset.
    */
    private String fileName     = "";




    // ---------------------------- Constructor -------------------------------




    /** Constructs a window and adds two panels to it.
    * A Filepanel and a ButtonPanel is added to the window
    * to respectivly show input from a file and hold two
    * buttons. The window acts as it's own listener.
    *
    * @see FilePanel
    * @see ButtonPanel
    */
    public Window()
    {
        // Set the layout, the color, and the size and location of the window
        setLayout(new BorderLayout());
        setBounds(winx, winy, winwidth, winheight);
        setBackground(Color.white);

        // Set the background of the two panels in the window
        filePanel.setBackground(Color.white);
        buttonPanel.setBackground(Color.white);

        // Add the panels to the window
        add("Center",filePanel);
        add("South",buttonPanel);

        // Add a listener to respond to the events in the window
        addWindowListener(this);
    }




    // -------------------- Automatically called functions --------------------




    /** Takes care of painting the graphics in the window.
    * The actual painting is done in the FilePanel's paint function.
    *
    * @param g the Graphics context in which things will be painted
    */
    public void paint(Graphics g)
    {
        filePanel.repaint();
    }




    /** Calls the FilePanel's paint function.
    *
    * @param g the Graphics context in which things will be painted
    */
    public void update(Graphics g)
    {
        filePanel.repaint();
    }




    // ---------------------- Action-related functions ------------------------




    /** Decides what to to when one of the buttons in the window is clicked.
    *
    * @param g the action event
    */
    public void actionPerformed(ActionEvent e)
    {
        String chosen = new String(e.getActionCommand());

        // Close-button pressed
        if (chosen.equals("Close"))
        {
            setVisible(false);
            bottom.writeError("");
        }

        // Print-button pressed
        else if (chosen.equals("Print"))
        {
/*            Toolkit t      = (filePanel.getTextArea()).getToolkit();
            PrintJob job   = t.getPrintJob(this, getTitle(), new Properties());

            // User clicked cancel in the print-dialog
            if (job == null)
               return;

            Graphics page  = job.getGraphics();
            Dimension pageSize = job.getPageDimension();
//            page.setClip(7,7,pageSize.width-14,pageSize.width-14);
  //          page.paint((filePanel.getTextArea()).getGraphics());
//            page.drawString((filePanel.getTextArea()).getText(),20,20);
//            page.drawString("Morn kjekke karer! :) Dere er jammen FLINKE! Skulle ønske vi var dere...",20,40);
//            (filePanel.getTextArea()).print(page);
            print(page);
            page.dispose();
            job.end(); */
        }
    }




    // ---------------------- Window-related functions ------------------------

    
    
    
    /** Decides what to to when the window is activated.
    * It is set visible and FilePanel's requestFocus is called.
    *
    * @param e the window event
    */
    public void windowActivated(WindowEvent e){setVisible(true);filePanel.requestFocus();}

    
    
    
    /** Decides what to to when the window is closed.
    * It is set not visible.
    *
    * @param e the window event
    */
    public void windowClosed(WindowEvent e){setVisible(false); bottom.writeError("");}

    
    
    
    /** Decides what to to when the window is closing.
    * It is set not visible.
    *
    * @param e the window event
    */
    public void windowClosing(WindowEvent e){setVisible(false); bottom.writeError("");}

    
    

    /** Decides what to to when the window is opened.
    * It is set visible and FilePanel's requestFocus is called.
    *
    * @param e the window event
    */
    public void windowOpened(WindowEvent e){setVisible(true);filePanel.requestFocus();}



    
    /** Just here to satisfy the compiler.
    *
    * @param e the window event
    */
    public void windowDeactivated(WindowEvent e){}




    /** Just here to satisfy the compiler.
    *
    * @param e the window event
    */
    public void windowDeiconified(WindowEvent e){}




    /** Just here to satisfy the compiler.
    *
    * @param e the window event
    */
    public void windowIconified(WindowEvent e){}




    // -------------------------- Other functions -----------------------------




    /** Returns the filename of the last file read.
    *
    * @return the contents of the fileName variable
    */
    public String getFilename(){return fileName;}



/*
    public void print(Graphics g)
    {
        page.drawLine(1,100,pageSize.width-5,1);
    }
  */





    /** Reads the file sent to the function.
    *
    * @param l the base URL as a string
    * @param f the filename
    */
    public void readFile(String l, String f){filePanel.readFile(l, f);}

        

  
    /** Sets the panel where messages should be output.
    * Sets the private variable bottom, and calls
    * the FilePanel and ButtonPanel's setBottom functions.
    *
    * @param b the panel where messages should be output
    */
    public void setBottom(BottomPanel b)
    {
        bottom = b;
        filePanel.setBottom(b);
    }




    /** Sets the name of the file that is to be read.
    *
    * @param f the filename
    */
    public void setFilename(String f){fileName = f;}

    
    
    
    /** Fill the window with the text from the file sent to the function.
    *
    * @param title the title of the window
    * @param f the filerader
    */
    public void outputAlgorithm(String title)
    {
        // Define the title of the window
        setTitle(title);

        // If the window allready is up, bring it to front and give it focus...
        if (isShowing())
        {
            toFront();
            requestFocus();
        }

        // ...otherwise make it visible and set which file to read and read (and show) it
        else
            setVisible(true);

        repaint();
    }
}




// -------------------------------- End of class ------------------------------
