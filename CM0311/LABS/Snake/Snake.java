/* applet to demonstrate snakes - converted from Shah & William's code
 * the inverted distance transform is used to drive the snake
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

public class Snake extends Applet
{
    Image displayImage;
    public static int width,height,depth;
    public static int [][] dtImage = new int[1000][1000];
    public static boolean fileError = false;
    public static String dtFileName = "turing_dti.pgm";
    public static int [] xdata = new int[5000];
    public static int [] ydata = new int[5000];
    public static int [] xdata2 = new int[5000];
    public static int [] ydata2 = new int[5000];
    public int numPixels = 0;
    public int numPixels2 = 0;
    private Button button1,button2;
    public boolean adding_points = false;

    public void greedy_snake()
    {
        final int J = 1;
        final int MID = 4;           // number of a point that does not move
        final int BIG = 1000000;
    
        int d[]  = { 3, 2, 1,   4,8,0,   5,6,7};
        int xi[] = {-1, 0, 1,  -1,0,1,  -1,0,1};
        int yi[] = {-1,-1,-1,   0,0,0,   1,1,1};
    
        int x_shift,y_shift,dx,dy;
        int shift_thresh = numPixels / 10;
        int max_mag,k_max;
        double [] mmag = new double[9];
        double [] ccont = new double[9];
        double [] ccur = new double[9];
        double ia,ib,ic;
        double [] a = new double[5000];
        double [] b = new double[5000];
        double [] c = new double[5000];
        int mag;
        int n;
        int iteration,i,j;
        int jmin,min_mag;
        double min_e,e;
        int cont,cur,cx,cy,cont_x,cont_y;
        double ds;
        int xo,yo;        /* offset into 3x3 window from centre */
        int prev2_i,prev_i,this_i,next_i;
        int prev_iJ,next_iJ;
        int max_iterations = 200;
    
        ia = 0.8;
        ib = 1.4;
        ic = 1.7;
    
        ds = dx = dy = 0;
    
        for (i = 0; i < numPixels; i++) {
            if (i > 0) {
                    dx = xdata[i] - xdata[i-1];
                    dy = ydata[i] - ydata[i-1];
                ds += Math.sqrt((double) (dx * dx + dy * dy));
            }
        }
    
        ds = ds / (numPixels - 1.0);
    
        for (i = 0; i < numPixels; i++) {
            a[i] = ia;
            b[i] = ib;
            c[i] = ic;
        }
    
        iteration = 0;
        do {
            iteration++;
            x_shift = y_shift = 0;
    
            /* for each point in snake */
            for (i = 0; i < numPixels + 1; i++) {
                max_mag = k_max = 0;
                min_mag = BIG;
                prev2_i = (i - 2 + numPixels) % numPixels;
                prev_i = (i - 1 + numPixels) % numPixels;
                next_i = (i + 1 + numPixels) % numPixels;
                this_i = i % numPixels;
                prev_iJ = (i - J + numPixels) % numPixels;
                next_iJ = (i + J + numPixels) % numPixels;
                /* for each point in 3x3 neighbourhood */
                for (j = 0; j < 9; j++) {
                    xo = xi[j]; yo = yi[j];
                    if (
                        /* prevent 2 adj pts being same */
                        (((xdata[this_i] + xo) != (xdata[prev_i])) ||
                         ((ydata[this_i] + yo) != (ydata[prev_i])))
                         )
                     {
                        /* calculate curvature over distance 2xJ */
                        cx = xdata[next_iJ] - 2 * (xdata[this_i] + xo) + xdata[prev_iJ];
                        cx = cx * cx;
                        cy = ydata[next_iJ] - 2 * (ydata[this_i] + yo) + ydata[prev_iJ];
                        cy = cy * cy;
                        cur = cx + cy;
                        
                        cont_x = xdata[prev_i] - xdata[this_i] - xo;
                        cont_x = cont_x * cont_x;
                        cont_y = ydata[prev_i] - ydata[this_i] - yo;
                        cont_y = cont_y * cont_y;
                        cont = cont_x + cont_y;
    
                        mag = dtImage[(xdata[this_i] + xo)][(ydata[this_i] + yo)];
                        max_mag = Math.max(max_mag,mag);
                        min_mag = Math.min(max_mag,mag);
                        mmag[j] = (double) mag;
                        ccont[j] = Math.abs(ds - Math.sqrt((double) cont));
    
                        /*
                        ccont[j] *= ccont[j];
                        */
    
                        k_max = Math.max(k_max,cur);
                        ccur[j] = (double) cur;
                    }
                    else
                        mmag[j] = ccont[j] = ccur[j] = 0.0;
                }
    
                min_e = BIG;
                jmin = MID;
                for (j = 0; j < 9; j++) {
                    if ((max_mag - min_mag) < 5)
                        min_mag = max_mag - 5;
                    e = -c[this_i] * (mmag[j] - min_mag) / (max_mag - min_mag) +
                         a[this_i] * ccont[j] + b[this_i] * ccur[j] / k_max;
                    if ((mmag[j] != 0) && (e < min_e) &&
                        (((xdata[this_i] + xi[j]) != (xdata[prev2_i])) ||
                         ((ydata[this_i] + yi[j]) != (ydata[prev2_i]))))
                    {
                        min_e = e;
                        jmin = j;
                    }
                }
                xdata[this_i] += xi[jmin];
                ydata[this_i] += yi[jmin];
                x_shift += Math.abs(xi[jmin]);
                y_shift += Math.abs(yi[jmin]);
            }
            
            Graphics g = getGraphics();
            update(g);

            // this didn't work as the requests just got accumulated and
            // executed at the end, so do the above
            //repaint();
    
        } while (((x_shift > shift_thresh) || (y_shift > shift_thresh)) &&
                 (iteration < max_iterations));
    }

    // override update
    public void update(Graphics g) {
        paint(g);
    }

    public void paint (Graphics g)
    {
        g.drawImage(this.displayImage,0,0,this);

        if (fileError) {
            Font font = new Font("Helvetica",Font.PLAIN,40);
            g.setFont(font);
            g.setColor(Color.red);
            g.drawString("Error in reading filename",100,100);
            return;
        }
        else {
            g.setColor(Color.red);
            for (int i = 0; i < numPixels-1; i++)
                g.drawLine(xdata[i],ydata[i],xdata[i+1],ydata[i+1]);
            // only close contour when the user has finished inputting points
            if (!adding_points && numPixels != 0)
                g.drawLine(xdata[numPixels-1],ydata[numPixels-1],xdata[0],ydata[0]);
        }
    }

    public boolean action(Event e, Object o)
    {
        if (e.target instanceof Button) {
            if (e.target == button1) {
                numPixels = 0;
                adding_points = true;
            }

            if (e.target == button2) {
                adding_points = false;
                resample_data();
                greedy_snake();
            }
        }

        repaint();

        return true;
    }

    // densely sparse points
    public void resample_data()
    {
        double dx,dy,dist;

        numPixels2 = 0;
        for (int i = 0; i < numPixels; i++) {
            dx = xdata[(i+1)%numPixels] - xdata[i];
            dy = ydata[(i+1)%numPixels] - ydata[i];
            dist = Math.sqrt(dx*dx + dy*dy);
            for (int j = 0; j < dist-1; j += 2) {
                xdata2[numPixels2] = xdata[i] + (int)((double)j / dist * dx);
                ydata2[numPixels2] = ydata[i] + (int)((double)j / dist * dy);
                numPixels2++;
            }
        }

        // copy the data back into original array
        for (int i = 0; i < numPixels2; i++) {
            xdata[i] = xdata2[i];
            ydata[i] = ydata2[i];
        }
        numPixels = numPixels2;
    }

    public boolean mouseDown(Event e, int x, int y)
    {
        boolean moved;

        if (adding_points) {
            xdata[numPixels] = x;
            ydata[numPixels] = y;
            numPixels++;
            repaint();
        }

        return true;
    }

    public void init()
    {
        button1 = new Button("Initialise");
        button2 = new Button("Refine");
        add(button1);
        add(button2);

        displayImage = this.getImage(getDocumentBase(),"turing.gif");
        ReadPgm(dtFileName,dtImage);
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
                "http://users.cs.cf.ac.uk/Paul.Rosin/CM0311/LABS/Snake/"+fileName);
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
}
