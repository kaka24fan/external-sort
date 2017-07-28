package uk.ac.cam.jp738.fjava.tick0;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ExternalSort {

    private final static boolean TESTING = true;
	public static void sort(String filenameA, String filenameB)
			 throws FileNotFoundException, IOException
	 {
		 //choose the sort implementation:

         //ISort sortingMethod = new Sort1();
         ISort sortingMethod = new Sort2();

         sortingMethod.sort(filenameA, filenameB);
	 }

	public static void main(String[] args) throws Exception {	
		if (TESTING)
        {
            Test.test();
            return;
        }

		sort(args[0], args[1]);
		System.out.println("The checksum is: "+checkSum(args[0]));
	}
	
	private static String byteToHex(byte b) {
		String r = Integer.toHexString(b);
		if (r.length() == 8) {
			return r.substring(6);
		}
		return r;
	}

	public static String checkSum(String f) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			DigestInputStream ds = new DigestInputStream(
					new FileInputStream(f), md);
			byte[] b = new byte[512];
			while (ds.read(b) != -1)
				;

			String computed = "";
			for(byte v : md.digest()) 
				computed += byteToHex(v);

			ds.close();
			return computed;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "<error computing checksum>";
	}

}