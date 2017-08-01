package uk.ac.cam.jp738.fjava.tick0;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

import static java.lang.Math.toIntExact;

public class Test {

    public static void test() throws java.io.IOException // all test cases
    {
        copyTestsFromBackup();
        long time0 = System.nanoTime();

        List<String> checksums = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader("test-suite/checksum.txt")))
        {
            String line;
            while ((line = br.readLine()) != null) {
                checksums.add(line.split(" : ")[1]);
            }
        }

        for (int i = 0; i < 18; i++)
        {
            String f1 = "test-suite\\test" + i + "a.dat";
            String f2 = "test-suite\\test" + i + "b.dat";

            //if (i==13) printFile(f1, true);
            ExternalSort.sort(f1, f2);
            //if (i==13) printFile(f1, false);

            if (i != 0)
            {
                String ourChecksum = ExternalSort.checkSum(f1);
                String correctChecksum = checksums.get(i-1);
                System.out.println(i + ": " + (ourChecksum.equals(correctChecksum) ? "OK" : "WRONG"));
            }
        }

        System.out.println("Done in time " + (System.nanoTime() - time0)/1000000.0 + "ms");
    }

    public static void test(int test_num) throws java.io.IOException // specified, single test case
    {
        copyTestsFromBackup();
        long time0 = System.nanoTime();

        List<String> checksums = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader("test-suite/checksum.txt")))
        {
            String line;
            while ((line = br.readLine()) != null) {
                checksums.add(line.split(" : ")[1]);
            }
        }
        String f1 = "test-suite\\test" + test_num + "a.dat";
        String f2 = "test-suite\\test" + test_num + "b.dat";

        printFile(f1, true);
        ExternalSort.sort(f1, f2);
        printFile(f1, false);

        if (test_num != 0)
        {
            String ourChecksum = ExternalSort.checkSum(f1);
            String correctChecksum = checksums.get(test_num-1);
            System.out.println(test_num + ": " + (ourChecksum.equals(correctChecksum) ? "OK" : "WRONG"));
            System.out.println("Done in time " + (System.nanoTime() - time0)/1000000.0 + "ms");
        }
    }

    public static void printFile(String path, boolean before) throws java.io.IOException {
        System.out.println("\n" + (before ? "(Before) " : "(After) ") + path + ":");
        printFile(path);
    }

    public static void printFile(String path) throws java.io.IOException {
        System.out.println("Printing " + path);
        RandomAccessFile a = new RandomAccessFile(path, "r");
        DataInputStream reader = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a.getFD()), toIntExact(Runtime.getRuntime().maxMemory()) / Sort1.MAX_MEM_DIVIDER));

        for (int i = 0; i < a.length() / 4; i++)
        {
            int r = reader.readInt();
            System.out.println(r);
        }
    }

    public static void printTwoFiles(String path1, String path2) throws java.io.IOException {
        //System.out.println("Printing 2 files: " + path1 + ", " + path2);
        RandomAccessFile a = new RandomAccessFile(path1, "r");
        DataInputStream reader = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a.getFD()), toIntExact(Runtime.getRuntime().maxMemory()) / (2 * Sort1.MAX_MEM_DIVIDER)));
        RandomAccessFile a2 = new RandomAccessFile(path2, "r");
        DataInputStream reader2 = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a2.getFD()), toIntExact(Runtime.getRuntime().maxMemory()) / (2 * Sort1.MAX_MEM_DIVIDER)));

        for (int i = 0; i < a.length() / 4; i++)
        {
            int r = reader.readInt();
            System.out.print(r + "   ");
            System.out.print(reader2.readInt() + "\n");
        }

        for (int i = 0; i < 25; i++)
        {
            System.out.print("-");
        }
        System.out.print("\n");
    }

    private static void copyTestsFromBackup()
    {
        for (int i = 0; i <= 17; i++)
        {
            String s = "backup-test-suite\\test" + i + "a.dat";
            String d = "test-suite\\test" + i + "a.dat";
            File source = new File(s);
            File dest = new File(d);
            try {
                FileUtils.copyFile(source, dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
            s = "backup-test-suite\\test" + i + "b.dat";
            d = "test-suite\\test" + i + "b.dat";
            source = new File(s);
            dest = new File(d);
            try {
                FileUtils.copyFile(source, dest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeExampleFile() throws java.io.IOException
    {
        String d = "backup-test-suite\\test" + 0 + "a.dat";
        RandomAccessFile a12 = new RandomAccessFile(d, "rw");
        DataOutputStream w12 = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(a12.getFD())));
        int[] ar = {4, 7, 6, 2, 9, 5, 8, 1, 3};
        for (int n : ar)
            w12.writeInt(n);

        w12.flush();
        w12.close();
        a12.close();
    }
    public static void writeExampleFile2() throws java.io.IOException
    {
        String d = "backup-test-suite\\test" + 0 + "b.dat";
        RandomAccessFile a12 = new RandomAccessFile(d, "rw");
        DataOutputStream w12 = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(a12.getFD())));
        int[] ar = {4, 7, 6, 2, 9, 5, 8, 1, 3};
        for (int n : ar)
            w12.writeInt(n);

        w12.flush();
        w12.close();
        a12.close();
    }

}
