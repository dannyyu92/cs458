import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;


public class KeyGen {
	public static void main(String argv[]) throws Exception {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		keyGen.initialize(1024);
		
		KeyPair pairA = keyGen.generateKeyPair();
		byte[] privA = pairA.getPrivate().getEncoded();
		byte[] pubA = pairA.getPublic().getEncoded();
		
		KeyPair pairB = keyGen.generateKeyPair();
		byte[] privB = pairB.getPrivate().getEncoded();
		byte[] pubB = pairB.getPublic().getEncoded();
		
		/*
		// Test pairs
		byte[] test = "123".getBytes();
		System.out.println(new String(test));
		
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, pairB.getPublic());
		byte[] encryptedKey = cipher.doFinal(test);
		
		cipher.init(Cipher.DECRYPT_MODE, pairB.getPrivate());
		byte[] decryptedKey = cipher.doFinal(encryptedKey);
		System.out.println(new String(decryptedKey));
		*/
		
		// Create files
		
		File privAkey = new File("privA.key");
		File pubAkey = new File("pubA.key");
		File privBkey = new File("privB.key");
		File pubBkey = new File("pubB.key");
		privAkey.createNewFile();
		pubAkey.createNewFile();
		privBkey.createNewFile();
		pubBkey.createNewFile();

		// Public keys
		FileOutputStream fos = new FileOutputStream("pubA.key");
		fos.write(pubA);
		fos.close();
		
		FileOutputStream fos2 = new FileOutputStream("pubB.key");
		fos2.write(pubB);
		fos2.close();
		
		// Private Keys
		FileOutputStream fos3 = new FileOutputStream("privA.key");
		fos3.write(privA);
		fos3.close();
		
		FileOutputStream fos4 = new FileOutputStream("privB.key");
		fos4.write(privB);
		fos4.close();
	}
}
