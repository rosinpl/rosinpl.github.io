/* Kitchen/Rosenfeld corner detector
 * does not really work well!
 *
 * Paul Rosin
 * Cardiff University
 * December 1992
 */

import java.io.*;
import java.util.*;

public class Corners {
    public static void main(String[] args)
    throws java.io.IOException
    {
        String fileNameIn =  args[0];
        String fileNameOut = args[1];
        Image image = new Image();
        double gx[][] = new double[1500][1500];
        double gy[][] = new double[1500][1500];
        double gxx[][] = new double[1500][1500];
        double gyy[][] = new double[1500][1500];
        double gxy[][] = new double[1500][1500];
        double cornerity[][] = new double[1500][1500];
        boolean maxima[][] = new boolean[1500][1500];
        int nms_window_radius = 1;
        int threshold = 1000;
        int x,y;
        int numberCorners = 0;
        int border;

        image.ReadPGM(fileNameIn);

        // calculate 1st derivatives
        for (y = 1; y < image.height-1; y++) {
            for (x = 1; x < image.width-1; x++) {
               gx[x][y] = (double)image.pixels[x-1][y-1] +
                          (double)image.pixels[x][y-1]*2 +
                          (double)image.pixels[x+1][y-1] -
                          (double)image.pixels[x-1][y+1] -
                          (double)image.pixels[x][y+1]*2 -
                          (double)image.pixels[x+1][y+1];
    
               gy[x][y] = (double)image.pixels[x-1][y-1] +
                          (double)image.pixels[x-1][y]*2 +
                          (double)image.pixels[x-1][y+1] -
                          (double)image.pixels[x+1][y-1] -
                          (double)image.pixels[x+1][y]*2 -
                          (double)image.pixels[x+1][y+1];
            }
        }
    
        // calculate 2nd derivatives
        for (y = 1; y < image.height-1; y++) {
            for (x = 1; x < image.width-1; x++) {
               gxx[x][y] = (double)gx[x-1][y-1]+
                           (double)gx[x][y-1]*2+
                           (double)gx[x+1][y-1]-
                           (double)gx[x-1][y+1]-
                           (double)gx[x][y+1]*2-
                           (double)gx[x+1][y+1];
    
               gxy[x][y] = (double)gx[x-1][y-1]+
                           (double)gx[x-1][y]*2+
                           (double)gx[x-1][y+1]-
                           (double)gx[x+1][y-1]-
                           (double)gx[x+1][y]*2-
                           (double)gx[x+1][y+1];
    
               gyy[x][y] = (double)gy[x-1][y-1]+
                           (double)gy[x-1][y]*2+
                           (double)gy[x-1][y+1]-
                           (double)gy[x+1][y-1]-
                           (double)gy[x+1][y]*2-
                           (double)gy[x+1][y+1];
            }
        }

        // calculate corner strength
        for (y = 3; y < image.height-3; y++) {
            for (x = 3; x < image.width-3; x++) {
                double val  = gxx[x][y] * gy[x][y] * gy[x][y] + gyy[x][y] * gx[x][y] * gx[x][y] - 2 * gxy[x][y] * gx[x][y] * gy[x][y];
                double div  = gx[x][y]  * gx[x][y] + gy[x][y] * gy[x][y];
                val = (div == 0) ? 0.0 : val/div;
    
                // take absolute since curvature (val) is signed
                cornerity[x][y] = Math.abs(val);
            }
        }

        border = 3 + nms_window_radius;
        // perform non-maximal suppression on "cornerity"
        for (x = border; x < image.width-border; x++) {
            for (y = border; y < image.height-border; y++) {
                int colour;
                boolean maximum = true;
                maxima[x][y] = true;
                for (int xx = -nms_window_radius; xx <= nms_window_radius && maximum; xx++)
                    for (int yy = -nms_window_radius; yy <= nms_window_radius && maximum; yy++) {
                        if (xx != 0 || yy != 0) {
                            if (cornerity[x+xx][y+yy] > cornerity[x][y]) {
                                maxima[x][y] = false;
                                maximum = false;
                            }
                       }
                    }

                // plot corner if maximum and above threshold
                if (maximum && (cornerity[x][y] > threshold)) {
                    numberCorners++;
                    if (image.pixels[x][y] < 128)
                        colour = 255;
                    else
                        colour = 0;
                    image.pixels[x][y] = colour;
                    image.pixels[x+1][y] = colour;
                    image.pixels[x+1][y+1] = colour;
                    image.pixels[x][y+1] = colour;
                }
            }
        }

        System.out.println("found "+numberCorners+" corners");

        image.WritePGM(fileNameOut);
    }
}
