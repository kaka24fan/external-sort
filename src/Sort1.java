import java.io.*;

public class Sort1 implements ISort {

	@Override
	public void sort(String path1, String path2) throws java.io.IOException {
        RandomAccessFile a1 = new RandomAccessFile(path1, "r");
        boolean switchRequired = false;
        int blockLen = 1;
        long fileLen = a1.length();
        while (blockLen < fileLen)
        {
            if (switchRequired)
                sort(path1, path2, blockLen, fileLen);
            else
                sort(path2, path1, blockLen, fileLen);
            switchRequired = !switchRequired;
            blockLen *= 2;
        }
    }

    public void swap(String from, String to, long fileLen) throws java.io.IOException
    {
        RandomAccessFile a = new RandomAccessFile(from, "r");
        RandomAccessFile b = new RandomAccessFile(to, "w");

        DataInputStream reader = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a.getFD())));
        DataOutputStream writer = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(b.getFD())));

        for (int i = 0; i < fileLen; i++)
        {
            writer.write(reader.read());
        }

        reader.close();
        writer.close();
        a.close();
        b.close();
    }

	public void sort(String path1, String path2, int blockLen, long fileLen) throws java.io.IOException {
		if (blockLen >= fileLen)
		    return;

	    RandomAccessFile a1 = new RandomAccessFile(path1, "r");
		RandomAccessFile a2 = new RandomAccessFile(path1, "r");
		RandomAccessFile b = new RandomAccessFile(path2, "w");

		long numBlocks = (fileLen+blockLen-1)/blockLen;
        long blocksToSkip = (numBlocks+1)/2;
		a2.seek(blocksToSkip*blockLen*4); // each number is 4 bytes

        DataInputStream reader1 = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a1.getFD())));
        DataInputStream reader2 = new DataInputStream(
                new BufferedInputStream(new FileInputStream(a2.getFD())));
        DataOutputStream writer = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(b.getFD())));


        int r1 = reader1.read();
        int r2 = reader2.read();

        int count1 = 0;
        int count2 = 0;

        int i = 1;

        for ( ; i <= numBlocks/2-1; i++)
        {
            while (true)
            {
                if (count1 >= blockLen*i && count2 >= blockLen*i)
                    break;
                else if (count1 >= blockLen*i)
                {
                    writer.write(r2);
                    r2 = reader2.read();
                }
                else if (count2 >= blockLen*i)
                {
                    writer.write(r1);
                    r1 = reader1.read();
                }
                else if (r1 < r2)
                {
                    writer.write(r1);
                    r1 = reader1.read();
                }
                else
                {
                    writer.write(r2);
                    r2 = reader2.read();
                }
            }
            count1 = -blockLen;
            count2 = -blockLen;
        }


        while (true)
        {
            if (count1 >= blockLen*i && (count2 >= blockLen*i || count2 >= fileLen))
                break;
            else if (count1 >= 0)
            {
                writer.write(r2);
                r2 = reader2.read();
            }
            else if (count2 >= blockLen*i || count2 >= fileLen)
            {
                writer.write(r1);
                r1 = reader1.read();
            }
            else if (r1 < r2)
            {
                writer.write(r1);
                r1 = reader1.read();
            }
            else
            {
                writer.write(r2);
                r2 = reader2.read();
            }
        }


        if (numBlocks % 2 == 1)
        {
            for (int j = 0; j < blockLen; j++)
            {
                writer.write(r2);
                r2 = reader2.read();
            }
        }


        reader1.close();
        reader2.close();
        writer.close();

        a1.close();
		a2.close();
		b.close();

	}
	
	
}
