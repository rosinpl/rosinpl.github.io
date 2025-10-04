/**@author Chris Bobo -=BOBO GAMES=-
 * copyright 1999 all rights reserved -=BOBO GAMES=-
 * this is an applet that demonstrates image Component Labeling
 * @version 1.0
 */
import java.io.*;
import java.applet.Applet;
import java.applet.*;
import java.awt.*;
import java.awt.Toolkit;
import java.awt.event.*;
import java.*;

class Pixel {
	int state,
		label,
		x,
		y;
	boolean marked,
			labeled;
	
	Pixel(int row, int col, int label, int pixelSize, int imageX, int imageY) {
		this.x = col * pixelSize + imageX;
		this.y = row * pixelSize + imageY;
		this.state = 0;
		this.label = label;
		this.marked = false;
		this.labeled = false;
	}
	
	public void clearPixel() {
		if(this.marked) this.label = 1;
		else this.label = 0;
		this.state = 0;
		this.marked = false;
		this.labeled = false;
	}
	
	public void clearLabeling() {
		if(this.marked) this.label = 1;
		else this.label = 0;
		this.state = 0;
		this.labeled = false;
	}
	
	public void mark(boolean markPixel) {
		if(markPixel) {
			this.label = 1;
			this.marked = true;
		}
		else {
			this.label = 0;
			this.marked = false;
		}
	}
	
	public void draw(Applet a, Graphics g, Image pixelColors[]) {
		// draw pixel
		g.drawImage(pixelColors[state], this.x, this.y, a);
		// draw the pixel's label
		int strX = this.x + 8;
		int strY = this.y + 15;
		if(this.labeled) {
			// make adjustments for size of number being drawn
			if(this.label > 9) strX -= 4;
			if(this.label > 99) strX -= 3;
			g.drawString("" + this.label, strX, strY);
		}
		else if(this.marked) {
			g.drawString("*", strX - 1, strY + 3);
		}
	}
}// end class Pixel

public class ImageComponentLabeling extends Applet implements Runnable {
	// critical values for GUI and animation
	final int // button setup data
			  _buttonX			= 20,
			  _buttonY			= 70,
			  _wideButton		= 100,
			  _buttonHeight		= 23,
			  _buttonHSpacer	= _buttonHeight + 8,
			  _buttonWSpacer	= _wideButton + 10,
			  // display 
			  _displayX			= 245,
			  _displayY			= 15,
			  _displayWidth		= 340,
			  _displayHeight	= 340,
			  // messageCenter setup data
			  _messageWidth		= _displayWidth,
			  _messageHeight	= 80,
			  _messageX			= _displayX,
			  _messageY			= _displayY + _displayHeight + 10,
			  // speedBar setup data
			  _speedBarX		= _buttonX,
			  _speedBarY		= _messageY + _messageHeight - 20,
			  _speedBarWidth	= _wideButton * 2,
			  _speedBarHeight	= 13,
			  // pixel data
			  _pixelSize		= 20,
			  _imageSize		= 15,
			  _imageX			= _displayX,
			  _imageY			= _displayY,
			  // positions for program name
			  _programNameX		= 10,
			  _programNameY		= 10,
			  // active_button state
			  _none				= 100,
			  _markingPixels	= 101,
			  // pixel image states
			  _normal			= 0,
			  _scanning			= 1,
			  _scanningNbrs		= 2,
			  _addToQ			= 3,
			  _deleteFromQ		= 4,
			  // legend constants
			  _legendX			= _buttonX + 5,
			  _legendY			= 280,
			  _legendYSpacer	= _pixelSize + 5,
			  _legendXSpacer	= _pixelSize + 5,
			  _legendTextYAdjust= 14,
			  _numOfPixelColors	= 5,
			  // labeling states
			  _scanForMarkedPixel	= 11,
			  _incrementLoops		= 12,
			  _markedPixelFound		= 13,
			  _scanForMarkedNbrs	= 14,
			  _foundMarkedNbr		= 15,
			  _removePixelFromQ		= 16,
			  _doneLabelingImage	= 17;
		

