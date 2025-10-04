/* applet to demonstrate classification
 *
 * Paul Rosin
 * 2001
 */

import java.awt.*;
import java.applet.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class Classify extends Applet
{
    int xp,yp;
    public static boolean fileError = false;
    public boolean tryAgain = true;
    final int xoffset = 80;
    final int yoffset = 205;
    final static int numClasses = 3;
    double ellip,circ;
    final int graphSize = 300;
    public static double x1,y1=0,x2,y2=0;
    private Button button1,button2;

    // record which fish are selected for training
    public static int row[] = new int[10];
    public static int col[] = new int[10];
    public static int numSelected = 0;

    // fish pixel data
    public static int [][][] X1 = new int[numClasses][5][1000];
    public static int [][][] Y1 = new int[numClasses][5][1000];
    public static int [][] numPixels = new int[numClasses][5];
    public static Polygon [][] polygons = new Polygon[numClasses][5];

    // fish classification data
    public static double [][] circularity = new double[numClasses][5];
    public static double [][] ellipticity = new double[numClasses][5];
    public static double [][] label = new double[numClasses][5];
    public static double meanC[] = new double[numClasses];
    public static double meanE[] = new double[numClasses];

    public void paint (Graphics g)
    {
        int i,j,k;

        if (fileError) {
            Font font = new Font("Helvetica",Font.PLAIN,40);
            g.setFont(font);
            g.setColor(Color.red);
            g.drawString("Error in reading filename",100,100);
            return;
        }
        else {
			// classification results
            if (y1 != y2) {
                g.setColor(Color.red);
                g.drawLine((int)(x1*graphSize+6*xoffset),(int)(y1*graphSize+100),
                           (int)(x2*graphSize+6*xoffset),(int)(y2*graphSize+100));

                for (k = 0; k < numClasses; k++) {
                    for (j = 0; j < 5; j++) {
                        if (label[k][j] < 0)
                            g.setColor(Color.black);
                        else
                            g.setColor(Color.gray);

                        g.fillPolygon(polygons[k][j]);
                    }
                }
            }

            // plot basic shapes and their point in feature space
            for (k = 0; k < numClasses; k++) {
                if (k == 0)      g.setColor(Color.magenta);
                else if (k == 1) g.setColor(Color.cyan);
                else if (k == 2) g.setColor(Color.blue);
                for (j = 0; j < 5; j++)
                    for (i = 0; i < numPixels[k][j]-1; i++) {
                        g.drawLine(X1[k][j][i]+xoffset*j,Y1[k][j][i]+yoffset*k,
                                   X1[k][j][i+1]+xoffset*j,Y1[k][j][i+1]+yoffset*k);
                        g.fillOval((int)(circularity[k][j]*graphSize+6*xoffset),
                                   (int)(ellipticity[k][j]*graphSize+100),
                                   7,7);
                        g.drawRect(xoffset*0,yoffset*k+2,xoffset*5-4,yoffset-12);
						// ?????????????????????
                        g.drawRect(xoffset*0-1,yoffset*k+2-1,xoffset*5-4+2,yoffset-12+2);
                }
            }

            // recolour selected shapes
            g.setColor(Color.green);
            for (int m = 0; m < numSelected; m++) {
                k = row[m];
                j = col[m];
                for (i = 0; i < numPixels[k][j]-1; i++)
                    g.drawLine(X1[k][j][i]+xoffset*j,Y1[k][j][i]+yoffset*k,
                               X1[k][j][i+1]+xoffset*j,Y1[k][j][i+1]+yoffset*k);
            }

            // resize selected shapes points in feature space
            for (int m = 0; m < numSelected; m++) {
                k = row[m];
                j = col[m];
                if (k == 0)      g.setColor(Color.magenta);
                else if (k == 1) g.setColor(Color.cyan);
                else if (k == 2) g.setColor(Color.blue);
                g.fillOval((int)(circularity[k][j]*graphSize+6*xoffset),
                           (int)(ellipticity[k][j]*graphSize+100),
                           11,11);
            }

            // plot axes
            g.setColor(Color.black);
            g.drawLine(6*xoffset,100,6*xoffset+300,100);
            g.drawLine(6*xoffset,100,6*xoffset,100+graphSize);
            Font font = new Font("Helvetica",Font.PLAIN,20);
            g.setFont(font);
            g.setColor(Color.red);
            g.drawString("circularity",6*xoffset+100,100-5);
            g.drawString("ellipticity",6*xoffset-40,100+graphSize+15);
        }
    }

    public boolean mouseDown(Event e, int x, int y)
    {
        boolean moved;

        /* select which fish to train classifier */
        xp = x / xoffset;
        yp = y / yoffset;
        if ((xp < 5) && (yp < numClasses)) {
            col[numSelected] = xp;
            row[numSelected] = yp;
            numSelected++;
        }

        repaint();

        return true;
    }

    public boolean action(Event e, Object o)
    {
        if (e.target instanceof Button) {
            if (e.target == button1)
                means();

            if (e.target == button2) {
                numSelected = 0;
                y1 = y2 = 0;
            }
        }

        repaint();

        return true;
    }

    public void means()
    {
        int j,k;
        int [] count = new int[numClasses];
        double Cm,Em,grad;
        int pos1=0,pos2=0; 

        for (k = 0; k < numClasses; k++) {
            count[k] = 0;
            meanC[k] = meanE[k] = 0;
        }

        for (int m = 0; m < numSelected; m++) {
            k = row[m];
            j = col[m];
            meanC[k] += circularity[k][j];
            meanE[k] += ellipticity[k][j];
            count[k]++;
        }

        for (k = 0; k < numClasses; k++) {
            meanC[k] /= count[k];
            meanE[k] /= count[k];
        }

        if (count[0] == 0) {
            pos1 = 1;
            pos2 = 2;
        }
        else if (count[1] == 0) {
            pos1 = 0;
            pos2 = 2;
        }
        else if (count[2] == 0) {
            pos1 = 0;
            pos2 = 1;
        }

        Cm = (meanC[pos1] + meanC[pos2])/2;
        Em = (meanE[pos1] + meanE[pos2])/2;
        grad = (meanE[pos1] - meanE[pos2]) / (meanC[pos1] - meanC[pos2]);

        x1 = 0;
        y1 = -1 / grad * (x1 - Cm) + Em;
        x2 = 1;
        y2 = -1 / grad * (x2 - Cm) + Em;

        // perform classification
        for (k = 0; k < numClasses; k++)
            for (j = 0; j < 5; j++)
                label[k][j] = -1 / grad * (circularity[k][j] - Cm) + Em - ellipticity[k][j];
    }
    
    // Proffitt's shape measures
    public void shape(int row, int col)
    {
        int i;
        double np,c1,c2;
        double x,y,xsum,ysum,x10=0,y10=0,x11,y11,x12,y12,xy,xysq;
        double r,uplus,vplus,uminus,vminus;
        double zpmod,zmmod,zsq;
        double iu,iv,k,idir;
        double a,b;
    
        np = numPixels[row][col];
        c1 = 2 * Math.cos(2.0*Math.PI/np);
        c2 = Math.sin(2.0*Math.PI/np);
    
        xsum = ysum = x11 = y11 = x12 = y12 = xysq = 0;
    
        for (i = 0; i < np; i++) {
            x = X1[row][col][i];
            y = Y1[row][col][i];
            xsum += x;
            ysum += y;
            xysq += x*x + y*y;
            x10 = x + c1*x11-x12;
            y10 = y + c1*y11-y12;
            x12 = x11;
            y12 = y11;
            x11 = x10;
            y11 = y10;
        }
        xsum /= np;
        ysum /= np;
        xysq = xysq / np - SQR(xsum) - SQR(ysum);
        xy = Math.sqrt(xysq);
        c1 /= 2.0;
        uplus = (x10-x12*c1-y12*c2) / (xy*np);
        vplus = (y10-y12*c1+x12*c2) / (xy*np);
        uminus = (x10-x12*c1+y12*c2) / (xy*np);
        vminus = (y10-y12*c1-x12*c2) / (xy*np);
        circ = Math.sqrt(SQR(uplus)+SQR(vplus));
        ellip = Math.sqrt(SQR(uplus)+SQR(vplus)+SQR(uminus)+SQR(vminus));

    }

    public double SQR(double d)
    {
        return d*d;
    }

    public void init()
    {
        button1 = new Button("Classify");
        button2 = new Button("Reset");
        add(button1);
        add(button2);

        Read5Pix("c",0);
        Read5Pix("e",1);
        Read5Pix("h",2);

        for (int k = 0; k < numClasses; k++)
            for (int j = 0; j < 5; j++) {
                polygons[k][j] = new Polygon();
                for (int i = 0; i < numPixels[k][j]; i++)
                    polygons[k][j].addPoint(X1[k][j][i]+xoffset*j,Y1[k][j][i]+yoffset*k);
            }

        for (int k = 0; k < numClasses; k++)
            for (int j = 0; j < 5; j++) {
                shape(k,j);
                circularity[k][j] = circ;
                ellipticity[k][j] = 70*(ellip-0.978);
            }
    }

    public void Read5Pix(String fileName, int i)
    {
        numPixels[i][0] = ReadPix(fileName+"0"+".pix",X1[i][0],Y1[i][0]);
        numPixels[i][1] = ReadPix(fileName+"1"+".pix",X1[i][1],Y1[i][1]);
        numPixels[i][2] = ReadPix(fileName+"2"+".pix",X1[i][2],Y1[i][2]);
        numPixels[i][3] = ReadPix(fileName+"3"+".pix",X1[i][3],Y1[i][3]);
        numPixels[i][4] = ReadPix(fileName+"4"+".pix",X1[i][4],Y1[i][4]);
    }

    public static int ReadPix(String fileName, int [] X1, int [] Y1)
    {
        String line;
        StringTokenizer st;
        int count = 0;
        URL fileURL = null;

        fileError = false;

        try {
            fileURL = new URL(
                "http://users.cs.cf.ac.uk/Paul.Rosin/CM0311/LABS/Classifier/"+fileName);
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
                X1[count] = Integer.parseInt(st.nextToken());
                Y1[count] = Integer.parseInt(st.nextToken());
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
