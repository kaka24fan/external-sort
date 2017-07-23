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
        long count2Limit = m_fileLen - blocksToSkip*blockLen; // this is how many ints the reader2 can read before EOF

        DataInputStream reader1 = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a1.getFD())));
        DataInputStream reader2 = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a2.getFD())));
        DataOutputStream writer = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(b.getFD())));

        int r1 = reader1.readInt();
        //System.out.println("r1 = " + r1);
        int r2 = reader2.readInt();
        //System.out.println("r2 = " + r2);

        int count1 = 1; // ints read from reader1
        int count2 = 1; // ints read from reader2

        boolean over1 = false;
        boolean over2 = false;

        int i = 1; // 1-based number of current block read by reader1

        for ( ; i <= numBlocks/2-1; i++)
        {
            while (true)
            {
                if (over1)
                {
                    //System.out.println("Writing " + r2);
                    writer.writeInt(r2);
                    if (count2 == blockLen*i)
                        break;
                    r2 = reader2.readInt();
                    count2++;
                }
                else if (over2)
                {
                    //System.out.println("Writing " + r1);
                    writer.writeInt(r1);
                    if (count1 == blockLen*i)
                        break;
                    r1 = reader1.readInt();
                    count1++;
                }
                else if (r1 < r2)
                {
                    //System.out.println("Writing " + r1);
                    writer.writeInt(r1);
                    if (count1 == blockLen*i)
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
                    if (count2 == blockLen*i)
                        over2 = true;
                    else
                    {
                        r2 = reader2.readInt();
                        count2++;
                    }
                }
            }
        }

        // the last pair of blocks to be merged. Done separately to only have the check against m_fileLen when necessary
        while (true)
        {
            if (over1)
            {
                //System.out.println("Writing " + r2);
                writer.writeInt(r2);
                if (count2 == blockLen*i || count2 == count2Limit) // first condition probably redundant
                    break;
                r2 = reader2.readInt();
                count2++;
            }
            else if (over2)
            {
                //System.out.println("Writing " + r1);
                writer.writeInt(r1);
                if (count1 == blockLen*i)
                    break;
                r1 = reader1.readInt();
                count1++;
            }
            else if (r1 < r2)
            {
                //System.out.println("Writing " + r1);
                writer.writeInt(r1);
                if (count1 == blockLen*i)
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
                if (count2 == blockLen*i || count2 == count2Limit)
                    over2 = true;
                else
                {
                    r2 = reader2.readInt();
                    count2++;
                }
            }
        }

        writer.flush();
        writer.close();
        reader1.close();
        reader2.close();

        a1.close();
		a2.close();
		b.close();

		//System.out.print("During sorting... ");
		//Test.printFile(path2, false);
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

}
