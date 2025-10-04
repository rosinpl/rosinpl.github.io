// ---------------------------------- Imports ---------------------------------




import java.awt.*;




// ------------------------------- Start of class -----------------------------




/**
* A class representing the panel containing the menu.
* This panel coantains a number of buttonsand three choiceBoxes.
*
* @version        May 1998
* @author         Bjørn E. Gustafson & Jørgen Kjensli
*/
public class MenuPanel extends Panel
{
    // ----- Variables ----

    /** The layout of this panel.
    */
    private GridBagLayout gridbag           = new GridBagLayout();

    /** Describes each grid in the layout.
    */
    private GridBagConstraints constraints  = new GridBagConstraints();

    /** The buttons in the menu.
    */
    private Button button[]                 = new Button[7];

    /** The pulldown-menus in the menu.
    */
    private Choice choice[]                 = new Choice[3];

    /** The applet class that uses this menu.
    */
    private BinaryTreesome mainClass        = null;




    // ---------------------------- Constructor -------------------------------




    /** Constructs the panel and initializes the components.
    * The constructor calls other functions in the class to
    * set up the components.
    *
    * @param t the main applet class which will act as listener for the components
    */
    public MenuPanel(BinaryTreesome t)
    {
        mainClass   = t;                        // Remember the applet-class
        setLayout(gridbag);                     // Set layout

        int l = 10;                             // Default distance to the left...
        int r = 5;                              // and to the right of each component
        int space = 13;                         // Space between each section in the menu

        // Each component last in row (i.e. always only one component per row)
        constraints.gridwidth = GridBagConstraints.REMAINDER;

        // ------- select a tree ------------
        // Add a label and a pulldownmenu to panel
        constraints.fill = GridBagConstraints.HORIZONTAL;   // Drag comp. out horizontally
        constraints.insets  = new Insets(0,l,0,r);          // No extra padding between label and pull-menu
        constraints.weighty = 0.0;                          // Vertical grid-room =  component-sizes
        makeSectionLabel("Tree:");                          // Add label
        makeChoice(0, BinaryTreesome.treeType);             // Add pulldown-menu

        // ------- select node-operation ------------
        // Add a new label and some buttons to panel
        constraints.fill = GridBagConstraints.HORIZONTAL;   // Drag comp. out horizontally
        constraints.insets  = new Insets(space,l,0,r);      // Leave some room to stuff over label
        constraints.weighty = 0.0;                          // Vertical grid-room =  component-sizes
        makeSectionLabel("Operation:");                     // Add the label
        constraints.fill = GridBagConstraints.BOTH;         // Fill extra hor. & vert. space in grid
        constraints.insets  = new Insets(0,l,0,r);          // No extra padding between buttons
        constraints.weighty = 0.0;                          // Don't clump together in center
        makeButton(0);                                      // Add button
        makeButton(1);                                      // Add button
        makeButton(2);                                      // Add button

        // ------- select iterative operation ------------
        // Add a label and a pulldownmenu to panel
        constraints.fill = GridBagConstraints.HORIZONTAL;   // Drag comp. out horizontally
        constraints.insets  = new Insets(space,l,0,r);      // No extra padding between label and pull-menu
        constraints.weighty = 0.0;                          // Vertical grid-room =  component-sizes
        makeSectionLabel("Iteration:");                     // Add label
        constraints.insets  = new Insets(0,l,0,r);          // No extra padding between label and pull-menu
        makeChoice(1, BinaryTreesome.iteration);            // Add pulldown-menu
        makeButton(3);                                      // Add button

        // ------- select view ------------
        // Add a label and a pulldownmenu to panel
        constraints.fill = GridBagConstraints.HORIZONTAL;   // Drag comp. out horizontally
        constraints.insets  = new Insets(space,l,0,r);      // Leave some room to stuff above
        constraints.weighty = 0.0;                          // Vertical grid-room =  component-sizes
        makeSectionLabel("Algorithms:");                    // Add label
        constraints.insets  = new Insets(0,l,0,r);          // No extra padding between label and pull-menu
        makeChoice(2, BinaryTreesome.algorithm);            // Add pulldown-menu
        makeButton(4);                                      // Add button

        constraints.fill = GridBagConstraints.HORIZONTAL;   // Drag comp. out horizontally
        constraints.insets  = new Insets(space,l,0,r);      // Leave some room to stuff over label
        constraints.weighty = 0.0;                          // Vertical grid-room =  component-sizes
        makeSectionLabel("Help:");                          // Add the label
        constraints.fill = GridBagConstraints.BOTH;         // Fill extra hor. & vert. space in grid
        constraints.insets  = new Insets(0,l,0,r);          // No extra padding between buttons
        constraints.weighty = 0.0;                          // Don't clump together in center
        makeButton(5);                                      // Add button
        makeButton(6);                                      // Add button
    }




    // -------------------------- Other functions -----------------------------




    /** Returns the name of the button in the menu that contains
    * the coordinates sent to the function.
    *
    * @param x the x coordinate of the point where the mouse was clicked
    * @param y the y coordinate of the point where the mouse was clicked
    */
    public String findOutWhichButtonWasClicked(int x, int y)
    {
        for (int i=0; i<(BinaryTreesome.menuItem).length; i++)
        {
            if (button[i].contains(x,y))
                return BinaryTreesome.menuItem[i];
        }

        return null;
    }




    /** Creates a button with an actionlistener and adds it to the menu.
    * The label of the button will be the string in BinaryTreesome.menuItem[index].
    *
    * @param index the index of the new button in the array button
    * @see MenuPanel#button
    * @see BinaryTreesome#menuItem
    */
    public void makeButton(int index)
    {
        button[index] = new Button(BinaryTreesome.menuItem[index]);
        button[index].addActionListener(mainClass);
        button[index].setBackground(Color.lightGray);
        button[index].setForeground(Color.black);
        gridbag.setConstraints(button[index], constraints);
        add(button[index]);
    }




    /** Creates a pulldown menu with an itemlistener and adds it to the menu.
    * The choices are set to be the elements of the array of strings sent to the function.
    *
    * @param index the index of the new pulldown menu in the array choice
    * @param names the strings that will be the choices in the pulldown menu
    * @see MenuPanel#choice
    */
    public void makeChoice(int index, String[] names)
    {
        choice[index] = new Choice();
        choice[index].addItemListener(mainClass);
        choice[index].setBackground(Color.lightGray);
        choice[index].setForeground(Color.black);
        for(int i=0; i<names.length; i++)
            choice[index].add(names[i]);
        gridbag.setConstraints(choice[index], constraints);
        add(choice[index]);
    }




    /** Creates a label.
    * The label is drawn in it's own canvas with a certain color,
    * and it's this canvas that is added to the menu.
    *
    * @param name the string that will the text on the label
    */
    public void makeSectionLabel(String name)
    {
        SectionLabel label = new SectionLabel(name);
        gridbag.setConstraints(label, constraints);
        add(label);
    }




    /** Sets the current operation as the selected algorithm in the pulldown-menu.
    *
    * @param s the string that should be selected in the pulldown menu
    */
    public void setAlgorithm(String s){ choice[2].select(s);}




    /** Sets the current algorithm as the selected algorithm in the pulldown-menu.
    *
    * @param s the string that should be selected in the pulldown menu
    */
    public void setTree(String s){ choice[0].select(s);}
}




// -------------------------------- End of class ------------------------------

