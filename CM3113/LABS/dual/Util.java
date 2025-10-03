public class Util 
{
    public static boolean debugMode = false;

    public static void dbgPrintln(String s) 
    {
	if(debugMode) 
	    System.out.println(s);
    }
    
    public static void assert(boolean pred, String errMsg) 
    {
	try {
	    if(!pred)
		throw new Exception();
	}
	catch (Exception e) {
	    System.err.println("ASSERTION FAILED--> "+errMsg);
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    public static double min(double a, double b)
    {
	if(a < b) return a; else return b;
    }

    public static double max(double a, double b)
    {
	if(a >= b) return a; else return b;
    }


}
