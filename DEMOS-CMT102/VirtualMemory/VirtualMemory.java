// small modification - PLR 2012

//
// 				CSCI 402 Midterm 2
//
// Author: Murilo Coutinho
// USC ID: 607-60-7386
//
// Based on the demo applet: 
//	~csci402/java/demo/GraphLayout/Graph.java
//
import java.util.*;
import java.awt.*;
import java.applet.Applet;

// This class is used for both Virtual Memory and Physical
// pages.
class Page {
    double x;
    double y;

    int VMpageIndex;
    int LRUcounter;

    boolean dirty;
    boolean empty;

    String lbl;
}

class MyProcess {
    double x, y;

    int PHpageIndex;

    boolean IsRunning;
    boolean FaultedPage;

    String lbl;
}

class VirtualMemoryPanel extends Panel implements Runnable {
    VirtualMemory graph;
    int nVMpages = 16;
    Page VMpages[] = new Page[nVMpages];

    int nPHpages = 8;
    Page PHpages[] = new Page[nPHpages];

    int nprocess = 8;
    int indexRunningProcess = 0;
    MyProcess RunProcess[] = new MyProcess[nprocess];

	int box_spacing = 30;  // controls spacing between rectangles representing pages, etc
	int delay = 300;       // controls running speed of simulation

    Thread relaxer;

    VirtualMemoryPanel(VirtualMemory graph) {
	this.graph = graph;
    }

    void ConstructVMpages() {
	for (int i=0; i < nVMpages; i++) {
	   Page pg = new Page();
	   pg.x = 50;
       pg.y = 20 + box_spacing*i;
	   pg.lbl = "VM page";
	   VMpages[i] = pg;
	}
    }

    void ConstructPHpages() {
	for (int i=0; i < nPHpages; i++) {
	   Page pg = new Page();
	   pg.x = 250;
       pg.y = 110 + box_spacing*i;
	   pg.lbl = "MM frame ";
	   pg.empty = true;
	   pg.VMpageIndex = -1;
	   PHpages[i] = pg;
	}
    }

    void ConstructProcess() {
	for (int i=0; i < nprocess; i++) {
	   MyProcess p = new MyProcess();
	   p.x = 450;
	   p.y = 110 + box_spacing*i;
	   p.IsRunning = false;
	   p.FaultedPage = false;
	   p.PHpageIndex = -1;
  	   p.lbl = "Process";
	   RunProcess[i] = p;
	}
    }	

    public void run() {
	while (true) {
	    timeslice();
	    repaint();
	    try {
		Thread.sleep(delay);
	    } catch (InterruptedException e) {
		break;
	    }
	    referencePage();
	    repaint();
	    try {
		Thread.sleep(delay);
	    } catch (InterruptedException e) {
		break;
	    }
	    RunProcess[indexRunningProcess].FaultedPage = false;
	}
    }

    synchronized void timeslice() {
	int nextRunningProcess = (int)(Math.random() * nprocess);
        RunProcess[indexRunningProcess].IsRunning = false;
	RunProcess[nextRunningProcess].IsRunning = true;
	indexRunningProcess = nextRunningProcess;
    }

    synchronized void referencePage() {
	int pageIndex = (int)(Math.random() * nVMpages);
	
	// Check if page is already in Physical memory
	int hit = -1;
	int emptySlot= -1;
	int i = 0;
	do {
	   if (!PHpages[i].empty) {
	       if (PHpages[i].VMpageIndex == pageIndex) {
		   hit = i;
	       }
	   }
	   if (PHpages[nPHpages - i - 1].empty) {
		emptySlot = nPHpages - i - 1;
	   }
	   i++;
	} while ((hit == -1) && (i < nPHpages));
	if (hit == -1) {
	   // We have a page fault
	   RunProcess[indexRunningProcess].FaultedPage = true;
	   repaint();
	   try {
	      Thread.sleep(delay);
	   } catch (InterruptedException e) {
	   }

	   // Check if there are empty slots in the Physical memory.
	   if (emptySlot != -1) {

	      // Move page from VM to PH

	      // Edge from running process to PH[emptySlot]
	      RunProcess[indexRunningProcess].PHpageIndex = emptySlot;

	      PHpages[emptySlot].VMpageIndex = pageIndex;
	      PHpages[emptySlot].dirty = false;
	      PHpages[emptySlot].empty = false;
	      PHpages[emptySlot].LRUcounter = 0;
	   }
	   else {
	      // All slots are full. Substitute LRU page
	      int LRUindex = 0;
	      for (int j = 1; j < nPHpages; j++) {
		  if (PHpages[LRUindex].LRUcounter < PHpages[j].LRUcounter) {
		      LRUindex = j;
		  }
		  else {
		      PHpages[j].LRUcounter++;
		  }
	      }
	      // Edge from running process to PH[LRUindex]
	      RunProcess[indexRunningProcess].PHpageIndex = LRUindex;

	      PHpages[LRUindex].VMpageIndex = pageIndex;
	      PHpages[LRUindex].dirty = false;
	      PHpages[LRUindex].empty = false;
	      PHpages[LRUindex].LRUcounter = 0;
	   }
	}
	else {
	   // The running process will read from or write to the hit page
	   RunProcess[indexRunningProcess].PHpageIndex = hit;

	   PHpages[hit].LRUcounter = 0;
	   int action = (int)(Math.random() * 3);
	   if (action == 0) {
	      // The process will write to the page. Set its dirty bit
	      PHpages[hit].dirty = true;
           }
        }
    }

