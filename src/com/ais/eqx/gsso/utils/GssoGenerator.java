package com.ais.eqx.gsso.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.RandomStringUtils;

import ec02.af.utils.Log;
;

public class GssoGenerator {

	public static String generateTransactionId(ArrayList<String> listTransactionId) {
		boolean isUnique = false;
		String uniqueNumber = "";
		while (!isUnique) {
			uniqueNumber = RandomStringUtils.random(18, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");

			if (listTransactionId.size() > 0) {
				if (!listTransactionId.contains(uniqueNumber)) {
					isUnique = true;
				}
			}
			else {
				isUnique = true;
			}
		}

		listTransactionId.add(uniqueNumber);

		return uniqueNumber;
	}

	public static String generateTransactionIdNumber(ArrayList<String> listTransactionId) {
		boolean isUnique = false;
		String uniqueNumber = "";
		while (!isUnique) {
			uniqueNumber = RandomStringUtils.random(18, "0123456789");

			if (listTransactionId.size() > 0) {
				if (!listTransactionId.contains(uniqueNumber)) {
					isUnique = true;
				}
			}
			else {
				isUnique = true;
			}
		}

		listTransactionId.add(uniqueNumber);

		return uniqueNumber;
	}

	public static String generateOrderReference(String nodeName, ArrayList<String> listOrderReference) {
		String orderRefNumber = "";
		boolean isUnique = false;
		while (!isUnique) {

			StringBuilder result = new StringBuilder();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");// 20151122114411
			Date resultdate = new Date(System.currentTimeMillis());
			String date = sdf.format(resultdate);

			/* add node name 2 digit and date format yyyyMMddHHmmss 14 digit */
			result.append(nodeName).append(date);

			/* New require add random number 2 digit */
			for (int idx = 0; idx < 2; ++idx) {
				result.append(randomInteger());
			}
			orderRefNumber = result.toString();
			if (listOrderReference.size() > 0) {
				if (!listOrderReference.contains(orderRefNumber)) {
					isUnique = true;
				}
			}
			else {
				isUnique = true;
			}
		}
		listOrderReference.add(orderRefNumber);

		Log.d("\n     ORDER REFERENCE GENERATE: " + orderRefNumber);

		return orderRefNumber;
	}

	public static String generateOrderReferenceToUSMP() {
		
		return RandomStringUtils.random(25, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
	}
	
	public static String generateOrderReferenceWSDL() {
		
		return RandomStringUtils.random(18, "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
	}
	
	public static String generateNumberByLength(String size, ArrayList<String> listNumber) {

		int loopSize = Integer.parseInt(size);
		String uniqueNumber = "";
		boolean isUnique = false;
		while (!isUnique) {

			StringBuilder result = new StringBuilder();

			for (int idx = 0; idx < loopSize; ++idx) {
				result.append(randomInteger());
			}
			uniqueNumber = result.toString();

			if (listNumber.size() > 0) {
				if (!listNumber.contains(uniqueNumber)) {
					isUnique = true;
				}
			}
			else {
				isUnique = true;
			}
		}

		listNumber.add(uniqueNumber);

		Log.d("\n     NUMBER GENERATE: " + uniqueNumber);

		return uniqueNumber;
	}

	private static int randomInteger() {
		int aStart = 0;
		int aEnd = 9;

		/** Initialize SecureRandom **/
		/* code GSSO version 1 */
		/*SecureRandom aRandom = null;
		try {
			aRandom = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (NoSuchAlgorithmException e) {
			Log.d("\n     SecureRandom NoSuchAlgorithmException : ON Generator Integer");
		}
		if (aStart > aEnd) {

		}*/
		
		long range = (long) aEnd - (long) aStart + 1;
		
//		long fraction = (long) (range * aRandom.nextDouble());
		
		double randomDouble =  Math.random();
		
		long fraction = (long) (range * randomDouble);
		int randomNumber = (int) (fraction + aStart);

		return randomNumber;
	}

	public static String encryptToken(String dataForEncrypt, String seedkey) {
		byte[] seed_key = seedkey.getBytes();

		SecretKeySpec keySpec = new SecretKeySpec(seed_key, "TripleDES");
		Cipher nCipher;
		String encryptTxt = null;
		try {
			nCipher = Cipher.getInstance("TripleDES");
			nCipher.init(Cipher.ENCRYPT_MODE, keySpec);
			byte[] cipherbyte = nCipher.doFinal(dataForEncrypt.getBytes("UTF8"));
			encryptTxt = new String(org.apache.commons.codec.binary.Base64.encodeBase64(cipherbyte), "UTF8");
		}
		catch (Exception e) {
			encryptTxt = "";
			Log.e(e.getMessage());
		}
		return encryptTxt;
	}

	public static String decryptToken(String passkey, String seedkey) {
		byte[] seed_key = seedkey.getBytes();
		// byte[] seed_key = (ConfigValues.SEEDKEY).getBytes();

		SecretKeySpec keySpec = new SecretKeySpec(seed_key, "TripleDES");
		Cipher nCipher;
		byte[] decryptedByte;
		String decryptedTxt = null;
		try {
			nCipher = Cipher.getInstance("TripleDES");
			nCipher.init(Cipher.DECRYPT_MODE, keySpec);
			byte[] encData = org.apache.commons.codec.binary.Base64.decodeBase64(passkey.getBytes());
			decryptedByte = nCipher.doFinal(encData);
			decryptedTxt = new String(decryptedByte, "UTF8");

		}
		catch (Exception e) {
			decryptedTxt = "";
			Log.e(e.getMessage());
		}
		return decryptedTxt;
	}

}