	// stuff to create doublebuffer
	Image buffer;
	Graphics bufferGraphics;
	Dimension bufferSize;
		
	Font f = new Font("TimesRoman", Font.PLAIN, 12);
		
	boolean pause = false;
	boolean markPixel = false;
	boolean labelingImage = false;
	boolean imageLabeled = false;
	Button markPixelsButton, labelImageButton, clearLabelingButton,
		   clearImageButton, pauseButton, continueButton, preMarkedImageButton;
	Scrollbar speedBar;
	TextArea messageCenter = new TextArea();
	Choice preMarkedImageChoice = new Choice();
	int frameRate = 10;

	// active_button is used to determine which button is "Down" or activated.
	int active_button = 0;
	
	// values used during the labeling process
	// these values cannot be contained inside the 
	// labeling method due to the animation process
	// which must exit the labeling maethod so that 
	// the screen can be repainted after each animation step
	final int NumOfNbrs = 4;	// neighbors of a pixel
	int row = 0,
		col = 0,
		id = 0,
		nbrLoopCnt = 0,
		labelingState = 0;
	LinkedQueue q = new LinkedQueue(20);
	Position here = new Position(0, 0);
	Position nbr = new Position(0, 0);
	Position[] offset = new Position[4];
	
	Thread animate;
	Graphics graphics;
	Toolkit toolkit;
	
	Dimension appletSize;
	
	Image boboGames, programName;
	Image pixelColors[] = new Image[_numOfPixelColors];
	String pixelLegend[] = new String[_numOfPixelColors];

	Pixel[][] pixel;
	

	
	// premade boards
	static char checkerImage[][] =		{ {'#','O','#','O','#','O','#','O','#','O','#','O','#','O','#'},
										  {'O','#','O','#','O','#','O','#','O','#','O','#','O','#','O'},
										  {'#','O','#','O','#','O','#','O','#','O','#','O','#','O','#'},
										  {'O','#','O','#','O','#','O','#','O','#','O','#','O','#','O'},
										  {'#','O','#','O','#','O','#','O','#','O','#','O','#','O','#'},
										  {'O','#','O','#','O','#','O','#','O','#','O','#','O','#','O'},
										  {'#','O','#','O','#','O','#','O','#','O','#','O','#','O','#'},
										  {'O','#','O','#','O','#','O','#','O','#','O','#','O','#','O'},
										  {'#','O','#','O','#','O','#','O','#','O','#','O','#','O','#'},
										  {'O','#','O','#','O','#','O','#','O','#','O','#','O','#','O'},
										  {'#','O','#','O','#','O','#','O','#','O','#','O','#','O','#'},
										  {'O','#','O','#','O','#','O','#','O','#','O','#','O','#','O'},
										  {'#','O','#','O','#','O','#','O','#','O','#','O','#','O','#'},
										  {'O','#','O','#','O','#','O','#','O','#','O','#','O','#','O'},
										  {'#','O','#','O','#','O','#','O','#','O','#','O','#','O','#'} };

	static char houseImage[][] =		{ {'O','O','O','O','O','O','O','O','O','O','O','O','O','O','O'},
										  {'O','O','O','O','O','O','O','O','O','O','O','O','O','O','O'},
										  {'O','O','O','O','O','O','O','#','O','O','O','O','O','O','O'},
										  {'O','O','O','O','O','O','#','#','#','O','O','O','O','O','O'},
										  {'O','O','O','O','O','#','#','#','#','#','O','O','O','O','O'},
										  {'O','O','O','O','#','#','#','#','#','#','#','O','O','O','O'},
										  {'O','O','O','#','#','O','O','O','O','O','#','#','O','O','O'},
										  {'O','O','O','O','#','O','O','O','O','O','#','O','O','O','O'},
										  {'O','O','O','O','#','O','O','O','O','O','#','O','O','O','O'},
										  {'O','O','O','O','#','O','#','#','#','O','#','O','O','O','O'},
										  {'O','O','O','O','#','O','#','#','#','O','#','O','O','O','O'},
										  {'O','O','O','O','#','O','#','#','#','O','#','O','O','O','O'},
										  {'O','O','O','O','#','O','#','#','#','O','#','O','O','O','O'},
										  {'O','O','O','O','#','O','#','#','#','O','#','O','O','O','O'},
										  {'O','O','O','O','#','O','#','#','#','O','#','O','O','O','O'} };

