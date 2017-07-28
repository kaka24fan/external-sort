package uk.ac.cam.jp738.fjava.tick0;

import java.io.File;

public class Statics {

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

    public static void swap(String from, String to) throws java.io.IOException
    {
        if (ExternalSort.TESTING)
            swapYesDelete(from, to);
        else
            swapNoDelete(from, to);
    }

    public static void swapNoDelete(String from, String to) throws java.io.IOException
    {
        new File(from).renameTo(new File(to));
    }

    public static void log(String msg)
    {
        if (ExternalSort.TESTING)
            System.out.println(msg);
    }

}
