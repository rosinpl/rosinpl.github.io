/* histogram equalisation
 *
 * Paul Rosin
 * Cardiff University
 * December 2008
 */

import java.io.*;
import java.util.*;

public class Equalise {
    public static void main(String[] args)
    throws java.io.IOException
    {
        String fileNameIn =  args[0];
        String fileNameOut = args[1];
        Image image = new Image();
        int hist[][] = new int[256][2];
        int i,n,x,y;
        double sum,temp;

        image.ReadPGM(fileNameIn);

        for (i = 0; i < 256; i++)
            hist[i][0] = hist[i][1] = 0;

        n = 0;
        for (x = 0; x < image.width; x++)
            for (y = 0; y < image.height; y++) {
                hist[image.pixels[x][y]][0]++;
                n++;
            }

        sum = 0;
        for (i = 0; i < 256; i++) {
            sum += hist[i][0];
            temp = 255 * sum / n;
            temp = (temp > 255) ? 255 : temp;
            hist[i][1] = (int)temp;
        }

        for (y = 0; y < image.height; y++)
            for (x = 0; x < image.width; x++)
               image.pixels[x][y] = hist[image.pixels[x][y]][1];

        image.WritePGM(fileNameOut);
    }
}