	static char ufImage[][] =			{ {'O','O','O','O','O','O','O','O','O','O','O','O','O','O','O'},
										  {'O','O','#','#','O','O','#','#','O','#','#','#','#','O','O'},
										  {'O','O','#','#','O','O','#','#','O','#','O','#','#','O','O'},
										  {'O','O','O','#','O','O','#','O','O','#','O','O','O','O','O'},
										  {'O','O','O','#','O','O','#','O','O','#','O','O','O','O','O'},
										  {'O','O','O','#','O','O','#','O','O','#','O','O','O','O','O'},
										  {'#','#','O','#','O','O','#','O','O','#','#','#','O','#','#'},
										  {'#','#','O','#','O','O','#','O','O','#','O','O','O','#','#'},
										  {'O','O','O','#','O','O','#','O','O','#','O','O','O','O','O'},
										  {'O','O','O','#','O','O','#','O','O','#','O','O','O','O','O'},
										  {'O','O','O','#','O','O','#','O','O','#','O','O','O','O','O'},
										  {'O','O','O','#','O','O','#','O','O','#','O','O','O','O','O'},
										  {'O','O','O','#','O','O','#','O','#','#','#','O','O','O','O'},
										  {'O','O','O','O','#','#','O','O','#','#','#','O','O','O','O'},
										  {'O','O','O','O','O','O','O','O','O','O','O','O','O','O','O'} };
	
// I N I T A I L I Z E   A P P L E T///////////////////////////////// 	 
	public void init() {
		boboGames	= this.getImage(this.getCodeBase(), "bobo_games.gif");
		waitForImage(this, boboGames);
		programName	= this.getImage(this.getCodeBase(), "program_name.gif");
		waitForImage(this, programName);
		pixelColors[0]	= this.getImage(this.getCodeBase(), "pixel_normal.gif");
		waitForImage(this, pixelColors[0]);
		pixelColors[1]	= this.getImage(this.getCodeBase(), "pixel_scan.gif");
		waitForImage(this, pixelColors[1]);
		pixelColors[2]	= this.getImage(this.getCodeBase(), "pixel_scan_nbr.gif");
		waitForImage(this, pixelColors[2]);
		pixelColors[3]	= this.getImage(this.getCodeBase(), "pixel_add_q.gif");
		waitForImage(this, pixelColors[3]);
		pixelColors[4]	= this.getImage(this.getCodeBase(), "pixel_delete_q.gif");
		waitForImage(this, pixelColors[4]);

		// set up doublebuffer
		bufferSize = this.getSize();
		buffer = this.createImage(bufferSize.width, bufferSize.height);
		bufferGraphics = buffer.getGraphics();
		
		toolkit = this.getToolkit();

		this.setFont(f);
		bufferGraphics.setFont(f);
		
		// get applet size on window
		appletSize = this.getSize();
		
		// create 2D-array of pixels (the image)
		// make the image +2 sizes bigger in each direction, makes space for wall of pixels
		pixel = new Pixel[_imageSize + 2][_imageSize + 2];
		for(int i = 0; i < _imageSize + 2; i++) {
			for(int j = 0; j < _imageSize + 2; j++) {
				pixel[i][j] = new Pixel(i, j, 0, _pixelSize, _imageX, _imageY);
			}
		}
		
		// pixel typenames, used in legend
		pixelLegend[0] = "dormant";
		pixelLegend[1] = "scanning";
		pixelLegend[2] = "scanning neighbors of";
		pixelLegend[3] = "adding to Queue";
		pixelLegend[4] = "deleteing from Queue";
	
		// initialize offsets
		offset[0] = new Position(0, 1);		// right
		offset[1] = new Position(1, 0);		// down
		offset[2] = new Position(0, -1);	// left
		offset[3] = new Position(-1, 0);	// up
	
		// setlayout manager to null
		// all objects must be placed manually
		setLayout(null);
		
		markPixelsButton = new Button("mark pixels");
		markPixelsButton.setBounds(_buttonX, _buttonY, _wideButton, _buttonHeight);
		add(markPixelsButton);
		markPixelsButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if(active_button == _markingPixels) active_button = _none;
				else active_button = _markingPixels;
			}// end mousePressed
		});

		labelImageButton = new Button("label image");
		labelImageButton.setBounds(_buttonX, _buttonY + _buttonHSpacer, _wideButton, _buttonHeight);
		add(labelImageButton);
		labelImageButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				active_button = _none;
				if(imageLabeled || labelingImage) {
					toolkit.beep();
					messageCenter.append("Hit 'Clear Label' or 'Clear Image' first\n"); 
				}
				else {
					ResetLabelingData();
					labelingImage = true;
				}
			}// end mousePressed
		});

		clearLabelingButton = new Button("clear labeling");
		clearLabelingButton.setBounds(_buttonX, _buttonY + _buttonHSpacer * 2, _wideButton, _buttonHeight);
		add(clearLabelingButton);
		clearLabelingButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				active_button = _none;
				ClearLabeling();
			}// end mousePressed
		});

		clearImageButton = new Button("clear image");
		clearImageButton.setBounds(_buttonX, _buttonY + _buttonHSpacer * 3, _wideButton, _buttonHeight);
		add(clearImageButton);
		clearImageButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				active_button = _none;
				ClearImage();
			}// end mousePressed
		});

		pauseButton = new Button("pause");
		pauseButton.setBounds(_buttonX, _buttonY + _buttonHSpacer * 4, _wideButton, _buttonHeight);
		add(pauseButton);
		pauseButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) { 
				// remove the pause button and replace it with a continue button
				remove(pauseButton);
				add(continueButton);
				pause = true;
			}
		});

		continueButton = new Button("continue");
		continueButton.setBounds(_buttonX, _buttonY + _buttonHSpacer * 4, _wideButton, _buttonHeight);
		continueButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) { 
				// remove the continue button and replce it with a pause button
				remove(continueButton);
				add(pauseButton);
				pause = false;
			}
		});

		//speed bar
		speedBar = new Scrollbar(Scrollbar.HORIZONTAL, 100, 0, 1, 60);
		speedBar.setBounds(_speedBarX, _speedBarY, _speedBarWidth, _speedBarHeight);
		add(speedBar);
		speedBar.setValue(10);
		speedBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				frameRate = e.getValue();
			}
		});	
		
		// message center
		messageCenter = new TextArea();
		messageCenter.setBounds(_messageX, _messageY, _messageWidth, _messageHeight);
		messageCenter.setEditable(true);
		messageCenter.append("All messages will appear here...\n");
		add(messageCenter);
		
		//preMarkedImageChoice choice
		preMarkedImageChoice.add("**PreMarked Image**");
		preMarkedImageChoice.add("Checker");
		preMarkedImageChoice.add("house");
		preMarkedImageChoice.add("-UF-");
		preMarkedImageChoice.setBounds(_buttonX, _buttonY + _buttonHSpacer * 5,(int)( _wideButton * 1.5), _buttonHeight);
		add(preMarkedImageChoice);
		preMarkedImageChoice.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				active_button = _none;
				int selection = preMarkedImageChoice.getSelectedIndex();
				if(selection == 1) PreMarkedImageSelect(checkerImage);
				else if(selection == 2) PreMarkedImageSelect(houseImage);
				else if(selection == 3) PreMarkedImageSelect(ufImage);
				else ClearImage();

			}
		});

