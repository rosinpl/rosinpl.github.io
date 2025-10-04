/* simple applet to display intensity histogram of image
 *
 * Paul Rosin
 * 2001
 */

import java.io.*;
import java.util.*;
import java.applet.Applet;
import java.awt.*;

public class Hist extends Applet
{
    private TextField filetextfield = new TextField(10);
    public static int width,height,depth;
    public static int [][] image = new int[600][600];
    public static double [] hist = new double[256];
    public static boolean fileBoundsError = false;
    public static boolean fileReadError = false;

    public void paint (Graphics g)
    {
        int yoffset = 50;
        int w = size().width;
        int h = size().height;
        int barwidth = (w-256) / 256;
        double maxHist,yscale = (h - yoffset);
        Font font = new Font("Helvetica",Font.PLAIN,20);

        g.setFont(font);

        // blank window
        g.setColor(Color.white);
        g.fillRect(0,yoffset,w,h);

        if (fileBoundsError) {
            g.setColor(Color.red);
            g.drawString("Error: image too big",55,28);
            return;
        }
        if (fileReadError) {
            g.setColor(Color.red);
            g.drawString("Error in reading filename",55,28);
            return;
        }

        // normalise the histogram
        maxHist = 0;
        for (int i = 0; i < 256; i++)
            if (hist[i] > maxHist)
                maxHist = hist[i];
        for (int i = 0; i < 256; i++)
            hist[i] /= maxHist;

        g.setColor(Color.blue);
        for (int i = 0; i < 256; i++) {
            int y = (int)(hist[i] * yscale);
            int yy = yoffset+(h-y);
            g.fillRect(i*barwidth+i,(h-y),barwidth,y);
        }
    }

    public void init()
    {
        add(new Label("Filename:"));
        add(filetextfield);
    }

    public boolean action(Event e, Object o)
    {
        if (e.target == filetextfield) {
            System.out.println("read name "+o);
            String fileName = (String)o;
            Image image  = new Image();
            image.ReadPGM(fileName);
            if (!fileBoundsError && !fileReadError) {
                for (int i = 0; i < 256; i++)
                    hist[i] = 0;
                for (int y = 0; y < image.height; y++)
                    for (int x = 0; x < image.width; x++)
                        hist[image.pixels[x][y]]++;
            }
            repaint();
        }
        return true;
    }
}
