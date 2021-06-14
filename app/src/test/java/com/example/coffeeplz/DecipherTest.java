package com.example.coffeeplz;



/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class DecipherTest {

	public static void main (String[] args){

	byte[][] payload = new byte[5][];
	byte[] uid = {(byte)0x5E,(byte)0x69,(byte)0x8C,(byte)0x02};

	int test = Byte.toUnsignedInt(uid[0]);

	decipher testDecipher = new decipher(payload,uid);

	int[] resultKey = testDecipher.getxTeaKey();

	}
}