// M O U S E   D R A G G E D/////////////////////////////////////////
		this.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {
				if(!labelingImage && !imageLabeled) {
					// translate the mousePos into (x,y) coordinates for the board
					int col = ((e.getX() - _imageX) / _pixelSize);
					int row = ((e.getY() - _imageY) / _pixelSize);
					if(WithinImage(col, row) && active_button == _markingPixels) {
						if(markPixel)
							pixel[row][col].mark(true);
						else // clear any mark from the current pixel
							pixel[row][col].mark(false);
					}
					else if(active_button == _markingPixels) {
						// trying to mark pixels outside of the image
						toolkit.beep();
					}
				}// end if
			}// end mousePressed
		});
		
// M O U S E   P R E S S E D/////////////////////////////////////////		
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if(!labelingImage && !imageLabeled) {
					// translate mousePos into (x,y) coordinates for the board
					int col = ((e.getX() - _imageX) / _pixelSize);
					int row = ((e.getY() - _imageY) / _pixelSize);
					if(WithinImage(col, row) && active_button == _markingPixels) {
						if(pixel[row][col].marked) {
							markPixel = false;	// erase pixels
							pixel[row][col].mark(false);
						}
						else {// clear any mark from the current pixel
							markPixel = true;	// mark pixels
							pixel[row][col].mark(true);
						}
					}
					else if(active_button == _markingPixels) {
						// trying to mark pixels outside of the image
						toolkit.beep();
						messageCenter.append("You can only mark a pixel in the image\n");
					}
				}// end if
			}// end mousePressed
		});
	} // end init()
	
