package uk.ac.cam.jp738.fjava.tick0;

import java.io.File;

public class Statics {

    public static void swap(String from, String to) throws java.io.IOException
    {
        if (ExternalSort.TESTING)
            swapYesDelete(from, to);
        else
            swapNoDelete(from, to);
    }

    // Used on my machine (TESTING == true)
    public static void swapYesDelete(String from, String to) throws java.io.IOException
    {
        System.out.println("Deleting " + to);
        System.out.print(
                new File(to).delete()
                        + " ");
        System.out.println(
                new File(from).renameTo(new File(to))
        );
    }

    // Used in submission build (TESTING == false)
    public static void swapNoDelete(String from, String to) throws java.io.IOException
    {
        new File(from).renameTo(new File(to));
    }

    public static void log(Object msg)
    {
        if (ExternalSort.DEBUG)
            System.out.println(msg);
    }

}
