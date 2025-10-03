import java.applet.*;
import java.awt.*;

public class dualapplet extends Applet
{
    Controls cntl;

    public void init()
    {
	BorderLayout layout = new BorderLayout();
	layout.setHgap(8);
	setLayout(layout);
	setBackground(Color.gray);

	DrawingArea primal = new DrawingArea(400,true);
	add(primal, "West");	

	DrawingArea dual = new DrawingArea(400,false);
	add(dual, "Center");	

	Controls cntl = new Controls(primal, dual);
	add(cntl, "North");

	validate();
	setVisible(true);

	primal.initialize(dual);
	dual.initialize(primal);
    }

    public void destroy() 
    {
	System.out.println("destroy");
	super.destroy();
    }
    
    public void start()
    {
	System.out.println("start");
	super.start();
	if(cntl != null)
	    cntl.resetEverything();
    }


    public void stop()
    {
	System.out.println("stop");
	super.stop();
	if(cntl != null)
	    cntl.resetEverything();
    }
    
}
