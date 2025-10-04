// ---------------------------------- Imports ---------------------------------




import java.awt.*;




// ------------------------------- Start of class -----------------------------




/**
* A class representing a small panel in the top panel of the applet.
* This panel coantains a textField and two buttons.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class InputPanel extends Panel
{
    // ----- Variables ----

    /** The box where input should be taken from user.
    */
    private TextField inputBox      = new TextField(20);

    /** The tree that should listen to the actions from the buttons.
    */
    private TreeTemplate tree       = null;

    // The buttons in this panel

    /** The add button in this panel.
    */
    private Button addButton        = new Button(BinaryTreesome.inputType[0]);

    /** The change button in this panel.
    */
    private Button changeButton     = new Button(BinaryTreesome.inputType[1]);




    // ---------------------------- Constructor -------------------------------




    /** Constructs a panel and adds a textField and two buttons.
    */
    public InputPanel()
    {
        // Set the layout and add the textfield and the buttons to the panel
        setLayout(new FlowLayout(FlowLayout.LEFT));

        addButton.setBackground(Color.lightGray);
        changeButton.setBackground(Color.lightGray);

        add(inputBox);
        add(addButton);
        add(changeButton);
    }




    // -------------------------- Other functions -----------------------------




    /** Updates the variable "tree", and lets it become the new listener to the components.
    *
    * @param t the tree that will be the new listener for the components
    */
    public void addListener(TreeTemplate t)
    {
        tree = t;

        inputBox.addKeyListener(tree);
        addButton.addActionListener(tree);
        changeButton.addActionListener(tree);
    }




    /** Clears the text in the textField.
    */
    public void clearInput(){inputBox.setText(""); inputBox.requestFocus();}




    /** Returns the text in the textField.
    */
    public String getInput(){return inputBox.getText();}




    /** Gives the textField focus so that user can enter input.
    */
    public void readyForInput(){inputBox.requestFocus();}




    /** Removes the components listeners
    */
    public void removeListener()
    {
        inputBox.removeKeyListener(tree);
        addButton.removeActionListener(tree);
        changeButton.removeActionListener(tree);
    }




    /** Sets the label of the rightmost button to "Change".
    */
    public void setChangeButton(){changeButton.setLabel(BinaryTreesome.inputType[1]);}




    /** Sets the string received as the text in the textField.
    *
    * @param s the string to be set in the textField.
    */
    public void setInput(String s)
    {
        inputBox.setText(s);
        inputBox.setSelectionStart(0);
        inputBox.setSelectionEnd((inputBox.getText()).length());
        inputBox.requestFocus();
    }




    /** Sets the label of the rightmost button to "Random".
    */
    public void setRandomButton(){changeButton.setLabel(BinaryTreesome.inputType[2]);}
}




// -------------------------------- End of class ------------------------------

