import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;


public class BankServer {
    public static void main(String argv[]) throws Exception{
    	// Err checking
    	if (argv.length != 1) {
    		System.out.println("Incorrect # of args: java bank <port#>");
    		System.exit(0);
    	}
    	
    	try {
	    	// Socket stuff
	    	int portNum = Integer.parseInt(argv[0]);
	    	ServerSocket listen = new ServerSocket(portNum);
	    	listen.setReuseAddress(true);
	    	Socket conn = listen.accept(); 
	    	conn.setReuseAddress(true);
	    	//BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
	    	//PrintWriter out = new PrintWriter(conn.getOutputStream(),true);
	    	DataOutputStream out = new DataOutputStream(conn.getOutputStream());
	    	DataInputStream in = new DataInputStream(conn.getInputStream());
	    	
	    	// Public and Private Keys
	    	byte[] pubBkeyBytes = loadKey("pubB.key");
	    	byte[] privBkeyBytes = loadKey("privB.key");
	    	byte[] pubAkeyBytes = loadKey("pubA.key");
	    	PrivateKey privBkey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privBkeyBytes));
	    	
    		String cardNum = "";
    		String password = "";
	    	String inputLine;
    		byte[] encryptedKey = null;
    		byte[] encryptedCardPw = null;
    		byte[] decryptedCardPw = null;
    		byte[] Kbytes = null;
	    	
