import java.awt.*;

public class main
{
    public static void main(String args[]) {
	CustomFrame f = new CustomFrame("Duality", 815, 459);
	BorderLayout layout = new BorderLayout();
	layout.setHgap(8);
	f.setLayout(layout);
	f.setBackground(Color.gray);

	DrawingArea primal = new DrawingArea(400,true);
	f.add(primal, "West");	

	DrawingArea dual = new DrawingArea(400,false);
	f.add(dual, "Center");	

	Controls cntl = new Controls(primal, dual);
	f.add(cntl, "North");

	f.validate();
	f.pack();
	f.setVisible(true);

	primal.initialize(dual);
	dual.initialize(primal);
    }
}
