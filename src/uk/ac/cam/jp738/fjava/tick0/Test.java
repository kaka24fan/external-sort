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

        for (int i = 1; i < 18; i++)
        {
            String f1 = "test-suite\\test" + i + "a.dat";
            String f2 = "test-suite\\test" + i + "b.dat";

            //printFile(f1, true);
            ExternalSort.sort(f1, f2);
            //printFile(f1, false);

            String ourChecksum = ExternalSort.checkSum(f1);
            String correctChecksum = checksums.get(i-1);
            System.out.println(i + ": " + (ourChecksum.equals(correctChecksum) ? "OK" : "WRONG"));
        }

        System.out.println("Done in time " + (System.nanoTime() - time0)*1000000.0 + "ms");
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

        String ourChecksum = ExternalSort.checkSum(f1);
        String correctChecksum = checksums.get(test_num-1);
        System.out.println(test_num + ": " + (ourChecksum.equals(correctChecksum) ? "OK" : "WRONG"));
        System.out.println("Done in time " + (System.nanoTime() - time0)*1000000.0 + "ms");
    }

    public static void printFile(String path, boolean before) throws java.io.IOException {
        System.out.println("\n" + (before ? "(Before) " : "(After) ") + path + ":");
        printFile(path);
    }

    public static void printFile(String path) throws java.io.IOException {
        RandomAccessFile a = new RandomAccessFile(path, "r");
        DataInputStream reader = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a.getFD()), toIntExact(Runtime.getRuntime().maxMemory()) / Sort1.MAX_MEM_DIVIDER));

        for (int i = 0; i < a.length() / 4; i++)
        {
            int r = reader.readInt();
            System.out.println(r);
        }
    }

    private static void copyTestsFromBackup()
    {
        for (int i = 1; i <= 17; i++)
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
        }
    }
}