    Image offscreen;
    Dimension offscreensize;
    Graphics offgraphics;
    
    final Color VMpageColor = new Color(250, 220, 100);
    final Color edgeColor = Color.black;
    final Color PHemptyColor = Color.white;
    final Color PHpageColor = Color.green;
    final Color PHdirtyColor = Color.yellow;
    final Color pageFaultColor = Color.red;
    final Color runningProcess = Color.blue;
    final Color readyProcess = new Color(135, 206, 235);

    public void DrawLinePHtoVM(Graphics g, FontMetrics fm) {
	for (int i = 0 ; i < nPHpages ; i++) {
	    if (PHpages[i].VMpageIndex != -1) {
	        int x1 = (int)PHpages[i].x;
	        int y1 = (int)PHpages[i].y;
	        int w = fm.stringWidth(PHpages[i].lbl) + 30;
	        x1 -= w/2;
		int x2 = (int)VMpages[PHpages[i].VMpageIndex].x;
		int y2 = (int)VMpages[PHpages[i].VMpageIndex].y;
		w = fm.stringWidth(VMpages[PHpages[i].VMpageIndex].lbl) + 30;
		x2 += w/2;
		g.setColor(edgeColor);
		offgraphics.drawLine(x1, y1, x2, y2);
	    }
      	}
    }
	
    public void DrawLineRunningProcess(Graphics g, FontMetrics fm) {
	if (indexRunningProcess != -1) {
	    int x1 = (int)RunProcess[indexRunningProcess].x;
	    int y1 = (int)RunProcess[indexRunningProcess].y;
	    int w = fm.stringWidth(RunProcess[indexRunningProcess].lbl) + 30;
	    x1 -= w/2;
	    if (RunProcess[indexRunningProcess].PHpageIndex != -1) {
		int x2 = (int)PHpages[RunProcess[indexRunningProcess].PHpageIndex].x;
		int y2 = (int)PHpages[RunProcess[indexRunningProcess].PHpageIndex].y;
		w = fm.stringWidth(PHpages[RunProcess[indexRunningProcess].PHpageIndex].lbl) + 30;
		x2 += w/2;
	        if (!RunProcess[indexRunningProcess].FaultedPage) {
		    g.setColor(edgeColor);
		    offgraphics.drawLine(x1, y1, x2, y2);
		}
	    }
      	}
    }
	
    public void paintProcess(Graphics g, MyProcess p, FontMetrics fm) {
	int x = (int)p.x;
	int y = (int)p.y;

	// added - PLR
	int xt = 750, yt = 30;
	g.setColor(runningProcess);
	g.fillRect(xt, yt, xt+100, yt+20);
	g.setColor(edgeColor);
	g.drawString("process running", xt+50, yt+20);

	yt += 50;
	g.setColor(readyProcess);
	g.fillRect(xt, yt, xt+100, yt+20);
	g.setColor(edgeColor);
	g.drawString("process ready", xt+50, yt+20);

	yt += 50;
	g.setColor(PHemptyColor);
	g.fillRect(xt, yt, xt+100, yt+20);
	g.setColor(edgeColor);
	g.drawString("frame empty", xt+50, yt+20);

	yt += 50;
	g.setColor(pageFaultColor);
	g.fillRect(xt, yt, xt+100, yt+20);
	g.setColor(edgeColor);
	g.drawString("page fault", xt+50, yt+20);

	yt += 50;
	g.setColor(PHpageColor);
	g.fillRect(xt, yt, xt+100, yt+20);
	g.setColor(edgeColor);
	g.drawString("new frame", xt+50, yt+20);

	yt += 50;
	g.setColor(PHdirtyColor);
	g.fillRect(xt, yt, xt+100, yt+20);
	g.setColor(edgeColor);
	g.drawString("dirty frame", xt+50, yt+20);

	// hack - I don't know why the rectangles were too big - PLR
	yt += 50;
	g.setColor(PHdirtyColor);
	g.fillRect(xt, yt, xt+100, yt+20);

	if (p.IsRunning) {
	   if (p.FaultedPage)
	      g.setColor(pageFaultColor);
	   else
	      g.setColor(runningProcess);
	}
	else
	   g.setColor(readyProcess);
	int w = fm.stringWidth(p.lbl) + 30;
	int h = fm.getHeight() + 4;
	g.fillRect(x - w/2, y - h / 2, w, h);
	g.setColor(edgeColor);
	g.drawRect(x - w/2, y - h / 2, w-1, h-1);
	g.drawString(p.lbl, x - (w-10)/2, (y - (h-4)/2) + fm.getAscent());
    }
	
