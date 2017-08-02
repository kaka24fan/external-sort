package uk.ac.cam.jp738.fjava.tick0;

import java.io.*;
import static java.lang.Math.toIntExact;

import java.util.LinkedList;
import java.util.Queue;


public class Sort2 implements ISort {

    private int m_maxMem = -1;
    public static final int MAX_MEM_DIVIDER = 20;
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

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //// Create rafs, readers, writers. ------------------------------------------------------------------------- //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        RandomAccessFile a1 = new RandomAccessFile(path1, "r");

        m_fileLen = toIntExact(a1.length()/4);
        if (m_fileLen <= 4) return; // trivial cases

        DataInputStream r1 = createReader(null, a1, m_maxMem/MAX_MEM_DIVIDER);
        RandomAccessFile a2 = new RandomAccessFile(path2, "r");
        DataInputStream r2 = createReader(null, a2, m_maxMem/MAX_MEM_DIVIDER);
        RandomAccessFile a11 = new RandomAccessFile(path1, "rw");
        DataOutputStream w11 = createWriter(null, a11, m_maxMem/MAX_MEM_DIVIDER);
        RandomAccessFile a12 = new RandomAccessFile(path1, "rw");
        DataOutputStream w12 = createWriter(null, a12, m_maxMem/MAX_MEM_DIVIDER);
        RandomAccessFile a21 = new RandomAccessFile(path2, "rw");
        DataOutputStream w21 = createWriter(null, a21, m_maxMem/MAX_MEM_DIVIDER);
        RandomAccessFile a22 = new RandomAccessFile(path2, "rw");
        DataOutputStream w22 = createWriter(null, a22, m_maxMem/MAX_MEM_DIVIDER);

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //// Create temp variables, doesn't matter what they're instantiated to. ------------------------------------ //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        RandomAccessFile tmpRandomAccessFile = a1;
        DataInputStream tmpDataInputStream = r1;
        DataOutputStream tmpDataOutputStream = w11;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //// -------------------------------------------------------------------------------------------------------- //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        Queue<Integer> toDoQueue = new LinkedList<>();
        toDoQueue.add(0);
        toDoQueue.add(m_fileLen);

        while (!toDoQueue.isEmpty())
        {
            lo = toDoQueue.poll();
            hi = toDoQueue.poll();
            nPivEq = 1;
            nPivGr = 0;
            backwardsWriteBufferElemCount = 0;
            backwardsBufferNotUsedYet = true;

            r1 = createReader(r1, a1, m_maxMem/MAX_MEM_DIVIDER, lo * 4);

            w21 = createWriter(w21, a21, m_maxMem/MAX_MEM_DIVIDER, lo * 4);

            w11 = createWriter(w11, a11, m_maxMem/MAX_MEM_DIVIDER, lo * 4);

            w22 = createWriter(w22, a22, m_maxMem/MAX_MEM_DIVIDER, hi * 4);

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

                //System.out.println("a11=" + a11.getFilePointer() + ", a21=" + a21.getFilePointer());
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
                                w22.flush();
                                w22 = createWriter(w22, a22, m_maxMem/MAX_MEM_DIVIDER, (int)a22.getFilePointer() - BACKWARDS_WRITE_BUFFER_SIZE * 4 * 2);
                            }

                            else
                            {
                                w22.flush();
                                w22 = createWriter(w22, a22, m_maxMem/MAX_MEM_DIVIDER, (int)a22.getFilePointer() - BACKWARDS_WRITE_BUFFER_SIZE * 4 );
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

                w11.flush();
                w11 = createWriter(w11, a11, m_maxMem/MAX_MEM_DIVIDER, (int)a21.getFilePointer());
                System.out.println("Pivot writing: a11=" + a11.getFilePointer() + ", a21=" + a21.getFilePointer());
                for (int i = 0; i < nPivEq; i++)
                {
                    w21.writeInt(piv);
                    w11.writeInt(piv);
                }

                if (!backwardsBufferNotUsedYet)
                {
                    w22.flush();
                    w22 = createWriter(w22, a22, m_maxMem/MAX_MEM_DIVIDER, (int)a22.getFilePointer() - (BACKWARDS_WRITE_BUFFER_SIZE + backwardsWriteBufferElemCount) * 4);
                }
                else
                {
                    w22.flush();
                    w22 = createWriter(w22, a22, m_maxMem/MAX_MEM_DIVIDER, (int)a22.getFilePointer() - (backwardsWriteBufferElemCount) * 4);
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
                    toDoQueue.add(lo);
                    toDoQueue.add(lo + (hi - lo) - nPivGr - nPivEq);
                }

                if (hi - (lo + (hi - lo) - nPivGr) > 1)
                {
                    toDoQueue.add(lo + (hi - lo) - nPivGr);
                    toDoQueue.add(hi);
                }
            }

            if (!toDoQueue.isEmpty() && toDoQueue.peek() < hi)
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

                tmpRandomAccessFile = a1;
                a1 = a2;
                a2 = tmpRandomAccessFile;

                tmpRandomAccessFile = a11;
                a11 = a21;
                a21 = tmpRandomAccessFile;

                tmpRandomAccessFile = a12;
                a12 = a22;
                a22 = tmpRandomAccessFile;
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
        tmpDataOutputStream.close();
        tmpDataInputStream.close();
        tmpRandomAccessFile.close();

        if (swapNeeded)
        {
            Statics.swap(path2, path1);
        }
    }

    private static DataOutputStream createWriter(DataOutputStream curr, RandomAccessFile raf, int bufferSize) throws java.io.IOException
    {
        DataOutputStream ret = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(raf.getFD()), bufferSize)
        );
        if (curr != null)
            curr.close();
        return ret;
    }

    private static DataInputStream createReader(DataInputStream curr, RandomAccessFile raf, int bufferSize) throws java.io.IOException
    {
        DataInputStream ret = new DataInputStream(
                new BufferedInputStream(new FileInputStream(raf.getFD()), bufferSize)
        );
        if (curr != null)
            curr.close();
        return ret;
    }

    private static DataOutputStream createWriter(DataOutputStream curr, RandomAccessFile raf, int bufferSize, int seekPos) throws java.io.IOException
    {
        raf.seek(seekPos);
        DataOutputStream ret =  new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(raf.getFD()), bufferSize)
        );
        if (curr != null)
            curr.close();
        return ret;
    }

    private static DataInputStream createReader(DataInputStream curr, RandomAccessFile raf, int bufferSize, int seekPos) throws java.io.IOException
    {
        raf.seek(seekPos);
        DataInputStream ret =  new DataInputStream(
                new BufferedInputStream(new FileInputStream(raf.getFD()), bufferSize)
        );
        if (curr != null)
            curr.close();
        return ret;
    }
}
