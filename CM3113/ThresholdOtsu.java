/* threshold an image by minimising within class variance
 * automatically determined using:
 *     Otsu, A threshold selection method from gray-level histograms
 *     IEEE T. SMC, vol 9, pp. 62-66, 1979.
 *
 * Paul Rosin
 * Cardiff University
 * December 2011
 */

import java.io.*;
import java.util.*;

public class ThresholdOtsu {
    public static void main(String[] args)
    throws java.io.IOException
    {
        String fileNameIn =  args[0];
        String fileNameOut = args[1];
        Image image = new Image();
        int x,y,i,t;
        int thr;
        // NOTE: sigma_1,sigma_2 are actually WEIGHTED variances
        double sigma_W,sigma_1,sigma_2;
        double mean_1,mean_2,omega_1,omega_2;
        double min_sigma_W;
        double count;
        double hist[] = new double[256];

        image.ReadPGM(fileNameIn);

        // calculate histogram
        for (t = 0; t < 256; t++)
            hist[t] = 0;

        for (y = 0; y < image.height; y++) {
            for (x = 0; x < image.width; x++) {
                t = image.pixels[x][y];
                hist[t]++;
            }
        }
        count = image.width*image.height;

        /* normalise so that histogram sums to 1 */
        for (t = 0; t < 256; t++)
            hist[t] /= count;

        thr = -1;
        min_sigma_W = 9e20;
        for (t = 1; t < 255; t++) {
            sigma_1 = omega_1 = mean_1 = 0;
            for (i = 0; i <= t; i++) {
                omega_1 += hist[i];
                mean_1 += i * hist[i];
            }
            mean_1 /= omega_1;
            for (i = 0; i <= t; i++)
                sigma_1 += hist[i] * (i - mean_1)*(i - mean_1);

            sigma_2 = omega_2 = mean_2 = 0;
            for (i = t+1; i <= 255; i++) {
                omega_2 += hist[i];
                mean_2 += i * hist[i];
            }
            mean_2 /= omega_2;

            for (i = t+1; i <= 255; i++)
                sigma_2 += hist[i] * (i - mean_2)*(i - mean_2);

            /* skip if the image is uniform at this threshold */
            if (omega_1 == 0) continue;
            if (omega_2 == 0) continue;

            /* within class variance */
            sigma_W = sigma_1 + sigma_2;

            if (sigma_W < min_sigma_W) {
                min_sigma_W = sigma_W;
                thr = t;
            }
        }

        /* actually threshold image now */
        System.out.println("thresholding at "+thr);
        System.out.println("min_sigma_W = "+min_sigma_W);

        for (y = 0; y < image.height; y++)
            for (x = 0; x < image.width; x++)
                if (image.pixels[x][y] < thr)
                    image.pixels[x][y] = 0;
                else
                    image.pixels[x][y] = 255;

        image.WritePGM(fileNameOut);
    }
}
