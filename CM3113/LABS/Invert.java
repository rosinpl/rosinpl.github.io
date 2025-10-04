import java.io.*;
import java.util.*;

public class Invert {
    public static void main(String[] args)
    throws java.io.IOException
    {
        String fileNameIn =  args[0];
        String fileNameOut = args[1];
        Image image = new Image();

        image.ReadPGM(fileNameIn);
  
        for (int y = 0; y < image.height; y++)
            for (int x = 0; x < image.width; x++)
               image.pixels[x][y] = 255 - image.pixels[x][y];

        image.WritePGM(fileNameOut);
    }    
}