// W I T H I N   I M A G E///////////////////////////////////////////////	
	public boolean WithinImage(int col, int row) {
		// checks if acitve_button is valid
		// and if mouse is on board
		if(col > 0 && col <= _imageSize &&
		   row > 0 && row <= _imageSize ) // tile is inside of board
			return true;
		
		// tile not inside board, give appropriate error below
		return false;
	}

// L A B E L   I M A G E//////////////////////////////////////////////
	public void LabelImage() {
		switch (labelingState) {
		// the reason the lableing process is broken up into so many steps 
		// is because this provides us with the neccesary numbers of seperate
		// steps for each animation sequence
		case _scanForMarkedPixel: 
			pixel[row][col].state = _scanning;
			if(pixel[row][col].label == 1)
				labelingState = _markedPixelFound;
			else
				labelingState = _incrementLoops;
			break;
		
		case _incrementLoops:
			if(col + 1 <= _imageSize) {
				pixel[row][col].state = _normal;
				col++;
				pixel[row][col].state = _scanning;
				labelingState = _scanForMarkedPixel;
			}				
			else if(row + 1 <= _imageSize) { 
				pixel[row][col].state = _normal;
				col = 1; 
				row++; 
				pixel[row][col].state = _scanning;
				labelingState = _scanForMarkedPixel;
			}
			else labelingState = _doneLabelingImage;
			break;
		
		case _markedPixelFound:
			pixel[row][col].label = ++id;
			pixel[row][col].labeled = true;
			here.row = row; here.col = col;
			// init nbrLoopCnt = -1
			nbrLoopCnt = -1;
			pixel[here.row][here.col].state = _scanningNbrs;
			labelingState = _scanForMarkedNbrs;
			break;
		
		case _scanForMarkedNbrs:
			pixel[here.row][here.col].state = _scanningNbrs;
			pixel[nbr.row][nbr.col].state = _normal;
			if(++nbrLoopCnt >= NumOfNbrs) {
				// no marked pixels around pixel[here.row][here.col]
				// try and remove a pixel from the Q, then search it's nbrs
				labelingState = _removePixelFromQ;
				break;
			}
			else {
				nbr.row = here.row + offset[nbrLoopCnt].row;
				nbr.col = here.col + offset[nbrLoopCnt].col;
				pixel[nbr.row][nbr.col].state = _scanning;
				if(pixel[nbr.row][nbr.col].label == 1) {
					labelingState = _foundMarkedNbr;
				}
				break;
			}
			
		case _foundMarkedNbr:
			pixel[nbr.row][nbr.col].label = id;
			pixel[nbr.row][nbr.col].labeled = true;
			pixel[nbr.row][nbr.col].state = _addToQ;
			q.put(nbr.row);
			q.put(nbr.col);
			labelingState = _scanForMarkedNbrs;
			break;
			
		case _removePixelFromQ:
			pixel[here.row][here.col].state = _normal;
			if(q.isEmpty()) {
				labelingState = _scanForMarkedPixel;
				break;
			}
			else {
				here.row = q.remove();
				here.col = q.remove();
				pixel[here.row][here.col].state = _deleteFromQ;
				nbrLoopCnt = -1;
				labelingState = _scanForMarkedNbrs;
			}			
			break;
			
		case _doneLabelingImage:
			pixel[row][col].state = _normal;
			Done();
			break;
		}// end switch
	}// end LabelImage		

