import java.io.*;
import static java.lang.Math.toIntExact;

public class Sort1 implements ISort {

    private int m_maxMem = -1;
    public static final int MAX_MEM_DIVIDER = 100;
    private long m_fileLen = -1;


	@Override
	public void sort(String path1, String path2) throws java.io.IOException {

        m_maxMem = toIntExact(Runtime.getRuntime().maxMemory());
	    //System.out.println("maxMem: " + m_maxMem);

        RandomAccessFile a1 = new RandomAccessFile(path1, "r");
        int blockLen = 1; // 1 int (4 bytes)
        m_fileLen = a1.length()/4; // in ints, not bytes
        //System.out.println("fileLen: " + m_fileLen);
        a1.close();

        boolean swapRequired = false;

        while (blockLen < m_fileLen)
        {
            if (swapRequired)
                pass(path2, path1, blockLen);
            else
                pass(path1, path2, blockLen);
            swapRequired = !swapRequired;
            blockLen *= 2;
        }
        if (swapRequired)
            swap(path2, path1);
    }


	public void pass(String path1, String path2, int blockLen) throws java.io.IOException
    {
		if (blockLen >= m_fileLen)
		    return;

	    RandomAccessFile a1 = new RandomAccessFile(path1, "r");
		RandomAccessFile a2 = new RandomAccessFile(path1, "r");
		RandomAccessFile b = new RandomAccessFile(path2, "rw");

		long numBlocks = (m_fileLen+blockLen-1)/blockLen;
        long blocksToSkip = (numBlocks+1)/2;
		a2.seek(blocksToSkip*blockLen*4); // each int is 4 bytes
        long tmp = m_fileLen % blockLen;
        long count2Limit = (tmp == 0) ? blockLen : tmp; // this is how many ints reader2 can read in last block

        DataInputStream reader1 = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a1.getFD())));
        DataInputStream reader2 = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a2.getFD())));
        DataOutputStream writer = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(b.getFD())));



        int i = 1; // 1-based number of current block read by reader1

        for ( ; i <= numBlocks/2-1; i++)
        {
            boolean over1 = false;
            boolean over2 = false;

            int r1 = reader1.readInt();
            //System.out.println("r1 = " + r1);
            int r2 = reader2.readInt();
            //System.out.println("r2 = " + r2);

            int count1 = 1; // ints read from reader1
            int count2 = 1; // ints read from reader2

            while (true)
            {
                if (over1)
                {
                    //System.out.println("Writing " + r2);
                    writer.writeInt(r2);
                    if (count2 == blockLen)
                        break;
                    r2 = reader2.readInt();
                    count2++;
                }
                else if (over2)
                {
                    //System.out.println("Writing " + r1);
                    writer.writeInt(r1);
                    if (count1 == blockLen)
                        break;
                    r1 = reader1.readInt();
                    count1++;
                }
                else if (r1 < r2)
                {
                    //System.out.println("Writing " + r1);
                    writer.writeInt(r1);
                    if (count1 == blockLen)
                        over1 = true;
                    else
                    {
                        r1 = reader1.readInt();
                        count1++;
                    }
                }
                else
                {
                    //System.out.println("Writing " + r2);
                    writer.writeInt(r2);
                    if (count2 == blockLen)
                        over2 = true;
                    else
                    {
                        r2 = reader2.readInt();
                        count2++;
                    }
                }
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////
        // handle the last pair of blocks
        // and if the numBlocks is odd, also the middle block:
        //

        // get a reader set on the middle block:
        RandomAccessFile a3 = new RandomAccessFile(path1, "r");
        a3.seek((blocksToSkip-1)*blockLen*4); // only use a3 in the (blockLen odd) case!
        DataInputStream reader3 = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a3.getFD())));

        int intsToMergeIn = mod(-m_fileLen, blockLen); // we merge this many ints from middle block together with the
                                                       // last pair of blocks

        if (numBlocks % 2 == 0 || intsToMergeIn == 0)
        {
            boolean over1 = false;
            boolean over2 = false;

            int r1 = reader1.readInt();
            //System.out.println("r1 = " + r1);
            int r2 = reader2.readInt();
            //System.out.println("r2 = " + r2);

            int count1 = 1; // ints read from reader1
            int count2 = 1; // ints read from reader2

            // the last pair of blocks to be merged. Done separately to only have the check against m_fileLen when necessary
            while (true)
            {
                if (over1)
                {
                    //System.out.println("Writing " + r2);
                    writer.writeInt(r2);
                    if (count2 == blockLen || count2 == count2Limit) // first condition probably redundant
                        break;
                    r2 = reader2.readInt();
                    count2++;
                }
                else if (over2)
                {
                    //System.out.println("Writing " + r1);
                    writer.writeInt(r1);
                    if (count1 == blockLen)
                        break;
                    r1 = reader1.readInt();
                    count1++;
                }
                else if (r1 < r2)
                {
                    //System.out.println("Writing " + r1);
                    writer.writeInt(r1);
                    if (count1 == blockLen)
                        over1 = true;
                    else
                    {
                        r1 = reader1.readInt();
                        count1++;
                    }
                }
                else
                {
                    //System.out.println("Writing " + r2);
                    writer.writeInt(r2);
                    if (count2 == blockLen || count2 == count2Limit)
                        over2 = true;
                    else
                    {
                        r2 = reader2.readInt();
                        count2++;
                    }
                }
            }
        }
        else // if (numBlocks % 2 == 1 && intsToMergeIn > 0)
        {
            boolean over1 = false;
            boolean over2 = false;
            boolean over3 = false;

            int r1 = reader1.readInt();
            //System.out.println("r1 = " + r1);
            int r2 = reader2.readInt();
            //System.out.println("r2 = " + r2);
            int r3 = reader3.readInt();
            //System.out.println("r3 = " + r3);

            int count1 = 1; // ints read from reader1
            int count2 = 1; // ints read from reader2
            int count3 = 1; // ints read from reader3

            // the last pair of blocks to be merged together with a piece of the middle block of the size intsToMergeIn:

            while (true)
            {
                if (over1 && over3)
                {
                    //System.out.println("Writing " + r2);
                    writer.writeInt(r2);
                    if (count2 == blockLen || count2 == count2Limit) // first condition probably redundant
                        break;
                    r2 = reader2.readInt();
                    count2++;
                }
                else if (over2 && over3)
                {
                    //System.out.println("Writing " + r1);
                    writer.writeInt(r1);
                    if (count1 == blockLen)
                        break;
                    r1 = reader1.readInt();
                    count1++;
                }
                else if (over1 && over2)
                {
                    //System.out.println("Writing " + r3);
                    writer.writeInt(r3);
                    if (count3 == intsToMergeIn)
                        break;
                    r3 = reader3.readInt();
                    count3++;
                }
                else if (over3)
                {
                    if (r1 < r2)
                    {
                        //System.out.println("Writing " + r1);
                        writer.writeInt(r1);
                        if (count1 == blockLen)
                            over1 = true;
                        else
                        {
                            r1 = reader1.readInt();
                            count1++;
                        }
                    }
                    else // if (r2 <= r1)
                    {
                        //System.out.println("Writing " + r2);
                        writer.writeInt(r2);
                        if (count2 == blockLen || count2 == count2Limit)
                            over2 = true;
                        else
                        {
                            r2 = reader2.readInt();
                            count2++;
                        }
                    }
                }
                else if (over2)
                {
                    if (r1 < r3)
                    {
                        //System.out.println("Writing " + r1);
                        writer.writeInt(r1);
                        if (count1 == blockLen)
                            over1 = true;
                        else
                        {
                            r1 = reader1.readInt();
                            count1++;
                        }
                    }
                    else // if (r3 <= r1)
                    {
                        //System.out.println("Writing " + r3);
                        writer.writeInt(r3);
                        if (count3 == intsToMergeIn)
                            over3 = true;
                        else
                        {
                            r3 = reader3.readInt();
                            count3++;
                        }
                    }
                }
                else if (over1)
                {
                    if (r2 < r3)
                    {
                        //System.out.println("Writing " + r2);
                        writer.writeInt(r2);
                        if (count2 == blockLen || count2 == count2Limit)
                            over2 = true;
                        else
                        {
                            r2 = reader2.readInt();
                            count2++;
                        }
                    }
                    else // if (r3 <= r2)
                    {
                        //System.out.println("Writing " + r3);
                        writer.writeInt(r3);
                        if (count3 == intsToMergeIn)
                            over3 = true;
                        else
                        {
                            r3 = reader3.readInt();
                            count3++;
                        }
                    }
                }
                else // nothing is over
                {
                    int min = Math.min(Math.min(r1, r2), r3);
                    if (min == r1)
                    {
                        //System.out.println("Writing " + r1);
                        writer.writeInt(r1);
                        if (count1 == blockLen)
                            over1 = true;
                        else
                        {
                            r1 = reader1.readInt();
                            count1++;
                        }
                    }
                    else if (min == r2)
                    {
                        //System.out.println("Writing " + r2);
                        writer.writeInt(r2);
                        if (count2 == blockLen || count2 == count2Limit)
                            over2 = true;
                        else
                        {
                            r2 = reader2.readInt();
                            count2++;
                        }
                    }
                    else // (min == r3)
                    {
                        //System.out.println("Writing " + r3);
                        writer.writeInt(r3);
                        if (count3 == intsToMergeIn)
                            over3 = true;
                        else
                        {
                            r3 = reader3.readInt();
                            count3++;
                        }
                    }
                }
            }
        }
        // paste the remainder of the middle block at the end:
        if (numBlocks % 2 == 1)
        {
            for (int j = 0; j < blockLen - intsToMergeIn; j++)
            {
                writer.writeInt(reader3.readInt());
            }
        }

        writer.flush();
        writer.close();
        reader1.close();
        reader2.close();
        reader3.close();

        a1.close();
		a2.close();
		a3.close();
		b.close();

		//System.out.println("During sorting...:");
		//Test.printFile(path2);
	}

    private void swap(String from, String to) throws java.io.IOException
    {
        //System.out.println("Swapping...");
        RandomAccessFile a = new RandomAccessFile(from, "r");
        RandomAccessFile b = new RandomAccessFile(to, "rw");
        b.seek(0);

        DataInputStream reader = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a.getFD()), m_maxMem/MAX_MEM_DIVIDER));
        DataOutputStream writer = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(b.getFD()), m_maxMem/MAX_MEM_DIVIDER));

        for (int i = 0; i < m_fileLen; i++)
        {
            writer.writeInt(reader.readInt());
        }

        writer.flush();
        writer.close();
        reader.close();

        a.close();
        b.close();
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
