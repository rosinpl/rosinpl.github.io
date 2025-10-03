/*

Linked.java   --  A Gentle Introduction to Linked Lists
Copyright (C) 2002, Michael H. Goldwasser

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

*/

import java.applet.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;
import javax.swing.event.*;

public class Linked extends JApplet implements ActionListener,DocumentListener {
    private int numCellsDefault = 16;
    private int numCellsVal = numCellsDefault;
    private JTextField numCells = new JTextField(3);

    private int startIndexDefault = 14;
    private int startIndexVal = startIndexDefault;
    private JTextField startIndex = new JTextField(3);

    private JTextField headIndex = new JTextField(2);

    private JPanel sp = null;
    private JTextField cells[];

    private ImagePanel canvas = null;
    private MyContents contents;

    private Random ran = new Random();

    public void init() {
	getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	getContentPane().add(Box.createRigidArea(new Dimension(0,10)));
	JLabel l = new JLabel("A Gentle Introduction to Linked Lists");
	l.setAlignmentX(Component.CENTER_ALIGNMENT);
	getContentPane().add(l);
	l = new JLabel("Michael Goldwasser");
	l.setAlignmentX(Component.CENTER_ALIGNMENT);
	getContentPane().add(l);
	getContentPane().add(Box.createRigidArea(new Dimension(0,10)));

	JPanel temp = createTopPanel();
	getContentPane().add(temp);

	sp = new myPanel();
	sp.setBorder(BorderFactory.createLoweredBevelBorder());
	getContentPane().add(sp);
	resetMemory();

	contents = new MyContents();
	canvas = new ImagePanel(contents);
	canvas.setPreferredSize(new Dimension(getWidth(),getHeight()));
	canvas.setBorder(BorderFactory.createLoweredBevelBorder());
	getContentPane().add(canvas);

	getContentPane().add(Box.createRigidArea(new Dimension(0,10)));

	repaint();
    }

    public void insertUpdate(DocumentEvent e) {
	handleUpdate(e);
    }

    public void removeUpdate(DocumentEvent e) {
	handleUpdate(e);
    }

    public void changedUpdate(DocumentEvent e) {
	// should never happen
    }

