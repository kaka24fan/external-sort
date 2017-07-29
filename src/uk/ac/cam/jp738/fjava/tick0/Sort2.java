package uk.ac.cam.jp738.fjava.tick0;

import java.io.*;
import static java.lang.Math.toIntExact;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


public class Sort2 implements ISort {

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
        LinkedList<Integer> backwardsWriteBuffer = new LinkedList<Integer>();
        int backwardsWriteBufferElemCount;
        boolean swapNeeded = false;
        boolean backwardsBufferNotUsedYet;

        m_maxMem = toIntExact(Runtime.getRuntime().maxMemory());
        //Statics.log("maxMem: " + m_maxMem);

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

        m_fileLen = toIntExact(a1.length()/4); // in ints, not bytes

        if (m_fileLen <= 4) return;

        Queue<Integer> to_process = new LinkedList<Integer>();
        to_process.add(0);
        to_process.add(m_fileLen);

        while (!to_process.isEmpty())
        {
            lo = to_process.poll();
            hi = to_process.poll();
            Statics.log("\nPolling: lo=" + lo + ", hi=" + hi);
            nPivEq = 1; // this is the num of ints equal to pivot
            nPivGr = 0; // this is the num of ints greater than pivot
            backwardsWriteBufferElemCount = 0;
            backwardsBufferNotUsedYet = true;

            // reset reader:
            a1.seek(lo * 4);

            // set forward writer:
            a21.seek(lo * 4);

            // set backward writer:
            a22.seek(hi * 4);


            // if hi-lo too small do a different sort
            if (hi-lo <= SMALL_SORT_THRESHOLD)
            {
                int[] small_array = new int[hi-lo];
                for (int i = 0; i < hi-lo; i++)
                    small_array[i] = r1.readInt();

                // Select Sort:
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

                // write the sorted array:
                for (int j = 0; j < hi-lo-1; j++)
                {
                    w21.writeInt(small_array[j]);
                    //Statics.log("w" + small_array[j]);
                }

                // now write it to the other file as well. After that the lo-hi segment will never be touch again in
                // either of the files
                a11.seek(lo);
                for (int j = 0; j < hi-lo-1; j++)
                {
                    w11.writeInt(small_array[j]);
                    //Statics.log("w" + small_array[j]);
                }
                w11.flush();

            }
            else // hi-lo big enough, continue doing quicksort
            {
                // choose pivot:
                piv = r1.readInt();
                Statics.log("piv=" + piv);
                
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
                            if (!backwardsBufferNotUsedYet)
                            {
                                Statics.log("\n(1)Before writing: " + (a22.getFilePointer() - BACKWARDS_WRITE_BUFFER_SIZE * 4 * 2));
                                a22.seek(a22.getFilePointer() - BACKWARDS_WRITE_BUFFER_SIZE * 4 * 2); // * 2 because we've just written to it
                            }

                            else
                            {
                                Statics.log("\n(2)Before writing: " + (a22.getFilePointer() - BACKWARDS_WRITE_BUFFER_SIZE * 4));
                                a22.seek(a22.getFilePointer() - BACKWARDS_WRITE_BUFFER_SIZE * 4 );
                                backwardsBufferNotUsedYet = false;
                            }

                            // write out of the buffer:
                            for (Integer n : backwardsWriteBuffer)
                            {
                                w22.writeInt(n);
                                //Statics.log("w" + n);
                            }
                            w22.flush();

                            Statics.log("  After writing: " + a22.getFilePointer());

                            // clear the buffer:
                            backwardsWriteBuffer.clear();
                            backwardsWriteBuffer.add(tmpint);
                            backwardsWriteBufferElemCount = 1;
                        }
                        else // buffer has room
                        {
                            backwardsWriteBuffer.add(tmpint);
                            backwardsWriteBufferElemCount++;
                        }
                    }
                    else if (piv > tmpint)
                    {
                        w21.writeInt(tmpint);
                        //Statics.log("w" + tmpint);
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
                    //Statics.log("w" + piv);
                }

                // empty the backwardsWriteBuffer:
                if (!backwardsBufferNotUsedYet)
                {
                    Statics.log("\n(3)Before last write: " + (a22.getFilePointer() - (BACKWARDS_WRITE_BUFFER_SIZE + backwardsWriteBufferElemCount) * 4));
                    a22.seek(a22.getFilePointer() - (BACKWARDS_WRITE_BUFFER_SIZE + backwardsWriteBufferElemCount) * 4);
                }
                else
                {
                    Statics.log("\n(4)Before last write: " + (a22.getFilePointer() - (backwardsWriteBufferElemCount) * 4));
                    a22.seek(a22.getFilePointer() - (backwardsWriteBufferElemCount) * 4);
                    //backwardsBufferNotUsedYet = false; // this value would never be used anyway
                }

                for (Integer n : backwardsWriteBuffer)
                {
                    w22.writeInt(n);
                    //Statics.log("w" + n);
                }

                w22.flush();
                Statics.log("  After last write: " + a22.getFilePointer() + "\n");
            }


            // flush the writers
            // (now we are sure where their file accessors are) although this is irrelevant in the current implementation
            w21.flush();
            w22.flush();

            Test.printFile(swapNeeded ? path1 : path2);

            ////////////////
            ////////// prepare for next loop:
            ////////////

            if (hi-lo > SMALL_SORT_THRESHOLD) // otherwise this piece is sorted!
            {
                if (lo + (hi - lo) - nPivGr - nPivEq - lo > 1)
                {
                    to_process.add(lo); // 1st pair to be processed: next lo is this lo
                    to_process.add(lo + (hi - lo) - nPivGr - nPivEq); // 1st pair to be processed: next hi
                    Statics.log("Pushing: lo=" + lo + ", hi=" + (lo + (hi - lo) - nPivGr - nPivEq));
                }

                if (hi - (lo + (hi - lo) - nPivGr) > 1)
                {
                    to_process.add(lo + (hi - lo) - nPivGr); // 2nd pair to be processed: next lo
                    to_process.add(hi); // 2nd pair to be processed: next hi is this hi
                    Statics.log("Pushing: lo=" + (lo + (hi - lo) - nPivGr) + ", hi=" + hi);
                }
            }

            if (!to_process.isEmpty() && to_process.peek() < hi) // we got to the end of file and are about to loop around
            {
                swapNeeded = !swapNeeded;

                // swap readers:
                tmpDataInputStream = r1;
                r1 = r2;
                r2 = tmpDataInputStream;

                // swap forward writers:
                tmpDataOutputStream = w11;
                w11 = w21;
                w21 = tmpDataOutputStream;

                // swap backward writers:
                tmpDataOutputStream = w12;
                w12 = w22;
                w22 = tmpDataOutputStream;

                Object tmp = a1;
                a1 = a2;
                a2 = (RandomAccessFile) tmp;

                tmp = a11;
                a11 = a21;
                a21 = (RandomAccessFile) tmp;

                tmp = a12;
                a12 = a22;
                a22 = (RandomAccessFile) tmp;
            }
            else // we haven't gotten to the end of the file
            {

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
        //done
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