import java.awt.*;
import java.awt.event.*;

class Controls extends Panel 
    implements ActionListener, ItemListener
{
    DrawingArea primal, dual;
    Button reset;
    Checkbox insertMode, editMode, queryMode, pointMode, segMode,
	triMode, circMode;

    public Controls(DrawingArea p, DrawingArea d)
    {
	((FlowLayout)getLayout()).setHgap(10);
	primal = p;
	dual   = d;

	reset = new Button("reset");
	reset.setActionCommand(reset.getLabel());
	reset.addActionListener(this);
	add(reset);

	insertMode = new Checkbox("Insert Mode", true);
	insertMode.addItemListener(this);
	add(insertMode);

	queryMode = new Checkbox("Query Mode", false);
	queryMode.addItemListener(this);
	add(queryMode);

	editMode = new Checkbox("Edit Mode", false);
	editMode.addItemListener(this);
	add(editMode);

	pointMode = new Checkbox("Point", true);
	pointMode.addItemListener(this);
	add(pointMode);

	segMode = new Checkbox("Segment(Line)", false);
	segMode.addItemListener(this);
	add(segMode);

	triMode = new Checkbox("Triangle", false);
	triMode.addItemListener(this);
	add(triMode);

	circMode = new Checkbox("Circle", false);
	circMode.addItemListener(this);
	add(circMode);

	primal.setMode(DrawingArea.INSERT);
	dual.setMode(DrawingArea.INSERT);
	primal.setMethod(DrawingArea.POINT);
	dual.setMethod(DrawingArea.POINT);
	
	triMode.setEnabled(false);
	circMode.setEnabled(false);
    }
    
    public void actionPerformed(ActionEvent e) 
    {
	if(e.getActionCommand().equals(reset.getLabel())) {
	    resetEverything();
	}
    }

    public synchronized void resetEverything()
    {
	primal.initialize(dual);
	dual.initialize(primal);

	primal.setMode(DrawingArea.INSERT);
	dual.setMode(DrawingArea.INSERT);
	primal.setMethod(DrawingArea.POINT);
	dual.setMethod(DrawingArea.POINT);

	pointMode.setState(true);
	segMode.setState(false);
	triMode.setState(false);
	circMode.setState(false);
	
	pointMode.setEnabled(true);
	segMode.setEnabled(true);
	triMode.setEnabled(false);
	circMode.setEnabled(false);
    }

    public void itemStateChanged(ItemEvent e)
    {
	Object source = e.getItemSelectable();
	boolean selected = e.getStateChange() == ItemEvent.SELECTED;
	if(!selected) {
	    ((Checkbox)source).setState(true); // cannot deselect itself
	    return;
	}
	if(source == insertMode) {
	    primal.setMode(DrawingArea.INSERT);
	    dual.setMode(DrawingArea.INSERT);
	    editMode.setState(false);
	    queryMode.setState(false);

	    primal.setMethod(DrawingArea.POINT);
	    dual.setMethod(DrawingArea.POINT);
	    pointMode.setState(true);
	    segMode.setState(false);
	    triMode.setState(false);
	    circMode.setState(false);

	    pointMode.setEnabled(true);
	    segMode.setEnabled(true);
	    triMode.setEnabled(false);
	    circMode.setEnabled(false);
	}
	else if(source == editMode) {
	    primal.setMode(DrawingArea.EDIT);
	    dual.setMode(DrawingArea.EDIT);
	    insertMode.setState(false);
	    queryMode.setState(false);

	    pointMode.setState(false);
	    segMode.setState(false);
	    triMode.setState(false);
	    circMode.setState(false);

	    pointMode.setEnabled(false);
	    segMode.setEnabled(false);
	    triMode.setEnabled(false);
	    circMode.setEnabled(false);
	} 
	else if(source == queryMode) {
	    primal.setMode(DrawingArea.QUERY);
	    dual.setMode(DrawingArea.QUERY);
	    insertMode.setState(false);
	    editMode.setState(false);

	    primal.setMethod(DrawingArea.SEG);
	    dual.setMethod(DrawingArea.SEG);
	    pointMode.setState(false);
	    segMode.setState(true);
	    triMode.setState(false);
	    circMode.setState(false);

	    pointMode.setEnabled(false);
	    segMode.setEnabled(true);
	    triMode.setEnabled(true);
	    circMode.setEnabled(true);
	}
	else if(source == pointMode) {
	    primal.setMethod(DrawingArea.POINT);
	    dual.setMethod(DrawingArea.POINT);
	    segMode.setState(false);
	    triMode.setState(false);
	    circMode.setState(false);
	}
	else if(source == segMode) {
	    primal.setMethod(DrawingArea.SEG);
	    dual.setMethod(DrawingArea.SEG);
	    pointMode.setState(false);
	    triMode.setState(false);
	    circMode.setState(false);
	}
	else if(source == triMode) {
	    primal.setMethod(DrawingArea.TRIANG);
	    dual.setMethod(DrawingArea.TRIANG);
	    pointMode.setState(false);
	    segMode.setState(false);
	    circMode.setState(false);
	}
	else if(source == circMode) {
	    primal.setMethod(DrawingArea.CIRCLE);
	    dual.setMethod(DrawingArea.CIRCLE);
	    pointMode.setState(false);
	    segMode.setState(false);
	    triMode.setState(false);
	}
    }
}

