// inverts PPM image

import java.io.*;
import java.util.*;

public class InvertPPM
{
    public static void main(String[] args)
    throws java.io.IOException
    {
        String fileNameIn = args[0];
        String fileNameOut = args[1];
        ImagePPM image = new ImagePPM();

        image.ReadPPM(fileNameIn);
  
        // invert image
        for (int y = 0; y < image.height; y++)
            for (int x = 0; x < image.width; x++)
                for (int i = 0; i < 3; i++)
                    image.pixels[i][x][y] = 255 - image.pixels[i][x][y];

        image.WritePPM(fileNameOut);
    }
}
