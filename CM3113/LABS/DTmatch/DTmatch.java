/* applet to demonstrate matching using distance transform
 * click mouse to position ear model in the left hand window
 * click again to make it move
 * you can run the process again by clicking
 *
 * Paul Rosin
 * 2001
 */

import java.awt.*;
import java.awt.image.*;
import java.applet.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class DTmatch extends Applet
{
    Image displayImage1,displayImage2;
    int xp = -999;
    int yp = -999;
    public static int width,height,depth;
    public static int [][] dtImage = new int[1000][1000];
    public static boolean fileError = false;
    public static String dtFileName = "turing_dt.pgm";
    public static String pixFileName = "ear.pix";
    public static int [] xdata = new int[1000];
    public static int [] ydata = new int[1000];
    public int numPixels,xc,yc;
    public boolean tryAgain = true;

    public void paint (Graphics g)
    {
        g.drawImage(this.displayImage1,0,0,this);
        g.drawImage(this.displayImage2,512,0,this);

        if (fileError) {
            Font font = new Font("Helvetica",Font.PLAIN,40);
            g.setFont(font);
            g.setColor(Color.red);
            g.drawString("Error in reading filename",100,100);
            return;
        }
        else if (xp != -999) {
            if (tryAgain)
                g.setColor(Color.red);
            else
                g.setColor(Color.blue);
            for (int i = 0; i < numPixels-1; i++)
                g.drawLine(xdata[i]+xp,ydata[i]+yp,xdata[i+1]+xp,ydata[i+1]+yp);
            for (int i = 0; i < numPixels-1; i++)
                g.drawLine(xdata[i]+xp+512,ydata[i]+yp,xdata[i+1]+xp+512,ydata[i+1]+yp);

            if (tryAgain) {
                Font font = new Font("Helvetica",Font.PLAIN,40);
                g.setFont(font);
                g.setColor(Color.green);
                // g.drawString("match error value: "+measureError(xp,yp),100,100);
                g.drawString("match error value: "+(int)measureError(xp,yp),100,100);
            }
        }
    }

    public boolean mouseDown(Event e, int x, int y)
    {
        boolean moved;

        if (tryAgain && (x < width) && (y < height)) {
            xp = x-xc;
            yp = y-yc;
            repaint();
            tryAgain = false;
        }
        else {
            do {
                moved = moveModel();
                repaint();
            } while (moved);
            tryAgain = true;
        }

        return true;
    }

    public void init()
    {
        displayImage1 = this.getImage(getDocumentBase(),"turing.gif");
        displayImage2 = this.getImage(getDocumentBase(),"turing_dt.gif");
        ReadPgm(dtFileName,dtImage);
        numPixels = ReadPix(pixFileName,xdata,ydata);
        if (!fileError) {
            for (int i = 0; i < numPixels; i++) {
                xc += xdata[i];
                yc += ydata[i];
            }
            xc /= numPixels; yc /= numPixels;
        }
    }

    public double measureError(int xp, int yp)
    {
        double error = 0;
        int count = 0;
        int x,y;

        for (int i = 0; i < numPixels; i++) {
            x = xdata[i]+xp;
            y = ydata[i]+yp;
            if ((x >= 0) && (y >= 0))
            if ((x < width) && (y < height)) {
                error += dtImage[x][y];
                count++;
            }
        }

        return(error/(double)count);
    }

    public boolean moveModel()
    {
        double up,down,left,right,here;
        int x,y;

        x = y = 0;

        here = measureError(xp,yp);
        up = measureError(xp,yp-1);
        down = measureError(xp,yp+1);
        right = measureError(xp-1,yp);
        left = measureError(xp+1,yp);

        if ((up < down) && (up < right) && (up < left) && (up < here))
            y = -1;
        else if ((down < up) && (down < right) && (down < left) && (down < here))
            y = 1;
        else if ((right < up) && (right < down) && (right < left) && (right < here))
            x = -1;
        else if ((left < up) && (left < down) && (left < right) && (left < here))
            x = 1;
        
        xp += x;
        yp += y;
        return ((x != 0) || (y != 0));
    }

    public static void ReadPgm(String fileName,int [][] image)
    {
        String line;
        StringTokenizer st;
        int i;
        URL fileURL = null;
        
        fileError = false;

        try {
            fileURL = new URL(
				"http://users.cs.cf.ac.uk/Paul.Rosin/CM0311/LABS/DTmatch/"+fileName);
        }
        catch (MalformedURLException e) {
            fileError = true;
        }

        try {
            DataInputStream in =
              new DataInputStream(
                new BufferedInputStream(
                  fileURL.openStream()));

            // read PGM image header
  
            // skip comments
            in.readLine();
            do {
                line = in.readLine();
            } while (line.charAt(0) == '#');
  
            // the current line has dimensions
            st = new StringTokenizer(line);
            width = Integer.parseInt(st.nextToken());
            height = Integer.parseInt(st.nextToken());
  
            // next line has pixel depth
            line = in.readLine();
            st = new StringTokenizer(line);
            depth = Integer.parseInt(st.nextToken());
  
            // read pixels now
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    image[x][y] = in.readUnsignedByte();
  
            in.close();
        } catch(IOException e) {
            fileError = true;
        }
    }

    public static int ReadPix(String fileName, int [] xdata, int [] ydata)
    {
        String line;
        StringTokenizer st;
        int count = 0;
        URL fileURL = null;

        fileError = false;

        try {
            fileURL = new URL(
				"http://users.cs.cf.ac.uk/Paul.Rosin/CM0311/LABS/DTmatch/"+fileName);
        }
        catch (MalformedURLException e) {
            fileError = true;
        }

        try {
            DataInputStream in =
              new DataInputStream(
                new BufferedInputStream(
                  fileURL.openStream()));

            // skip header
            in.readLine();
            in.readLine();

            // read pixels
            do {
                line = in.readLine();
                st = new StringTokenizer(line);
                xdata[count] = Integer.parseInt(st.nextToken());
                ydata[count] = Integer.parseInt(st.nextToken());
                count++;
            } while (line.charAt(0) != '-');
            count--;
  
            in.close();
        } catch(IOException e) {
            fileError = true;
        }
        return(count);
    }
}
