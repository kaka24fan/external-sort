import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

public class Test {

    public static void test() throws java.io.IOException
    {
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

            printFile(f1, true);
            ExternalSort.sort(f1, f2);
            printFile(f1, false);

            String ourChecksum = ExternalSort.checkSum(f1);
            String correctChecksum = checksums.get(i-1);
            System.out.println(i + ": " + (ourChecksum.equals(correctChecksum) ? "OK" : "WRONG"));
        }
    }

    public static void test(int test_num) throws java.io.IOException
    {
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
    }

    public static void printFile(String path, boolean before) throws java.io.IOException {
        RandomAccessFile a = new RandomAccessFile(path, "r");
        DataInputStream reader = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a.getFD()), toIntExact(Runtime.getRuntime().maxMemory()) / Sort1.MAX_MEM_DIVIDER));

        int r = -69;

        System.out.println("\n" + (before ? "(Before) " : "(After) ") + path + ":");
        for (int i = 0; i < a.length() / 4; i++)
        {
            r = reader.read();
            System.out.println(r);
        }
    }
}