    public void paintVMpage(Graphics g, Page n, FontMetrics fm) {
	int x = (int)n.x;
	int y = (int)n.y;
	g.setColor(VMpageColor);
	int w = fm.stringWidth(n.lbl) + 30;
	int h = fm.getHeight() + 4;
	g.fillRect(x - w/2, y - h / 2, w, h);
	g.setColor(edgeColor);
	g.drawRect(x - w/2, y - h / 2, w-1, h-1);
	g.drawString(n.lbl, x - (w-10)/2, (y - (h-4)/2) + fm.getAscent());
    }

    public void paintPHpage(Graphics g, Page n, FontMetrics fm) {
	int x = (int)n.x;
	int y = (int)n.y;
	if (n.empty)
   	    g.setColor(PHemptyColor);
	else if (n.dirty)
	    g.setColor(PHdirtyColor);
	else
	    g.setColor(PHpageColor);
	int w = fm.stringWidth(n.lbl) + 30;
	int h = fm.getHeight() + 4;
	g.fillRect(x - w/2, y - h / 2, w, h);
	g.setColor(edgeColor);
	g.drawRect(x - w/2, y - h / 2, w-1, h-1);
	g.drawString(n.lbl, x - (w-10)/2, (y - (h-4)/2) + fm.getAscent());
    }

    public synchronized void update(Graphics g) {
	Dimension d = size();
	if ((offscreen == null) || (d.width != offscreensize.width) 
				|| (d.height != offscreensize.height)) {
	    offscreen = createImage(d.width, d.height);
	    offscreensize = d;
	    offgraphics = offscreen.getGraphics();
	    offgraphics.setFont(getFont());
	}

	offgraphics.setColor(getBackground());
	offgraphics.fillRect(0, 0, d.width, d.height);
	for (int i = 0 ; i < nVMpages ; i++) {
    	    FontMetrics fm = offgraphics.getFontMetrics();
	    paintVMpage(offgraphics, VMpages[i], fm);	
	    Page pg = VMpages[i];
	    int x1 = (int)pg.x;
	    int y1 = (int)pg.y;
      	    String lbl = String.valueOf(i);
	    offgraphics.setColor(edgeColor);
	    offgraphics.drawString(lbl, x1 + 20, y1 + 3);
	    offgraphics.setColor(edgeColor);
	}
	for (int i = 0 ; i < nPHpages ; i++) {
    	    FontMetrics fm = offgraphics.getFontMetrics();
	    paintPHpage(offgraphics, PHpages[i], fm);	
	    Page pg = PHpages[i];
	    int x1 = (int)pg.x;
	    int y1 = (int)pg.y;
      	    String lbl = String.valueOf(i);
	    offgraphics.setColor(edgeColor);
	    offgraphics.drawString(lbl, x1 + 20, y1 + 3);
	    offgraphics.setColor(edgeColor);
	}
	for (int i = 0 ; i < nprocess ; i++) {
    	    FontMetrics fm = offgraphics.getFontMetrics();
	    paintProcess(offgraphics, RunProcess[i], fm);	
	    MyProcess p = RunProcess[i];
	    int x1 = (int)p.x;
	    int y1 = (int)p.y;
      	    String lbl = String.valueOf(i);
	    offgraphics.setColor(edgeColor);
	    offgraphics.drawString(lbl, x1 + 20, y1 + 3);
	    offgraphics.setColor(edgeColor);
	}
	DrawLineRunningProcess(offgraphics, offgraphics.getFontMetrics());
	DrawLinePHtoVM(offgraphics, offgraphics.getFontMetrics());
	g.drawImage(offscreen, 0, 0, null);
    }

    public void start() {
	relaxer = new Thread(this);
	relaxer.start();
    }
    public void stop() {
	relaxer.stop();
    }
}

public class VirtualMemory extends Applet {
    VirtualMemoryPanel panel;

    public void init() {
	setLayout(new BorderLayout());

	panel = new VirtualMemoryPanel(this);
	add("Center", panel);
	Panel p = new Panel();
	add("South", p);

	panel.ConstructVMpages();
	panel.ConstructPHpages();
	panel.ConstructProcess();
    }

    public void start() {
	panel.start();
    }
    public void stop() {
	panel.stop();
    }
}