// P A I N T/////////////////////////////////////////////////////////	
	public void paint(Graphics g) {
		// fill the screen with black
		FillScreen(this.getSize(), bufferGraphics, Color.black);
		
		// display program name and bobo games logo
		bufferGraphics.drawImage(boboGames, appletSize.width - 25, appletSize.height - 110, this);
		bufferGraphics.drawImage(programName, _programNameX, _programNameY, this);

		// create display area
		FillArea(_displayX + 2, _displayY + 2, _displayWidth - 3, _displayHeight - 3, 
				 bufferGraphics, Color.white);
		bufferGraphics.setColor(Color.gray);
		bufferGraphics.draw3DRect(_displayX, _displayY, _displayWidth, _displayHeight, false);
		
		bufferGraphics.setColor(Color.white);
		//display animation speed
		bufferGraphics.drawString("Speed = " +(float)frameRate/2+ " frames/sec", _speedBarX + 50,
								  _speedBarY - _speedBarHeight + 5);
		
		// draw legend
		bufferGraphics.drawString("-=PIXEL LEGEND=-", _legendX - 3, _legendY - 8); 
		for(int i = 0; i < _numOfPixelColors; i++) {
			bufferGraphics.drawImage(pixelColors[i], _legendX, _legendY + _legendYSpacer * i, this);
			bufferGraphics.drawString("" +pixelLegend[i], _legendX + _legendXSpacer, 
									  _legendY + _legendTextYAdjust + _legendYSpacer * i );
			
		}
		
		for(int row = 1; row < _imageSize + 1; row++) {
			for(int col = 1; col < _imageSize + 1; col++) {
				pixel[row][col].draw(this, bufferGraphics, pixelColors);
			}
		}
		
		g.drawImage(buffer, 0, 0, this);
	}
	
// U P D A T E///////////////////////////////////////////////////////	
	public void update(Graphics g) { paint(g); }

// R U N/////////////////////////////////////////////////////////////
	public void run() {
		Thread thisThread = Thread.currentThread();
		long start = 0, sleep = 0;
		while(true) {
			start = System.currentTimeMillis();
			if(!pause) {
				if(labelingImage)
					LabelImage();
				repaint();
			}
			else {
				boolean fuck = true;
			}
			
			sleep = frameRate - (int)(System.currentTimeMillis() - start);
			if(sleep <= 0) sleep = 1;	// never divide by zero
			sleep = 2000 / sleep;
			if(labelingImage) {// no need to slow down applet unless we are animating
				try { thisThread.sleep(sleep); }
				catch (InterruptedException e) {}
			}
			else {// without these lines it runs REALLY SLOWLY!!!
				try { thisThread.sleep(0); }
				catch (InterruptedException e) {}
			}
		}
	}
	public void start() {
		if(animate == null)
			animate = new Thread(this);		
			animate.start();
	}
	public void stop() {
		animate = null;
	}

