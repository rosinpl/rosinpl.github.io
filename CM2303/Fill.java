import java.io.*;
import java.util.*;

public class Fill {
    public static void main(String[] args)
    throws java.io.IOException
    {
        String fileNameIn =  args[0];
        String fileNameOut = args[1];
        Image image = new Image();
        ArrayStack stack = new ArrayStack(5000);

        image.ReadPGM(fileNameIn);
  
		...... perform flood filling here ......

        image.WritePGM(fileNameOut);
    }    
}