    private void handleUpdate(DocumentEvent e) {
	if (contents.active) {
	    String name = (String) e.getDocument().getProperty("name");
	    if (name.equals("head")) {
		headIndex.setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.red));
	    } else {
		int i=-1;
		try {
		    i = Integer.parseInt(name);
		} catch (NumberFormatException ex) {
		}
		cells[i].setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.red));
	    }
	}
    }


    public void actionPerformed(ActionEvent e) {
	if (e.getActionCommand().equals("restart")) {
	    contents.active = false;
	    resetMemory();
	    validate();
	    repaint();

	} else if (e.getActionCommand().equals("draw")) {
	    prepareDrawing();
	    resetMemoryImage();
	    repaint();
	} else if (e.getActionCommand().equals("random")) {
	    setRandom();
	    //	    prepareDrawing();
	    //	    resetMemoryImage();
	    repaint();
	}

    }


    protected JPanel createTopPanel() {
	JPanel panel = new JPanel();
	panel.setBorder(BorderFactory.createLoweredBevelBorder());
	JButton restartB = new JButton("Reset Memory");
	restartB.addActionListener(this);
	restartB.setActionCommand("restart");
	panel.add(restartB);
	panel.add(new JLabel("Number of Memory Cells:"));
	numCells.setText(String.valueOf(numCellsDefault));
	numCells.setHorizontalAlignment(JTextField.CENTER);
	numCells.setBorder(BorderFactory.createLoweredBevelBorder());
	panel.add(numCells);
	panel.add(new JLabel("Starting Index:"));
	startIndex.setText(String.valueOf(startIndexDefault));
	startIndex.setHorizontalAlignment(JTextField.CENTER);
	startIndex.setBorder(BorderFactory.createLoweredBevelBorder());
	panel.add(startIndex);

	return(panel);
    }


    protected void resetMemoryImage() {
	for (int i=0; i<numCellsVal; i++) {
	    cells[i].setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.black));
	}
	headIndex.setBorder(BorderFactory.createMatteBorder(2,2,2,2,Color.black));
    }




    protected void resetMemory() {

	try {
	    numCellsVal = Integer.parseInt(numCells.getText());
	} catch (NumberFormatException ex) {
	    numCellsVal = numCellsDefault;
	    numCells.setText(String.valueOf(numCellsVal));
	}
	if (numCellsVal<2) {
	    numCellsVal = numCellsDefault;
	    numCells.setText(String.valueOf(numCellsVal));
	}

	try {
	    startIndexVal = Integer.parseInt(startIndex.getText());
	} catch (NumberFormatException ex) {
	    startIndexVal = startIndexDefault;
	    startIndex.setText(String.valueOf(startIndexVal));
	}
	if (startIndexVal<0) {
	    startIndexVal = startIndexDefault;
	    startIndex.setText(String.valueOf(startIndexVal));
	}


	sp.removeAll();
	GridBagLayout lay = new GridBagLayout();
	GridBagConstraints con;
	JLabel l;
	sp.setLayout(lay);

	con = new GridBagConstraints();
	con.gridx=0; con.gridy=0;
	Component rigid = Box.createRigidArea(new Dimension(0,25));
	lay.setConstraints(rigid,con);
	sp.add(rigid);

	con = new GridBagConstraints();
	con.gridx=0; con.gridy=1;
	con.ipadx=10;
	con.weighty=1;
	con.anchor=GridBagConstraints.SOUTH;
	l = new JLabel("Memory Cell");
	lay.setConstraints(l,con);
	sp.add(l);
	con = new GridBagConstraints();
	con.gridx=0; con.gridy=2;
	l = new JLabel("Contents");
	lay.setConstraints(l,con);
	sp.add(l);

	int digits = decimalDigits(startIndexVal+numCellsVal-1);
 
	String num;

	cells = new JTextField[numCellsVal];
	for (int i=0; i<numCellsVal; i++) {
	    con = new GridBagConstraints();
	    con.gridx=1+i; con.gridy=1;
	    con.anchor=GridBagConstraints.SOUTH;
	    num = String.valueOf(startIndexVal+i);
	    if (decimalDigits(startIndexVal+i) < digits) {
		num = "0"+num;
	    }
	    l = new JLabel(num);
	    lay.setConstraints(l,con);
	    sp.add(l);

	    cells[i] = new JTextField(digits);
	    cells[i].setHorizontalAlignment(JTextField.CENTER);
	    cells[i].getDocument().putProperty("name",String.valueOf(i));
	    cells[i].getDocument().addDocumentListener(this);
	    con = new GridBagConstraints();
	    con.anchor=GridBagConstraints.CENTER;
	    con.gridx=1+i; con.gridy=2;
	    lay.setConstraints(cells[i],con);
	    sp.add(cells[i]);
	}


	con = new GridBagConstraints();
	con.gridx=0; con.gridy=4;
	rigid = Box.createRigidArea(new Dimension(0,25));
	lay.setConstraints(rigid,con);
	sp.add(rigid);


	con = new GridBagConstraints();
	con.gridx=0; con.gridy=5;
	con.weighty=1;
	l = new JLabel("Head Index:");
	lay.setConstraints(l,con);
	sp.add(l);

	con = new GridBagConstraints();
	con.gridx=1; con.gridy=5;
	con.weighty=1;
	headIndex = new JTextField(digits);
	lay.setConstraints(headIndex,con);
	headIndex.setText("");
	headIndex.setHorizontalAlignment(JTextField.CENTER);
	headIndex.getDocument().putProperty("name","head");
	headIndex.getDocument().addDocumentListener(this);
	sp.add(headIndex);

	con = new GridBagConstraints();
	con.gridx=0; con.gridy=6;
	rigid = Box.createRigidArea(new Dimension(0,25));
	lay.setConstraints(rigid,con);
	sp.add(rigid);

	JButton draw = new JButton("Update Figure");
	draw.addActionListener(this);
	draw.setActionCommand("draw");
	con = new GridBagConstraints();
	con.gridx=0; con.gridy=7;
	con.gridwidth=GridBagConstraints.RELATIVE;
	con.weighty=10;
	con.anchor=GridBagConstraints.NORTHWEST;
	lay.setConstraints(draw,con);
	sp.add(draw);

	JButton random = new JButton("Random List");
	random.addActionListener(this);
	random.setActionCommand("random");
	con = new GridBagConstraints();
	con.gridx=0; con.gridy=7;
	con.gridwidth=GridBagConstraints.REMAINDER;
	con.weighty=10;
	con.anchor=GridBagConstraints.NORTHEAST;
	lay.setConstraints(random,con);
	sp.add(random);

	con = new GridBagConstraints();
	con.gridx=0; con.gridy=8;
	rigid = Box.createRigidArea(new Dimension(0,25));
	lay.setConstraints(rigid,con);
	sp.add(rigid);

	resetMemoryImage();
    }


    private int decimalDigits(int n) {
	int count=1;
	for (int i=n; i>=10; i/=10, count++);
	return(count);
    }

    protected void setRandom() {
	int[] order = new int[numCellsVal/2];

	for (int i=0; i<numCellsVal/2; i++) order[i]=i;
	for (int i=numCellsVal/2-1; i>0; i--) {
	    int r = ran.nextInt(i+1);
	    int temp = order[i];
	    order[i]=order[r];
	    order[r]=temp;
	}


	for (int i=0; i<cells.length; i++) {
	    cells[i].setText("");
	}
	headIndex.setText(String.valueOf(2*order[0]+startIndexVal));
	for (int i=0; i<numCellsVal/2-2; i++) {
	    cells[2*order[i]].setText(String.valueOf((char) (((int) 'A')+ran.nextInt(26))));
	}
	for (int i=0; i<numCellsVal/2-3; i++) {
	    cells[2*order[i]+1].setText(String.valueOf(2*order[i+1]+startIndexVal));
	}
    }


    protected void prepareDrawing() {
	try {
	    contents.head = Integer.parseInt(headIndex.getText());
	} catch (NumberFormatException ex) {
	    contents.head = -1;
	}

	contents.entry = new String[numCellsVal];
	for (int i=0; i<numCellsVal; i++) {
	    contents.entry[i] = cells[i].getText();
	}
	contents.num = numCellsVal;
	contents.start = startIndexVal;
	contents.active = true;

	contents.data = new String[(contents.num+1)/2];
	contents.index = new int[(contents.num/2)];
	contents.count = 0;

	// build copy of list
	int walk;

	int[] visited = new int[contents.num];
	for (int i=0; i<contents.num; i++) { visited[i]=-1;}

	for (contents.count=0,walk=contents.head;
	     (walk>=contents.start &&
	      walk<(contents.start+contents.num) &&
	      visited[walk-contents.start]==-1);
	     contents.count++) {

	    contents.index[contents.count] = walk;
	    contents.data[contents.count] = contents.entry[walk-contents.start];
	    visited[walk-contents.start]=contents.count;

	    if ((walk+1)<(contents.start+contents.num)) {
		try {
		    walk = Integer.parseInt(contents.entry[walk+1-contents.start]);
		} catch (NumberFormatException ex) {
		    walk = -1;
		}
	    } else {
		walk = -1;
	    }
	}

	contents.cycle = -1;
	if (walk>=contents.start &&
	    walk<(contents.start+contents.num) &&
	    visited[walk-contents.start]!=-1) {
	    contents.cycle = visited[walk-contents.start];
	}


	canvas.resizeDrawing();
    }


}


