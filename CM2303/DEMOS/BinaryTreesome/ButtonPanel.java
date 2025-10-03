// ---------------------------------- Imports ---------------------------------




import java.awt.*;




// ------------------------------- Start of class -----------------------------




/**
* A class representing a panel with a close and a print button.
* Used in the algorithm windows.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class ButtonPanel extends Panel
{
    // ----- Variables ----

    // The buttons in this panel

    /** The close button in this panel.
    */
    private Button close              = new Button("Close");

    /** The print button in this panel.
    */
    private Button print              = new Button("Print");




    // ---------------------------- Constructor -------------------------------




    /** Constructs a panel with a close and print button.
    *
    * @param w the window that will act as an actionListener for the buttons
    */
    public ButtonPanel(Window w)
    {
        // Set the buttons' action-listener
        close.addActionListener(w);
        print.addActionListener(w);

        // Add the buttons to the panel
        add(close);
        add(print);
    }
}




// -------------------------------- End of class ------------------------------

