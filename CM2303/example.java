// simple program to get filename from the command line
// and then read integers (separated by white space) from file
// also shows how to specify output format
//
// Paul Rosin
// Cardiff University

import java.io.*;
import java.util.*;

public class example
{
    public static void main (String[] args) throws Exception
    {
        Formatter formatter = new Formatter ((OutputStream)System.out);
        Scanner scan = null;
        int k = 0;

        if (args.length != 1)
            throw new RuntimeException("exactly 1 command line argument expected");

        // we easily could read other values off the command line, e.g.
        //int k = args[1];

        try {
            scan = new Scanner( new File(args[0]) );
        } catch(FileNotFoundException e)   { System.out.println("file not found");
        } catch(Exception e)   { System.out.println("problems with opening file"); }

        while (scan.hasNextInt()) {
             k = scan.nextInt();

             //System.out.println("read: "+k);
             formatter.format("read: %4d\n",k);
        }
        formatter.flush ();
    }
}
