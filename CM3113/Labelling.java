/*
 * simple recursive 8-way connected component labelling for binary images
 *
 * NOTE: a recursive solution can result in a stack overflow!
 *       unless the image is tiny you should increase the stack size
 *       java -Xss512m Labelling input.pgm output.pgm
 *
 * assumes < 255 labels
 *
 * Paul Rosin
 * Cardiff University
 * December 2014
 */

import java.io.*;
import java.util.*;

public class Labelling {
    static Image image = new Image();
    static Image labels = new Image();

    public static void main(String[] args)
    throws java.io.IOException
    {
        String fileNameIn =  args[0];
        String fileNameOut = args[1];
        int i,x,y;
        int number_labels = 0;

        image.ReadPGM(fileNameIn);

        // check input image is binary {0,255}
        for (y = 0; y < image.height; y++)
            for (x = 0; x < image.width; x++)
                if ((image.pixels[x][y] != 0) && (image.pixels[x][y] != 255)) {
                    System.out.println("ERROR: only binary iages allowed");
                    System.exit(-1);
                }

        // use 255 as background label
        labels.height = image.height;
        labels.width = image.width;
        for (y = 0; y < labels.height; y++)
            for (x = 0; x < labels.width; x++)
                labels.pixels[x][y] = 255;

        for (y = 0; y < image.height; y++)
            for (x = 0; x < image.width; x++) {
                // if unlabelled foreground pixel
                if ((image.pixels[x][y] == 0) && (labels.pixels[x][y] == 255)) {
                    grow(x,y,number_labels);
                    number_labels++;

                    // images output as 8 bit
                    if (number_labels > 255) {
                        System.out.println("ERROR: too many components (can only represent 255)");
                        System.exit(-1);
                    }
                }
            }

        System.out.println(number_labels+" connected foreground components");
        labels.WritePGM(fileNameOut);
    }

    // recursively grow connectected component label
    public static void grow(int x,int y,int label)
    {
        int xx,yy,tx,ty;

        // outside image
        if ((x < 0) || (y < 0) || (x >= image.width) || (y >= image.height))
            return;

        // not foreground or already labelled
        if ((image.pixels[x][y] != 0) || (labels.pixels[x][y] != 255))
            return;

        labels.pixels[x][y] = label;

        // check neighbourhood for another foreground pixel
        for (xx = -1; xx <= 1; xx++)
            for (yy = -1; yy <= 1; yy++) {
                tx = x + xx;
                ty = y + yy;
                grow(tx,ty,label);
            }
    }
}
