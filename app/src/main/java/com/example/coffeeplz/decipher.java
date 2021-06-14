package com.example.coffeeplz;

public class decipher {


	private int[] xTeaKey;
	/**
	 * Deciphers the tag
	 * @param payload Sectors 8,9,10,11,13
	 * @param uid tag uid
	 */
	public decipher (byte[][] payload, byte[] uid){

		xTeaKey = computeXteaKey (uid);

	}

	/**
	 * This methods computes the XTea key in function of the badge sector 0 block 0
	 * To test, a UID 0x5E698C02 should return a XTea key = 8b 19 5e 69 d8 46 8c 02 10 53 4c ca cb ff 26 57
	 * @param uid Uid of the tag
	 * @return The xTea Key
	 */
	private int[] computeXteaKey(byte[] uid) {
		int[] xTeaKey = new int[8];
		xTeaKey[0]=0x198B;
		xTeaKey[1]=((uid[0]<<8)|(uid[1]))&0xFFFF;
		xTeaKey[2]=0x46D8;
		xTeaKey[3]=((uid[2]<<8)|(uid[3]))&0xFFFF;
		xTeaKey[4]=0x5310;
		xTeaKey[5]=(xTeaKey[1] ^ 0xA312)&0xFFFF;
		xTeaKey[6]=0xFFCB;
		xTeaKey[7]=(xTeaKey[3] ^ 0x55AA)&0xFFFF;

		System.out.println("Computing xTeaKey");
		for (int i=0; i<8;i++){
			System.out.println(Long.toHexString(xTeaKey[i]));
		}

	return xTeaKey;
	}

	/**
	 * Get Xtea key
	 * @return the xTea key
	 */
	public int[] getxTeaKey() {
		return xTeaKey;
	}

}
