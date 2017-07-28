package uk.ac.cam.jp738.fjava.tick0;

import java.io.*;
import static java.lang.Math.toIntExact;

import java.util.LinkedList;
import java.util.Queue;

public class Sort2 implements ISort {

    private int m_maxMem = -1;
    public static final int MAX_MEM_DIVIDER = 5;
    private int m_fileLen = -1;
    private static final int BACKWARDS_WRITE_BUFFER_SIZE = 100; // ???

    @Override
    public void sort(String path1, String path2) throws java.io.IOException
    {
        int lo, hi, piv, nPivEq, nPivGr, tmpint;
        LinkedList<Integer> backwardsWriteBuffer = new LinkedList<Integer>();
        int backwardsWriteBufferElemCount = 0;

        m_maxMem = toIntExact(Runtime.getRuntime().maxMemory());
        //System.out.println("maxMem: " + m_maxMem);

        DataInputStream tmpDataInputStream;
        DataOutputStream tmpDataOutputStream;

        RandomAccessFile a1 = new RandomAccessFile(path1, "r");
        DataInputStream r1 = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a1.getFD())));
        RandomAccessFile a2 = new RandomAccessFile(path2, "r");
        DataInputStream r2 = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a2.getFD())));
        RandomAccessFile a11 = new RandomAccessFile(path1, "rw");
        DataOutputStream w11 = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(a11.getFD())));
        RandomAccessFile a12 = new RandomAccessFile(path1, "rw");
        DataOutputStream w12 = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(a12.getFD())));
        RandomAccessFile a21 = new RandomAccessFile(path2, "rw");
        DataOutputStream w21 = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(a21.getFD())));
        RandomAccessFile a22 = new RandomAccessFile(path2, "rw");
        DataOutputStream w22 = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(a22.getFD())));

        m_fileLen = toIntExact(a1.length()/4); // in ints, not bytes

        Queue<Integer> to_process = new LinkedList<Integer>();
        to_process.add(0);
        to_process.add(m_fileLen);

        while (true)
        {
            lo = to_process.poll();
            hi = to_process.poll();
            nPivEq = 1; // this is the num of ints equal to pivot
            nPivGr = 0; // this is the num of ints greater than pivot

            a21.seek(lo * 4);
            a22.seek((hi - 1) * 4);

            // if hi-lo too small do a different sort

            // choose pivot:
            piv = r1.readInt();

            // process the block:
            for (int i = 1; i < hi - lo; i++)
            {
                tmpint = r1.readInt();
                if (piv < tmpint) // predictive branching inefficiency?
                {
                    nPivGr++;
                    if (backwardsWriteBufferElemCount == BACKWARDS_WRITE_BUFFER_SIZE) // remember to write out of the buffer at the very end
                    {
                        // move the access point back:
                        a22.seek(a22.getFilePointer() - BACKWARDS_WRITE_BUFFER_SIZE * 4 * 2); // * 2 because we've just written to it

                        // write out of the buffer:
                        for (Integer n : backwardsWriteBuffer)
                            w22.writeInt(n);

                        // clear the buffer:
                        backwardsWriteBuffer.clear();
                        backwardsWriteBuffer.add(tmpint);
                        backwardsWriteBufferElemCount = 1;
                    }
                    else // buffer has room
                    {
                        backwardsWriteBuffer.add(tmpint);
                    }
                }
                else if (piv > tmpint)
                {
                    w21.writeInt(tmpint);
                }
                else // piv == tmp
                {
                    nPivEq++;
                }
            }

            // write the pivot nPivEq times:
            for (int i = 0; i < nPivEq; i++)
            {
                w21.writeInt(piv);
            }

            // empty the backwardsWriteBuffer:
            a22.seek(a22.getFilePointer() - (BACKWARDS_WRITE_BUFFER_SIZE + backwardsWriteBufferElemCount) * 4);
            for (Integer n : backwardsWriteBuffer)
                w22.writeInt(n);

            // flush the writers
            // now we are sure where their file accessors are
            w21.flush();
            w22.flush();

            ////////////////
            ////////// prepare for next loop:
            ////////////

            to_process.add(lo); // 1st pair to be processed: next lo is this lo
            to_process.add(lo + (hi - lo) - nPivGr - nPivEq); // 1st pair to be processed: next hi

            to_process.add(lo + (hi - lo) - nPivGr); // 2nd pair to be processed: next lo
            to_process.add(hi); // 2nd pair to be processed: next hi is this hi

            if (hi == m_fileLen) // we got to the end of file
            {
                // reset this reader:
                a1.seek(0);

                // swap readers:
                tmpDataInputStream = r1;
                r1 = r2;
                r2 = tmpDataInputStream;

                // reset the writers:
                a21.seek(0);
                a22.seek((m_fileLen - BACKWARDS_WRITE_BUFFER_SIZE) * 4);

                // swap forward writers:
                tmpDataOutputStream = w11;
                w11 = w21;
                w21 = tmpDataOutputStream;

                // swap backward writers:
                tmpDataOutputStream = w12;
                w12 = w22;
                w22 = tmpDataOutputStream;
            }
            else // we haven't gotten to the end of the file
            {
                // move forward writer:
                a21.seek()
            }
        }

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

Maintain a stack with next block indices (don't have to do if we don't tri-split in quicksort)

 */