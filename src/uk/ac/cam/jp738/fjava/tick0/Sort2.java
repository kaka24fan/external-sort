package uk.ac.cam.jp738.fjava.tick0;

import java.io.*;
import static java.lang.Math.toIntExact;


public class Sort2 implements ISort {

    private int m_maxMem = -1;
    public static final int MAX_MEM_DIVIDER = 5;
    private long m_fileLen = -1;


    @Override
    public void sort(String path1, String path2) throws java.io.IOException
    {
        m_maxMem = toIntExact(Runtime.getRuntime().maxMemory());
        //System.out.println("maxMem: " + m_maxMem);

        RandomAccessFile a1 = new RandomAccessFile(path1, "r");

        m_fileLen = a1.length()/4; // in ints, not bytes
    }

    private int mod(int n, int m)
    {
        return (n % m + m) % m;
    }
    private int mod(long n, int m)
    {
        return mod((int) n, m);
    }
}


/*
Quicksort finished off with insertsort

1. Choose the pivot
2. Go sequentially through the block filling the space in the other file from both ends
3. At the end put the pivot in the middle
   * can count equals to pivot and put them all in at the end


 */