	    	while (true) {
	    		// Get E(Pub, K)
	    		int length = in.readInt();
	    		encryptedKey = parseInput(length, in);
	    		
    			// Decrypt
    			if (Kbytes == null) {
	    			Kbytes = decrypt("RSA", privBkey, encryptedKey);
    			}
    			
    			// Get E(K, card||password);
        		length = in.readInt();
        		encryptedCardPw = parseInput(length, in);
	    			
    			// Recreate DES Key "K"
    			DESKeySpec desKeySpec = new DESKeySpec(Kbytes);
    			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
    			SecretKey DESKey = keyFactory.generateSecret(desKeySpec);

    			// Decrypt card||password
    			if (decryptedCardPw == null) {
	    			decryptedCardPw = decrypt("DES", DESKey, encryptedCardPw);
    			}
    			
    			// Parse cardNum and password
    			int cardNumBytesLength = 8;
    			byte[] decryptedCard = new byte[cardNumBytesLength];
    		    System.arraycopy(decryptedCardPw, 0, decryptedCard, 0, cardNumBytesLength);
    		    byte[] decryptedPassword = new byte[decryptedCardPw.length - cardNumBytesLength];
	    		System.arraycopy(decryptedCardPw, cardNumBytesLength, decryptedPassword, 0, decryptedCardPw.length - cardNumBytesLength);
	    		//System.out.println(new String(decryptedCard));
	    		//System.out.println(new String(decryptedPassword));
	    		
    			// Load passwords
    			HashMap<String, String> passwordMap = loadPasswd("passwd");
    			//System.out.println(passwordMap.get(new String(decryptedCard)));
    			
    			// Hash the unencrypted password
    			MessageDigest md = MessageDigest.getInstance("MD5");
    			byte[] passwordMD5 = md.digest(decryptedPassword);
    			
    			// Compare password hashes
    			String passwordConfirmation = "";
    			if (new String(passwordMD5).equals(passwordMap.get(new String(decryptedCard)))) {
    				//System.out.println("Password correct");
    				passwordConfirmation = "1";
    	    		out.writeInt(passwordConfirmation.length());
    	    		out.writeBytes(passwordConfirmation);
    	    		out.flush();
    	    		System.out.println("User authenticated.");
    				
    	    		while (true) {
	    	    		// Get options
	    				length = in.readInt();
	            		String userOption = new String(parseInput(length, in));
	            		//System.out.println("userOption: " + userOption);
	
	            		// Withdraw
	            		if (userOption.equals("1")) {
	        				length = in.readInt();
	                		String checkingOrSavings = new String(parseInput(length, in));
	                		//System.out.println(checkingOrSavings);
	                		// Checkings
	                		String amount = "";
	                		String response = "";
	                		if (checkingOrSavings.equals("0")) {
	            				length = in.readInt();
	                    		amount = new String(parseInput(length, in));
	                    		//System.out.println(amount);
	                    		response = withdraw(Integer.parseInt(amount), new String(decryptedCard), checkingOrSavings);
	                    		//System.out.println(response);
	                    		out.writeInt(response.length());
		        	    		out.write(response.getBytes());
		        	    		out.flush();
	                		}
	                		// Savings
	                		else if (checkingOrSavings.equals("1")) {
	                			length = in.readInt();
	                			amount = new String(parseInput(length, in));
	                			//System.out.println(amount);
	                    		response = withdraw(Integer.parseInt(amount), new String(decryptedCard), checkingOrSavings);
	                    		//System.out.println(response);
	                    		out.writeInt(response.length());
		        	    		out.write(response.getBytes());
		        	    		out.flush();
	                		}
	            		}
	            		// Check Balance
	            		else if (userOption.equals("2")) {
	            			String balances = getBalance(new String(decryptedCard));
	            			byte[] encryptedBalance = encrypt("DES", DESKey, balances.getBytes());
	        	    		out.writeInt(encryptedBalance.length);
	        	    		out.write(encryptedBalance);
	        	    		out.flush();
	            		}
	            		else {
	            			conn.setSoLinger(true, 0);
	            			in.close();
	            			out.close();
	            			conn.close();
	            	    	listen = new ServerSocket(portNum);
	            	    	conn = listen.accept(); 
	            	    	out = new DataOutputStream(conn.getOutputStream());
	            	    	in = new DataInputStream(conn.getInputStream());
	            	    	break;
	            			//System.exit(0);
	            		}
    	    		}
    			}
    			else {
    				//System.out.println("Password incorrect");
    				passwordConfirmation = "0";
    	    		out.writeInt(passwordConfirmation.length());
    	    		out.writeBytes(passwordConfirmation);
    	    		out.flush();
    	    		decryptedCardPw = null;
    	    		continue;
    			}
    		}
    	} catch(Exception e) {
    		e.printStackTrace();
    		System.out.println("Correct useage: java bank <port#>");
    	}
    } 
    
	public static byte[] parseInput(int length, DataInputStream in) throws IOException {
		byte[] message = new byte[length];
		if (length > 0) {
			in.readFully(message);
		}
		return message;
	}
    
	public static byte[] encrypt(String algo, Key key, byte[] data) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(algo);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(data);
	}
	
	public static byte[] decrypt(String algo, Key key, byte[] data) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(algo);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(data);
	}
    
    public static byte[] loadKey(String keyName) throws IOException {
    	FileInputStream keyfis = new FileInputStream(keyName);
    	byte[] encodedKey = new byte[keyfis.available()];  
    	keyfis.read(encodedKey);

    	keyfis.close();
    	return encodedKey;
    }
    
    public static String withdraw(int amount, String cardNum, String checkingOrSavings) throws IOException {
    	String line;
    	String[] splitLine;
    	String newBalance = "";
    	String result = "0";
    	String tempLine = "";
		
		BufferedReader br = new BufferedReader(new FileReader("balance"));
		PrintWriter out = new PrintWriter("balance2");
    	// Get balance
    	while ((line = br.readLine()) != null) {
    	   splitLine = line.split(":");
    	   if (splitLine[0].equals(cardNum)) {
    		   tempLine = line;
    	   }
    	   else {
    		   out.println(line);
    	   }
		}
    	br.close();
    	
    	String[] splitTempLine = tempLine.split(":");
    	System.out.println(tempLine);
    	// checkings
    	if (checkingOrSavings.equals("0")) {
    		int orgCheckings = Integer.parseInt(splitTempLine[1]);
    		if (amount <= orgCheckings) {
    			result = "1";
    			newBalance = String.valueOf(orgCheckings - amount);
    			out.println(cardNum + ":" + newBalance + ":" + splitTempLine[2]);
    		}
    		else {
    			out.println(cardNum + ":" + orgCheckings + ":" + splitTempLine[2]);
    		}
    	}
    	// savings
    	else {
    		int orgSavings = Integer.parseInt(splitTempLine[2]);
    		if (amount <= orgSavings) {
    			result = "1";
    			newBalance = String.valueOf(orgSavings - amount);
    			out.println(cardNum + ":" + splitTempLine[1] + ":" + newBalance);
    		}
    		else {
    			out.println(cardNum + ":" + splitTempLine[1] + ":" + orgSavings);
    		}
    	}
    	out.close();
    	File oldFile = new File("balance");
    	File newFile = new File("balance2");
    	newFile.renameTo(oldFile);
    	return result;
    }
    
    public static void updateBalance(String cardNum, String newBalance, String oldBalance) throws IOException {
    	BufferedReader br = new BufferedReader(new FileReader("balance"));
    	String line;
    	String[] splitLine;
    	while ((line = br.readLine()) != null) {
    	   splitLine = line.split(":");
    	}
    	br.close();
    }
    
    public static String getBalance(String cardNum) throws IOException {
    	BufferedReader br = new BufferedReader(new FileReader("balance"));
    	String line;
    	String[] splitLine;
    	String result = null;
    	while ((line = br.readLine()) != null) {
    		splitLine = line.split(":");
    		if (splitLine[0].equals(cardNum)) {
    			result = line;
    		}
    	}
    	br.close();
    	//System.out.println(passwordMap.get("11111111"));
    	//System.out.println(passwordMap.get("00000000"));
    	return result;
    }
    
    public static HashMap<String, String> loadPasswd(String fileName) throws IOException {
    	BufferedReader br = new BufferedReader(new FileReader(fileName));
    	String line;
    	String[] splitLine;
    	HashMap<String, String> passwordMap = new HashMap<String, String>();
    	while ((line = br.readLine()) != null) {
    	   splitLine = line.split(":");
    	   passwordMap.put(splitLine[0], splitLine[1]);
    	}
    	br.close();
    	//System.out.println(passwordMap.get("11111111"));
    	//System.out.println(passwordMap.get("00000000"));
    	return passwordMap;
    }
}