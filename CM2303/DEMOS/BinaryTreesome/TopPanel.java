// ---------------------------------- Imports ---------------------------------




import java.awt.*;




// ------------------------------- Start of class -----------------------------




/**
* A class representing a panel to hold other components.
* This is the panel at top of the applet.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class TopPanel extends Panel
{
    // ----- Variables ----

    /** A canvas to output text connected to the inputPanel.
    */
    private InputLabel label        = new InputLabel();

    /** The panel where the user can input numbers.
    */
    private InputPanel input        = new InputPanel();

    /** A canvas where messages concerning which operation is in progress can be output.
    */
    private MessagePanel message    = new MessagePanel();




    // ---------------------------- Constructor -------------------------------

    
    
    
    /** Constructs a panel with several components in it.
    * The private variables message, label and input are
    * added to the panel which has a border layout.
    */
    public TopPanel()
    {
        // Set the backgroundcolor of the different components in the panel
    	label.setBackground(Color.lightGray);
    	input.setBackground(Color.lightGray);
    	message.setBackground(Color.lightGray);

        // Set layout type
    	setLayout(new BorderLayout());

        // Add the components to the panel
        add("Center",message);
        add("North",label);
        add("West",input);
    }




    // -------------------------- Other functions -----------------------------

    
    
    
    /** Adds a listener to the InputPanel input.
    * Sets the tree received to listen to the events in the InputPanel.
    * This is done by calling inputPanel's addListener function.
    *
    * @param tree the tree that will act as listener
    * @see InputPanel#addListener
    */
    public void addListener(TreeTemplate tree){input.addListener(tree);}

    
    
    
    /** Clears the text in the TextField in the InputPanel.
    * This is done by calling InputPanel's clearInput function.
    *
    * @see InputPanel#clearInput
    */
    public void clearInput(){input.clearInput();}




    /** Returns the text in the TextField in the InputPanel.
    * This is done by calling InputPanel's getInput function.
    *
    * @see InputPanel#getInput
    */
    public String getInput(){return input.getInput();}

    
    
    
    /** Makes the TextField in the InputPanel input ready for insertion input.
    * Makes the inputbox visible and outputs the insertion label above it.
    * This is done by calling InputPanel's readyForInput and
    * setVisible (with b as parameter) functions.
    * The function writeInputLabel is called if b is true,
    * and the InputLabel label's writeWelcome function if false.
    *
    * @see TopPanel#writeInputLabel
    * @see InputPanel#readyForInput
    * @see InputLabel#writeWelcome
    */
    public void readyForInsertionInput(boolean b)
    {
        input.setVisible(b);

        // Input panel is visible, draw input-guide
        if (b)
            writeInputLabel(InfoMessage.enterInsertionNumbers());
        else
            label.writeWelcome();

        input.readyForInput();
    }




    /** Makes the TextField in the InputPanel input ready for search input.
    * Makes the inputbox visible and outputs the search label above it.
    * This is done by calling InputPanel's readyForInput and
    * setVisible (with b as parameter) functions.
    * The function writeInputLabel is called if b is true,
    * and the InputLabel label's writeWelcome function if false.
    *
    * @see TopPanel#writeInputLabel
    * @see InputPanel#readyForInput
    * @see InputLabel#writeWelcome
    */
    public void readyForSearchInput(boolean b)
    {
        input.setVisible(b);

        // Input panel is visible, draw input-guide
        if (b)
            writeInputLabel(InfoMessage.enterSearchNumber());
        else
            label.writeWelcome();

        input.readyForInput();
    }

    
    
    
    /** Removes the listener from the inputPanel input.
    * This is done by calling InputPanel's removeListener function.
    *
    * @param tree the listener that is to be removed
    * @see InputPanel#removeListener
    */
    public void removeListener(){input.removeListener();}

    
    
    
    /** Sets the label of the rightmost button to "Change".
	 *
    * @see InputPanel#setChangeButton
    */
    public void setChangeButton(){if (input.isVisible()) input.setChangeButton();}




    /** Sets the string received as the text in the InputPanel input's TextField.
    * This is done by calling InputPanel's setInput function.
    *
    * @param s the string to be set in the textField.
    * @see InputPanel#setInput
    */
    public void setInput(String s){input.setInput(s);}




    /** Sets the label of the rightmost button to "Random".
	 *
    * @see InputPanel#setRandomButton
    */
    public void setRandomButton(){if (input.isVisible()) input.setRandomButton();}




    /** Calls the InputLabel label's writeInfo function.
    * This function makes sure the string is printed.
    *
    * @param s the string that should be printed
    * @see InputLabel#writeInfo
    */
    public void writeInputLabel(String s){label.writeInfo(s);}



    
    /** Calls the MessagePanel message's writeOperation function.
    * This function makes sure the string is printed.
    *
    * @param s the string that should be printed
    * @see MessagePanel#writeOperation
    */
    public void writeOperation(String s){message.writeOperation(s);}
}




// -------------------------------- End of class ------------------------------
