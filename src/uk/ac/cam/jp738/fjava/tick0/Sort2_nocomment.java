package uk.ac.cam.jp738.fjava.tick0;

import java.io.*;
import static java.lang.Math.toIntExact;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


public class Sort2_nocomment implements ISort {

    private int m_maxMem = -1;
    public static final int MAX_MEM_DIVIDER = 10;
    private int m_fileLen = -1;
    private static final int BACKWARDS_WRITE_BUFFER_SIZE = 100; // ???
    private static final int SMALL_SORT_THRESHOLD = 6; // ???

    /**
     * path1, path2 do not change - path1 is always the path we want the final result on
     *
     */
    @Override
    public void sort(String path1, String path2) throws java.io.IOException
    {
        int lo, hi, piv, nPivEq, nPivGr, tmpint;
        LinkedList<Integer> backwardsWriteBuffer = new LinkedList<>();
        int backwardsWriteBufferElemCount;
        boolean swapNeeded = false;
        boolean backwardsBufferNotUsedYet;

        m_maxMem = toIntExact(Runtime.getRuntime().maxMemory());

        DataInputStream tmpDataInputStream;
        DataOutputStream tmpDataOutputStream;

        RandomAccessFile a1 = new RandomAccessFile(path1, "r");
        DataInputStream r1 = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a1.getFD()), m_maxMem/MAX_MEM_DIVIDER));
        RandomAccessFile a2 = new RandomAccessFile(path2, "r");
        DataInputStream r2 = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a2.getFD()), m_maxMem/MAX_MEM_DIVIDER));
        RandomAccessFile a11 = new RandomAccessFile(path1, "rw");
        DataOutputStream w11 = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(a11.getFD()), m_maxMem/MAX_MEM_DIVIDER));
        RandomAccessFile a12 = new RandomAccessFile(path1, "rw");
        DataOutputStream w12 = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(a12.getFD()), m_maxMem/MAX_MEM_DIVIDER));
        RandomAccessFile a21 = new RandomAccessFile(path2, "rw");
        DataOutputStream w21 = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(a21.getFD()), m_maxMem/MAX_MEM_DIVIDER));
        RandomAccessFile a22 = new RandomAccessFile(path2, "rw");
        DataOutputStream w22 = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(a22.getFD()), m_maxMem/MAX_MEM_DIVIDER));

        m_fileLen = toIntExact(a1.length()/4);

        if (m_fileLen <= 4) return;

        Queue<Integer> to_process = new LinkedList<>();
        to_process.add(0);
        to_process.add(m_fileLen);

        while (!to_process.isEmpty())
        {
            lo = to_process.poll();
            hi = to_process.poll();
            nPivEq = 1;
            nPivGr = 0;
            backwardsWriteBufferElemCount = 0;
            backwardsBufferNotUsedYet = true;

            a1.seek(lo * 4);

            a21.seek(lo * 4);
            a11.seek(lo * 4);

            a22.seek(hi * 4);

            w11 = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(a11.getFD()), m_maxMem/MAX_MEM_DIVIDER));
            // maybe can at least not create the FileOutputStream again?
            // but then maybe the overhead is negligable, I just don't know
            w21 = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(a21.getFD()), m_maxMem/MAX_MEM_DIVIDER));

            System.out.println("Polling: lo=" + lo + ", hi=" + hi);
            System.out.println("Currently main write file is " + (swapNeeded ? path1 : path2));

            if (hi-lo <= SMALL_SORT_THRESHOLD)
            {
                int[] small_array = new int[hi-lo];
                for (int i = 0; i < hi-lo; i++)
                    small_array[i] = r1.readInt();

                for (int j = 0; j < hi-lo-1; j++)
                {
                    tmpint = small_array[j];
                    int indMin = j;
                    int min = tmpint;
                    for (int i = j+1; i < hi-lo; i++)
                    {
                        if (small_array[i] < min)
                        {
                            min = small_array[i];
                            indMin = i;
                        }
                    }
                    small_array[j] = min;
                    small_array[indMin] = tmpint;
                }

                //////////////////////////////////////////////////////////////////
                System.out.print("Sorted array:");
                for (int n : small_array)
                    System.out.print(" " + n);
                System.out.print("\n");
                //////////////////////////////////////////////////////////////////

                System.out.print("BEFORE WRITING ARRAY:\n");
                Test.printTwoFiles(path1, path2);


                System.out.println("a11=" + a11.getFilePointer() + ", a21=" + a21.getFilePointer());
                for (int j = 0; j < hi-lo; j++)
                {
                    w11.writeInt(small_array[j]);
                    w21.writeInt(small_array[j]);
                }
                w11.flush();
                w21.flush();

                System.out.print("AFTER WRITING ARRAY:\n");
                Test.printTwoFiles(path1, path2);
            }
            else
            {
                piv = r1.readInt();

                for (int i = 1; i < hi - lo; i++)
                {
                    tmpint = r1.readInt();
                    if (piv < tmpint)
                    {
                        nPivGr++;
                        if (backwardsWriteBufferElemCount == BACKWARDS_WRITE_BUFFER_SIZE)
                        {
                            if (!backwardsBufferNotUsedYet)
                            {
                                a22.seek(a22.getFilePointer() - BACKWARDS_WRITE_BUFFER_SIZE * 4 * 2);
                            }

                            else
                            {
                                a22.seek(a22.getFilePointer() - BACKWARDS_WRITE_BUFFER_SIZE * 4 );
                                backwardsBufferNotUsedYet = false;
                            }

                            for (Integer n : backwardsWriteBuffer)
                            {
                                w22.writeInt(n);
                            }
                            w22.flush();

                            backwardsWriteBuffer.clear();
                            backwardsWriteBuffer.add(tmpint);
                            backwardsWriteBufferElemCount = 1;
                        }
                        else
                        {
                            backwardsWriteBuffer.add(tmpint);
                            backwardsWriteBufferElemCount++;
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

                w21.flush();
                a11.seek(a21.getFilePointer());
                System.out.println("Pivot writing: a11=" + a11.getFilePointer() + ", a21=" + a21.getFilePointer());
                for (int i = 0; i < nPivEq; i++)
                {
                    w21.writeInt(piv);
                    w11.writeInt(piv);
                }

                if (!backwardsBufferNotUsedYet)
                {
                    a22.seek(a22.getFilePointer() - (BACKWARDS_WRITE_BUFFER_SIZE + backwardsWriteBufferElemCount) * 4);
                }
                else
                {
                    a22.seek(a22.getFilePointer() - (backwardsWriteBufferElemCount) * 4);
                }

                for (Integer n : backwardsWriteBuffer)
                {
                    w22.writeInt(n);
                }

                w22.flush();
            }

            w21.flush();
            w22.flush();

            Test.printTwoFiles(path1, path2);

            if (hi-lo > SMALL_SORT_THRESHOLD)
            {
                if (lo + (hi - lo) - nPivGr - nPivEq - lo > 1)
                {
                    to_process.add(lo);
                    to_process.add(lo + (hi - lo) - nPivGr - nPivEq);
                }

                if (hi - (lo + (hi - lo) - nPivGr) > 1)
                {
                    to_process.add(lo + (hi - lo) - nPivGr);
                    to_process.add(hi);
                }
            }

            if (!to_process.isEmpty() && to_process.peek() < hi)
            {
                System.out.println("Swap references!");
                swapNeeded = !swapNeeded;

                tmpDataInputStream = r1;
                r1 = r2;
                r2 = tmpDataInputStream;

                tmpDataOutputStream = w11;
                w11 = w21;
                w21 = tmpDataOutputStream;

                tmpDataOutputStream = w12;
                w12 = w22;
                w22 = tmpDataOutputStream;

                RandomAccessFile tmp = a1;
                a1 = a2;
                a2 = tmp;

                tmp = a11;
                a11 = a21;
                a21 = tmp;

                tmp = a12;
                a12 = a22;
                a22 = tmp;
            }
        }

        w11.close();
        w12.close();
        w21.close();
        w22.close();
        r1.close();
        r2.close();
        a1.close();
        a2.close();
        a11.close();
        a12.close();
        a21.close();
        a22.close();

        if (swapNeeded)
        {
            Statics.swap(path2, path1);
        }
    }
}

/**
 Ok, when you have an open buffered reader and then you call seek() on the underlying RandomAccessFile
 the next reader.readInt() is not guaranteed to read from where you sought (seek-ed) to.
 What is the overhead to closing that reader and constructing a new one with the repositioned fd?

 Same with writers? Or not? Does flushing help? Doesn't seem to ehhhhhhhhhhhh
 */