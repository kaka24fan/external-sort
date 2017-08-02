package uk.ac.cam.jp738.fjava.tick0;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

import static java.lang.Math.toIntExact;

public class Verification
{

    public static void verifyLength(int testNum) throws java.io.IOException
    {
        String pathTrue = "backup-test-suite\\test" + testNum + "a.dat";
        String pathToVerify = "test-suite\\test" + testNum + "a.dat";

        RandomAccessFile a1 = new RandomAccessFile(pathTrue, "r");
        RandomAccessFile a2 = new RandomAccessFile(pathToVerify, "r");

        long l1 = a1.length();
        long l2 = a2.length();

        if (l1 == l2)
            System.out.println("File " + pathToVerify + " has the right length (" + (l1/4) + " ints)!");

        else
            System.out.println("File " + pathToVerify + " has incorrect length: " + (l2/4) + " instead of " + (l1/4) + " ints.");

    }

    public static void verifySortedness(int testNum) throws java.io.IOException
    {
        verifySortedness("test-suite\\test" + testNum + "a.dat");
    }

    public static void verifySortedness(String path) throws java.io.IOException
    {
        RandomAccessFile a = new RandomAccessFile(path, "r");
        DataInputStream reader = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a.getFD()), toIntExact(Runtime.getRuntime().maxMemory()) / 3));

        if (a.length() == 0 || a.length() == 4)
        {
            System.out.println("File " + path + " is sorted!");
            return;
        }
        int last = reader.readInt();
        for (int i = 0; i < a.length() / 4 - 4; i++)
        {
            int r = reader.readInt();
            if (last > r)
            {
                System.out.println("File " + path + " is not sorted. At lines " + (i+1) + ", " + (i+2) + ": " + last + " > " + r);
                return;
            }
            last = r;
        }
        System.out.println("File " + path + " is sorted!");
    }
}