class MyContents {
    public boolean active = false;
    public int num;
    public int start;
    public int head;
    public String entry[];
    public float fontsize;

    public String data[];
    public int index[];
    public int cycle=-1;
    public int count=0;
    public int w;
    public int h;

    public int XupArrow[] = {0,0,0,0};
    public int YupArrow[] = {0,0,0,0};
    public int XrightArrow[] = {0,0,0,0};
    public int YrightArrow[] = {0,0,0,0};
}


class ImagePanel extends JPanel implements ComponentListener {
    private MyContents contents;

    public ImagePanel(MyContents c) {
	contents = c;
	addComponentListener(this);
    }

    public void resizeDrawing() {
	if (contents.active && contents.count>0) {
	    Graphics g = getGraphics();
	    Font f = g.getFont();
	    FontMetrics fm;
	    Insets insets = getInsets();
	    int currentWidth = getWidth() - insets.left - insets.right;
	    int currentHeight = getHeight() - insets.top - insets.bottom;

	    int targetW = (currentWidth /(2*(2*contents.count+1)));
	    int targetH = (currentHeight*2/5);

	    fm = g.getFontMetrics();
	    int curW = fm.stringWidth("X");
	    int curH = fm.getHeight();

	    float factorW = ((float) targetW)/((float) curW);
	    float factorH = ((float) targetH)/((float) curH);
	    float factor = (factorW>factorH) ? factorH : factorW;

	    contents.fontsize = f.getSize2D()*factor;
	    g.setFont(f.deriveFont(contents.fontsize));

	    contents.w = g.getFontMetrics().stringWidth("X");
	    contents.h = g.getFontMetrics().getHeight();

	    // update arrow templates
	    contents.XrightArrow[0] = contents.w*11/2;      // w*6 - w/2
	    contents.XrightArrow[1] = contents.w*84/15;     // w*6 - w/2 + w/10
	    contents.XrightArrow[2] = contents.w*11/2;
	    contents.XrightArrow[3] = contents.w*6;

	    contents.YrightArrow[0] = getHeight()/2-contents.w/4;
	    contents.YrightArrow[1] = getHeight()/2;
	    contents.YrightArrow[2] = getHeight()/2+contents.w/4;
	    contents.YrightArrow[3] = getHeight()/2;

	    contents.XupArrow[0] = contents.w*11/4;
	    contents.XupArrow[1] = contents.w*3;
	    contents.XupArrow[2] = contents.w*13/4;
	    contents.XupArrow[3] = contents.w*3;

	    contents.YupArrow[0] = getHeight()/2+contents.h*5/8+contents.w/2;
	    contents.YupArrow[1] = getHeight()/2+contents.h*5/8+contents.w*2/5;
	    contents.YupArrow[2] = getHeight()/2+contents.h*5/8+contents.w/2;
	    contents.YupArrow[3] = getHeight()/2+contents.h*5/8;

	}
    }