// W A I T   F O R   I M A G E///////////////////////////////////////	
    public static void waitForImage(Component component, Image image) {
        MediaTracker tracker = new MediaTracker(component);
        try {
            tracker.addImage(image, 0);
            tracker.waitForID(0);
        }
        catch(InterruptedException e) { e.printStackTrace(); }
    }
	
// F I L L   S R E E N///////////////////////////////////////////////
	/**fill the screen(g refers to) with the specified color*/
	public void FillScreen(Dimension size, Graphics g, Color color) {
		Color old_color = g.getColor();
		g.setColor(color);
		g.fillRect(0, 0, size.width, size.height);
		g.setColor(old_color);
	}
	
// F I L L   A R E A/////////////////////////////////////////////////
	/**fill an area(g refers to) with the specified color*/
	public void FillArea(int x, int y, int _x, int _y, Graphics g, Color color) {
		Color old_color = g.getColor();
		g.setColor(color);
		g.fillRect(x, y, _x, _y);
		g.setColor(old_color);
	}
	
// P R E M A R K E D   I M A G E   S E L E C T//////////////////////////////
	public void PreMarkedImageSelect(char preMarkedImage[][]) {
		ClearImage();
		for(int col = 0; col < _imageSize; col++) {
			for(int row = 0; row < _imageSize; row++) {
				if(preMarkedImage[row][col] == '#')
					pixel[row + 1][col + 1].mark(true);					
			}// end for row
		}// end for col
	}// end PreMarkedImageSelect

// Done()////////////////////////////////////////////////////////////
	public void Done() {
		toolkit.beep();
		labelingImage = false;
		imageLabeled = true;
		messageCenter.append("The image has been sucessfully labeled.\n");
	}

// min///////////////////////////////////////////////////////////////////////	
	public int min(int x, int min) {
		if(x < min) return min;
		else return x;
	}		   

// max///////////////////////////////////////////////////////////////////////	
	public int max(int x, int max) {
		if(x > max) return max;
		else return x;
	}		   

// C L E A R  I M A G E////////////////////////////////////////////////////////
	public void ClearImage() {
		ResetLabelingData();
		labelingImage = false;
		imageLabeled = false;
		for(int row = 1; row < _imageSize + 1; row++) {
			for(int col = 1; col < _imageSize + 1; col++) {
				pixel[row][col].clearPixel();
			}
		}
		// if continue button is showing replace it with pause button
		if(continueButton.isShowing()) {
			this.remove(continueButton);
			this.add(pauseButton);
			pause = false;
		}
		
	}
			
// C L E A R   L A B E L I N G////////////////////////////////////////////////
	public void ClearLabeling() {
		ResetLabelingData();
		labelingImage = false;
		imageLabeled = false;
		for(int row = 1; row < _imageSize + 1; row++) {
			for(int col = 1; col < _imageSize + 1; col++) {
				pixel[row][col].clearLabeling();
			}
		}
		// if continue button is showing replace it with pause button
		if(continueButton.isShowing()) {
			this.remove(continueButton);
			this.add(pauseButton);
			pause = false;
		}
	}
	
// R E S E T   L A B E L I N G   D A T A///////////////////////////////////////
	public void ResetLabelingData() {
		labelingImage = false;
		imageLabeled = false;
		row = col = 1;
		here.row = here.col = 0;
		nbr.row = nbr.col = 0;
		id = 1;
		labelingState = _scanForMarkedPixel;
		while(!q.isEmpty()) q.remove();
	}
		
}// end wires class
