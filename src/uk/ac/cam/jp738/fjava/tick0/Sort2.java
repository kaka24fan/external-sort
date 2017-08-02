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
    private static final int SMALL_SORT_THRESHOLD = 4; // ???

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
        String pathFrom = path1;
        String pathTo = path2;
        boolean backwardsBufferNotUsedYet;

        m_maxMem = toIntExact(Runtime.getRuntime().maxMemory());

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //// Get file length. --------------------------------------------------------------------------------------- //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        RandomAccessFile tmp = new RandomAccessFile(path1, "r");
        m_fileLen = toIntExact(tmp.length()/4);
        if (m_fileLen <= 4) return; // trivial cases
        tmp.close();

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

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            //// Create readers, writers. --------------------------------------------------------------------------- //
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // forward reader FROM
            DataInputStream r1 = createReader(null, pathFrom, m_maxMem/MAX_MEM_DIVIDER, lo * 4);

            // forward writer FROM
            DataOutputStream w11 = createWriter(null, pathFrom, m_maxMem/MAX_MEM_DIVIDER, lo * 4);

            // forward writer TO
            DataOutputStream w21 = createWriter(null, pathTo, m_maxMem/MAX_MEM_DIVIDER, lo * 4);

            // backwards writer TO
            DataOutputStream w22 = createWriter(null, pathTo, m_maxMem/MAX_MEM_DIVIDER, hi * 4);

            int w22_position = hi * 4;
            int w21_position = lo * 4;

            if (ExternalSort.DEBUG)
            {
                System.out.println("Polling: lo=" + lo + ", hi=" + hi);
                System.out.println("Currently main write file is " + pathTo);
            }

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
                if (ExternalSort.DEBUG)
                {
                    System.out.print("Sorted array:");
                    for (int n : small_array)
                        System.out.print(" " + n);
                    System.out.print("\n");
                }
                //////////////////////////////////////////////////////////////////

                if (ExternalSort.DEBUG)
                {
                    System.out.print("BEFORE WRITING ARRAY:\n");
                    Test.printTwoFiles(path1, path2);
                }

                for (int j = 0; j < hi-lo; j++)
                {
                    w11.writeInt(small_array[j]);
                    w21.writeInt(small_array[j]);
                }
                w11.flush();
                w21.flush();

                if (ExternalSort.DEBUG)
                {
                    System.out.print("AFTER WRITING ARRAY:\n");
                    Test.printTwoFiles(path1, path2);
                }
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
                                w22 = createWriter(w22, pathTo, m_maxMem/MAX_MEM_DIVIDER, w22_position - BACKWARDS_WRITE_BUFFER_SIZE * 4 * 2);
                            }

                            else
                            {
                                w22.flush();
                                w22 = createWriter(w22, pathTo, m_maxMem/MAX_MEM_DIVIDER, w22_position - BACKWARDS_WRITE_BUFFER_SIZE * 4 );
                                backwardsBufferNotUsedYet = false;
                            }

                            for (Integer n : backwardsWriteBuffer)
                            {
                                w22.writeInt(n);
                            }
                            w22.flush();
                            w22_position -= BACKWARDS_WRITE_BUFFER_SIZE * 4;

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
                        w21_position += 4;
                    }
                    else // piv == tmp
                    {
                        nPivEq++;
                    }
                }

                ////////////////////////////////////////////////////////////////////////////////////////////////////////
                //// No need to update writer positions from here on.------------------------------------------------ //
                ////////////////////////////////////////////////////////////////////////////////////////////////////////

                w11.flush();
                w11 = createWriter(w11, pathFrom, m_maxMem/MAX_MEM_DIVIDER, w21_position);
                for (int i = 0; i < nPivEq; i++)
                {
                    w21.writeInt(piv);
                    w11.writeInt(piv);
                }

                if (!backwardsBufferNotUsedYet)
                {
                    w22.flush();
                    w22 = createWriter(w22, pathTo, m_maxMem/MAX_MEM_DIVIDER, w22_position - (BACKWARDS_WRITE_BUFFER_SIZE + backwardsWriteBufferElemCount) * 4);
                }
                else
                {
                    w22.flush();
                    w22 = createWriter(w22, pathTo, m_maxMem/MAX_MEM_DIVIDER, w22_position - (backwardsWriteBufferElemCount) * 4);
                }

                for (Integer n : backwardsWriteBuffer)
                {
                    w22.writeInt(n);
                }
                backwardsWriteBuffer.clear();

                w22.flush();
            }

            w21.flush();
            w22.flush();

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
                swapNeeded = !swapNeeded;

                String tmpString = pathFrom;
                pathFrom = pathTo;
                pathTo = tmpString;
            }

            w11.close();
            w21.close();
            w22.close();
            r1.close();

            if (ExternalSort.DEBUG)
            {
                System.out.print("END OF PASS:\n");
                Test.printTwoFiles(path1, path2);
            }
        }


        if (swapNeeded)
        {
            Statics.swap(path2, path1);
        }
    }

    private static DataOutputStream createWriter(DataOutputStream curr, String filePath, int bufferSize, int seekPos) throws java.io.IOException
    {
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
        raf.seek(seekPos);
        DataOutputStream ret =  new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(raf.getFD()), bufferSize)
        );
        if (curr != null)
            curr.close();
        return ret;
    }

    private static DataInputStream createReader(DataInputStream curr, String filePath, int bufferSize, int seekPos) throws java.io.IOException
    {
        RandomAccessFile raf = new RandomAccessFile(filePath, "r");
        raf.seek(seekPos);
        DataInputStream ret =  new DataInputStream(
                new BufferedInputStream(new FileInputStream(raf.getFD()), bufferSize)
        );
        if (curr != null)
            curr.close();
        return ret;
    }
}