    public void paintComponent(Graphics g) {
	super.paintComponent(g);  // paint background

	int center = getHeight()/2;

	if (contents.active) {
	    if (contents.count==0) {
		g.setFont(g.getFont().deriveFont(24.0f));
		String s = "invalid Head";
		g.drawString(s,getWidth()/2-g.getFontMetrics().stringWidth(s)/2,
			     getHeight()/2+g.getFontMetrics().getHeight()/2);
	    } else {
		// process the list
		int temp = 0;

		// draw the boxes
		g.setColor(Color.black);
		for (int i=0; i<contents.count; i++) {
		    g.fillRect(contents.w*(2*(2*i+1)+1)-contents.w,
			       center-5*contents.h/8,
			       2*contents.w,
			       5*contents.h/4);
		}	
		g.setColor(getBackground());
		for (int i=0; i<contents.count; i++) {
		    g.fillRect(contents.w*(2*(2*i+1)+1)-contents.w+3,
			       center-5*contents.h/8+3,
			       2*contents.w-6,
			       5*contents.h/4-6);
		}	


		// draw the indicies
		g.setFont(g.getFont().deriveFont(contents.fontsize/2.0f));
		g.setColor(Color.black);
		for (int i=0; i<contents.count; i++) {
		    String s = String.valueOf(contents.index[i]);
		    temp = g.getFontMetrics().stringWidth(s);
		    g.drawString(s,contents.w*(2*(2*i+1)+1)-temp/2,
				 center - 7*contents.h/8);
		}


		// draw the data
		g.setFont(g.getFont().deriveFont(contents.fontsize));
		Rectangle2D rect;
		for (int i=0; i<contents.count; i++) {
		    rect = g.getFontMetrics().getStringBounds(contents.data[i],g);
		    g.drawString(contents.data[i],
				 contents.w*(2*(2*i+1)+1)-((int) (rect.getWidth()/2.0)),
				 center +3*contents.h/8);
		}	


		// draw the arrows
		int[] XA = new int[4];
		int[] YA = new int[4];
		for (int i=0; i<4; i++)  XA[i]=contents.XrightArrow[i];
		for (int i=0; i<4; i++)  YA[i]=contents.YrightArrow[i];

		for (int i=1; i<contents.count; i++) {
		    g.fillRect(contents.w*4*i,
			       center-1,
			       contents.w*24/15,
			       3);

		    g.fillPolygon(XA,YA,4);
		    for (int j=0; j<4; j++) XA[j]+=4*contents.w;
		}	


		if (contents.cycle!=-1) {
		    // segment 1
		    g.fillRect(contents.w*4*contents.count,
			       center-1,
			       contents.w,
			       3);

		    // segment 2
		    g.fillRect(contents.w*(4*contents.count+1)-1,
			       center-1,
			       3,
			       5*contents.h/4+3);

		    // segment 3
		    g.fillRect(contents.w*(4*(contents.cycle+1)-1),
			       center+5*contents.h/4-1,
			       contents.w*(4*(contents.count-contents.cycle-1)+2),
			       3);

		    // segment 4
		    g.fillRect(contents.w*(4*(contents.cycle+1)-1)-1,
			       center+5*contents.h/8+contents.w*2/5,
			       3,
			       5*contents.h/8-contents.w*2/5+1);

		    for (int j=0; j<4; j++)  XA[j]=contents.XupArrow[j]+
						 contents.w*4*contents.cycle;
		    for (int j=0; j<4; j++)  YA[j]=contents.YupArrow[j];
		    g.fillPolygon(XA,YA,4);

		}
	    }
	}
    }

    public void componentHidden(ComponentEvent e) {
    }
    public void componentMoved(ComponentEvent e) {
    }
    public void componentShown(ComponentEvent e) {
    }
    public void componentResized(ComponentEvent e) {
	resizeDrawing();
    }

}



class myPanel extends JPanel implements ComponentListener {

    private float lastwidth = (float) 0.0f;
    private Font base;

    public myPanel() {
	addComponentListener(this);
	base = getFont();
    }

    public void componentHidden(ComponentEvent e) {
    }
    public void componentMoved(ComponentEvent e) {
    }
    public void componentShown(ComponentEvent e) {
    }
    public void componentResized(ComponentEvent e) {
	if (getWidth() != lastwidth) {
	    lastwidth = getWidth();
	    doResize();
	}
    }


    public void doResize() {
	Font newf = base.deriveFont(0.7f*base.getSize2D()*((float) getWidth())/
				    ((float) getLayout().minimumLayoutSize(this).getWidth()));
	setFont(newf);

	for (int i=getComponentCount()-1; i>=0; i--) {
	    getComponent(i).setFont(newf);
	}

    }


}
